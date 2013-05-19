/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

/**
 *
 * @author abailey
 */
public interface PageBoundaryObserver {
    void notifyPageCrossed();
}
