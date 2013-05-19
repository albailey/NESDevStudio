/*
 * IOMappedMemory.java
 *
 * Created on December 5, 2008, 10:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes;

/**
 *
 * @author abailey
 */
public interface IOMappedMemory {

    public boolean isReadMapped(int address);
    public boolean isWriteMapped(int address);
    // these next two should never be called unless isMapped returns true
    public byte getMappedMemory(int address);
    public void setMappedMemory(int address, byte val);

    public byte getMappedMemoryDirect(int address); // provides the ability to read the data without any side effects
    
  
  
}
