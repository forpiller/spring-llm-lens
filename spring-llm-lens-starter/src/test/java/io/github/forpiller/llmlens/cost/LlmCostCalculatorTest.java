package io.github.forpiller.llmlens.cost;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LlmCostCalculatorTest {

    LlmCostCalculator calculator = new LlmCostCalculator(ModelPriceTable.defaults());

    @Test
    void calculatesCostForKnownModel() {
        // gpt-4o: input 5.00/M, output 15.00/M
        // 1000 input + 500 output = 0.005000 + 0.007500 = 0.012500
        BigDecimal cost = calculator.calculateCostUsd("gpt-4o", 1000, 500);
        assertThat(cost).isEqualByComparingTo("0.01250000");
    }

    @Test
    void returnsZero_forUnknownModel() {
        BigDecimal cost = calculator.calculateCostUsd("unknown-model-xyz", 1000, 500);
        assertThat(cost).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void returnsZero_whenBothTokensAreZero() {
        BigDecimal cost = calculator.calculateCostUsd("gpt-4o", 0, 0);
        assertThat(cost).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
