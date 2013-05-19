/*
 * PPU.java
 *
 * Created on October 25, 2007, 1:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes;

import model.NESPaletteModel;
import utilities.ByteFormatter;
import emulator.core.CPU6502.MemoryInterface;
import emulator.tv.Display;

/**
 * Picture Processing Unit of the NES
 * @author abailey
 */
public final class PPU implements IOMappedMemory, NESPaletteModel {

    /* From the wiki. There are 8 PPU registers exposed to the CPU
     *These nominally sit at $2000 through $2007 in the CPU's address space, but because they're incompletely decoded, they're mirrored in every 8 bytes from $2008 through $3FFF, so a write to $3456 is the same as a write to $2006.

     *
    PPUCTRL ($2000)
    Various flags controlling PPU operation (write)

    76543210
    ||||||||
    ||||||++- Base nametable address
    ||||||    (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
    |||||+--- VRAM address increment per CPU read/write of PPUDATA
    |||||     (0: increment by 1, going across; 1: increment by 32, going down)
    ||||+---- Sprite pattern table address for 8x8 sprites (0: $0000; 1: $1000)
    |||+----- Background pattern table address (0: $0000; 1: $1000)
    ||+------ Sprite size (0: 8x8; 1: 8x16)
    |+------- PPU master/slave select (has no effect on the NES)
    +-------- Generate an NMI at the start of the
    vertical blanking interval (0: off; 1: on)

     * Also important for scrolling
    76543210
    ||
    |+- 1: Add 256 to the X scroll position
    +-- 1: Add 240 to the Y scroll position

     *
    PPUMASK ($2001)
    Screen enable, masking, and intensity (write)

    76543210
    ||||||||
    |||||||+- Grayscale (0: normal color; 1: AND all palette entries
    |||||||   with 0x30, effectively producing a monochrome display;
    |||||||   note that colour emphasis STILL works when this is on!)
    ||||||+-- Enable background in leftmost 8 pixels of screen (0: clip; 1: display)
    |||||+--- Enable sprite in leftmost 8 pixels of screen (0: clip; 1: display)
    ||||+---- Enable background rendering
    |||+----- Enable sprite rendering
    ||+------ Intensify reds (and darken other colors)
    |+------- Intensify greens (and darken other colors)
    +-------- Intensify blues (and darken other colors)

    PPUSTATUS ($2002)
    Reading PPUSTATUS will clear D7 of PPUSTATUS and also the address latch used by PPUSCROLL and PPUADDR.

    Caution: Reading PPUSTATUS at the exact start of vertical blank will return a 0 in D7 but clear the latch anyway, causing the program to miss frames.

     *
     *
    OAMADDR ($2003)
    OAM address (write)

    Write the address of OAM you want to access here. Most games just write $00 here and then use OAM_DMA ($4014).

    This register also seems to affect Sprite 0 Hit, though it is not yet understood exactly how it does. The upper 5 bits of this register seem to select which SPR-RAM data is used for sprites 0 and 1 (instead of the first 8 bytes of SPR-RAM), though actual behavior varies between resets.

    OAMDATA ($2004)
    OAM data port (r/w)

    Write OAM data here. Writes will increment OAMADDR; reads won't.

    Most games access this register through $4014 instead. Reading OAMDATA while the PPU is rendering will expose internal OAM accesses during sprite evaluation and loading; Micro Machines does this.

    PPUSCROLL ($2005)
    Scroll register (2x write) (first x scroll value, then y scroll value)
    Horizontal offsets range from 0 to 255. "Normal" vertical offsets range from 0 to 239. (Values of 240 to 255 are treated as -16 through -1 in a way, pulling tile data from the attribute table.)


    PPUADDR ($2006)
    VRAM address register (2x write)

    After reading PPUSTATUS to reset the address latch, write the 16-bit address of VRAM you want to access here, upper byte first. Valid addresses are $0000-$3FFF.

    Access to PPUSCROLL and PPUADDR during screen refresh produces interesting raster effects; the starting position of each scanline can be set to any pixel position in nametable memory. For more information, see "The Skinny on NES Scrolling" by loopy, available from the main site.

    PPUDATA ($2007)
    VRAM data register (r/w)

    When the screen is turned off in PPUMASK or during vertical blank, read or write data from VRAM through this port.

    Reads are delayed by one cycle; discard the first byte read. Do not attempt to access this register while the PPU is rendering; if you do, Bad Things will happen (i.e. graphical glitches and RAM corruption).


     *
     */
    // 20 lines for vblank
    // 262 lines total
    // needs some adjusting
    private final static int PPU_DEV_MODE_OFF = 0;
    private final static int PPU_DEV_MODE_MINIMAL = 1;
    private final static int PPU_DEV_MODE_NORMAL = 2;
    private final static int PPU_DEV_MODE_EXTENDED = 3;
    private final static int PPU_DEV_MODE_TRACE = 4;
    private final static int PPU_DEV_MODE_DEV = 5;
    
    private final static int PPU_DEV_MODE = PPU_DEV_MODE_OFF;

    public final static byte VBL_MASK = (byte) 0x80; // bit 7
    public final static byte SPRITE_ZERO_MASK = (byte) 0x40; // bit 6
    public final static byte SPRITE_OVERFLOW_MASK = (byte) 0x20; // bit 5
    public final static byte SPRITE_OVERFLOW_MASK_CLEAR = (byte) 0xDF; // bit 5
    public final static byte VBL_MASK_CLEAR = (byte) 0x7F;

    private final static int DUMMY_LINE = 20;
    private final static int ACTIVE_LINES = 260;
    private final static int SLEEP_LINE = 261;

    private final static int PPU_MASTER_CYCLES = 5;
    private final static int PPU_VBLANK_SCANLINES_NTSC = 20;
    private final static int PPU_VBLANK_SCANLINES_PAL = 70;
    private final static int PPU_SCANLINE_CYCLES = 340;

    private MemoryInterface _mem = null;
    private NES _nes = null;
    private Display _display = null;
    private int master_ppu_clock = 0;
    private int vblank_num_scanlines = PPU_VBLANK_SCANLINES_NTSC;
    private int scanline = -1;
    private int scanline_cycle = 0;
    private int spriteZeroPixelsHit = 0;
    private int _loopyV = 0; // the PPU address
    private int _loopyT = 0; // the temp PPU address
    private byte _ppuLatch = 0x00;
    private byte _ppuReadBuffer = 0x00;
    private byte _ppu_ctrl = 0x00;
    private byte _ppu_mask = 0x00;
    private byte _ppu_status = 0x00;
    private byte _ppu_oam_addr = 0x00;
    private byte _ppu_fineX_scroll = 0x00;
    private byte _ppu_fineY_scroll = 0x00;

    private boolean oddFrame = false;
    private boolean debugMode = false;
    private boolean suppressNMI = false;

    private int ntMirroring[] = { 0,1,2,3 };

    private boolean highBitToggle = false;

