/*
 * StampEditor.java
 *
 * Created on August 17, 2008, 10:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampEditor;

import gameTools.stampSetEditor.ActiveStampObserver;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import model.NESModelListener;
import ui.chr.CHRDisplayControls;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.palette.PalettePanel;
import ui.chr.tileEditor.CHRMultiTilePanel;
import utilities.EnvironmentUtilities;
import utilities.GUIUtilities;

public class StampEditor extends JInternalFrame implements NESModelListener, StampSettings {

	private static final long serialVersionUID = -5125199944752852118L;
	
	public final static String FRAME_TITLE = "Stamp Editor";
	
	private final static String STAMP_EDITOR_LAST_FILE_PROPERTY = "STAMP_EDITOR_LAST_FILE_PROPERTY";

	// these object types are tied to the engne.
	// TO DO: support loading these in from a config file.
	private final static String OBJECT_TYPES[] = { 
		"AIR", "COVER", "PUSH",  "WATER", "DOOR", "STAIRS", "CARRIER", "TRIGGER", "PLATFORM", "SOLID", "SLIPPY", "CLING", "PAIN" 
	};
	private final static String OBJECT_TYPE_TOOLTIPS[] = {
		"AIR", "COVER", "PUSH",  "WATER", "DOOR", "STAIRS", "CARRIER", "TRIGGER", "PLATFORM", "SOLID", "SLIPPY", "CLING", "PAIN" 
	};

	
	public static final String SHOW_TILE_GRID_TITLE = "Show Tile Grid";
	public static final String SHOW_TILE_GRID_TOOLTIP = "Show grid for every tile";
	public static final String STAMP_EDITOR_SHOW_TILE_GRID = "STAMP_EDITOR_SHOW_TILE_GRID";
	public static final boolean DEFAULT_STAMP_EDITOR_SHOW_TILE_GRID = false;

	public static final String SHOW_OAM_GRID_TITLE = "Show OAM Grid";
	public static final String SHOW_OAM_GRID_TOOLTIP = "Show grid to indicate OAM region";
	public static final String STAMP_EDITOR_SHOW_OAM_GRID = "STAMP_EDITOR_SHOW_OAM_GRID";
	public static final boolean DEFAULT_STAMP_EDITOR_SHOW_OAM_GRID = true;

	public static final String STAMP_TILES_NUM_WIDE_TITLE = "Tiles Wide";
	public static final String STAMP_TILES_NUM_WIDE_TOOLTIP = "Width (in tiles) of the stamp.";
	public static final String STAMP_EDITOR_STAMP_TILES_NUM_WIDE = "STAMP_EDITOR_STAMP_TILES_NUM_WIDE";
	public static final int DEFAULT_STAMP_EDITOR_STAMP_TILES_NUM_WIDE = 2;
	public static final int MIN_STAMP_EDITOR_STAMP_TILES_NUM_WIDE = 1;
	public static final int MAX_STAMP_EDITOR_STAMP_TILES_NUM_WIDE = 32;
	public static final int STEP_STAMP_EDITOR_STAMP_TILES_NUM_WIDE = 1;

	public static final String STAMP_TILES_NUM_HIGH_TITLE = "Tiles High";
	public static final String STAMP_TILES_NUM_HIGH_TOOLTIP = "Height (in tiles) of the stamp.";
	public static final String STAMP_EDITOR_STAMP_TILES_NUM_HIGH = "STAMP_EDITOR_STAMP_TILES_NUM_HIGH";
	public static final int DEFAULT_STAMP_EDITOR_STAMP_TILES_NUM_HIGH = 2;
	public static final int MIN_STAMP_EDITOR_STAMP_TILES_NUM_HIGH = 1;
	public static final int MAX_STAMP_EDITOR_STAMP_TILES_NUM_HIGH = 30;
	public static final int STEP_STAMP_EDITOR_STAMP_TILES_NUM_HIGH = 1;

	public static final String STAMP_TILES_SCALE_TITLE = "Scale";
	public static final String STAMP_TILES_SCALE_TOOLTIP = "Amount to scale each tile to make editing easier. This is viewer only and does not affect the data.";
	public static final String STAMP_EDITOR_STAMP_TILES_SCALE = "STAMP_EDITOR_STAMP_TILES_SCALE";
	public static final int DEFAULT_STAMP_EDITOR_STAMP_TILES_SCALE = 1;
	public static final int MIN_STAMP_EDITOR_STAMP_TILES_SCALE = 1;
	public static final int MAX_STAMP_EDITOR_STAMP_TILES_SCALE = 10;
	public static final int STEP_STAMP_EDITOR_STAMP_TILES_SCALE = 1;

	private CHREditorModel _modelRef = null;
	private CHRMultiTilePanel _stamp = null;
	private CHRMultiTilePanel _sector = null;
	private CHRMultiTilePanel _masterStamp = null;

	private CHRDisplayControls _stampControls = null;
	private PalettePanel _palettePanel = null;
	private ButtonGroup oamButtonGroup = new ButtonGroup();
	private ButtonGroup objectTypeButtonGroup = new ButtonGroup();
	private JTextField _activeSectorField = null;
	private ActiveStampObserver _observer;
	private int _activeSectorIndex = 0;

	public StampEditor(CHRMultiTilePanel tilePanel, ActiveStampObserver observer) {
		super(FRAME_TITLE, true, true, false, false);
		_modelRef = new CHREditorModel();
		_observer = observer;
		if(tilePanel != null) {
			tilePanel.copyIntoPalette(_modelRef);
		}
		_masterStamp = tilePanel;

		_activeSectorIndex = 0;
		if (tilePanel == null) {
			_stampControls = new CHRDisplayControls("STAMP_EDITOR");
			_stamp = new CHRMultiTilePanel(2, 2, _modelRef, _stampControls);
			_sector = new CHRMultiTilePanel(2, 2, _modelRef, _stampControls);
			reloadPrefs();
		} else {
			_stamp = new CHRMultiTilePanel(tilePanel);
			_stampControls = _stamp.getControls();
			_sector = new CHRMultiTilePanel(2, 2, _modelRef, _stampControls);
		}
		setupUI();
		pack();
		setLocation(0, 0);
		_stamp.notifyDisplayInterfaceUpdated();
		setupMouseControls();
		processLeftClick(0,0,0);
	}	
	
	private void processLeftClick(int x, int y, int index) {
		_activeSectorIndex = index;
		_sector.setRegion(_stamp, x, y, index);
		updateGUIForCurrentSector();
	}

	private void processRightClick(int x, int y, int index) {
		_activeSectorIndex = index;
		_sector.setRegion(_stamp, x, y, index);
		updateGUIForCurrentSector();
	}

	protected void setupMouseControls() {
		// right click to set the color
		final int tilesWide = _stamp.getTilesWide();
		final int tilesHigh = _stamp.getTilesHigh();
		final int oamsWide = tilesWide / 2;
		_stamp.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1
						|| e.getButton() == MouseEvent.BUTTON3) {
					
					int xPos = e.getX();
					int sx = 2 * PPUConstants.CHR_WIDTH * _stampControls.getScale();
					if (xPos < 0 || xPos > sx * tilesWide) {
						// ignore it
						return;
					}
					int yPos = e.getY();
					int sy = 2 * PPUConstants.CHR_HEIGHT * _stampControls.getScale();
					if (yPos < 0 || yPos > sy * tilesHigh) {
						// ignore it
						return;
					}
					int relX = xPos / sx;
					int relY = yPos / sy;
					int index = (relY * oamsWide) + relX;
					if (e.getButton() == MouseEvent.BUTTON1) {
						processLeftClick(relX, relY, index);
					}
					if (e.getButton() == MouseEvent.BUTTON3) {
						processRightClick(relX, relY, index);
					}
				}
			}
		});
	}

	
	private void reloadPrefs() {
		String tmp = EnvironmentUtilities.getStringEnvSetting(
				STAMP_EDITOR_LAST_FILE_PROPERTY, null);
		if (tmp != null) {
			if (!_stamp.loadFromFile(0, 0, tmp)) {
				EnvironmentUtilities.updateStringEnvSetting(
						STAMP_EDITOR_LAST_FILE_PROPERTY, "");
			} else {
				postLoadStamp();
			}
		}
	}

	private void postLoadStamp() {
		EnvironmentUtilities.updateStringEnvSetting(
				STAMP_EDITOR_LAST_FILE_PROPERTY, _stamp.getFileName());
		updateGUIForCurrentSector();
		updateTitle();
	}

	private void updateTitle() {
		String fullString = _stamp.getFileName();
		if (fullString.length() > 10) {
			if (fullString.lastIndexOf(File.separatorChar) != -1) {
				String subString = fullString.substring(fullString
						.lastIndexOf(File.separatorChar) + 1);
				fullString = subString;
			}
		}
		setTitle(FRAME_TITLE + " for: " + fullString);
	}

	// I think if it looks a bit like Paint, I'd be happy
	private void setupUI() {
		setLayout(new BorderLayout());
		setupMenuBar();
		add(setupSettingsPanel(), BorderLayout.WEST);
		add(setupStampRegion(), BorderLayout.CENTER);
		add(setupSectorPanel(), BorderLayout.SOUTH);
	}

	private void updateGUIForCurrentSector() {
		if(_activeSectorField != null) {
			_activeSectorField.setText("" + _activeSectorIndex);
		}
		int curOam = 0;
		int sectorOAM  = _sector.getOAM(0);
		Enumeration<AbstractButton> e = oamButtonGroup.getElements();
		while (e.hasMoreElements()) {
			JRadioButton r = (JRadioButton) e.nextElement();
			if (curOam == sectorOAM) {
				oamButtonGroup.setSelected(r.getModel(), true);
				break;
			}
			curOam++;
		}
		// Also the drop down list
		int curObjectType = 0;
		int sectorObjType = _sector.getObjectType(0);
		System.out.println("Sector Object Type:"  + sectorObjType);
		e = objectTypeButtonGroup.getElements();
		while (e.hasMoreElements()) {
			JRadioButton r = (JRadioButton) e.nextElement();
			if (curObjectType == sectorObjType) {
				objectTypeButtonGroup.setSelected(r.getModel(), true);
				break;
			}
			curObjectType++;
		}
	}

	private void setSelectedOam(int oamIndex) {
		if (oamIndex != _sector.getOAM(0)) {
			_sector.setSectorOAM(0,oamIndex);
			if (_stamp != null) {
				_stamp.setSectorOAM(_activeSectorIndex, oamIndex);
			}
			if (_masterStamp != null) {
				_masterStamp.setSectorOAM(_activeSectorIndex, oamIndex);
				if(_observer != null){
					_observer.stampChanged(_masterStamp);
					_masterStamp.saveStamp();
				}
			}
		}
	}
	private void setObjectType(int objType){
		System.out.println("Setting Sector and Master OBject type to:" + objType);
		if (objType != _sector.getObjectType(0)) {
			_sector.setSectorObjectType(0,objType);
			if (_stamp != null) {
				_stamp.setSectorObjectType(_activeSectorIndex, objType);
			}
			if (_masterStamp != null) {
				_masterStamp.setSectorObjectType(_activeSectorIndex, objType);
				if(_observer != null){
					_observer.stampChanged(_masterStamp);
					_masterStamp.saveStamp();
				}
			}
		}
	}


	private void setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final Component parent = this;
		final NESModelListener list = this;

		JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');
		GUIUtilities.createMenuItem(fileMenu, "Load", 'L',
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadStamp();
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Save", 'S',
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveStamp();
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Save As", 'A',
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveStampAs();
			}
		});

		JMenu paletteMenu = GUIUtilities.createMenu(menuBar, "Palette", 'P');
		GUIUtilities.createMenuItem(paletteMenu, "Load", 'L',
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_modelRef.loadPalette(parent, list);
			}
		});

	}

	private void loadStamp() {
		if (_stamp.loadFromFile(0, 0)) {
			postLoadStamp();
		}
	}

	private void saveStamp() {
		_stamp.saveStamp();
	}

	private void saveStampAs() {
		_stamp.saveStampAs();
	}

	
	private JPanel setupSectorPanel() {
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Sector Settings"));
		p.setLayout(new BorderLayout());
		
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new GridLayout(4, (OBJECT_TYPES.length/4)+1));
		radioPanel.setBorder(new TitledBorder("Object Type"));
		{
			
			for(int i=0;i<OBJECT_TYPES.length;i++){
				JRadioButton b0 = new JRadioButton(OBJECT_TYPES[i]);
				b0.setToolTipText(OBJECT_TYPE_TOOLTIPS[i]);
				objectTypeButtonGroup.add(b0);
				radioPanel.add(b0);
				final int objIndex = i;
				b0.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							setObjectType(objIndex);
						}
					}
				});
			}
		}
		JPanel objTypePanel = new JPanel();
		objTypePanel.setLayout(new BorderLayout());
		objTypePanel.add(new JScrollPane(radioPanel), BorderLayout.CENTER);
		
		
		JPanel sectorPanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		sectorPanel.setLayout(gbl);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		int xPos = 0;
		int yPos = 0;
		JLabel sectorLabel = GUIUtilities.createLabel("Sector", "Active Sector", gbc, gbl, xPos, yPos);
		yPos++;
		sectorPanel.add(sectorLabel);

		_activeSectorField = GUIUtilities.createTextField("?", "Current Sector", 4, false, gbc, gbl, xPos, yPos);
		yPos++;
		sectorPanel.add(_activeSectorField);
		
		yPos++;
	    gbc.gridx = xPos;
        gbc.gridy = yPos;
        gbl.setConstraints(_sector, gbc);           
		sectorPanel.add(_sector);

		xPos++;
		sectorPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, xPos, yPos));
		xPos = 0;
		yPos++;
		sectorPanel.add(GUIUtilities.createFillerHeight(gbc, gbl, xPos, yPos));
		
		
		p.add(sectorPanel, BorderLayout.CENTER);

		p.add(objTypePanel, BorderLayout.SOUTH);

		p.add(setupPalettePanel(), BorderLayout.EAST);
		
		
		return p;
	}
	
	private JPanel setupPalettePanel() {
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.setBorder(new TitledBorder("OAM"));
		JPanel radioPanel = new JPanel();
		{
			JRadioButton b0 = new JRadioButton("OAM 0");
			oamButtonGroup.add(b0);
			radioPanel.add(b0);

			JRadioButton b1 = new JRadioButton("OAM 1");
			oamButtonGroup.add(b1);
			radioPanel.add(b1);

			JRadioButton b2 = new JRadioButton("OAM 2");
			oamButtonGroup.add(b2);
			radioPanel.add(b2);

			JRadioButton b3 = new JRadioButton("OAM 3");
			oamButtonGroup.add(b3);
			radioPanel.add(b3);

			b0.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setSelectedOam(0);
					}
				}
			});
			b1.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setSelectedOam(1);
					}
				}
			});
			b2.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setSelectedOam(2);
					}
				}
			});
			b3.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setSelectedOam(3);
					}
				}
			});
			updateGUIForCurrentSector();
		}
		southPanel.add(radioPanel, BorderLayout.NORTH);
		
		_palettePanel = new PalettePanel(this, _modelRef.getCHRModel(), true,
				false, 16, 0, false, true, true);
		_palettePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		_palettePanel.removeClickListener(_palettePanel);

		southPanel.add(_palettePanel, BorderLayout.SOUTH);
		return southPanel;
	}
		
	
	private JPanel setupStampRegion() {
		JPanel panel1 = new JPanel();
		panel1.setBorder(new EmptyBorder(1, 1, 1, 1));
		panel1.setLayout(new BorderLayout());

		JPanel panel2 = new JPanel();
		panel2.setBorder(new EmptyBorder(1, 1, 1, 1));
		panel2.setLayout(new BorderLayout());

		panel1.add(panel2, BorderLayout.NORTH);
		panel1.add(new JPanel(), BorderLayout.CENTER);
		panel2.add(_stamp, BorderLayout.WEST);
		panel2.add(new JPanel(), BorderLayout.CENTER);

		return panel1;
	}

	private JPanel setupSettingsPanel() {
		JPanel p = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		p.setLayout(gbl);
		p.setBorder(new TitledBorder("Stamp Settings"));

		gbc.anchor = GridBagConstraints.WEST;
		Class<?> intClass[] = { Integer.class };
		Class<?> boolClass[] = { Boolean.class };
		int yPos = 0;
		try {
			JCheckBox cb = GUIUtilities.addCustomCheckBox(this, gbc, gbl, p,
					yPos, SHOW_TILE_GRID_TITLE, SHOW_TILE_GRID_TOOLTIP,
					STAMP_EDITOR_SHOW_TILE_GRID,
					DEFAULT_STAMP_EDITOR_SHOW_TILE_GRID,
					getClass().getMethod("updateMetaTilesGrid", boolClass));
			cb.setSelected(_stampControls.getShowTileGrid());
			yPos++;

			cb = GUIUtilities.addCustomCheckBox(this, gbc, gbl, p, yPos,
					SHOW_OAM_GRID_TITLE, SHOW_OAM_GRID_TOOLTIP,
					STAMP_EDITOR_SHOW_OAM_GRID,
					DEFAULT_STAMP_EDITOR_SHOW_OAM_GRID,
					getClass().getMethod("updateMetaTilesOAMGrid", boolClass));
			cb.setSelected(_stampControls.getShowOAMGrid());
			yPos++;

			GUIUtilities.addCustomSpinner(this, gbc, gbl, p, yPos,
					STAMP_TILES_SCALE_TITLE, STAMP_TILES_SCALE_TOOLTIP,
					_stampControls.getScale(),
					MIN_STAMP_EDITOR_STAMP_TILES_SCALE,
					MAX_STAMP_EDITOR_STAMP_TILES_SCALE,
					STEP_STAMP_EDITOR_STAMP_TILES_SCALE,
					getClass().getMethod("updateMetaTilesScale", intClass));
			yPos++;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return p;
	}

	public void updateMetaTilesScale(Integer val) {
		_stampControls.setScale(val.intValue());
		_stamp.notifyDisplayInterfaceUpdated();
	}

	public void updateMetaTilesGrid(Boolean val) {
		_stampControls.setShowTileGrid(val.booleanValue());
		_stamp.notifyDisplayInterfaceUpdated();
	}

	public void updateMetaTilesOAMGrid(Boolean val) {
		_stampControls.setShowOAMGrid(val.booleanValue());
		_stamp.notifyDisplayInterfaceUpdated();
	}

	public void updateNumTilesWide(Integer val) {
		_stamp.setTilesWide(val.intValue());
		_stamp.notifyDisplayInterfaceUpdated();
	}

	public void updateNumTilesHigh(Integer val) {
		_stamp.setTilesHigh(val.intValue());
		_stamp.notifyDisplayInterfaceUpdated();
	}

	// NESModelListener methods
	public void notifyImagePaletteChanged() {
		if (_palettePanel != null) {
			_palettePanel.resetPalette();
		}
		_stamp.notifyDisplayInterfaceUpdated();
	}

	public void notifySpritePaletteChanged() {
		if (_palettePanel != null) {
			_palettePanel.resetPalette();
		}
		_stamp.notifyDisplayInterfaceUpdated();
	}

	public void notifyPatternTableChanged() {
		_stamp.notifyDisplayInterfaceUpdated();
	}

	public void notifyNameTableChanged() {
		System.out.println("Name Table Changed. Ignored.");
	}

	public void notifyPatternTableSelected(int pageNum, int index) {
		System.out.println("notifyPatternTableSelected " + pageNum + " "
				+ index + " Ignored by Editor");
	}

	public void notifyPatternTableToBeModified(int pageNum, int index) {
		System.out.println("notifyPatternTableToBeModified " + pageNum + " "
				+ index + "Ignored by Editor");
	}

	// StampSettings
	public int getNumBricksX() {
		return _stamp.getTilesWide();
	}

	public int getNumBricksY() {
		return _stamp.getTilesHigh();
	}

	public int getBrickSizeX() {
		return PPUConstants.CHR_WIDTH;
	}

	public int getBrickSizeY() {
		return PPUConstants.CHR_HEIGHT;
	}

	public int getScaleX() {
		return _stampControls.getScale();
	}

	public int getScaleY() {
		return _stampControls.getScale();
	}

}
