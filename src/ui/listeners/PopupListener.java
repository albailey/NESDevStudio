/*
 * PopupListener.java
 *
 * Created on August 14, 2008, 9:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.listeners;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 *
 * @author abailey
 */
public class PopupListener extends MouseAdapter {
    private JPopupMenu _popup = null;
    public Component popupTrigger = null;
    
    /**
     * Creates a new instance of PopupListener
     */
    public PopupListener(JPopupMenu popup) {
        _popup = popup;
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {            
            popupTrigger = e.getComponent();
            _popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
