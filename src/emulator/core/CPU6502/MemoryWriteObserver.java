/*
 * MemoryWriteObserver.java
 *
 * Created on December 3, 2008, 8:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

/**
 *
 * @author abailey
 */
public interface MemoryWriteObserver {
    void updateWriteMemory(int memLocation, byte val);
}
