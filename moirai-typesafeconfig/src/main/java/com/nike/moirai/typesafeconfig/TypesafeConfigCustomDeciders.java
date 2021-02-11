package com.nike.moirai.typesafeconfig;

import com.nike.moirai.config.ConfigDecisionInput;
import com.nike.moirai.config.EnabledCustomDimensionConfigDecider;
import com.typesafe.config.Config;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.nike.moirai.typesafeconfig.TypesafeConfigReader.TYPESAFE_CONFIG_READER;

/**
 * Predicate implementations that read from a Typesafe {@link Config}.
 */
public class TypesafeConfigCustomDeciders {

    /**
     * Reads the enabled values using {@link TypesafeConfigReader#enabledValues(Config, String, String, Function)}.
     *
     * Your custom dimension values must match the provided type. If they do not, then the predicate will return false and log a warning message.
     *
     * @param dimensionKey the key used for the dimension; this should match how you construct your FeatureCheckInput
     * @param configKey the key used for the enabled values for the dimension
     * @param conversion a function to convert the values in the config from strings to the data-type used in your custom dimension
     * @param <V> the data-type of your custom dimension values
     * @return a Predicate that will return true if the FeatureCheckInput has a value for your custom dimension that matches the values read from the Config
     */
    public static <V> Predicate<ConfigDecisionInput<Config>> enabledCustomDimension(String dimensionKey, String configKey, Function<String, V> conversion) {
        return new EnabledCustomDimensionConfigDecider<Config, V>() {
            @Override
            protected String dimensionKey() {
                return dimensionKey;
            }

            @Override
            protected Collection<V> enabledValues(Config config, String featureIdentifier) {
                return TYPESAFE_CONFIG_READER.enabledValues(config,featureIdentifier, configKey, conversion);
            }
        };
    }

    /**
     * Reads the enabled values using {@link TypesafeConfigReader#enabledValues(Config, String, String, Function)}.
     *
     * Your custom dimension values must be strings. If the values are not strings, then the predicate will return false and log a warning message.
     *
     * @param dimensionKey the key used for the dimension; this should match how you construct your FeatureCheckInput
     * @param configKey the key used for the configuration value to be read
     * @return a Predicate that will return true if the FeatureCheckInput has a value for your custom dimension that matches the values read from the Config
     */
    public static Predicate<ConfigDecisionInput<Config>> enabledCustomStringDimension(String dimensionKey, String configKey) {
        return enabledCustomDimension(dimensionKey, configKey, Function.identity());
    }

    private TypesafeConfigCustomDeciders() {
        // prevent instantiation
    }
}