    // _ppu_mask flags
    private boolean isMonochrome = false;
    private boolean isLeftBGEnabled = false;
    private boolean isLeftSpriteEnabled = false;
    private boolean isBGEnabled = false;
    private boolean isSpriteEnabled = false;
    private boolean isRedIntensified = false;
    private boolean isGreenIntensified = false;
    private boolean isBlueIntensified = false;

    private byte primarySpriteOAM[] = new byte[256];// 256 primary
    private byte secondarySpriteOAM[] = new byte[32]; // 32 secondary
    private byte spriteScanline[] = new byte[32]; // holds 8 sprites
    private int primaryIndex = 0;
    private int secondaryIndex = 0;

    private boolean specialSpriteFlag = false;
    private boolean isSpriteZeroOnSetupScanline = false;
    private boolean isSpriteZeroOnRenderScanline = false;
    private boolean unsetNMIFlag = false;

    /** Creates a new instance of PPU */
    public PPU() {
        _mem = null;
        clear();
        setNTSCMode();
    }

    void setMemoryInterface(MemoryInterface mem) {
        _mem = mem;
    }

    public void ppuWarning(String warningString) {
        if (debugMode) {
            System.out.println(warningString);
        }
    }

    public void ppuDebug(String debugString) {
        if (debugMode) {
            System.out.println(debugString);
        }
    }


    // Clear any PPU states, etc..
    // TO DO:  http://www.nesdevwiki.org/wiki/Power-up_state_of_PPU
    public void clear() {
        if (PPU_DEV_MODE >= PPU_DEV_MODE_EXTENDED) {
            System.out.println("PPU Clear called");
        }
        master_ppu_clock = 0;
        scanline = -1;
        scanline_cycle = 0;
        oddFrame = true;
        unsetNMIFlag = false;
        suppressNMI = false;
        vblank_num_scanlines = PPU_VBLANK_SCANLINES_NTSC;

        _ppuLatch = 0x00;
        highBitToggle = false;
        _ppuReadBuffer = 0x00;
        isSpriteZeroOnSetupScanline = false;
        isSpriteZeroOnRenderScanline = false;

        _ppu_ctrl = 0x00;
        _ppu_mask = 0x00;
        _ppu_status = 0x00;
        _ppu_oam_addr = 0x00;
        _ppu_fineX_scroll = 0x00;
        _ppu_fineY_scroll = 0x00;
        _loopyT = 0;
        _loopyV = 0;
    }

    public void setPalMode() {
        vblank_num_scanlines = PPU_VBLANK_SCANLINES_PAL;
    }

    public void setNTSCMode() {
        vblank_num_scanlines = PPU_VBLANK_SCANLINES_NTSC;
    }

    public void setNESCallback(NES nes, Display display, boolean isNTSC) {
        _nes = nes;
        _display = display;
        if (isNTSC) {
            setNTSCMode();
        } else {
            setPalMode();
        }
    }

    public void triggerNMI() {
//    	System.out.println("Triggered NMI at scanline:" + scanline);
        _nes.preTriggerNMI(master_ppu_clock);
        master_ppu_clock = 0;
        scanline = 0;
        oddFrame = !oddFrame;
        unsetNMIFlag = false;
        _display.refreshDisplay();
    }

    /*

     VRAM access via 2007
     --------------------
If the VRAM address increment bit (2000.2) is clear (inc. amt. = 1), all the
scroll counters are daisy-chained (in the order of HT, VT, H, V, FV) so that
the carry out of each counter controls the next counter's clock rate. The
result is that all 5 counters function as a single 15-bit one. Any access to
2007 clocks the HT counter here.

If the VRAM address increment bit is set (inc. amt. = 32), the only
difference is that the HT counter is no longer being clocked, and the VT
counter is now being clocked by access to 2007.
*/

    public int getPPUIncrement() {
        if ((_ppu_ctrl & 0x4) == 0x4) {
            return 32;
        } else {
            return 1;
        }
    }

    public void postSetPPUMask() {
        isMonochrome = ((_ppu_mask & 0x1) == 0x1);
        isLeftBGEnabled = ((_ppu_mask & 0x2) == 0x2);
        isLeftSpriteEnabled = ((_ppu_mask & 0x4) == 0x4);
        isBGEnabled = ((_ppu_mask & 0x8) == 0x8);
        isSpriteEnabled = ((_ppu_mask & 0x10) == 0x10);
        isRedIntensified = ((_ppu_mask & 0x20) == 0x20);
        isGreenIntensified = ((_ppu_mask & 0x40) == 0x40);
        isBlueIntensified = ((_ppu_mask & 0x80) == 0x80);
    }

    public boolean isBGEnabled() {
        return isBGEnabled; // ((_ppu_mask & 0x8) == 0x8);
    }

    public boolean isLeftBGEnabled() {
        return isLeftBGEnabled; // ((_ppu_mask & 0x2) == 0x2);
    }

    public boolean isSpriteEnabled() {
        return isSpriteEnabled; // ((_ppu_mask & 0x10) == 0x10);
    }

    public boolean isLeftSpriteEnabled() {
        return isLeftSpriteEnabled; // ((_ppu_mask & 0x4) == 0x4);
    }

    public boolean isNMIEnabled() {
        // there is a special case
        // If PPUSTATUS was queried one PPU cycle before VBLANK, or on cycle 0 or 1, NMI acts differently
        // Returns true if BIT 7 of _ppu_ctrl (0x2000) is set to 1
        if (suppressNMI) {
            System.out.println("NMI Suppressed");
            suppressNMI = false;
            return false;
        }
        return ((_ppu_ctrl & 0x80) == 0x80);
    }

