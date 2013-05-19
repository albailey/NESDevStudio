/*
 * PatternTableManagementPanel.java
 *
 * Created on August 13, 2008, 8:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampSetEditor;

import gameTools.levelEditor.LevelEditorSettings;
import gameTools.metaEditor.StampMetaDataPanel;
import gameTools.stampEditor.CHRMultiTilePanelDecorator;
import gameTools.stampEditor.StampEditor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import model.NESModelListener;
import model.NESPaletteListener;
import ui.chr.CHRDisplayControls;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.patternTable.PatternTablePanel;
import ui.chr.tileEditor.CHRMultiTilePanel;
import ui.chr.tileEditor.CHRTile;
import ui.listeners.PopupListener;
import utilities.FileUtilities;
import utilities.GUIUtilities;

/**
 *
 * @author abailey
 */
public class PatternTableManagementPanel extends JPanel {
    
    /**
	 * 
	 */
	private final static boolean META_DATA_SUPPORTED = false;
	
	private static final long serialVersionUID = -6900312674801344458L;
	private CHRDisplayControls _stampControls = null;
    private Vector<CHRMultiTilePanelDecorator> _stamps = null;
    private JPanel _stampPanel = null;
    private CHRMultiTilePanelDecorator _lastStamp = null;
    private int _insertionPoint = 1; // for use in inserting into the pattern table
    private int _metaInsertionPoint = 1;
    private PatternTablePanel _tilePanel = null;
    private JLabel _tileCountLabel = null;
    private int _targetBank = 0;
    private ActiveStampObserver _activeStampObserver = null;
    private NESModelListener _listener = null;
    private CHREditorModel _modelRef = null;
    private String _prefix = null;
    private PopupListener _popupListener = null;
    private StampMetaDataPanel _metaPanel = null;
    private String _paletteFileName = null;
    
    /** Creates a new instance of PatternTableManagementPanel */
    public PatternTableManagementPanel(ActiveStampObserver activeStampObserver, NESModelListener listener, CHREditorModel modelRef, String prefix, int targetBank) {
        super();
        _activeStampObserver = activeStampObserver;
        _modelRef = modelRef;
        _prefix = prefix;
        _targetBank = targetBank;
        _listener = listener;
        // setup stamp parts
        _stampControls = new CHRDisplayControls("LEVEL_EDITOR_STAMPS_" + targetBank);
        _stamps = new Vector<CHRMultiTilePanelDecorator>();
        setupUI();
        addBlankStamp();
    }
    
     public File loadStamps(String stampPropsFile){
        return loadStamps(stampPropsFile, false, null);
     }
     
    public File loadStamps(String stampPropsFile, boolean reloadPalette, NESPaletteListener palListener){
        File selectedFile = null;
        if(stampPropsFile != null){
            selectedFile = new File(stampPropsFile);
        } else {
            selectedFile = FileUtilities.selectFileForOpen( this );
        }
        
        if(selectedFile != null){

            boolean canFail = true;
            try {
                FileInputStream fis = new FileInputStream(selectedFile);
                DataInputStream dis = new DataInputStream(fis);
                int numStamps = dis.readInt();
                for(int i=0;i<numStamps;i++){
                    String str = dis.readUTF();
                    insertStampFile(str);
                }
                canFail = false;
                if(reloadPalette){
                    for(int i=0;i<16;i++){
                        _modelRef.setPaletteValue(PPUConstants.IMAGE_PALETTE_TYPE,i, dis.readByte(), null);// pass null for the listener so I dont update 32 times
                    }
                    for(int i=0;i<16;i++){
                        _modelRef.setPaletteValue(PPUConstants.SPRITE_PALETTE_TYPE,i, dis.readByte(), null);
                    }   
                }
                fis.close();
                dis.close();
            } catch(Exception e){
                if(canFail){
                    e.printStackTrace();
                    return null;
                }
            }
            if(palListener != null){ //notify the palette listener
                palListener.notifyImagePaletteChanged();
                palListener.notifySpritePaletteChanged();
            }
            // store the binary stuff based on the prefix in the same directory
            return selectedFile;
            
        }
        return  null;
    }
    
    public void setPaletteFileName(String paletteFileName){
        _paletteFileName = paletteFileName;
    }
    public String getPaletteFileName(){
        return _paletteFileName;
    }
    
