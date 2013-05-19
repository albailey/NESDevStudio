/*
 * ActiveStampObserver.java
 *
 * Created on September 14, 2008, 9:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampSetEditor;

import ui.chr.tileEditor.CHRMultiTilePanel;

/**
 *
 * @author abailey
 */
public interface ActiveStampObserver {
     void setActiveStamp(CHRMultiTilePanel stamp, int bank);
     void stampChanged(CHRMultiTilePanel stamp);
     
}
