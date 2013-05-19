/*
 * CPU6502MemoryModelInterface.java
 *
 * Created on December 14, 2006, 11:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502.mvc;

import emulator.core.CPU6502.MemoryWriteObserver;

/**
 *
 * @author abailey
 */
public interface CPU6502MemoryModelInterface extends MemoryWriteObserver {
     void updateMemory(byte memory[]);
}
