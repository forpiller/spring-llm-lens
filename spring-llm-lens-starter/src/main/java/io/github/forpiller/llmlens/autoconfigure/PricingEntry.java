package io.github.forpiller.llmlens.autoconfigure;

import java.math.BigDecimal;

public class PricingEntry {

    private BigDecimal inputPerMillionTokens = BigDecimal.ZERO;
    private BigDecimal outputPerMillionTokens = BigDecimal.ZERO;

    public BigDecimal getInputPerMillionTokens() {
        return inputPerMillionTokens;
    }

    public void setInputPerMillionTokens(BigDecimal inputPerMillionTokens) {
        this.inputPerMillionTokens = inputPerMillionTokens;
    }

    public BigDecimal getOutputPerMillionTokens() {
        return outputPerMillionTokens;
    }

    public void setOutputPerMillionTokens(BigDecimal outputPerMillionTokens) {
        this.outputPerMillionTokens = outputPerMillionTokens;
    }
}
