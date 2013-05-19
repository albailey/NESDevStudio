/*
 * MultiNameTablePanel.java
 *
 * Created on January 6, 2009, 9:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

import ui.chr.PPUConstants;
import emulator.nes.PPU;

/**
 *
 * @author abailey
 */
public class MultiNameTablePanel extends JPanel implements PPUViewInterface{

    /**
	 * 
	 */
	private static final long serialVersionUID = 6497207154190293295L;
	private int wid;
    private int hgt;
    private int pix[] = null;
    private MemoryImageSource mis = null;
    private Image img = null;
    private Image scaledImage = null;
    private int currentScaledWidth = 0;
    private int currentScaledHeight = 0;
    
//    private boolean isHorizontalMirroring = false;
//    private boolean isVerticalMirroring = false;
    
    /** Creates a new instance of MultiNameTablePanel */
    public MultiNameTablePanel() {
        this(2*32*8,2*30*8,0.5f);
    }

    public MultiNameTablePanel(int pixWid, int pixHgt, float prefScale) {
        setBackground(Color.black);
        wid = pixWid;
        hgt = pixHgt;
        setMinimumSize(new Dimension(wid/2, hgt/2));
        setPreferredSize(new Dimension((int)(wid*prefScale), (int)(hgt*prefScale)));
        setMaximumSize(new Dimension(wid*2, hgt*2));
        pix = new int[wid*hgt];
        mis = new MemoryImageSource(wid, hgt, pix, 0, wid);
        mis.setAnimated(true); 
        refreshFromPPU(null);
    }

    public byte calculatePixel(int x, int y, PPU ppu){
      return ppu.getBGPixelFromPaletteIndex(ppu.getBGPixelIndexDirect(x,y));
    }

    public void refreshFromPPU(PPU ppu){

        int minX = -1;
        int minY = -1;
        int maxX = -1;
        int maxY = -1;
        
        if(ppu != null){
            int prev = 0;
            int pos = 0;
            for(int y=0;y<hgt;y++){
                pos = y*wid;
               for(int x=0;x<wid;x++){
                   prev = PPUConstants.NES_PALETTE[calculatePixel(x,y,ppu)& 0xFF].getRGB();
                   if(pix[pos+x] != prev){
                        pix[pos+x] = prev;
                        if(minX == -1 ){
                            minX = x;
                            maxX = x;
                            minY = y;
                            maxY = y;
                        } 
                        if(x<minX){
                            minX = x;
                        }
                        if(x>maxX){
                            maxX = x;
                        }
                        if(y<minY){
                            minY = y;
                        }
                        if(y>maxY){
                            maxY = y;
                        }                        
                   }
               }
            }
        }
        if(minX == -1){
            mis.newPixels();
        } else {
            mis.newPixels(minX, minY, maxX-minX+1, maxY-minY+1);
        }
        
    
    }
/*
    public void setMirroring(boolean isHorizontal, boolean isVertical){ // both true for 1 screen, both false for 4 screen
      isHorizontalMirroring = isHorizontal;
      isVerticalMirroring = isVertical;      
    }
 */
    
     public void paintComponent(Graphics g){
        super.paintComponent(g);    // paints background
        if(img == null){
            mis.newPixels();
            img = createImage(mis);
            scaledImage = null;
        }     
        if(getWidth() != currentScaledWidth){
            currentScaledWidth = getWidth();
            scaledImage = null;
        }
        if(getHeight() != currentScaledHeight){
            currentScaledHeight = getHeight();
            scaledImage = null;
        }
        if(scaledImage == null){
            scaledImage = img.getScaledInstance(currentScaledWidth, currentScaledHeight, Image.SCALE_SMOOTH);
        }
//     g.drawImage(img,0,0, currentScaledWidth, currentScaledHeight, this);
       g.drawImage(scaledImage,0,0,this);
    }
   
    
}
