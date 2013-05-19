/*
 * LevelEditorSettings.java
 *
 * Created on May 25, 2008, 11:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package gameTools.levelEditor;

import java.awt.Color;

import utilities.EnvironmentUtilities;

/**
 *
 * @author abailey
 */
public class LevelEditorSettings {

    public static final String LEVEL_EDITOR_PREFS_GROUP = "LEVEL_EDITOR_PREFS_GROUP";
    // The following are constants used for saving preferences, GUI strings, etc..
    public static final String SHOW_TILE_GRID_TITLE = "Show Tile Grid";
    public static final String SHOW_TILE_GRID_TOOLTIP = "Show grid for every tile";
    public static final String LEVEL_EDITOR_SHOW_TILE_GRID = "LEVEL_EDITOR_SHOW_TILE_GRID";
    public static final boolean DEFAULT_LEVEL_EDITOR_SHOW_TILE_GRID = false;
    public static final String SHOW_OAM_GRID_TITLE = "Show OAM Grid";
    public static final String SHOW_OAM_GRID_TOOLTIP = "Show grid to indicate OAM region";
    public static final String LEVEL_EDITOR_SHOW_OAM_GRID = "LEVEL_EDITOR_SHOW_OAM_GRID";
    public static final boolean DEFAULT_LEVEL_EDITOR_SHOW_OAM_GRID = true;
    public static final String SHOW_SCREEN_BOUNDS_TITLE = "Show Screen Bounds";
    public static final String SHOW_SCREEN_BOUNDS_TOOLTIP = "Show Bounds of each screen";
    public static final String LEVEL_EDITOR_SHOW_SCREEN_BOUNDS = "LEVEL_EDITOR_SHOW_SCREEN_BOUNDS";
    public static final boolean DEFAULT_LEVEL_EDITOR_SHOW_SCREEN_BOUNDS = true;
    public static final String LEVEL_TILES_SCALE_TITLE = "Scale";
    public static final String LEVEL_TILES_SCALE_TOOLTIP = "Amount to scale each tile to make editing easier. This is viewer only and does not affect the level data.";
    public static final String LEVEL_EDITOR_LEVEL_TILES_SCALE = "LEVEL_EDITOR_LEVEL_TILES_SCALE";
    public static final int DEFAULT_LEVEL_EDITOR_LEVEL_TILES_SCALE = 1;
    public static final int MIN_LEVEL_EDITOR_LEVEL_TILES_SCALE = 1;
    public static final int MAX_LEVEL_EDITOR_LEVEL_TILES_SCALE = 10;
    public static final int STEP_LEVEL_EDITOR_LEVEL_TILES_SCALE = 1;
    public static final String LEVEL_TILES_WIDE_TITLE = "Tiles Wide";
    public static final String LEVEL_TILES_WIDE_TOOLTIP = "Number of tiles wide to make the screen. For a horizontal scroller, make this 32";
    public static final String LEVEL_EDITOR_LEVEL_TILES_WIDE = "LEVEL_EDITOR_LEVEL_TILES_WIDE";
    public static final int DEFAULT_LEVEL_EDITOR_LEVEL_TILES_WIDE = 32; // 32 x 8
    public static final int MIN_LEVEL_EDITOR_LEVEL_TILES_WIDE = 2; // 32 x 1
    public static final int MAX_LEVEL_EDITOR_LEVEL_TILES_WIDE = 32; // 32 x 32
    public static final int STEP_LEVEL_EDITOR_LEVEL_TILES_WIDE = 2; // It makes it easier.  I can drop this to a lower number later
    // At the moment this level editor is intended for a horizontal platformer.  I'll need to make improvements to support vertical scrolling
    public static final String LEVEL_TILES_HIGH_TITLE = "Tiles High";
    public static final String LEVEL_TILES_HIGH_TOOLTIP = "Number of tiles high to make this entire level. For a horizontal scroller, make it less than 30 to support a status bar.";
    public static final String LEVEL_EDITOR_LEVEL_TILES_HIGH = "LEVEL_EDITOR_LEVEL_TILES_HIGH";
    public static final int DEFAULT_LEVEL_EDITOR_LEVEL_TILES_HIGH = 30;
    public static final int MIN_LEVEL_EDITOR_LEVEL_TILES_HIGH = 2;
    public static final int MAX_LEVEL_EDITOR_LEVEL_TILES_HIGH = 30;
    public static final int STEP_LEVEL_EDITOR_LEVEL_TILES_HIGH = 2;
    public static final String LEVEL_SCREENS_WIDE_TITLE = "Number of Screens Wide";
    public static final String LEVEL_SCREENS_WIDE_TOOLTIP = "Number of screens wide to make the level. For a vertical scroller, this should be 1.";
    public static final String LEVEL_EDITOR_LEVEL_SCREENS_WIDE = "LEVEL_EDITOR_LEVEL_SCREENS_WIDE";
    public static final int DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_WIDE = 8;
    public static final int MIN_LEVEL_EDITOR_LEVEL_SCREENS_WIDE = 1; // 32 x 1
    public static final int MAX_LEVEL_EDITOR_LEVEL_SCREENS_WIDE = 32; // 32 x 32
    public static final int STEP_LEVEL_EDITOR_LEVEL_SCREENS_WIDE = 1;
    // At the moment this level editor is intended for a horizontal platformer.  I'll need to make improvements to support vertical scrolling
    public static final String LEVEL_SCREENS_HIGH_TITLE = "Number of Screens High";
    public static final String LEVEL_SCREENS_HIGH_TOOLTIP = "Number of screens high to make this entire level. For a horizontal scroller, this should be 1.";
    public static final String LEVEL_EDITOR_LEVEL_SCREENS_HIGH = "LEVEL_EDITOR_LEVEL_SCREENS_HIGH";
    public static final int DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_HIGH = 1;
    public static final int MIN_LEVEL_EDITOR_LEVEL_SCREENS_HIGH = 1;
    public static final int MAX_LEVEL_EDITOR_LEVEL_SCREENS_HIGH = 32;
    public static final int STEP_LEVEL_EDITOR_LEVEL_SCREENS_HIGH = 1;
    public static final String LEVEL_EDITOR_LEVEL_LAST_FILENAME = "LEVEL_EDITOR_LEVEL_LAST_FILENAME";
    public static final String DEFAULT_LEVEL_EDITOR_LEVEL_LAST_FILENAME = null;
    private int tilesWide = DEFAULT_LEVEL_EDITOR_LEVEL_TILES_WIDE;
    private int tilesHigh = DEFAULT_LEVEL_EDITOR_LEVEL_TILES_HIGH;
    private int screensWide = DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_WIDE;
    private int screensHigh = DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_HIGH;
    private boolean showTileGrid = DEFAULT_LEVEL_EDITOR_SHOW_TILE_GRID;
    private boolean showOAMGrid = DEFAULT_LEVEL_EDITOR_SHOW_OAM_GRID;
    private boolean showScreenBounds = DEFAULT_LEVEL_EDITOR_SHOW_SCREEN_BOUNDS;
    private int scale = DEFAULT_LEVEL_EDITOR_LEVEL_TILES_SCALE;
    private String lastLevelFileName = null;
    private String _grp = null;

