package com.healthify.guardian.platform.healthmonitoring.domain.model.entities;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Internal entity of {@code HealthReport} summarizing the readings of one vital sign type within
 * the report period: average, minimum, maximum and how stable the readings were.
 *
 * <p>{@code stabilityIndex} is {@value #STABLE} when no reading left the normal range of its type,
 * {@value #UNSTABLE} when some did, and {@value #RECURRENT} when more than
 * {@value #RECURRENT_ANOMALIES_LIMIT} did (US24, "more than 3 anomalies of the same type").</p>
 */
@Getter
public class VitalSignSummary {

    public static final String STABLE = "STABLE";
    public static final String UNSTABLE = "UNSTABLE";
    public static final String RECURRENT = "RECURRENT";
    public static final int RECURRENT_ANOMALIES_LIMIT = 3;

    private static final String VALUES_EMPTY_KEY = "vital-sign-summary.values.empty";

    private Long id;
    private String metricType;
    private Double averageValue;
    private Double minValue;
    private Double maxValue;
    private Integer readingsCount;
    private Integer outOfRangeCount;
    private String stabilityIndex;

    /** Reconstitution constructor, used when deserializing a persisted report. */
    public VitalSignSummary() {
    }

    public VitalSignSummary(Long id, String metricType, Double averageValue, Double minValue, Double maxValue,
                            Integer readingsCount, Integer outOfRangeCount, String stabilityIndex) {
        this.id = id;
        this.metricType = metricType;
        this.averageValue = averageValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.readingsCount = readingsCount;
        this.outOfRangeCount = outOfRangeCount;
        this.stabilityIndex = stabilityIndex;
    }

    /**
     * Summarizes the readings of a single vital sign type.
     *
     * @param id         position of the summary inside its report
     * @param type     the vital sign type, whose code labels the summary
     * @param readings readings of that type, never empty
     * @return the summary
     */
    public static VitalSignSummary of(Long id, VitalSignType type, List<VitalSign> readings) {
        if (readings == null || readings.isEmpty()) {
            throw new IllegalArgumentException(VALUES_EMPTY_KEY);
        }
        var values = readings.stream().map(reading -> reading.getValue().value()).toList();
        var sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        var average = sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
        var min = values.stream().min(Comparator.naturalOrder()).orElseThrow();
        var max = values.stream().max(Comparator.naturalOrder()).orElseThrow();
        var outOfRange = (int) readings.stream()
                .filter(reading -> type.classify(reading.getValue()).isOutOfRange())
                .count();
        return new VitalSignSummary(id, type.code(), average.doubleValue(), min.doubleValue(), max.doubleValue(),
                values.size(), outOfRange, toStabilityIndex(outOfRange));
    }

    public boolean isStable() {
        return STABLE.equals(stabilityIndex);
    }

    public boolean isRecurrent() {
        return RECURRENT.equals(stabilityIndex);
    }

    private static String toStabilityIndex(int outOfRangeCount) {
        if (outOfRangeCount == 0) {
            return STABLE;
        }
        return outOfRangeCount > RECURRENT_ANOMALIES_LIMIT ? RECURRENT : UNSTABLE;
    }
}
