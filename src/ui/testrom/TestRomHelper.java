/*
 * TestRomHelper.java
 *
 * Created on April 13, 2008, 11:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.testrom;

import java.awt.Component;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import ui.chr.model.CHREditorModel;
import utilities.FileUtilities;
import emulator.core.CPU6502.MemoryInterface;
import emulator.nes.INES_ROM_INFO;
import emulator.nes.NES;
import gameTools.animationHelper.AnimationKeyframeData;

/**
 *
 * @author abailey
 */
public class TestRomHelper {
    
    /**
     * Creates a new instance of TestRomHelper
     */
    private TestRomHelper() {
    }
    
    

    public static boolean createTestROM(CHREditorModel modelRef, Component parentComponent){
        File selectedFile = FileUtilities.selectFileForSave(parentComponent);
        if(selectedFile == null){
            return false;
        }
        
        // load the injector.nes file
        // overwrite the chr, palette and nametable
        // save it to the selectedFile
        InputStream inStream = null;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String injectorNes = "ui/testrom/injector.nes";
            inStream = loader.getResourceAsStream(injectorNes);
            if(inStream == null){
                throw new Exception("Unable to load the sample injector ROM: " + injectorNes);
            }
            dis = new DataInputStream(inStream);
            fos = new FileOutputStream(selectedFile);
            dos = new DataOutputStream(fos);
            
            byte header[] = new byte[INES_ROM_INFO.INES_HEADER_SIZE];
            int headerCount = dis.read(header,0,header.length);
            if(headerCount != header.length){
                throw new Exception("Header mismatch" + headerCount + " != " + INES_ROM_INFO.INES_HEADER_SIZE);
            }
            dos.write(header,0,header.length);
            // double check that the CHR uses 1 CHR back and 1 PRG bank
            int prgBanks = (int)header[4];
            int chrBanks = (int)header[5];
            if(chrBanks < 1){
                throw new Exception("Unexpected TEST ROM. CHR banks=" + chrBanks + " < 1" );
            }
            if(prgBanks < 1){
                throw new Exception("Unexpected TEST ROM. PRG banks=" + prgBanks + " < 1" );
            }
            
            // now the PRG
            // the first 4096 bytes are code
            // then 1K is the nametable
            // the next 32 bytes is the image then sprite palette
            // most of the rest is animation
            // the last 6 bytes are the vectors
            byte prgsection[] = new byte[prgBanks * INES_ROM_INFO.PRG_BANK_SIZE];
            int count = 0;
            while(count < prgsection.length){
                int prgReadCount = dis.read(prgsection,count,prgsection.length-count);
                count+=prgReadCount;
                if(prgReadCount <=0 ){
                    break;
                }
            }
            int oldLen = 4096;
            int len = modelRef.getCHRModel().nameTableIndexes[0].length;
            System.arraycopy(modelRef.getCHRModel().nameTableIndexes[0],0,prgsection,oldLen,len);
       //     System.out.println("Writing NT:" + len + " at: " + oldLen);

            oldLen += len;            
            len = modelRef.getCHRModel().oamValues[0].length;
            System.arraycopy(modelRef.getCHRModel().oamValues[0],0,prgsection,oldLen,len);
      //      System.out.println("Writing OAM:" + len + " at: " + oldLen);
            
            oldLen += len;
            len = modelRef.getCHRModel().imagePalette.length;
            System.arraycopy(modelRef.getCHRModel().imagePalette,0,prgsection,oldLen,len);
     //       System.out.println("Writing IMG Palette:" + len + " at: " + oldLen);
            
            oldLen += len;
            len = modelRef.getCHRModel().spritePalette.length;
            System.arraycopy(modelRef.getCHRModel().spritePalette,0,prgsection,oldLen,len);
     //       System.out.println("Writing SPR Palette:" + len + " at: " + oldLen);
            
            oldLen += len;
            byte animationBytes[] = encodeAnimationBytes(modelRef);
            len = animationBytes.length;
            System.arraycopy(animationBytes,0,prgsection,oldLen,len);
     //       System.out.println("Writing Animation Bytes:" + len + " at: " + oldLen);
            

            dos.write(prgsection,0,prgsection.length);

            // now overwrite the 8K CHR data
            byte chrsection[] = new byte[chrBanks * INES_ROM_INFO.CHR_BANK_SIZE];
            
            count = 0;
            while(count < chrsection.length){
                int chrReadCount = dis.read(chrsection,count,chrsection.length-count);
                count+=chrReadCount;
                if(chrReadCount <=0 ){
                    break;
                }
            }            
            // overwrite all of it
            System.arraycopy(modelRef.getCHRModel().patternTable[0],0,chrsection,0,modelRef.getCHRModel().patternTable[0].length);
            System.arraycopy(modelRef.getCHRModel().patternTable[1],0,chrsection,modelRef.getCHRModel().patternTable[0].length,modelRef.getCHRModel().patternTable[1].length);
            dos.write(chrsection,0,chrsection.length);
            
            dos.close();
            fos.close();
            dis.close();
            inStream.close();
            
        } catch(Exception ex){
            ex.printStackTrace();
            try {
                if(dos != null){
                    dos.close();
                    dos = null;
                }
            } catch(Exception e){ e.printStackTrace(); }
            try {
                if(fos != null){
                    fos.close();
                    fos = null;
                }
            } catch(Exception e){ e.printStackTrace(); }
            try {
                if(dis != null){
                    dis.close();
                    dis =  null;
                }
            } catch(Exception e){ e.printStackTrace(); }
            try {
                if(inStream != null){
                    inStream.close();
                    inStream = null;
                }
            } catch(Exception e){ e.printStackTrace(); }
        }
        return true;
    }
    
 
    public static byte[] encodeAnimationBytes(CHREditorModel modelRef){
        // numAnimations (1 byte)
        // for each animation 
        // Num Frames (1 byte)
        // for each frame: 0 for no change, N for the index of the keyframe data to use  (N bytes)
        
        byte b[] = new byte[4096];
        int numAnimations  = modelRef.getNumAnimations();
        int animDuration = 0;
        if(numAnimations > 0){
            animDuration = modelRef.getAnimationDuration(0);
        }        
        
        Vector<AnimationKeyframeData> uniqueKFs = new Vector<AnimationKeyframeData>();
        HashMap<AnimationKeyframeData,Byte> kfMap = new HashMap<AnimationKeyframeData,Byte>();
        
        int cnt = 0;
        b[cnt++] = (byte)numAnimations;
        b[cnt++] = (byte)animDuration;
        
        for(int i=0;i<numAnimations;i++) {
            for(int j=0;j<animDuration;j++){
                if(modelRef.getKeyFrame(i,j) == null){
                    b[cnt++] = 0;                    
                } else {
                    AnimationKeyframeData kf = modelRef.getKeyFrame(i,j);
                    if(kfMap.containsKey(kf)){
                        b[cnt++] = ((Byte)kfMap.get(kf)).byteValue();
                    } else {
                        // new entry
                        uniqueKFs.add(kf);
                        Byte val = new Byte((byte)uniqueKFs.size());
                        kfMap.put(kf, val);
                        b[cnt++] = val.byteValue();
                    }
                }
            }
        }
        
        // now to store the keyframes
        b[cnt++] = (byte)uniqueKFs.size();      
        System.out.println("Unique frames:" + uniqueKFs.size());
        Iterator<AnimationKeyframeData> itor = uniqueKFs.iterator();
        while(itor.hasNext()){
            // lets make em 4 bytes each
            AnimationKeyframeData kf = itor.next();
            b[cnt++] = kf.yPos;                    
            b[cnt++] = kf.spriteIndex;                    
            b[cnt++] = kf.getSpriteAttributesByte();
            b[cnt++] = kf.xPos; 
        }
        
        byte b2[] = new byte[cnt];
        System.arraycopy(b,0,b2,0,cnt);
  
        return b2;
    }
    
    public static int decodeAnimationBytes( byte prgsection[], int offset, CHREditorModel modelRef) {
        int cnt = 0;
        int numAnimations = prgsection[offset + cnt] & 0xFF; cnt++;
        int animDuration = prgsection[offset+cnt] & 0xFF;  cnt++;    
        int animGrid[][] = new int[numAnimations][animDuration];
        for(int i=0;i<numAnimations;i++) {
            for(int j=0;j<animDuration;j++){
               animGrid[i][j] = prgsection[offset+cnt] & 0xFF;  cnt++;    
            }
        }
        
        int numKfs = prgsection[offset+cnt] & 0xFF;  cnt++;  
        AnimationKeyframeData kfs[] = new AnimationKeyframeData[numKfs];
        // now extract the keyframes
        for(int i=0;i<numKfs;i++){
            kfs[i] = new AnimationKeyframeData();
            kfs[i].yPos = prgsection[offset+cnt];  cnt++;
            kfs[i].spriteIndex = prgsection[offset+cnt];  cnt++;
            kfs[i].setSpriteAttributesByte(prgsection[offset+cnt]);cnt++;
            kfs[i].xPos = prgsection[offset+cnt];  cnt++;            
        }
        for(int i=0;i<animGrid.length;i++) {
            modelRef.addAnimation();
            for(int j=0;j<animGrid[i].length;j++){
                int val = animGrid[i][j];
                if(val != 0) {
                    AnimationKeyframeData kf = modelRef.constructKeyFrame(i,j);
                    kfs[val-1].copyInto(kf);
                }
            }
        }        
        
        return cnt;
    }
    
    
    public static boolean extractFromTestROM(CHREditorModel newModelRef, Component parentComponent){
        File selectedFile = FileUtilities.selectFileForOpen(parentComponent);
        if(selectedFile == null){
            return false;
        }
        // load the injector.nes file
        // overwrite the chr, palette and nametable
        // save it to the selectedFile
        InputStream inStream = null;
        DataInputStream dis = null;
        
        try {
            inStream = new FileInputStream(selectedFile);       
            dis = new DataInputStream(inStream);
            
            byte header[] = new byte[INES_ROM_INFO.INES_HEADER_SIZE];
            int headerCount = dis.read(header,0,header.length);
            if(headerCount != header.length){
                throw new Exception("Header mismatch" + headerCount + " != " + INES_ROM_INFO.INES_HEADER_SIZE);
            }
            // double check that the CHR uses 1 CHR back and 1 PRG bank
            int prgBanks = (int)header[4];
            int chrBanks = (int)header[5];
            if(chrBanks < 1){
                throw new Exception("Unexpected TEST ROM. CHR banks=" + chrBanks + " < 1" );
            }
            if(prgBanks < 1){
                throw new Exception("Unexpected TEST ROM. PRG banks=" + prgBanks + " < 2" );
            }
            
            // now the PRG
            // the first 1K is the nametable
            // the next 32 bytes is the image then sprite palette
            // ignore the rest
            byte prgsection[] = new byte[prgBanks * INES_ROM_INFO.PRG_BANK_SIZE];
            int count = 0;
            while(count < prgsection.length){
                int prgReadCount = dis.read(prgsection,count,prgsection.length-count);
                count+=prgReadCount;
                if(prgReadCount <=0 ){
                    break;
                }
            }
            int oldLen = 4096;
            int len = newModelRef.getCHRModel().nameTableIndexes[0].length;
            System.arraycopy(prgsection, oldLen, newModelRef.getCHRModel().nameTableIndexes[0],0,len);
       //     System.out.println("Reading NT:" + len + " at: " + oldLen);

            oldLen += len;            
            len = newModelRef.getCHRModel().oamValues[0].length;
            System.arraycopy(prgsection,oldLen,newModelRef.getCHRModel().oamValues[0],0,len);
      //      System.out.println("Reading OAM:" + len + " at: " + oldLen);
            
            oldLen += len;
            len = newModelRef.getCHRModel().imagePalette.length;
            System.arraycopy(prgsection, oldLen, newModelRef.getCHRModel().imagePalette,0,len);
     //       System.out.println("Reading IMG Palette:" + len + " at: " + oldLen);
            
            oldLen += len;
            len = newModelRef.getCHRModel().spritePalette.length;
            System.arraycopy(prgsection, oldLen, newModelRef.getCHRModel().spritePalette,0,len);
     //       System.out.println("Reading SPR Palette:" + len + " at: " + oldLen);
            
            oldLen += len;
            len =  decodeAnimationBytes(prgsection, oldLen, newModelRef);
     //       System.out.println("Reading Animation Bytes:" + len + " at: " + oldLen);

            // now load the 8K CHR data
            byte chrsection[] = new byte[chrBanks * INES_ROM_INFO.CHR_BANK_SIZE];
            
            count = 0;
            while(count < chrsection.length){
                int chrReadCount = dis.read(chrsection,count,chrsection.length-count);
                count+=chrReadCount;
                if(chrReadCount <=0 ){
                    break;
                }
            }            
            // overwrite all of it
            int offset = 0;
            int dataLen = newModelRef.getCHRModel().patternTable[0].length;
            System.arraycopy(chrsection,offset,newModelRef.getCHRModel().patternTable[0],0,dataLen);
            offset += dataLen;
            dataLen = newModelRef.getCHRModel().patternTable[1].length;
            System.arraycopy(chrsection,offset,newModelRef.getCHRModel().patternTable[1],0,dataLen);
            
            dis.close();
            inStream.close();
            
        } catch(Exception ex){
            ex.printStackTrace();
            try {
                if(dis != null){
                    dis.close();
                    dis =  null;
                }
            } catch(Exception e){ e.printStackTrace(); }
            try {
                if(inStream != null){
                    inStream.close();
                    inStream = null;
                }
            } catch(Exception e){ e.printStackTrace(); }
        }
        return true;
    }
    
    public static boolean extractFromEmulator(CHREditorModel newModelRef, NES nes){
        try {

        	MemoryInterface mem = nes._memoryManager;
        	// copy the nametable
            int len = newModelRef.getCHRModel().nameTableIndexes[0].length;
            byte nt[] = new byte[len];
            for(int i=0;i<len;i++){
            	nt[i] = mem.getCHRMemory(0x2000 + i);
            }
            System.arraycopy(nt, 0, newModelRef.getCHRModel().nameTableIndexes[0],0,len);
     
            // copy the oam
            len = newModelRef.getCHRModel().oamValues[0].length;
            byte oam[] = new byte[len];
            for(int i=0;i<len;i++){
            	oam[i] = mem.getCHRMemory(0x2400-len + i);
            }
            System.arraycopy(oam,0,newModelRef.getCHRModel().oamValues[0],0,len);
            
            // copy the palettes
            len = newModelRef.getCHRModel().imagePalette.length;
            byte pal[] = new byte[len];
            for(int i=0;i<len;i++){
            	pal[i] = mem.getCHRMemory(0x3F00 + i);
            }
            
            System.arraycopy(pal, 0, newModelRef.getCHRModel().imagePalette,0,len);
            len = newModelRef.getCHRModel().spritePalette.length;
            pal = new byte[len];
            for(int i=0;i<len;i++){
            	pal[i] = mem.getCHRMemory(0x3F10 + i);
            }
            System.arraycopy(pal, 0, newModelRef.getCHRModel().spritePalette,0,len);

            
            byte ctrlByte = nes._ppu.getPPUCTRLDirect();
            boolean switchTiles = ((ctrlByte & 0x10) == 0x10);
            
            
            // copy the tiles
            len = newModelRef.getCHRModel().patternTable[0].length;
            byte tiles[] = new byte[len];
            for(int i=0;i<len;i++){
            	tiles[i] = mem.getCHRMemory(0x0000 + i);
            }
            
            if(switchTiles)
            	System.arraycopy(tiles,0,newModelRef.getCHRModel().patternTable[1],0,len);
            else
            	System.arraycopy(tiles,0,newModelRef.getCHRModel().patternTable[0],0,len);
            	
            len = newModelRef.getCHRModel().patternTable[1].length;
            tiles = new byte[len];
            for(int i=0;i<len;i++){
            	tiles[i] = mem.getCHRMemory(0x1000 + i);
            }
            if(switchTiles)
            	System.arraycopy(tiles,0,newModelRef.getCHRModel().patternTable[0],0,len);
            else
            	System.arraycopy(tiles,0,newModelRef.getCHRModel().patternTable[1],0,len);
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return true;
    }

    
}
