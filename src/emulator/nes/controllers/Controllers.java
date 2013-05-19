/*
 * Controllers.java
 *
 * Created on January 7, 2009, 10:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes.controllers;

import utilities.ByteFormatter;

/**
 *
 * @author abailey
 */
public class Controllers implements JoypadInterface {

//    private int keycodes[] = { 'Z','X','C','V','UP','DOWN','LEFT','RIGHT'};
    private int keycodes1[] = {90, 88, 67, 86, 38, 40, 37, 39}; // these should be configurable
    private int keycodes2[] = {89, 87, 66, 85, 1, 2, 3, 4}; // these should be configurable
    private StandardController joypad1 = null;
    private StandardController joypad2 = null;
    private boolean strobeMode = false;

    /** Creates a new instance of Joypad */
    public Controllers() {
        joypad1 = new StandardController(keycodes1);
        joypad2 = new StandardController(keycodes2);
    }

    public boolean processKeyEvent(int keyCode, boolean isPressed) {
        return (joypad1.processKeyEvent(keyCode, isPressed) || joypad2.processKeyEvent(keyCode, isPressed));
    }

  

    public boolean isReadMapped(int address) {
        return (address == 0x4016 || address == 0x4017);
    }

    public boolean isWriteMapped(int address) {
        return (address == 0x4016);
    }

    // therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
    public byte getMappedMemory(int address) {
        if (address == 0x4016) {
            return getRegister4016();
        } else if (address == 0x4017) {
            return getRegister4017();
        } else {
            System.err.println("Error. Joypad not legal to query address 0x" + ByteFormatter.formatInt(address));
            return (byte) 0xFF;
        }
    }
 // therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
    public byte getMappedMemoryDirect(int address) {
        return (byte) 0xFF;
    }

    public void setMappedMemory(int address, byte val) {
        if (address == 0x4016) {
            setRegister4016(val);
        } else {
            System.err.println("Error. Joypad not legal to set address 0x" + ByteFormatter.formatInt(address) + " to val: " + ByteFormatter.formatByte(val));
        }
    }

    public void setRegister4016(byte val) {
        if ((val & 0x1) == 0x1) {
            strobeMode = true;
        }
        if ((val & 0x1) == 0x0) {
            if (strobeMode == true) {
                joypad1.strobe();
                joypad2.strobe();
            }
            strobeMode = false;
            joypad1.clearStrobe();
            joypad2.clearStrobe();
        }
    }

    public byte getRegister4016() {
        return joypad1.getByte();
    }

    public byte getRegister4017() {
        return joypad2.getByte();
    }
}   
