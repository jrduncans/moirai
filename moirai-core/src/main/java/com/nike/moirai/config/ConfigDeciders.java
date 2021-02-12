package com.nike.moirai.config;

import java.util.Optional;
import java.util.function.Predicate;

import static com.nike.moirai.config.ConfigDeciderSupport.userIdCheck;

/**
 * {@link Predicate} factories given a {@link ConfigReader}
 */
public class ConfigDeciders {
    public static <C, R extends ConfigReader<C>> ConfigDecider<C, R> featureEnabled() {
        return new ConfigDecider<>((configReader, configDecisionInput) ->
            configReader.featureEnabled(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).orElse(false));
    }

    public static <C, R extends ConfigReader<C>> ConfigDecider<C, R> enabledUsers() {
        return new ConfigDecider<>(((configReader, configDecisionInput) ->
            userIdCheck(configDecisionInput.getFeatureCheckInput(), userId ->
                configReader.enabledUsers(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).contains(userId))));
    }

    public static <C, R extends ConfigReader<C>> ConfigDecider<C, R> proportionOfUsers() {
        return new ConfigDecider<>((configReader, configDecisionInput) ->
            userIdCheck(configDecisionInput.getFeatureCheckInput(), userId ->
                configReader.enabledProportion(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).map(enabledProportion ->
                    userHashEnabled(
                        userId,
                        configDecisionInput.getFeatureIdentifier(),
                        configReader.featureGroup(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()),
                        enabledProportion)
                ).orElse(false)
            ));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static boolean userHashEnabled(String userId, String featureIdentifier, Optional<String> featureGroup, double proportion) {
        return (Math.abs((userId + featureGroup.orElse(featureIdentifier)).hashCode()) % 100) / 100.0 < proportion;
    }

    private ConfigDeciders() {
        // Prevent instantiation
    }
}
