/*
 * EnvironmentUtilities.java
 *
 * Created on August 1, 2007, 3:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package utilities;

import java.util.Properties;

/**
 *
 * @author abailey
 */
public class EnvironmentUtilities {
    
    public static final String LAST_PROJECT_DIR_SETTING = "LAST_PROJECT_DIR";
    public static final String NES_DEV_PROPERTIES_FILENAME = "NESDevUI.properties";
    public static final String NES_DEV_PROPERTIES_HEADER = "NES Dev UI Properties";
    
    public static Properties envSettings = new Properties();
    
    /** Creates a new instance of EnvironmentUtilities */
    private EnvironmentUtilities() {
    }
    
    public static void updateBooleanEnvSetting(String group, String key, boolean value){
        updateBooleanEnvSetting(group + "_" + key, value);
    }
    public static void updateIntegerEnvSetting(String group, String key, int value){
        updateIntegerEnvSetting(group + "_" + key, value);
    }
    public static void updateStringEnvSetting(String group, String key, String value){
        updateStringEnvSetting(group + "_" + key, value);
    }

    public static boolean getBooleanEnvSetting(String group, String key, boolean defaultValue){
        return getBooleanEnvSetting(group + "_" + key,defaultValue); // concatenate them
    }
    public static int getIntegerEnvSetting(String group, String key, int defaultValue){
        return getIntegerEnvSetting(group + "_" + key,defaultValue); // concatenate them
    }
    public static String getStringEnvSetting(String group, String key, String defaultValue){
        return getStringEnvSetting(group + "_" + key,defaultValue); // concatenate them
    }    
    
    public static void updateBooleanEnvSetting(String key, boolean value){
        PropertiesUtilities.updateBooleanEnvSetting(envSettings, key, value);
    }    
    public static void updateIntegerEnvSetting(String key, int value){
        PropertiesUtilities.updateIntegerEnvSetting(envSettings, key, value);
    }    
    public static void updateFloatEnvSetting(String key, float value){
        PropertiesUtilities.updateFloatEnvSetting(envSettings, key, value);
    }    
    public static void updateStringEnvSetting(String key, String value){
        PropertiesUtilities.updateStringEnvSetting(envSettings, key, value);
    }
        
    public static boolean getBooleanEnvSetting(String key, boolean defaultValue){
        return PropertiesUtilities.getBooleanEnvSetting(envSettings, key, defaultValue);
    }
    public static int getIntegerEnvSetting(String key, int defaultValue){
        return PropertiesUtilities.getIntegerEnvSetting(envSettings, key, defaultValue);
    }
    public static float getFloatEnvSetting(String key, float defaultValue){
        return PropertiesUtilities.getFloatEnvSetting(envSettings, key, defaultValue);
    }
    public static String getStringEnvSetting(String key, String defaultValue){
        return PropertiesUtilities.getStringEnvSetting(envSettings, key, defaultValue);
    }
    
    
    public static void initializeEnvironment(){
        try {
            PropertiesUtilities.loadPropsFile(envSettings, NES_DEV_PROPERTIES_FILENAME);
            String projDir = envSettings.getProperty(LAST_PROJECT_DIR_SETTING);
            if(projDir != null){
                System.setProperty("user.dir", projDir);
            }
        } catch(Exception e){
            System.err.println("Unable to initialize the properties file.  Creating a new one");
            //e.printStackTrace();
        }
    }
        
    public static void storeSettings(){
        try {
            PropertiesUtilities.savePropsFile(envSettings, NES_DEV_PROPERTIES_FILENAME, NES_DEV_PROPERTIES_HEADER);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