    public final void processInstructionsUntil(int cpuTimestamp) {

        if (master_ppu_clock >= cpuTimestamp) {
            return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
        }


        while (scanline < DUMMY_LINE) {
            suppressNMI = false;
            while (scanline_cycle < PPU_SCANLINE_CYCLES) {
                scanline_cycle++;
                master_ppu_clock += PPU_MASTER_CYCLES;
                if (master_ppu_clock >= cpuTimestamp) {
                    return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
                }
            }
            // vblank means we do nothing for N number of scanlines
            scanline++;
            scanline_cycle = 0;
            if (scanline == DUMMY_LINE) {
                _ppu_status = 0x00;
                spriteZeroPixelsHit = 0;
                isSpriteZeroOnSetupScanline = false;
                isSpriteZeroOnRenderScanline = false;
            }
            _nes.notifyScanlineChanged();
            master_ppu_clock += PPU_MASTER_CYCLES;
            if (master_ppu_clock >= cpuTimestamp) {
                return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
            }
        }


        if (scanline == DUMMY_LINE) {
            // clear VBL, sprite zero, and overflow
            // copy loopyT to loopyV (copies temp PPU address to the one that is used during rendering)
            if (scanline_cycle == 0) {
                //      _ppu_status = 0x00;
                //      spriteZeroPixelsHit = 0;
                //      isSpriteZeroOnSetupScanline = false;
                //      isSpriteZeroOnRenderScanline = false;

                if (isBGEnabled() || isSpriteEnabled()) {
                    _loopyV = _loopyT;
                    // extract fineY
                    // t:0111000000000000=d:00000111
                    _ppu_fineY_scroll = (byte) ((_loopyV >> 12) & 0x7);
                }
            }

            while (scanline_cycle < PPU_SCANLINE_CYCLES) {
                scanline_cycle++;
                master_ppu_clock += PPU_MASTER_CYCLES;
                // Blargg's testing indicates the even/odd skip occurs line 20, clock 339
                if (scanline_cycle == 339) {
                    if (oddFrame && vblank_num_scanlines == PPU_VBLANK_SCANLINES_NTSC && isBGEnabled()) {
                        scanline_cycle++; // skip a cycle
                    }
                }
                if (master_ppu_clock >= cpuTimestamp) {
                    return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
                }
            }
            scanline++;
            scanline_cycle = 0;
            _nes.notifyScanlineChanged();
            master_ppu_clock += PPU_MASTER_CYCLES;
            if (master_ppu_clock >= cpuTimestamp) {
                return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
            }
        }


        // draw the pixels...
        while (scanline <= ACTIVE_LINES) {
            while (scanline_cycle < PPU_SCANLINE_CYCLES) {
                doScanlineCycle(scanline - (DUMMY_LINE + 1));
                scanline_cycle++;
                master_ppu_clock += PPU_MASTER_CYCLES;
                if (master_ppu_clock >= cpuTimestamp) {
                    return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
                }
            }
            scanline++;
            scanline_cycle = 0;
            if (isBGEnabled() || isSpriteEnabled()) {
                //  v:0000010000011111=t:0000010000011111
                _loopyV = (_loopyV & 0xFBE0) + (_loopyT & 0x41F);
            }

            _nes.notifyScanlineChanged();
            master_ppu_clock += PPU_MASTER_CYCLES;
            if (master_ppu_clock >= cpuTimestamp) {
                return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
            }
        }

        if (scanline == SLEEP_LINE) {
            while (scanline_cycle < PPU_SCANLINE_CYCLES) {
                scanline_cycle++;
                master_ppu_clock += PPU_MASTER_CYCLES;
                if (master_ppu_clock >= cpuTimestamp) {
                    return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
                }
            }
            scanline++;
            scanline_cycle = 0;
            _nes.notifyScanlineChanged();
            master_ppu_clock += PPU_MASTER_CYCLES;
            if (master_ppu_clock > cpuTimestamp) {
                return; // the PPU is ahead of the CPU. we need to wait for PPU to catch up
            }
        }

 //       System.out.println("Trigger NMI :" + master_ppu_clock + " unset:" + unsetNMIFlag + " suppress:" + suppressNMI);
        
        // end of frame
        if (!unsetNMIFlag) {
            _ppu_status = (byte) (_ppu_status | VBL_MASK); // sets only the top bit
        }

        triggerNMI();

    }

    public String getStateString() {
        return "Master Cycles: " + master_ppu_clock;
    }

    public int getPPUCycle() {
        return master_ppu_clock;
    }

    private void incrementSpriteZeroHit() {
        spriteZeroPixelsHit++;
        if (spriteZeroPixelsHit == 1) {
            _ppu_status = (byte) (_ppu_status | SPRITE_ZERO_MASK);
        }
    }

    public byte getBGPixelFromPaletteIndex(int palIndex) {
        int address = 0x3F00;
        if (palIndex % 4 != 0) {
            address = 0x3F00 + palIndex;
        }
        return _mem.getCHRMemory(address);
    }

    public byte getSpritePixelDirect(boolean isPrimary, int spriteIndex, int origX, int origY, int screenX, int screenY, int bgIndex) {
        return _mem.getCHRMemory(0x3F00 + getSpritePixelIndexDirect(isPrimary, spriteIndex, origX, origY, screenX, screenY, bgIndex, false));
    }

    public int getSpritePixelIndexDirect(boolean isPrimary, int spriteIndex, int origX, int origY, int screenX, int screenY, int bgIndex, boolean isPartOfPipeline) {
        int spriteY = (isPrimary) ? (primarySpriteOAM[spriteIndex * 4] & 0xFF) : (spriteScanline[spriteIndex * 4] & 0xFF);
        if (spriteY >= 240) {
            return 0;
        }
        byte attrib = (isPrimary) ? (primarySpriteOAM[spriteIndex * 4 + 2]) : (spriteScanline[spriteIndex * 4 + 2]);

        int x = origX;
        int y = origY;
        int tile = (isPrimary) ? (primarySpriteOAM[spriteIndex * 4 + 1] & 0xFF) : (spriteScanline[spriteIndex * 4 + 1] & 0xFF);
        int st = (((_ppu_ctrl & 0x8) == 0x8) ? 0x1000 : 0x0000) + tile * 16;
        int hgt = 7;
        if (getSpriteSize() == 16) {
            hgt = 15;
            // if offset of the sprite value exceeds 8, use 1000 else 0000
            st = ((origY >= 8) ? 0x1000 : 0x0000) + tile * 16;
        }
        boolean flipX = ((attrib & 0x40) == 0x40);
        boolean flipY = ((attrib & 0x80) == 0x80);
        if (flipX) {
            x = 7 - x;
        }
        if (flipY) {
            y = hgt - y;
        }

        byte m1 = _mem.getCHRMemory(st + y);
        byte m2 = _mem.getCHRMemory(st + 8 + y);
        m1 = (byte) (m1 >> (7 - x));
        m2 = (byte) (m2 >> (7 - x));
        int palIndex = ((m1 & 1) + ((m2 & 1) << 1));
        palIndex += ((attrib & 0x3) << 2);
        if (palIndex % 4 == 0) {
            return 0;
        } else {
            // even if we are obstructed by the background, we still need to increment sprite zero hit if applicable
            if (isPartOfPipeline && isSpriteZeroOnRenderScanline && screenX != 255 && spriteIndex == 0 && bgIndex != 0) {
                incrementSpriteZeroHit();
            }
            if (((attrib & 0x20) == 0x20) && bgIndex != 0) {
                return 0;
            }
            return (16 + palIndex);
        }
    }

    public byte getCHRTilePixelDirect(int tile, int x, int y, int startingPoint) {
        // startingPoint $0000; 1: $1000
        int remX = x % 8;
        int remY = y % 8;
        int shift = 7 - remX;
        int mask = 1 << shift;
        int pos = startingPoint + tile * 16 + remY;
        int b1 = _mem.getCHRMemory(pos) & 0xFF;
        int b2 = _mem.getCHRMemory(pos + 8) & 0xFF;

        b1 = (b1 & mask) >> shift;
        b2 = (b2 & mask) >> shift;

        int palIndex = ((b2 << 1) + b1);
        return _mem.getCHRMemory(0x3F00 + (palIndex & 0x3));
    }

