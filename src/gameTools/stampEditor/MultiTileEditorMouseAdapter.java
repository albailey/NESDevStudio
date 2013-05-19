/*
 * MultiTileEditorMouseAdapter.java
 *
 * Created on August 15, 2008, 11:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampEditor;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author abailey
 */
public class MultiTileEditorMouseAdapter  extends MouseInputAdapter {
    private StampSettings _settings;
    private EditorCallbackView _callback;
    private MouseEvent startingEvent = null;
    private Point startingPoint = null;
    private Point currentPoint = null;
    private int buttonDragType = MouseEvent.BUTTON1_DOWN_MASK;
    
    public MultiTileEditorMouseAdapter(StampSettings settings, EditorCallbackView callback, int activeButton){
        _settings = settings;
        _callback = callback;
        switch(activeButton){
            case MouseEvent.BUTTON2:
                buttonDragType =  MouseEvent.BUTTON2_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                buttonDragType =  MouseEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                buttonDragType =  MouseEvent.BUTTON1_DOWN_MASK;
                break;
        }
    }
    
    public void mouseEntered(MouseEvent e) {
        clearSettings();
    }
    public void mouseExited(MouseEvent e) {
        clearSettings();
    }
    public void mousePressed(MouseEvent e) {
        if(isHandlingDown(e.getModifiersEx())){
            initSettings(e);
        }
    }
    public void mouseReleased(MouseEvent e) {
        if(isHandlingUp(e.getModifiersEx())){
            processSettings(e);
            clearSettings();
        }
    }
    
    
    public void mouseDragged(MouseEvent e) {
        if(isHandlingDown(e.getModifiersEx())){
            updateSettings(e);
        }
    }
    
    
    
    private boolean isHandlingDown(int maskToCheck){
        return((maskToCheck & buttonDragType) == buttonDragType) ;
    }
    private boolean isHandlingUp(int maskToCheck){
        return((maskToCheck & buttonDragType) != buttonDragType) ; // make sure we no longer have that button masked
    }
    
    private void initSettings(MouseEvent e){
        Point p = getNormalizedLocationFromScaledLocation(e.getX(), e.getY());
        if(p == null){ // not being tracked
            return;
        }
        p = getTileLocationFromNormalizedLocation(p.x, p.y); // now get it in terms of the tile, and its index
        if(startingPoint == null){
            startingPoint = p;
        }
        if(startingEvent == null){
            startingEvent = e;
        }
        
    }
    private void clearSettings(){
        startingEvent = null;
        startingPoint = null;
        currentPoint = null;
        _callback.processClear();
    }
    
    private void updateVisual(int sx, int sy, int cx, int cy){
        _callback.updateVisual(sx,sy,cx,cy);
    }
    
    private void reallyProcess(int sx, int sy, int cx, int cy){
        if(startingPoint == null || currentPoint == null){
            // out of bounds.
            return;
        }
        //    System.out.println("[" +sx +"," + sy + "] [" + cx + "," + cy + "]");
        if(startingPoint.x == currentPoint.x && startingPoint.y == currentPoint.y){
            _callback.processClick(startingPoint.x,startingPoint.y);
        } else {
            _callback.processDrag(sx, sy, cx, cy);
        }
    }
    
    private void updateSettings(MouseEvent e){
        if(startingEvent == null){
            initSettings(e);
        } else {
            Point p = getNormalizedLocationFromScaledLocation(e.getX(), e.getY());
            if(p == null){ // not being tracked
                return;
            }
            p = getTileLocationFromNormalizedLocation(p.x, p.y); // now get it in terms of the tile, and its index
            if(startingPoint == null){
                startingPoint = p;
                updateVisual(startingEvent.getX(),startingEvent.getY(),e.getX(),e.getY());
            } else if(currentPoint == null){
                currentPoint = p;
                updateVisual(startingEvent.getX(),startingEvent.getY(),e.getX(),e.getY());
            } else if(p.x != currentPoint.x || p.y != currentPoint.y){
                currentPoint = p;
                updateVisual(startingEvent.getX(),startingEvent.getY(),e.getX(),e.getY());
            } else {
                // do nothing
            }
        }
    }
    private void processSettings(MouseEvent e){
        if(startingEvent != null){
            updateSettings(e);
            reallyProcess(startingEvent.getX(),startingEvent.getY(),e.getX(),e.getY());
        }
    }
    
    private Point getNormalizedLocationFromScaledLocation(int xPos, int yPos){
        return getNormalizedLocationFromScaledLocation(xPos, yPos, _settings.getBrickSizeX(), _settings.getBrickSizeY(),  _settings.getNumBricksX(),  _settings.getNumBricksY(), _settings.getScaleX(), _settings.getScaleY());
    }
    
    private Point getTileLocationFromNormalizedLocation(int normX, int normY){
        return getTileLocationFromNormalizedLocation(normX, normY,  _settings.getBrickSizeX(), _settings.getBrickSizeY(), _settings.getNumBricksX(),  _settings.getNumBricksY());
    }
    
    public static Point getTilePointFromXY(int px, int py, StampSettings settings ){
        Point p = getNormalizedLocationFromScaledLocation(px, py, settings.getBrickSizeX(), settings.getBrickSizeY(),  settings.getNumBricksX(),  settings.getNumBricksY(), settings.getScaleX(), settings.getScaleY());
        if(p == null){ // not being tracked
            return null;
        }
        return getTileLocationFromNormalizedLocation(p.x, p.y, settings.getBrickSizeX(), settings.getBrickSizeY(), settings.getNumBricksX(),  settings.getNumBricksY());
    }
    
    public static Point getNormalizedLocationFromScaledLocation(int xPos, int yPos, int brickX, int brickY, int numX, int numY, int scaleX, int scaleY){
        if(xPos < 0 || yPos < 0){
            return null;
        }
        if(brickX <= 0 || brickY <= 0){
            return null;
        }
        if(scaleX <= 0 || scaleY <= 0){
            return null;
        }
        if(numX <= 0 || numY <= 0){
            return null;
        }
        
        int maxX = numX * brickX * scaleX;
        if( xPos > maxX ){
            return null;
        }
        
        int maxY = numY * brickY * scaleY;
        if( yPos > maxY ){
            return null;
        }
        return new Point(xPos / scaleX, yPos / scaleY);
    }
    public static Point getTileLocationFromNormalizedLocation(int normX, int normY, int brickX, int brickY, int numX, int numY){
        int tx = (normX / brickX) % numX;
        int ty = (normY / brickY) % numY;        
        int tileIndex = ( ty *  numX ) + tx;
        int pixelIndex  = ((normY % brickY ) *  brickX) + (normX %  brickX);
        return new Point(tileIndex, pixelIndex);
    }
    
}

