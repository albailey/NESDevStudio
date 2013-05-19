/*
 * GridMouseAdapterCallback.java
 *
 * Created on January 21, 2007, 3:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.input.dndOLD;

import ui.input.GridMouseConstraintsModel;
import ui.input.GridMouseResultsModel;

/**
 *
 * @author abailey
 */
public interface GridMouseAdapterCallback {
    void doCallback(GridMouseConstraintsModel model, GridMouseResultsModel results);     
}
