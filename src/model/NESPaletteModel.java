/*
 * NESPaletteModel.java
 *
 * Created on November 22, 2007, 5:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package model;

/**
 *
 * @author abailey
 */
public interface NESPaletteModel {
    void updateImagePaletteIndex(int index, byte val);
    void updateSpritePaletteIndex(int index, byte val);
    byte getImagePaletteAtIndex(int index);
    byte getSpritePaletteAtIndex(int index);
}
