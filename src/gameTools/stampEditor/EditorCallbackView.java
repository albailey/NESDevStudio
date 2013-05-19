/*
 * EditorCallbackView.java
 *
 * Created on August 15, 2008, 11:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampEditor;

/**
 *
 * @author abailey
 */
public interface EditorCallbackView {
    void processClear();
   void processClick(int tileIndex, int pixelIndex);
   void processDrag(int sx, int sy, int ex, int ey);
   void updateVisual(int sx, int sy, int ex, int ey);

   
}
