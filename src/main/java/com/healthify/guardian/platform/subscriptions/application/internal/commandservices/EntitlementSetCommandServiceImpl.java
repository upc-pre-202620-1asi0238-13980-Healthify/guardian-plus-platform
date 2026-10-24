package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EntitlementSetCommandService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.EntitlementDefinition;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RevokeAllEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.UpdateEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementSetRepository;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementCatalogRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EntitlementSetCommandServiceImpl
        implements EntitlementSetCommandService {

    private final EntitlementSetRepository entitlementSetRepository;

    private final PlanRepository planRepository;

    private final EntitlementCatalogRepository
            entitlementCatalogRepository;

    public EntitlementSetCommandServiceImpl(
            EntitlementSetRepository entitlementSetRepository,
            PlanRepository planRepository,
            EntitlementCatalogRepository entitlementCatalogRepository
    ) {
        this.entitlementSetRepository =
                entitlementSetRepository;

        this.planRepository =
                planRepository;

        this.entitlementCatalogRepository =
                entitlementCatalogRepository;
    }

    @Override
    public Result<EntitlementSet, ApplicationError> handle(
            UpdateEntitlementsCommand command
    ) {

        final String operation =
                "update-entitlements";

        try {

            SubscriptionId subscriptionId =
                    new SubscriptionId(
                            command.subscriptionId()
                    );

            var existing =
                    entitlementSetRepository
                            .findBySubscriptionId(
                                    subscriptionId
                            );

            if (existing.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriptionId
                                        .value()
                                        .toString()
                        )
                );
            }

            Set<Entitlement> entitlements =
                    command.entitlements() == null
                            ? Set.of()
                            : command.entitlements()
                            .stream()
                            .map(
                                    EntitlementSetCommandServiceImpl
                                            ::toEntitlement
                            )
                            .collect(
                                    Collectors.toSet()
                            );

            EntitlementSet entitlementSet =
                    existing.get();

            Instant now = Instant.now();

            entitlementSet.replaceWith(
                    entitlements,
                    now
            );
            EntitlementSet saved =
                    entitlementSetRepository
                            .save(
                                    entitlementSet
                            );

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }
    @Override
    public Result<EntitlementSet, ApplicationError> handle(
            SyncEntitlementsFromPlanCommand command
    ) {

        final String operation =
                "sync-entitlements-from-plan";

        try {

            SubscriptionId subscriptionId =
                    new SubscriptionId(
                            command.subscriptionId()
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

            var existingSet =
                    entitlementSetRepository
                            .findBySubscriptionId(
                                    subscriptionId
                            );

            if (existingSet.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriptionId
                                        .value()
                                        .toString()
                        )
                );
            }

            var entitlementCodes =
                    plan.get()
                            .getEntitlementCodes();

            Set<Entitlement> entitlements =
                    entitlementCatalogRepository
                            .findAllByCodes(
                                    entitlementCodes
                            );

            /*
             * A plan must not reference entitlement codes
             * that do not exist in the catalog.
             */
            if (entitlements.size()
                    != entitlementCodes.size()) {

                return Result.failure(
                        ApplicationError.businessRuleViolation(
                                operation,
                                "Subscription plan references unknown entitlements"
                        )
                );
            }

            EntitlementSet entitlementSet =
                    existingSet.get();

            entitlementSet.replaceWith(
                    entitlements,
                    Instant.now()
            );

            EntitlementSet saved =
                    entitlementSetRepository
                            .save(
                                    entitlementSet
                            );

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }

    @Override
    public Result<EntitlementSet, ApplicationError> handle(
            RevokeAllEntitlementsCommand command
    ) {

        final String operation =
                "revoke-all-entitlements";

        try {

            SubscriptionId subscriptionId =
                    new SubscriptionId(
                            command.subscriptionId()
                    );

            var existing =
                    entitlementSetRepository
                            .findBySubscriptionId(
                                    subscriptionId
                            );

            if (existing.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriptionId
                                        .value()
                                        .toString()
                        )
                );
            }

            EntitlementSet entitlementSet =
                    existing.get();

            Instant now = Instant.now();

            entitlementSet.revokeAll(
                    now
            );

            EntitlementSet saved =
                    entitlementSetRepository
                            .save(
                                    entitlementSet
                            );

            return Result.success(
                    saved
            );

        } catch (IllegalArgumentException exception) {

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );

        } catch (IllegalStateException exception) {

            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }

    private static Entitlement toEntitlement(
            EntitlementDefinition definition
    ) {

        Entitlement entitlement =
                new Entitlement();

        entitlement.setCode(
                new EntitlementCode(
                        definition.code()
                )
        );

        entitlement.setName(
                definition.name()
        );

        entitlement.setDescription(
                definition.description()
        );

        return entitlement;
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
}