/*
 * LevelEditor.java
 *
 * Created on May 12, 2008, 9:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.levelEditor;

import gameTools.screenLayout.ScreenLayoutUI;
import gameTools.stampSetEditor.PatternTableManagementPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Iterator;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import model.NESModelListener;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.palette.PalettePanel;
import ui.chr.tileEditor.CHRTile;
import utilities.ByteFormatter;
import utilities.FileUtilities;
import utilities.GUIUtilities;

/**
 *
 * @author abailey
 */
public class LevelEditor extends JInternalFrame  implements NESModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1921489054742286022L;



	public final static String FRAME_TITLE = "Level Editor";



	private LevelEditorSettings editorSettings = null;
	private LevelEditorModel editorModel = null;
	private LevelEditorNameTablePanel levelPanel = null;


	private PatternTableManagementPanel bgStampPanel = null;
	private PatternTableManagementPanel spriteStampPanel = null;


	private CHREditorModel modelRef = null;
	private PalettePanel paletteRegion = null;

	protected JSpinner tilesWideSpinner = null;
	protected JSpinner tilesHighSpinner = null;
	protected JSpinner screensWideSpinner = null;
	protected JSpinner screensHighSpinner = null;
	protected JSpinner scaleSpinner = null;
	protected JTabbedPane tabPanel = null;

	public LevelEditor() {
		this(null,null);
	}
	public LevelEditor(CHREditorModel existingModel, LevelEditorModel levelModel) {
		super(FRAME_TITLE, true, true, false, false);
		setTitle(getFrameTitle());

		boolean reloadMode = true;
		editorSettings = new LevelEditorSettings();
		if(levelModel == null){
			editorModel = new LevelEditorModel(0);
		} else {
			editorModel= levelModel;
			reloadMode = false;
		}
		if(existingModel  == null) {
			modelRef = new CHREditorModel();
			matchCHRModel();
		} else {
			modelRef = existingModel;
		}


		setupUI();

		if(reloadMode){
			loadLastLevel(this, false);
		} 

		updateGUIControlsBasedOnEditorModel();

		pack();
		setLocation(0,0);
		updateEveryTile();
	}

	// declare "public"  so it can be called by reflection
	
	// declare "public"  so it can be called by reflection
	public void updateLevelTilesWide(Integer val){
		if(editorSettings.setTilesWide(val.intValue())){
			editorModel.setTilesWide(val.intValue());
		}
		matchModelRef();
	}
	// declare "public"  so it can be called by reflection
	public void updateLevelTilesHigh(Integer val){
		if(editorSettings.setTilesHigh(val.intValue())){
			editorModel.setTilesHigh(val.intValue());
		}
		matchModelRef();
	}
	// declare "public"  so it can be called by reflection
	public void updateLevelScreensWide(Integer val){
		if(editorSettings.setScreensWide(val.intValue())){
			editorModel.setScreensWide(val.intValue());
		}
		matchModelRef();
	}
	// declare "public"  so it can be called by reflection
	public void updateLevelScreensHigh(Integer val){
		if(editorSettings.setScreensHigh(val.intValue())){
			editorModel.setScreensHigh(val.intValue());
		}
		matchModelRef();
	}

	private void matchModelRef(){
		if(modelRef != null){
			matchCHRModel();
		} else {
			if(levelPanel != null){
				levelPanel.setShowTileGrid(editorSettings.getShowTileGrid());
				levelPanel.setShowOAMGrid(editorSettings.getShowOAMGrid());
				levelPanel.setShowPageGrid(editorSettings.getShowScreenBounds());
				levelPanel.notifyDisplayInterfaceUpdated();
			}
		}
	}
	// declare "public"  so it can be called by reflection
	public void updateLevelTilesScale(Integer val){
		editorSettings.setScale(val.intValue());
		levelPanel.setScale(val.intValue());
	}



	private void matchCHRModel(){
		if(modelRef != null){
			int numPgs = (editorModel.getScreensWide()*editorModel.getScreensHigh());
			modelRef.getCHRModel().setNumPages( numPgs );
			modelRef.setNumNameTablePages( numPgs );
			if(levelPanel != null){
				levelPanel.updateScale();
			}
		}
	}

	// The rest is all GUI code


	private void setupUI(){
		getContentPane().setLayout(new BorderLayout());

		JSplitPane  splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		levelPanel = new LevelEditorNameTablePanel(this, modelRef, editorModel, editorSettings);
		setupMenuBar();

		{
			JPanel subLevelPanel  = new JPanel();
			subLevelPanel.setLayout(new BorderLayout());
			JPanel subLevelPanel2  = new JPanel();
			subLevelPanel2.setLayout(new BorderLayout());
			subLevelPanel.add(subLevelPanel2, BorderLayout.NORTH);
			subLevelPanel2.add(levelPanel, BorderLayout.WEST);

			JScrollPane levelPane = new JScrollPane(subLevelPanel);
			Dimension d = levelPanel.getPreferredSize();
			if(d.width > 500){
				d.width = 500;
			}
			levelPane.setPreferredSize(d);
			levelPane.setMaximumSize(levelPanel.getMaximumSize());

		}

		tabPanel = new JTabbedPane();


		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new BorderLayout());
		controlsPanel.add(setupSettingsPanel(), BorderLayout.NORTH);
		//  System.out.println("controlsPanel" + controlsPanel.getMinimumSize());
		tabPanel.addTab("Controls",controlsPanel);

		paletteRegion = new PalettePanel(this, modelRef.getCHRModel(), true, true);
		paletteRegion.setBorder(new EmptyBorder(0,0,0,0));
		tabPanel.addTab("Palette", paletteRegion);
		//  System.out.println("paletteRegion" + paletteRegion.getMinimumSize());

		bgStampPanel = new PatternTableManagementPanel(levelPanel, this, modelRef, "BG", PPUConstants.IMAGE_PALETTE_TYPE);
		tabPanel.addTab("Backgrounds",bgStampPanel);
		//  System.out.println("bgStampPanel" + bgStampPanel.getMinimumSize());

		spriteStampPanel = new PatternTableManagementPanel(levelPanel, this, modelRef, "Sprite", PPUConstants.SPRITE_PALETTE_TYPE);
		tabPanel.addTab("Sprites",spriteStampPanel);

		splitPane.setLeftComponent(tabPanel);
		JScrollPane panelScrollPane = new JScrollPane(levelPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		panelScrollPane.setPreferredSize(new Dimension(32*8*2,30*8*2));
		splitPane.setRightComponent(panelScrollPane);
	}
	private void setupMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		setupFileMenu(menuBar);
		if(isViewControlsEnabled()) {
			setupViewMenu(menuBar);
		}
		setupGraphicsMenu(menuBar);
		setupStampsMenu(menuBar);
	}
	
	private void setupFileMenu(JMenuBar menuBar){
		JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');
		final Component parent = this;
		
		GUIUtilities.createMenuItem(fileMenu, "Load Level ", 'L', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				loadLevel(parent);
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Save Level ", 'S', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				saveLevel(parent, getLevelID());
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Save Level As.. ", 'A', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				saveAsLevel(parent, getLevelID());
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Open Enclosing Folder", 'O', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				openLevelEnclosingFolder();
			}
		});
		
		/*
		GUIUtilities.createMenuItem(fileMenu, "Create Test ROM ", 'C', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				createTestROM();
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Extract from Test ROM ", 'x', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				extractFromTestROM();
			}
		});
		*/
	}
	
	protected void setShowTileGrid(boolean flag) {
		editorSettings.setShowTileGrid(flag);
		levelPanel.setShowTileGrid(flag);
		levelPanel.notifyDisplayInterfaceUpdated();		
	}
	protected void setShowOAMGrid(boolean flag) {
		editorSettings.setShowOAMGrid(flag);
		levelPanel.setShowOAMGrid(flag);
		levelPanel.notifyDisplayInterfaceUpdated();	
	}
	protected void setShowScreenBounds(boolean flag) {
		editorSettings.setShowScreenBounds(flag);
		levelPanel.setShowPageGrid(flag);
		levelPanel.notifyDisplayInterfaceUpdated();	
	}
	
	private void setupViewMenu(JMenuBar menuBar){
		JMenu viewMenu = GUIUtilities.createMenu(menuBar, "View", 'V');

		GUIUtilities.createCheckboxMenuItem(viewMenu, "Screen Tile Grid", 'T', editorSettings.getShowTileGrid(), new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setShowTileGrid(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		GUIUtilities.createCheckboxMenuItem(viewMenu, "Show OAM Grid", 'O', editorSettings.getShowOAMGrid(), new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setShowOAMGrid(e.getStateChange() == ItemEvent.SELECTED);
				
			}
		});
		GUIUtilities.createCheckboxMenuItem(viewMenu, "Screen Bounds", 'B', editorSettings.getShowScreenBounds(), new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setShowScreenBounds(e.getStateChange() == ItemEvent.SELECTED);
				
			}
		});
	}
	private void setupStampsMenu(JMenuBar menuBar){
		JMenu  stampsMenu = GUIUtilities.createMenu(menuBar, "Stamps", 'S');

		GUIUtilities.createMenuItem(stampsMenu, "Load Stamp Set for Background", 'B', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				// warn in case there are existing stamps
				// if load successful
				tabPanel.setSelectedIndex(2);
			}
		}
		);

		GUIUtilities.createMenuItem(stampsMenu, "Load Stamp Set for Sprites", 'S', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				// warn in case there are existing stamps
				tabPanel.setSelectedIndex(3);

			}
		}
		);
	}

	private void setupGraphicsMenu(JMenuBar menuBar){
		JMenu graphicsMenu = GUIUtilities.createMenu(menuBar, "Graphics", 'G');

		GUIUtilities.createMenuItem(graphicsMenu, "View In Screen Layout Tool", 'V', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				viewInScreenLayoutTool();
			}
		}
		);


		JMenu patternMenu = GUIUtilities.createSubMenu(graphicsMenu, "CHR Tiles", 'C');
		JMenu allPatternMenu = GUIUtilities.createSubMenu(patternMenu, "Background & Sprite (8K)", '&');


		final Component comp = this;
		final NESModelListener listener = this;

		GUIUtilities.createMenuItem(allPatternMenu, "Load", 'L', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.loadPatternTables(comp, listener);
			}
		}
		);
		GUIUtilities.createMenuItem(allPatternMenu, "Save", 'S', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.savePatternTables(comp, listener);
			}
		}
		);
		GUIUtilities.createMenuItem(allPatternMenu, "Save As...", 'A', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.saveAsPatternTables(comp, listener);
			}
		}
		);

		JMenu bgPatternMenu = GUIUtilities.createSubMenu(patternMenu, "Background Only (4K)", 'B');
		/*     GUIUtilities.createMenuItem(bgPatternMenu, "Load", 'L', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Not supported");
            }
        }
        );
		 */
		GUIUtilities.createMenuItem(bgPatternMenu, "Save", 'S', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.savePatternTableBankByIndex(comp, listener, 0);
			}
		}
		);
		GUIUtilities.createMenuItem(bgPatternMenu, "Save As...", 'A', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.savePatternTableBankByIndex(comp, listener, 0);
			}
		}
		);



		JMenu sprPatternMenu = GUIUtilities.createSubMenu(patternMenu, "Sprite Only (4K)", 'S');
		/*
        GUIUtilities.createMenuItem(sprPatternMenu, "Load", 'L', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Not supported");
            }
        }
        );
		 */ 
		GUIUtilities.createMenuItem(sprPatternMenu, "Save", 'S', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.savePatternTableBankByIndex(comp, listener, 1);
			}
		}
		);
		GUIUtilities.createMenuItem(sprPatternMenu, "Save As...", 'A', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.savePatternTableBankByIndex(comp, listener, 1);
			}
		}
		);



		JMenu paletteMenu = GUIUtilities.createSubMenu(graphicsMenu, "Palette", 'P');
		GUIUtilities.createMenuItem(paletteMenu, "Load", 'L', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.loadPalette(comp, listener);
			}
		}
		);

		GUIUtilities.createMenuItem(paletteMenu, "Save", 'S', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.savePalette(comp);
			}
		}
		);
		GUIUtilities.createMenuItem(paletteMenu, "Save As...", 'A', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.saveAsPalette(comp);
			}
		}
		);

		JMenu partialPalMenu = GUIUtilities.createSubMenu(paletteMenu, "Partial", 'P');
		GUIUtilities.createMenuItem(partialPalMenu, "Save Background Palette", 'B', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.saveSpecificPalette(comp, listener, PPUConstants.IMAGE_PALETTE_TYPE, 0, PPUConstants.NES_IMAGE_PALETTE_SIZE);
			}
		}
		);
		GUIUtilities.createMenuItem(partialPalMenu, "Save Sprite Palette", 'S', new ActionListener()  {
			public void actionPerformed(ActionEvent e) {
				modelRef.saveSpecificPalette(comp, listener, PPUConstants.SPRITE_PALETTE_TYPE, 0, PPUConstants.NES_SPRITE_PALETTE_SIZE);
			}
		}
		);
	}

	private void viewInScreenLayoutTool(){
		if(modelRef == null){
			return;
		}
		// launch a NEW CHR Layout tool
		ScreenLayoutUI frame = new ScreenLayoutUI(modelRef);
		frame.setVisible(true); //necessary as of 1.3
		getParent().add(frame);
		try {
			frame.setSelected(true);
		} catch (java.beans.PropertyVetoException ex) {
			ex.printStackTrace();
		}
	}


	protected boolean tilesWideVisible() {
		return true;
	}
	
	protected boolean tilesHighVisible() {
		return true;
	}
	
	protected boolean screensWideVisible() {
		return true;
	}
	
	protected boolean screensHighVisible() {
		return true;
	}
	protected boolean screenScaleVisible() {
		return  true;
	}
	protected boolean isViewControlsEnabled() {
		return true;
	}
	
	
	private JPanel  setupSettingsPanel() {
		JPanel controlPanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		controlPanel.setLayout(gbl);
		controlPanel.setBorder(new TitledBorder("Settings"));

		int yPos = 0;

		Class<?> intClass[] = {Integer.class};
		try {
			tilesWideSpinner = GUIUtilities.addCustomSpinner(this, gbc, gbl, controlPanel, tilesWideVisible(), yPos,
					LevelEditorSettings.LEVEL_TILES_WIDE_TITLE, LevelEditorSettings.LEVEL_TILES_WIDE_TOOLTIP,
					editorSettings.getTilesWide(),
					LevelEditorSettings.MIN_LEVEL_EDITOR_LEVEL_TILES_WIDE, LevelEditorSettings.MAX_LEVEL_EDITOR_LEVEL_TILES_WIDE, LevelEditorSettings.STEP_LEVEL_EDITOR_LEVEL_TILES_WIDE,
					getClass().getMethod("updateLevelTilesWide",intClass));
			yPos++;
		} catch(Exception e){
			e.printStackTrace();
		}
		try {
			tilesHighSpinner = GUIUtilities.addCustomSpinner(this, gbc, gbl, controlPanel, tilesHighVisible(), yPos,
					LevelEditorSettings.LEVEL_TILES_HIGH_TITLE,  LevelEditorSettings.LEVEL_TILES_HIGH_TOOLTIP,
					editorSettings.getTilesHigh(),
					LevelEditorSettings.MIN_LEVEL_EDITOR_LEVEL_TILES_HIGH, LevelEditorSettings.MAX_LEVEL_EDITOR_LEVEL_TILES_HIGH,  LevelEditorSettings.STEP_LEVEL_EDITOR_LEVEL_TILES_HIGH,
					getClass().getMethod("updateLevelTilesHigh",intClass));
			yPos++;
		} catch(Exception e){
			e.printStackTrace();
		}
		try {
			screensWideSpinner = GUIUtilities.addCustomSpinner(this, gbc, gbl, controlPanel, screensWideVisible(), yPos,
					LevelEditorSettings.LEVEL_SCREENS_WIDE_TITLE,  LevelEditorSettings.LEVEL_SCREENS_WIDE_TOOLTIP,
					editorSettings.getScreensWide(),
					LevelEditorSettings.MIN_LEVEL_EDITOR_LEVEL_SCREENS_WIDE, LevelEditorSettings.MAX_LEVEL_EDITOR_LEVEL_SCREENS_WIDE,  LevelEditorSettings.STEP_LEVEL_EDITOR_LEVEL_SCREENS_WIDE,
					getClass().getMethod("updateLevelScreensWide",intClass));
			yPos++;
		} catch(Exception e){
			e.printStackTrace();
		}
		try {
			screensHighSpinner = GUIUtilities.addCustomSpinner(this, gbc, gbl, controlPanel, screensHighVisible(), yPos,
					LevelEditorSettings.LEVEL_SCREENS_HIGH_TITLE,  LevelEditorSettings.LEVEL_SCREENS_HIGH_TOOLTIP,
					editorSettings.getScreensHigh(),
					LevelEditorSettings.MIN_LEVEL_EDITOR_LEVEL_SCREENS_HIGH, LevelEditorSettings.MAX_LEVEL_EDITOR_LEVEL_SCREENS_HIGH,  LevelEditorSettings.STEP_LEVEL_EDITOR_LEVEL_SCREENS_HIGH,
					getClass().getMethod("updateLevelScreensHigh",intClass));
			yPos++;
		} catch(Exception e){
			e.printStackTrace();
		}

		try {

			scaleSpinner = GUIUtilities.addCustomSpinner(this, gbc, gbl, controlPanel, screenScaleVisible(), yPos,
					LevelEditorSettings.LEVEL_TILES_SCALE_TITLE, LevelEditorSettings.LEVEL_TILES_SCALE_TOOLTIP,
					editorSettings.getScale(),
					LevelEditorSettings.MIN_LEVEL_EDITOR_LEVEL_TILES_SCALE, LevelEditorSettings.MAX_LEVEL_EDITOR_LEVEL_TILES_SCALE, LevelEditorSettings.STEP_LEVEL_EDITOR_LEVEL_TILES_SCALE,
					getClass().getMethod("updateLevelTilesScale",intClass));
			yPos++;
		} catch(Exception e){
			e.printStackTrace();
		}
		controlPanel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0,yPos));
		return controlPanel;
	}


	public boolean loadLastLevel(Component parentComponent, boolean selectMode) {
		String str = editorSettings.getLastLevelFileName();
		if(!selectMode && (str == null || str.trim().length() == 0)){
			return false;
		}
		if(str == null || str.trim().length() == 0){ 
			return loadLevel(parentComponent);
		}
		File selectedFile = new File(str);
		boolean workIt = false;
		try {
			if(selectedFile.canRead()) {
				workIt = true;
			}
		} catch (Exception e){
			workIt = false;
		}
		if(workIt){
			return loadLevelFromFile(selectedFile);
		} else if(selectMode) {
			return loadLevel(parentComponent);
		} else {
			return false;
		}
	}

	public boolean loadLevel(Component parentComponent){
		File selectedFile = FileUtilities.selectFileForOpen(parentComponent);
		if(selectedFile == null){
			return false;
		}
		return loadLevelFromFile(selectedFile);
	}

	public String getFrameTitle() {
		return FRAME_TITLE;
	}
	public boolean loadLevelFromFile(File selectedFile){
		try {
			editorModel.loadFile(selectedFile);			
			editorSettings.setLastLevelFileName(selectedFile.getAbsolutePath());            			
			updateGUIControlsBasedOnEditorModel();			
			setTitle(getFrameTitle() + " for: " + selectedFile.getName());			
			matchCHRModel();
			
			// bring in the palette
			String paletteFileName = editorModel.getPaletteFileName();
			if(paletteFileName != null) {
				modelRef.loadAsciiPaletteFromFile(new File(selectedFile.getParentFile(), paletteFileName), this);
			}
			
			
			// bring in the stamps and their meta info		
			String bgStampFileName = editorModel.getBGStampsFileName();
			if(bgStampFileName != null) {
				bgStampPanel.loadStamps(editorModel.getBGStampsFileName());
			}
			
			String spriteStampFileName = editorModel.getSpriteStampsFileName();
			if(spriteStampFileName != null) {
				spriteStampPanel.loadStamps(editorModel.getSpriteStampsFileName());
			}

			String columnFileName = editorModel.getColumnFileName();
			if(columnFileName != null) {
				levelPanel.loadAsciiColumnsFromFile(new File(selectedFile.getParentFile(), columnFileName));
			}

			return true;
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Load last level not properly implemented yet");
			// JOptionPane.showMessageDialog(this,"Unable to load the level from file:" + selectedFile, "Failed to Load Level", JOptionPane.ERROR_MESSAGE);
			// editorSettings.setLastLevelFileName(null);            
			// e.printStackTrace();
			return false;
		}
	}

	public void createTestROM(){
		if(modelRef == null){
			return;
		}
		try {
			LevelEditorTestRomHelper.createTestROM(modelRef, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void extractFromTestROM(){

		try {
			if(LevelEditorTestRomHelper.extractFromTestROM(modelRef, this)){
				ScreenLayoutUI frame = new ScreenLayoutUI(modelRef);
				frame.setVisible(true); //necessary as of 1.3
				getParent().add(frame);
				frame.setSelected(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public boolean saveLevel(Component parentComponent, int levelID){
		String str = editorSettings.getLastLevelFileName();
		if(str == null){
			return saveAsLevel(parentComponent, levelID);
		}
		File selectedFile = new File(str);

		boolean workIt = false;
		try {
			if(selectedFile.canWrite()) {
				workIt = true;
			}
		} catch (Exception e){
			workIt = false;
		}
		if(workIt){
			return saveLevelToFile(selectedFile, levelID);
		} else {
			return saveAsLevel(parentComponent, levelID);
		}
	}
	
	
	
	
	
	private void openLevelEnclosingFolder() {
		try {
			Desktop desktop = Desktop.getDesktop();
			String lastLevelFileName = editorSettings.getLastLevelFileName();
			if(lastLevelFileName != null && lastLevelFileName.length() > 0) {
				File levelFile = new File(lastLevelFileName);
				if(levelFile != null && levelFile.canRead()) {
					File dirFile = levelFile.getParentFile();
					if(dirFile != null && dirFile.isDirectory()) {
						desktop.open(dirFile);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public boolean saveAsLevel(Component parentComponent, int levelID){
		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		return saveLevelToFile(selectedFile, levelID);
	}


	private int getLevelID() {
		System.err.println("getLevelID needs to be implemented");
		return 1;
	}
	
	private boolean saveLevelToFile(File selectedFile, int levelID){
		try {
			String levelName = "Level_" + levelID;
			
			String paletteFileName = levelName + ".palette";
			editorModel.setPaletteFileName(paletteFileName);
			String columnFileName = levelName + ".columns";
			editorModel.setColumnFileName(columnFileName);
			
			
			String bgStampFileName = levelName + ".bgStamps";
			editorModel.setBGStampsFileName(bgStampFileName);			
			bgStampPanel.saveStamps(bgStampFileName);

			String spriteStampFileName = levelName + ".sprStamps";
			editorModel.setSpriteStampsFileName(spriteStampFileName);			
			spriteStampPanel.saveStamps(spriteStampFileName);
			
			
			editorModel.storeFile(selectedFile);
			editorSettings.setLastLevelFileName(selectedFile.getAbsolutePath());
			setTitle(FRAME_TITLE + " for: " + selectedFile.getName());

			// three files
			File levelFile  = new File(selectedFile.getParentFile(), levelName + ".lvl");
			FileWriter wr = new FileWriter(levelFile);

			String cr = System.getProperty("line.separator");
/*
 * Example of a Level
   .byt $05 ; Tile Bank
   .byt $F0 ; Num CHR Tiles to load into $0000.  $F0 is the MAX
   .addr level2Tiles ; Address of CHR Tiles to load into $0000
   .addr MetaTileSet2 ; Address of MetaTile Set to use
   .byt $8 ; Num screens.  16 oam columns per screen, 16 bytes per column.
   .addr Level2Columns ; Address of columns used by this level
 */
			int numScreens = editorModel.getScreensWide();
			wr.write("; Level: " + selectedFile.getAbsolutePath() + cr);
			wr.write(levelName + ":" + cr);
			wr.write(".byt $" + ByteFormatter.formatByte((byte)0)      + " ; Tilebank.  Change this to whichever bank is used for the tiles" + cr);
			
			int numTiles = bgStampPanel.getNumTiles();
			if(numTiles > 0xF0){
				System.err.println("Level Editor only supports 0xF0 tiles");
			}
			wr.write(".byt $" + ByteFormatter.formatByte((byte)(numTiles&0xFF)) + " ; Number of CHR Tiles to load. F0 is the max" + cr);
			wr.write(".addr " + levelName + "Tiles ; Address of CHR Tiles to loadinto $0000" + cr);
			wr.write(".addr " + levelName + "TileSet ; Address of MetaTileSet to use" + cr);
			wr.write(".byt $" + ByteFormatter.formatByte((byte)numScreens)      + " ; Num Level screens. " + cr);
			wr.write(".addr " + levelName + "Columns ; Address of columns used by this level" + cr);
			
			// putting the columns in the same file
			wr.write(cr);
			wr.write("; -=-=-=-=-=-=-=-=-=-=-=-=-=-=-= " + cr);
			wr.write("; This section is the palette" + cr);
			wr.write(".include " +  levelName + ".palette" + cr);
			
			wr.write(cr);			
			wr.write("; -=-=-=-=-=-=-=-=-=-=-=-=-=-=-= " + cr);
			wr.write("; This section are the metatiles" + cr);
			wr.write(".include " +  levelName + ".metatiles" + cr);
			
			wr.write(cr);
			wr.write("; -=-=-=-=-=-=-=-=-=-=-=-=-=-=-= " + cr);
			wr.write("; This section are the tiles" + cr);
			wr.write(".incbin " +  levelName + ".tiles" + cr);

			wr.write(cr);
			wr.write("; -=-=-=-=-=-=-=-=-=-=-=-=-=-=-= " + cr);
			wr.write("; This section are the columns." + cr);
			wr.write(".include " +  levelName + ".columns" + cr);
			wr.close();

			File paletteFile  = new File(selectedFile.getParentFile(), paletteFileName);
			wr = new FileWriter(paletteFile);
			wr.write(levelName + "Palette:" + cr);
			wr.write(".byt ");
			for(int i=0;i<16;i++){
				if(i != 0){
					wr.write(", ");
				}
				wr.write("$" + ByteFormatter.formatByte(modelRef.getCHRModel().getImagePaletteAtIndex(i)));
			}
			wr.write(cr);
			wr.write(".byt ");
			for(int i=0;i<16;i++){
				if(i != 0){
					wr.write(", ");
				}
				wr.write("$" + ByteFormatter.formatByte(modelRef.getCHRModel().getSpritePaletteAtIndex(i)));
			}
			wr.write(cr);
			wr.close();
			
			
			File columnFile  = new File(selectedFile.getParentFile(), levelName + ".columns");
			wr = new FileWriter(columnFile);
			wr.write(levelName + "Columns:" + cr);

			// At the moment I only support 16 columns per page and 16 entries per column.
			int hgt = 16;
			int wid = 16;
			// We write the data  one entire row at a time   (this may change as the engine develops)
			for(int l=0;l<numScreens;l++) {   
				for(int x=0;x<wid;x++){
					for(int y=0;y<hgt;y++){   
						if(y == 0){
							wr.write(".byt ");
						} else {
							wr.write(", ");
						}
						wr.write("$" + ByteFormatter.formatByte((byte)levelPanel.getMetaTileIndex(l,x,y)));                    
					}
					wr.write(cr);
				}
				wr.write(cr);
			}
			wr.close();

			
			File tileSetFile  = new File(selectedFile.getParentFile(), levelName + ".metatiles");
			wr = new FileWriter(tileSetFile);
			wr.write("; The MetaTileSet for this level" + cr);
			wr.write(levelName + "TileSet:" + cr);
			Iterator<MetaTile> metaTileIter = levelPanel.getMetaTiles();
			while(metaTileIter.hasNext()) {
				MetaTile mtile = metaTileIter.next();
				mtile.writeToFile(wr);
			}
			wr.close();

			// third file (the tiles) is binary
			File tileFile  = new File(selectedFile.getParentFile(), levelName + ".tiles");
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tileFile));
			for(int i=0;i<numTiles;i++) {
				CHRTile tile = modelRef.getPatternTableTile(0,i);
				byte mask[] = tile.asMask();
				bos.write(mask, 0, 16);
			}
			bos.flush();
			bos.close();
			
			return true;
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}


	protected void updateGUIControlsBasedOnEditorModel(){
		tilesWideSpinner.getModel().setValue( new Integer(editorModel.getTilesWide()));
		tilesHighSpinner.getModel().setValue( new Integer(editorModel.getTilesHigh()));
		screensWideSpinner.getModel().setValue( new Integer(editorModel.getScreensWide()));
		screensHighSpinner.getModel().setValue( new Integer(editorModel.getScreensHigh()));
		scaleSpinner.getModel().setValue( new Integer(1));
	}

	private void updateEveryTile() {
		if(bgStampPanel != null){
			bgStampPanel.updatePatternTable();
			bgStampPanel.updateStamps();
		}
		if(spriteStampPanel != null){
			spriteStampPanel.updatePatternTable();
			spriteStampPanel.updateStamps();
		}

		if(levelPanel != null){
			levelPanel.updateNameTableTiles(true);
			levelPanel.notifyDisplayInterfaceUpdated();
		}
	}

	private void updateEverything() {
		if(paletteRegion != null) {
			paletteRegion.resetPalette();
		}
		updateEveryTile();
	}

	// NESModelListener methods
	public void notifyImagePaletteChanged(){
		updateEverything();
	}
	public void notifySpritePaletteChanged(){
		updateEverything();
	}

	public void notifyPatternTableChanged(){
		updateEveryTile();
	}

	public void notifyNameTableChanged(){
		System.out.println("Name Table Changed. Ignored by Level Editor");
	}

	public void notifyPatternTableSelected(int pageNum, int index) {
		System.out.println("notifyPatternTableSelected " + pageNum + " " + index + " Ignored by Level Editor");
		//        modelRef.lastPageNum = pageNum;
		//        modelRef.lastPatternIndex = index;
		//        levelPanel.updateNameTableTiles(true);
	}

	public void notifyPatternTableToBeModified(int pageNum, int index){
		System.out.println("notifyPatternTableToBeModified " + pageNum + " " + index + "Ignored by Level Editor");
		//        modelRef.lastPageNum = pageNum;
		//        modelRef.lastPatternIndex = index;
		//        levelPanel.updateNameTableTiles(true);
	}







}