/*
 * NESDebuggerInterface.java
 *
 * Created on January 6, 2009, 8:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.debugger;

/**
 *
 * @author abailey
 */
public interface NESDebuggerInterface {
    void pause();
//    void step();
    void resume();    
    
    void stepCPUInstruction();
    void stepCPUCycle();
    void startCPUCapture();
    void endCPUCapture();
    void addCPUBreakpoint(int programCounter);
    void removeCPUBreakpoint(int programCounter);

    void addOpcodeBreakpoint(byte opcode);
    void removeOpcodeBreakpoint(byte opcode);

    // ppu triggers
    void stepNextFrame();
    void stepNextScanline();
    //void stepStatusChange();

    // mem watches
    void addWatch(int address, boolean readMode, boolean writeMode);
    void removeWatch(int address); 
    // need to add PPU ones too
}
