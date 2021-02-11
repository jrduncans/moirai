package com.nike.moirai.config;

import java.util.Collection;
import java.util.Optional;

public interface ConfigReader<C> {
    /**
     * Provide the collection of users that should have the given feature enabled. Return an empty list if no configuration is provided for the feature.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return the collection of userIds that should be enabled for the feature
     */
    Collection<String> enabledUsers(C config, String featureIdentifier);

    /**
     * Provide the boolean value on whether the feature should be enabled.
     * Returning Optional.empty() is equivalent to false.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return true, false, or {@link Optional#empty()}
     */
    Optional<Boolean> featureEnabled(C config, String featureIdentifier);

    /**
     * Provide the proportion of users that should be enabled for the given feature. The proportion should be a double from 0.0 to 1.0.
     * 0.0 means no users will have the feature enabled, and 1.0 will mean that all users will have the feature enabled.
     * Values below zero will be treated the same as 0.0 and values above 1.0 will be treated the same as 1.0.
     * Returning Optional.empty() is equivalent to 0.0; both will return false for all users.
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return some proportion between 0.0 and 1.0, or {@link Optional#empty()}
     */
    Optional<Double> enabledProportion(C config, String featureIdentifier);

    /**
     * Provide the featureGroup that the feature should belong to. This will affect the {@link ConfigDeciders#proportionOfUsers(ConfigReader)} so that
     * if two features with the same featureGroup have the same enabled proportion, the same users will be included in that proportion.
     * Returning Optional.empty() will result in the feature being grouped by itself (the featureIdentifier will be used as the featureGroup).
     *
     * @param config the config source
     * @param featureIdentifier the feature
     * @return the identifier of the feature group or {@link Optional#empty()}
     */
    Optional<String> featureGroup(C config, String featureIdentifier);
}