    public int getBGPixelIndex(int x, int y) {
        //grab courseX, courseY, fineX, fineY

        // bottom 5 bits of loopyV are course scroll
        int calcX = x + (((_loopyV & 0x1F) << 3) | _ppu_fineX_scroll) + (256 * ((_loopyV >> 10) & 0x1));


        // Normally: You go from scroll 239 on NT $2000 to scroll 0 on NT $2800
        // But if you have a scroll over 239, then you go from 255 on NT $2000 to scroll 0 on NT $2000 (not $2800, which would be logical)
        int lower = ((((_loopyV >> 5) & 0x1F) << 3) | _ppu_fineY_scroll);
        int upper = (240 * ((_loopyV >> 11) & 0x1));
        int calcY = y + lower + upper;

        //  Mostly works
        // int calcY = y +  ((((_loopyV >> 5)& 0x1F)<<3)|_ppu_fineY_scroll) + (240*((_loopyV>>11)&0x1));

        return getBGPixelIndexDirect(calcX, calcY, (lower >= 240));
    }

    public int getBGPixelIndexDirect(int x, int y) {
        return getBGPixelIndexDirect(x, y, false);
    }

    public int getBGPixelIndexDirect(int x, int y, boolean specialY) {
        int xVal = x % 512;
        int yVal = y % 480;

        // get nametable value and dont forget scrolling
        // get oam
        int xPlus = 0x0000;
        int yPlus = 0x0000;
        if (xVal >= 256) {
            xPlus = 0x0400;
            xVal -= 256;
        }
        if (yVal >= 240) {
            if (!specialY) {
                yPlus = 0x0800;
            }
            yVal -= 240;
        }
        int tileIndexX = xVal / 8;
        int tileIndexY = yVal / 8;
        int ntOffset = 0x2000 + xPlus + yPlus;

        int remX = xVal % 8;
        int remY = yVal % 8;

        int ntIndex = ntOffset + tileIndexY * 32 + tileIndexX;
        int oamSpot = (32 * 30) + ((tileIndexY / 4) * 8) + (tileIndexX / 4);
        int oamIndex = ntOffset + oamSpot;

        return calcBGPixelIndexDirect(remX, remY, tileIndexX, tileIndexY, ntIndex, oamIndex);
    }

    public int calcBGPixelIndexDirect(int remX, int remY, int tileIndexX, int tileIndexY, int ntIndex, int oamIndex) {
        int shift = 7 - remX;
        int mask = 1 << shift;

        int pos = ((_ppu_ctrl & 0x10) << 8) + ((getNameTableByte(ntIndex) & 0xFF) << 4) + remY;
        int b1 = ((_mem.getCHRMemory(pos) & 0xFF) & mask) >> shift;
        int b2 = ((_mem.getCHRMemory(pos + 8) & 0xFF) & mask) >> shift;
      
        return ((((getNameTableByte(oamIndex) & 0xFF) >> ((((((tileIndexY & 3) >> 1) << 1) + ((tileIndexX & 3) >> 1)) << 1))) & 0x03) << 2) + ((b2 << 1) + b1);
    }


// returns the palette index or 0 if its transparent
    public int calcSprite(int x, int y, int bgIndex) {
    	
   // 	System.out.println("Sprite calc:" + x + " , " + y);
    	
        for (int i = 0; i < 8; i++) {
            int yVal = 1 + spriteScanline[i * 4] & 0xFF;
            int offY = y - yVal;
            int xVal = spriteScanline[i * 4 + 3] & 0xFF;
            int offX = x - xVal;
            if (offX < 0 || offY < 0 || offX >= 8 || offY >= getSpriteSize()) {
                continue;
            }
            int val = getSpritePixelIndexDirect(false, i, offX, offY, x, y, bgIndex, true);
            if(val != 0){
                return val;
            }
//            return getSpritePixelIndexDirect(false, i, offX, offY, x, y, bgIndex, true);
        }
        return 0;
    }

    private void reallyPlotPixel(int x, int y, boolean isBG, boolean isSpr) {
       
    	int val = 0; // background color
        if (isBG) {
            val = getBGPixelIndex(x, y);
        } else {
            // When  BG rendering is off, we display color index 0
            // unless the PPU is currently pointing at $3Fxx in which case that value is
            // NOT WORKING!!!
            if ((_loopyV & 0x3F00) == 0x3F00) {
                val = _loopyV & 0x1F;
                if ((val & 0x13) == 0x10) {
                    val ^= 0x10; // mirrored entries
                }
            }
        }
        int palIndex = val;

        if (val % 4 == 0) {
            palIndex = 0;
        }
        if (isSpr) {
            int val2 = calcSprite(x, y, (val % 4));
            if (val2 % 4 == 0) {
                val2 = 0;
            }
            if ((val % 4 == 0) || val2 != 0) {
                palIndex = val2;
            }
        }
        byte palColor = _mem.getCHRMemory(0x3F00 + palIndex);
        _display.plot(x, y, (palColor & 0xFF), isMonochrome, isRedIntensified, isGreenIntensified, isBlueIntensified);
    }

