/*
 * DisassemblerUIInterface.java
 *
 * Created on December 14, 2006, 1:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.romLoader.disassembler;

/**
 *
 * @author abailey
 */
public interface DisassemblerUIInterface {
    
    public final static int UNDEFINED_DISASSEMBLER_ACTION = 0;    
    public final static int DISASSEMBLER_CONTINUE_ACTION = 1;
    public final static int DISASSEMBLER_STOP_ACTION = 2;
    public final static int DISASSEMBLER_SEEK_ACTION = 3;
    
    void pause();
    
    int getNextUserAction();
    
    int getSeekAddress();
    int[] getSpecialAddresses();
     
}
