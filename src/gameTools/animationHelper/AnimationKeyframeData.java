/*
 * AnimationKeyframeData.java
 *
 * Created on October 1, 2008, 11:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.animationHelper;

/**
 *
 * @author abailey
 */
public class AnimationKeyframeData {
    
    public byte spriteIndex = 0;
    public byte xPos = 0;
    public byte yPos = 0;
    public byte oamIndex = 0;
    public boolean isFlippedHorizontal = false;
    public boolean isFlippedVertical = false;
    public boolean isUnderBG = false;
    
    /** Creates a new instance of AnimationKeyframeData */
    public AnimationKeyframeData() {
    }
    
    public int hashCode() {
        return  (yPos<<24) | (spriteIndex<<16) | (getSpriteAttributesByte()<< 8) | xPos;        
    }

    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(! (obj instanceof AnimationKeyframeData)) {
            return false;
        }
        AnimationKeyframeData tv = (AnimationKeyframeData)obj;
        return (xPos == tv.xPos) && (yPos == tv.yPos) && (spriteIndex == tv.spriteIndex) && (getSpriteAttributesByte() == tv.getSpriteAttributesByte());
    }
    
    public void copyInto(AnimationKeyframeData dest){
        if(dest == null){
            return;
        }
        dest.spriteIndex = spriteIndex;
        dest.xPos = xPos;
        dest.yPos = yPos;
        dest.oamIndex = oamIndex;
        dest.isFlippedHorizontal = isFlippedHorizontal;
        dest.isFlippedVertical = isFlippedVertical;
        dest.isUnderBG = isUnderBG;
    }
    
    public byte getSpriteAttributesByte(){
        // bit7-flip sprite vertically
        // bit6-flip sprite horizontally
        // bit5-priority. (0:in front of background. 1:behind background
        // bit4-unimplemented
        // bit4,3,2-unimplemented
        // bit1,0-palette (4 to 7) of sprite
        return (byte)((isFlippedVertical ? 0x80 : 0x00)
                    | (isFlippedHorizontal ? 0x40 : 0x00)
                    | (isUnderBG ? 0x20 : 0x00)
                    | oamIndex
                    ); 
                        
    }
    public void setSpriteAttributesByte(byte b){
        // bit7-flip sprite vertically
        // bit6-flip sprite horizontally
        // bit5-priority. (0:in front of background. 1:behind background
        // bit4-unimplemented
        // bit4,3,2-unimplemented
        // bit1,0-palette (4 to 7) of sprite
        isFlippedVertical = ((b & 0x80)== 0x80);
        isFlippedHorizontal = ((b & 0x40)== 0x40);
        isUnderBG = ((b & 0x20)== 0x20);
        oamIndex = (byte)(b & 0x3);
    }
    
}
