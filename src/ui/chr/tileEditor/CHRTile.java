/*
 * CHRTile.java
 *
 * Created on October 6, 2006, 10:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.tileEditor;

import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;


/**
 *
 * @author abailey
 */
public class CHRTile implements Comparable<CHRTile> {

	public static int IMAGE_SIMILARITY_THRESHOLD = 0;
   
    public int addDataX = 0;
    public int addDataY = 0;
    
    private int tileWidth = 8;
    private int tileHeight = 8;
    
 //   private MemoryImageSource mis = null;
    private int pix[] = null;
    
    
    private int oam = 0;
    private int pixIndex[] = null;
    private byte mask[] = null;
    private int paletteIndex = 0;
    private CHREditorModel  modelRef;
    
    private int rgb[][];
    /** Creates a new instance of CHRTile */
    public CHRTile( byte newMask[] , int palette, CHREditorModel model) {
        this(newMask, palette, model, 0);
    }
    public CHRTile( byte newMask[] , int palette, CHREditorModel model, int newOAM) {
        
        modelRef = model;
        mask = new byte[16];
        oam = newOAM;
        System.arraycopy(newMask,0,mask,0,16);
        paletteIndex = palette;
        
        pix = new int[64];
        pixIndex = new int[64];
        updatePixels();
    }
    public CHRTile( CHRTile copy, int pixOverride[]){
        addDataX = copy.addDataX;
        addDataY = copy.addDataY;
        tileWidth = copy.tileWidth;
        tileHeight = copy.tileHeight;
        modelRef = copy.modelRef;
        mask = new byte[16];
        System.arraycopy(copy.mask,0,mask,0,16);
        oam = copy.oam;
        paletteIndex = copy.paletteIndex;
        pix = new int[64];
        System.arraycopy(pixOverride,0,pix,0,64);
        pixIndex = new int[64];
        System.arraycopy(copy.pixIndex,0,pixIndex,0,64);
        
        for(int i=0;i<64;i++){
             setPixelIndex(i %8, i/8, pix[i], false);
        }
        updatePixels();
    }
    
    public void setAdditionalInfo(int x, int y){
        addDataX = x;
        addDataY = y;
    }
    
    public int getOAM(){
        return oam;
    }
    
    public CHRTile(CHRTile copy, int newOAM){
        modelRef = copy.modelRef;
        mask = new byte[16];
        System.arraycopy(copy.mask,0,mask,0,16);
        paletteIndex = copy.paletteIndex;
        oam = newOAM;
        pix = new int[64];
        pixIndex = new int[64];
        updatePixels();
    }
    
    public boolean isEmpty(){
        for(int i=0;i<mask.length;i++){
            if(mask[i] != 0){
                return false;
            }
        }
        return true;
    }
    
    public void setOAM(int newOAM){
        oam = newOAM;
        updatePixels();
    }
    
 
    private int calculateRMS(int difpix[][], double rms_threshold){
    	double val;
    	double redSum = 0; 
    	double greenSum = 0; 
    	double blueSum = 0; 
    	for (int i = 0; i < 64; i++) {
    		 val = (rgb[i][0] - difpix[i][0])/255.0;
             redSum += val * val;
    		 val = (rgb[i][1] - difpix[i][1])/255.0;
             greenSum += val * val;
    		 val = (rgb[i][2] - difpix[i][2])/255.0;
             blueSum += val * val;
         }
    	/*
    	if((Math.sqrt(redSum / 64) + Math.sqrt(greenSum / 64) + Math.sqrt(blueSum / 64)) < (rms_threshold*3)) {
    		return 0;
    	}
    	*/
            
    	if((Math.sqrt(redSum / 64)) > rms_threshold) {
    //    	System.out.println("Red exceeds:" + Math.sqrt(redSum / 64) + " thresh:" + rms_threshold);
    		return -1;
    	}
    	if((Math.sqrt(greenSum / 64)) > rms_threshold) {
    //    	System.out.println("Green exceeds:" + Math.sqrt(greenSum / 64) + " thresh:" + rms_threshold);
    		return -1;
    	}
    	if((Math.sqrt(blueSum / 64)) > rms_threshold) {
     //   	System.out.println("Blue exceeds:" + Math.sqrt(blueSum / 64) + " thresh:" + rms_threshold);
    		return -1;
    	}
         return 0;
    }
    
