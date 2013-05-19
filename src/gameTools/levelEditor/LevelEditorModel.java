/*
 * LevelEditorModel.java
 *
 * Created on May 25, 2008, 11:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package gameTools.levelEditor;

import java.io.File;

import utilities.PropertiesWrapper;

/**
 *
 * @author abailey
 */
public class LevelEditorModel extends PropertiesWrapper {

    public final static String LEVEL_EDITOR_SCREENS_WIDE = "LEVEL_EDITOR_SCREENS_WIDE_";
    public final static String LEVEL_EDITOR_SCREENS_HIGH = "LEVEL_EDITOR_SCREENS_HIGH_";
    public final static String LEVEL_EDITOR_TILES_WIDE = "LEVEL_EDITOR_TILES_WIDE_";
    public final static String LEVEL_EDITOR_TILES_HIGH = "LEVEL_EDITOR_TILES_HIGH_";
    public final static String LEVEL_EDITOR_TILE_HEIGHT = "LEVEL_EDITOR_TILE_HEIGHT_FIXED_";
    public final static String LEVEL_EDITOR_TILE_WIDTH = "LEVEL_EDITOR_TILE_WIDTH_FIXED_";
    public final static String LEVEL_EDITOR_PALETTE_FILE = "LEVEL_EDITOR_PALETTE_FILE_";
    public final static String LEVEL_EDITOR_COLUMN_FILE  = "LEVEL_EDITOR_COLUMN_FILE_";
    public final static String LEVEL_EDITOR_BG_STAMPS_FILE = "LEVEL_EDITOR_BG_STAMPS_FILE_";
    public final static String LEVEL_EDITOR_SPRITE_STAMPS_FILE = "LEVEL_EDITOR_SPRITE_STAMPS_FILE_";
    // maybe someday I'll support 8x16 tiles
    private static final int TILE_WIDTH = 8;
    private static final int TILE_HEIGHT = 8;
    private int index = 0;

    // since some calls are super expensive
    private final static int UNINITIALIZED = -1;
    private int screensWide;
    private int screensHigh;
    private int tilesWide;
    private int tilesHigh;
    private int tileWidth;
    private int tileHeight;
    
    
    /** Creates a new instance of LevelEditorModel */
    public LevelEditorModel(int index) {
        super();
        screensWide = UNINITIALIZED;
        screensHigh = UNINITIALIZED;
        tilesWide = UNINITIALIZED;
        tilesHigh = UNINITIALIZED;
        tileWidth = UNINITIALIZED;
        tileHeight = UNINITIALIZED;
        
        this.index = index;
    }

    public String getPaletteFileName() {
        return getStringEnvSetting(LEVEL_EDITOR_PALETTE_FILE, index, null);
    }

    public void setPaletteFileName(String val) {
        setStringEnvSetting(LEVEL_EDITOR_PALETTE_FILE, index, val);
    }
    
    public String getColumnFileName() {
        return getStringEnvSetting(LEVEL_EDITOR_COLUMN_FILE, index, null);
    }
    
    public void setColumnFileName(String val) {
        setStringEnvSetting(LEVEL_EDITOR_COLUMN_FILE, index, val);
    }
    
    public String getBGStampsFileName() {
        return getStringEnvSetting(LEVEL_EDITOR_BG_STAMPS_FILE, index, null);
    }

    public void setBGStampsFileName(String val) {
        setStringEnvSetting(LEVEL_EDITOR_BG_STAMPS_FILE, index, val);
    }

    public String getSpriteStampsFileName() {
        return getStringEnvSetting(LEVEL_EDITOR_SPRITE_STAMPS_FILE, index, null);
    }

    public void setSpriteStampsFileName(String val) {
        setStringEnvSetting(LEVEL_EDITOR_SPRITE_STAMPS_FILE, index, val);
    }

    public int getTilesWide() {
    	if(tilesWide == UNINITIALIZED){
    		tilesWide = getIntegerEnvSetting(LEVEL_EDITOR_TILES_WIDE, index, ((index == 0) ? LevelEditorSettings.DEFAULT_LEVEL_EDITOR_LEVEL_TILES_WIDE : 2)); 
    	}
        return tilesWide;
    }

    public void setTilesWide(int val) {
    	tilesWide = val;
        setIntegerEnvSetting(LEVEL_EDITOR_TILES_WIDE, index, val);
    }

    
    public int getTilesHigh() {
    	if(tilesHigh == UNINITIALIZED){
    		tilesHigh =  getIntegerEnvSetting(LEVEL_EDITOR_TILES_HIGH, index, ((index == 0) ? LevelEditorSettings.DEFAULT_LEVEL_EDITOR_LEVEL_TILES_HIGH : 2));
    	}
    	return tilesHigh;
    }

    public void setTilesHigh(int val) {
    	tilesHigh = val;
        setIntegerEnvSetting(LEVEL_EDITOR_TILES_HIGH, index, val);
    }

    public int getScreensWide() {
    	if(screensWide == UNINITIALIZED){
    		screensWide =  getIntegerEnvSetting(LEVEL_EDITOR_SCREENS_WIDE, index, ((index == 0) ? LevelEditorSettings.DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_WIDE : 1));
    	}
    	return screensWide;
    }

    public void setScreensWide(int val) {
    	screensWide = val;
        setIntegerEnvSetting(LEVEL_EDITOR_SCREENS_WIDE, index, val);
    }

    public int getScreensHigh() {
    	if(screensHigh == UNINITIALIZED){
    		screensHigh = getIntegerEnvSetting(LEVEL_EDITOR_SCREENS_HIGH, index, ((index == 0) ? LevelEditorSettings.DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_HIGH : 1));
    	}
    	return screensHigh;
    }

    public void setScreensHigh(int val) {
    	screensHigh = val;
        setIntegerEnvSetting(LEVEL_EDITOR_SCREENS_HIGH, index, val);
    }

    // no setter for these....
    public int getTileHeight() {
    	if(tileHeight == UNINITIALIZED) {
    		tileHeight =  getIntegerEnvSetting(LEVEL_EDITOR_TILE_HEIGHT, index, TILE_HEIGHT);
    	}
    	return tileHeight;
    }

    public int getTileWidth() {
    	if(tileWidth == UNINITIALIZED) {
    		tileWidth = getIntegerEnvSetting(LEVEL_EDITOR_TILE_WIDTH, index, TILE_WIDTH);
    	}
    	return tileWidth;
    }

    public void storeFile(File selectedFile) throws Exception {
        super.storeFile(selectedFile);
    }
}
