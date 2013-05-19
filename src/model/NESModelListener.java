/*
 * NESModelListener.java
 *
 * Created on October 5, 2006, 11:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package model;

/**
 *
 * @author abailey
 */
public interface NESModelListener extends NESPaletteListener {
    
    public final static int UNKNOWN_CHANGE_TYPE = 0;
    public final static int PALETTE_CHANGE_TYPE = 1;
    public final static int PATTERN_TABLE_CHANGE_TYPE = 2;        
    
    void notifyPatternTableChanged();
    
    void notifyNameTableChanged();
    
    
    void notifyPatternTableSelected(int pageNum, int index);
    void notifyPatternTableToBeModified(int pageNum, int index); // the pattern table at this page and index should be replaced with the listeners editor tile
            
    
}
