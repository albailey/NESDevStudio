/*
 * PropertiesUtilities.java
 *
 * Created on August 23, 2007, 3:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 *
 * @author abailey
 */
public class PropertiesUtilities {
    
    /** Creates a new instance of PropertiesUtilities */
    private PropertiesUtilities() {
    }
    
    public static void loadPropsFile(Properties props, File file) throws Exception{
        FileInputStream fis = new FileInputStream( file);
        props.load(fis);
        fis.close();        
    }
    public static void loadPropsFile(Properties props, String fileName) throws Exception{
        FileInputStream fis = new FileInputStream( fileName);
        props.load(fis);
        fis.close();
    }
    public static void savePropsFile(Properties props, File file, String header) throws Exception{
        FileOutputStream fos = new FileOutputStream(file);
        props.store(fos, header);
        fos.close();        
    }
    public static void savePropsFile(Properties props, String fileName, String header) throws Exception{
        FileOutputStream fos = new FileOutputStream(fileName);
        props.store(fos, header);
        fos.close();
    }
    
    public static void updateBooleanEnvSetting(Properties props, String key, boolean value){
        props.setProperty(key, String.valueOf(value));
    }    
    public static void updateStringEnvSetting(Properties props, String key, String value){
        props.setProperty(key, value);
    }
    public static void updateIntegerEnvSetting(Properties props, String key, int value){
        props.setProperty(key, String.valueOf(value));
    }    
    public static void updateFloatEnvSetting(Properties props, String key, float value){
        props.setProperty(key, String.valueOf(value));
    }    
    
    
    public static String getStringEnvSetting(Properties props, String key, String defaultValue){      
        String tempVal = props.getProperty(key);
        if(tempVal == null){
            return defaultValue;
        } else {
            return tempVal;
        }
    }
    
    public static boolean getBooleanEnvSetting(Properties props, String key, boolean defaultValue){
        boolean val = defaultValue;
        String tempVal = props.getProperty(key);
        if(tempVal != null){
            try {
                val = new Boolean(tempVal).booleanValue();
            } catch(Exception e){
                System.err.println("Invalid format for property:" + key + " returning default");
                val = defaultValue;
            }
        }
        return val;
    }
    
    public static int getIntegerEnvSetting(Properties props, String key, int defaultValue){
        int val = defaultValue;
        String tempVal = props.getProperty(key);
        if(tempVal != null){
            try {
                val = new Integer(tempVal).intValue();
            } catch(Exception e){
                System.err.println("Invalid format for property:" + key + " returning default");
                val = defaultValue;
            }
        }
        return val;
    }
    
    public static float getFloatEnvSetting(Properties props, String key, float defaultValue){
        float val = defaultValue;
        String tempVal = props.getProperty(key);
        if(tempVal != null){
            try {
                val = new Float(tempVal).floatValue();
            } catch(Exception e){
                System.err.println("Invalid format for property:" + key + " returning default");
                val = defaultValue;
            }
        }
        return val;
    }
    
}
