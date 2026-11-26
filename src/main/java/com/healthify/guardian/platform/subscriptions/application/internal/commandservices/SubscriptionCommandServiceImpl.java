package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;

import com.healthify.guardian.platform.subscriptions.application.commandservices.SubscriptionCommandService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ActivateSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.CancelSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ExpireSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCancellationCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ApplyPlanChangeCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RenewSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EntitlementSetCommandService;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RevokeAllEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.BiConsumer;

@Service
@Transactional
public class SubscriptionCommandServiceImpl
        implements SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final EntitlementSetCommandService entitlementSetCommandService;

    public SubscriptionCommandServiceImpl(
            SubscriptionRepository subscriptionRepository,
            PlanRepository planRepository,
            EntitlementSetCommandService entitlementSetCommandService
    ) {
        this.subscriptionRepository =
                subscriptionRepository;

        this.planRepository =
                planRepository;

        this.entitlementSetCommandService =
                entitlementSetCommandService;
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            RequestSubscriptionCommand command
    ) {

        final String operation =
                "request-subscription";

        try {

            SubscriberId subscriberId =
                    new SubscriberId(
                            command.subscriberId()
                    );

            PlanId planId =
                    new PlanId(
                            command.planId()
                    );

            var plan =
                    planRepository.findById(
                            planId
                    );

            if (plan.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Plan",
                                planId.value().toString()
                        )
                );
            }

            if (!plan.get().isActive()) {

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Selected subscription plan is not active"
                        )
                );
            }

            subscriptionRepository
                    .acquireSubscriberLock(
                            subscriberId
                    );

            var existingSubscription =
                    subscriptionRepository
                            .findActiveBySubscriberId(
                                    subscriberId
                            );

            if (existingSubscription.isPresent()) {

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Subscriber already has an active subscription"
                        )
                );
            }

            var pendingSubscription =
                    subscriptionRepository
                            .findPendingBySubscriberId(
                                    subscriberId
                            );

            Instant now =
                    Instant.now();

            if (pendingSubscription.isPresent()) {

                Subscription pending =
                        pendingSubscription.get();

                if (!pending.getPlanId().equals(planId)) {

                    return Result.failure(
                            ApplicationError.businessRuleViolation(
                                    operation,
                                    "Subscriber already has a pending subscription for another plan"
                            )
                    );
                }

                if (!plan.get().requiresPayment()) {

                    return activateFreeSubscription(
                            pending,
                            plan.get(),
                            now
                    );
                }

                return Result.success(pending);
            }

            Subscription subscription =
                    new Subscription(
                            subscriberId,
                            planId,
                            now
                    );

            Subscription saved =
                    subscriptionRepository
                            .save(subscription);

            if (!plan.get().requiresPayment()) {

                return activateFreeSubscription(
                        saved,
                        plan.get(),
                        now
                );
            }

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }

    private Result<Subscription, ApplicationError>
    activateFreeSubscription(
            Subscription subscription,
            Plan plan,
            Instant now
    ) {

        final String operation =
                "activate-free-subscription";

        Instant periodEnd =
                calculatePeriodEnd(
                        now,
                        plan.getBillingCycle()
                );

        subscription.activate(
                subscription.getPlanId(),
                now,
                periodEnd,
                now
        );

        var entitlementResult =
                entitlementSetCommandService.handle(
                        new SyncEntitlementsFromPlanCommand(
                                subscription.getId().value(),
                                subscription.getPlanId().value()
                        )
                );

        if (entitlementResult.isFailure()) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            "Unable to synchronize subscription entitlements"
                    )
            );
        }

        Subscription saved =
                subscriptionRepository.save(
                        subscription
                );

        return Result.success(saved);
    }

    private Instant calculatePeriodEnd(
            Instant periodStart,
            BillingCycle billingCycle
    ) {

        return switch (billingCycle) {

            case MONTHLY ->
                    periodStart
                            .atZone(ZoneOffset.UTC)
                            .plusMonths(1)
                            .toInstant();

            case ANNUAL ->
                    periodStart
                            .atZone(ZoneOffset.UTC)
                            .plusYears(1)
                            .toInstant();
        };
    }

    private static void markRollbackOnlyIfTransactionActive() {

        if (TransactionSynchronizationManager
                .isActualTransactionActive()) {

            TransactionAspectSupport
                    .currentTransactionStatus()
                    .setRollbackOnly();
        }
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            ActivateSubscriptionCommand command
    ) {

        final String operation =
                "activate-subscription";

        try {

            SubscriptionId subscriptionId =
                    new SubscriptionId(
                            command.subscriptionId()
                    );

            PlanId planId =
                    new PlanId(
                            command.planId()
                    );

            subscriptionRepository
                    .acquireSubscriberLockForSubscription(
                            subscriptionId
                    );

            var subscription =
                    subscriptionRepository
                            .findById(
                                    subscriptionId
                            );

            if (subscription.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriptionId
                                        .value()
                                        .toString()
                        )
                );
            }

            var plan =
                    planRepository
                            .findById(
                                    planId
                            );

            if (plan.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Plan",
                                planId
                                        .value()
                                        .toString()
                        )
                );
            }

            if (!plan.get().isActive()) {

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Selected subscription plan is not active"
                        )
                );
            }

            Instant now =
                    Instant.now();

            Subscription current =
                    subscription.get();

            current.activate(
                    planId,
                    command.periodStart(),
                    command.periodEnd(),
                    now
            );

            var entitlementResult =
                    entitlementSetCommandService.handle(
                            new SyncEntitlementsFromPlanCommand(
                                    current.getId().value(),
                                    planId.value()
                            )
                    );

            if (entitlementResult.isFailure()) {

                markRollbackOnlyIfTransactionActive();

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Unable to synchronize subscription entitlements"
                        )
                );
            }

            Subscription saved =
                    subscriptionRepository
                            .save(current);

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            RequestSubscriptionCancellationCommand command
    ) {
        return applyToExistingSubscription(
                command.subscriptionId(),
                "request-subscription-cancellation",
                Subscription::requestCancellation,
                false
        );
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            CancelSubscriptionCommand command
    ) {
        return applyToExistingSubscription(
                command.subscriptionId(),
                "cancel-subscription",
                Subscription::cancel,
                true
        );
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            ApplyPlanChangeCommand command
    ) {

        final String operation =
                "apply-plan-change";

        try {

            SubscriptionId subscriptionId =
                    new SubscriptionId(
                            command.subscriptionId()
                    );

            PlanId newPlanId =
                    new PlanId(
                            command.newPlanId()
                    );

            subscriptionRepository
                    .acquireSubscriberLockForSubscription(
                            subscriptionId
                    );

            var subscription =
                    subscriptionRepository.findById(
                            subscriptionId
                    );

            if (subscription.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriptionId
                                        .value()
                                        .toString()
                        )
                );
            }

            var newPlan =
                    planRepository.findById(
                            newPlanId
                    );

            if (newPlan.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Plan",
                                newPlanId
                                        .value()
                                        .toString()
                        )
                );
            }

            if (!newPlan.get().isActive()) {

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Selected subscription plan is not active"
                        )
                );
            }

            if (newPlan.get().requiresPayment()) {

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Changing to a paid plan requires a confirmed payment"
                        )
                );
            }

            Subscription current =
                    subscription.get();

            current.changePlan(
                    newPlanId,
                    Instant.now()
            );

            var entitlementResult =
                    entitlementSetCommandService.handle(
                            new SyncEntitlementsFromPlanCommand(
                                    current.getId().value(),
                                    newPlanId.value()
                            )
                    );

            if (entitlementResult.isFailure()) {

                markRollbackOnlyIfTransactionActive();

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Unable to synchronize subscription entitlements"
                        )
                );
            }

            Subscription saved =
                    subscriptionRepository.save(
                            current
                    );

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            ExpireSubscriptionCommand command
    ) {
        return applyToExistingSubscription(
                command.subscriptionId(),
                "expire-subscription",
                Subscription::expire,
                true
        );
    }

    private Result<Subscription, ApplicationError>
    applyToExistingSubscription(
            UUID rawSubscriptionId,
            String operation,
            BiConsumer<Subscription, Instant> action,
            boolean revokeEntitlements
    ){

        try {

            SubscriptionId subscriptionId =
                    new SubscriptionId(
                            rawSubscriptionId
                    );

            subscriptionRepository
                    .acquireSubscriberLockForSubscription(
                            subscriptionId
                    );

            var subscription =
                    subscriptionRepository
                            .findById(
                                    subscriptionId
                            );

            if (subscription.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriptionId
                                        .value()
                                        .toString()
                        )
                );
            }

            Subscription current =
                    subscription.get();

            Instant now =
                    Instant.now();

            action.accept(
                    current,
                    now
            );

            if (revokeEntitlements) {

                var entitlementResult =
                        entitlementSetCommandService.handle(
                                new RevokeAllEntitlementsCommand(
                                        current.getId().value()
                                )
                        );

                if (entitlementResult.isFailure()) {

                    markRollbackOnlyIfTransactionActive();

                    return Result.failure(
                            ApplicationError.businessRuleViolation(
                                    operation,
                                    "Unable to revoke subscription entitlements"
                            )
                    );
                }
            }

            Subscription saved =
                    subscriptionRepository
                            .save(current);

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }

    private static String resolve(
            RuntimeException exception
    ) {
        return resolve(
                exception.getMessage()
        );
    }

    private static String resolve(
            String messageKey
    ) {
        return MessageResolver.resolveOrDefault(
                messageKey,
                messageKey
        );
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            RenewSubscriptionCommand command
    ) {

        final String operation =
                "renew-subscription";

        try {

            SubscriptionId subscriptionId =
                    new SubscriptionId(
                            command.subscriptionId()
                    );

            subscriptionRepository
                    .acquireSubscriberLockForSubscription(
                            subscriptionId
                    );

            var subscription =
                    subscriptionRepository.findById(
                            subscriptionId
                    );

            if (subscription.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriptionId
                                        .value()
                                        .toString()
                        )
                );
            }

            Subscription current =
                    subscription.get();

            current.renew(
                    command.newPeriodEnd(),
                    Instant.now()
            );

            Subscription saved =
                    subscriptionRepository.save(
                            current
                    );

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            markRollbackOnlyIfTransactionActive();

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }
}