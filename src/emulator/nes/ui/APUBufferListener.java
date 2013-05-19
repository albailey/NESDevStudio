/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.ui;

/**
 *
 * @author abailey
 */
public interface APUBufferListener {

    void notifyBufferUpdates(byte soundBuffer[], int offset, int updateSize);

}
