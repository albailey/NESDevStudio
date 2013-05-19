/*
 * ImagePanel.java
 *
 * Created on October 10, 2006, 3:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.imageHelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;
import javax.swing.TransferHandler;

import ui.chr.PPUConstants;
import ui.input.GridMouseResultsModel;
import ui.input.dndOLD.GridMouseInputAndDragAdapter;
import ui.input.dndOLD.MouseActionCallback;
import ui.input.dndOLD.MouseDragCallback;

/**
 *
 * @author abailey
 */
public class ImagePanel extends JPanel {
    
	private static final long serialVersionUID = -7430901028159263847L;
	private MemoryImageSource mis = null;
    private Image misImg = null;
    private int wid;
    private int hgt;
    boolean dirtyFlag = false;
    int minX ;
    int minY ;
    int maxX ;
    int maxY ;
    int scaleFactor;
    private GridMouseInputAndDragAdapter dragAdapter = null;
    private GridMouseResultsModel resultsModel = null;
    
    /** Creates a new instance of ImagePanel */
    public ImagePanel(MemoryImageSource imgMis, int newWid, int newHgt, boolean supportsRegion, GridMouseResultsModel saveRegion) {
        this(imgMis, newWid, newHgt, supportsRegion, saveRegion, 1);
    }
    public ImagePanel(MemoryImageSource imgMis, int newWid, int newHgt, boolean supportsRegion, GridMouseResultsModel saveRegion, int scale) {
        mis = imgMis;
        wid = newWid;
        hgt = newHgt;
        scaleFactor = scale;
        dirtyFlag = true;
        setBackground(PPUConstants.NES_PALETTE[0]);
        setMinimumSize(new Dimension(wid,hgt));
        setPreferredSize(new Dimension(wid*scaleFactor,hgt*scaleFactor));
        mis.setAnimated(true);
        resultsModel = saveRegion;
        if(supportsRegion) {
            resultsModel = saveRegion;
            minX = resultsModel.startX;
            minY = resultsModel.startY;
            maxX = resultsModel.endX;
            maxY = resultsModel.endY;
            dragAdapter =  new GridMouseInputAndDragAdapter(resultsModel);
            setupMouseActions();
        }
    }
    public void setScaleFactor(int val){
        if(val > 0 && val < 32){
            scaleFactor = val;
            setPreferredSize(new Dimension(wid*scaleFactor,hgt*scaleFactor));
            // invalidate();
            // validate();
            misImg = null;
            dirtyFlag = true;
            revalidate();
        }
    }
    private void setupMouseActions() {
        addMouseListener(dragAdapter);
        addMouseMotionListener(dragAdapter);
        dragAdapter.refreshCTD(TransferHandler.NONE, getDropTarget());
        dragAdapter.assignMousePressedCallback(MouseEvent.BUTTON1, new MouseActionCallback() {
            public void doCallback(MouseEvent e, GridMouseResultsModel results) {
                results.resetBox();
                
            }
        });
        dragAdapter.assignMouseDraggedCallback( new MouseDragCallback() {
            private void leftDrag(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel results){
                results.assignBoxX(e.getX());
                results.assignBoxY(e.getY());
                results.assignPatternPage(0);
                updateOverlay();
                
            }
            private void rightDrag(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel resultsModel){
                // you can ONLY drag a results box
                if(!resultsModel.isBoxValid()){
                    return;
                }               
            }
            public void doDragCallback(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel resultsModel) {
                if((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK){
                    leftDrag(firstEvent, e, resultsModel);
                }
                if((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK){
                    rightDrag(firstEvent, e, resultsModel);
                }
            }
        });
        
        
        
    }
    
    public void setMis(MemoryImageSource imgMis){
        mis = imgMis;
        misImg = null;
        dirtyFlag = true;
        repaint();
    }
    public void setMis(MemoryImageSource imgMis, int newWid, int newHgt){
        mis = imgMis;
        misImg = null;
        wid = newWid;
        hgt = newHgt;
        setScaleFactor(scaleFactor);
    }
    
    private void updateOverlay(){
        if(resultsModel == null){
            return;
        }
        if(!resultsModel.isBoxValid()){
            return;
        }
        
        minX = resultsModel.startX;
        minY = resultsModel.startY;
        maxX = resultsModel.endX;
        maxY = resultsModel.endY;
        
        if(resultsModel.startX > resultsModel.endX){
            minX = resultsModel.endX;
            maxX = resultsModel.startX;
        }
        if(resultsModel.startY > resultsModel.endY){
            minY = resultsModel.endY;
            maxY = resultsModel.startY;
        }
       
        repaint();
    }
    
    public void reallyRepaint(){
        misImg = null;
        dirtyFlag = true;
        repaint();
    }
    
    public void setRenderingHint(Object obj){
        misImg = null;
        dirtyFlag = true;
        repaint();
    }
    
    
    public void paintComponent(Graphics g){
        
        if(misImg == null){
            misImg = createImage(mis);
        }
        super.paintComponent(g);  // paints background
        g.setColor(Color.white);
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(Color.lightGray);
        int upper = getWidth();
        if(getHeight() > upper){
            upper = getHeight();
        }
        for(int i=0;i<upper*2;i+=10){
            g.drawLine(i,0,0,i);
        }
        
        g.drawImage(misImg,0,0,wid*scaleFactor,hgt*scaleFactor, this);

        if(resultsModel != null){
            if(resultsModel.isBoxValid()){
                g.setColor(Color.GREEN);
                int stx = (resultsModel.startX*PPUConstants.CHR_WIDTH);
                int sty = (resultsModel.startY*PPUConstants.CHR_HEIGHT);
                int wx = ((resultsModel.endX-resultsModel.startX)*PPUConstants.CHR_WIDTH);
                int wy = ((resultsModel.endY-resultsModel.startY)*PPUConstants.CHR_HEIGHT);                
                g.drawRect(stx * scaleFactor, sty * scaleFactor, wx * scaleFactor, wy * scaleFactor);
            }
        }
    }
    
    
}
