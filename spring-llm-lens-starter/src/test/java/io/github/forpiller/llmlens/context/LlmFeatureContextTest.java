package io.github.forpiller.llmlens.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmFeatureContextTest {

    @AfterEach
    void cleanup() {
        LlmFeatureContext.clear();
    }

    @Test
    void get_returnsUnknown_whenNotSet() {
        assertThat(LlmFeatureContext.get()).isEqualTo("unknown");
    }

    @Test
    void set_thenGet_returnsFeature() {
        LlmFeatureContext.set("chatbot");
        assertThat(LlmFeatureContext.get()).isEqualTo("chatbot");
    }

    @Test
    void with_clearsContext_afterSupplierReturns() {
        String result = LlmFeatureContext.with("chatbot", () -> {
            assertThat(LlmFeatureContext.get()).isEqualTo("chatbot");
            return "ok";
        });
        assertThat(result).isEqualTo("ok");
        assertThat(LlmFeatureContext.get()).isEqualTo("unknown");
    }

    @Test
    void with_clearsContext_evenIfSupplierThrows() {
        assertThatThrownBy(() ->
            LlmFeatureContext.with("chatbot", () -> { throw new RuntimeException("boom"); })
        ).isInstanceOf(RuntimeException.class);
        assertThat(LlmFeatureContext.get()).isEqualTo("unknown");
    }
}
