/*
 * CHRDisplayControls.java
 *
 * Created on November 1, 2006, 3:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr;

import java.awt.Color;

import utilities.EnvironmentUtilities;

/**
 *
 * @author abailey
 */
public class CHRDisplayControls {
	public static final Color OAM_COLOR = new Color(190,190,64,90); 
	public static final Color GRID_COLOR = new Color(128,32,128,90); 
	
	
    // Settings properties that are persisted
    public static final String CHR_DISPLAY_SHOW_TILE_GRID_PROPERTY = "CHRDisplayShowTileGrid";
    public static final boolean DEFAULT_CHR_DISPLAY_SHOW_TILE_GRID_PROPERTY = false;

    public static final String CHR_DISPLAY_SHOW_OAM_GRID_PROPERTY = "CHRDisplayShowOAMGrid";
    public static final boolean DEFAULT_CHR_DISPLAY_SHOW_OAM_GRID_PROPERTY = false;
    
    public static final String CHR_DISPLAY_SHOW_OAM_SELECTION_PROPERTY = "CHRDisplayShowOAMSelection";
    public static final boolean DEFAULT_CHR_DISPLAY_SHOW_OAM_SELECTION_PROPERTY = true;
    
    public static final String CHR_DISPLAY_SHOW_PAGE_GRID_PROPERTY = "CHRDisplayShowPageGrid";
    public static final boolean DEFAULT_CHR_DISPLAY_SHOW_PAGE_GRID_PROPERTY = false;
    
    public static final String CHR_DISPLAY_SHOW_SELECTION_PROPERTY = "CHRDisplayShowSelection";
    public static final boolean DEFAULT_CHR_DISPLAY_SHOW_SELECTION_PROPERTY = true;
    
    public static final String CHR_DISPLAY_SCALE_PROPERTY = "CHRDisplayScale";
    public static final int DEFAULT_CHR_DISPLAY_SCALE_PROPERTY = 1;
    
    private boolean _showTileGrid = DEFAULT_CHR_DISPLAY_SHOW_TILE_GRID_PROPERTY;
    private boolean _showOAMGrid = DEFAULT_CHR_DISPLAY_SHOW_OAM_GRID_PROPERTY;
    private boolean _showPageGrid = DEFAULT_CHR_DISPLAY_SHOW_PAGE_GRID_PROPERTY;
    private boolean _showOAMSelection = DEFAULT_CHR_DISPLAY_SHOW_OAM_SELECTION_PROPERTY;
    private boolean _showSelection = DEFAULT_CHR_DISPLAY_SHOW_SELECTION_PROPERTY;
    private int     _scale = DEFAULT_CHR_DISPLAY_SCALE_PROPERTY;
    private String _propertyGroup;
    
    /** Creates a new instance of CHRDisplayControls */
    public CHRDisplayControls(String propertyGroup) {
        _propertyGroup = propertyGroup;
        _showTileGrid = EnvironmentUtilities.getBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_TILE_GRID_PROPERTY, DEFAULT_CHR_DISPLAY_SHOW_TILE_GRID_PROPERTY );
        _showOAMGrid = EnvironmentUtilities.getBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_OAM_GRID_PROPERTY, DEFAULT_CHR_DISPLAY_SHOW_OAM_GRID_PROPERTY );
        _showOAMSelection = EnvironmentUtilities.getBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_OAM_SELECTION_PROPERTY, DEFAULT_CHR_DISPLAY_SHOW_OAM_SELECTION_PROPERTY );
        _showPageGrid = EnvironmentUtilities.getBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_PAGE_GRID_PROPERTY, DEFAULT_CHR_DISPLAY_SHOW_PAGE_GRID_PROPERTY );
        _showSelection = EnvironmentUtilities.getBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_SELECTION_PROPERTY, DEFAULT_CHR_DISPLAY_SHOW_SELECTION_PROPERTY );
        _scale = EnvironmentUtilities.getIntegerEnvSetting(_propertyGroup, CHR_DISPLAY_SCALE_PROPERTY, DEFAULT_CHR_DISPLAY_SCALE_PROPERTY );
    }
    
    
    public void setShowTileGrid(boolean flag) {
        _showTileGrid  = flag;
         EnvironmentUtilities.updateBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_TILE_GRID_PROPERTY, _showTileGrid );
    }
    public boolean getShowTileGrid() {
        return _showTileGrid;
    }
    public Color getGridColor(){
        return GRID_COLOR;
    }
    public void setShowOAMGrid(boolean flag) {
        _showOAMGrid  = flag;
         EnvironmentUtilities.updateBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_OAM_GRID_PROPERTY, _showOAMGrid );
    }
    public boolean getShowOAMGrid() {
        return _showOAMGrid;
    }
    public Color getOAMGridColor(){
        return OAM_COLOR; 
    }
    public void setShowOAMSelection(boolean flag) {
        _showOAMSelection  = flag;
         EnvironmentUtilities.updateBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_OAM_SELECTION_PROPERTY, _showOAMSelection );
    }
    public boolean getShowOAMSelection() {
        return _showOAMSelection;
    }
    public Color getOAMSelectionColor(){
        return Color.CYAN;
    }
    public void setShowPageGrid(boolean flag) {
        _showPageGrid  = flag;
         EnvironmentUtilities.updateBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_PAGE_GRID_PROPERTY, _showPageGrid );
    }
    public boolean getShowPageGrid() {
        return _showPageGrid;
    }
    public Color getPageGridColor(){
        return Color.PINK;
    }
    
    public void setScale(int val) {
        _scale = val;
         EnvironmentUtilities.updateIntegerEnvSetting(_propertyGroup, CHR_DISPLAY_SCALE_PROPERTY, _scale );
    }
    
    public int getScale() {
        return _scale;
    }
  
    public void setShowSelection(boolean flag) {
        _showSelection = flag;
         EnvironmentUtilities.updateBooleanEnvSetting(_propertyGroup, CHR_DISPLAY_SHOW_SELECTION_PROPERTY, _showSelection );
    }
    
    public boolean getShowSelection() {
        return _showSelection;
    }
}
