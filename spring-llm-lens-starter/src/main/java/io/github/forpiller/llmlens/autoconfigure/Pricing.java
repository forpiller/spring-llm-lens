package io.github.forpiller.llmlens.autoconfigure;

import java.util.HashMap;
import java.util.Map;

public class Pricing {

    private Map<String, PricingEntry> models = new HashMap<>();

    public Map<String, PricingEntry> getModels() {
        return models;
    }

    public void setModels(Map<String, PricingEntry> models) {
        this.models = models;
    }
}
