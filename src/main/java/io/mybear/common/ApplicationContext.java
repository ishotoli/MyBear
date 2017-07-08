package io.mybear.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ApplicationContext {
    private static final String DEFAULT_CONFIG_FILE_NAME = "config.properties";

    private static final Properties properties = new Properties();

    public ApplicationContext() throws IOException{
        URL url = getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE_NAME);
        properties.load(new FileInputStream(url.getFile()));
    }

    public ApplicationContext(String... filePath) throws IOException {
        for (String path : filePath) {
            URL url = getClass().getClassLoader().getResource(path);
            properties.load(new FileInputStream(url.getFile()));
        }
    }


    public static void setProperties(Properties properties) {
        ApplicationContext.properties = properties;
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public String toString() {
        return "ApplicationContext{properties=" + properties + "}";
    }
}
