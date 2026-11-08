package io.github.forpiller.llmlens.autoconfigure;

import io.github.forpiller.llmlens.advisor.LlmObservabilityAdvisor;
import io.github.forpiller.llmlens.aop.LlmFeatureAspect;
import io.github.forpiller.llmlens.cost.LlmCostCalculator;
import io.github.forpiller.llmlens.metrics.LlmMetricsRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LlmLensAutoConfigurationTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(LlmLensAutoConfiguration.class));

    @Test
    void autoConfigures_allBeans_whenDependenciesPresent() {
        contextRunner
            .withBean(io.micrometer.core.instrument.MeterRegistry.class, SimpleMeterRegistry::new)
            .run(ctx -> {
                assertThat(ctx).hasSingleBean(LlmObservabilityAdvisor.class);
                assertThat(ctx).hasSingleBean(LlmCostCalculator.class);
                assertThat(ctx).hasSingleBean(LlmMetricsRecorder.class);
                assertThat(ctx).hasSingleBean(LlmFeatureAspect.class);
            });
    }

    @Test
    void defaultFeature_isUnknown_byDefault() {
        contextRunner
            .withBean(io.micrometer.core.instrument.MeterRegistry.class, SimpleMeterRegistry::new)
            .run(ctx -> {
                LlmLensProperties props = ctx.getBean(LlmLensProperties.class);
                assertThat(props.getDefaultFeature()).isEqualTo("unknown");
            });
    }

    @Test
    void customPricing_keepsDefaultModels() {
        contextRunner
            .withBean(io.micrometer.core.instrument.MeterRegistry.class, SimpleMeterRegistry::new)
            .withPropertyValues(
                "llm.lens.pricing.models.my-model.input-per-million-tokens=1.00",
                "llm.lens.pricing.models.my-model.output-per-million-tokens=2.00")
            .run(ctx -> {
                LlmCostCalculator calc = ctx.getBean(LlmCostCalculator.class);
                // built-in default should still be available after adding a custom model
                assertThat(calc.calculateCostUsd("gpt-4o-mini", 1_000_000, 0))
                    .isEqualByComparingTo("0.15000000");
            });
    }

    @Test
    void customPricing_overridesDefaults() {
        contextRunner
            .withBean(io.micrometer.core.instrument.MeterRegistry.class, SimpleMeterRegistry::new)
            .withPropertyValues(
                "llm.lens.pricing.models.my-model.input-per-million-tokens=1.00",
                "llm.lens.pricing.models.my-model.output-per-million-tokens=2.00")
            .run(ctx -> {
                LlmCostCalculator calc = ctx.getBean(LlmCostCalculator.class);
                assertThat(calc.calculateCostUsd("my-model", 1_000_000, 0))
                    .isEqualByComparingTo("1.00000000");
            });
    }
}
