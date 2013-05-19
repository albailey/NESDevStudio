/*
 * LevelEditorTestRomHelper.java
 *
 * Created on April 13, 2008, 11:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.levelEditor;

import java.awt.Component;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import ui.chr.model.CHREditorModel;
import utilities.ByteFormatter;
import utilities.CompressionUtilities;
import utilities.FileUtilities;
import emulator.nes.INES_ROM_INFO;

/**
 *
 * @author abailey
 */
public class LevelEditorTestRomHelper {
    
    /**
     * Creates a new instance of LevelEditorTestRomHelper
     */
    private LevelEditorTestRomHelper() {
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
            String injectorNes = "gametools/levelEditor/engine.nes";
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
            // double check that the CHR uses 1 CHR back and 2 PRG bank
            int prgBanks = (int)header[4];
            int chrBanks = (int)header[5];
            if(chrBanks < 1){
                throw new Exception("Unexpected TEST ROM. CHR banks=" + chrBanks + " < 1" );
            }
            if(prgBanks < 2){
                throw new Exception("Unexpected TEST ROM. PRG banks=" + prgBanks + " < 2" );
            }
            
            // now the PRG
            // the first 32 bytes is the palette
            // the next N bytes are the tableInfo
            // the next chunks are the compressed nametables themselves
            // Max that can be written are 16K
            
            // keep the rest
            byte prgsection[] = new byte[prgBanks * INES_ROM_INFO.PRG_BANK_SIZE];
            int count = 0;
            while(count < prgsection.length){
                int prgReadCount = dis.read(prgsection,count,prgsection.length-count);
                count+=prgReadCount;
                if(prgReadCount <=0 ){
                    break;
                }
            }
            int oldLen = 0;
            int len = 0;
            
            // 32 bytes of palette
            len = modelRef.getCHRModel().imagePalette.length;
            System.arraycopy(modelRef.getCHRModel().imagePalette,0,prgsection,oldLen,len);
            System.out.println("Writing IMG Palette:" + len + " at: " + ByteFormatter.formatInt( 0x8000 + oldLen ));
            oldLen += len;
            
            len = modelRef.getCHRModel().spritePalette.length;
            System.arraycopy(modelRef.getCHRModel().spritePalette,0,prgsection,oldLen,len);
            System.out.println("Writing SPR Palette:" + len + " at: " + ByteFormatter.formatInt( 0x8000 + oldLen ));
            oldLen += len;
            
            
            
            // Next is the table...  numTables * 2 + 1
            // store the number of tables
            // then the address of each table (2 bytes)
            int numTables = modelRef.getCHRModel().nameTableIndexes.length;
            int totalTableSize = modelRef.getCHRModel().nameTableIndexes[0].length + modelRef.getCHRModel().oamValues[0].length;
            byte tempRawBytes[][] = new byte[numTables][totalTableSize];
            int compressedSizes[] = new int[numTables];
            len = numTables*2 + 1;
            byte b[] = new byte[len];
            b[0] = (byte)numTables;
            System.out.println("Writing table size:" + b[0] + "at:"  + ByteFormatter.formatInt( 0x8000 + oldLen ));
            int nameTablePortionPtr = 0x8000 + oldLen + len;
            
            for(int i=0;i<numTables;i++){
                modelRef.saveAsNameTable(i,parentComponent, null);
                System.arraycopy(modelRef.getCHRModel().nameTableIndexes[i],0,tempRawBytes[i],0,modelRef.getCHRModel().nameTableIndexes[i].length);
                System.arraycopy(modelRef.getCHRModel().oamValues[i],0,tempRawBytes[i],modelRef.getCHRModel().nameTableIndexes[i].length,modelRef.getCHRModel().oamValues[i].length);
                byte compressedBytes[] = CompressionUtilities.compressData(CompressionUtilities.RLE_COMPRESSION,tempRawBytes[i]);
                compressedSizes[i] = compressedBytes.length + 3;
                tempRawBytes[i][0] = (byte)(CompressionUtilities.RLE_COMPRESSION & 0xFF);
                tempRawBytes[i][1] = (byte)(compressedBytes.length & 0xFF);
                tempRawBytes[i][2] = (byte)((compressedBytes.length >> 8) & 0xFF);
                System.arraycopy(compressedBytes,0,tempRawBytes[i],3,compressedBytes.length);
                b[i*2+2] = (byte)((nameTablePortionPtr >> 8)&0xFF); // low byte
                b[i*2+1] = (byte)((nameTablePortionPtr     )&0xFF); // high byte
                System.out.println("Writing address:" + ByteFormatter.formatInt( nameTablePortionPtr ) );
                nameTablePortionPtr += compressedSizes[i];
            }
            
            System.arraycopy(b,0,prgsection,oldLen,len);
            oldLen += len;
            
            for(int i=0;i<numTables;i++){
                len = compressedSizes[i];
                System.arraycopy(tempRawBytes[i],0,prgsection,oldLen,len);
                oldLen += len;
            }
            
            
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
/*            int oldLen = 0;
            int len = newModelRef.getCHRModel().nameTableIndexes.length;
            System.arraycopy(prgsection, oldLen, newModelRef.getCHRModel().nameTableIndexes,0,len);
       //     System.out.println("Reading NT:" + len + " at: " + oldLen);
 
            oldLen += len;
            len = newModelRef.getCHRModel().oamValues.length;
            System.arraycopy(prgsection,oldLen,newModelRef.getCHRModel().oamValues,0,len);
      //      System.out.println("Reading OAM:" + len + " at: " + oldLen);
 
            oldLen += len;
            len = newModelRef.getCHRModel().imagePalette.length;
            System.arraycopy(prgsection, oldLen, newModelRef.getCHRModel().imagePalette,0,len);
     //       System.out.println("Reading IMG Palette:" + len + " at: " + oldLen);
 
            oldLen += len;
            len = newModelRef.getCHRModel().spritePalette.length;
            System.arraycopy(prgsection, oldLen, newModelRef.getCHRModel().spritePalette,0,len);
     //       System.out.println("Reading SPR Palette:" + len + " at: " + oldLen);
 
 
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
            int dataLen = newModelRef.getCHRModel().patternTableZero.length;
            System.arraycopy(chrsection,offset,newModelRef.getCHRModel().patternTableZero,0,dataLen);
            offset += dataLen;
            dataLen = newModelRef.getCHRModel().patternTableOne.length;
            System.arraycopy(chrsection,offset,newModelRef.getCHRModel().patternTableOne,0,dataLen);
 */
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
    
}
