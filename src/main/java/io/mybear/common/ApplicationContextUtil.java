package io.mybear.common;

public class ApplicationContextUtil {
    private static final String CONFIG_PROPERTY_ACTIVE_PROFILE = "active_profile";
    private static final String ENV_DEV = "dev";

    public static boolean isDev(){
        String activeProfile = ApplicationContext.getInstance().getProperty(CONFIG_PROPERTY_ACTIVE_PROFILE);
        return activeProfile != null && activeProfile.toLowerCase().equals(ENV_DEV);
    }

}
