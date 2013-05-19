/*
 * GridMouseResultsModel.java
 *
 * Created on January 21, 2007, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.input;

import model.CHRModel;
import ui.NES_UI_Constants;

/**
 *
 * @author abailey
 */
public class GridMouseResultsModel {
    int minX;
    int minY;
    int maxX;
    int maxY;
    
    
    private int xIndex;
    private int yIndex;
    private int index;
    
    public int startX;
    public int startY;
    public int endX;
    public int endY;
    
    public int pageNum;
    
    public CHRModel modelRef;
    
      public GridMouseResultsModel(int minX, int minY, int maxX, int maxY, CHRModel modelRef){
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.modelRef = modelRef;
        
        xIndex = NES_UI_Constants.INVALID_VALUE;
        yIndex = NES_UI_Constants.INVALID_VALUE;
        index = NES_UI_Constants.INVALID_VALUE;
        
        startX = NES_UI_Constants.INVALID_VALUE;
        startY = NES_UI_Constants.INVALID_VALUE;
        endX = NES_UI_Constants.INVALID_VALUE;
        endY = NES_UI_Constants.INVALID_VALUE;
        pageNum = NES_UI_Constants.INVALID_VALUE;
    }
    public int getStartX() {
    	return (startX < endX) ? startX : endX;
    }
    public int getStartY() {
    	return (startY < endY) ? startY : endY;
    }
    public int getWidth() {
    	return Math.abs(endX-startX);
    }
    public int getHeight() {
    	return Math.abs(endY-startY);
    }
        
    public String toString(){
        StringBuffer sb = new StringBuffer("GridMouseResultsModel:");
        sb.append("Box: [");
        sb.append(startX);
        sb.append(",");
        sb.append(startY);
        sb.append("] [");
        sb.append(endX);
        sb.append(",");
        sb.append(endY);
        sb.append("]");
        sb.append("Index: ");
        sb.append(index);
        sb.append(" [");
        sb.append(xIndex);
        sb.append(",");
        sb.append(yIndex);
        sb.append("]"  );
        return sb.toString();
    }
    public void assignIndexX(int val){
        xIndex = val;        
    }
    public int getXIndex(){
        return xIndex;
    }
    public void assignIndexY(int val){
        yIndex = val;        
    }
    public int getYIndex(){
        return yIndex;
    }
    public void assignIndex(int val){
        index = val;        
    }
    public int getIndex(){
        return index;
    }
    
    public void assignPatternPage(int page){
        pageNum = page;
    }
    
    public boolean assignBoxX(int x){
        boolean ret = false;
        if(x >= minX && x <= maxX){
        	if(startX == NES_UI_Constants.INVALID_VALUE){
                startX = x;
                ret = true;
            }
        	if(endX != x){
        		ret = true;
        	}
            endX = x;
        }
        return ret;
    }
    public boolean assignBoxY(int y){
        boolean ret = false;
        if(y >= minY && y <= maxY){
            if(startY == NES_UI_Constants.INVALID_VALUE){
                startY = y;
                ret = true;
            }
        	if(endY != y){
        		ret = true;
        	}
            endY = y;
        }
        return ret;
    }
   
    public boolean isBoxValid(){
        return ( ! (startX == NES_UI_Constants.INVALID_VALUE 
                || startY  == NES_UI_Constants.INVALID_VALUE 
                || endX  == NES_UI_Constants.INVALID_VALUE 
                || endY  == NES_UI_Constants.INVALID_VALUE 
                || pageNum  == NES_UI_Constants.INVALID_VALUE));
    }
    
    public void resetBox(){
        startX = NES_UI_Constants.INVALID_VALUE;
        startY = NES_UI_Constants.INVALID_VALUE;
        endX = NES_UI_Constants.INVALID_VALUE;
        endY = NES_UI_Constants.INVALID_VALUE;
        pageNum = NES_UI_Constants.INVALID_VALUE;
        
    } 
}
