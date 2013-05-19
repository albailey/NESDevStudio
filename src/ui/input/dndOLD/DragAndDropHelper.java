/*
 * DragAndDropHelper.java
 *
 * Created on January 31, 2007, 4:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.input.dndOLD;

import java.awt.Component;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

/**
 *
 * @author abailey
 */
public class DragAndDropHelper implements DropTargetListener{
    
    /** Creates a new instance of DragAndDropHelper */
    public DragAndDropHelper() {
    }

    public void addDropTarget(Component c){
        new DropTarget(c,this);
    }
    
 // Drop Target
    public void dragEnter(DropTargetDragEvent evt) {
        System.out.println("dragEnter:" + evt);
            // Called when the user is dragging and enters this drop target.
        }
        public void dragOver(DropTargetDragEvent evt) {
        System.out.println("dragOver:" + evt);
            // Called when the user is dragging and moves over this drop target.
        }
        public void dragExit(DropTargetEvent evt) {
            System.out.println("dragExit:" + evt);
            // Called when the user is dragging and leaves this drop target.
        }
        public void dropActionChanged(DropTargetDragEvent evt) {
            System.out.println("dropActionChanged:" + evt);
            // Called when the user changes the drag action between copy or move.
        }
        public void drop(DropTargetDropEvent evt) {
            System.out.println("drop:" + evt);
            // Called when the user finishes or cancels the drag operation.
        }    
}
