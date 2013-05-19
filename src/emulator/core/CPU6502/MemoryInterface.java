/*
 * MemoryInterface.java
 *
 * Created on December 5, 2008, 8:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

/**
 *
 * @author abailey
 */
public interface MemoryInterface {
    public MemoryInterface makeCopy();
    public int determineAddress(int address);
    public byte getMemory(int address, boolean shouldNotify);
    public byte getMemoryDirect(int address); // bypass any side effects
    public void setMemory(int address, byte val, boolean shouldNotify);

    public byte getCHRMemory(int address);
    public void setCHRMemory(int address, byte val, boolean shouldNotify);
    
}
