-- Guardian Plus - Subscription lifecycle protection
--
-- Garantiza que un suscriptor tenga como maximo una
-- suscripcion pendiente, activa o con cancelacion programada.
--
-- Conserva el historial de suscripciones canceladas y vencidas.

BEGIN;

-- Verificar que no existan suscripciones duplicadas
-- antes de crear el indice unico parcial.

DO $$
BEGIN
    IF EXISTS (
        SELECT subscriber_id
        FROM public.subscriptions
        WHERE status IN (
            'PENDING',
            'ACTIVE',
            'CANCELLATION_SCHEDULED'
        )
        GROUP BY subscriber_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Duplicate current subscriptions detected. Manual review is required.';
    END IF;
END $$;

-- Impedir multiples suscripciones vigentes
-- para un mismo suscriptor.

CREATE UNIQUE INDEX IF NOT EXISTS
    ux_subscriptions_subscriber_current
ON public.subscriptions (subscriber_id)
WHERE status IN (
    'PENDING',
    'ACTIVE',
    'CANCELLATION_SCHEDULED'
);

COMMIT;