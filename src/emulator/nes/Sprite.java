/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes;

/**
 *
 * @author abailey
 */
public class Sprite {
    private int x,y,tile;
    private byte attrib;
//    private int size = 8;
    private byte[] mask = new byte[16];
    private byte[] pix = new byte[64];
    public final static byte PRIORITY_MASK = 0x20;

    /*
     Byte 2 
Attributes 

76543210
||||||||
||||||++- Palette (4 to 7) of sprite
|||+++--- Unimplemented, reads back as 0
||+------ Priority (0: in front of background; 1: behind background)
|+------- Flip sprite horizontally
+-------- Flip sprite vertically
* 
     */
    
    public Sprite(byte y, byte tile, byte attrib, byte x){
        this.y = y & 0xFF;
        this.tile = tile & 0xFF;
        this.attrib = attrib;
        this.x = x & 0xFF;
//        this.size = 8;
    }
    
    public boolean isValid(int checkY){
        return (y >= 0 && y < 240 && y<=checkY && checkY < y+8 );
    }

    public void assignPattern(int startingOffset, int sz, byte[] ppuMem) {
        if(sz != 8){
            System.err.println("I do not yet support 8x16 sprites");
        }

        int st = startingOffset + tile*16;
            for(int i=0;i<16;i++){
                mask[i] = ppuMem[st + i];
            }
            int pixOffset = 0;
            for(int i=0;i<8;i++){
                byte m1 = mask[i];
                byte m2 = mask[i+8];
                for(int q=0;q<8;q++){
                    pix[pixOffset+7-q] = (byte)((m1 & 1) + ((m2 & 1)<<1));
                    m1 = (byte)(m1 >> 1);
                    m2 = (byte)(m2 >> 1);
                }
                pixOffset+=8;
            }
        
    }

    public int getEvaluatedIndex(int xVal, int yVal) {
        boolean flipX = ((attrib & 0x40) == 0x40);
        boolean flipY = ((attrib & 0x80) == 0x80);
        int newX = xVal - x;
        int newY = yVal - y;
        if((newX < 0) || (newX >= 8) || (newY < 0) || (newY>= 8) ){
            System.err.println("BAD ARGS");
        }
        if(flipX) { newX = 7 - newX; }
        if(flipY) { newY = 7 - newY; }
        return pix[newY*8 + newX];
    }

    public boolean hitsPixel(int xVal, int yVal){
        if(y >=240){
            return false;
        }
        return ((getEvaluatedIndex(xVal, yVal) % 4) != 0);
    }
/*
    public byte getEvaluatedPixel(int xVal, int yVal, byte[] palette){
        return palette[16 + ((attrib & 0x3)<<2) + getEvaluatedIndex(xVal,yVal)];
    }
*/
    public boolean isInFrontOfBackground(){
        return ! isBehindBackground();
    }

    public boolean isBehindBackground(){
        return ((attrib & PRIORITY_MASK) == PRIORITY_MASK);
    }

}