    private void doScanlineCycle(int line) {

        if (scanline_cycle < 256) {
            if (scanline_cycle < 8) {
                reallyPlotPixel(scanline_cycle, line, (isLeftBGEnabled && isBGEnabled), (isLeftSpriteEnabled && isSpriteEnabled));
            } else {
                reallyPlotPixel(scanline_cycle, line, isBGEnabled, isSpriteEnabled);
            }
            

            // I can safely set this to FF since it is only _ppu_oam_data that's value matters mid scanline
            // Evaluate sprites (for next scanline)
            if (scanline_cycle < 64) { // cycles 0 to 63 initialize secondary OAM to FF
        //        _ppu_oam_data = (byte) 0xFF;
                if (scanline_cycle == 0) {
                    for (int i = 0; i < 32; i++) {
                        secondarySpriteOAM[i] = (byte) 0xFF;
                    }
                }
            // setup phase
            } else { // cycles 64 to 256 // sprite evaluation
                if (scanline_cycle == 64) {
                    // first cycle of sprite evaluation.  Lets set this up properly
                    primaryIndex = 0;
                    secondaryIndex = 0;
                    specialSpriteFlag = true;
                    isSpriteZeroOnSetupScanline = false;
                }
                if (primaryIndex < 64) {
                    byte _ppu_oam_data = primarySpriteOAM[primaryIndex * 4];
                    if (secondaryIndex < 8) {
                        secondarySpriteOAM[secondaryIndex * 4] = _ppu_oam_data;
                    }
                    int tmp = _ppu_oam_data & 0xFF;
                    if (line >= tmp && line < tmp + getSpriteSize()) {
                        if (secondaryIndex < 8) {
                            // copy the rest
                            secondarySpriteOAM[(secondaryIndex * 4) + 1] = primarySpriteOAM[primaryIndex * 4 + 1];
                            secondarySpriteOAM[(secondaryIndex * 4) + 2] = primarySpriteOAM[primaryIndex * 4 + 2];
                            secondarySpriteOAM[(secondaryIndex * 4) + 3] = primarySpriteOAM[primaryIndex * 4 + 3];
                            _ppu_oam_data = primarySpriteOAM[primaryIndex * 4 + 3];
                            if (primaryIndex == 0) {
                                isSpriteZeroOnSetupScanline = true; // sprite zero happens on next line
                            }
                        }
                        secondaryIndex++;
                        if (secondaryIndex == 9) {
                            setSpriteOverflow(); // overflow happens on current line
                        }
                    }
                    primaryIndex++;
                }
            }
        // from this point onward is HBLANK
        } else if (scanline_cycle <= 319) { // sprite fetches
            if (scanline_cycle == 256) {
                for (int i = 0; i < spriteScanline.length; i++) {
                    spriteScanline[i] = (byte) 0xFF;
                }
                if (secondaryIndex > 8) {
                    secondaryIndex = 8;
                }
                System.arraycopy(secondarySpriteOAM, 0, spriteScanline, 0, secondaryIndex * 4);
                isSpriteZeroOnRenderScanline = isSpriteZeroOnSetupScanline;
            }

            /*
            Cycles 256-319: Sprite fetches (8 sprites total, 8 cycles per sprite)
            1-4: Read the Y-coordinate, tile number, attributes, and X-coordinate of the selected sprite
            5-8: Read the X-coordinate of the selected sprite 4 times.
            On the first empty sprite slot, read the Y-coordinate of sprite #63 followed by $FF for the remaining 7 cycles
            On all subsequent empty sprite slots, read $FF for all 8 reads
             */
            int foo = scanline_cycle - 256;
            int foo1 = foo / 8;
            if (foo1 >= secondaryIndex) {
                if (specialSpriteFlag) {
                    specialSpriteFlag = false;
           //         _ppu_oam_data = primarySpriteOAM[63 * 4];
                } else {
           //         _ppu_oam_data = (byte) 0xFF;
                }
            } else {
                // copy sprites to scanline buffer
                int foo2 = foo % 8;
                if (foo2 > 3) {
                    foo2 = 3;
                }
          //      _ppu_oam_data = secondarySpriteOAM[foo1 * 4 + foo2];
            }
        } else { // 320..340 // background render pipeline init
            if (secondaryIndex > 0) {
        //        _ppu_oam_data = secondarySpriteOAM[0];
            } else {
        //        _ppu_oam_data = primarySpriteOAM[63 * 4];
            }

        }

    }

    /*
    // from the wiki
    During all visible scanlines, the PPU scans through OAM to determine which sprites to render on the next scanline. During each pixel clock (341 total per scanline), the PPU accesses OAM in the following pattern:

    Cycles 0-63: Secondary OAM (32-byte buffer for current sprites on scanline) is initialized to $FF - attempting to read $2004 will return $FF
    Cycles 64-255: Sprite evaluation
    On even cycles, data is read from (primary) OAM
    On odd cycles, data is written to secondary OAM (unless writes are inhibited, in which case it will read the value in secondary OAM instead)
    1. Starting at n = 0, read a sprite's Y-coordinate (OAM[n][0], copying it to the next open slot in secondary OAM (unless 8 sprites have been found, in which case the write is ignored).
    1a. If Y-coordinate is in range, copy remaining bytes of sprite data (OAM[n][1] thru OAM[n][3]) into secondary OAM.
    2. Increment n
    2a. If n has overflowed back to zero (all 64 sprites evaluated), go to 4
    2b. If less than 8 sprites have been found, go to 1
    2c. If exactly 8 sprites have been found, disable writes to secondary OAM
    3. Starting at m = 0, evaluate OAM[n][m] as a Y-coordinate.
    3a. If the value is in range, set the sprite overflow flag in $2002 and read the next 3 entries of OAM (incrementing 'm' after each byte and incrementing 'n' when 'm' overflows); if m = 3, increment n
    3b. If the value is not in range, increment n AND m (without carry). If n overflows to 0, go to 4; otherwise go to 3
    4. Attempt (and fail) to copy OAM[n][0] into the next free slot in secondary OAM, and increment n (repeat until HBLANK is reached)
    Cycles 256-319: Sprite fetches (8 sprites total, 8 cycles per sprite)
    1-4: Read the Y-coordinate, tile number, attributes, and X-coordinate of the selected sprite
    5-8: Read the X-coordinate of the selected sprite 4 times.
    On the first empty sprite slot, read the Y-coordinate of sprite #63 followed by $FF for the remaining 7 cycles
    On all subsequent empty sprite slots, read $FF for all 8 reads
    Cycles 320-340: Background render pipeline initialization
    Read the first byte in secondary OAM (the Y-coordinate of the first sprite found, sprite #63 if no sprites were found)
    This pattern was determined by doing carefully timed reads from $2004 using various sets of sprites. In the case where there are 8 sprites on a scanline, the sprite evaluation logic effectively breaks and starts evaluating the tile number/attributes/X-coordinates of other sprites as Y-coordinates, resulting in rather inconsistent sprite overflow behavior (showing both false positives and false negatives).

    0 based
    PPU X address is incremented no earlier than every 3rd cycle on the scanline (3, 11, 19, etc)

    PPU Y address is incremented on cycle 251

    PPU X address is reset on cycle 257
     */
    public int getSpriteSize() {
        // check PPU CTRL to see if we are 8x16
        return (((_ppu_ctrl & 0x20) == 0x20) ? 16 : 8);
    }

    public boolean isOAMWritesEnabled() {
        return ((_ppu_status & SPRITE_OVERFLOW_MASK) != SPRITE_OVERFLOW_MASK);
    }

    private void setSpriteOverflow() {
        // This overflow flag is cleared on CPU cycle 2272 according to Blargg's tests: http://nesdev.parodius.com/bbs/viewtopic.php?p=11416
        if (PPU_DEV_MODE >= PPU_DEV_MODE_NORMAL) {
            System.out.println("Setting Sprite Overflow at clock:" + (master_ppu_clock / 5) + " at scanline" + scanline);
        }
        // Only set sprite overflow when sprite rendering (or BG rendering) enabled
        // but NOT if neither are enabled
        if (isSpriteEnabled | isBGEnabled) {
            _ppu_status = (byte) (_ppu_status | SPRITE_OVERFLOW_MASK);
        }
    }

    //From the wiki. There are 8 PPU registers exposed to the CPU
    //These nominally sit at $2000 through $2007 in the CPU's address space, but because they're incompletely decoded, they're mirrored in every 8 bytes from $2008 through $3FFF, so a write to $3456 is the same as a write to $2006.
    // therefore 0x2000 to $3FFF are mapped
    public boolean isReadMapped(int address) {
        return (address >= 0x2000 && address < 0x4000 || address == 0x4014);
    }
    //From the wiki. There are 8 PPU registers exposed to the CPU
    //These nominally sit at $2000 through $2007 in the CPU's address space, but because they're incompletely decoded, they're mirrored in every 8 bytes from $2008 through $3FFF, so a write to $3456 is the same as a write to $2006.
    // therefore 0x2000 to $3FFF are mapped

