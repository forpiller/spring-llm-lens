package io.github.forpiller.llmlens.context;

import java.util.function.Supplier;

/**
 * Contesto ThreadLocal che mantiene il nome della feature LLM corrente.
 * Viene impostato dall'aspect @LlmFeature e letto dall'advisor per taggare le metriche.
 * Per chiamate programmatiche (lambda, thread virtuali) usare il metodo with().
 */
public final class LlmFeatureContext {

    private static final String DEFAULT = "unknown";
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private LlmFeatureContext() {
    }

    /** Restituisce la feature corrente, o "unknown" se non impostata. */
    public static String get() {
        String value = CONTEXT.get();
        return value != null ? value : DEFAULT;
    }

    /** Restituisce la feature corrente senza fallback (può essere null). */
    public static String getRaw() {
        return CONTEXT.get();
    }

    public static void set(String feature) {
        CONTEXT.set(feature);
    }

    /** Rimuove la feature dal ThreadLocal per evitare memory leak nei thread pool. */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Esegue il supplier con la feature indicata, ripristinando il contesto precedente al termine.
     * Alternativa programmatica a @LlmFeature per casi in cui l'AOP non è applicabile
     * (es. lambda, chiamate interne allo stesso bean).
     */
    public static <T> T with(String feature, Supplier<T> supplier) {
        String previous = CONTEXT.get();
        CONTEXT.set(feature);
        try {
            return supplier.get();
        } finally {
            if (previous != null) {
                CONTEXT.set(previous);
            } else {
                CONTEXT.remove();
            }
        }
    }
}
