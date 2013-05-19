/*
 * GridMouseResultsTransferHandler.java
 *
 * Created on January 25, 2007, 2:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ui.input;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import ui.input.dndOLD.GridMouseResultsModelTransferable;

/**
 *
 * @author abailey
 */
public class GridMouseResultsTransferHandler extends TransferHandler { // implements Transferable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3693633656109234752L;

	/**
     * Creates a new instance of GridMouseResultsTransferHandler
     */
    public GridMouseResultsTransferHandler() {
    }

    public boolean canImport(JComponent receiverComp, DataFlavor flavor[]) {
        if (receiverComp instanceof GridMouseResultsType) {
            return canGridMouseResultsTypeImport(flavor);
        }
        // get here, we return false
        return false;
    }

    protected void exportDone(JComponent c, Transferable data, int action) {
        if (action == TransferHandler.MOVE) {
            if (data instanceof GridMouseResultsModelTransferable) {
                GridMouseResultsModelTransferable tr = (GridMouseResultsModelTransferable) data;
                if (c instanceof GridMouseResultsType) {
                    GridMouseResultsType source = (GridMouseResultsType) c;
                    source.moveGridData(tr.getGridResults());
                }
            } else {
                System.out.println("I dont support this:" + data);
            }
        }
    }

    public boolean importData(JComponent receiverComp, Transferable t) {
        if (canImport(receiverComp, t.getTransferDataFlavors())) {
            try {
                for (int i = 0; i < GridMouseResultsModelTransferable.flavors.length; i++) {
                    if (t.isDataFlavorSupported(GridMouseResultsModelTransferable.flavors[i])) {
                        if (importGridData(receiverComp, (GridMouseResultsModel) t.getTransferData(GridMouseResultsModelTransferable.flavors[i]))) {
                            return true;
                        }
                    }
                }
            } catch (UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return false;
    }

    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof GridMouseResultsType) {
            GridMouseResultsType source = (GridMouseResultsType) c;
            return new GridMouseResultsModelTransferable(source);
        }
        return null;
    }

    private boolean importGridData(JComponent receiverComp, GridMouseResultsModel transferData) {
        if (receiverComp instanceof GridMouseResultsType) {
            GridMouseResultsType source = (GridMouseResultsType) receiverComp;
            return source.importGridData(transferData);
        }
        return false;
    }

    private boolean canGridMouseResultsTypeImport(DataFlavor flavor[]) {
        for (int i = 0, n = flavor.length; i < n; i++) {
            for (int j = 0; j < GridMouseResultsModelTransferable.flavors.length; j++) {
                if (flavor[i].equals(GridMouseResultsModelTransferable.flavors[j])) {
                    return true;
                }
            }
        }
        return false;
    }
}
