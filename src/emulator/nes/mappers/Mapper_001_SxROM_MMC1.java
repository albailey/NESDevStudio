/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emulator.nes.mappers;

/**
 * From the WIKI:  http://wiki.nesdev.com/w/index.php/MMC1
 *
 * PRG ROM size: Up to 256 KB depending on board
 * PRG ROM bank size: 16 KB or 32 KB
 * PRG RAM: Up to 8 KB
 * CHR capacity: Up to 128 KB ROM or 8 KB RAM
 * CHR bank size: 8 KB or 4 KB
 * Nametable mirroring: Controlled by mapper
 * Subject to bus conflicts: No
 *
 * Details:
 *
 * Load register ($8000-$FFFF)
7  bit  0
---- ----
Rxxx xxxD
|       |
|       +- Data bit to be shifted into shift register, LSB first
+--------- 1: Reset shift register and write Control with (Control OR $0C),
locking PRG ROM at $C000-$FFFF to the last bank.

 *
 * Control (internal, $8000-$9FFF)
4bit0
-----
CPPMM
|||||
|||++- Mirroring (0: one-screen, lower bank; 1: one-screen, upper bank;
|||               2: vertical; 3: horizontal)
|++--- PRG ROM bank mode (0, 1: switch 32 KB at $8000, ignoring low bit of bank number;
|                         2: fix first bank at $8000 and switch 16 KB bank at $C000;
|                         3: fix last bank at $C000 and switch 16 KB bank at $8000)
+----- CHR ROM bank mode (0: switch 8 KB at a time; 1: switch two separate 4 KB banks)

 *
 * CHR bank 0 (internal, $A000-$BFFF)
4bit0
-----
CCCCC
|||||
+++++- Select 4 KB or 8 KB CHR bank at PPU $0000 (low bit ignored in 8 KB mode)
MMC1 can do CHR banking in 4KB chunks. Known carts with CHR RAM have 8 KiB, so that makes 2 banks. RAM vs ROM doesn't make any difference for address lines. For carts with 8 KiB of CHR (be it ROM or RAM), MMC1 follows the common behavior of using only the low-order bits: the bank number is in effect ANDed with 1.

 *
 * CHR bank 1 (internal, $C000-$DFFF)
4bit0
-----
CCCCC
|||||
+++++- Select 4 KB CHR bank at PPU $1000 (ignored in 8 KB mode)

 *
 * PRG bank (internal, $E000-$FFFF)
4bit0
-----
RPPPP
|||||
|++++- Select 16 KB PRG ROM bank (low bit ignored in 32 KB mode)
+----- PRG RAM chip enable (0: enabled; 1: disabled; ignored on MMC1A)
 *
 * @author abailey
 */
public final class Mapper_001_SxROM_MMC1 extends AbstractMapper {

    int shiftRegisterCount = 0;
    int shifter = 0;
    int lowPrgBank = 0;
    int highPrgBank = 1;
    int prg32Bank = 0;
    boolean using32PRGBank = false;
    boolean lowBankFixed = false;
    boolean isPRGRAMEnabled = false;
    boolean chr4KMode = true;
    int chrLowBank = 0;
    int chrHighBank = 0;
    int mirror = 0;

    public Mapper_001_SxROM_MMC1() {
    }
   // called by initMapper after the CHR and PRG are assigned
   public byte[] getInitialPRGData(){
        // nothing is done by mapper 0, however the data IS doubled up if theres only 1 PRG bank
        byte b[] = new byte[0x8000];
        if(prgData.length < 0x4000){
            System.err.println("Invalid MMC1 PRG data");
        } else if(prgData.length == 0x4000){
            System.arraycopy(prgData,0,b,0,0x4000);
            System.arraycopy(prgData,0,b,0x4000,0x4000);
        } else {
            if (using32PRGBank) {
                System.arraycopy(prgData,prg32Bank*0x8000,b,0,0x8000);
            } else {
                System.arraycopy(prgData,lowPrgBank*0x4000,b,0,0x4000);
                System.arraycopy(prgData,highPrgBank*0x4000,b,0x4000,0x4000);
            }
        }
        return b;
    }

    public void setPRGMemory(byte[] data) {
        super.setPRGMemory(data);

        shiftRegisterCount = 0;
        shifter = 0;
        lowPrgBank = 0;
        highPrgBank = 1;
        prg32Bank = 0;
        using32PRGBank = false;
        lowBankFixed = false;
        isPRGRAMEnabled = false;

        chr4KMode = true;
        chrLowBank = 0;
        chrHighBank = 0;
        mirror = 0;
        setControl((byte) 0x0C);
    }

    public boolean isReadMapped(int address) {
        return ((address >= 0x8000 && address <= 0xFFFF));
    }

