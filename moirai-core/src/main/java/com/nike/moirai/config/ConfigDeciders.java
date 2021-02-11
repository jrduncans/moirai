package com.nike.moirai.config;

import com.nike.moirai.FeatureCheckInput;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static com.nike.moirai.config.ConfigDeciderSupport.userIdCheck;

/**
 * {@link Predicate} factories given a {@link ConfigReader}
 */
public class ConfigDeciders {
    public static <C> Predicate<ConfigDecisionInput<C>> featureEnabled(ConfigReader<C> configReader) {
        return configDecisionInput ->
            configReader.featureEnabled(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).orElse(false);
    }

    public static <C> Predicate<ConfigDecisionInput<C>> enabledUsers(ConfigReader<C> configReader) {
        return new EnabledValuesConfigDecider<C, String>() {
            @Override
            protected Collection<String> enabledValues(C config, String featureIdentifier) {
                return configReader.enabledUsers(config, featureIdentifier);
            }

            @Override
            protected boolean checkValue(FeatureCheckInput featureCheckInput, Predicate<String> check) {
                return userIdCheck(featureCheckInput, check);
            }
        };
    }

    public static <C> Predicate<ConfigDecisionInput<C>> proportionOfUsers(ConfigReader<C> configReader) {
        return configDecisionInput ->
            userIdCheck(configDecisionInput.getFeatureCheckInput(), userId ->
                configReader.enabledProportion(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()).map(enabledProportion ->
                    userHashEnabled(
                        userId,
                        configDecisionInput.getFeatureIdentifier(),
                        configReader.featureGroup(configDecisionInput.getConfig(), configDecisionInput.getFeatureIdentifier()),
                        enabledProportion)
                ).orElse(false)
            );
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static boolean userHashEnabled(String userId, String featureIdentifier, Optional<String> featureGroup, double proportion) {
        return (Math.abs((userId + featureGroup.orElse(featureIdentifier)).hashCode()) % 100) / 100.0 < proportion;
    }

    private ConfigDeciders() {
        // Prevent instantiation
    }
}
