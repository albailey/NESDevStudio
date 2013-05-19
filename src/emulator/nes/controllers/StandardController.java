/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emulator.nes.controllers;

/**
 *
 * @author abailey
 */
public class StandardController {

    private final static boolean JOYPAD_DEV_MODE = false;
    public final static int MAX_STATE = 8;
    private byte ON_STATE = (byte) 0x41;
    private byte OFF_STATE = (byte) 0x40;
    private byte states[] = {0, 0, 0, 0, 0, 0, 0, 0};
    private byte activeStates[] = {0, 0, 0, 0, 0, 0, 0, 0};
    private int keycodes[] = new int[8];
    private int internalStrobe = MAX_STATE;

    public StandardController(int codes[]) {
        System.arraycopy(codes, 0, keycodes, 0, MAX_STATE);
        clear();
    }

    private void clear() {
        for (int i = 0; i < MAX_STATE; i++) {
            activeStates[i] = OFF_STATE;
            states[i] = OFF_STATE;
        }
    }

    public boolean processKeyEvent(int keyCode, boolean isPressed) {
        for (int i = 0; i < keycodes.length; i++) {
            if (keycodes[i] == keyCode) {
                activeStates[i] = (isPressed) ? ON_STATE : OFF_STATE;
                if (JOYPAD_DEV_MODE) {
                    System.out.println(JoypadInterface.PAD_NAMES[i] + ((isPressed) ? "PRESSED" : "RELEASED"));
                }
                return true;
            }
        }
        return false;
    }

    public void strobe() {
        System.arraycopy(activeStates, 0, states, 0, MAX_STATE);
    }

    public void clearStrobe() {
        internalStrobe = 0;
    }

    public byte getByte() {
        if (internalStrobe >= MAX_STATE) {
            return OFF_STATE;
        }
        byte val = states[internalStrobe];
        internalStrobe++;
        return val;

    }
}