    public boolean isWriteMapped(int address) {
        return (address >= 0x2000 && address < 0x4000 || address == 0x4014);
    }

    // therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
    public byte getMappedMemory(int address) {
        return getRegister((address - 0x2000) % 8);
    }

    // therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
    public byte getMappedMemoryDirect(int address) {
        return getRegisterDirect((address - 0x2000) % 8);
    }

    // therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
    public void setMappedMemory(int address, byte val) {
        if (address == 0x4014) {
            setSpriteDMA(val);
        } else {
            setRegister((address - 0x2000) % 8, val);
        }
    }

    // deal with the eight different registers
    // If it is not a register we deal with specifically, the latch is assigned to the data;
    private void setRegister(int register, byte val) {
        // any time we write to a PPU register, the bottom 5 bits of PPUSTATUS reflect that value
        _ppu_status = (byte)((_ppu_status&0xE0)|(val & 0x1F));
        switch (register) {
            case 0:
                setPPUCTRL(val);
                break;  // 0x2000
            case 1:
                setPPUMASK(val);
                break; // 0x2001
            case 3:
                setOAMADDR(val);
                break; // 0x2003
            case 4:
                setOAMDATA(val);
                break; // 0x2004
            case 5:
                setPPUSCROLL(val);
                break; // 0x2005
            case 6:
                setPPUADDR(val);
                break; // 0x2006
            case 7:
                setPPUDATA(val);
                break; // 0x2007
            default:
                _ppuLatch = val;
                break;
        }

    }

    // deal with the eight different registers
    // Only 2,4,7 are readable
    // If it is not a register we deal with specifically, the latch is returned;
    private byte getRegister(int register) {
        byte val = _ppuLatch;
        switch (register) {
            case 2:
                val = getPPUSTATUS(); // 0x2002
                break;
            case 4:
                val = getOAMDATA();  // 0x2004
                break;
            case 7:
                val = getPPUDATA(); // 0x2007
                break;
            default:
                val = _ppuLatch;
                break;
        }
 //       System.out.println("getRegister: " + register + " = " + ByteFormatter.formatByte(val) + " at master cycle:" + master_ppu_clock + " S:" + scanline + " SC:" + scanline_cycle);

        if (PPU_DEV_MODE >= PPU_DEV_MODE_TRACE) {
            System.out.println("getRegister: " + register + " returning: " + ByteFormatter.formatByte(val));
        }
        // never gets here
        return val;
    }

    private byte getRegisterDirect(int register) {
        byte val = _ppuLatch;
        switch (register) {
            case 2:
                val = _ppu_status; // 0x2002
                break;
            case 4:
                val = primarySpriteOAM[_ppu_oam_addr & 0xFF];  // 0x2004
                break;
            case 7:
                val = getPPUDATA_direct(); // 0x2007
                break;
            default:
                break;
        }
        return val;
    }

    private byte getPPUSTATUS() {
//    Reading PPUSTATUS will clear D7 of PPUSTATUS and also the address latch used by PPUSCROLL and PPUADDR.
//    Caution: Reading PPUSTATUS at the exact start of vertical blank will return a 0 in D7 but clear the latch anyway, causing the program to miss frames.
// Reading $2002 within a few PPU clocks of when VBL is set results in special-case behavior.
// Reading one PPU clock before reads it as clear and never sets the flag or generates NMI for that frame.
// Reading on the same PPU clock or one later reads it as set, clears it, and suppresses the NMI for that frame.
// Reading two or more PPU clocks before/after it's set behaves normally (reads flag's value, clears it, and doesn't affect NMI operation).
// The VBL flag is cleared 6820 PPU clocks, or exactly 20 scanlines, after it is set.
// Retrieved from "http://www.nesdevwiki.org/wiki/PPU_Frame_Timing"
        byte val = _ppu_status;

   
      //  System.out.println("Reading PPU Status at  master cycle: " + master_ppu_clock );

        // Reading 1 PPU clock before VBL should suppress setting
        if ((scanline == SLEEP_LINE) && (scanline_cycle >= (PPU_SCANLINE_CYCLES))) {
            unsetNMIFlag = true;
        }

        if ((scanline == SLEEP_LINE) && (scanline_cycle >= (PPU_SCANLINE_CYCLES - 1))) {
            suppressNMI = true;
        }

        _ppu_status = (byte) (_ppu_status & VBL_MASK_CLEAR); // clears the top bit
        highBitToggle = false;
        _ppuLatch = _ppu_status;

        return val;

    }

    private byte getOAMDATA() {
        return primarySpriteOAM[_ppu_oam_addr&0xFF]; // does not increment
    }

    private byte getPPUDATA() {
        // apparently the latch gets assigned to the previous read
        // the previous read gets assigned to the real value
        // then the latch (or junk) gets returned, thus we are always one byte off.
        // this does not apply to the palette which can be returned immediately (although I dont think its really accessable)
        //int loc = (((_ppu_addr_high & 0xFF) << 8) | (_ppu_addr_low & 0xFF)) & 0x3FFF;

/*
 * When reading while the VRAM address is in the range 0-$3EFF, the read will return the contents of an internal buffer.
After the CPU reads, the PPU will then immediately read the byte at the current VRAM address into this internal buffer.
Thus, after setting the VRAM address, one should first read this register and discard the result.
This behavior doesn't occur when the VRAM address is in the $3F00-$3FFF palette range; reads come directly from palette RAM and don't affect the internal buffer.
Since accessing this register increments the VRAM address, it should not be accessed outside vblank when rendering is enabled, because it will cause graphical glitches,
and if writing, write to an unpredictable address in VRAM.
*/

        int loc = _loopyV & 0x3FFF;

        if ((loc & 0x3F00) == 0x3F00) {
            _ppuLatch = _mem.getCHRMemory(0x3F00 + (loc & 0x1F));
            // Note the following weirdness for palette reads affecting the read buffer for future nametable reads
            // http://nesdev.parodius.com/bbs/viewtopic.php?t=567
            // From Quietrust
            /*
             * Palette RAM consists of twenty-eight 6-bit words of DRAM embedded within the PPU and accessible when the VRAM address is between $3F00 and $3FFF (inclusive).
             * When you read PPU $3F00-$3FFF, you get immediate data from Palette RAM (without the 1-read delay usually present when reading from VRAM)
             * and the PPU will also fetch nametable data from the corresponding address (which is mirrored from PPU $2F00-$2FFF).
             * This phenomenon does not occur during writes (as it would result in corrupting the contents of the nametables when writing to the palette)
             * and only happens during reading (since it has no noticeable side effects).
             */
            _ppuReadBuffer = _mem.getCHRMemory(loc);
            //_ppuReadBuffer = getNameTableByte(loc & 0x2FFF);
        } else {
            _ppuLatch = _ppuReadBuffer;
            
            _ppuReadBuffer = _mem.getCHRMemory(loc);
        }
        incrementLoopyV(getPPUIncrement());
        return _ppuLatch;
    }

