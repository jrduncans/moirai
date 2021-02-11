package com.nike.moirairiposteexample;

import com.google.common.io.Resources;
import com.nike.moirai.ConfigFeatureFlagChecker;
import com.nike.moirai.Suppliers;
import com.nike.moirai.config.ConfigDecisionInput;
import com.nike.moirai.typesafeconfig.TypesafeConfigLoader;
import com.nike.moirairiposteexample.endpoints.GetShoeListEndpoint;
import com.nike.riposte.server.Server;
import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.server.logging.AccessLogger;
import com.typesafe.config.Config;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.nike.moirai.config.ConfigDeciders.enabledUsers;
import static com.nike.moirai.config.ConfigDeciders.proportionOfUsers;
import static com.nike.moirai.typesafeconfig.TypesafeConfigReader.TYPESAFE_CONFIG_READER;

public class Main {

    public static class AppServerConfig implements ServerConfig {

        ConfigFeatureFlagChecker<Config> featureFlagChecker() {
            Predicate<ConfigDecisionInput<Config>> whiteListedUsersDecider =
                enabledUsers(TYPESAFE_CONFIG_READER).or(proportionOfUsers(TYPESAFE_CONFIG_READER));

            String conf;
                try {
                    conf = Resources.toString(Resources.getResource("moirai.conf"), Charset.forName("UTF-8"));
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
                Supplier<Config> supp = Suppliers.supplierAndThen(() -> conf, TypesafeConfigLoader.FROM_STRING);
                return ConfigFeatureFlagChecker.forConfigSupplier(supp, whiteListedUsersDecider);
        }
        private final Collection<Endpoint<?>> endpoints = Collections.singleton(new GetShoeListEndpoint(featureFlagChecker()));
        private final AccessLogger accessLogger = new AccessLogger();

        @Override
        public Collection<Endpoint<?>> appEndpoints() {
            return endpoints;
        }

        @Override
        public AccessLogger accessLogger() {
            return accessLogger;
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(new AppServerConfig());
        server.startup();
    }
}