    public void purge(){
            boolean tryAgain = false;
            do {
                tryAgain = false;
                Iterator<CHRMultiTilePanelDecorator> itor =_stamps.iterator();
                while(itor.hasNext()){
                    CHRMultiTilePanelDecorator stamp = itor.next();
                    if(stamp.isRemoveable()) {
                        if(removeStamp(stamp, false)){
                            tryAgain = true;
                            break; // otherwise the iterator is broken
                        }
                    }
                }        
            } while(tryAgain);
    }
    public File saveStamps(String stampPropsFile){
        File selectedFile = null;
        if(stampPropsFile != null){
            selectedFile = new File(stampPropsFile);
        } else {
            selectedFile = FileUtilities.selectFileForSave( this );
        }
        
        if(selectedFile != null ){
            // store the property to the selectedFile.
            
            try {
                FileOutputStream fos = new FileOutputStream(selectedFile);
                DataOutputStream dos = new DataOutputStream(fos);
                Iterator<CHRMultiTilePanelDecorator> itor =_stamps.iterator();
                int count = 0;
                while(itor.hasNext()){
                    CHRMultiTilePanelDecorator stamp = itor.next();
                    if(stamp.isRemoveable()) {
                        count++;
                    }
                }
                dos.writeInt(count);
                
                itor =_stamps.iterator();
                while(itor.hasNext()){
                    CHRMultiTilePanelDecorator stamp = ((CHRMultiTilePanelDecorator)itor.next());
                    if(stamp.isRemoveable()) {
                        dos.writeUTF(stamp.getTilePanel().getFileName());
                    }
                    // store the meta properties
                }
                for(int i=0;i<16;i++){
                    dos.writeByte(_modelRef.getCHRModel().getImagePaletteAtIndex(i));
                }
                for(int i=0;i<16;i++){
                    dos.writeByte(_modelRef.getCHRModel().getSpritePaletteAtIndex(i));
                }
                fos.close();
                dos.close();
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }
            
            // store the binary stuff based on the prefix in the same directory
            return selectedFile;
            
        }
        return  null;
    }
    
    
    
    public void updateStamps(){
        Iterator<CHRMultiTilePanelDecorator> itor =_stamps.iterator();
        while(itor.hasNext()){
            itor.next().getTilePanel().notifyDisplayInterfaceUpdated();
        }
    }
    
    public void updatePatternTable(){
        if(_tilePanel != null) {
            _tilePanel.updatePatternTable();
        }
    }
    
    public void insertStampFile(String fileName){
        CHRMultiTilePanel newStamp = new CHRMultiTilePanel(2,2, _modelRef, _stampControls);
        if(newStamp.loadFromFile(_insertionPoint, _metaInsertionPoint, fileName)){
            CHRMultiTilePanelDecorator decorator = new CHRMultiTilePanelDecorator(newStamp, _targetBank);
            // need to load meta from file as well.
            if(META_DATA_SUPPORTED) {
            	decorator.setMetaProperties(_metaPanel.getDefaultPropertiesCopy());
            }
            addStamp(decorator);
            if(_activeStampObserver != null){
                _activeStampObserver.setActiveStamp(newStamp, _targetBank); // this needs work
            }
        }
    }
    
    public void updateMetaTilesScale(Integer val) {
        _stampControls.setScale(val.intValue());
        updateStamps();
    }
    public void updateMetaTilesGrid(Boolean val) {
        _stampControls.setShowTileGrid(val.booleanValue());
        updateStamps();
    }
    public void updateMetaTilesOAMGrid(Boolean val) {
        _stampControls.setShowOAMGrid(val.booleanValue());
        updateStamps();
    }
    
    
    private void addBlankStamp(){
        CHRMultiTilePanel blankStamp = new CHRMultiTilePanel(2,2, _modelRef, _stampControls);
        blankStamp.setDescription("Blank");
        blankStamp.setInsertionPoint(0,0);
		blankStamp.setSingleTilePattern(0);

        CHRMultiTilePanelDecorator decorator = new CHRMultiTilePanelDecorator(blankStamp, _targetBank);
        decorator.setRemoveable(false);
        decorator.setAdded(true); // this bypasses loading blank tile data
        if(META_DATA_SUPPORTED) {
        	decorator.setMetaProperties(_metaPanel.getDefaultPropertiesCopy());
        }
        addStamp(decorator);
        if(_activeStampObserver != null){
            _activeStampObserver.setActiveStamp(blankStamp, _targetBank); // this needs work
        }

    }
    
