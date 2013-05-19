/*
 * PalettePanel.java
 *
 * Created on September 24, 2006, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.palette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import model.NESPaletteListener;
import model.NESPaletteModel;
import ui.chr.PPUConstants;
import emulator.nes.PPU;

/**
 * The Nintendo palette consists of 64 possible values (0x00 to 0x3F) of which the PPU can have 32 of them in its palette table (stored at 0x3F00-0x3F1F)
 * The purpose of this tool is to assist in setting up that palette table
 * There are some limitations:
 * Changes made to 0x3F00 and 0x3F10 affect one another (ie: they must be the same)
 * Each 4 bytes is a copy of 0x3F00.  So dont even bother trying to write to 0x3F04, 0x3F08, 0x3F0C, 0x3F10, 0x3F14, 0x3F18, 0x3F1C
 * This tool allows a 32 entry palette to be loaded and stored (so it can be .incbin'ed into the code)
 * Obviously there will really only be 26 distinct values.
 */
public class PalettePanel extends JPanel implements PaletteClickListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 7812761999310608051L;
	// NOT persisted
    public final static String PALETTE_PANEL_PALETTE_INDEX = "PalettePanelPaletteIndex_";
    public final static boolean DEFAULT_PALETTE_PANEL_PALETTE_INDEX_VALUE = false;
    
    private JPanel imagePalettePanel = null;
    private JPanel spritePalettePanel = null;
    private PaletteEntryPanel imagePalette[] = null;
    private PaletteEntryPanel spritePalette[] = null;
    private PaletteEntryPanel fixedPalette[] = null;
    
    private NESPaletteListener listener = null;
    private NESPaletteModel  paletteModel = null;
    // these are redundant
    private int lastClickedIndex = 0;
    private int lastClickedComponent = 0;
    
    private Vector<PaletteClickListener> clickListeners = new Vector<PaletteClickListener>();
    
    private boolean  supportBackgroundPalette = true;
    private boolean  supportSpritePalette = true;
    private boolean  supportRemainingPalette = true;
    private boolean supportSelecting = true;
    private boolean clickOverride = false;
    
    private int visibleBackgroundPaletteSize = PPUConstants.NES_IMAGE_PALETTE_SIZE;
    private int visibleSpritePaletteSize = PPUConstants.NES_SPRITE_PALETTE_SIZE;
    
    private int maxBackgroundPaletteSize = PPUConstants.NES_IMAGE_PALETTE_SIZE;
    private int maxSpritePaletteSize = PPUConstants.NES_SPRITE_PALETTE_SIZE;
    
    public final static int BANNED[] = { 0x0D };
    public boolean preventedIndex[] = new boolean[PPUConstants.NES_FIXED_PALETTE_SIZE]; // by default NONE are prevented

    private PaletteColorProvider _colorProvider = null;
    /**
     * Creates a new instance of PalettePanel
     */
    public PalettePanel(NESPaletteListener callback, NESPaletteModel model, boolean setupBackgroundPalette, boolean setupSpritePalette) {
        this(callback, model, setupBackgroundPalette, setupSpritePalette,  PPUConstants.NES_IMAGE_PALETTE_SIZE,  PPUConstants.NES_SPRITE_PALETTE_SIZE);
    }
    
    public PalettePanel(NESPaletteListener callback, NESPaletteModel model, boolean setupBackgroundPalette, boolean setupSpritePalette,  int visibleBGPaletteSize, int visibleSprPaletteSize) {
        this(callback, model, setupBackgroundPalette, setupSpritePalette,  visibleBGPaletteSize, visibleSprPaletteSize, true, true, false);
    }
    public PalettePanel(NESPaletteListener callback, NESPaletteModel model, boolean setupBackgroundPalette, boolean setupSpritePalette,  int visibleBGPaletteSize, int visibleSprPaletteSize, boolean showRemainingPalette, boolean shouldSupportClicking, boolean allowAnyClick) {
        this(callback, model, setupBackgroundPalette, setupSpritePalette,  visibleBGPaletteSize, visibleSprPaletteSize, PPUConstants.NES_IMAGE_PALETTE_SIZE, PPUConstants.NES_SPRITE_PALETTE_SIZE, showRemainingPalette, shouldSupportClicking, allowAnyClick);
    }
    public PalettePanel(NESPaletteListener callback, NESPaletteModel model, boolean setupBackgroundPalette, boolean setupSpritePalette,  int visibleBGPaletteSize, int visibleSprPaletteSize, int maxBGPaletteSize, int maxSprPaletteSize, boolean showRemainingPalette, boolean shouldSupportClicking, boolean allowAnyClick) {
        this(callback, model, setupBackgroundPalette, setupSpritePalette,  visibleBGPaletteSize, visibleSprPaletteSize, maxBGPaletteSize, maxSprPaletteSize, showRemainingPalette, shouldSupportClicking, allowAnyClick, null);
    }
    public PalettePanel(NESPaletteListener callback, NESPaletteModel model, boolean setupBackgroundPalette, boolean setupSpritePalette,  int visibleBGPaletteSize, int visibleSprPaletteSize, int maxBGPaletteSize, int maxSprPaletteSize, boolean showRemainingPalette, boolean shouldSupportClicking, boolean allowAnyClick, PaletteColorProvider colorProvider) {
        listener = callback;
        paletteModel = model;
        supportBackgroundPalette = setupBackgroundPalette;
        supportSpritePalette = setupSpritePalette;
        visibleBackgroundPaletteSize = visibleBGPaletteSize;
        visibleSpritePaletteSize = visibleSprPaletteSize;
        maxBackgroundPaletteSize = maxBGPaletteSize;
        maxSpritePaletteSize = maxSprPaletteSize;
        supportRemainingPalette = showRemainingPalette;
        supportSelecting = shouldSupportClicking; 
        clickOverride = allowAnyClick;
        if(listener != null) {
            addClickListener(this);
        }
        _colorProvider = colorProvider;
        setupUI();
    }
    
    public void setVisibleImagePaletteSize(int sz, boolean triggerUpdate){
        visibleBackgroundPaletteSize = sz;
        if(triggerUpdate){
            resetVisibility();
        }
    }
    public void setVisibleSpritePaletteSize(int sz, boolean triggerUpdate){
        visibleSpritePaletteSize = sz;
        if(triggerUpdate){
            resetVisibility();
        }
    }
    
    public int getCurrentPaletteIndex(){
        return lastClickedIndex;
    }
    // palette panel contains a color chooser,  16 panels representing the palette entries, 4 panels representing the active entries
    private void setupUI(){
        setBorder(new TitledBorder("Palette"));
        setLayout(new BorderLayout());
        setupDisplayUI();
    }
    
    public void addClickListener(PaletteClickListener newListener){
        clickListeners.add(newListener);
    }
  
    public void removeClickListener(PaletteClickListener newListener){
        clickListeners.remove(newListener);
    }    
    
    void notifyClicked(int componentIndex, int panelSet, boolean wasRightClicked){
        if(clickListeners.size() > 0){
            Iterator<PaletteClickListener> itor = clickListeners.iterator();
            while(itor.hasNext()){
                itor.next().processPaletteClick(componentIndex, panelSet, wasRightClicked);
            }
        }                   
    }

    public Color getColor(int palette, int index) {
        if(palette == PPUConstants.IMAGE_PALETTE_TYPE){
            return imagePalette[index].getBackground();
        } else  if(palette == PPUConstants.SPRITE_PALETTE_TYPE){
            return spritePalette[index].getBackground();
        } else {
            return fixedPalette[index].getBackground();
        }
    }
    
   public void processPaletteClick(int componentIndex, int panelSet, boolean wasRightClicked){

       if(paletteModel == null){
           return;
       }

        boolean imageChanged = false;
        boolean spriteChanged = false;
        
        if(supportBackgroundPalette){
            if(panelSet == PPUConstants.IMAGE_PALETTE_TYPE){ // only process palette set for the editable set
                int palLength = maxBackgroundPaletteSize;
                //int palLength = PPUConstants.NES_IMAGE_PALETTE_SIZE;
                if(componentIndex >= 0 && componentIndex < palLength){
                    imageChanged = true;
                    imagePalette[componentIndex].setPaletteIndex(lastClickedIndex);
                    paletteModel.updateImagePaletteIndex(componentIndex,(byte)lastClickedIndex);
                    if(componentIndex == 0){
                        spriteChanged = true;
                        if(supportSpritePalette){
                            spritePalette[0].setPaletteIndex(lastClickedIndex);
                        }
                        paletteModel.updateSpritePaletteIndex(0,(byte)lastClickedIndex);
                        
                        for(int i=1;i<palLength;i++){
                            if(i % 4 == 0 ){
                                if(supportSpritePalette){
                                    spritePalette[i].setPaletteIndex(lastClickedIndex);
                                }
                                paletteModel.updateImagePaletteIndex(i,(byte)lastClickedIndex);
                                imagePalette[i].setPaletteIndex(lastClickedIndex);
                                paletteModel.updateSpritePaletteIndex(i,(byte)lastClickedIndex);
                            }
                        }
                    }
                }
            }
        }
        if(supportSpritePalette){
            if(panelSet == PPUConstants.SPRITE_PALETTE_TYPE) {
                int palLength = maxSpritePaletteSize;
                //int palLength = PPUConstants.NES_SPRITE_PALETTE_SIZE;
                if(componentIndex >= 0 && componentIndex < palLength){
                    spriteChanged = true;
                    spritePalette[componentIndex].setPaletteIndex(lastClickedIndex);
                    paletteModel.updateSpritePaletteIndex(componentIndex,(byte)lastClickedIndex);
                    if(componentIndex == 0){
                        imageChanged = true;
                        if(supportBackgroundPalette){
                            imagePalette[0].setPaletteIndex(lastClickedIndex);
                        }
                        paletteModel.updateImagePaletteIndex(0,(byte)lastClickedIndex);
                        for(int i=1;i<palLength;i++){
                            if(i % 4 == 0){
                                spritePalette[i].setPaletteIndex(lastClickedIndex);
                                paletteModel.updateSpritePaletteIndex(i,(byte)lastClickedIndex);
                                if(supportBackgroundPalette){
                                    imagePalette[i].setPaletteIndex(lastClickedIndex);
                                }
                                paletteModel.updateImagePaletteIndex(i,(byte)lastClickedIndex);
                            }
                        }
                    }
                }
            }
        }
        if(panelSet == PPUConstants.NES_PALETTE_TYPE)  {
            if(wasRightClicked){
                if(componentIndex >= 0 && componentIndex < PPUConstants.NES_FIXED_PALETTE_SIZE){
                    preventedIndex[componentIndex] = ! preventedIndex[componentIndex];
                    boolean isClickable = isSelectable(componentIndex);
                    fixedPalette[componentIndex].setLeftClickable(isClickable);
                    if(isClickable) {
                        fixedPalette[componentIndex].setBorder(new BevelBorder(BevelBorder.RAISED));
                    } else {
                        fixedPalette[componentIndex].setBorder(LineBorder.createGrayLineBorder());
                    }
                }
                // we need to prevent this index from being used anymore
                if(! isSelectable(lastClickedIndex)){
                    for(int i=0;i<PPUConstants.NES_FIXED_PALETTE_SIZE;i++){
                        if(isSelectable(i)){
                            lastClickedComponent = i;
                            lastClickedIndex = i;
                            componentIndex = i;
                            break;
                        }
                    }
                }
            }
            if(isSelectable(componentIndex)){
                fixedPalette[lastClickedComponent].setBorder(new BevelBorder(BevelBorder.RAISED)); // restore old clicked palette entry
                
                lastClickedComponent = componentIndex;
                lastClickedIndex = fixedPalette[componentIndex].getPaletteIndex();
                fixedPalette[lastClickedComponent].setBorder(new BevelBorder(BevelBorder.LOWERED));// set new clicked palette entry
            }
        }
        if(imageChanged){
            listener.notifyImagePaletteChanged();
        }
        if(spriteChanged){
            listener.notifySpritePaletteChanged();
        }
    }
    
    public boolean isSelectable(int componentIndex){
        if(componentIndex < 0 || componentIndex >= preventedIndex.length){
            return false;
        }
        for(int i=0;i<BANNED.length;i++){
            if(componentIndex == BANNED[i]){
                return false;
            }
        }
        return ( ! preventedIndex[componentIndex]);
    }
    // allow only the settable palette  to process this.  The NES fixed palette is left alone
    void notifyRightClicked(int componentIndex, int panelSet){
        notifyClicked(componentIndex, panelSet, true);
    }
    
    void notifyLeftClicked(int componentIndex, int panelSet){
        notifyClicked(componentIndex, panelSet, false);
    }

    public void refreshFromPPU(PPU ppu){
        if(supportBackgroundPalette){
            for(int i=0;i<16;i++) {
                imagePalette[i].setPaletteIndex(ppu.getImagePaletteAtIndex(i));
            }
        }
        if(supportSpritePalette){
            for(int i=0;i<16;i++) {
                spritePalette[i].setPaletteIndex(ppu.getSpritePaletteAtIndex(i));
            }
        }
    }


    
    public void resetVisibility(){
        if(supportBackgroundPalette){
            for(int i=0;i< maxBackgroundPaletteSize;i++){
                if(i < visibleBackgroundPaletteSize){
                    imagePalette[i].setVisible(true);
                } else {
                    imagePalette[i].setVisible(false);
                }
            }
        }
        if(supportSpritePalette){
            for(int i=0;i< maxSpritePaletteSize;i++){
                if(i < visibleSpritePaletteSize){
                    spritePalette[i].setVisible(true);
                } else {
                    spritePalette[i].setVisible(false);
                }
            }
        }
    }

    byte getPaletteModelColor(int palette, int index){
        if(paletteModel == null){
            return 0x00;
        }
        if(palette == PPUConstants.IMAGE_PALETTE_TYPE){
            return paletteModel.getImagePaletteAtIndex(index);
        } else  if(palette == PPUConstants.SPRITE_PALETTE_TYPE){
            return paletteModel.getSpritePaletteAtIndex(index);
        } else {
            return (byte)index;
        }

    }
    public void resetPalette(){
        // reset image palette
        if(supportBackgroundPalette){
            imagePalettePanel.removeAll();
            imagePalettePanel.setBorder(new TitledBorder("Image Palette"));
            imagePalettePanel.setLayout(new GridLayout(1, maxBackgroundPaletteSize, 0,0));
            imagePalette = new PaletteEntryPanel[  maxBackgroundPaletteSize];
            for(int i=0;i< maxBackgroundPaletteSize;i++){
                boolean isClickable = true;
                if(i % 4 == 0 && i != 0){
                    isClickable = clickOverride;
                }
                imagePalette[i] = new PaletteEntryPanel(i,getPaletteModelColor(PPUConstants.IMAGE_PALETTE_TYPE, i),PPUConstants.IMAGE_PALETTE_TYPE,this,isClickable&&supportSelecting,isClickable&&supportSelecting);
                imagePalette[i].setPaletteColorProvider(_colorProvider);
                if(!isClickable){
                    imagePalette[i].setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
                } else {
                    imagePalette[i].setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
                }
                imagePalettePanel.add(imagePalette[i]);
            }
        }
        
        // reset sprite palette
        if(supportSpritePalette) {
            spritePalettePanel.removeAll();
            spritePalettePanel.setBorder(new TitledBorder("Sprite Palette"));
            spritePalettePanel.setLayout(new GridLayout(1, maxSpritePaletteSize, 0,0));
            spritePalette = new PaletteEntryPanel[ maxSpritePaletteSize];
            for(int i=0;i<maxSpritePaletteSize;i++){
                boolean isClickable = true;
                if(i % 4 == 0){
                    isClickable = clickOverride;
                }
                spritePalette[i] = new PaletteEntryPanel(i,getPaletteModelColor(PPUConstants.SPRITE_PALETTE_TYPE, i),PPUConstants.SPRITE_PALETTE_TYPE,this,isClickable&&supportSelecting ,isClickable&&supportSelecting, ((isClickable) ? null : "X"));
                spritePalette[i].setPaletteColorProvider(_colorProvider);

                if(!isClickable){
                    spritePalette[i].setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
            //        spritePalette[i].setVisible(false);
            //        JButton tempVal = new JButton("X");
           //         tempVal.setEnabled(false);
           //         spritePalettePanel.add(tempVal);
                    spritePalettePanel.add(spritePalette[i]);
                } else {
                    spritePalette[i].setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
                    spritePalettePanel.add(spritePalette[i]);
                }
                
            }
        }
        resetVisibility();
        validate();
        invalidate();
    }
    
    private void setupDisplayUI(){
        JPanel westPanel = new JPanel();
        add(westPanel, BorderLayout.NORTH);
        westPanel.setLayout(new BorderLayout());
        JPanel bodyPanel = new JPanel();
        westPanel.add(bodyPanel, BorderLayout.WEST);
        
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        bodyPanel.setLayout(gbl);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        
        if(supportBackgroundPalette){
            imagePalettePanel = new JPanel();
            gbl.setConstraints(imagePalettePanel,gbc);
            bodyPanel.add(imagePalettePanel);
            
            gbc.gridx = 1;
            gbc.weightx = 1;
            JPanel filler = new JPanel();
            gbl.setConstraints(filler,gbc);
            bodyPanel.add(filler);
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0;
        }
        if(supportSpritePalette){
            spritePalettePanel = new JPanel();
            gbl.setConstraints(spritePalettePanel,gbc);
            bodyPanel.add(spritePalettePanel);
            
            gbc.gridx = 1;
            gbc.weightx = 1;
            JPanel filler = new JPanel();
            gbl.setConstraints(filler,gbc);
            bodyPanel.add(filler);
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0;
        }
        if(supportRemainingPalette){
            JPanel fixedPalettePanel = new JPanel();
            fixedPalettePanel.setBorder(new TitledBorder("NES Palette"));
            //fixedPalettePanel.setLayout(new GridLayout(4,16,2,2));
            fixedPalettePanel.setLayout(new GridLayout(4,16,0,0));
            fixedPalette = new PaletteEntryPanel[PPUConstants.NES_FIXED_PALETTE_SIZE];
            for(int i=0;i<PPUConstants.NES_FIXED_PALETTE_SIZE;i++){
                // preventedIndex[i] = EnvironmentUtilities.getBooleanEnvSetting(PALETTE_PANEL_PALETTE_INDEX+i, DEFAULT_PALETTE_PANEL_PALETTE_INDEX_VALUE );
                boolean isClickable  = isSelectable(i);
                fixedPalette[i] = new PaletteEntryPanel(i, i, PPUConstants.NES_PALETTE_TYPE, this, isClickable, true); // always right clickable
                if(isClickable) {
                    fixedPalette[i].setBorder(new BevelBorder(BevelBorder.RAISED));
                } else {
                    fixedPalette[i].setBorder(LineBorder.createGrayLineBorder());
                }
                fixedPalettePanel.add(fixedPalette[i]);
            }
            gbc.gridheight = 4;
            gbl.setConstraints(fixedPalettePanel,gbc);
            bodyPanel.add(fixedPalettePanel);
        }
        JPanel widFiller = new JPanel();
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbl.setConstraints(widFiller,gbc);
        bodyPanel.add(widFiller);
        
        JPanel hgtFiller = new JPanel();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbl.setConstraints(hgtFiller,gbc);
        bodyPanel.add(hgtFiller);
        
        resetPalette();
    }
    
    
    
    
}
