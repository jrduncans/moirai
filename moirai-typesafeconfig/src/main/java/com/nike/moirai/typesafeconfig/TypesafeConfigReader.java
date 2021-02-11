package com.nike.moirai.typesafeconfig;

import com.nike.moirai.config.ConfigReader;
import com.typesafe.config.Config;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypesafeConfigReader implements ConfigReader<Config> {
    public static TypesafeConfigReader TYPESAFE_CONFIG_READER = new TypesafeConfigReader();

    private TypesafeConfigReader() {
    }

    /**
     * Reads the enabled list from the config at a path of "moirai.[featureIdentifier].enabledUserIds". For instance, for a
     * feature identifier of "foo.service.myfeature", the config value "moirai.foo.service.myfeature.enabledUserIds" will be read.
     * If that config path does not exist, an empty list of users will be provided.
     *
     * @see ConfigReader#enabledUsers(Object, String)
     */
    @Override
    public Collection<String> enabledUsers(Config config, String featureIdentifier) {
        String path = String.format("moirai.%s.enabledUserIds", featureIdentifier);
        return TypesafeConfigExtractor.extractCollection(config, path, Config::getStringList);
    }

    /**
     * Reads the boolean value from the config at a path of "moirai.[featureIdentifier].featureEnabled". For instance, for a
     * feature identifier of "foo.service.myfeature", the config value "moirai.foo.service.myfeature.featureEnabled" will be read. If that config
     * path does not exist, {@link Optional#empty()} will be provided.
     *
     * @see ConfigReader#featureEnabled(Object, String)
     */
    @Override
    public Optional<Boolean> featureEnabled(Config config, String featureIdentifier) {
        String path = String.format("moirai.%s.featureEnabled", featureIdentifier);
        return TypesafeConfigExtractor.extractOptional(config, path, Config::getBoolean);
    }

    /**
     * Reads the enabled proportion of users from the config at a path of "moirai.[featureIdentifier].enabledProportion". For instance, for a
     * feature identifier of "foo.service.myfeature", the config value "moirai.foo.service.myfeature.enabledProportion" will be read. If that config
     * path does not exist, {@link Optional#empty()} will be provided.
     *
     * @see ConfigReader#enabledProportion(Object, String)
     */
    @Override
    public Optional<Double> enabledProportion(Config config, String featureIdentifier) {
        String path = String.format("moirai.%s.enabledProportion", featureIdentifier);
        return TypesafeConfigExtractor.extractOptional(config, path, Config::getDouble);
    }

    /**
     * Reads the feature group from the config at a path of "moirai.[featureIdentifier].featureGroup". For instance, for a feature identifier
     * of "foo.service.myfeature", the config value "moirai.foo.service.myfeature.featureGroup" will be read. If that config path does not exist,
     * {@link Optional#empty()} will be provided.
     *
     * @param config            the config source
     * @param featureIdentifier the feature
     * @see ConfigReader#featureGroup(Object, String)
     */
    @Override
    public Optional<String> featureGroup(Config config, String featureIdentifier) {
        String path = String.format("moirai.%s.featureGroup", featureIdentifier);
        return TypesafeConfigExtractor.extractOptional(config, path, Config::getString);
    }

    /**
     * Reads the enabled values from the config at a path of of "moirai.[featureIdentifier].[configKey]". For instance, for a
     * feature identifier of "foo.service.myfeature" and a configKey of "enabledCountries", the config value "moirai.foo.service.myfeature.enabledCountries"
     * will be read. If that config path does not exist, an empty list of users will be provided.
     *
     * @param configKey  the key used for the enabled values for the dimension
     * @param conversion a function to convert the values in the config from strings to the data-type used in your custom dimension
     * @param <V>        the data-type of your custom dimension values
     * @return the collection of enabled values
     */
    public <V> Collection<V> enabledValues(Config config, String featureIdentifier, String configKey, Function<String, V> conversion) {
        String path = String.format("moirai.%s.%s", featureIdentifier, configKey);

        return TypesafeConfigExtractor.extractCollection(config, path, Config::getStringList)
            .stream().map(conversion).collect(Collectors.toList());
    }
}
