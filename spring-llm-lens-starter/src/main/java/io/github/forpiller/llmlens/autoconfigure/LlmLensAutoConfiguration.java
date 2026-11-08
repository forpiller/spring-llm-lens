package io.github.forpiller.llmlens.autoconfigure;

import io.github.forpiller.llmlens.advisor.LlmObservabilityAdvisor;
import io.github.forpiller.llmlens.aop.LlmFeatureAspect;
import io.github.forpiller.llmlens.cost.LlmCostCalculator;
import io.github.forpiller.llmlens.cost.ModelPriceTable;
import io.github.forpiller.llmlens.cost.PriceEntry;
import io.github.forpiller.llmlens.metrics.LlmMetricsRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LlmLensProperties.class)
public class LlmLensAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ModelPriceTable llmModelPriceTable(LlmLensProperties properties) {
        Map<String, PriceEntry> merged = new HashMap<>(ModelPriceTable.defaults().entries());
        properties.getPricing().getModels().forEach((model, entry) ->
            merged.put(model, new PriceEntry(
                entry.getInputPerMillionTokens(),
                entry.getOutputPerMillionTokens()
            ))
        );
        return ModelPriceTable.of(merged);
    }

    @Bean
    @ConditionalOnMissingBean
    LlmCostCalculator llmCostCalculator(ModelPriceTable llmModelPriceTable) {
        return new LlmCostCalculator(llmModelPriceTable);
    }

    @Bean
    @ConditionalOnMissingBean
    LlmMetricsRecorder llmMetricsRecorder(MeterRegistry meterRegistry, LlmCostCalculator llmCostCalculator) {
        return new LlmMetricsRecorder(meterRegistry, llmCostCalculator);
    }

    @Bean
    @ConditionalOnMissingBean
    LlmObservabilityAdvisor llmObservabilityAdvisor(LlmMetricsRecorder llmMetricsRecorder) {
        return new LlmObservabilityAdvisor(llmMetricsRecorder);
    }

    @Bean
    @ConditionalOnMissingBean
    LlmFeatureAspect llmFeatureAspect() {
        return new LlmFeatureAspect();
    }

    @Bean
    ChatClientCustomizer llmObservabilityClientCustomizer(LlmObservabilityAdvisor llmObservabilityAdvisor) {
        return builder -> builder.defaultAdvisors(llmObservabilityAdvisor);
    }
}
