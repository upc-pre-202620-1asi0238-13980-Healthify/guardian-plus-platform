package com.healthify.guardian.platform.healthmonitoring.acceptance.steps;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.HealthReport;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.CompileWeeklySummaryCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.GenerateHealthReportCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.entities.VitalSignSummary;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetLiveVitalSignsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.interfaces.events.VitalSignAnomalyDetectedIntegrationEvent;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LiveVitalSignResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LiveVitalSignsResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.LiveVitalSignsResourceFromEntityAssembler;
import com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.ParameterType;
import io.cucumber.java.es.Entonces;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.healthify.guardian.platform.healthmonitoring.testsupport.HealthMonitoringTestContext.NOW;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions of the Health Monitoring acceptance features. Each scenario gets a fresh
 * instance, hence a fresh {@link HealthMonitoringTestContext} wiring the real application services
 * and event handlers over in-memory repositories.
 */
public class HealthMonitoringStepDefinitions {

    private static final Duration LIVE_SIGNAL_WINDOW = Duration.ofSeconds(60);

    private final HealthMonitoringTestContext context = new HealthMonitoringTestContext();
    private UUID recipient;
    private WearableDevice device;
    // Readings are transmitted one second apart and end just before NOW, inside the live signal window
    private long secondsAgo = 50;
    private LiveVitalSignsResource liveView;
    private Result<List<VitalSign>, ApplicationError> batchResult;
    private int batchSize;
    private Result<HealthReport, ApplicationError> reportResult;

    /**
     * Locale-independent decimal: the built-in {@code {bigdecimal}} follows the Spanish locale of the
     * features and would read {@code 37.8} as {@code 378}.
     */
    @ParameterType("-?\\d+(?:\\.\\d+)?")
    public BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private DetectVitalSignsCommand reading(String code, BigDecimal value, long secondsBeforeNow) {
        return new DetectVitalSignsCommand(device.getId().value(), recipient, code,
                value, NOW.minusSeconds(secondsBeforeNow), NOW);
    }

    private void transmit(String code, BigDecimal value) {
        var result = context.vitalSignCommandService.handle(reading(code, value, secondsAgo--));
        assertThat(result.isSuccess()).as("reading accepted: %s", result).isTrue();
    }

    @Dado("una persona bajo cuidado con una pulsera asignada")
    public void unaPersonaBajoCuidadoConUnaPulseraAsignada() {
        recipient = UUID.randomUUID();
        device = context.linkDevice(recipient, "GP-ESP32-S3-0001");
    }

    @Dado("el rango normal de {word} es de {decimal} a {decimal}")
    public void elRangoNormalEs(String code, BigDecimal minimum, BigDecimal maximum) {
        var normalRange = VitalSignType.fromCode(code).normalRange();
        assertThat(normalRange.minimum()).isEqualByComparingTo(minimum);
        assertThat(normalRange.maximum()).isEqualByComparingTo(maximum);
    }

    @Cuando("la pulsera transmite una lectura de {word} de {decimal}")
    public void laPulseraTransmiteUnaLectura(String code, BigDecimal value) {
        transmit(code, value);
    }

    @Cuando("la pulsera transmite las lecturas de {word}: {string}")
    public void laPulseraTransmiteLasLecturas(String code, String values) {
        Arrays.stream(values.split(",")).map(String::strip).map(BigDecimal::new).forEach(value -> transmit(code, value));
    }

    @Dado("la última lectura de {word} de {decimal} fue transmitida hace {int} minutos")
    public void laUltimaLecturaFueTransmitidaHace(String code, BigDecimal value, int minutes) {
        var result = context.vitalSignCommandService.handle(reading(code, value, minutes * 60L));
        assertThat(result.isSuccess()).isTrue();
    }

    @Cuando("el cuidador consulta los signos vitales en vivo")
    public void elCuidadorConsultaLosSignosVitalesEnVivo() {
        var careRecipientProfileId = new CareRecipientProfileId(recipient);
        var latest = context.vitalSignQueryService.handle(new GetLiveVitalSignsByCareRecipientProfileIdQuery(careRecipientProfileId));
        liveView = LiveVitalSignsResourceFromEntityAssembler.toResourceFromEntities(
                recipient, latest, LIVE_SIGNAL_WINDOW, NOW);
    }

