/*
 * CHRModel.java
 *
 * Created on September 29, 2006, 3:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package model;

import ui.chr.PPUConstants;


/**
 *
 * @author abailey
 */
public class CHRModel implements NESPaletteModel {
   
    
    public byte imagePalette[] = new byte[PPUConstants.NES_IMAGE_PALETTE_SIZE];
    public byte spritePalette[] = new byte[PPUConstants.NES_SPRITE_PALETTE_SIZE];
    
     public byte patternTable[][] = new byte[2][PPUConstants.PATTERN_TABLE_PAGE_SIZE ];
    
     public byte nameTableIndexes[][] = new byte[1][PPUConstants.NAME_TABLE_WIDTH * PPUConstants.NAME_TABLE_HEIGHT];
     public byte oamValues[][] = new byte[1][PPUConstants.OAM_TABLE_SIZE];  
     
    /** Creates a new instance of CHRModel */
    public CHRModel() {
        for(int i=0;i<imagePalette.length;i++){
            if(i>0 && (i % 4 != 0)) {
                imagePalette[i] = (byte)i;
            }
        }
        for(int i=0;i<spritePalette.length;i++){
            if(i>0 && (i % 4 != 0)) {
                spritePalette[i] = (byte)i;
            }
        }
    }
    public int getNumPages(){
        return nameTableIndexes.length;
    }
    
    public void setNumPages(int numPages){
        byte nti[][] = new byte[numPages][PPUConstants.NAME_TABLE_WIDTH * PPUConstants.NAME_TABLE_HEIGHT];
        byte newOam[][] = new byte[numPages][PPUConstants.OAM_TABLE_SIZE];
        int max = numPages;
        if(nameTableIndexes.length < max){
            max = nameTableIndexes.length;
        }
        for(int i=0;i<max;i++){
            System.arraycopy(nameTableIndexes[i],0,nti[i],0,PPUConstants.NAME_TABLE_WIDTH * PPUConstants.NAME_TABLE_HEIGHT);
            System.arraycopy(oamValues[i],0,newOam[i],0,PPUConstants.OAM_TABLE_SIZE);
        }
        nameTableIndexes = nti;
        oamValues = newOam;
    }
      
    public void updateImagePaletteIndex(int index, byte val){
        imagePalette[index] = val;
    }
     public void updateSpritePaletteIndex(int index, byte val){
        spritePalette[index] = val;
    }
    public byte getImagePaletteAtIndex(int index){
        return imagePalette[index];
    }
     public byte getSpritePaletteAtIndex(int index){
        return spritePalette[index];
    }
     
    public int getOAMFromNTIndex(int page, int ntIndex){
        // 
        int relX = ntIndex % PPUConstants.NAME_TABLE_WIDTH;
        int relY = ntIndex / PPUConstants.NAME_TABLE_WIDTH;
        int oamIndexX = relX  / 4; // gets us in terms of an 8x8 table
        int oamIndexY = relY / 4;
        int oamTableIndex = oamIndexY * PPUConstants.OAM_INDEX_GRID_SIZE + oamIndexX;
        byte oamVal = oamValues[page][oamTableIndex];
        
        int oamGroupX = (relX / 2) % 2; // gets us in terms of a 16 x 16 table
        int oamGroupY = (relY / 2) % 2;        
        int subOAM = oamGroupY * 2 + oamGroupX;
        
        int retVal = ((oamVal >> (2*subOAM)) & 0x03);
//        System.out.println("OAM for NT:" + ntIndex + " is:" + retVal + " OAM:" + oamTableIndex + " Sub: " + subOAM + " Entire val is:" + oamVal);
        return  retVal;
    }
    
    
    public void assignImagePalette(byte[] srcData,int offset){
        if(srcData.length < offset + PPUConstants.NES_IMAGE_PALETTE_SIZE){
            System.err.println("Invalid temp palette size:" + srcData.length + " and offset:" + offset);
            return;
        }
        System.arraycopy(srcData,offset,imagePalette,0,PPUConstants.NES_IMAGE_PALETTE_SIZE);
        for(int i=0;i<PPUConstants.NES_IMAGE_PALETTE_SIZE;i++){
            if(imagePalette[i] > 64 || imagePalette[i] < 0){
                System.out.println("Invalid image palette entry:" + imagePalette[i] +" at index: " + i +" . Using 0");
                imagePalette[i] = 0;
            }
        }
    }
    public void assignSpritePalette(byte[] srcData, int offset){
        if(srcData.length < offset + PPUConstants.NES_SPRITE_PALETTE_SIZE){
            System.err.println("Invalid temp palette size:" + srcData.length + " and offset:" + offset);
            return;
        }
        System.arraycopy(srcData,offset,spritePalette,0,PPUConstants.NES_SPRITE_PALETTE_SIZE);
        for(int i=0;i<PPUConstants.NES_SPRITE_PALETTE_SIZE;i++){
            if(spritePalette[i] > 64 || spritePalette[i]<0){
                System.out.println("Invalid sprite palette entry:" + spritePalette[i] +" at index: " + i +" . Using 0");
                spritePalette[i] = 0;
            }
        }
    }
    
    public void assignPatternTable(int bank, byte[] srcData, int offset){
         if(srcData.length < offset + PPUConstants.PATTERN_TABLE_PAGE_SIZE){
            System.err.println("Invalid input pattern table buffer of size:" + srcData.length + " and offset:" + offset);
            return;
        }
        System.arraycopy(srcData,offset,patternTable[bank],0,PPUConstants.PATTERN_TABLE_PAGE_SIZE);
    }
    
    public byte[] getPatternTableBytesForTile(int bank, int tile){
        if(bank < 0 || bank >= patternTable.length){
            throw new IllegalArgumentException("Invalid bank:" + bank);
        }
        if(tile < 0 || tile >= PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE){
            throw new IllegalArgumentException("Invalid tile index:" + tile);
        }
        // return the 16 btes that make up this tile
        byte b[] = new byte[PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY];
        int offset = tile*PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY;
        System.arraycopy(patternTable[bank],offset,b,0,PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY);
        return b;
    }
    
}
    
