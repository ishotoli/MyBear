package io.mybear.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ApplicationContext {
    private static final String DEFAULT_CONFIG_FILE_NAME = "config.properties";

    private static final Properties properties = new Properties();

    private static ApplicationContext INSTANCE;

    public ApplicationContext() throws IOException{
        this(DEFAULT_CONFIG_FILE_NAME);
    }

    public ApplicationContext(String... filePath) throws IOException {
        for (String path : filePath) {
            URL url = getClass().getClassLoader().getResource(path);
            properties.load(new FileInputStream(url.getFile()));
        }

        INSTANCE = this;
    }

    public static ApplicationContext getInstance(){
        return INSTANCE;
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public int getIntValue(String propertyName, int defaultValue){
        try {
            return Integer.parseInt(properties.getProperty(propertyName));
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    @Override
    public String toString() {
        return "ApplicationContext{properties=" + properties + "}";
    }
}
