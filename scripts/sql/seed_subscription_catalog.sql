BEGIN;

-- ============================================================
-- Guardian+ subscription catalog
-- Currency: PEN
-- Billing cycle: MONTHLY
--
-- Existing plans and entitlement definitions are preserved.
-- This script can be executed repeatedly.
-- ============================================================


-- 1. Subscription plans

INSERT INTO subscription_plans (
    id,
    active,
    billing_cycle,
    name,
    price_amount,
    price_currency
)
SELECT
    gen_random_uuid(),
    TRUE,
    'MONTHLY',
    catalog.name,
    catalog.price_amount,
    'PEN'
FROM (
    VALUES
        ('Guardian Basic', 0.00),
        ('Guardian Premium', 29.90),
        ('Cuidado Pro', 59.90)
) AS catalog(name, price_amount)
WHERE NOT EXISTS (
    SELECT 1
    FROM subscription_plans existing
    WHERE LOWER(existing.name) = LOWER(catalog.name)
);


-- Validate that each catalog plan has a unique name.

DO $$
BEGIN
    IF EXISTS (
        SELECT LOWER(name)
        FROM subscription_plans
        WHERE LOWER(name) IN (
            'guardian basic',
            'guardian premium',
            'cuidado pro'
        )
        GROUP BY LOWER(name)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Duplicate subscription plan names detected';
    END IF;
END $$;


-- 2. Entitlement catalog

INSERT INTO entitlements (
    code,
    name,
    description
)
VALUES
    (
        'EMERGENCY_ALERTS',
        'Emergency alerts',
        'Access to emergency alerts and SOS functionality'
    ),
    (
        'BASIC_REMINDERS',
        'Basic reminders',
        'Access to basic care reminders'
    ),
    (
        'REAL_TIME_LOCATION',
        'Real-time location',
        'Allows real-time location monitoring'
    ),
    (
        'HEALTH_MONITORING',
        'Health monitoring',
        'Allows health monitoring capabilities'
    ),
    (
        'GEOFENCING',
        'Geofencing',
        'Access to safe zones and perimeter monitoring'
    ),
    (
        'FALL_DETECTION',
        'Fall detection',
        'Access to fall detection alerts'
    ),
    (
        'VITAL_SIGNS_HISTORY',
        'Vital signs history',
        'Access to historical vital sign readings'
    ),
    (
        'LOCATION_HISTORY',
        'Location history',
        'Access to historical location readings'
    ),
    (
        'TREATMENT_ADHERENCE',
        'Treatment adherence',
        'Access to treatment adherence monitoring'
    ),
    (
        'SLEEP_ACTIVITY_MONITORING',
        'Sleep and activity monitoring',
        'Access to sleep and activity monitoring records'
    )
ON CONFLICT (code) DO NOTHING;


-- 3. Guardian Basic entitlements

INSERT INTO plan_entitlements (
    plan_id,
    entitlement_code
)
SELECT
    plan.id,
    entitlement.code
FROM subscription_plans plan
CROSS JOIN entitlements entitlement
WHERE LOWER(plan.name) = 'guardian basic'
AND entitlement.code IN (
    'EMERGENCY_ALERTS',
    'BASIC_REMINDERS'
)
ON CONFLICT (entitlement_code, plan_id) DO NOTHING;


-- 4. Guardian Premium entitlements

INSERT INTO plan_entitlements (
    plan_id,
    entitlement_code
)
SELECT
    plan.id,
    entitlement.code
FROM subscription_plans plan
CROSS JOIN entitlements entitlement
WHERE LOWER(plan.name) = 'guardian premium'
AND entitlement.code IN (
    'EMERGENCY_ALERTS',
    'BASIC_REMINDERS',
    'REAL_TIME_LOCATION',
    'HEALTH_MONITORING',
    'GEOFENCING',
    'FALL_DETECTION'
)
ON CONFLICT (entitlement_code, plan_id) DO NOTHING;


-- 5. Cuidado Pro entitlements

INSERT INTO plan_entitlements (
    plan_id,
    entitlement_code
)
SELECT
    plan.id,
    entitlement.code
FROM subscription_plans plan
CROSS JOIN entitlements entitlement
WHERE LOWER(plan.name) = 'cuidado pro'
AND entitlement.code IN (
    'EMERGENCY_ALERTS',
    'BASIC_REMINDERS',
    'REAL_TIME_LOCATION',
    'HEALTH_MONITORING',
    'GEOFENCING',
    'FALL_DETECTION',
    'VITAL_SIGNS_HISTORY',
    'LOCATION_HISTORY',
    'TREATMENT_ADHERENCE',
    'SLEEP_ACTIVITY_MONITORING'
)
ON CONFLICT (entitlement_code, plan_id) DO NOTHING;


COMMIT;