    public int compareTo(CHRTile tile){      
        if(tile.tileWidth != tileWidth){
            return ((tile.tileWidth < tileWidth) ? 1 : -1);
        }
        if(tile.tileHeight != tileHeight){
            return ((tile.tileHeight < tileHeight) ? 1 : -1);
        }
        
       
        if(IMAGE_SIMILARITY_THRESHOLD > 0) {
//        	double RMS_THRESHOLD = IMAGE_SIMILARITY_THRESHOLD / 100.0;
        	int rms_error = calculateRMS(tile.rgb, IMAGE_SIMILARITY_THRESHOLD/100.0);
        	
        	if(rms_error ==0) {
        		return 0;
        	}
        }
/*       if(IMAGE_SIMILARITY_THRESHOLD > 0) {
    	   
        	int diffCount = 0;
        	// do pixel by pixel check
            for(int i=0;i<pix.length;i++){
                if(tile.pix[i] != pix[i]){
                	diffCount++;
                	if(diffCount> IMAGE_SIMILARITY_THRESHOLD)
                		return ((tile.pix[i]< pix[i]) ? 1 : -1);
                }
            }
        } else {
*/        
        	// do mask check
        	for(int i=0;i<mask.length;i++){
        		if(tile.mask[i] != mask[i]){
            		return ((tile.mask[i]< mask[i]) ? 1 : -1);
        		}
        	}
 //       }
        return 0;
    }
    
    public int getWidth(){
        return tileWidth;
    }
    
    public int getHeight(){
        return tileHeight;
    }
    
    public void updatePixels(){
        int pixOffset = 0;
        for(int i=0;i<8;i++){
            byte tempVal = 0;
            byte m1 = mask[i];
            byte m2 = mask[i+8];
            for(int q=0;q<8;q++){
                tempVal = (byte)((m1 & 1) + ((m2 & 1)<<1));
                pixIndex[pixOffset+7-q] = tempVal;
                if(paletteIndex == PPUConstants.SPRITE_PALETTE_TYPE && tempVal == 0) {
                    pix[pixOffset+7-q] = 0;
                } else {
                    pix[pixOffset+7-q] =  modelRef.getPaletteColor(paletteIndex, oam, tempVal).getRGB();
                }
                m1 = (byte)(m1 >> 1);
                m2 = (byte)(m2 >> 1);
            }
            pixOffset+=tileWidth;
        }
        rgb = new int[64][3];
        for(int i=0;i<64;i++){
        	rgb[i][0] = (pix[i] >> 16) & 0xFF; 
        	rgb[i][1] = (pix[i] >> 8 ) & 0xFF; 
        	rgb[i][2] = (pix[i]      ) & 0xFF; 
        }
    }
    
    public int[] getPix(){
        int destPix[] = new int[64];
        System.arraycopy(pix,0,destPix,0,64);
        return destPix;
    }
    public int getPixelIndex(int index){
        return pixIndex[index];
    }
    
    public static byte setBit(byte srcVal, byte newBit, int column){
        byte b[] = new byte[8];
        
        int bVal = srcVal;
        for(int i=0;i<8;i++){
            b[7-i] = (byte)(bVal & 1);
            bVal = (bVal >> 1);
        }
        b[column] = newBit;
        bVal = (b[0]<<7) + (b[1] << 6) + (b[2]<<5) + (b[3] << 4) + (b[4] << 3) + (b[5] << 2) + (b[6] << 1) + b[7];
        return (byte)bVal;
    }
    
    public void setPixelIndex(int column, int row, int val, boolean shouldUpdate){
        // System.out.println("[" + column + "," + row + "] = " + val);
        byte lower = (byte)(val & 1);
        byte upper = (byte)((val>>1) & 1);
        mask[row] = setBit(mask[row], lower, column);
        mask[row+8] = setBit(mask[row+8], upper, column);
        if(shouldUpdate) {
            updatePixels();
        }
    }
    public void setPixelIndex(int index, int val){
        setPixelIndex(index %8, index/8, val, true);
    }
    
    public int[] getPixelIndexes(){
        int destPix[] = new int[64];
        System.arraycopy(pixIndex,0,destPix,0,64);
        return destPix;
    }
    
    public byte[] asMask() {
        byte b[] = new byte[16];
        System.arraycopy(mask,0,b,0,16);
        return b;
    }
    
}
