/*
 * Utilities6502.java
 *
 * Created on November 23, 2006, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

/**
 *
 * @author abailey
 */
public class Utilities6502 {
    // we dont want to istantiate it
    private Utilities6502() {
    }
    
    public static int calculate16BitAddress(byte low, byte high){
        int lowByte = low & 0xFF;    // 8 bits
        int highByte = high & 0xFF; // 8 bits
        return ((highByte << 8) + lowByte) & 0xFFFF;    
    }
    public static int calculate16BitAddressWithOffset(byte low, byte high, int offset, PageBoundaryObserver callback){
        int lowByte = low & 0xFF;    // 8 bits
        int highByte = high & 0xFF; // 8 bits
        int startAddress =  (highByte << 8) + lowByte;
        if((callback != null) && ((startAddress & 0xFF00) != ((startAddress + offset) & 0xFF00))) {
            callback.notifyPageCrossed();
        }
        return (startAddress + offset) & 0xFFFF ;
    }    
    public final static int determineAddress(int index, byte romData[]) {
        if(index < 0 || index+1 >= romData.length){
            System.err.println("Index: " + index + " out of range. Min = 0 Max=" + romData.length);
            return -1;
        }
        return calculate16BitAddress(romData[index], romData[index+1]);
    }

}
