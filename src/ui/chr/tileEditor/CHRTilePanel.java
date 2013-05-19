/*
 * CHRTilePanel.java
 *
 * Created on November 2, 2007, 2:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.tileEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import ui.chr.CHRDisplayControls;
import ui.chr.CHRDisplayInterface;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;

/**
 *
 * @author abailey
 */
public class CHRTilePanel extends JPanel implements CHRDisplayInterface {
    
   /**
	 * 
	 */
	private static final long serialVersionUID = -6629448606418179399L;

public static final String CHR_TILE_PANEL_PROPERTY_GROUP    = "CHRTilePanel";
            
    public final static int CHR_PIXEL_WIDTH = 10;
    public final static int CHR_PIXEL_HEIGHT = 10;
    
    protected CHRDisplayControls controls = null;
    protected CHREditorModel  modelRef = null;
    protected CHRTile tile = null;
//    protected int patternTableIndex = 0;
    protected int currentColorIndex = 0;
    protected int paletteType = 0;
    
    /** Creates a new instance of CHRTilePanel */
    public CHRTilePanel( CHREditorModel model, int newPaletteType, int width, int height, String propGroup) {
     //   patternTableIndex = 0;
        paletteType = newPaletteType;
        modelRef = model;
        controls = new CHRDisplayControls(propGroup);
        setTile(0, new byte[16]);
        setupUI();        
    }
    
    public byte[] getTileMask(){
        return tile.asMask();        
    }
    
    public void setTile(int patternIndex, byte b[]) throws IllegalArgumentException {       
     //   patternTableIndex = patternIndex;
        tile = new CHRTile(b,paletteType, modelRef);   
        repaint();
    }
    public void notifyDisplayInterfaceUpdated() {
        repaint();        
    }
     public CHRDisplayControls getControls() {
         return controls;         
     }
     
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);    // paints background
        // OK,  paint it ourselves
        int curIndex = 0;
        int pixIndexes[] = tile.getPixelIndexes();
        for(int y=0;y<tile.getHeight();y++){
            for(int x=0;x<tile.getWidth();x++){
                int val = pixIndexes[curIndex]; // value between zero and 4
                if(supportPixelIndex(val)){
                    if(paletteType == PPUConstants.IMAGE_PALETTE_TYPE){
                        g.setColor(modelRef.getImagePaletteColor(val));
                    } else if(paletteType == PPUConstants.SPRITE_PALETTE_TYPE){
                        g.setColor(modelRef.getSpritePaletteColor(val));
                    } else  { // NES palette
                        g.setColor(modelRef.getNESPaletteColor(val));
                    }

                    g.fillRect(x*CHR_PIXEL_WIDTH,y*CHR_PIXEL_HEIGHT,CHR_PIXEL_WIDTH,CHR_PIXEL_HEIGHT);
                    drawPixelOutline(g, curIndex, x,y);               
                }
                curIndex++;
            }
        }
        
    }
    
    public boolean supportPixelIndex(int index){
        return true;
    }
    
    protected void drawPixelOutline(Graphics g, int curIndex, int x, int y){
        // default is to NOT draw an outline per pixel
    }
    
    protected void setupUI() {
        setBackground(Color.WHITE);
    
        int gridWidth = tile.getWidth();
        int gridHeight = tile.getHeight();
        
        setMinimumSize(new Dimension(gridWidth*CHR_PIXEL_WIDTH, gridHeight*CHR_PIXEL_HEIGHT));
        setPreferredSize(new Dimension(gridWidth*CHR_PIXEL_WIDTH, gridHeight*CHR_PIXEL_HEIGHT));
        setMaximumSize(new Dimension(gridWidth*CHR_PIXEL_WIDTH, gridHeight*CHR_PIXEL_HEIGHT));
        
        setupMouseControls();
        
    }
    
     protected void setupMouseControls(){
        // right click to set the color
        addMouseListener(new MouseInputAdapter() {
            public void mouseClicked(MouseEvent e) {
                if( e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3){
                    int xPos = e.getX();
                    if(xPos < 0 || xPos > CHR_PIXEL_WIDTH*tile.getWidth()){
                        // ignore it
                        return;
                    }
                    int yPos = e.getY();
                    if(yPos < 0 || yPos > CHR_PIXEL_HEIGHT*tile.getHeight()){
                        // ignore it
                        return;
                    }
                    int relX = xPos / CHR_PIXEL_WIDTH;
                    int relY = yPos / CHR_PIXEL_HEIGHT;
                    int index = (relY * tile.getWidth()) + relX;
                    if( e.getButton() == MouseEvent.BUTTON1){
                        processLeftClick(index);
                    }
                    if( e.getButton() == MouseEvent.BUTTON3){
                        processRightClick(index);
                    }
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                processMouseDragged(e);              
            }
            public void mouseMoved(MouseEvent e) {
                processMouseMoved(e);
            }
        }
        );        
        
    }
    protected void processLeftClick(int index){
    }
    
    protected void processRightClick(int index){
    }
   
    protected void processMouseDragged(MouseEvent e){
    }
    protected void processMouseMoved(MouseEvent e) {
    }
}
