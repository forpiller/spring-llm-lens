package io.github.forpiller.llmlens.cost;

import java.math.BigDecimal;

/** Prezzo di un modello LLM, espresso in USD per milione di token. */
public class PriceEntry {

    private final BigDecimal inputPerMillionTokens;
    private final BigDecimal outputPerMillionTokens;

    public PriceEntry(BigDecimal inputPerMillionTokens, BigDecimal outputPerMillionTokens) {
        this.inputPerMillionTokens = inputPerMillionTokens;
        this.outputPerMillionTokens = outputPerMillionTokens;
    }

    /** Crea un PriceEntry a partire da stringhe decimali (es. "3.00", "15.00"). */
    public static PriceEntry of(String input, String output) {
        return new PriceEntry(new BigDecimal(input), new BigDecimal(output));
    }

    public BigDecimal inputPerMillionTokens() {
        return inputPerMillionTokens;
    }

    public BigDecimal outputPerMillionTokens() {
        return outputPerMillionTokens;
    }
}
