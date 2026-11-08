package io.github.forpiller.llmlens.aop;

import io.github.forpiller.llmlens.annotation.LlmFeature;
import io.github.forpiller.llmlens.context.LlmFeatureContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LlmFeatureAspect {

    private static final Logger log = LoggerFactory.getLogger(LlmFeatureAspect.class);

    /**
     * Imposta il nome della feature nel ThreadLocal prima che il metodo annotato venga eseguito,
     * poi ripristina il valore precedente (o lo cancella) nel blocco finally.
     * Supporta chiamate @LlmFeature annidate: la feature più esterna viene ripristinata
     * quando quella interna completa.
     */
    @Around("@annotation(llmFeature)")
    public Object aroundLlmFeature(ProceedingJoinPoint joinPoint, LlmFeature llmFeature) throws Throwable {
        String previous = LlmFeatureContext.getRaw();
        String feature = llmFeature.value();
        log.debug("Setting LlmFeature context — feature={}", feature);
        LlmFeatureContext.set(feature);
        try {
            return joinPoint.proceed();
        } finally {
            if (previous != null) {
                LlmFeatureContext.set(previous);
            } else {
                LlmFeatureContext.clear();
            }
        }
    }
}
