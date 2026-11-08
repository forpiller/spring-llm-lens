package io.github.forpiller.llmlens.cost;

import java.util.Map;

/** Tabella immutabile dei prezzi per modello, espressi in USD per milione di token. */
public final class ModelPriceTable {

    private final Map<String, PriceEntry> prices;

    private ModelPriceTable(Map<String, PriceEntry> prices) {
        this.prices = Map.copyOf(prices);
    }

    /** Restituisce una tabella con i prezzi di default per i modelli OpenAI e Anthropic più comuni. */
    public static ModelPriceTable defaults() {
        return new ModelPriceTable(Map.of(
            "gpt-4o",            PriceEntry.of("5.00",  "15.00"),
            "gpt-4o-mini",       PriceEntry.of("0.15",  "0.60"),
            "claude-3-5-sonnet", PriceEntry.of("3.00",  "15.00"),
            "claude-3-5-haiku",  PriceEntry.of("0.80",  "4.00")
        ));
    }

    /** Crea una tabella personalizzata a partire da una mappa di prezzi. */
    public static ModelPriceTable of(Map<String, PriceEntry> prices) {
        return new ModelPriceTable(prices);
    }

    /** Restituisce il prezzo per il modello indicato, o null se non presente. */
    public PriceEntry get(String model) {
        return prices.get(model);
    }

    public Map<String, PriceEntry> entries() {
        return prices;
    }
}