    /** Creates a new instance of LevelEditorSettings */
    public LevelEditorSettings() {
        this(LEVEL_EDITOR_PREFS_GROUP);
    }

    public LevelEditorSettings(String grp) {
        _grp = grp;

        tilesWide = EnvironmentUtilities.getIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_TILES_WIDE, DEFAULT_LEVEL_EDITOR_LEVEL_TILES_WIDE);
        tilesHigh = EnvironmentUtilities.getIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_TILES_HIGH, DEFAULT_LEVEL_EDITOR_LEVEL_TILES_HIGH);
        screensWide = EnvironmentUtilities.getIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_SCREENS_WIDE, DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_WIDE);
        screensHigh = EnvironmentUtilities.getIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_SCREENS_HIGH, DEFAULT_LEVEL_EDITOR_LEVEL_SCREENS_HIGH);
        showTileGrid = EnvironmentUtilities.getBooleanEnvSetting(_grp, LEVEL_EDITOR_SHOW_TILE_GRID, DEFAULT_LEVEL_EDITOR_SHOW_TILE_GRID);
        showOAMGrid = EnvironmentUtilities.getBooleanEnvSetting(_grp, LEVEL_EDITOR_SHOW_OAM_GRID, DEFAULT_LEVEL_EDITOR_SHOW_OAM_GRID);
        showScreenBounds = EnvironmentUtilities.getBooleanEnvSetting(_grp, LEVEL_EDITOR_SHOW_SCREEN_BOUNDS, DEFAULT_LEVEL_EDITOR_SHOW_SCREEN_BOUNDS);
        scale = EnvironmentUtilities.getIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_TILES_SCALE, DEFAULT_LEVEL_EDITOR_LEVEL_TILES_SCALE);
        lastLevelFileName = EnvironmentUtilities.getStringEnvSetting(_grp, LEVEL_EDITOR_LEVEL_LAST_FILENAME, DEFAULT_LEVEL_EDITOR_LEVEL_LAST_FILENAME);
    }

    public Color getTileGridColor() {
        return Color.BLACK;
    }

    public Color getOAMGridColor() {
        return Color.CYAN;
    }

    public Color getScreenBoundsColor() {
        return Color.DARK_GRAY;
    }

    public int getTilesWide() {
        return tilesWide;
    }

    public boolean setTilesWide(int val) {
        boolean ret = true;
        if (val < MIN_LEVEL_EDITOR_LEVEL_TILES_WIDE || val > MAX_LEVEL_EDITOR_LEVEL_TILES_WIDE) {
            System.err.println("Invalid  tiles Wide [" + val + "]. Value must be between :" + MIN_LEVEL_EDITOR_LEVEL_TILES_WIDE + " <= N <=  " + MAX_LEVEL_EDITOR_LEVEL_TILES_WIDE);
            ret = false;
        } else {
            tilesWide = val;
            EnvironmentUtilities.updateIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_TILES_WIDE, val);
        }
        return ret;
    }

    public int getTilesHigh() {
        return tilesHigh;
    }

    public boolean setTilesHigh(int val) {
        boolean ret = true;
        if (val < MIN_LEVEL_EDITOR_LEVEL_TILES_HIGH || val > MAX_LEVEL_EDITOR_LEVEL_TILES_HIGH) {
            System.err.println("Invalid tiles High [" + val + "]. Value must be between :" + MIN_LEVEL_EDITOR_LEVEL_TILES_HIGH + " <= N <=  " + MAX_LEVEL_EDITOR_LEVEL_TILES_HIGH);
            ret = false;
        } else {
            tilesHigh = val;
            EnvironmentUtilities.updateIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_TILES_HIGH, val);
        }
        return ret;
    }

    public int getScreensWide() {
        return screensWide;
    }

    public boolean setScreensWide(int val) {
        boolean ret = true;
        if (val < MIN_LEVEL_EDITOR_LEVEL_SCREENS_WIDE || val > MAX_LEVEL_EDITOR_LEVEL_SCREENS_WIDE) {
            System.err.println("Invalid  screens Wide [" + val + "]. Value must be between :" + MIN_LEVEL_EDITOR_LEVEL_SCREENS_WIDE + " <= N <=  " + MAX_LEVEL_EDITOR_LEVEL_SCREENS_WIDE);
            ret = false;
        } else {
            screensWide = val;
            EnvironmentUtilities.updateIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_SCREENS_WIDE, val);
        }
        return ret;
    }

    public int getScreensHigh() {
        return screensHigh;
    }

    public boolean setScreensHigh(int val) {
        boolean ret = true;
        if (val < MIN_LEVEL_EDITOR_LEVEL_SCREENS_HIGH || val > MAX_LEVEL_EDITOR_LEVEL_SCREENS_HIGH) {
            System.err.println("Invalid screens High [" + val + "]. Value must be between :" + MIN_LEVEL_EDITOR_LEVEL_SCREENS_HIGH + " <= N <=  " + MAX_LEVEL_EDITOR_LEVEL_SCREENS_HIGH);
            ret = false;
        } else {
            screensHigh = val;
            EnvironmentUtilities.updateIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_SCREENS_HIGH, val);
        }
        return ret;
    }

    public boolean getShowTileGrid() {
        return showTileGrid;
    }

    public boolean setShowTileGrid(boolean val) {
        showTileGrid = val;
        EnvironmentUtilities.updateBooleanEnvSetting(_grp, LEVEL_EDITOR_SHOW_TILE_GRID, val);
        return true;
    }

    public boolean getShowOAMGrid() {
        return showOAMGrid;
    }

    public boolean setShowOAMGrid(boolean val) {
        showOAMGrid = val;
        EnvironmentUtilities.updateBooleanEnvSetting(_grp, LEVEL_EDITOR_SHOW_OAM_GRID, val);
        return true;
    }

    public boolean getShowScreenBounds() {
        return showScreenBounds;
    }

    public boolean setShowScreenBounds(boolean val) {
        showScreenBounds = val;
        EnvironmentUtilities.updateBooleanEnvSetting(_grp, LEVEL_EDITOR_SHOW_SCREEN_BOUNDS, val);
        return true;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int val) {
        if (val < MIN_LEVEL_EDITOR_LEVEL_TILES_SCALE || val > MAX_LEVEL_EDITOR_LEVEL_TILES_SCALE) {
            System.err.println("Invalid scale [" + val + "]. Value must be between :" + MIN_LEVEL_EDITOR_LEVEL_TILES_SCALE + " < N <  " + MAX_LEVEL_EDITOR_LEVEL_TILES_SCALE);
        } else {
            scale = val;
            EnvironmentUtilities.updateIntegerEnvSetting(_grp, LEVEL_EDITOR_LEVEL_TILES_SCALE, val);
        }
    }

    public String getLastLevelFileName() {
        return lastLevelFileName;
    }

    public boolean setLastLevelFileName(String val) {
        if (val == null) {
            lastLevelFileName = "";
        } else {
            lastLevelFileName = val;
        }
        EnvironmentUtilities.updateStringEnvSetting(_grp, LEVEL_EDITOR_LEVEL_LAST_FILENAME, lastLevelFileName);
        return true;
    }
}
