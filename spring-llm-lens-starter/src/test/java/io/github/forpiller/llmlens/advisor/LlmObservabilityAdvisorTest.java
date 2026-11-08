package io.github.forpiller.llmlens.advisor;

import io.github.forpiller.llmlens.context.LlmFeatureContext;
import io.github.forpiller.llmlens.cost.LlmCostCalculator;
import io.github.forpiller.llmlens.cost.ModelPriceTable;
import io.github.forpiller.llmlens.metrics.LlmMetricsRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmObservabilityAdvisorTest {

    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    LlmCostCalculator costCalc = new LlmCostCalculator(ModelPriceTable.defaults());
    LlmMetricsRecorder metricsRecorder = new LlmMetricsRecorder(registry, costCalc);

    @Mock
    CallAdvisorChain chain;

    LlmObservabilityAdvisor advisor = new LlmObservabilityAdvisor(metricsRecorder);

    @AfterEach
    void cleanup() {
        LlmFeatureContext.clear();
    }

    @Test
    void recordsMetrics_onSuccessfulCall() {
        ChatClientRequest request = ChatClientRequest.builder()
            .prompt(new Prompt("test"))
            .build();
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
            .usage(new DefaultUsage(100, 50))
            .model("gpt-4o")
            .build();
        ChatResponse chatResponse = new ChatResponse(List.of(), metadata);
        ChatClientResponse response = ChatClientResponse.builder()
            .chatResponse(chatResponse)
            .build();

        when(chain.nextCall(request)).thenReturn(response);

        LlmFeatureContext.set("chatbot");
        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        assertThat(registry.counter("llm.tokens.input", "model", "gpt-4o", "feature", "chatbot").count())
            .isEqualTo(100.0);
    }

    @Test
    void handlesNullChatResponse_withoutThrowing() {
        ChatClientRequest request = ChatClientRequest.builder()
            .prompt(new Prompt("test"))
            .build();
        ChatClientResponse response = ChatClientResponse.builder().build();
        when(chain.nextCall(request)).thenReturn(response);
        assertThatNoException().isThrownBy(() -> advisor.adviseCall(request, chain));
    }

    @Test
    void handlesNullUsage_withoutThrowing() {
        ChatClientRequest request = ChatClientRequest.builder()
            .prompt(new Prompt("test"))
            .build();
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
            .model("gpt-4o")
            .build();
        ChatResponse chatResponse = new ChatResponse(List.of(), metadata);
        ChatClientResponse response = ChatClientResponse.builder()
            .chatResponse(chatResponse)
            .build();

        when(chain.nextCall(request)).thenReturn(response);

        assertThatNoException().isThrownBy(() -> advisor.adviseCall(request, chain));
    }
}
