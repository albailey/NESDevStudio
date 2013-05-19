/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.ui;

import emulator.nes.PPU;

/**
 *
 * @author abailey
 */
public class SpritePanel extends MultiNameTablePanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8337465551030421091L;
	private boolean primaryMode = true;
    private int numWide = 1;
    private int numHigh = 1;
    public SpritePanel() {
        this(true,8,8);
    }

    public SpritePanel(boolean mode, int numWide, int numHigh){
        super(numWide*8, numHigh*8,2); // 64 sprites total
        this.numWide = numWide;
        this.numHigh = numHigh;
        primaryMode = mode;
    }
    public String toString(){
    	return "SpritePanel. Width: " + numWide + " Height:" + numHigh + " PrimaryMode:" + primaryMode;
    }
    
     public byte calculatePixel(int x, int y, PPU ppu){
         int sprX =  x/8;
         int sprY =  y/8;
         int spriteIndex = sprY*numWide+sprX;
         return ppu.getSpritePixelDirect(primaryMode, spriteIndex, x %8, y %8, x, y, 0 );
    }
}
