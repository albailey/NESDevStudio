package gameTools.playerAnimations;

import java.awt.BorderLayout;


import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.UUID;

import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import model.NESModelListener;
import ui.chr.CHRDisplayControls;
import ui.chr.model.CHREditorModel;
import ui.chr.palette.PalettePanel;
import ui.chr.tileEditor.CHRMultiTilePanel;
import utilities.FileUtilities;
import utilities.GUIUtilities;



public class PlayerAnimator extends JInternalFrame implements NESModelListener {

	private static final long serialVersionUID = -1391929171734394812L;

	public final static String FRAME_TITLE = "Player Animator";
	
	public static final String SHOW_TILE_GRID_TITLE = "Show Tile Grid";
	public static final String SHOW_TILE_GRID_TOOLTIP = "Show grid for every tile";
	public static final String PLAYER_EDITOR_SHOW_TILE_GRID = "PLAYER_EDITOR_SHOW_TILE_GRID";
	public static final boolean DEFAULT_PLAYER_EDITOR_SHOW_TILE_GRID = false;
	
	
	public static final String SHOW_OAM_GRID_TITLE = "Show OAM Grid";
	public static final String SHOW_OAM_GRID_TOOLTIP = "Show grid to indicate OAM region";
	public static final String PLAYER_EDITOR_SHOW_OAM_GRID = "PLAYER_EDITOR_SHOW_OAM_GRID";
	public static final boolean DEFAULT_PLAYER_EDITOR_SHOW_OAM_GRID = false;
	
	public static final String STAMP_TILES_SCALE_TITLE = "Scale";
	public static final String STAMP_TILES_SCALE_TOOLTIP = "Amount to scale each tile to make editing easier. This is viewer only and does not affect the data.";
	public static final String PLAYER_EDITOR_STAMP_TILES_SCALE = "PLAYER_EDITOR_STAMP_TILES_SCALE";
	public static final int DEFAULT_PLAYER_EDITOR_STAMP_TILES_SCALE = 1;
	public static final int MIN_PLAYER_EDITOR_STAMP_TILES_SCALE = 1;
	public static final int MAX_PLAYER_EDITOR_STAMP_TILES_SCALE = 10;
	public static final int STEP_PLAYER_EDITOR_STAMP_TILES_SCALE = 1;
	
	private CHREditorModel _modelRef = null;
	private CHRMultiTilePanel _playerStamp = null;
	private CHRDisplayControls _stampControls = null;
	private PalettePanel _palettePanel = null;
	private PlayerAnimationModel _currentAnimation = null;
	
	public PlayerAnimator() {
		super(FRAME_TITLE, true, true, false, false);
		_modelRef = new CHREditorModel();
		_stampControls = new CHRDisplayControls("PLAYER_EDITOR");		
		_playerStamp = new CHRMultiTilePanel(2, 2, _modelRef, _stampControls);
		_currentAnimation = new PlayerAnimationModel();
		reloadPrefs();
		setupUI();
		pack();
		setLocation(0, 0);
		_playerStamp.notifyDisplayInterfaceUpdated();
		setupMouseControls();
		processLeftClick(0,0,0);
	}	
	
	
	
	private void reloadPrefs() {
		
		String foo = "A00A0000A00000A00000F000009000000";
		
		int counter = 0;
		byte daBytes[] = new byte[16];
		byte daOtherBytes[] = new byte[16];
		for(int i=0;i<32;i+=2){
			byte b1 = Byte.parseByte(foo.substring(i,i+1),16);
			byte b2 = Byte.parseByte(foo.substring(i+1,i+2),16);
			
			daBytes[counter] = (byte)((b1 * 16) + b2);
			daOtherBytes[counter] = (byte)((b2 * 16) + b1);
			counter++;
		}
		System.out.println("String:" + foo);
		UUID uuid = UUID.nameUUIDFromBytes(daBytes);
		System.out.println("UUID:" + uuid);
		
		uuid = UUID.nameUUIDFromBytes(daOtherBytes);
		System.out.println("Other UUID:" + uuid);
		
		/*UUID uuid = UUID.randomUUID();
		System.out.println("Random:" + uuid);
		System.out.println("MOST:" + Long.toHexString(uuid.getMostSignificantBits()).toUpperCase());
		System.out.println("LEAST:" + Long.toHexString(uuid.getLeastSignificantBits()).toUpperCase());
		
		String foo = Long.toHexString(uuid.getMostSignificantBits()).toUpperCase() + Long.toHexString(uuid.getLeastSignificantBits()).toUpperCase();
		
		byte[] byte_array = foo.getBytes();
		
		UUID uuid3 = UUID.nameUUIDFromBytes(byte_array);
		
		int len = byte_array.length;
		
		BigInteger bi = new BigInteger(foo.substring(0,16), 16);
		long most = bi.longValue();
		bi = new BigInteger(foo.substring(16,32), 16);
		long least = bi.longValue();
		
		UUID uuid2 = new UUID(most, least);
		System.out.println("Reformed from the string:" + uuid2);
		
		System.out.println("From String getBytes:" + uuid3);
		
		System.out.println("From byte array length:" + len);
		
		
		//  REST IS JUNK
		byte daBytes[] = new byte[32];
		for(int i=0;i<daBytes.length;i++){
		//		System.out.println(foo.substring(i,i+1));
				daBytes[i] = Byte.parseByte(foo.substring(i,i+1),16);
		}
		System.out.println("String:" + foo);
		 uuid = UUID.nameUUIDFromBytes(daBytes);
		System.out.println("UUID:" + uuid);
		*/
		
	}
	
