/*
 * NESCartridge.java
 *
 * Created on October 25, 2007, 12:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes;

import java.io.File;

import utilities.FileUtilities;

/**
 *
 * @author abailey
 */
public class NESCartridge {
    
    public INESHeader header = null;
    byte[] _prgData;
    byte[] _chrData;
    
    /** Creates a new instance of NESCartridge */
    private NESCartridge() {
        header = new INESHeader();
        _prgData = new byte[2*INES_ROM_INFO.PRG_BANK_SIZE];
        _chrData = new byte[1*INES_ROM_INFO.CHR_BANK_SIZE];
    }
    private boolean setHeaderData(byte headerData[]){
        return header.validateHeader(headerData);
    }
    private void assignPRGData(boolean createNew, int dataOffset, byte src[]){
        if(createNew){
            _prgData = new byte[header._numPrgBanks*INES_ROM_INFO.PRG_BANK_SIZE];
        }
        System.arraycopy(src,dataOffset,_prgData,0,_prgData.length);
    }
    private void assignCHRData(boolean createNew, int dataOffset, byte src[]){
        if(createNew){
            _chrData = new byte[header._numChrBanks*INES_ROM_INFO.CHR_BANK_SIZE];
        }
        System.arraycopy(src,dataOffset,_chrData,0,_chrData.length);
    }
    
    public Mapper getMapper(){
        return header.getMapper();
    }
    public byte[] getPRGData(){
        return _prgData;
    }
    public byte[] getCHRData(){
        return _chrData;
    }
    // mirroring can be overridden by the mapper
    public boolean getHorizontalMirroring(){
        return (((header._baseMapperInfo % 16) & 0x1) == 0x0);
        
    }
    public boolean getVerticalMirroring(){
        return (((header._baseMapperInfo % 16) & 0x1) == 0x1);
    }
    
    public static NESCartridge createCartridge(String romFileName){
     
        File selectedFile = new File(romFileName);
   
        byte headerData[] = new byte[INES_ROM_INFO.INES_HEADER_SIZE];
        int headerCount = FileUtilities.loadBytes(selectedFile, headerData, INES_ROM_INFO.INES_HEADER_SIZE);
        
        if(headerCount < INES_ROM_INFO.INES_HEADER_SIZE){
            System.err.println("Header too small" + headerCount + " < " + INES_ROM_INFO.INES_HEADER_SIZE);
            return null;
        }
        NESCartridge cart = new NESCartridge();
        if( ! cart.setHeaderData(headerData)){
            System.err.println("Header not in INES format");
            return null;            
        }
        
        // check if mapper supported
        if(cart.getMapper() == null){
            System.err.println("Unsupported/Unimplemented Mapper");
            return null;            
        }
        
        // store the data thats in the CHR and PRG banks into memory (what a pig I am)
        // I am such a memory PIG :)
        int entireRomContentsSize = (INES_ROM_INFO.INES_HEADER_SIZE) + (cart.header._numPrgBanks * INES_ROM_INFO.PRG_BANK_SIZE) + (cart.header._numChrBanks * INES_ROM_INFO.CHR_BANK_SIZE);
        byte entireRomContents[] = new byte[entireRomContentsSize];
        int romLoadedVal = FileUtilities.loadBytes(selectedFile, entireRomContents, entireRomContentsSize);
        if(romLoadedVal != entireRomContentsSize ){
            System.err.println("Rom data does not correspond to the header. Unable to load the data");
            return null;       
        }
        
        int offset = INES_ROM_INFO.INES_HEADER_SIZE;
        cart.assignPRGData( true, offset, entireRomContents );
        offset += cart._prgData.length;
        cart.assignCHRData( true, offset, entireRomContents );
        
        return cart;
    }
    
    
}
