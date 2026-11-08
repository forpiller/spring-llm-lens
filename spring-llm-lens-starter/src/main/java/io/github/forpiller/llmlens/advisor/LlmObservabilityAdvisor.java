package io.github.forpiller.llmlens.advisor;

import io.github.forpiller.llmlens.context.LlmFeatureContext;
import io.github.forpiller.llmlens.metrics.LlmMetricsRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.core.Ordered;

public class LlmObservabilityAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LlmObservabilityAdvisor.class);

    private final LlmMetricsRecorder metricsRecorder;

    public LlmObservabilityAdvisor(LlmMetricsRecorder metricsRecorder) {
        this.metricsRecorder = metricsRecorder;
        log.info("LlmObservabilityAdvisor initialized");
    }

    /**
     * Intercetta la chiamata, misura il tempo e registra le metriche, poi prosegue la chain.
     * Sovrascrive completamente il template di BaseAdvisor per catturare le metriche
     * di errore anche quando il modello lancia un'eccezione.
     *
     * L'ordine è LOWEST_PRECEDENCE - 1 per eseguire appena prima del terminale
     * ChatModelCallAdvisor (che ha ordine Integer.MAX_VALUE e non chiama
     * chain.nextCall(), quindi qualsiasi advisor con lo stesso ordine verrebbe saltato).
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String feature = LlmFeatureContext.get();
        long start = System.currentTimeMillis();
        log.debug("LlmObservabilityAdvisor intercept — feature={}", feature);
        try {
            ChatClientResponse response = chain.nextCall(before(request, chain));
            return recordSuccess(response, chain, feature, start);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - start;
            log.info("LLM call failed — feature={} error={} ms={}", feature, e.getClass().getSimpleName(), durationMs);
            metricsRecorder.record("unknown", feature, 0, 0, durationMs, true, e.getClass().getSimpleName());
            throw e;
        }
    }

    /** Richiesto da BaseAdvisor; mai invocato perché adviseCall() è completamente sovrascritto. */
    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        return request;
    }

    /** Richiesto da BaseAdvisor; mai invocato perché adviseCall() è completamente sovrascritto. */
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    /** Estrae i contatori di token e il nome del modello dai metadati della risposta e registra le metriche. */
    private ChatClientResponse recordSuccess(ChatClientResponse response, AdvisorChain chain,
                                             String feature, long start) {
        long durationMs = System.currentTimeMillis() - start;
        if (response.chatResponse() == null) {
            metricsRecorder.record("unknown", feature, 0, 0, durationMs, false, null);
            return response;
        }
        ChatResponseMetadata metadata = response.chatResponse().getMetadata();
        String model = metadata.getModel() != null ? metadata.getModel() : "unknown";
        Usage usage = metadata.getUsage();
        int inputTokens  = usage != null && usage.getPromptTokens()     != null ? usage.getPromptTokens()     : 0;
        int outputTokens = usage != null && usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
        log.debug("LLM call completed — model={} feature={} in={} out={} ms={}", model, feature, inputTokens, outputTokens, durationMs);
        metricsRecorder.record(model, feature, inputTokens, outputTokens, durationMs, false, null);
        return response;
    }

    @Override
    public String getName() {
        return "LlmObservabilityAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