    private byte getPPUDATA_direct() {
        int loc = _loopyV & 0x3FFF;

        if ((loc & 0x3F00) == 0x3F00) {
            return _mem.getCHRMemory(0x3F00 + (loc & 0x1F));
        } else {
            return _ppuReadBuffer;
        }
    }


    // Setters
    private void setPPUCTRL(byte val) {
        if (PPU_DEV_MODE >= PPU_DEV_MODE_MINIMAL) {
            System.out.println("PPUCTRL assigned to:" + ByteFormatter.formatBits(val));
        }
        boolean oldNMI = ((_ppu_ctrl & 0x80) == 0x80);
        _ppu_ctrl = val;
        // loopyT bits 10 and 11 are set to the bottom 2 bits of the val
        //ie: t:0000110000000000=d:00000011
        _loopyT = (_loopyT & 0xF3FF) + ((_ppu_ctrl & 0x3) << 10);
        // if VBL is set, immediately set NMI
        if ((_ppu_status & VBL_MASK) == VBL_MASK) {
            if ((_ppu_ctrl & 0x80) == 0x80 && !oldNMI) { // dont trigger NMI if it was OFF before
                // VBL enabled, so fire NMI right away
                _nes.delayTriggerNMI();
            }
            // NMI shouldn't occur when disabled 0 PPU clocks after VBL
            if ((_ppu_ctrl & 0x80) == 0x0) {
                 _nes.suppressDelayTriggerNMI();
            }
        }
    }

    private void setPPUMASK(byte val) {
        if (PPU_DEV_MODE >= PPU_DEV_MODE_MINIMAL) {
            System.out.println("PPUMASK assigned to:" + ByteFormatter.formatBits(val));
        }
        _ppu_mask = val;
        postSetPPUMask();
    }

    private void setOAMADDR(byte val) {
        if (PPU_DEV_MODE >= PPU_DEV_MODE_MINIMAL) {
            System.out.println("OAMADDR assigned to:" + ByteFormatter.formatByte(val));
        }
        _ppu_oam_addr = val;

    }

    private byte memRead(int index) {
        return _nes._memoryManager.getMemory(index, false);
    }

    private void setSpriteDMA(byte val) {
        if (PPU_DEV_MODE >= PPU_DEV_MODE_MINIMAL) {
            System.out.println("Sprite DMA assigned to:" + ByteFormatter.formatByte(val));
        }
        // we are in DMA mode.
        for (int i = 0; i < 256; i+=4, _ppu_oam_addr+=4) {
            primarySpriteOAM[_ppu_oam_addr & 0xFF] = memRead((val & 0xFF) * 0x100 + i);
            primarySpriteOAM[(_ppu_oam_addr+1) & 0xFF] = memRead((val & 0xFF) * 0x100 + i+1);
            /*
             *  special case for byte 2: 
             *  From the wiki: http://wiki.nesdev.com/w/index.php/PPU_OAM
             *  Each OAM entry is 29 bits wide. The unimplemented bits of each sprite's byte 2 do not exist in the PPU. 
             *  On PPU revisions that allow reading PPU OAM through $2004, the unimplemented bits of each sprite's byte 2 always read back as 0. 
             *  This can be emulated by ANDing byte 2 with $E3, either when writing to OAM or when reading back.             
             */           
            primarySpriteOAM[(_ppu_oam_addr+2) & 0xFF] = (byte)(memRead((val & 0xFF) * 0x100 + i+2) & 0xE3);
            primarySpriteOAM[(_ppu_oam_addr+3) & 0xFF] = memRead((val & 0xFF) * 0x100 + i+3);
        }
        if (PPU_DEV_MODE >= PPU_DEV_MODE_DEV) {
            for (int i = 0; i < 64; i++) {
                System.out.println("Sprite:" + i + ") at:[" + (primarySpriteOAM[i * 4 + 3] & 0xFF) + "," + (primarySpriteOAM[i * 4] & 0xFF) + "]" + " Tile:" + ByteFormatter.formatByte(primarySpriteOAM[i * 4 + 1]) + " Attribs:" + ByteFormatter.formatBinaryByte(primarySpriteOAM[i * 4 + 2]));
            }
        }
    }

    private void setOAMDATA(byte val) {
        if (PPU_DEV_MODE >= PPU_DEV_MODE_DEV) {
            System.out.println("OAMDATA assigned to:" + ByteFormatter.formatByte(val) + " at: " + ByteFormatter.formatByte(_ppu_oam_addr));
        }
        if(((_ppu_oam_addr & 4) != 0) && ((_ppu_oam_addr & 2) == 0)){
            // special case for byte 2
            primarySpriteOAM[_ppu_oam_addr & 0xFF] = (byte)(val & 0xE3);
        } else {
            primarySpriteOAM[_ppu_oam_addr & 0xFF] = val;
        }
        _ppu_oam_addr = (byte)((_ppu_oam_addr+1) & 0xFF);
    }

    /*
    private void incrementLoopyT() {
        // increments the fine Y
        // bits 12,13,14
        int fineY = ((_loopyT >> 12) & 7) + 1;
        if (fineY > 7) {
            // clear fineY
            _loopyT = (_loopyT & 0x8FFF);
            // increase courseY
            int courseY = ((((_loopyT >> 5) & 0x1F) + 1) % 30);
            _loopyT = (_loopyT & 0xFC1F) + (courseY << 5);
        } else {
            // only increment fineY
            _loopyT = (_loopyT & 0x8FFF) + (fineY << 12);
        }
    }
*/

    private void incrementLoopyV(int inc) {
/*
 
        if(inc == 32){
           // special code to increment DOWNWARD instead of sideways
            if((_loopyV & 0x400) != ((_loopyV+inc)& 0x400)) {
                // 2000->2800
                // 2400->2C00
               _loopyV += 0x400;
           }
        }
  */
        _loopyV = (_loopyV + inc)  & 0x3FFF;
    }