    public byte getMappedMemory(int address, boolean isDirect) {
        if (using32PRGBank) {
            return prgData[prg32Bank * 0x8000 + (address - 0x8000)];
        } else {
            if (address < 0xC000) {
                return prgData[lowPrgBank * 0x4000 + (address - 0x8000)];
            }
            return prgData[highPrgBank * 0x4000 + (address - 0xC000)];
        }
    }

    public boolean isWriteMapped(int address) {
        return ((address >= 0x8000 && address <= 0xFFFF));
    }

    public void setMappedMemory(int address, byte val) {
        // do a PRG RAM swap
        if ((val & 0x80) == 0x80) {
            //Reset shift register and write Control with (Control OR $0C),
            //  locking PRG ROM at $C000-$FFFF to the last bank.
//            System.out.println("Resetting MMC1:" + ByteFormatter.formatBits(val));
            shiftRegisterCount = 0;
            shifter = 0;
            setControl((byte) (val ^ 0x0C));
        } else {
            //           System.out.println("Input :" + ByteFormatter.formatInt(address) + "  " + ByteFormatter.formatBits(val) + " counter:" + shiftRegisterCount);
            shifter = ((val & 0x1) << shiftRegisterCount) | shifter;
            shiftRegisterCount++;

            if (shiftRegisterCount >= 5) {
                //                System.out.println("Assigning :" + ByteFormatter.formatBits((byte)shifter));
                if (address < 0xA000) {
                    setControl((byte) shifter);
                } else if (address < 0xC000) {
                    setCHRLowBank((byte) shifter);
                } else if (address < 0xE000) {
                    setCHRHighBank((byte) shifter);
                } else {
                    setPRGBank((byte) shifter);
                }
                shifter = 0;
                shiftRegisterCount = 0;
            }
        }

    }

    private void setControl(byte val) {
//        System.out.println("SetControl:" + ByteFormatter.formatBits((byte)val));
        chr4KMode = ((val & 0x10) == 0x10);
        int prgMode = ((val >> 2) & 0x3);
        if (prgMode < 2) {
            using32PRGBank = true;
            lowBankFixed = false;
        } else if (prgMode == 2) {
            lowBankFixed = true;
            lowPrgBank = 0;
        } else {
            lowBankFixed = false;
            highPrgBank = (prgData.length / 0x4000) - 1;
        }
        mirror = (val & 0x3);
        if(_ppuRef != null) {
    // Mirroring (0: one-screen, lower bank; 1: one-screen, upper bank;
    //         2: vertical; 3: horizontal)
        switch (mirror) {
            case 0:  _ppuRef.setMirroringMode(0,0,0,0); // all are $2000
                break;
            case 1:  _ppuRef.setMirroringMode(1,1,1,1); // all are $2400
                break;
            case 2:  _ppuRef.setVerticalMirroringMode(); 
                break;
            case 3:
            default:
                // never gets here
                _ppuRef.setHorizontalMirroringMode(); 
                break;
        }
        }
    }

    private void setCHRLowBank(int val) {
        // Select 4 KB or 8 KB CHR bank at PPU $0000 (low bit ignored in 8 KB mode)
        if (chr4KMode) {
            chrLowBank = val;
        } else {
            chrLowBank = (val & 0x1E) >> 1;
        }
    }

    private void setCHRHighBank(int val) {
        if (chr4KMode) {
            chrHighBank = val;
        }
    }

    private void setPRGBank(int val) {
        isPRGRAMEnabled = ((val & 0x10) != 0x10);
        if (using32PRGBank) {
            prg32Bank = (val & 0xE) >> 1;
            if (prg32Bank >= (prgData.length / 0x8000)) {
                System.err.println("Invalid 32K bank switch" + prg32Bank);
            }
        } else if (lowBankFixed) {
            // switch upper bank
            highPrgBank = (val & 0xF);
        } else {
            lowPrgBank = (val & 0xF);
        }
    }
   public  boolean isCHRReadMapped(int address) {
        return (address < 0x2000);
    }

    public  boolean isCHRWriteMapped(int address) {
        return (address < 0x2000);
    }

    public byte getCHRMappedMemory(int address, boolean isDirect) {
        if (!chr4KMode) {
            return chrData[chrLowBank * 0x2000 + address];
        }
        if (address < 0x1000) {
            return chrData[chrLowBank * 0x1000 + address];
        }
        return chrData[chrHighBank * 0x1000 + (address - 0x1000)];
    }

    public void setCHRMappedMemory(int address, byte val) {
        if (!chr4KMode) {
            chrData[chrLowBank * 0x2000 + address] = val;
        } else {
            if (address < 0x1000) {
                chrData[chrLowBank * 0x1000 + address] = val;
            } else {
                chrData[chrHighBank * 0x1000 + (address - 0x1000)] = val;
            }
        }
    }
}
