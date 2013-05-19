/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.mappers;

/**
 * Mapper #7   AxROM
 *  * http://wiki.nesdev.com/w/index.php/AxROM
 * @author abailey
 */

/* From the wiki
 * The generic designation AxROM refers to Nintendo cartridge boards NES-AMROM, NES-ANROM, NES-AN1ROM, NES-AOROM
 * , their HVC counterparts, and clone boards.
 * Most games developed by Rare Ltd. used an AxROM series board.
 * The iNES format assigns mapper 7 to AxROM.
 *
 * RG ROM size: up to 256 KB
 * PRG ROM bank size: 32 KB
 * PRG RAM: None
 * CHR capacity: 8 KB RAM
 * CHR bank size: Not bankswitched
 * Nametable mirroring: Single-screen, mapper-selectable
 * Subject to bus conflicts: AMROM/AOROM only
 *
 * CPU $8000-$FFFF: 32 KB switchable PRG ROM bank
 *
 * Bank select ($8000-$FFFF)
 * 7  bit  0
 * ---- ----
 * xxxM xPPP
 *    |  |||
 *    |  +++- Select 32 KB PRG ROM bank for CPU $8000-$FFFF
 *    +------ Select 1 KB VRAM page for all 4 nametables
 *
 * The AMROM, ANROM, AN1ROM and AOROM boards contain a 74HC161 binary counter used as a quad D latch (4-bit register).
 * The ANROM and AN1ROM boards also contains a 74HC02 which is used to disable the PRG ROM during writes
 * , thus avoiding bus conflicts.
 */
public class Mapper_007_AxROM extends AbstractMapper {

    int prgBank = 0;
    boolean highSingleScreenMirroring = false;

    public Mapper_007_AxROM(){
        prgBank = 0;
        highSingleScreenMirroring = false;
    }
     public void setPRGMemory(byte[] data) {
        super.setPRGMemory(data);
        prgBank = 0;
    }

     // called after ppuRef is initialized
     public void initMapper() {
       _ppuRef.setMirroringMode(0,0,0,0); // all are $2000
   }
   // called by initMapper after the CHR and PRG are assigned
   public byte[] getInitialPRGData(){
        // nothing is done by mapper 0, however the data IS doubled up if theres only 1 PRG bank
        byte b[] = new byte[0x8000];
        if(prgData.length < 0x8000){
            System.err.println("Invalid AxROM PRG data");
        } else {
            System.arraycopy(prgData,prgBank*0x8000,b,0,0x8000);
        }
        return b;
    }


    public boolean isReadMapped(int address){
        return((address >= 0x8000 && address <= 0xFFFF));
    }
    public boolean isWriteMapped(int address){
        return((address >= 0x8000 && address <= 0xFFFF));
    }

    public byte getMappedMemory(int address, boolean isDirect) {
        if( address < 0x8000){
            return 0;
        }
        return prgData[prgBank * 0x8000 + (address - 0x8000)];
    }
    public void setMappedMemory(int address, byte val) {
        if(getMappedMemoryDirect(address) == val) {
            // do a swap
            prgBank = (val & 0x7);
            highSingleScreenMirroring = ((val & 0x10) == 0x10);
            // if on, use 1,1,1,1
            // if off use 0,0,0,0
            if(highSingleScreenMirroring) {
                _ppuRef.setMirroringMode(1,1,1,1); // all are $2400
            } else {
                _ppuRef.setMirroringMode(0,0,0,0); // all are $2000
            }
        }
    }
}
