package io.github.forpiller.llmlens.aop;

import io.github.forpiller.llmlens.annotation.LlmFeature;
import io.github.forpiller.llmlens.context.LlmFeatureContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {LlmFeatureAspect.class, LlmFeatureAspectTest.TestConfig.class})
class LlmFeatureAspectTest {

    @Autowired
    TestService service;

    @Test
    void setsFeatureContext_duringMethodExecution() {
        String captured = service.callAnnotatedMethod();
        assertThat(captured).isEqualTo("test-feature");
    }

    @Test
    void clearsFeatureContext_afterMethodReturns() {
        service.callAnnotatedMethod(); // result discarded, just checking side effect
        assertThat(LlmFeatureContext.get()).isEqualTo("unknown");
    }

    @Test
    void doesNotClearToNull_whenContextWasExplicitlySetToUnknown() {
        LlmFeatureContext.set("unknown");
        service.callAnnotatedMethod();
        // aspect should restore "unknown" (set), not null (clear)
        assertThat(LlmFeatureContext.getRaw()).isEqualTo("unknown");
    }

    @Test
    void clearsFeatureContext_evenIfMethodThrows() {
        assertThatThrownBy(() -> service.throwingMethod())
            .isInstanceOf(RuntimeException.class);
        assertThat(LlmFeatureContext.get()).isEqualTo("unknown");
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        TestService testService() {
            return new TestService();
        }
    }

    @Component
    static class TestService {

        @LlmFeature("test-feature")
        String callAnnotatedMethod() {
            return LlmFeatureContext.get();
        }

        @LlmFeature("throwing-feature")
        void throwingMethod() {
            throw new RuntimeException("boom");
        }
    }
}