	private void setupUI() {
		setLayout(new BorderLayout());
		setupMenuBar();
		add(setupSettingsPanel(), BorderLayout.WEST);
		add(setupPlayerRegion(), BorderLayout.CENTER);
		add(setupModesRegion(), BorderLayout.SOUTH);
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
				loadPlayerAnimations();
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Save", 'S',
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePlayerAnimations();
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Save As", 'A',
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePlayerAnimationsAs();
			}
		});
		
		JMenu animMenu = GUIUtilities.createMenu(menuBar, "Animations", 'A');
		GUIUtilities.createMenuItem(animMenu, "New", 'N',
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addNewAnimation();
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
	public void updateMetaTilesScale(Integer val) {
		_stampControls.setScale(val.intValue());
		_playerStamp.notifyDisplayInterfaceUpdated();
	}

	public void updateMetaTilesGrid(Boolean val) {
		_stampControls.setShowTileGrid(val.booleanValue());
		_playerStamp.notifyDisplayInterfaceUpdated();
	}

	public void updateMetaTilesOAMGrid(Boolean val) {
		_stampControls.setShowOAMGrid(val.booleanValue());
		_playerStamp.notifyDisplayInterfaceUpdated();
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
						PLAYER_EDITOR_SHOW_TILE_GRID,
						DEFAULT_PLAYER_EDITOR_SHOW_TILE_GRID,
						getClass().getMethod("updateMetaTilesGrid", boolClass));
				cb.setSelected(_stampControls.getShowTileGrid());
				yPos++;

				cb = GUIUtilities.addCustomCheckBox(this, gbc, gbl, p, yPos,
						SHOW_OAM_GRID_TITLE, SHOW_OAM_GRID_TOOLTIP,
						PLAYER_EDITOR_SHOW_OAM_GRID,
						DEFAULT_PLAYER_EDITOR_SHOW_OAM_GRID,
						getClass().getMethod("updateMetaTilesOAMGrid", boolClass));
				cb.setSelected(_stampControls.getShowOAMGrid());
				yPos++;

				GUIUtilities.addCustomSpinner(this, gbc, gbl, p, yPos,
						STAMP_TILES_SCALE_TITLE, STAMP_TILES_SCALE_TOOLTIP,
						_stampControls.getScale(),
						MIN_PLAYER_EDITOR_STAMP_TILES_SCALE,
						MAX_PLAYER_EDITOR_STAMP_TILES_SCALE,
						STEP_PLAYER_EDITOR_STAMP_TILES_SCALE,
						getClass().getMethod("updateMetaTilesScale", intClass));
				yPos++;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return p;
	}
	
	private JPanel setupPlayerRegion() {
	
			
			JPanel panel1 = new JPanel();
			panel1.setBorder(new EmptyBorder(1, 1, 1, 1));
			panel1.setLayout(new BorderLayout());

			JPanel panel2 = new JPanel();
			panel2.setBorder(new EmptyBorder(1, 1, 1, 1));
			panel2.setLayout(new BorderLayout());

			panel1.add(panel2, BorderLayout.NORTH);
			panel1.add(new JPanel(), BorderLayout.CENTER);
			panel2.add(_playerStamp, BorderLayout.WEST);
			panel2.add(new JPanel(), BorderLayout.CENTER);

			return panel1;
	}

	private JPanel setupModesRegion() {
		
		return new JPanel();
	}

	
	private void setupMouseControls() {
	}
	
	private void processLeftClick(int x, int y, int z) {
	}
	
	private void loadPlayerAnimations() {
		 File selectedFile = FileUtilities.selectFileForOpen(this);
		 if(selectedFile != null) {
			 _currentAnimation.loadFromXMLFile(selectedFile);
		 }
	}
	
	private void savePlayerAnimations() {
		System.err.println("savePlayerAnimations not implemented");
	}
	
	private void savePlayerAnimationsAs() {
		 File outFile = FileUtilities.selectFileForSave(this);
         if(outFile != null) {
        	 _currentAnimation.saveToXMLFile(outFile);
         }
	}

	private void addNewAnimation() {
		_currentAnimation.addNewAnimation();
	}
	
	// NESModelListener methods
	public void notifyImagePaletteChanged() {
		if (_palettePanel != null) {
			_palettePanel.resetPalette();
		}
		_playerStamp.notifyDisplayInterfaceUpdated();
	}
	
	public void notifySpritePaletteChanged() {
		if (_palettePanel != null) {
			_palettePanel.resetPalette();
		}
		_playerStamp.notifyDisplayInterfaceUpdated();
	}
	public void notifyPatternTableChanged() {
		_playerStamp.notifyDisplayInterfaceUpdated();
	}

	public void notifyNameTableChanged() {
	//	System.out.println("Name Table Changed. Ignored.");
	}

	public void notifyPatternTableSelected(int pageNum, int index) {
	//	System.out.println("notifyPatternTableSelected " + pageNum + " " + index + " Ignored by Editor");
	}

	public void notifyPatternTableToBeModified(int pageNum, int index) {
	//	System.out.println("notifyPatternTableToBeModified " + pageNum + " "	+ index + "Ignored by Editor");
	}
	
}
