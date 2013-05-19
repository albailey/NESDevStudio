/*
 * PaletteEntryPanel.java
 *
 * Created on September 24, 2006, 3:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;

import ui.chr.PPUConstants;
import utilities.ByteFormatter;

/**
 *
 * @author abailey
 */
public class PaletteEntryPanel extends JPanel implements PaletteColorProvider {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5026774578691094349L;
	private boolean isLeftClickModeFlag = false;
    private boolean isRightClickModeFlag = false;
    private int componentIndex = 0;
    private int paletteIndex = 0;
    private int panelSet = 0;
    private PalettePanel paletteFrame = null;
    private String _overlayText = null;
    private JTextField textField = null;
    private static final int PAL_WIDTH = 12;
    private static final int PAL_HEIGHT = 12;

    private PaletteColorProvider _colorProvider = null;
    /**
     * Creates a new instance of PaletteEntryPanel
     */
    public PaletteEntryPanel(int compIndex, byte initColor, int clickSet, PalettePanel callback, boolean isLeftClickMode, boolean isRightClickMode) {
        this(compIndex, (int)(initColor & 0xFF), clickSet, callback, isLeftClickMode, isRightClickMode, null);
    }
    public PaletteEntryPanel(int compIndex, int initColor, int clickSet, PalettePanel callback, boolean isLeftClickMode, boolean isRightClickMode) {
        this(compIndex, initColor, clickSet, callback, isLeftClickMode, isRightClickMode, null);
    }
    public PaletteEntryPanel(int compIndex, int initColor, int clickSet, PalettePanel callback, boolean isLeftClickMode, boolean isRightClickMode, String overlayText) {
        componentIndex = compIndex;
        paletteIndex = initColor;
        panelSet = clickSet;
        paletteFrame = callback;
        isLeftClickModeFlag = isLeftClickMode;
        isRightClickModeFlag = isRightClickMode;
        _overlayText = overlayText;
        setupUI();
    }
    public void setLeftClickable(boolean val){
        isLeftClickModeFlag = val;
    }
    
    public void setPaletteColorProvider(PaletteColorProvider provider){
        _colorProvider = provider;
        updateDisplay();
    }
    public Color getPaletteColor(int palIndex){
        if(_colorProvider != null) {
            return _colorProvider.getPaletteColor(palIndex);
        } else {
            return PPUConstants.NES_PALETTE[palIndex];
        }
    }
    private void updateDisplay(){
       setBackground(getPaletteColor(paletteIndex));
        setToolTipText(ByteFormatter.formatSingleByteInt(paletteIndex));
        if(textField != null){
            textField.setToolTipText(ByteFormatter.formatSingleByteInt(paletteIndex));
        }
    }
    private void setupUI() {
        setMinimumSize(new Dimension(PAL_WIDTH,PAL_HEIGHT));
        setMaximumSize(new Dimension(PAL_WIDTH,PAL_HEIGHT));
        setPreferredSize(new Dimension(PAL_WIDTH,PAL_HEIGHT));
        if(_overlayText != null) {
            setLayout(new BorderLayout());
            textField = new JTextField(_overlayText );
            textField.setEditable(false);
            add( textField, BorderLayout.CENTER );
        }
        updateDisplay();
        // right click to set the color
        addMouseListener(new MouseInputAdapter() {
            public void mouseReleased(MouseEvent e) {
                if(isLeftClickModeFlag){
                    if( e.getButton() == MouseEvent.BUTTON1){
                        paletteFrame.notifyLeftClicked(componentIndex, panelSet);
                    }
                }
                if(isRightClickModeFlag){
                    if( e.getButton() == MouseEvent.BUTTON3){
                        paletteFrame.notifyRightClicked(componentIndex, panelSet);
                    }
                }
            }
        });
    }
    public void setPaletteIndex(int index){
        paletteIndex = index;
        updateDisplay();
    }
    
    public int getPaletteIndex(){
        return paletteIndex;
    }
}
