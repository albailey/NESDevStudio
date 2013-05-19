/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.mappers;

/**
 * http://wiki.nesdev.com/w/index.php/MMC2
 * @author abailey
 *
 * From the wiki
 *
 *
PRG ROM bank select ($A000-$AFFF)
7  bit  0
---- ----
xxxx PPPP
     ||||
     ++++- Select 8 KB PRG ROM bank for CPU $8000-$9FFF
CHR ROM $FD/0000 bank select ($B000-$BFFF)
7  bit  0
---- ----
xxxC CCCC
   | ||||
   +-++++- Select 4 KB CHR ROM bank for PPU $0000-$0FFF
           used when latch 0 = $FD
CHR ROM $FE/0000 bank select ($C000-$CFFF)
7  bit  0
---- ----
xxxC CCCC
   | ||||
   +-++++- Select 4 KB CHR ROM bank for PPU $0000-$0FFF
           used when latch 0 = $FE
CHR ROM $FD/1000 bank select ($D000-$DFFF)
7  bit  0
---- ----
xxxC CCCC
   | ||||
   +-++++- Select 4 KB CHR ROM bank for PPU $1000-$1FFF
           used when latch 1 = $FD
CHR ROM $FE/1000 bank select ($E000-$EFFF)
7  bit  0
---- ----
xxxC CCCC
   | ||||
   +-++++- Select 4 KB CHR ROM bank for PPU $1000-$1FFF
           used when latch 1 = $FE
 Mirroring ($F000-$FFFF)
7  bit  0
---- ----
xxxx xxxM
        |
        +- Select nametable mirroring (0: vertical; 1: horizontal)

 */
public class Mapper_009_PxROM_MMC2 extends AbstractMapper {

    // CPU $8000-$9FFF: 8 KB switchable PRG ROM bank
    // CPU $A000-$FFFF: Three 8 KB PRG ROM banks, fixed to the last three banks
    int[] prgBank =   { 0,0,0,0 };
    
    int lowBank = 0;
    int highBank = 0;

    int lowTrigger = 0;
    int highTrigger = 0;

    int fdLowCHR = 0;
    int feLowCHR = 0;
    int fdHighCHR = 0;
    int feHighCHR = 0;

    public Mapper_009_PxROM_MMC2(){
        prgBank[0] = 0;
        prgBank[1] = 0;
        prgBank[2] = 0;
        prgBank[3] = 0;
    }
     public void setPRGMemory(byte[] data) {
        super.setPRGMemory(data);
        int numBanks = data.length / 0x2000;
        prgBank[0] = 0;
        // fixed
        prgBank[1] = numBanks - 3;
        prgBank[2] = numBanks - 2;
        prgBank[3] = numBanks - 1;
    }

     // called after ppuRef is initialized
     public void initMapper() {
       _ppuRef.setHorizontalMirroringMode();
        lowBank = 0; lowTrigger=0;
        highBank = 0; highTrigger = 0;
   }

   // called by initMapper after the CHR and PRG are assigned
   public byte[] getInitialPRGData(){
        // nothing is done by mapper 0, however the data IS doubled up if theres only 1 PRG bank
        byte b[] = new byte[0x8000];
        if(prgData.length < 0x8000){
            System.err.println("Invalid PRG data");
        } else {
            System.arraycopy(prgData,prgBank[0]*0x2000,b,0,0x2000);
            System.arraycopy(prgData,prgBank[1]*0x2000,b,0x2000,0x2000);
            System.arraycopy(prgData,prgBank[2]*0x2000,b,0x4000,0x2000);
            System.arraycopy(prgData,prgBank[3]*0x2000,b,0x6000,0x2000);
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
        if( address < 0x8000 || address > 0xFFFF) {
            return 0;
        }
        return prgData[(prgBank[(address - 0x8000) / 0x2000]) * 0x2000 + ((address - 0x8000) & 0x1FFF)];
    }
    public void setMappedMemory(int address, byte val) {
        // no bus conflicts
        if (address >= 0xA000) {
            if(address <= 0xAFFF) {
                prgBank[0] = val & 0xF;
            } else if(address <= 0xBFFF) {
                fdLowCHR = val & 0xFF;
                // immediately switch if in FD low mode
               if(lowTrigger == 0xFD) {
                    lowBank = fdLowCHR;
                }
              
            } else if(address <= 0xCFFF) {
                feLowCHR = val & 0xFF;
                // immediately switch if in FE low mode
                if(lowTrigger == 0xFE) {
                    lowBank = feLowCHR;
                }
                
            } else if(address <= 0xDFFF) {
                fdHighCHR = val & 0xFF;
                 // immediately switch if in FD high mode
                
                 if(highTrigger == 0xFD) {
                    highBank = fdHighCHR;
                }
                
            } else if(address <= 0xEFFF) {
                feHighCHR = val & 0xFF;
                 // immediately switch if in FE high mode
                
                 if(highTrigger == 0xFE) {
                    highBank = feHighCHR;
                }
                
            } else {
                if((val & 0x1) == 0x1) {
                    _ppuRef.setHorizontalMirroringMode();
                } else {
                    _ppuRef.setVerticalMirroringMode();
                }
            }
        }
    }

  public boolean isCHRReadMapped(int address) {
        return (address < 0x2000);
    }

    public boolean isCHRWriteMapped(int address) {
        return false;
    }

    public byte getCHRMappedMemory(int address, boolean isDirect) {
        // read BEFORE we potentially bank switch
        byte b = chrData[((address < 0x1000) ? lowBank : highBank) * 0x1000 + (address & 0xFFF)];
        if(! isDirect) {
        //PPU reads $0FD0 through $0FDF: latch 0 is set to $FD for subsequent reads
        //PPU reads $0FE0 through $0FEF: latch 0 is set to $FE for subsequent reads
        //PPU reads $1FD0 through $1FDF: latch 1 is set to $FD for subsequent reads
        //PPU reads $1FE0 through $1FEF: latch 1 is set to $FE for subsequent reads
            if( (address & 0x1FD0) == 0x0FD0) {
                lowBank = fdLowCHR;
                lowTrigger = 0xFD;
            }
        else if( (address & 0x1FE0) == 0x0FE0) {
                lowBank = feLowCHR;
                lowTrigger = 0xFE;
        }
        else if( (address & 0x1FD0) == 0x1FD0) {
                highBank = fdHighCHR;
                highTrigger = 0xFD;
            }
        else if( (address & 0x1FE0) == 0x1FE0) {
                highBank = feHighCHR;
                highTrigger = 0xFE;
            }
        }
        return b;
    }
}
