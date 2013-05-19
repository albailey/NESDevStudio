/*
 * GridMouseResultsType.java
 *
 * Created on January 25, 2007, 4:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.input;

/**
 *
 * @author abailey
 */
public interface GridMouseResultsType {
    
    GridMouseResultsModel getGridMouseResults();
    
    void moveGridData(GridMouseResultsModel results);
    
    boolean importGridData(GridMouseResultsModel results);
}
