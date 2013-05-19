/*
 * GridMouseResultsModelTransferable.java
 *
 * Created on January 25, 2007, 4:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.input.dndOLD;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import ui.input.GridMouseResultsModel;
import ui.input.GridMouseResultsType;

/**
 *
 * @author abailey
 */
public class GridMouseResultsModelTransferable implements Transferable {
    
    static DataFlavor localFlavor = DataFlavor.stringFlavor;
    static DataFlavor serialFlavor= DataFlavor.stringFlavor;
    static String localType = DataFlavor.javaJVMLocalObjectMimeType + ";class=ui.input.GridMouseResultsModel";
    public static final DataFlavor flavors[] = new DataFlavor[2];
    
    static {
        serialFlavor = new DataFlavor(GridMouseResultsModel.class,"GridMouseResultsModel");
        try {
            localFlavor = new DataFlavor(localType);
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to create data flavor:" + localType);
            localFlavor = serialFlavor;
        }
        flavors[0] = localFlavor;
        flavors[1] = serialFlavor;   
    }
    
    GridMouseResultsModel results;
    
    /** Creates a new instance of GridMouseResultsModelTransferable */
    public GridMouseResultsModelTransferable(GridMouseResultsType resultsComp) {
        this.results = resultsComp.getGridMouseResults();
    }
    
    public GridMouseResultsModel getGridResults(){
        return results;
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return results;
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { localFlavor, serialFlavor };
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor) {
       for (int j = 0; j < GridMouseResultsModelTransferable.flavors.length; j++) {
            if (flavor.equals(GridMouseResultsModelTransferable.flavors[j])) {
                return true;
            }
        }
        return false;
    }
}