    private void addStamp(CHRMultiTilePanelDecorator decorator){
        _stamps.add(decorator);
        _stampPanel.add(decorator);
        if(! decorator.isAdded()) {
            CHRMultiTilePanel stamp = decorator.getTilePanel();
            CHRTile tiles[] = stamp.getTiles();
            for(int i=0;i<tiles.length;i++){
                int byteOffset = _insertionPoint * 16;
                System.arraycopy(tiles[i].asMask(),0,_modelRef.getCHRModel().patternTable[_targetBank],byteOffset,16);
                _insertionPoint++;
            }
            _metaInsertionPoint += stamp.getMetaTilesSize();
            decorator.setAdded(true);
            decorator.addMouseListener(_popupListener);
        }
        
        MouseListener actListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(_lastStamp != null){
                    _lastStamp.setBorder(new BevelBorder(BevelBorder.RAISED));
                }
                _lastStamp = (CHRMultiTilePanelDecorator)e.getSource();
                _lastStamp.setBorder(new BevelBorder(BevelBorder.LOWERED));
                if(_activeStampObserver != null){
                    _activeStampObserver.setActiveStamp(_lastStamp.getTilePanel(), _targetBank); // this needs work
                }
                if(META_DATA_SUPPORTED) {
                	_metaPanel.updateUIFields(((CHRMultiTilePanelDecorator)e.getSource()).getMetaProperties());
                }
            }
        };
        decorator.addMouseListener(actListener);
        
        
        _tileCountLabel.setText(_prefix + " Tiles Remaining:" + (256 - _insertionPoint));
        _stampPanel.revalidate();
        _tilePanel.updatePatternTable();
        if(_listener != null){
            _listener.notifyPatternTableChanged();
        }
    }
    
    private void cyclePaletteForStamp(CHRMultiTilePanelDecorator multiStamp){
        CHRMultiTilePanel stamp = multiStamp.getTilePanel();
        stamp.cycleOAM();
        _activeStampObserver.stampChanged(stamp);
        stamp.saveStamp();   
    }
    
    private void setPaletteForStamp(CHRMultiTilePanelDecorator stamp){
        System.out.println("Set Palette for Stamp not yet implemented");
        // stamp.getTilePanel();
    }
    
    private void editStamp(CHRMultiTilePanelDecorator stamp){
        // launch a NEW CHR Layout tool
        StampEditor frame = null;
        
        Container c = getParent();
        int depth = 0;
        while(depth < 10){
            if(c instanceof JDesktopPane){
                frame = new StampEditor(stamp.getTilePanel(), _activeStampObserver); // this needs a copy....
                frame.setVisible(true); //necessary as of 1.3
                ((JDesktopPane)c).add(frame);
                try {
                	frame.setSelected(true);
        		} catch (java.beans.PropertyVetoException e) {
        		}
        		break;
            }
            depth++;
            c = c.getParent();
        }
    }
    
    private void saveStamp(CHRMultiTilePanelDecorator stamp){
        System.out.println("Save Stamp not yet implemented");
        // stamp.getTilePanel();
    }
  
    /*
    private void removeStamp(){
        removeStamp(_lastStamp, true);
    }
    */
    
    public int getNumTiles() {
    	return _insertionPoint;
    }
    
    private boolean removeStamp(CHRMultiTilePanelDecorator stampToRemove, boolean confirm){
        if(stampToRemove == null){
            return false;
        }
        if(!stampToRemove.isRemoveable()){
            return false;
        }
        if(confirm){
            if ( JOptionPane.showConfirmDialog(this
                ,"Removing this stamp means removing it from the pattern table and nametable. Are you sure you wish to remove it?"
                ,"Are you sure?"
                , JOptionPane.YES_NO_OPTION) == 1)
            {
               return false;
            }
        }
        _stamps.remove(stampToRemove);
        _stampPanel.remove(stampToRemove);
        if(_activeStampObserver != null){
            _activeStampObserver.setActiveStamp(null, _targetBank);
        }
        CHRMultiTilePanel stamp = stampToRemove.getTilePanel();
        int oldPos = stamp.getInsertionPoint();
        int offDiff = stamp.getTilesSize();
        int offMetaDiff = stamp.getMetaTilesSize();
        if(stampToRemove == _lastStamp){
            _lastStamp = null;
        }
        int oldEnd = oldPos + offDiff;
        
        // tell everyone with a insertionPoint larger than oldEnd
        Iterator<CHRMultiTilePanelDecorator> itor = _stamps.iterator();
        while(itor.hasNext()){
            CHRMultiTilePanel pan = itor.next().getTilePanel();
            if(pan.getInsertionPoint() >= oldEnd){
                pan.setInsertionPoint(pan.getInsertionPoint() - offDiff, pan.getMetaInsertionPoint() - offMetaDiff);
            }
        }
        // shift BACK all the tiles in CHR
        int rem = _insertionPoint - oldEnd;
        byte b[] = null;
        if(rem > 0){
            b = new byte[rem*16];
            System.arraycopy(_modelRef.getCHRModel().patternTable[_targetBank], oldEnd*16, b,0,b.length);
            System.arraycopy(b,0,_modelRef.getCHRModel().patternTable[_targetBank], oldPos*16,b.length);
        }
        b = new byte[offDiff*16]; // sets it all back to zeroes
        _insertionPoint -= offDiff;
        System.arraycopy(b,0,_modelRef.getCHRModel().patternTable[_targetBank], _insertionPoint*16,b.length);
        
        // Now fix the nametables
        int numPgs = _modelRef.getCHRModel().getNumPages();
        
        for(int pg=0;pg<numPgs;pg++){
            for(int i=0;i<PPUConstants.NUM_NAME_TABLE_ENTRIES;i++){
                byte chr = _modelRef.getCHRModel().nameTableIndexes[pg][i];
                if( chr >= oldPos && chr < oldEnd ){
                    // replace inserts for the stamp with TILE ZERO
                    _modelRef.getCHRModel().nameTableIndexes[pg][i] = (byte)0;
                    _modelRef.updateNameTableIndex(pg, i, (byte)0);
                } else if(chr >= oldEnd){
                    // shift HIGHER stamped tiles DOWN
                    _modelRef.getCHRModel().nameTableIndexes[pg][i] = (byte)(chr - offDiff);
                    _modelRef.updateNameTableIndex(pg, i, (byte)(chr - offDiff));
                }
            }
        }
        
        // refresh GUI parts
        _tileCountLabel.setText(_prefix + " Tiles Remaining:" + (256 - _insertionPoint));
        _stampPanel.revalidate();
        _stampPanel.repaint();
        _tilePanel.updatePatternTable();
        if(_listener != null){
            _listener.notifyPatternTableChanged();
        }
        return true;
    }
    
    
    
    
    private void setupPopupMenu(){
        JPopupMenu popupMenu = new JPopupMenu();
        
        
        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(_popupListener.popupTrigger instanceof CHRMultiTilePanelDecorator){
                    editStamp((CHRMultiTilePanelDecorator)_popupListener.popupTrigger);
                } else {
                    System.err.println("Developer Error. Edit Stamp triggered on non stamp object");
                }
            }
        }
        );
        popupMenu.add(editItem);
        
        JMenuItem cyclePaletteItem = new JMenuItem("Cycle Palette");
        cyclePaletteItem.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(_popupListener.popupTrigger instanceof CHRMultiTilePanelDecorator){
                    cyclePaletteForStamp((CHRMultiTilePanelDecorator)_popupListener.popupTrigger);
                } else {
                    System.err.println("Developer Error. Set Palette For Stamp triggered on non stamp object");
                }
            }
        }
        );
        popupMenu.add(cyclePaletteItem);
        
        JMenuItem setPaletteItem = new JMenuItem("Set Palette");
        setPaletteItem.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(_popupListener.popupTrigger instanceof CHRMultiTilePanelDecorator){
                    setPaletteForStamp((CHRMultiTilePanelDecorator)_popupListener.popupTrigger);
                } else {
                    System.err.println("Developer Error. Set Palette For Stamp triggered on non stamp object");
                }
            }
        }
        );
        popupMenu.add(setPaletteItem);
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(_popupListener.popupTrigger instanceof CHRMultiTilePanelDecorator){
                    saveStamp((CHRMultiTilePanelDecorator)_popupListener.popupTrigger);
                } else {
                    System.err.println("Developer Error. Save Stamp triggered on non stamp object");
                }
            }
        }
        );
        popupMenu.add(saveItem);
        
        // an entry to do a remove
        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(_popupListener.popupTrigger instanceof CHRMultiTilePanelDecorator){
                    removeStamp((CHRMultiTilePanelDecorator)_popupListener.popupTrigger, true);
                }
            }
        }
        );
        popupMenu.add(removeItem);
        
        //Add listener to components that can bring up popup menus.
        _popupListener = new PopupListener(popupMenu);
        
    }
    
    private JPanel setupMetaPanel(){
        _metaPanel = new StampMetaDataPanel();
        _metaPanel.setBorder(new TitledBorder("Selected Stamp Settings"));
        if(_targetBank ==  PPUConstants.IMAGE_PALETTE_TYPE){
            _metaPanel.loadDefaultBGProperties();
        } else {
            _metaPanel.loadDefaultSpriteProperties();
        }
        return _metaPanel;
    }
    
    private void setupUI(){
        setLayout(new BorderLayout());
        setupPopupMenu();
        
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        
        if(META_DATA_SUPPORTED) {
        	innerPanel.add(setupMetaPanel(), BorderLayout.SOUTH);
        }
        
        try {
            JPanel _settingsPanel = new JPanel();
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            _settingsPanel.setLayout(gbl);
            _settingsPanel.setBorder(new TitledBorder(_prefix + " Display Settings"));
            Class<?> intClass[] = {Integer.class};
            Class<?> boolClass[] = {Boolean.class};
            int yPos = 0;
            
            GUIUtilities.addCustomCheckBox(this, gbc, gbl, _settingsPanel, yPos,
                    LevelEditorSettings.SHOW_TILE_GRID_TITLE, LevelEditorSettings.SHOW_TILE_GRID_TOOLTIP,
                    LevelEditorSettings.LEVEL_EDITOR_SHOW_TILE_GRID, LevelEditorSettings.DEFAULT_LEVEL_EDITOR_SHOW_TILE_GRID,
                    getClass().getMethod("updateMetaTilesGrid",boolClass));
            yPos++;
            
            GUIUtilities.addCustomCheckBox(this, gbc, gbl, _settingsPanel, yPos,
                    LevelEditorSettings.SHOW_OAM_GRID_TITLE, LevelEditorSettings.SHOW_OAM_GRID_TOOLTIP,
                    LevelEditorSettings.LEVEL_EDITOR_SHOW_OAM_GRID, LevelEditorSettings.DEFAULT_LEVEL_EDITOR_SHOW_OAM_GRID,
                    getClass().getMethod("updateMetaTilesOAMGrid",boolClass));
            yPos++;
            
            GUIUtilities.addCustomSpinner(this, gbc, gbl, _settingsPanel, yPos,
                    LevelEditorSettings.LEVEL_TILES_SCALE_TITLE, LevelEditorSettings.LEVEL_TILES_SCALE_TOOLTIP,
                    _stampControls.getScale(),
                    LevelEditorSettings.MIN_LEVEL_EDITOR_LEVEL_TILES_SCALE, LevelEditorSettings.MAX_LEVEL_EDITOR_LEVEL_TILES_SCALE, LevelEditorSettings.STEP_LEVEL_EDITOR_LEVEL_TILES_SCALE,
                    getClass().getMethod("updateMetaTilesScale",intClass));
            yPos++;
            int xPos = 0;
            _settingsPanel.add( GUIUtilities.createButton("Load Stamp"
                    , "Load a stamp from disk"
                    ,new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    insertStampFile(null);
                }
            }
            ,gbc, gbl, xPos,yPos)
            );
            xPos++;
      
            
            JPanel northPanel = new JPanel();
            northPanel.setLayout(new BorderLayout());
            northPanel.add(_settingsPanel, BorderLayout.WEST);
            
            JPanel tileDataPanel = new JPanel();
            tileDataPanel.setLayout(new BorderLayout());
            _tilePanel = new PatternTablePanel(_listener, _modelRef , _targetBank, _prefix + " Tiles", _targetBank);
            _tileCountLabel = new JLabel(" Tiles Remaining:" + (256 - _insertionPoint));
            tileDataPanel.add(_tilePanel, BorderLayout.CENTER);
            tileDataPanel.add(_tileCountLabel, BorderLayout.NORTH);
            
            northPanel.add(tileDataPanel, BorderLayout.CENTER);
            innerPanel.add(northPanel, BorderLayout.NORTH);
        } catch(Exception e){
            e.printStackTrace();
        }
        add(innerPanel, BorderLayout.NORTH);
        
        // now the stamp area
        _stampPanel = new JPanel();
        _stampPanel.setBorder(new TitledBorder(_prefix + " Stamps"));
        
        _stampPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 2));
        _stampPanel.setMinimumSize(new Dimension(80,80));
        JScrollPane scrollPane = new JScrollPane(_stampPanel);
        scrollPane.setPreferredSize(new Dimension(200,100));
        
        add(scrollPane, BorderLayout.CENTER);
        
        
    }
    
    
}
