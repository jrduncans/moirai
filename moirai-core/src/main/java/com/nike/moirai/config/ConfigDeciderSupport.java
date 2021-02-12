package com.nike.moirai.config;

import com.nike.moirai.FeatureCheckInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * Support functions for creating a {@link java.util.function.Predicate} for {@link ConfigDecisionInput}.
 */
public class ConfigDeciderSupport {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigDeciderSupport.class);

    /**
     * Apply a predicate to the userId from a feature check input.
     *
     * @param featureCheckInput the input to check
     * @param userIdCheck       the predicate to apply
     * @return false if there is no userId in the input, otherwise the result of the predicate applied to the userId
     */
    public static boolean userIdCheck(FeatureCheckInput featureCheckInput, Predicate<String> userIdCheck) {
        return featureCheckInput.getUserId().map(userIdCheck::test).orElse(false);
    }

    /**
     * Apply a predicate to the dimension value from a feature check input.
     *
     * @param featureCheckInput the input to check
     * @param dimensionKey      the dimension to check
     * @param dimensionCheck    the predicate to apply
     * @param <V>               the type of value for the dimension
     * @return false if there is no userId in the input, otherwise the result of the predicate applied to the userId
     */
    public static <V> boolean customDimensionCheck(FeatureCheckInput featureCheckInput, String dimensionKey, Predicate<V> dimensionCheck) {
        return featureCheckInput.getDimension(dimensionKey).map(cast(dimensionCheck)::test).orElse(false);
    }

    private static <V> Predicate<Object> cast(Predicate<V> check) {
        return o -> {
            try {
                return check.test((V)o);
            } catch (ClassCastException e) {
                LOGGER.warn("Mismatched type found, got: " + o, e);
                return false;
            }
        };
    }

    private ConfigDeciderSupport() {
        // Prevent instantiation
    }
}
