package io.github.forpiller.llmlens.cost;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LlmCostCalculator {

    private static final Logger log = LoggerFactory.getLogger(LlmCostCalculator.class);
    private static final BigDecimal MILLION = new BigDecimal("1000000");
    private static final int SCALE = 8;

    private final ModelPriceTable priceTable;

    public LlmCostCalculator(ModelPriceTable priceTable) {
        this.priceTable = priceTable;
        log.info("LlmCostCalculator initialized with {} price entries", priceTable.entries().size());
    }

    /**
     * Calcola il costo in USD di una singola chiamata LLM in base ai contatori di token.
     * Restituisce 0 se il modello non è presente nella tabella prezzi (viene loggato un WARN).
     * Costo = (inputTokens * prezzoInputPerMilione + outputTokens * prezzoOutputPerMilione) / 1_000_000
     */
    public BigDecimal calculateCostUsd(String model, int inputTokens, int outputTokens) {
        PriceEntry entry = priceTable.get(model);
        if (entry == null) {
            log.warn("No price entry for model '{}', cost will be reported as 0", model);
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal inputCost = entry.inputPerMillionTokens()
            .multiply(BigDecimal.valueOf(inputTokens))
            .divide(MILLION, SCALE, RoundingMode.HALF_UP);
        BigDecimal outputCost = entry.outputPerMillionTokens()
            .multiply(BigDecimal.valueOf(outputTokens))
            .divide(MILLION, SCALE, RoundingMode.HALF_UP);
        return inputCost.add(outputCost).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
