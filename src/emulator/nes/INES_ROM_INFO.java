/*
 * INES_ROM_INFO.java
 *
 * Created on November 14, 2006, 4:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes;

/**
 *
 * @author abailey
 */
public interface INES_ROM_INFO {
   
public final static int INES_HEADER_SIZE = 16;
public final static int CHR_BANK_SIZE = 8192;
public final static int PRG_BANK_SIZE = 16384;

// http://wiki.nesdev.com/w/index.php/INES
public final static int MAX_MAPPER = 15;
public final static String[] MAPPER_STRINGS = {
	"NROM",
	"SxROM (MMC1)",
	"UxROM",
	"CNROM",
	"TxROM/HKROM (MMC3)",
	"ExROM (MMC5)",
    "6 Unsupported",
    "AxROM",
    "8 Unsupported",
    "PxROM (MMC2)",
    "FxROM (MMC4)",
    "Color Dreams",
    "12 Unsupported",
    "CPROM",
    "14 Unsupported",
    "15 (100-in-1)"

};

public final static int MAX_MAPPER_FLAGS = 16;
public final static String MAPPER_FLAGS[] = {
	" (Horizontal Mirroring)",
	" (Vertical Mirroring)",
	" (Horizontal Mirroring + Battery Backed SRAM)",
	" (Vertical Mirroring + Battery Backed SRAM)",
	" (Horizontal Mirroring + Trainer ON)",
	" (Vertical Mirroring + Trainer ON)",
	" (Horizontal Mirroring + Battery Backed SRAM and Trainer)",
	" (Vertical Mirroring + Battery Backed SRAM and Trainer)",
	" (Horizontal Mirroring + 4 Screen VRAM ON)",
	" (Vertical Mirroring + 4 Screen VRAM ON)",
	" (Horizontal Mirroring + Battery Backed SRAM and 4 Screen VRAM)",
	" (Vertical Mirroring + Battery Backed SRAM and 4 Screen VRAM)",
	" (Horizontal Mirroring + 4 Screen VRAM and Trainer)",
	" (Vertical Mirroring + 4 Screen VRAM and Trainer)",
	" (Horizontal Mirroring + Battery Backed SRAM, 4 Screen VRAM,and Trainer)",
	" (Vertical Mirroring + Battery Backed SRAM, 4 Screen VRAM  and Trainer)"
};
    
}
