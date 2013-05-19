/*
 * GridMouseInputAndDragAdapter.java
 *
 * Created on January 28, 2007, 5:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ui.input.dndOLD;

import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.TooManyListenersException;

import javax.swing.TransferHandler;
import javax.swing.event.MouseInputAdapter;

import ui.input.GridMouseResultsModel;

/**
 *
 * @author abailey
 */
public class GridMouseInputAndDragAdapter extends MouseInputAdapter {

    public static final int MIN_DRAG_THRESHOLD = 5;
    public static final int maxMouseEvent = MouseEvent.BUTTON3 + 1;
    private static final int minValue = MouseEvent.BUTTON1;
    private static final int maxValue = MouseEvent.BUTTON3;
    private GridMouseResultsModel resultsModel;
    private MouseEvent firstPressEvents[];
    private MouseEvent lastDragEvents[];
    private MouseActionCallback pressCallbacks[];
    private MouseActionCallback clickCallbacks[];
    private MouseActionCallback releaseCallbacks[];
    private MouseDragCallback dragCallback;
    private MouseMoveCallback moveCallback;
    private CustomDropTarget cdt = null;
    public int dragMode = TransferHandler.NONE;

    public GridMouseInputAndDragAdapter(GridMouseResultsModel resultsModel) {
        this.resultsModel = resultsModel;
        firstPressEvents = new MouseEvent[maxMouseEvent]; // all initially null
        lastDragEvents = new MouseEvent[maxMouseEvent]; // all initially null
        clickCallbacks = new MouseActionCallback[maxMouseEvent];
        pressCallbacks = new MouseActionCallback[maxMouseEvent];
        releaseCallbacks = new MouseActionCallback[maxMouseEvent];
    }

    public void clear() {
        dragCallback = null;
        moveCallback = null;
        for (int i = 0; i < firstPressEvents.length; i++) {
            firstPressEvents[i] = null;
            pressCallbacks[i] = null;
            clickCallbacks[i] = null;
            releaseCallbacks[i] = null;
            lastDragEvents[i] = null;
        }
    }

    public MouseEvent getDropPosition(int buttonCode) {
        analyzeCode("getDropPosition", buttonCode);
        return lastDragEvents[buttonCode];
    }

    public void assignMousePressedCallback(int buttonCode, MouseActionCallback cb) {

        pressCallbacks[buttonCode] = cb;
    }

    public void assignMouseReleasedCallback(int buttonCode, MouseActionCallback cb) {
        analyzeCode("assignMouseReleasedCallback", buttonCode);
        releaseCallbacks[buttonCode] = cb;
    }

    public void assignMouseClickedCallback(int buttonCode, MouseActionCallback cb) {
        analyzeCode("assignMouseClickedCallback", buttonCode);
        clickCallbacks[buttonCode] = cb;
    }

    public void assignMouseDraggedCallback(MouseDragCallback cb) {
        dragCallback = cb;
    }

    public void assignMouseMovedCallback(MouseMoveCallback cb) {
        moveCallback = cb;
    }

    public void clearPressEvent(int buttonCode) {
        analyzeCode("clearPressEvent", buttonCode);
        firstPressEvents[buttonCode] = null;
    }

    public void mousePressed(MouseEvent e) {
        int buttonCode = e.getButton();
        firstPressEvents[buttonCode] = e;
        if (pressCallbacks[buttonCode] != null) {
            pressCallbacks[buttonCode].doCallback(e, resultsModel);
        }
    }

    public void mouseReleased(MouseEvent e) {
        int buttonCode = e.getButton();
        if (releaseCallbacks[buttonCode] != null) {
            releaseCallbacks[buttonCode].doCallback(e, resultsModel);
        }
        firstPressEvents[buttonCode] = null;
    }

    public void mouseMoved(MouseEvent e) {
        if (moveCallback != null) {
            moveCallback.doMoveCallback(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        int buttonCode = e.getButton();
        if (clickCallbacks[buttonCode] != null) {
            clickCallbacks[buttonCode].doCallback(e, resultsModel);
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (dragCallback != null) {
            int mask = e.getModifiers();
            int index = 0;
            if ((mask & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                index = MouseEvent.BUTTON1;
            } else if ((mask & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
                index = MouseEvent.BUTTON2;
            } else if ((mask & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                index = MouseEvent.BUTTON3;
            }
            lastDragEvents[index] = e;
            // ignore small frag amounts
            if (firstPressEvents[index] == null) {
                return;
            }
            int dx = Math.abs(e.getX() - firstPressEvents[index].getX());
            int dy = Math.abs(e.getY() - firstPressEvents[index].getY());
            if (dx > MIN_DRAG_THRESHOLD || dy > MIN_DRAG_THRESHOLD) {
                //Classify it as a drag
                dragCallback.doDragCallback(firstPressEvents[index], e, resultsModel);
            }
        }
    }

    private void analyzeCode(String title, int buttonCode) {
        if (buttonCode < minValue || buttonCode > maxValue) {
            throw new IndexOutOfBoundsException(title + " value: " + buttonCode + " not in the index: [" + minValue + "," + maxValue + "]");
        }
    }

    public Point getDropPoint() {
        if (cdt == null) {
            return null;
        } else {
            return cdt.lastPos;
        }
    }

    public void refreshCTD(int mode, DropTarget dt) {
        dragMode = mode;
        try {
            if (cdt == null && dt != null) {
                //System.out.println(dt);
                cdt = new CustomDropTarget();
                dt.addDropTargetListener(cdt);
            }
        } catch (TooManyListenersException tmle) {
            tmle.printStackTrace();
        }
    }

    class CustomDropTarget extends DropTargetAdapter {

        Point lastPos = null;

        public void dragEnter(DropTargetDragEvent e) {
            lastPos = e.getLocation();
        }

        public void dropActionChanged(DropTargetDragEvent e) {
            lastPos = e.getLocation();
        }

        public void dragExit(DropTargetEvent e) {
            lastPos = null;
        }

        public void dragOver(DropTargetDragEvent e) {
            lastPos = e.getLocation();
        }

        public void drop(DropTargetDropEvent e) {
            lastPos = e.getLocation();
        // System.out.println("Dropping at:" + lastPos);
        }
    }
}

