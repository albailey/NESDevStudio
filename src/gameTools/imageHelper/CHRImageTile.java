/*
 * CHRImageTile.java
 *
 * Created on April 10, 2008, 10:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.imageHelper;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import ui.chr.model.CHREditorModel;
import ui.chr.tileEditor.CHRTile;

/**
 *
 * @author abailey
 */
public class CHRImageTile implements Comparable<CHRImageTile> {
    
    private byte mask[] = null;
    private int orig[] = null;
    private int lim[] = null;
    private int paletteIndex = 0;
    private int oam = 0;
    private int ptx = 0;
    private int pty = 0;
    
    // All bits on except 1.  ie:  0111111 = 7F
//    private static final byte OFF_MASK[] = { (byte)0xFE, (byte)0xFD, (byte)0xFB, (byte)0xF7, (byte)0xEF, (byte)0xDF, (byte)0xBF, (byte)0x7F };
    private static final byte OFF_MASK[] = { (byte)0x7F, (byte)0xBF, (byte)0xDF,  (byte)0xEF, (byte)0xF7, (byte)0xFB, (byte)0xFD, (byte)0xFE };
    // 1000000 = 80
//    private static final byte ON_MASK[] = {  (byte)0x01, (byte)0x02, (byte)0x04, (byte)0x08, (byte)0x10, (byte)0x20, (byte)0x40, (byte)0x80 } ;
    private static final byte ON_MASK[] = {    (byte)0x80, (byte)0x40, (byte)0x20, (byte)0x10, (byte)0x08, (byte)0x04, (byte)0x02, (byte)0x01   } ;
    
    /** Creates a new instance of CHRImageTile */
    public CHRImageTile(int palIndex, int newOAM) {
        mask = new byte[16];
        orig = new int[64];
        lim = new int[64];
        paletteIndex= palIndex;
        oam = newOAM;
        ptx = 0;
        pty = 0;
       
    }
    
    public void setAdditionalInfo(int x, int y){
        ptx = x;
        pty = y;
    }
    
    public void reset(){
        mask = new byte[16];    
        orig = new int[64];
        lim = new int[64];
        ptx = 0;
        pty = 0;
    }

   
    public int compareTo(CHRImageTile tile){
         for(int i=0;i<mask.length;i++){
             if(tile.mask[i] != mask[i]){
           		 return ((tile.mask[i]< mask[i]) ? 1 : -1);
             }
         }
         return 0;
     }
 
    private byte setBit(byte srcVal, byte newBit, int column){
        if(newBit == 0){
          return setBitOff(srcVal, column);
       } else {
            return setBitOn(srcVal, column);
       }
    }

    private byte setBitOff(byte srcVal, int column){
        return (byte)(srcVal & OFF_MASK[column]);
    }

    private byte setBitOn(byte srcVal, int column){
        return (byte)(srcVal | ON_MASK[column]);
    }


    
    public void setPixelIndex(int column, int row, int val){
       // System.out.println("[" + column + "," + row + "] = " + val);
        mask[row] = setBit(mask[row], (byte)(val & 1), column);
        mask[row+8] = setBit(mask[row+8], (byte)((val>>1) & 1), column);
    }
    
    public void setOrigPixel(int column, int row, int indexVal, int origVal){
        int index = row*8+column;
        lim[index] = indexVal;
        orig[index] = origVal;
    }
    
    public double getMatchScore(){

        double sum = 0;
        // euclidean distance is sqrt of   [(r2-r1)*(r2-r1) + (g2-g1)*(g2-g1) + (b2-b1)*(b2-b1)].   where rgb values of 2 colors
        for(int i=0;i<64;i++){
            int c1 = lim[i];
            int r1 = (c1 >> 16) & 0xFF;
            int g1 = (c1 >> 8) & 0xFF;
            int b1 = (c1     ) & 0xFF;
            int c2 = orig[i];
            int r2 = (c2 >> 16) & 0xFF;
            int g2 = (c2 >> 8) & 0xFF;
            int b2 = (c2     ) & 0xFF;
            
            int sub = (r2-r1)*(r2-r1) + (g2-g1)*(g2-g1) + (b2-b1)*(b2-b1);
            sum += Math.sqrt(sub);            
        }
        return sum/64;
    }
    
    public void setPixelIndex(int index, int val){
        setPixelIndex(index %8, index/8, val);
    }
    
    public CHRTile createCHRTile(CHREditorModel model){
        CHRTile t = new CHRTile(mask, paletteIndex, model, oam);        
        t.setAdditionalInfo(ptx,pty);
         return t;
    }
    
    public static CHRTile reduce( CHRTile tile, int reductionX, int reductionY, Component comp){
    
       int pix[] = tile.getPix();
       
       BufferedImage     sourceImg = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
       Graphics2D graphics2D = sourceImg.createGraphics();
       Image misImg = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource( 8, 8, pix, 0, 8));                        
       graphics2D.drawImage(misImg, 0, 0, 8, 8, null);
       
     int newPix[] = new int[64];
    MediaTracker tracker = new MediaTracker(comp);
    Image img2 = sourceImg.getScaledInstance(reductionX, reductionY, Image.SCALE_SMOOTH);
    tracker.addImage(img2,1);
    try {
    tracker.waitForID(1);
    PixelGrabber pg = new PixelGrabber(img2, 8-reductionX, 8-reductionY, reductionX, reductionY, newPix, 0, 8);
    pg.grabPixels();
    } catch(Exception e){
        e.printStackTrace();
        return tile;
    }
    CHRTile t = new CHRTile(tile, newPix);
       return t;        
    }
}
