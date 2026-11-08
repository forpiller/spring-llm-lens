package io.github.forpiller.llmlens.metrics;

import io.github.forpiller.llmlens.cost.LlmCostCalculator;
import io.github.forpiller.llmlens.cost.ModelPriceTable;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmMetricsRecorderTest {

    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    LlmCostCalculator costCalc = new LlmCostCalculator(ModelPriceTable.defaults());
    LlmMetricsRecorder recorder = new LlmMetricsRecorder(registry, costCalc);

    @Test
    void recordsTokenCounters() {
        recorder.record("gpt-4o", "chatbot", 100, 50, 200, false, null);
        assertThat(registry.counter("llm.tokens.input", "model", "gpt-4o", "feature", "chatbot").count())
            .isEqualTo(100.0);
        assertThat(registry.counter("llm.tokens.output", "model", "gpt-4o", "feature", "chatbot").count())
            .isEqualTo(50.0);
    }

    @Test
    void recordsErrorCounter_onlyWhenError() {
        recorder.record("gpt-4o", "chatbot", 0, 0, 100, true, "RATE_LIMIT");
        assertThat(registry.counter("llm.request.errors",
            "model", "gpt-4o", "feature", "chatbot", "error_type", "RATE_LIMIT").count())
            .isEqualTo(1.0);
    }

    @Test
    void doesNotRecordErrorCounter_onSuccess() {
        recorder.record("gpt-4o", "chatbot", 10, 5, 100, false, null);
        assertThat(registry.find("llm.request.errors").counters()).isEmpty();
    }
}
