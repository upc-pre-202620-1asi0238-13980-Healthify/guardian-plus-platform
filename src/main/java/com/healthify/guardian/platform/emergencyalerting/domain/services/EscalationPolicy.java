package com.healthify.guardian.platform.emergencyalerting.domain.services;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertChannelSetting;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryTarget;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Resolves who must be notified at each level of the escalation chain and through which channels,
 * and which level an unacknowledged alert must move to next.
 */
@Service
public class EscalationPolicy {

    /**
     * Resolves the next level an alert must move to once its acknowledgement timeout expires.
     *
     * <ul>
     *   <li>From {@code PRIMARY}: to {@code SECONDARY} when escalation is enabled and there are
     *       secondary contacts; otherwise the chain is exhausted and the alert is broadcast.</li>
     *   <li>From {@code SECONDARY}: broadcast (<em>Critical Broadcast Fallback</em>).</li>
     *   <li>From {@code BROADCAST}, for severities that never escalate, or for an alert no longer
     *       awaiting acknowledgement: nothing.</li>
     * </ul>
     *
     * @param alert          the alert whose acknowledgement timeout expired
     * @param settings       the Fragile Citizen's alerting configuration
     * @param activeContacts the Fragile Citizen's active emergency contacts, in any order
     * @return the next level, or empty if the alert must not escalate any further
     */
    public Optional<RecipientLevel> resolveNextLevel(
            Alert alert, AlertSettings settings, List<EmergencyContact> activeContacts) {
        if (!alert.getSeverity().allowsEscalation() || !alert.isAwaitingAcknowledgement()) {
            return Optional.empty();
        }
        var current = alert.currentRecipientLevel();
        if (current == RecipientLevel.PRIMARY) {
            var hasSecondaryContacts = activeContacts.stream().filter(EmergencyContact::isActive).count() > 1;
            return Optional.of(settings.isEscalationEnabled() && hasSecondaryContacts
                    ? RecipientLevel.SECONDARY
                    : RecipientLevel.BROADCAST);
        }
        if (current == RecipientLevel.SECONDARY) {
            return Optional.of(RecipientLevel.BROADCAST);
        }
        return Optional.empty();
    }

    /**
     * Resolves the delivery targets of one level of the escalation chain.
     *
     * <p>Contacts are ranked by priority among the active ones, so gaps left by deactivated contacts
     * do not matter: {@code PRIMARY} is the first active contact, {@code SECONDARY} every other
     * one, and {@code BROADCAST} all of them. Each contact is notified through their enabled
     * channels, falling back to {@code IN_APP} when they have none; {@code SMS} is always added
     * for critical alerts and for broadcasts.</p>
     *
     * @param level           the level to resolve
     * @param severity        the alert's severity
     * @param contacts        the Fragile Citizen's emergency contacts
     * @param channelSettings the channel settings of those contacts' users
     * @return the targets to deliver to; empty if the level has no contacts
     */
    public List<DeliveryTarget> resolveRecipients(
            RecipientLevel level,
            Severity severity,
            List<EmergencyContact> contacts,
            List<AlertChannelSetting> channelSettings) {
        var ranked = contacts.stream()
                .filter(EmergencyContact::isActive)
                .sorted(Comparator.comparing(EmergencyContact::getPriorityOrder))
                .toList();
        var recipients = switch (level) {
            case PRIMARY -> ranked.stream().limit(1).toList();
            case SECONDARY -> ranked.stream().skip(1).toList();
            case BROADCAST -> ranked;
        };
        var smsRequired = severity.requiresSmsBackup() || level == RecipientLevel.BROADCAST;

        var targets = new ArrayList<DeliveryTarget>();
        for (var contact : recipients) {
            var channels = EnumSet.noneOf(NotificationChannel.class);
            channelSettings.stream()
                    .filter(setting -> setting.getUserId().equals(contact.getUserId()) && setting.isEnabled())
                    .forEach(setting -> channels.add(setting.getChannel()));
            if (channels.isEmpty()) {
                channels.add(NotificationChannel.IN_APP);
            }
            if (smsRequired) {
                channels.add(NotificationChannel.SMS);
            }
            channels.forEach(channel -> targets.add(new DeliveryTarget(contact.getUserId(), channel)));
        }
        return List.copyOf(targets);
    }
}
