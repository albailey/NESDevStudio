/*
 * CPU6502ViewInterface.java
 *
 * Created on December 14, 2006, 9:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502.mvc;

import emulator.core.CPU6502.CPU6502;

/**
 *
 * @author abailey
 */
public interface CPU6502ViewInterface {

    void refreshFromCPU(CPU6502 cpu);
    /*
    // flag accessors
    byte getFlags();
    void setFlags(byte f);
        
    void setNegativeFlag(boolean flag);
    boolean getNegativeFlag();

    void setZeroFlag(boolean flag);
    boolean getZeroFlag();

    void setOverflowFlag(boolean flag);
    boolean getOverflowFlag();
    
    void setCarryFlag(boolean flag);
    boolean getCarryFlag();

    void setInterruptFlag(boolean flag);
    boolean getInterruptFlag();

    void setBreakFlag(boolean flag);
    boolean getBreakFlag();
    
    void setDecimalFlag(boolean flag);
    boolean getDecimalFlag();
    
    // register accessors
    void setAccumulator(byte b);
    byte getAccumulator();

    void setXRegister(byte b);
    byte getXRegister();
    
    void setYRegister(byte b);
    byte getYRegister();
    
    // CPU state accessors
    void setProgramCounter(int pc);
    int getProgramCounter();
    
    // stack stuff
    byte getStackPointer();
    void setStackPointer(byte b);
*/
    
}
