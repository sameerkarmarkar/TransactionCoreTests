package com.unzer.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

public class Configuration {

    public static Configuration INSTANCE = new Configuration();

    static {
        conf = ConfigFactory.load();
    }

    private static Config conf;

    private Configuration() {

    }

    public String getProperty(String property) {
        try {
            return conf.getString(property);
        } catch (ConfigException e) {
            return null;
        }

    }
/*
    public String resolvePort(String property) {
        String propertyValue = getProperty(property);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return ":" + propertyValue;
        }
        return "";
    }*/
}

