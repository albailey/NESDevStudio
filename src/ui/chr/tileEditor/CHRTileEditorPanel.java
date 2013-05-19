/*
 * CHRTileEditorPanel.java
 *
 * Created on September 29, 2006, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.tileEditor;

import java.awt.Color;
import java.awt.Graphics;

import ui.chr.model.CHREditorModel;

/**
 * Its a grid.
 * @author abailey
 */
public class CHRTileEditorPanel extends CHRTilePanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 6837660418370391107L;

	public static final String CHR_TILE_EDITOR_PROPERTY_GROUP    = "CHRTileEditor";
    
    protected int selectedIndex = 0;    
    
    /**
     * Creates a new instance of CHRTileEditorPanel
     */
    public CHRTileEditorPanel( CHREditorModel model, int newPaletteType, int width, int height) {
        super( model, newPaletteType, width, height, CHR_TILE_EDITOR_PROPERTY_GROUP);
        selectedIndex = 0;
    }
    
  

    
 protected void processLeftClick(int index){
        // select old index
        selectedIndex = index;
        tile.setPixelIndex(index, currentColorIndex);
        repaint();
    }
    
    protected void processRightClick(int index){
        selectedIndex = index;
        
        int oldVal = tile.getPixelIndex(index);
        currentColorIndex = (oldVal + 1) % 4;
        tile.setPixelIndex(index, currentColorIndex);
       
        repaint();
    }
    
    protected void drawPixelOutline(Graphics g, int curIndex, int x, int y){
        if(controls.getShowTileGrid()){
                    g.setColor(Color.BLACK);
                    g.drawRect(x*CHR_PIXEL_WIDTH,y*CHR_PIXEL_HEIGHT,CHR_PIXEL_WIDTH,CHR_PIXEL_HEIGHT);
        }    
        if(curIndex == selectedIndex && controls.getShowSelection()){
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(x*CHR_PIXEL_WIDTH,y*CHR_PIXEL_HEIGHT,CHR_PIXEL_WIDTH,CHR_PIXEL_HEIGHT);
        }          
    }    
}
