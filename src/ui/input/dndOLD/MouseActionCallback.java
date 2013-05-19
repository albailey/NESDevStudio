/*
 * MouseActionCallback.java
 *
 * Created on January 28, 2007, 5:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.input.dndOLD;

import java.awt.event.MouseEvent;

import ui.input.GridMouseResultsModel;

/**
 *
 * @author abailey
 */
public interface MouseActionCallback {
    
    void doCallback(MouseEvent e, GridMouseResultsModel resultsModel);
    
}