    private void setPPUSCROLL(byte val) {
        if (highBitToggle) {
            if (PPU_DEV_MODE >= PPU_DEV_MODE_EXTENDED) {
                System.out.println("PPU SCROLL Y assigned to:" + ByteFormatter.formatByte(val));
            }
            // Normally: You go from scroll 239 on NT $2000 to scroll 0 on NT $2800
            // But if you have a scroll over 239, then you go from 255 on NT $2000 to scroll 0 on NT $2000 (not $2800, which would be logical)

            if ((val & 0xFF) >= 240) {
                // I need to do something weird here!!!
                // From Looopy's skinnynt
                //"
                //you can think of bits 0,1,2,3,4 of the vram address as the "x scroll"(*8)
                //that the ppu increments as it draws.  as it wraps from 31 to 0, bit 10 is
                //switched.  you should see how this causes horizontal wrapping between name
                //tables (0,1) and (2,3).

                //you can think of bits 5,6,7,8,9 as the "y scroll"(*8).  this functions
                //slightly different from the X.  it wraps to 0 and bit 11 is switched when
                //it's incremented from _29_ instead of 31.  there are some odd side effects
                //from this.. if you manually set the value above 29 (from either 2005 or
                //2006), the wrapping from 29 obviously won't happen, and attrib data will be
                //used as name table data.  the "y scroll" still wraps to 0 from 31, but
                //without switching bit 11.  this explains why writing 240+ to 'Y' in 2005
                //appeared as a negative scroll value.
            }
            // t:0000001111100000=d:11111000
            _loopyT = (_loopyT & 0xFC1F) | (((val >> 3) & 0x1F) << 5);
            // t:0111000000000000=d:00000111
            _loopyT = (_loopyT & 0x8FFF) | ((val & 0x7) << 12);

        } else {
            if (PPU_DEV_MODE >= PPU_DEV_MODE_EXTENDED) {
                System.out.println("PPU SCROLL X assigned to:" + ByteFormatter.formatByte(val));
            }
            //
            // t:0000000000011111=d:11111000
            _loopyT = (_loopyT & 0xFFE0) | ((val >> 3) & 0xFF);
            // dont forget to set fine scroll immediately
            _ppu_fineX_scroll = (byte) (val & 0x7); // fine X
        //       _ppu_x_scroll_tmp = val;
        }
        highBitToggle = !highBitToggle;
        _ppuLatch = val;
    }

    // write high byte first (assuming latch is cleared)
    private void setPPUADDR(byte val) {

        if (highBitToggle) {
            if (PPU_DEV_MODE >= PPU_DEV_MODE_EXTENDED) {
                System.out.println("PPU DATA LOW assigned to:" + ByteFormatter.formatByte(val));
            }
            // t:0000000011111111=d:11111111
            _loopyT = (_loopyT & 0x3F00) | (val & 0xFF);
            _loopyV = _loopyT;
            

            // extract fineY
            // t:0111000000000000=d:00000111
            _ppu_fineY_scroll = (byte) ((_loopyV >> 12) & 0x7);

            if((_loopyV & 0x3F00) != 0x3F00) {
                // not palette
                _ppuLatch = val;
            }


        } else {
            if (PPU_DEV_MODE >= PPU_DEV_MODE_EXTENDED) {
                System.out.println("PPU DATA HIGH assigned to:" + ByteFormatter.formatByte(val));
            }
            //  t:0011111100000000=d:00111111
            //  t:1100000000000000=0
            _loopyT = (_loopyT & 0xFF) | ((val & 0x3F) << 8);
//            _ppu_addr_high_swap = val;
        }
        highBitToggle = !highBitToggle;

        // SEMI
        //_ppuLatch = val;
    }

    private void setPPUDATA(byte val) {
        int loc = _loopyV & 0x3FFF; // $4000-$C000 is a mirror of $0000-$3FFF

        if (loc < 0x2000) { // pattern table
            _mem.setCHRMemory(loc, val, true);
        } else if (loc < 0x3F00) {
            // $2000 to$2FFF is nametable
            // $3000 to $3EFF is mirror of $2000-2EFF
            setNameTableByte(loc, val);
        }else { // $3F00 to $3FFF is palette
            setPalette(loc & 0x1F, (byte) (val & 0x3F));
        }
        incrementLoopyV(getPPUIncrement());
    }


    public void setHorizontalMirroringMode() {
        // 2000 and 2400 are both 2000
        // 2800 and 2C00 are both 2800
        setMirroringMode(0,0,2,2);
    }

    public void setVerticalMirroringMode() {
        // 2000 and 2800 are both 2000
        // 2400 and 2C00 are both 2400
        setMirroringMode(0,1,0,1);
    }

    public void setFourScreenMirroringMode() {
        // 2000 to $2C00 are all unique
        setMirroringMode(0,1,2,3);
    }

    public void setMirroringMode(int nt2000, int nt2400, int nt2800, int nt2C00) {
        ntMirroring[0] = nt2000;
        ntMirroring[1] = nt2400;
        ntMirroring[2] = nt2800;
        ntMirroring[3] = nt2C00;
    }


    public final int getNameTableMirroredAddress(int loc) {
        int nt = ((((loc & 0x2FFF) - 0x2000) >> 10) & 3 );
        return 0x2000 + (ntMirroring[nt] << 10) + (loc & 0x3FF);
    }

    public final void setNameTableByte(int loc, byte val) {
        _mem.setCHRMemory(getNameTableMirroredAddress(loc), val, true);
    }

    public final byte getNameTableByte(int loc) {
          return _mem.getCHRMemory(getNameTableMirroredAddress(loc));
    }
    // For external access and tracing

    public byte getXScrollDirect() {
        //
//        return (byte)(((_loopyT & 0x1F) << 3) | _ppu_fineX_scroll);
        return _ppu_fineX_scroll;
    }

    public byte getYScrollDirect() {
        return _ppu_fineY_scroll;
    }

    public byte get2007Direct() {
        return _ppuLatch;
    }

    public byte getPPUCTRLDirect() {
        return _ppu_ctrl;
    }

    public byte getPPUMASKDirect() {
        return _ppu_mask;
    }

    public byte getPPUStatusDirect() {
        return _ppu_status;
    }

    public int getScanlineIndex() {
        return scanline;
    }

    public int getScanlineCycle() {
        return scanline_cycle;
    }


    public byte getLatchDirect() {
        return _ppuLatch;
    }
    public byte getReadBufferDirect() {
        return _ppuReadBuffer;
    }
    public int getLoopyVDirect() {
        return _loopyV;
    }

    private void setPalette(int index, byte val) {
        if (PPU_DEV_MODE >= PPU_DEV_MODE_DEV) {
            System.out.println("PALETTE: " + ByteFormatter.formatSingleByteInt(index) + " assigned to:" + ByteFormatter.formatByte(val));
        }
        // only 3F00 and 3F10 will do writes
        if (index % 4 == 0) {
            if (index % 16 == 0) {
                //      System.out.println("Setting BASE PALETTE " + index + " to:" + ByteFormatter.formatByte(val));
                _mem.setCHRMemory(0x3F00, val, true);
                _mem.setCHRMemory(0x3F10, val, true);
//                palette[4] = val;
//                palette[8] = val;
//                palette[12] = val;

//                palette[16] = val;
//                palette[20] = val;
//                palette[24] = val;
//                palette[28] = val;
            }
        } else {
//            palette[index] = val;
        }
        _mem.setCHRMemory(0x3F00 + index, val, true);

    }
    // NesPaletteModel

    public void updateImagePaletteIndex(int index, byte val) {
        System.out.println("PPU updateImagePaletteIndex for NESPaleteModel not implemented");
    }

    public void updateSpritePaletteIndex(int index, byte val) {
        System.out.println("PPU updateSpritePaletteIndex for NESPaleteModel not implemented");
    }

    public byte getImagePaletteAtIndex(int index) {
        return _mem.getCHRMemory(0x3F00 + index);
    }

    public byte getSpritePaletteAtIndex(int index) {
        return _mem.getCHRMemory(0x3F00 + 16 + index);
    }
}
