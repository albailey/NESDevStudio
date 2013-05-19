/*
 * MouseDragCallback.java
 *
 * Created on January 28, 2007, 9:58 PM
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
public interface MouseDragCallback {
     void doDragCallback(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel resultsModel);
     
}
