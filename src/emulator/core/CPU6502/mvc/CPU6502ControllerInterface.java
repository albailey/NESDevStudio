/*
 * CPU6502ControllerInterface.java
 *
 * Created on December 14, 2006, 9:11 AM
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
public interface CPU6502ControllerInterface {
    
    
    public final static int MODEL_CHANGED = 0;
    public final static int MODEL_FLAGS_CHANGED = 1;
    public final static int MODEL_REGISTERS_CHANGED = 2;
    public final static int MODEL_STATES_CHANGED = 3;
    
    boolean addCPU6502View(CPU6502ViewInterface view);
    boolean removeCPU6502View(CPU6502ViewInterface view);
    
    boolean setCPU6502(CPU6502 cpu);
    CPU6502 getCPU6502();
    
    void notifyCPUModelChanged(int changeType);   
    void notifyCPUMemoryChanged();   
    
}