    private LiveVitalSignResource liveReading(String code) {
        return liveView.vitalSigns().stream()
                .filter(vitalSign -> code.equals(vitalSign.vitalSignType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No live reading of " + code));
    }

    @Entonces("el sistema muestra {word} con valor {decimal} clasificado como {word}")
    public void elSistemaMuestraClasificado(String code, BigDecimal value, String classification) {
        var live = liveReading(code);
        assertThat(live.value()).isEqualByComparingTo(value);
        assertThat(live.classification()).isEqualTo(classification);
        assertThat(live.liveSignal()).isTrue();
    }

    @Entonces("el sistema muestra el último valor de {word} sin señal en vivo")
    public void elSistemaMuestraElUltimoValorSinSenal(String code) {
        var live = liveReading(code);
        assertThat(live.value()).isNotNull();
        assertThat(live.liveSignal()).isFalse();
    }

    @Entonces("se notifica a Emergency & Alerting una anomalía de {word} {word}")
    public void seNotificaUnaAnomalia(String code, String classification) {
        assertThat(context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.vitalSignTypeCode()).isEqualTo(code);
                    assertThat(event.classification()).isEqualTo(classification);
                    assertThat(event.careRecipientProfileId()).isEqualTo(recipient);
                });
    }

    @Entonces("no se notifica ninguna anomalía")
    public void noSeNotificaNingunaAnomalia() {
        assertThat(context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class)).isEmpty();
    }

    @Entonces("se notifican {int} anomalías a Emergency & Alerting")
    public void seNotificanAnomalias(int count) {
        assertThat(context.eventsOfType(VitalSignAnomalyDetectedIntegrationEvent.class)).hasSize(count);
    }

    @Cuando("la pulsera sincroniza un lote de lecturas de {word}:")
    public void laPulseraSincronizaUnLote(String code, DataTable table) {
        var commands = table.asMaps().stream()
                .map(row -> reading(code, new BigDecimal(row.get("valor")), Long.parseLong(row.get("hace_segundos"))))
                .toList();
        batchSize = commands.size();
        batchResult = context.vitalSignCommandService.handleBatch(commands);
    }

    @Cuando("la pulsera sincroniza un lote con una lectura de otra persona bajo cuidado")
    public void laPulseraSincronizaUnLoteConUnaLecturaAjena() {
        var foreign = new DetectVitalSignsCommand(device.getId().value(), UUID.randomUUID(), "HR",
                new BigDecimal("70"), NOW.minusSeconds(10), NOW);
        batchResult = context.vitalSignCommandService.handleBatch(List.of(reading("HR", new BigDecimal("70"), 20), foreign));
    }

    @Entonces("el sistema almacena {int} lecturas y descarta {int} duplicadas")
    public void elSistemaAlmacenaYDescarta(int stored, int duplicates) {
        var storedReadings = HealthMonitoringTestContext.value(batchResult);
        assertThat(storedReadings).hasSize(stored);
        assertThat(batchSize - storedReadings.size()).isEqualTo(duplicates);
    }

    @Entonces("el lote es rechazado como no procesable")
    public void elLoteEsRechazado() {
        assertThat(batchResult).isInstanceOfSatisfying(Result.Failure.class,
                failure -> assertThat(((ApplicationError) failure.error()).code()).isEqualTo("BUSINESS_RULE_VIOLATION"));
        assertThat(context.vitalSignRepository.findAll()).isEmpty();
    }

    @Cuando("el cuidador solicita el reporte del {string} al {string}")
    public void elCuidadorSolicitaElReporte(String from, String to) {
        reportResult = context.healthReportCommandService.handle(new GenerateHealthReportCommand(
                recipient, UUID.randomUUID(), "ON_DEMAND", LocalDate.parse(from), LocalDate.parse(to)));
    }

    @Entonces("el sistema bloquea el reporte indicando que no existen registros en el rango")
    public void elSistemaBloqueaElReporte() {
        assertThat(reportResult).isInstanceOfSatisfying(Result.Failure.class,
                failure -> assertThat(((ApplicationError) failure.error()).code()).isEqualTo("BUSINESS_RULE_VIOLATION"));
        assertThat(context.healthReportRepository.findByCareRecipientProfileId(new CareRecipientProfileId(recipient))).isEmpty();
    }

    @Cuando("se compila el resumen semanal")
    public void seCompilaElResumenSemanal() {
        reportResult = context.healthReportCommandService.handle(new CompileWeeklySummaryCommand(recipient));
    }

    private HealthReport report() {
        return HealthMonitoringTestContext.value(reportResult);
    }

    private VitalSignSummary summary(String code) {
        return report().getSummaries().stream()
                .filter(summary -> code.equals(summary.getMetricType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No summary of " + code));
    }

    @Entonces("el reporte resume {int} parámetros")
    public void elReporteResume(int count) {
        assertThat(report().getSummaries()).hasSize(count);
    }

    @Entonces("el reporte es clínicamente estable")
    public void elReporteEsClinicamenteEstable() {
        assertThat(report().isClinicallyStable()).isTrue();
    }

    @Entonces("el reporte marca {word} como {word}")
    public void elReporteMarca(String code, String stabilityIndex) {
        assertThat(summary(code).getStabilityIndex()).isEqualTo(stabilityIndex);
    }

    @Entonces("el reporte incluye {int} parámetro(s) recurrente(s)")
    public void elReporteIncluyeParametrosRecurrentes(int count) {
        assertThat(report().getRecurrentAnomaliesCount()).isEqualTo(count);
    }
}
