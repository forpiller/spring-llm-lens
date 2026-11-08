package io.github.forpiller.llmlens.metrics;

import io.github.forpiller.llmlens.cost.LlmCostCalculator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class LlmMetricsRecorder {

    private static final Logger log = LoggerFactory.getLogger(LlmMetricsRecorder.class);

    private final MeterRegistry registry;
    private final LlmCostCalculator costCalculator;

    public LlmMetricsRecorder(MeterRegistry registry, LlmCostCalculator costCalculator) {
        this.registry = registry;
        this.costCalculator = costCalculator;
        log.info("LlmMetricsRecorder initialized");
    }

    /**
     * Registra tutti i segnali di osservabilità per una singola chiamata LLM:
     * contatori di token in input/output, costo stimato in USD, durata della richiesta,
     * e (in caso di errore) il tipo di eccezione.
     * Ogni metrica è taggata con model e feature per permettere il filtraggio in Grafana.
     */
    public void record(String model, String feature, int inputTokens, int outputTokens,
                       long durationMs, boolean error, String errorType) {
        log.debug("Recording metrics — model={} feature={} in={} out={} ms={} error={}", model, feature, inputTokens, outputTokens, durationMs, error);

        Counter.builder("llm.tokens.input")
            .tags("model", model, "feature", feature)
            .register(registry)
            .increment(inputTokens);

        Counter.builder("llm.tokens.output")
            .tags("model", model, "feature", feature)
            .register(registry)
            .increment(outputTokens);

        double costUsd = costCalculator.calculateCostUsd(model, inputTokens, outputTokens).doubleValue();
        Counter.builder("llm.cost.usd")
            .tags("model", model, "feature", feature)
            .register(registry)
            .increment(costUsd);

        String status = error ? "error" : "success";
        Timer.builder("llm.request.duration")
            .tags("model", model, "feature", feature, "status", status)
            .register(registry)
            .record(Duration.ofMillis(durationMs));

        if (error && errorType != null) {
            Counter.builder("llm.request.errors")
                .tags("model", model, "feature", feature, "error_type", errorType)
                .register(registry)
                .increment();
        }
    }
}
