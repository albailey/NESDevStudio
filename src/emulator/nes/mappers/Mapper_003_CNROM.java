/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.mappers;

/**
 *
 * @author abailey
 *
 * PRG ROM size: 16 KB or 32 KB (DIP-28 standard pinout)
PRG ROM bank size: Not bankswitched
PRG RAM: None
CHR capacity: 32 KB ROM (DIP-28 standard pinout)
CHR bank size: 8 KB
Nametable mirroring: Solder pads select vertical or horizontal mirroring
Subject to bus conflicts: Yes

 * Bank select ($8000-$FFFF)
7  bit  0
---- ----
xxDD xxCC
  ||   ||
  ||   ++- Select 8 KB CHR ROM bank for PPU $0000-$1FFF
  ++------ Security diodes config
 */
public class Mapper_003_CNROM extends AbstractMapper {

    int chrBank = 0;

    public Mapper_003_CNROM(){
        chrBank = 0;
    }

     public void setPRGMemory(byte[] data) {
        super.setPRGMemory(data);
    }

     public void setCHRMemory(byte[] data) {
        super.setCHRMemory(data);
        chrBank = 0;
    }


    public boolean isWriteMapped(int address){
        return((address >= 0x8000 && address <= 0xFFFF));
    }
    public void setMappedMemory(int address, byte val) {
       if(getMappedMemoryDirect(address) == val) {
            // do a swap
            chrBank = (val & 0x3); //
        }
    }

   public boolean isCHRReadMapped(int address) {
        return (address < 0x2000);
    }

    public boolean isCHRWriteMapped(int address) {
        return (address < 0x2000);
    }

    public byte getCHRMappedMemory(int address, boolean isDirect) {
        return chrData[chrBank * 0x2000 + address];
    }

    public void setCHRMappedMemory(int address, byte val) {
        chrData[chrBank * 0x2000 + address] = val;
    }
}
