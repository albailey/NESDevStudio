/*
 * StampSetEditor.java
 *
 * Created on September 14, 2008, 9:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampSetEditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.border.EmptyBorder;

import model.NESPaletteListener;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.palette.PalettePanel;
import utilities.EnvironmentUtilities;
import utilities.GUIUtilities;

// TO DO:
// Add ability to set attributes on tiles in a stamp separately
// Add ability to view palette
// Add ability to set OAM per tile in a stamp
// Add ASM save capabilities for using in the engine



/**
 *
 * @author abailey
 */
public class StampSetEditor  extends JInternalFrame implements NESPaletteListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -6307765536358398683L;
	public final static String FRAME_TITLE = "Stamp Set Editor";
    private final static String STAMP_SET_EDITOR_LAST_FILE_PROPERTY = "STAMP_SET_EDITOR_LAST_FILE_PROPERTY";
    
    private CHREditorModel _modelRef = null;
    private PatternTableManagementPanel _stampSetPanel = null;
    private PalettePanel _paletteRegion  = null;
    private String _lastFileName = null;
    
    /**
     * Creates a new instance of StampSetEditor
     */
    public StampSetEditor() {
        super(FRAME_TITLE, true, true, false, false);
        _modelRef = new CHREditorModel();
        setupUI();
        reloadPrefs();
        pack();
        setLocation(0,0);
    }
    
    
    private void reloadPrefs(){
        String tmp = EnvironmentUtilities.getStringEnvSetting(STAMP_SET_EDITOR_LAST_FILE_PROPERTY, null);
        if(tmp != null) {
            loadStampSet(tmp);
        }
    }
    
    private void setupUI(){
        setupMenuBar();
        _stampSetPanel = new PatternTableManagementPanel(null, null, _modelRef, "Set", PPUConstants.IMAGE_PALETTE_TYPE);
        setLayout(new BorderLayout());
        add(_stampSetPanel, BorderLayout.CENTER);
        _paletteRegion = new PalettePanel(this, _modelRef.getCHRModel(), true, true);
        _paletteRegion.setBorder(new EmptyBorder(0,0,0,0));
        add(_paletteRegion, BorderLayout.EAST);
    }
    
    private void setupMenuBar(){
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        final Component parent = this;
        final NESPaletteListener listener = this;
        
        JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');
        GUIUtilities.createMenuItem(fileMenu, "Load", 'L', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                loadStampSet();
            }
        });
        GUIUtilities.createMenuItem(fileMenu, "Save", 'S', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                saveStampSet();
            }
        });
        GUIUtilities.createMenuItem(fileMenu, "Save As", 'A', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                saveStampSetAs();
            }
        });
        
        JMenu paletteMenu = GUIUtilities.createMenu(menuBar, "Palette", 'P');
        GUIUtilities.createMenuItem(paletteMenu, "Load", 'L', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                _modelRef.loadPalette(parent, listener);
            }
        });
        
    }
    
    private boolean loadStampSet(){
        return  loadStampSet(null);
    }
    
    private boolean loadStampSet(String fileName){
        _stampSetPanel.purge();
        File lastFile = _stampSetPanel.loadStamps(fileName, true, this);
        if(lastFile != null){
            _lastFileName = lastFile.getAbsolutePath();
            EnvironmentUtilities.updateStringEnvSetting(STAMP_SET_EDITOR_LAST_FILE_PROPERTY, _lastFileName);
            setTitle(FRAME_TITLE + " for: " + lastFile.getName());
            return true;
        }
        return false;
    }
    
    private void saveStampSet(){
        File lastFile =  _stampSetPanel.saveStamps(_lastFileName);
        if(lastFile != null){
            _lastFileName = lastFile.getAbsolutePath();
            EnvironmentUtilities.updateStringEnvSetting(STAMP_SET_EDITOR_LAST_FILE_PROPERTY, _lastFileName);
            setTitle(FRAME_TITLE + " for: " + lastFile.getName());
        }
    }
    
    private void saveStampSetAs(){
        File lastFile =  _stampSetPanel.saveStamps(null);
        if(lastFile != null){
            _lastFileName = lastFile.getAbsolutePath();
            EnvironmentUtilities.updateStringEnvSetting(STAMP_SET_EDITOR_LAST_FILE_PROPERTY, _lastFileName);
            setTitle(FRAME_TITLE + " for: " + lastFile.getName());
        }
    }
    
    /*
    private void updateMouseSettingsPanel(){
        
    }
     */
    
    
    // NESPaletteListener
    public void notifyImagePaletteChanged(){
        _stampSetPanel.updatePatternTable();
        _stampSetPanel.updateStamps();
       _paletteRegion.resetPalette();
    }
    
    public void notifySpritePaletteChanged(){
        _stampSetPanel.updatePatternTable();
        _stampSetPanel.updateStamps();
       _paletteRegion.resetPalette();
    }
    
    
}
