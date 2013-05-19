/*
 * PaletteClickListener.java
 *
 * Created on August 23, 2008, 9:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.palette;

/**
 *
 * @author abailey
 */
public interface PaletteClickListener {
    void processPaletteClick(int componentIndex, int panelSet, boolean wasRightClicked);
}
