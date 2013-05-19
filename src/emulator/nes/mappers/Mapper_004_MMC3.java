package emulator.nes.mappers;
/**
 * From the WIKI:  http://wiki.nesdev.com/w/index.php/MMC3
 * 
 * Overview:
 * PRG ROM size: Up to 512 KB
 * PRG ROM bank size: 8 KB
 * PRG RAM: Up to 8 KB
 * CHR capacity: Up to 256 KB ROM or 8 KB RAM
 * CHR bank size: 1 KB and 2 KB
 * Nametable mirroring: Controlled by mapper
 * Subject to bus conflicts: No
 *
 *Banks
CPU $6000-$7FFF: 8 KB PRG RAM bank
CPU $8000-$9FFF (or $C000-$DFFF): 8 KB switchable PRG ROM bank
CPU $A000-$BFFF: 8 KB switchable PRG ROM bank
CPU $C000-$DFFF (or $8000-$9FFF): 8 KB PRG ROM bank, fixed to the second-last bank
CPU $E000-$FFFF: 8 KB PRG ROM bank, fixed to the last bank
PPU $0000-$07FF (or $1000-$17FF): 2 KB switchable CHR bank
PPU $0800-$0FFF (or $1800-$1FFF): 2 KB switchable CHR bank
PPU $1000-$13FF (or $0000-$03FF): 1 KB switchable CHR bank
PPU $1400-$17FF (or $0400-$07FF): 1 KB switchable CHR bank
PPU $1800-$1BFF (or $0800-$0BFF): 1 KB switchable CHR bank
PPU $1C00-$1FFF (or $0C00-$0FFF): 1 KB switchable CHR bank
 *
 *Registers
 *  The MMC3 has 4 pairs of registers at $8000-$9FFF, $A000-$BFFF, $C000-$DFFF, and $E000-$FFFF 
 *  even addresses ($8000, $8002, etc.) select the low register 
 *  odd addresses ($8001, $8003, etc.) select the high register in each pair
 *  
 *  
 *  
 *  
 *  Bank select ($8000-$9FFE, even)
7  bit  0
---- ----
CPxx xRRR
||    |||
||    +++- Specify which bank register to update on next write to Bank Data register
||         0: Select 2 KB CHR bank at PPU $0000-$07FF (or $1000-$17FF);
||         1: Select 2 KB CHR bank at PPU $0800-$0FFF (or $1800-$1FFF);
||         2: Select 1 KB CHR bank at PPU $1000-$13FF (or $0000-$03FF);
||         3: Select 1 KB CHR bank at PPU $1400-$17FF (or $0400-$07FF);
||         4: Select 1 KB CHR bank at PPU $1800-$1BFF (or $0800-$0BFF);
||         5: Select 1 KB CHR bank at PPU $1C00-$1FFF (or $0C00-$0FFF);
||         6: Select 8 KB PRG bank at $8000-$9FFF (or $C000-$DFFF);
||         7: Select 8 KB PRG bank at $A000-$BFFF
|+-------- PRG ROM bank configuration (0: $8000-$9FFF swappable,
|                                         $C000-$DFFF fixed to second-last bank;
|                                      1: $C000-$DFFF swappable,
|                                         $8000-$9FFF fixed to second-last bank)
+--------- CHR ROM bank configuration (0: two 2 KB banks at $0000-$0FFF,
                                          four 1 KB banks at $1000-$1FFF;
                                       1: two 2 KB banks at $1000-$1FFF,
                                          four 1 KB banks at $0000-$0FFF)
                                          
 *
 *Bank data ($8001-$9FFF, odd)
7  bit  0
---- ----
DDDD DDDD
|||| ||||
++++-++++- New bank value, based on last value written to Bank select register (mentioned above)
The PRG banks are 8192 bytes in size, half the size of an iNES PRG bank. If your emulator or copier handles PRG data in 16384 byte chunks, you can think of the lower bit as selecting the first or second half of the bank:[1]
7  bit  0  When $8000 AND #$06 == #$06
---- ----
xxBB BBBH
  || ||||
  || |||+- 0: Select first half of this bank;
  || |||   1: Select second half of this bank
  ++-+++-- Select 16 KB PRG bank at $8000-$9FFF, $A000-$BFFF, or $C000-$DFFF
 *
 *
 *
 *
 *Mirroring ($A000-$BFFE, even)
7  bit  0
---- ----
xxxx xxxM
        |
        +- Mirroring (0: vertical; 1: horizontal)





PRG RAM protect ($A001-$BFFF, odd)
7  bit  0
---- ----
RWxx xxxx
||
|+-------- Write protection (0: allow writes; 1: deny writes)
+--------- Chip enable (0: disable chip; 1: enable chip)
These bits work on the MMC3 that blargg tried, even if some emulators do not implement them.




IRQ latch ($C000-$DFFE, even)
 7  bit  0
---- ----
DDDD DDDD
|||| ||||
++++-++++- IRQ latch value
This register specifies the IRQ counter reload value. When the IRQ counter is zero (or a reload is requested through $C001), this value will be copied into the MMC3 IRQ counter at the end of the current scanline.



IRQ reload ($C001-$DFFF, odd)
7  bit  0
---- ----
xxxx xxxx
Writing any value to this register clears the MMC3 IRQ counter so that it will be reloaded at the end of the current scanline.



IRQ disable ($E000-$FFFE, even)
7  bit  0
---- ----
xxxx xxxx
Writing any value to this register will disable MMC3 interrupts AND acknowledge any pending interrupts.


IRQ enable ($E001-$FFFF, odd)
7  bit  0
---- ----
xxxx xxxx
Writing any value to this register will enable MMC3 interrupts.

 */

public class Mapper_004_MMC3  extends AbstractMapper {
	int lowPrgBank = 0;
	int highPrgBank = 1;
	
	public Mapper_004_MMC3() {
		System.err.println("This MAPPER  #4 is not yet implemented");
	}
	
	// called by initMapper after the CHR and PRG are assigned
	public byte[] getInitialPRGData() {
	       
	        byte b[] = new byte[0x8000];
	        if(prgData.length < 0x4000){
	            System.err.println("Invalid MMC3 PRG data");
	        } else if(prgData.length == 0x4000){
	            System.arraycopy(prgData,0,b,0,0x4000);
	            System.arraycopy(prgData,0,b,0x4000,0x4000);
	        } else {
/*	            if (using32PRGBank) {
	                System.arraycopy(prgData,prg32Bank*0x8000,b,0,0x8000);
	            } else {
*/	            
	                System.arraycopy(prgData,lowPrgBank*0x4000,b,0,0x4000);
	                System.arraycopy(prgData,highPrgBank*0x4000,b,0x4000,0x4000);
//	            }
	        }
	        return b;
	        
	    }

	public void setPRGMemory(byte[] data) {
	        super.setPRGMemory(data);

//	        shiftRegisterCount = 0;
//	        shifter = 0;
	        lowPrgBank = 0;
	        highPrgBank = 1;
//	        prg32Bank = 0;
//	        using32PRGBank = false;
//	        lowBankFixed = false;
//	        isPRGRAMEnabled = false;

//	        chr4KMode = true;
//	        chrLowBank = 0;
//	        chrHighBank = 0;
//	        mirror = 0;
//	        setControl((byte) 0x0C);
	    }

}
