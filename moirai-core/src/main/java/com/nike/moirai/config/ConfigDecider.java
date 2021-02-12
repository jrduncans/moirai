package com.nike.moirai.config;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ConfigDecider<C, R extends ConfigReader<C>> {
    private final BiFunction<R, ConfigDecisionInput<C>, Boolean> deciderFunction;

    public ConfigDecider(BiFunction<R, ConfigDecisionInput<C>, Boolean> deciderFunction) {
        this.deciderFunction = deciderFunction;
    }

    public Predicate<ConfigDecisionInput<C>> predicate(R configReader) {
        return configInput -> deciderFunction.apply(configReader, configInput);
    }

    public ConfigDecider<C, R> and(ConfigDecider<C, R> other) {
        return new ConfigDecider<>(((configReader, configDecisionInput) ->
            this.deciderFunction.apply(configReader, configDecisionInput) && other.deciderFunction.apply(configReader, configDecisionInput)));
    }

    public ConfigDecider<C, R> or(ConfigDecider<C, R> other) {
        return new ConfigDecider<>(((configReader, configDecisionInput) ->
            this.deciderFunction.apply(configReader, configDecisionInput) || other.deciderFunction.apply(configReader, configDecisionInput)));
    }

    public ConfigDecider<C, R> negate() {
        return new ConfigDecider<>(((configReader, configDecisionInput) ->
            !this.deciderFunction.apply(configReader, configDecisionInput)));
    }
}
