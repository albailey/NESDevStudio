/*
 * GridMouseConstraintsModel.java
 *
 * Created on January 21, 2007, 3:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.input;

import ui.NES_UI_Constants;

/**
 *
 * @author abailey
 */
public class GridMouseConstraintsModel {
    
    
    int minWidth;
    int minHeight;
    int maxWidth;
    int maxHeight;
    int gridPieceWidth;
    int gridPieceHeight;
    int numGridX; // numGridX * gridPieceWidth should equal maxWidth-minWidth
    int numGridY; // numGridY * gridPieceHeight should equal maxHeight-minHeight
    
    
    public GridMouseConstraintsModel(int minX, int minY, int maxX, int maxY, int gridX, int gridY, int numX, int numY)  {
        minWidth = minX;
        minHeight = minY;
        maxWidth = maxX;
        maxHeight = maxY;
        gridPieceWidth = gridX;
        gridPieceHeight = gridY;
        numGridX = numX;
        numGridY = numY;
    }
    
    public int getGridXIndexFromPosition(int x, int y){
        if (x < minWidth || x > maxWidth){
            // not in our range
            return NES_UI_Constants.INVALID_VALUE;
        }
        int relX = x / gridPieceWidth;
        return relX;
    }
    public int getPositionFromGridXIndex(int relX){
    	return relX * gridPieceWidth;
    }
    public int getPositionFromGridYIndex(int relY){
    	return relY * gridPieceHeight;
    }
    
    public int getGridYIndexFromPosition(int x, int y){
        if (y < minHeight || y > maxHeight){
            // not in our range
            return NES_UI_Constants.INVALID_VALUE;
        }
        int relY = y / gridPieceHeight;
        return relY;
    }
    
    public int getArrayIndexFromIndices(int relX, int relY){
        if(relX < 0 || relX > numGridX) {
            return NES_UI_Constants.INVALID_VALUE;
        }
        if(relY < 0 || relY > numGridY) {
            return NES_UI_Constants.INVALID_VALUE;
        }
        int index = (relY * numGridX) + relX;
        return index;
    }
    
    public int getArrayIndexFromPosition(int x, int y){
        int relX = getGridXIndexFromPosition(x,y);
        if(relX < 0 || relX > numGridX) {
            return NES_UI_Constants.INVALID_VALUE;
        }
        int relY = getGridYIndexFromPosition(x,y);
        if(relY < 0 || relY > numGridY) {
            return NES_UI_Constants.INVALID_VALUE;
        }
        return getArrayIndexFromIndices(relX, relY);
    }
    
}
