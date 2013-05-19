/*
 * PropertiesWrapper.java
 *
 * Created on June 4, 2008, 9:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package utilities;

import java.io.File;
import java.util.Properties;

/**
 *
 * @author abailey
 */
public class PropertiesWrapper {
    
    public Properties props = null;
    
    /** Creates a new instance of PropertiesWrapper */
    public PropertiesWrapper() {
        props = new Properties();
        
    }
    public void loadFile(File selectedFile) throws Exception {
        PropertiesUtilities.loadPropsFile(props, selectedFile);
    }
    
    public void storeFile(File selectedFile) throws Exception {
        PropertiesUtilities.savePropsFile(props, selectedFile, null);
    }

    // just some methods to make accessing the props easier
    public String getStringEnvSetting(String format, int index, String defaultValue){
        return getStringEnvSetting(format + index, defaultValue);
    }
    public String getStringEnvSetting(String key, String defaultValue){
        return PropertiesUtilities.getStringEnvSetting(props, key, defaultValue);
    }
    public void setStringEnvSetting(String format, int index, String value){
        setStringEnvSetting(format+index, value);
    }
    public void setStringEnvSetting(String key, String value){
        PropertiesUtilities.updateStringEnvSetting(props, key, value);
    }

    public int getIntegerEnvSetting(String format, int index, int defaultValue){
        return getIntegerEnvSetting(format + index, defaultValue);
    }
    public int getIntegerEnvSetting(String key, int defaultValue){
        return PropertiesUtilities.getIntegerEnvSetting(props, key, defaultValue);
    }    
    public void setIntegerEnvSetting(String format, int index, int value){
        setIntegerEnvSetting(format+index, value);
    }
    public void setIntegerEnvSetting(String key, int value){
        PropertiesUtilities.updateIntegerEnvSetting(props, key, value);
    }    

    public boolean getBooleanEnvSetting(String format, int index, boolean defaultValue){
        return getBooleanEnvSetting(format + index, defaultValue);
    }
    public boolean getBooleanEnvSetting(String key, boolean defaultValue){
        return PropertiesUtilities.getBooleanEnvSetting(props, key, defaultValue);
    }
    public void setBooleanEnvSetting(String format, int index, boolean value){
        setBooleanEnvSetting(format+index, value);
    }
    public void setBooleanEnvSetting(String key, boolean value){
        PropertiesUtilities.updateBooleanEnvSetting(props, key, value);
    }
    
}
