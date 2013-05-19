/*
 * CHRProject.java
 *
 * Created on February 20, 2007, 3:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.model;

import utilities.CompressionUtilities;
import utilities.PropertiesWrapper;

/**
 *
 * @author abailey
 */
public class CHRProject extends PropertiesWrapper {

    public final static String CHR_PATTERN_TABLE_KEY    = "CHR_FILE_";
    public final static String PALETTE_KEY              = "PALETTE_FILE_";
    public final static String NAME_TABLE_KEY           = "NAME_TABLE_";
    public final static String NAME_TABLE_COMP_KEY      = "NAME_TABLE_COMP_";
    public final static String ANIMATIONS_KEY           = "ANIMATIONS_FILE_";
            
    public final static String ASCII_KEY                = "ASCII_KEY_";
    public final static String ASCII_ENTRIES_KEY        = "ASCII_ENTRIES_KEY_";

    /** Creates a new instance of CHRProject */
    public CHRProject() {
       super();
    }
    
    public String getPatternTableFileName(){
        return getPatternTableFileName(0);
    }
    public String getPatternTableFileName(int index){
        return getStringEnvSetting(CHR_PATTERN_TABLE_KEY, index, null);
    }
    public void setPatternTableFileName(String val){
        setPatternTableFileName(val, 0);
    }
    public void setPatternTableFileName(String val, int index){
        setStringEnvSetting(CHR_PATTERN_TABLE_KEY, index, val);
    }
    
    public String getPaletteFileName(){
        return getPaletteFileName(0);
    }
    public String getPaletteFileName(int index){
        return getStringEnvSetting(PALETTE_KEY, index, null);
    }
   public void setPaletteFileName(String val){
        setPaletteFileName(val, 0);
    }
    public void setPaletteFileName(String val, int index){
        setStringEnvSetting(PALETTE_KEY, index, val);
    }

    public String getAnimationsFileName(){
        return getAnimationsFileName(0);
    }
    public String getAnimationsFileName(int index){
        return getStringEnvSetting(ANIMATIONS_KEY, index, null);
    }
   public void setAnimationsFileName(String val){
        setAnimationsFileName(val, 0);
    }
    public void setAnimationsFileName(String val, int index){
        setStringEnvSetting(ANIMATIONS_KEY, index, val);
    }
    
    public String getNameTableFileName(){
        return getNameTableFileName(0);
    }
    public String getNameTableFileName(int index){
        return getStringEnvSetting(NAME_TABLE_KEY, index, null);
    }
    public void setNameTableFileName(String val){
        setNameTableFileName(val, 0);
    }
    public void setNameTableFileName(String val, int index){
        setStringEnvSetting(NAME_TABLE_KEY, index, val);
    }    
    
    public int getNameTableCompressionType(){
        return getNameTableCompressionType(0);
    }
    public int getNameTableCompressionType(int index){
        return getIntegerEnvSetting(NAME_TABLE_COMP_KEY, index, CompressionUtilities.NO_COMPRESSION);
    }
    public void setNameTableCompressionType(int val){
        setNameTableCompressionType(val, 0);
    }
    public void setNameTableCompressionType(int val, int index){
        setIntegerEnvSetting(NAME_TABLE_COMP_KEY, index, val);
    }    
              
    public boolean getNameTableAsciiMode(){
        return getNameTableAsciiMode(0);
    }
    public boolean getNameTableAsciiMode(int index){
        return getBooleanEnvSetting(ASCII_KEY, index, true);
    }
    public void setNameTableAsciiMode(boolean val){
        setNameTableAsciiMode(val, 0);
    }
    public void setNameTableAsciiMode(boolean val, int index){
        setBooleanEnvSetting(ASCII_KEY, index, val);
    }
    
    public int getNameTableAsciiEntries(){
        return getNameTableAsciiEntries(0);
    }
    public int getNameTableAsciiEntries(int index){
        return getIntegerEnvSetting(ASCII_ENTRIES_KEY, index, 16);
    }
    public void setNameTableAsciiEntries(int val){
        setNameTableAsciiEntries(val, 0);
    }
    public void setNameTableAsciiEntries(int val, int index){
        setIntegerEnvSetting(ASCII_ENTRIES_KEY, index, val);
    }
 
}
