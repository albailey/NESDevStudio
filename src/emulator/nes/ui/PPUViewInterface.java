/*
 * PPUViewInterface.java
 *
 * Created on January 6, 2009, 6:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.ui;

import emulator.nes.PPU;

/**
 *
 * @author abailey
 */
public interface PPUViewInterface {
    void refreshFromPPU(PPU ppu);
}
