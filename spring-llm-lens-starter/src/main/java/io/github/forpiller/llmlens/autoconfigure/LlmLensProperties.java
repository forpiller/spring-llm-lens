package io.github.forpiller.llmlens.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm.lens")
public class LlmLensProperties {

    private String defaultFeature = "unknown";
    private Pricing pricing = new Pricing();

    public String getDefaultFeature() {
        return defaultFeature;
    }

    public void setDefaultFeature(String defaultFeature) {
        this.defaultFeature = defaultFeature;
    }

    public Pricing getPricing() {
        return pricing;
    }

    public void setPricing(Pricing pricing) {
        this.pricing = pricing;
    }
}
