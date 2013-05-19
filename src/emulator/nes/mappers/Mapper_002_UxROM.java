/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.mappers;

/**
 *
 * @author abailey
 *
 * The following comment is from the Firebug mapper document
  When the cart is first started, the first 16K ROM bank in the cart
           is loaded into $8000, and the LAST 16K ROM bank is loaded into
           $C000. This last 16K bank is permanently "hard-wired" to $C000,
           and it cannot be swapped.
        - This mapper has no provisions for VROM; therefore, all carts
           using it have 8K of VRAM at PPU $0000.
        - Most carts with this mapper are 128K. A few, mostly Japanese
           carts, such as Final Fantasy 2 and Dragon Quest 3, are 256K.
        - Overall, this is one of the easiest mappers to implement in
           a NES emulator.

 */
public class Mapper_002_UxROM extends AbstractMapper {

    int prgBank = 0;
    int fixedBank = 1;
    public Mapper_002_UxROM(){
        prgBank = 0;
        fixedBank = 1;
    }

     public void setPRGMemory(byte[] data) {
        super.setPRGMemory(data);
        prgBank = 0;
        fixedBank = (prgData.length / 0x4000) - 1;
    }

   // called by initMapper after the CHR and PRG are assigned
   public byte[] getInitialPRGData(){
        // nothing is done by mapper 0, however the data IS doubled up if theres only 1 PRG bank
        byte b[] = new byte[0x8000];
        if(prgData.length < 0x4000){
            System.err.println("Invalid UNROM PRG data");
        } else if(prgData.length == 0x4000){
            System.arraycopy(prgData,0,b,0,0x4000);
            System.arraycopy(prgData,0,b,0x4000,0x4000);
        } else {
            System.arraycopy(prgData,prgBank*0x4000,b,0,0x4000);
            System.arraycopy(prgData,fixedBank*0x4000,b,0x4000,0x4000);
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
        if(address < 0xC000){
            return prgData[prgBank * 0x4000 + (address - 0x8000)];
        }
        return prgData[fixedBank * 0x4000 + (address - 0xC000)];
    }
    public void setMappedMemory(int address, byte val) {
        if(getMappedMemory(address) == val) {
            // do a swap
            prgBank = (val & 0xF); //
        }
    }
}
