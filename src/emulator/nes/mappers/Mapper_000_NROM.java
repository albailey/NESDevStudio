/*
 * NROMMapper.java
 *
 * Created on October 25, 2007, 1:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.mappers;

/**
 * DOES NOTHING!!!    
 * @author abailey
 */
public final class Mapper_000_NROM extends AbstractMapper  {
    
    
    /** Creates a new instance of NROMMapper */
    public Mapper_000_NROM() {
    }
    
    public byte[] getInitialPRGData(){
        // nothing is done by mapper 0, however the data IS doubled up if theres only 1 PRG bank
        byte b[] = new byte[0x8000];
        if((prgData.length != 0x8000) && (prgData.length != 0x4000)){
            System.err.println("Invalid NROM PRG data");
        }
        if(prgData.length == 0x4000){
            System.arraycopy(prgData,0,b,0,0x4000);
            System.arraycopy(prgData,0,b,0x4000,0x4000);
        }
        if(prgData.length == 0x8000){
            System.arraycopy(prgData,0,b,0,0x8000);
        }
        return b;
    }

    public final boolean isCHRReadMapped(int address) {
         return false;
    }

    public final boolean isCHRWriteMapped(int address) {
        return false;
    }
  
}
