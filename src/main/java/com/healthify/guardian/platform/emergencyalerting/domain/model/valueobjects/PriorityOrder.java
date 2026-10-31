package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

/**
 * Position of an emergency contact within the escalation chain.
 *
 * @param value the position, starting at 1 for the primary contact
 */
public record PriorityOrder(Integer value) implements Comparable<PriorityOrder> {

    private static final String INVALID_MESSAGE_KEY = "priority-order.invalid";

    public PriorityOrder {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }

    /** Tells whether this is the primary contact's position. */
    public boolean isPrimary() {
        return value == 1;
    }

    @Override
    public int compareTo(PriorityOrder other) {
        return Integer.compare(value, other.value);
    }
}
