package com.nike.moirai.typesafeconfig;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.function.Function;

public class TypesafeConfigLoader {
    public static final Function<String, Config> FROM_STRING = s -> {
        Config config = ConfigFactory.parseString(s);
        return config.resolve();
    };

    private TypesafeConfigLoader() {
        // Prevent instantiation
    }
}
