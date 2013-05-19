/*
 * Mapper.java
 *
 * Created on October 25, 2007, 1:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes;

/**
 *
 * @author abailey
 */
public interface Mapper extends IOMappedMemory {
    
    public void setPPURef(PPU ppuRef);

    public void setPRGMemory(byte[] data);
    public void setCHRMemory(byte[] data);
    public byte[] getInitialPRGData();

    boolean isCHRReadMapped(int address);
    boolean isCHRWriteMapped(int address);
    public byte getCHRMappedMemory(int address);
    public void setCHRMappedMemory(int address, byte val);

}
