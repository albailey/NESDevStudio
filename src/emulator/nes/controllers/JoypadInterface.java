/*
 * JoypadInterface.java
 *
 * Created on January 7, 2009, 10:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.controllers;

import emulator.nes.IOMappedMemory;

/**
 *
 * @author abailey
 */
public interface JoypadInterface extends IOMappedMemory  {
    public final static int A_BUTTON_INDEX = 0;
    public final static int B_BUTTON_INDEX = 1;
    public final static int SELECT_BUTTON_INDEX = 2;
    public final static int START_BUTTON_INDEX = 3;
    public final static int UP_ARROW_INDEX = 4;
    public final static int DOWN_ARROW_INDEX = 5;
    public final static int LEFT_ARROW_INDEX = 6;
    public final static int RIGHT_ARROW_INDEX = 7;
    public String PAD_NAMES[] = { "A","B","SELECT","START","UP","DOWN","LEFT","RIGHT"};

    // returns true if this code is used by the joypad
    boolean processKeyEvent(int code, boolean isPressed);
    
   void setRegister4016(byte val)  ;
   byte getRegister4016();
   byte getRegister4017();
   
}
