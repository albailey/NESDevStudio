/*
 * NESPaletteListener.java
 *
 * Created on November 22, 2007, 5:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package model;

/**
 *
 * @author abailey
 */
public interface NESPaletteListener {
    void notifyImagePaletteChanged();
    void notifySpritePaletteChanged();

}
