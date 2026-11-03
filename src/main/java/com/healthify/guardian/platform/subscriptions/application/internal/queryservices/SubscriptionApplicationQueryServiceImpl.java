package com.healthify.guardian.platform.subscriptions.application.internal.queryservices;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import com.healthify.guardian.platform.subscriptions.application.queryservices.SubscriptionApplicationQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetAvailableEntitlementsQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetSubscriptionStatusQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementSetRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubscriptionApplicationQueryServiceImpl
        implements SubscriptionApplicationQueryService {

    private final SubscriptionRepository subscriptionRepository;

    private final EntitlementSetRepository entitlementSetRepository;

    public SubscriptionApplicationQueryServiceImpl(
            SubscriptionRepository subscriptionRepository,
            EntitlementSetRepository entitlementSetRepository
    ) {
        this.subscriptionRepository =
                subscriptionRepository;

        this.entitlementSetRepository =
                entitlementSetRepository;
    }

    @Override
    public Result<Subscription, ApplicationError> handle(
            GetSubscriptionStatusQuery query
    ) {

        final String operation =
                "get-subscription-status";

        try {

            SubscriberId subscriberId =
                    new SubscriberId(
                            query.subscriberId()
                    );

            var subscription =
                    subscriptionRepository
                            .findActiveBySubscriberId(
                                    subscriberId
                            );

            if (subscription.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriberId
                                        .value()
                                        .toString()
                        )
                );
            }

            return Result.success(
                    subscription.get()
            );

        } catch (IllegalArgumentException exception) {

            return Result.failure(
                    ApplicationError.validationError(
                            operation,
                            resolve(exception)
                    )
            );
        }
    }

    @Override
    public Result<EntitlementSet, ApplicationError> handle(
            GetAvailableEntitlementsQuery query
    ) {

        final String operation =
                "get-available-entitlements";

        try {

            SubscriberId subscriberId =
                    new SubscriberId(
                            query.subscriberId()
                    );

            var subscription =
                    subscriptionRepository
                            .findActiveBySubscriberId(
                                    subscriberId
                            );

            if (subscription.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                subscriberId
                                        .value()
                                        .toString()
                        )
                );
            }

            Subscription current =
                    subscription.get();

            var entitlementSet =
                    entitlementSetRepository
                            .findBySubscriptionId(
                                    current.getId()
                            );

            if (entitlementSet.isEmpty()) {

                return Result.failure(
                        ApplicationError.notFound(
                                "EntitlementSet",
                                current.getId()
                                        .value()
                                        .toString()
                        )
                );
            }

            return Result.success(
                    entitlementSet.get()
            );

        } catch (IllegalArgumentException exception) {

            return Result.failure(
                    ApplicationError.validationError(
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
}