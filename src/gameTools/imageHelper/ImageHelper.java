/*
 *
 * Created on October 10, 2006, 2:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package gameTools.imageHelper;

import gameTools.levelEditor.LevelEditor;
import gameTools.levelEditor.LevelEditorModel;
import gameTools.screenLayout.ScreenLayoutUI;
import gameTools.stampEditor.StampUtilities;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;
import java.awt.image.MemoryImageSource;
import java.awt.image.RescaleOp;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import model.CHRModel;
import model.NESModelListener;
import model.NESPaletteModel;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.palette.PalettePanel;
import ui.chr.patternTable.PatternTablePanel;
import ui.chr.tileEditor.CHRTile;
import ui.input.GridMouseResultsModel;
import ui.testrom.TestRomHelper;
import utilities.EnvironmentUtilities;
import utilities.FileUtilities;
import utilities.GUIUtilities;

/**
 * Purpose of this class is to take an arbitrary image and convert to NES format.
 * There are different modes:
 * Backgound Mode determines the images based on 4 assigned colors
 * Sprite Mode assigns 0 to transparent pixels and determines the remainder of the image based on 3 colors
 *
 * @author abailey
 */
public class ImageHelper extends JInternalFrame implements NESPaletteModel, NESModelListener {

	private static final long serialVersionUID = 5458235960531154553L;

	public final static String IMAGE_HELPER_FRAME_TITLE = "Image Helper Tool";

	public final static String IMAGE_HELPER_NUM_TILES_WIDE_PROPERTY = "ImageHelperNumTilesWide";
	public final static int DEFAULT_IMAGE_HELPER_NUM_TILES_WIDE_PROPERTY = 32;
	public final static int IMAGE_HELPER_MAX_NUM_TILES_WIDE_PROPERTY = 32 * 30;
	public final static String IMAGE_HELPER_NUM_TILES_HIGH_PROPERTY = "ImageHelperNumTilesHigh";
	public final static int DEFAULT_IMAGE_HELPER_NUM_TILES_HIGH_PROPERTY = 30;
	public final static int IMAGE_HELPER_MAX_NUM_TILES_HIGH_PROPERTY = 30;
	public final static String IMAGE_HELPER_SCALE_PROPERTY = "ImageHelperScale";
	public final static int DEFAULT_IMAGE_HELPER_SCALE_PROPERTY = 1;
	public final static String IMAGE_HELPER_OFFSET_PROPERTY = "ImageHelperCHROffset";
	public final static int DEFAULT_IMAGE_HELPER_OFFSET_PROPERTY = 1;

	public final static String IMAGE_HELPER_SOURCE_POS_X_PROPERTY = "ImageHelperSourcePosX";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_POS_X_PROPERTY = 0;
	public final static String IMAGE_HELPER_SOURCE_POS_Y_PROPERTY = "ImageHelperSourcePosY";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_POS_Y_PROPERTY = 0;
	public final static String IMAGE_HELPER_SOURCE_SCALE_X_PROPERTY = "ImageHelperSourceScaleX";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_SCALE_X_PROPERTY = 100;
	public final static String IMAGE_HELPER_SOURCE_SCALE_Y_PROPERTY = "ImageHelperSourceScaleY";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_SCALE_Y_PROPERTY = 100;
	public final static String IMAGE_HELPER_SOURCE_CLIP_X_PROPERTY = "ImageHelperSourceClipX";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_CLIP_X_PROPERTY = 0;
	public final static String IMAGE_HELPER_SOURCE_CLIP_Y_PROPERTY = "ImageHelperSourceClipY";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_CLIP_Y_PROPERTY = 0;
	public final static String IMAGE_HELPER_SOURCE_CLIP_WID_PROPERTY = "ImageHelperSourceClipWid";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_CLIP_WID_PROPERTY = 256;
	public final static String IMAGE_HELPER_SOURCE_CLIP_HGT_PROPERTY = "ImageHelperSourceClipHgt";
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_CLIP_HGT_PROPERTY = 240;
	public final static String IMAGE_HELPER_REGION_TOP_LEFT_X_PROPERTY = "ImageHelperRegionTopLeftX";
	public final static String IMAGE_HELPER_REGION_TOP_LEFT_Y_PROPERTY = "ImageHelperRegionTopLeftY";
	public final static String IMAGE_HELPER_REGION_WIDTH_PROPERTY = "ImageHelperRegionWidth";
	public final static String IMAGE_HELPER_REGION_HEIGHT_PROPERTY = "ImageHelperRegionHeight";
	public final static String IMAGE_HELPER_PALETTE_TYPE_PROPERTY = "ImageHelperPaletteType";
	public final static String IMAGE_HELPER_RED_CONTRAST_SCALE_PROPERTY = "IMAGE_HELPER_RED_CONTRAST_SCALE_PROPERTY";
	public final static String IMAGE_HELPER_GREEN_CONTRAST_SCALE_PROPERTY = "IMAGE_HELPER_GREEN_CONTRAST_SCALE_PROPERTY";
	public final static String IMAGE_HELPER_BLUE_CONTRAST_SCALE_PROPERTY = "IMAGE_HELPER_BLUE_CONTRAST_SCALE_PROPERTY";
	public final static String IMAGE_HELPER_BRIGHTNESS_PROPERTY = "IMAGE_HELPER_BRIGHTNESS_PROPERTY";
	public final static String IMAGE_HELPER_DITHER_PROPERTY = "IMAGE_HELPER_DITHER_PROPERTY";
	public final static String IMAGE_HELPER_BG_PALETTE_INDEX = "ImageHelperBGPaletteIndex_";
	public final static String IMAGE_HELPER_SPR_PALETTE_INDEX = "ImageHelperSprPaletteIndex_";
	public final static String IMAGE_HELPER_LAST_IMAGE_FILE_PROPERTY = "IMAGE_HELPER_LAST_IMAGE_FILE_PROPERTY";
	public final static String IMAGE_HELPER_TWO_BY_TWO_STAMP_FILE_PROPERTY = "IMAGE_HELPER_TWO_BY_TWO_STAMP_FILE_PROPERTY";
	public final static String IMAGE_HELPER_FOUR_BY_FOUR_STAMP_FILE_PROPERTY = "IMAGE_HELPER_FOUR_BY_FOUR_STAMP_FILE_PROPERTY";
	public final static String IMAGE_HELPER_GUESS_PALETTE_SIZE_PROPERTY = "IMAGE_HELPER_GUESS_PALETTE_SIZE_PROPERTY";
	public final static String IMAGE_HELPER_PALETTE_LOCKED_PROPERTY = "IMAGE_HELPER_PALETTE_LOCKED_PROPERTY";
	public final static String IMAGE_HELPER_SPRITE_PALETTE_SIZE_PROPERTY = "ImageHelperSpritePaletteSize";
	public final static String IMAGE_HELPER_BG_PALETTE_SIZE_PROPERTY = "ImageHelperBGPaletteSize";
	public final static int DEFAULT_IMAGE_HELPER_GUESS_PALETTE_SIZE_PROPERTY = 1;
	public final static int DEFAULT_IMAGE_HELPER_RED_CONTRAST_SCALE_PROPERTY = 1;
	public final static int DEFAULT_IMAGE_HELPER_GREEN_CONTRAST_SCALE_PROPERTY = 1;
	public final static int DEFAULT_IMAGE_HELPER_BLUE_CONTRAST_SCALE_PROPERTY = 1;
	public final static int DEFAULT_IMAGE_HELPER_BRIGHTNESS_PROPERTY = 1;
	public final static boolean DEFAULT_IMAGE_HELPER_DITHER_PROPERTY = false;
	public final static int DEFAULT_IMAGE_HELPER_PALETTE_TYPE_PROPERTY = PPUConstants.IMAGE_PALETTE_TYPE;
	public final static int DEFAULT_IMAGE_HELPER_SPRITE_PALETTE_SIZE_PROPERTY = 16;
	public final static int DEFAULT_IMAGE_HELPER_BG_PALETTE_SIZE_PROPERTY = 16;
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_BORDER_WIDTH_PROPERTY = 0;
	public final static int DEFAULT_IMAGE_HELPER_SOURCE_BORDER_HEIGHT_PROPERTY = 0;
	public final static int DEFAULT_IMAGE_HELPER_REGION_TOP_LEFT_X_PROPERTY = 0;
	public final static int DEFAULT_IMAGE_HELPER_REGION_TOP_LEFT_Y_PROPERTY = 0;
	public final static int DEFAULT_IMAGE_HELPER_REGION_WIDTH_PROPERTY = 0;
	public final static int DEFAULT_IMAGE_HELPER_REGION_HEIGHT_PROPERTY = 0;
	public final static boolean DEFAULT_IMAGE_HELPER_PALETTE_LOCKED_PROPERTY = false;
	public final static boolean DEFAULT_IMAGE_HELPER_USE_SUBSET_PROPERTY = false;
	public final static int DEFAULT_IMAGE_HELPER_BG_PALETTE = 0;
	public final static int DEFAULT_IMAGE_HELPER_SPR_PALETTE = 0;

	protected int numTilesWide;
	protected int numTilesHigh;
	protected int tileWid;
	protected int tileHgt;
	protected int imgWid;
	protected int imgHgt;
	protected int _scaleFactor;

	protected int sourceScaleX;
	protected int sourceScaleY;
	protected int sourcePosX;
	protected int sourcePosY;
	protected int sourceClipX;
	protected int sourceClipY;
	protected int sourceClipWid;
	protected int sourceClipHgt;

//	protected int origPix[] = null;
	protected int nesPix[] = null;


	protected int _tileStartOffset = 0;
	protected boolean keepOldData = true;
	private int regionTopLeftX;
	private int regionTopLeftY;
	private int regionWidth;
	private int regionHeight;
	private int paletteType;
	private int defaultObjectType = 0;
	private float redContrastScale;
	private float greenContrastScale;
	private float blueContrastScale;
	private float brightness;
	private int guessPaletteRepeatSize;
//	private MemoryImageSource origMis = null;
	protected MemoryImageSource nesMis = null;
	private IndexColorModel nesColorModel[] = new IndexColorModel[4];
	private int[][][] inPix = null;
	protected PalettePanel palettePanel = null;
	private PatternTablePanel patternTableRegion = null;
	private JLabel fileNameLabel = null;
	private JLabel nesTileCountLabel = null;
	private JCheckBoxMenuItem lockMenuItem = null;
	private JMenuItem oamStampMenuItem = null;
	private Image2DPanel origImgPanel = null;
	private ImagePanel nesImgPanel = null;
	private GridMouseResultsModel activeRegion = null;
	private int spritePaletteSize = DEFAULT_IMAGE_HELPER_SPRITE_PALETTE_SIZE_PROPERTY;

	protected int backgroundPaletteSize = DEFAULT_IMAGE_HELPER_BG_PALETTE_SIZE_PROPERTY;
	protected byte backgroundPalette[] = null;
	protected byte spritePalette[] = null;
	protected boolean paletteLocked = DEFAULT_IMAGE_HELPER_PALETTE_LOCKED_PROPERTY;
	protected boolean spriteMode = true;

	private CHREditorModel modelRef = null;
	private File selectedFile = null;
	private boolean isDitherEnabled = false;

	// GUI pieces
	private JSpinner srcPosXSpinner = null;
	private JSpinner srcPosYSpinner = null;
	private JSpinner sourceScaleXSpinner = null;
	private JSpinner sourceScaleYSpinner = null;
	private JSpinner srcClipXSpinner = null;
	private JSpinner srcClipYSpinner = null;
	private JSpinner srcClipWidSpinner = null;
	private JSpinner srcClipHgtSpinner = null;

	private ImageFilterTableModel filterModel = null;
	private int selectedRow = -1;
	

	
	public ImageHelper() {
		this(DEFAULT_IMAGE_HELPER_NUM_TILES_WIDE_PROPERTY, DEFAULT_IMAGE_HELPER_NUM_TILES_HIGH_PROPERTY, DEFAULT_IMAGE_HELPER_SCALE_PROPERTY, DEFAULT_IMAGE_HELPER_PALETTE_TYPE_PROPERTY);
	}

	public String getFrameTitle() {
		return IMAGE_HELPER_FRAME_TITLE;
	}

	public ImageHelper(int numTilesWid, int numTilesHgt, int scaleFactor, int palType) {
		super("Frame", true, true, false, false);
		setTitle(getFrameTitle());

		modelRef = new CHREditorModel();
		setupSettings();

		// need sub-region controls

		setupUI();
		updatePalettes();
	}
	protected void setupSettings() {
		numTilesWide = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_NUM_TILES_WIDE_PROPERTY, DEFAULT_IMAGE_HELPER_NUM_TILES_WIDE_PROPERTY);
		numTilesHigh = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_NUM_TILES_HIGH_PROPERTY, DEFAULT_IMAGE_HELPER_NUM_TILES_HIGH_PROPERTY);

		sourcePosX = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_POS_X_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_POS_X_PROPERTY);
		sourcePosY = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_POS_Y_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_POS_Y_PROPERTY);
		sourceScaleX = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_SCALE_X_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_SCALE_X_PROPERTY);
		sourceScaleY = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_SCALE_Y_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_SCALE_Y_PROPERTY);
		sourceClipX = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_X_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_CLIP_X_PROPERTY);
		sourceClipY = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_Y_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_CLIP_Y_PROPERTY);
		sourceClipWid = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_WID_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_CLIP_WID_PROPERTY);
		sourceClipHgt = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_HGT_PROPERTY, DEFAULT_IMAGE_HELPER_SOURCE_CLIP_HGT_PROPERTY);

		regionTopLeftX = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_REGION_TOP_LEFT_X_PROPERTY, DEFAULT_IMAGE_HELPER_REGION_TOP_LEFT_X_PROPERTY);
		regionTopLeftY = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_REGION_TOP_LEFT_Y_PROPERTY, DEFAULT_IMAGE_HELPER_REGION_TOP_LEFT_Y_PROPERTY);
		regionWidth = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_REGION_WIDTH_PROPERTY, DEFAULT_IMAGE_HELPER_REGION_WIDTH_PROPERTY);
		regionHeight = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_REGION_HEIGHT_PROPERTY, DEFAULT_IMAGE_HELPER_REGION_HEIGHT_PROPERTY);

		paletteType = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_PALETTE_TYPE_PROPERTY, DEFAULT_IMAGE_HELPER_PALETTE_TYPE_PROPERTY);
		guessPaletteRepeatSize = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_GUESS_PALETTE_SIZE_PROPERTY, DEFAULT_IMAGE_HELPER_GUESS_PALETTE_SIZE_PROPERTY);
		redContrastScale = EnvironmentUtilities.getFloatEnvSetting(IMAGE_HELPER_RED_CONTRAST_SCALE_PROPERTY, DEFAULT_IMAGE_HELPER_RED_CONTRAST_SCALE_PROPERTY);
		greenContrastScale = EnvironmentUtilities.getFloatEnvSetting(IMAGE_HELPER_GREEN_CONTRAST_SCALE_PROPERTY, DEFAULT_IMAGE_HELPER_GREEN_CONTRAST_SCALE_PROPERTY);
		blueContrastScale = EnvironmentUtilities.getFloatEnvSetting(IMAGE_HELPER_BLUE_CONTRAST_SCALE_PROPERTY, DEFAULT_IMAGE_HELPER_BLUE_CONTRAST_SCALE_PROPERTY);
		brightness = EnvironmentUtilities.getFloatEnvSetting(IMAGE_HELPER_BRIGHTNESS_PROPERTY, DEFAULT_IMAGE_HELPER_BRIGHTNESS_PROPERTY);
		spritePaletteSize = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SPRITE_PALETTE_SIZE_PROPERTY, DEFAULT_IMAGE_HELPER_SPRITE_PALETTE_SIZE_PROPERTY);
		backgroundPaletteSize = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_BG_PALETTE_SIZE_PROPERTY, DEFAULT_IMAGE_HELPER_BG_PALETTE_SIZE_PROPERTY);
		paletteLocked = EnvironmentUtilities.getBooleanEnvSetting(IMAGE_HELPER_PALETTE_LOCKED_PROPERTY, DEFAULT_IMAGE_HELPER_PALETTE_LOCKED_PROPERTY);
		isDitherEnabled = EnvironmentUtilities.getBooleanEnvSetting(IMAGE_HELPER_DITHER_PROPERTY, DEFAULT_IMAGE_HELPER_DITHER_PROPERTY);

		_scaleFactor = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SCALE_PROPERTY, DEFAULT_IMAGE_HELPER_SCALE_PROPERTY);
		_tileStartOffset = EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_OFFSET_PROPERTY, DEFAULT_IMAGE_HELPER_OFFSET_PROPERTY);

		String tmp = EnvironmentUtilities.getStringEnvSetting(IMAGE_HELPER_LAST_IMAGE_FILE_PROPERTY, null);
		if (tmp != null) {
			selectedFile = new File(tmp);
		}

		tileWid = PPUConstants.CHR_WIDTH;
		tileHgt = PPUConstants.CHR_HEIGHT;
		imgWid = tileWid * numTilesWide;
		imgHgt = tileHgt * numTilesHigh;
		spriteMode = (paletteType == PPUConstants.SPRITE_PALETTE_TYPE);

		backgroundPalette = new byte[PPUConstants.NES_SPRITE_PALETTE_SIZE];
		spritePalette = new byte[PPUConstants.NES_SPRITE_PALETTE_SIZE];
	}

	// NESPaletteListener
	public void notifyImagePaletteChanged() {
		if (!spriteMode) {
			updateConvertedImage(); // this calls: nesMis.newPixels()
			nesImgPanel.reallyRepaint();
		}
	}

	public void notifySpritePaletteChanged() {
		if (spriteMode) {
			updateConvertedImage(); // this calls: nesMis.newPixels()
			nesImgPanel.reallyRepaint();
		}
	}
	// NESPaletteModel

	public void updatePaletteIndex(int index, byte val, boolean goAhead) {
		if (paletteType == PPUConstants.SPRITE_PALETTE_TYPE) {
			updateSpritePaletteIndex(index, val, goAhead);
		} else {
			updateImagePaletteIndex(index, val, goAhead);
		}
	}

	public void updateImagePaletteIndex(int index, byte val) {
		updateImagePaletteIndex(index, val, true);
	}

	public void updateImagePaletteIndex(int index, byte val, boolean goAhead) {
		backgroundPalette[index] = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_BG_PALETTE_INDEX + index, val);
		if (goAhead && !spriteMode) {
			updateConvertedImage();
		}
	}

	public void updateSpritePaletteIndex(int index, byte val) {
		updateSpritePaletteIndex(index, val, true);
	}

	public void updateSpritePaletteIndex(int index, byte val, boolean goAhead) {
		spritePalette[index] = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SPR_PALETTE_INDEX + index, val);
		if (goAhead && spriteMode) {
			updateConvertedImage();
		}
	}

	private void setTileCount(int cnt, int offset) {
		if (nesTileCountLabel != null) {
			if (cnt < 0 || cnt > PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE) {
				nesTileCountLabel.setText(cnt + " (TOO MANY) ");
				nesTileCountLabel.setForeground(Color.RED);
			} if ((cnt + offset) > PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE) {
				nesTileCountLabel.setText(cnt + " " + offset + " (Offset wraps) ");
				nesTileCountLabel.setForeground(Color.RED);
			} else {
				nesTileCountLabel.setText(cnt + "");
				nesTileCountLabel.setForeground(fileNameLabel.getForeground());
			}
		}
	}

	public byte getImagePaletteAtIndex(int index) {
		return backgroundPalette[index];
	}

	public byte getSpritePaletteAtIndex(int index) {
		return spritePalette[index];
	}

	public boolean isPaletteMode() {
		return true;
	}

	public boolean isTilesMode() {
		return true;
	}

	public boolean isEffectsMode() {
		return true;
	}
	// here is the plan:
		// load in an image from file.
	// Dispay the image at 256*240 pixels (NTSC)
	// Display the image again: using the limited 64 color NES palette
	// Allow the user to generate it

	protected void setupUI() {
		getContentPane().setLayout(new BorderLayout());
		setupMenuBar();
		setupImagePanels(); // the input and output image panels
		setupControls();        
		filterModel.addTableModelListener(new TableModelListener() {
		      public void tableChanged(TableModelEvent e) {
		    	  origImgPanel.setFilters(filterModel.getFilters());
		      }
		    });
		updateConvertedImage();

		pack();
		//Set the window's location.
		setLocation(0, 0);
	}

	protected void setupControls() {
		JTabbedPane tabbedPane = new JTabbedPane(); // the tabbed controls at the top
		tabbedPane.add("Controls", setupImageSettings());
		if (isPaletteMode()) {
			tabbedPane.add("Palette", setupPalette());
		}
		if (isTilesMode()) {
			tabbedPane.add("Tiles", setupCHR());
		}
		if (isEffectsMode()) {
			tabbedPane.add("Effects", setupEffects());
		}
		getContentPane().add(tabbedPane, BorderLayout.NORTH);

	}

	private JPanel setupCHR() {
		JPanel panel = new JPanel();
		patternTableRegion = new PatternTablePanel(null, modelRef,0, "BG");

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout(gbl);
		GUIUtilities.initializeGBC(gbc);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbl.setConstraints(patternTableRegion, gbc);
		panel.add(patternTableRegion);

		panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 0));
		panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 1));
		return panel;
	}

	protected JPanel setupPalette() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		palettePanel = new PalettePanel(this, this, true, true, PPUConstants.NES_IMAGE_PALETTE_SIZE, PPUConstants.NES_SPRITE_PALETTE_SIZE);
		palettePanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel middleControlPanel = new JPanel();
		middleControlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		panel.setLayout(gbl);

		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipady = 0;
		gbc.ipadx = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbl.setConstraints(middleControlPanel, gbc);
		panel.add(middleControlPanel);

		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridy = 1;
		gbl.setConstraints(palettePanel, gbc);
		panel.add(palettePanel);

		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(spritePaletteSize);
			Integer min = new Integer(4);
			Integer max = new Integer(PPUConstants.NES_SPRITE_PALETTE_SIZE);
			Integer step = new Integer(4);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Size of Sprite Palette to use");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != spritePaletteSize) {
						setSpritePaletteSize(newVal);
					}
				}
			});
			middleControlPanel.add(new JLabel("Sprite Palette Size:"));
			middleControlPanel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(backgroundPaletteSize);
			Integer min = new Integer(4);
			Integer max = new Integer(PPUConstants.NES_IMAGE_PALETTE_SIZE);
			Integer step = new Integer(4);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Size of BG Palette to use");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != backgroundPaletteSize) {
						setBGPaletteSize(newVal);
					}
				}
			});
			middleControlPanel.add(new JLabel("BG Palette Size:"));
			middleControlPanel.add(spinner);
		}

		{
			final JSpinner spinner = new JSpinner();
			SpinItem spinItems[] = new SpinItem[2];
			spinItems[0] = new SpinItem("Background Mode", PPUConstants.IMAGE_PALETTE_TYPE);
			spinItems[1] = new SpinItem("Sprite Mode", PPUConstants.SPRITE_PALETTE_TYPE);
			SpinnerListModel spinnerModel = new SpinnerListModel(spinItems);
			spinnerModel.setValue(spinItems[paletteType]);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Type of Palette to use");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					setPaletteType(((SpinItem) spinner.getModel().getValue()).getInnerObj());
				}
			});
			middleControlPanel.add(new JLabel("Palette:"));
			middleControlPanel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			SpinItem spinItems[] = new SpinItem[3];
			spinItems[0] = new SpinItem("13 Unique Colors", 1);
			spinItems[1] = new SpinItem("10 Unique Colors", 2);
			spinItems[2] = new SpinItem("7 Unique Colors", 3);
			SpinnerListModel spinnerModel = new SpinnerListModel(spinItems);
			spinnerModel.setValue(spinItems[paletteType]);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Colors when guessing the palette.");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int tmp = ((SpinItem) spinner.getModel().getValue()).getInnerObj();
					if (tmp != guessPaletteRepeatSize) {
						guessPaletteRepeatSize = tmp;
						EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_GUESS_PALETTE_SIZE_PROPERTY, guessPaletteRepeatSize);
					}
				}
			});
			middleControlPanel.add(new JLabel("Guess Palette:"));
			middleControlPanel.add(spinner);
		}



		return panel;
	}

	protected  int getMaxTilesWide() {
		return IMAGE_HELPER_MAX_NUM_TILES_WIDE_PROPERTY;
	}
	protected  int getMaxTilesHigh() {
		return IMAGE_HELPER_MAX_NUM_TILES_HIGH_PROPERTY;
	}
	protected JPanel setupNametablePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(new TitledBorder("Nametable"));
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(numTilesWide);
			Integer min = new Integer(1);
			Integer max = new Integer(getMaxTilesWide());
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Number of tiles Wide to convert the image into the CHR");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != numTilesWide) {
						setNumTilesWide(newVal);
					}
				}
			});
			panel.add(new JLabel("Num Tiles Wide:"));
			panel.add(spinner);
		}

		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(numTilesHigh);
			Integer min = new Integer(1);
			Integer max = new Integer(getMaxTilesHigh());
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Number of tiles High to convert the image into the CHR");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != numTilesHigh) {
						setNumTilesHigh(newVal);
					}
				}
			});
			panel.add(new JLabel("Num Tiles High:"));
			panel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(_tileStartOffset);
			Integer min = new Integer(0);
			Integer max = new Integer(255);
			Integer step = new Integer(1);
			if(value < min){
				value = min;
			}
			if(value > max) {
				value = max;	
			}
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Offset for the CHR tiles");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != numTilesHigh) {
						setCHRTilesOffset(newVal);
					}                    
				}
			});
			panel.add(new JLabel("Tile Offset:"));
			panel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(_scaleFactor);
			Integer min = new Integer(1);
			Integer max = new Integer(16);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Scale the Image to make it easier to see the details");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != _scaleFactor) {
						setScaleFactor(newVal);
					}
				}
			});
			panel.add(new JLabel("Scale:"));
			panel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(0);
			Integer min = new Integer(0);
			Integer max = new Integer(99);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Margin of Error for tile comparison. Smaller is more accurate");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					CHRTile.IMAGE_SIMILARITY_THRESHOLD = newVal;
					guessPalette();
				}
			});
			panel.add(new JLabel("Margin of Error:"));
			panel.add(spinner);
		}        


		return panel;
	}

	// scale to fit (button)
	// scale 
	// clip
	// position within dest 
	private JPanel setupSourceImageControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(new TitledBorder("Source Image Layout"));

		JPanel scalePanel = new JPanel();
		scalePanel.setLayout(new GridLayout(2, 2));
		scalePanel.setBorder(new TitledBorder("Scale"));

		{
			sourceScaleXSpinner = new JSpinner();
			Integer value = new Integer(sourceScaleX);
			Integer min = new Integer(1);
			Integer max = new Integer(1000);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			sourceScaleXSpinner.setModel(spinnerModel);
			sourceScaleXSpinner.setToolTipText("Width Scale Percentage");
			sourceScaleXSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) sourceScaleXSpinner.getModel().getValue()).intValue();
					if (newVal != sourceScaleX) {
						setSourceScaleX(newVal);
					}
				}
			});
			scalePanel.add(new JLabel("Width Scale %"));
			scalePanel.add(sourceScaleXSpinner);
		}
		{
			sourceScaleYSpinner = new JSpinner();
			Integer value = new Integer(sourceScaleY);
			Integer min = new Integer(1);
			Integer max = new Integer(1000);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			sourceScaleYSpinner.setModel(spinnerModel);
			sourceScaleYSpinner.setToolTipText("Height Scale Percentage");
			sourceScaleYSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) sourceScaleYSpinner.getModel().getValue()).intValue();
					if (newVal != sourceScaleY) {
						setSourceScaleY(newVal);
					}
				}
			});
			scalePanel.add(new JLabel("Height Scale %"));
			scalePanel.add(sourceScaleYSpinner);
		}
		panel.add(scalePanel);

		JPanel clipPosPanel = new JPanel();
		clipPosPanel.setLayout(new GridLayout(2, 2));
		clipPosPanel.setBorder(new TitledBorder("Clip Position"));
		{
			srcClipXSpinner = new JSpinner();
			Integer value = new Integer(sourceClipX);
			Integer min = new Integer(-256);
			Integer max = new Integer(256);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			srcClipXSpinner.setModel(spinnerModel);
			srcClipXSpinner.setToolTipText("Clip X Position");
			srcClipXSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) srcClipXSpinner.getModel().getValue()).intValue();
					if (newVal != sourceClipX) {
						setSourceClipX(newVal);
					}
				}
			});
			clipPosPanel.add(new JLabel("X:"));
			clipPosPanel.add(srcClipXSpinner);
		}
		{
			srcClipYSpinner = new JSpinner();
			Integer value = new Integer(sourceClipY);
			Integer min = new Integer(-240);
			Integer max = new Integer(240);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			srcClipYSpinner.setModel(spinnerModel);
			srcClipYSpinner.setToolTipText("Clip Y Position");
			srcClipYSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) srcClipYSpinner.getModel().getValue()).intValue();
					if (newVal != sourceClipY) {
						setSourceClipY(newVal);
					}
				}
			});
			clipPosPanel.add(new JLabel("Y:"));
			clipPosPanel.add(srcClipYSpinner);
		}
		panel.add(clipPosPanel);

		JPanel clipSizePanel = new JPanel();
		clipSizePanel.setLayout(new GridLayout(2, 2));
		clipSizePanel.setBorder(new TitledBorder("Clip Dimensions"));
		{
			srcClipWidSpinner = new JSpinner();
			Integer value = new Integer(sourceClipWid);
			Integer min = new Integer(-256);
			Integer max = new Integer(256);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			srcClipWidSpinner.setModel(spinnerModel);
			srcClipWidSpinner.setToolTipText("Clip Width");
			srcClipWidSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) srcClipWidSpinner.getModel().getValue()).intValue();
					if (newVal != sourceClipWid) {
						setSourceClipWid(newVal);
					}
				}
			});
			clipSizePanel.add(new JLabel("Width:"));
			clipSizePanel.add(srcClipWidSpinner);
		}
		{
			srcClipHgtSpinner = new JSpinner();
			Integer value = new Integer(sourceClipHgt);
			Integer min = new Integer(-240);
			Integer max = new Integer(240);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			srcClipHgtSpinner.setModel(spinnerModel);
			srcClipHgtSpinner.setToolTipText("Clip Height");
			srcClipHgtSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) srcClipHgtSpinner.getModel().getValue()).intValue();
					if (newVal != sourceClipHgt) {
						setSourceClipHgt(newVal);
					}
				}
			});
			clipSizePanel.add(new JLabel("Height:"));
			clipSizePanel.add(srcClipHgtSpinner);
		}
		panel.add(clipSizePanel);

		JPanel positionPanel = new JPanel();
		positionPanel.setLayout(new GridLayout(2, 2));
		positionPanel.setBorder(new TitledBorder("Position"));
		{
			srcPosXSpinner = new JSpinner();
			Integer value = new Integer(sourcePosX);
			Integer min = new Integer(-256);
			Integer max = new Integer(256);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			srcPosXSpinner.setModel(spinnerModel);
			srcPosXSpinner.setToolTipText("X Pos");
			srcPosXSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) srcPosXSpinner.getModel().getValue()).intValue();
					if (newVal != sourcePosX) {
						setSourcePosX(newVal);
					}
				}
			});
			positionPanel.add(new JLabel("X:"));
			positionPanel.add(srcPosXSpinner);
		}
		{
			srcPosYSpinner = new JSpinner();
			Integer value = new Integer(sourcePosY);
			Integer min = new Integer(-240);
			Integer max = new Integer(240);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			srcPosYSpinner.setModel(spinnerModel);
			srcPosYSpinner.setToolTipText("Y Pos");
			srcPosYSpinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) srcPosYSpinner.getModel().getValue()).intValue();
					if (newVal != sourcePosY) {
						setSourcePosY(newVal);
					}
				}
			});
			positionPanel.add(new JLabel("Y:"));
			positionPanel.add(srcPosYSpinner);
		}
		panel.add(positionPanel);
		/*
        JButton scaleButton = new JButton("Fit Image");

        scaleButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
        srcPosXSpinner.getModel().setValue(new Integer(0));
        srcPosYSpinner.getModel().setValue(new Integer(0));
        }
        });
        scaleButton.setEnabled(false);
        scaleButton.setToolTipText("Not yet implemented");
        panel.add(scaleButton);
		 */
		return panel;
	}

	protected JPanel setupRegionControlsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(new TitledBorder("Sub Region"));
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(regionTopLeftX);
			Integer min = new Integer(0);
			Integer max = new Integer(31);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Top Left Corner X of the Region");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != regionTopLeftX) {
						setRegionTopLeftX(newVal);
					}
				}
			});
			panel.add(new JLabel("Region Top Left X"));
			panel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(regionTopLeftY);
			Integer min = new Integer(0);
			Integer max = new Integer(29);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Top Left Corner Y of the Region");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != regionTopLeftY) {
						setRegionTopLeftY(newVal);
					}
				}
			});
			panel.add(new JLabel("Region Top Left Y"));
			panel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(regionWidth);
			Integer min = new Integer(0);
			Integer max = new Integer(32);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Region Tiles Wide");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != regionWidth) {
						setRegionWidth(newVal);
					}
				}
			});
			panel.add(new JLabel("Region Tiles Wide"));
			panel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Integer value = new Integer(regionHeight);
			Integer min = new Integer(0);
			Integer max = new Integer(30);
			Integer step = new Integer(1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Region Tiles High");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					int newVal = ((Integer) spinner.getModel().getValue()).intValue();
					if (newVal != regionHeight) {
						setRegionHeight(newVal);
					}
				}
			});
			panel.add(new JLabel("Region Tiles High"));
			panel.add(spinner);
		}

		return panel;
	}

	protected JPanel setupImageSettings() {
		JPanel controlPanel = new JPanel();

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		controlPanel.setLayout(gbl);

		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.ipady = 0;
		gbc.ipadx = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.gridwidth = 1;
		gbc.gridx = 0;

		JPanel nametablePanel = setupNametablePanel();
		gbc.gridy = 0;
		gbl.setConstraints(nametablePanel, gbc);
		controlPanel.add(nametablePanel);

		JPanel srcManipulationPanel = setupSourceImageControlsPanel();
		gbc.gridy = 1;
		gbl.setConstraints(srcManipulationPanel, gbc);
		controlPanel.add(srcManipulationPanel);

		JPanel regionControlPanel = setupRegionControlsPanel();
		gbc.gridy = 2;
		gbl.setConstraints(regionControlPanel, gbc);
		controlPanel.add(regionControlPanel);

		JPanel filler = new JPanel();
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridy = 3;
		gbl.setConstraints(filler, gbc);
		controlPanel.add(filler);

		return controlPanel;
	}

	private void addNewFilter(int filterType) {
		ImageFilter filter = ImageFilterFactory.createNewFilter(filterType);
		if(filter != null){
			filterModel.addFilter(filter);
			origImgPanel.setFilters(filterModel.getFilters());
		}
		
	}
	private void editSelectedFilter() {
		if(selectedRow == -1){
			return;
		}
		origImgPanel.setFilters(filterModel.getFilters());
	}
	private void deleteSelectedFilter() {
		if(selectedRow == -1){
			return;
		}
		filterModel.removeRow(selectedRow);
		origImgPanel.setFilters(filterModel.getFilters());
	}
	
	
	private JPanel setupEffects() {
		
		JPanel bottomControlPanel = new JPanel();
		bottomControlPanel.setBorder(new TitledBorder("Filters"));
		bottomControlPanel.setLayout(new BorderLayout());
		
		// We want a check list of effects
		filterModel = new ImageFilterTableModel();
		 final JTable table = new JTable(filterModel);
		 table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		 final ListSelectionModel rowSM = table.getSelectionModel();
		 rowSM.addListSelectionListener(new ListSelectionListener() {
		 public void valueChanged(ListSelectionEvent e) {			 
			 if(e.getValueIsAdjusting()){
				 // ignore ALL but the last selection in the sequence
				 return;
			 }
			 if(rowSM.isSelectionEmpty()){
				 selectedRow = -1;
			 } else {
				 selectedRow = table.getSelectedRow();
			 }
		 }
		 });
		    // TableColumn column = table.getColumnModel().getColumn(3);
		    // column.setCellRenderer(renderer);
		    // column.setCellEditor(editor);
		 table.setPreferredScrollableViewportSize(new Dimension( 100, 5*table.getRowHeight() )); 
		 JScrollPane scrollPane = new JScrollPane(table);
		 
		 bottomControlPanel.add(scrollPane, BorderLayout.CENTER);

		 
		 
		 // Now some buttons
		 JPanel buttonPanel = new JPanel();
		 GridBagLayout gbl = new GridBagLayout();
	     GridBagConstraints gbc = new GridBagConstraints();
	     buttonPanel.setLayout(gbl);
	     GUIUtilities.initializeGBC(gbc);
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.fill = GridBagConstraints.BOTH;
	        gbc.weightx = 0;
	        gbc.weighty = 0;
	        gbc.gridwidth = 1;
	        gbc.gridheight = 1;

	        gbc.gridx = 0;
	        gbc.gridy = 0;
	        JButton newFilterButton = new JButton("New Filter");
/*	        newFilterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addNewFilter();
                }
            });
	        */
	        gbl.setConstraints(newFilterButton, gbc);	
	        buttonPanel.add(newFilterButton);


		 JButton editFilterButton = new JButton("Edit Filter");
		 editFilterButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 editSelectedFilter();
             }
         });
		 gbc.gridy = 1;
	        gbl.setConstraints(editFilterButton, gbc);
		 buttonPanel.add(editFilterButton);
		 
		 JButton deleteFilterButton = new JButton("Delete Filter");
		 deleteFilterButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 deleteSelectedFilter();
             }
         });
		 gbc.gridy = 2;
		  gbl.setConstraints(deleteFilterButton, gbc);
		 buttonPanel.add(deleteFilterButton);

		 bottomControlPanel.add(buttonPanel, BorderLayout.EAST);

		 //Add listener to components that can bring up popup menus.
		 final JPopupMenu popup = new JPopupMenu();
		 JMenuItem menuItem = new JMenuItem("Brighten");
		    menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addNewFilter(ImageFilterFactory.BRIGHTEN_FILTER);
                }
            });
		    popup.add(menuItem);

  
		    MouseListener popupListener = new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
		       // maybeShowPopup(e);
		    }

		    public void mouseReleased(MouseEvent e) {
		        //maybeShowPopup(e);
		    	alwaysShowPopup(e);
		    }
/*
		    private void maybeShowPopup(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		            popup.show(e.getComponent(),
		                       e.getX(), e.getY());
		        }
		    }
*/		    
		    private void alwaysShowPopup(MouseEvent e) {
	            popup.show(e.getComponent(),
		                       e.getX(), e.getY());
		    }
		    };
		    newFilterButton.addMouseListener(popupListener);

			
		
/*		
	//	bottomControlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		{
			final JSpinner spinner = new JSpinner();
			Float value = new Float(redContrastScale);
			Float min = new Float(-10);
			Float max = new Float(10);
			Float step = new Float(0.1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Red Contrast Scale");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					float newVal = ((Float) spinner.getModel().getValue()).floatValue();
					if (newVal != redContrastScale) {
						setRedContrastScale(newVal);
					}
				}
			});
			bottomControlPanel.add(new JLabel("Red Contrast Scale"));
			bottomControlPanel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Float value = new Float(greenContrastScale);
			Float min = new Float(-10);
			Float max = new Float(10);
			Float step = new Float(0.1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Green Contrast Scale");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					float newVal = ((Float) spinner.getModel().getValue()).floatValue();
					if (newVal != greenContrastScale) {
						setGreenContrastScale(newVal);
					}
				}
			});
			bottomControlPanel.add(new JLabel("Green Contrast Scale"));
			bottomControlPanel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Float value = new Float(blueContrastScale);
			Float min = new Float(-10);
			Float max = new Float(10);
			Float step = new Float(0.1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Blue Contrast Scale");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					float newVal = ((Float) spinner.getModel().getValue()).floatValue();
					if (newVal != blueContrastScale) {
						setBlueContrastScale(newVal);
					}
				}
			});
			bottomControlPanel.add(new JLabel("Blue Contrast Scale"));
			bottomControlPanel.add(spinner);
		}
		{
			final JSpinner spinner = new JSpinner();
			Float value = new Float(brightness);
			Float min = new Float(-1);
			Float max = new Float(10);
			Float step = new Float(0.1);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
			spinner.setModel(spinnerModel);
			spinner.setToolTipText("Brightness");
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					float newVal = ((Float) spinner.getModel().getValue()).floatValue();
					if (newVal != brightness) {
						setBrightness(newVal);
					}
				}
			});
			bottomControlPanel.add(new JLabel("Brightness"));
			bottomControlPanel.add(spinner);
		}
		
		 */

		return bottomControlPanel;

	}

	private void setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		//getContentPane().add(menuBar,BorderLayout.NORTH);

		JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');
		JMenu utilsMenu = GUIUtilities.createMenu(menuBar, "Utils", 'U');


		GUIUtilities.createMenuItem(fileMenu, "Load Image", 'L', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				loadImageData();
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Create Test ROM", 'C', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				saveResultsToROM();
			}
		});
		GUIUtilities.createMenuItem(fileMenu, "Extract from Test ROM", 'X', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				extractResultsFromTestROM();
			}
		});


		lockMenuItem = new JCheckBoxMenuItem("Lock Palette");
		lockMenuItem.setSelected(paletteLocked);

		oamStampMenuItem = new JMenuItem("Create OAM bounded Stamp");
		evaluateDependencies();
		/*
        GUIUtilities.reuseMenuItem(utilsMenu, lockMenuItem, 'L', new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
        lockUnlockPalette();
        }
        });
		 */
		if (isPaletteMode()) {
			GUIUtilities.createMenuItem(utilsMenu, "Guess Palette", 'G', new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					guessPalette();
				}
			});

			GUIUtilities.createMenuItem(utilsMenu, "Refresh Palette", 'R', new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					updateConvertedImage();
				}
			});
		}

		GUIUtilities.createMenuItem(utilsMenu, "Send to CHR", 'C', new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveResults();
			}
		});
		GUIUtilities.createMenuItem(utilsMenu, "Load Existing CHR", 'C', new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (modelRef == null) {
					return;
				}
				File selectedFile = FileUtilities.selectFileForOpen( null );
				if(selectedFile == null){
					return;
				}
				modelRef.loadPatternTablesFromFile(selectedFile, null);
				// rebuild
				updateConvertedImage();
			}
		});


		GUIUtilities.createMenuItem(utilsMenu, "Send to Level Editor", 'L', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				saveResultsAsLevel();
			}
		});



		GUIUtilities.reuseMenuItem(utilsMenu, oamStampMenuItem, 'O', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				createStamp();
			}
		});
	}

	private JPanel setupOriginalPanel() {
		JPanel origPanel = new JPanel();
		origPanel.setLayout(new BorderLayout());
		if(!usingTabsMode()){
			origPanel.setBorder(new TitledBorder("Original"));
		}
		origImgPanel = new Image2DPanel(imgWid, imgHgt, _scaleFactor);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout());
		subPanel.add(origImgPanel, BorderLayout.WEST);
		JPanel subPanel2 = new JPanel();
		subPanel2.setLayout(new BorderLayout());
		subPanel2.add(subPanel, BorderLayout.NORTH);
		origPanel.add(subPanel2, BorderLayout.CENTER);
		fileNameLabel = new JLabel("??");
		if(showOriginalFilename()) {
			JPanel origDescriptionPanel = new JPanel();
			origDescriptionPanel.setLayout(new FlowLayout());
			origDescriptionPanel.add(new JLabel("FileName:"));
			origDescriptionPanel.add(fileNameLabel);
			origPanel.add(origDescriptionPanel, BorderLayout.NORTH);
		}
		return origPanel;
	}

	private JPanel setupConvertedPanel() {
		JPanel nesPanel = new JPanel();
		nesPanel.setLayout(new BorderLayout());
		if(!usingTabsMode()){
			nesPanel.setBorder(new TitledBorder(getConvertedTitle()));
		}
		nesPix = new int[imgWid * imgHgt];
		nesMis = new MemoryImageSource(imgWid, imgHgt, nesPix, 0, imgWid);
		activeRegion = new GridMouseResultsModel(0, 0, imgWid, imgHgt, modelRef.getCHRModel());
		activeRegion.startX = regionTopLeftX;
		activeRegion.startY = regionTopLeftY;
		activeRegion.endX = regionTopLeftX + regionWidth;
		activeRegion.endY = regionTopLeftY + regionHeight;
		activeRegion.assignPatternPage(regionTopLeftX / 32);

		nesImgPanel = new ImagePanel(nesMis, imgWid, imgHgt, false, activeRegion, _scaleFactor);
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout());
		subPanel.add(nesImgPanel, BorderLayout.WEST);
		JPanel subPanel2 = new JPanel();
		subPanel2.setLayout(new BorderLayout());
		subPanel2.add(subPanel, BorderLayout.NORTH);

		nesPanel.add(subPanel2, BorderLayout.CENTER);
		nesTileCountLabel = new JLabel("??");
		if(showConvertedDescription()) {
			JPanel nesDescriptionPanel = new JPanel();
			nesDescriptionPanel.setLayout(new FlowLayout());
			nesDescriptionPanel.add(new JLabel("Tile Count:"));
			nesTileCountLabel.setToolTipText("Number of distinct tiles required to create this image. (Must not exceed 256)");
			nesDescriptionPanel.add(nesTileCountLabel);
			nesPanel.add(nesDescriptionPanel, BorderLayout.NORTH);
		}
		return nesPanel;
	}

	private void setupImagePanels() {
		if(usingTabsMode()) {
			JTabbedPane imgPanel = new JTabbedPane(); // the tabbed controls at the top
			imgPanel.add("Original", setupOriginalPanel());
			imgPanel.add(getConvertedTitle(), setupConvertedPanel());
			getContentPane().add(imgPanel, BorderLayout.CENTER);
		} else {
			JPanel imgPanel = new JPanel();
			imgPanel.setBackground(Color.DARK_GRAY);
			imgPanel.setLayout(new GridLayout(1,2,10,10));
			imgPanel.add(setupOriginalPanel());
			imgPanel.add(setupConvertedPanel());
			getContentPane().add(imgPanel, BorderLayout.CENTER);
		}
	}
	protected boolean usingTabsMode() {
		return true;
	}
	protected boolean showConvertedDescription() {
		return true;
	}
	protected boolean showOriginalFilename() {
		return true;
	}

	protected String getConvertedTitle() {
		return "NES";
	}

	private void setBGPaletteSize(int val) {
		backgroundPaletteSize = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_BG_PALETTE_SIZE_PROPERTY, backgroundPaletteSize);
		updatePalettes();
	}

	private void setSpritePaletteSize(int val) {
		spritePaletteSize = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SPRITE_PALETTE_SIZE_PROPERTY, spritePaletteSize);
		updatePalettes();
	}

	private void setScaleFactor(int val) {
		_scaleFactor = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SCALE_PROPERTY, _scaleFactor);
		origImgPanel.setScaleFactor(_scaleFactor);
		nesImgPanel.setScaleFactor(_scaleFactor);
	}

	private void setNumTilesWide(int val) {
		numTilesWide = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_NUM_TILES_WIDE_PROPERTY, numTilesWide);
		evaluateDependencies();
		resetDimensions();
	}

	private void setNumTilesHigh(int val) {
		numTilesHigh = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_NUM_TILES_HIGH_PROPERTY, numTilesHigh);
		evaluateDependencies();
		resetDimensions();
	}

	private void setCHRTilesOffset(int val) {
		_tileStartOffset = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_OFFSET_PROPERTY, _tileStartOffset);
		updateConvertedImage();
	}

	private void evaluateDependencies() {
		boolean enabledMode = false;
		if (activeRegion != null) {
			if (activeRegion.isBoxValid()) {
				if ((activeRegion.startX % 2 == 0) && (activeRegion.startY % 2 == 0) && (activeRegion.endX % 2 == 0) && (activeRegion.endY % 2 == 0)) {
					enabledMode = true;
				}
			}
		}
		oamStampMenuItem.setEnabled(enabledMode);
	}

	private void setSourcePosX(int val) {
		sourcePosX = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_POS_X_PROPERTY, val);
		reloadImageData();
	}

	private void setSourcePosY(int val) {
		sourcePosY = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_POS_Y_PROPERTY, val);
		reloadImageData();
	}

	private void setSourceScaleX(int val) {
		sourceScaleX = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_SCALE_X_PROPERTY, val);
		reloadImageData();
	}

	private void setSourceScaleY(int val) {
		sourceScaleY = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_SCALE_Y_PROPERTY, val);
		reloadImageData();
	}

	private void setSourceClipX(int val) {
		sourceClipX = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_X_PROPERTY, val);
		reloadImageData();
	}

	private void setSourceClipY(int val) {
		sourceClipY = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_Y_PROPERTY, val);
		reloadImageData();
	}

	private void setSourceClipWid(int val) {
		sourceClipWid = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_WID_PROPERTY, val);
		reloadImageData();
	}

	private void setSourceClipHgt(int val) {
		sourceClipHgt = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_CLIP_HGT_PROPERTY, val);
		reloadImageData();
	}
	/*
    private void setBorderWidth(int val){
    borderWidth = val;
    EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_BORDER_WIDTH_PROPERTY, borderWidth );
    reloadImageData();
    }

    private void setBorderHeight(int val){
    borderHeight = val;
    EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_BORDER_HEIGHT_PROPERTY, borderHeight );
    reloadImageData();
    }


    private void setTopLeftX(int val){
    topLeftX = val;
    EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_TOP_LEFT_X_PROPERTY, topLeftX );
    reloadImageData();
    }
    private void setTopLeftY(int val){
    topLeftY = val;
    EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_SOURCE_TOP_LEFT_Y_PROPERTY, topLeftY );
    reloadImageData();
    }
	 */

	private void setRegionTopLeftX(int val) {
		//        if(32-val >= regionWidth){
		regionTopLeftX = val;
		activeRegion.startX = regionTopLeftX;
		activeRegion.endX = regionTopLeftX + regionWidth;
		activeRegion.assignPatternPage(regionTopLeftX / 32);
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_REGION_TOP_LEFT_X_PROPERTY, regionTopLeftX);
		evaluateDependencies();
		if (nesImgPanel != null) {
			nesImgPanel.repaint();
		}
		//        }
}

	private void setRegionTopLeftY(int val) {
		//       if(30-val >= regionHeight){
		regionTopLeftY = val;
		activeRegion.startY = regionTopLeftY;
		activeRegion.endY = regionTopLeftY + regionHeight;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_REGION_TOP_LEFT_Y_PROPERTY, regionTopLeftY);
		evaluateDependencies();
		if (nesImgPanel != null) {
			nesImgPanel.repaint();
		}
		//       }
}

	private void setRegionWidth(int val) {
		//       if(32-regionTopLeftX >= val){
		regionWidth = val;
		activeRegion.endX = regionTopLeftX + regionWidth;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_REGION_WIDTH_PROPERTY, regionWidth);
		evaluateDependencies();
		if (nesImgPanel != null) {
			nesImgPanel.repaint();
		}
		//       }
}

	private void setRegionHeight(int val) {
		//       if(30-regionTopLeftY >= val){
		regionHeight = val;
		activeRegion.endY = regionTopLeftY + regionHeight;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_REGION_HEIGHT_PROPERTY, regionHeight);
		evaluateDependencies();
		if (nesImgPanel != null) {
			nesImgPanel.repaint();
		}
		//       }
}

	private void setRedContrastScale(float val) {
		redContrastScale = val;
		EnvironmentUtilities.updateFloatEnvSetting(IMAGE_HELPER_RED_CONTRAST_SCALE_PROPERTY, val);
		// reloadImageData();
		doPixelProcessing();
	}

	private void setGreenContrastScale(float val) {
		greenContrastScale = val;
		EnvironmentUtilities.updateFloatEnvSetting(IMAGE_HELPER_GREEN_CONTRAST_SCALE_PROPERTY, val);
		// reloadImageData();
		doPixelProcessing();
	}

	private void setBlueContrastScale(float val) {
		blueContrastScale = val;
		EnvironmentUtilities.updateFloatEnvSetting(IMAGE_HELPER_BLUE_CONTRAST_SCALE_PROPERTY, val);
		// reloadImageData();
		doPixelProcessing();
	}

	private void setBrightness(float val) {
		brightness = val;
		EnvironmentUtilities.updateFloatEnvSetting(IMAGE_HELPER_BRIGHTNESS_PROPERTY, val);
		// reloadImageData();
		doPixelProcessing();
	}
	

	private void setPaletteType(int val) {
		paletteType = val;
		EnvironmentUtilities.updateIntegerEnvSetting(IMAGE_HELPER_PALETTE_TYPE_PROPERTY, paletteType);
		spriteMode = (paletteType == PPUConstants.SPRITE_PALETTE_TYPE);
		resetDimensions();
	}

	private void resetDimensions() {
		imgWid = tileWid * numTilesWide;
		imgHgt = tileHgt * numTilesHigh;
		origImgPanel.setDimensions(imgWid, imgHgt);

		nesPix = new int[imgWid * imgHgt];
		nesMis = new MemoryImageSource(imgWid, imgHgt, nesPix, 0, imgWid);
		nesImgPanel.setMis(nesMis, imgWid, imgHgt);

		reloadImageData();
	}

	private void updatePalettes() {
		if (paletteType == PPUConstants.IMAGE_PALETTE_TYPE) {
			for (int i = 0; i < backgroundPalette.length; i++) {
				backgroundPalette[i] = (byte) EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_BG_PALETTE_INDEX + i, DEFAULT_IMAGE_HELPER_BG_PALETTE);
			}
			for (int i = 0; i < spritePalette.length; i++) {
				if (i % 4 == 0) {
					spritePalette[i] = backgroundPalette[i];
				} else {
					spritePalette[i] = (byte) EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SPR_PALETTE_INDEX + i, DEFAULT_IMAGE_HELPER_SPR_PALETTE);
				}
			}
		} else {
			// give priority to the saved sprite palette for index 0
			for (int i = 0; i < spritePalette.length; i++) {
				spritePalette[i] = (byte) EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_SPR_PALETTE_INDEX + i, DEFAULT_IMAGE_HELPER_SPR_PALETTE);
			}
			for (int i = 0; i < backgroundPalette.length; i++) {
				if (i % 4 == 0) {
					backgroundPalette[i] = spritePalette[i];
				} else {
					backgroundPalette[i] = (byte) EnvironmentUtilities.getIntegerEnvSetting(IMAGE_HELPER_BG_PALETTE_INDEX + i, DEFAULT_IMAGE_HELPER_BG_PALETTE);
				}
			}
		}

		if (palettePanel != null) {
			palettePanel.setVisibleSpritePaletteSize(spritePaletteSize, false);
			palettePanel.setVisibleImagePaletteSize(backgroundPaletteSize, false);
			palettePanel.resetPalette();
			resetDimensions();
		}
	}

	private void loadImageData() {
		try {
			selectedFile = FileUtilities.selectFileForOpen(this);
			if (selectedFile != null) {
				EnvironmentUtilities.updateStringEnvSetting(IMAGE_HELPER_LAST_IMAGE_FILE_PROPERTY, selectedFile.getAbsolutePath());
			}
			if (fileNameLabel != null) {
				if (selectedFile == null) {
					fileNameLabel.setToolTipText(" < none > ");
					fileNameLabel.setText("??");
				} else {
					fileNameLabel.setToolTipText(selectedFile.getAbsolutePath());
					fileNameLabel.setText(selectedFile.getName());
				}
			}
			reloadImageData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private CHRTile[][] getStampTiles(int sx, int sy, int ex, int ey) {
		CHRTile[][] t = new CHRTile[ex - sx][ey - sy];

		for (int i = sx; i < ex; i++) {
			for (int j = sy; j < ey; j++) {
				int off = j * 32 + i;
				int index = (modelRef.getCHRModel().nameTableIndexes[0][off] & 0xFF);
				int oamVal = modelRef.getCHRModel().getOAMFromNTIndex(0, off);
				int pixOffset = 16 * index;
				byte mask[] = new byte[16];
				System.arraycopy(modelRef.getCHRModel().patternTable[0], pixOffset, mask, 0, 16);
				t[i - sx][j - sy] = new CHRTile(mask, oamVal, modelRef);
			}
		}
		return t;

	}

	private void createStamp() {
		if (!activeRegion.isBoxValid()) {
			JOptionPane.showMessageDialog(this, "Invalid stamp dimenesions");
			return;
		}
		File stampFile = FileUtilities.selectFileForSave(this);
		if (stampFile == null) {
			return;
		}
		CHRTile[][] t = getStampTiles(activeRegion.startX, activeRegion.startY, activeRegion.endX, activeRegion.endY);
		StampUtilities.storeStampTiles(stampFile, t, defaultObjectType);
	}


	private void saveResults() {
		if (modelRef == null) {
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

	private void saveResultsAsLevel() {
		if (modelRef == null) {
			return;
		}
		// launch a NEW Level Editor tool
		LevelEditorModel levelModel = new LevelEditorModel(0);
		levelModel.setTilesHigh(numTilesHigh);
		levelModel.setTilesWide(DEFAULT_IMAGE_HELPER_NUM_TILES_WIDE_PROPERTY);
		levelModel.setScreensHigh(1);

		int numPages = 1;
		if (numTilesWide > PPUConstants.NAME_TABLE_WIDTH) {
			numPages = (numTilesWide / PPUConstants.NAME_TABLE_WIDTH);
			int mod = (numTilesWide % PPUConstants.NAME_TABLE_WIDTH);
			if (mod != 0) {
				numPages++;
			}
		}
		levelModel.setScreensWide(numPages);
		LevelEditor frame = new LevelEditor(modelRef, levelModel);
		frame.setVisible(true); //necessary as of 1.3
		getParent().add(frame);
		try {
			frame.setSelected(true);
		} catch (java.beans.PropertyVetoException ex) {
			ex.printStackTrace();
		}
	}

	private void saveResultsToROM() {
		if (modelRef == null) {
			return;
		}
		try {
			TestRomHelper.createTestROM(modelRef, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void extractResultsFromTestROM() {
		try {
			CHREditorModel newModelRef = new CHREditorModel();
			if (TestRomHelper.extractFromTestROM(newModelRef, this)) {
				ScreenLayoutUI frame = new ScreenLayoutUI(newModelRef);
				frame.setVisible(true); //necessary as of 1.3
				getParent().add(frame);
				frame.setSelected(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	// From this code: http://www.componenthouse.com/article-20
	public static BufferedImage blurImage(BufferedImage image) {
		float ninth = 1.0f/9.0f;
		float[] blurKernel = {
		ninth, ninth, ninth,
		ninth, ninth, ninth,
		ninth, ninth, ninth
		};

		Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key,Object>();
		map.put(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		RenderingHints hints = new RenderingHints(map);
		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel)); // , ConvolveOp.EDGE_NO_OP, hints);
		BufferedImage destImage = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
		op.filter(image, destImage);

		// extra action
		RescaleOp rop = new RescaleOp(1.5f, 1.0f, null);
		rop.filter(destImage,image);		
		
		return image;
	}
	private static BufferedImage resize(Image image, int width, int height, int type) {
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
	
	private void reloadImageData() {
		if (fileNameLabel != null) {
			if (selectedFile == null) {
				fileNameLabel.setToolTipText(" < none > ");
				fileNameLabel.setText("??");
			} else {
				fileNameLabel.setToolTipText(selectedFile.getAbsolutePath());
				fileNameLabel.setText(selectedFile.getName());
			}
		}

		if (selectedFile == null) {
			return;
		}
		origImgPanel.reload(imgWid, imgHgt, sourcePosX, sourcePosY, selectedFile);
		origImgPanel.setScale(sourceScaleX, sourceScaleY);
		
		int totalSize = imgWid * imgHgt;
		if (totalSize != nesPix.length) {
			nesPix = new int[totalSize]; // automatically zeroed
		} else {
			for (int i = 0; i < totalSize; i++) {			
				nesPix[i] = 0;
			}
		}

		try {
			doPixelProcessing();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void doPixelProcessing() {
		postProcessOriginalPix();
		origImgPanel.reallyRepaint();
		updateConvertedImage(); // this calls: nesMis.newPixels()
		nesImgPanel.reallyRepaint();
	}

	
	
	private void postProcessOriginalPix() {
/*
		//Calculate average value of all the color values.
		//Subtract the average from every color value
		//Multiply every color value by the same scale factor.  If the scale factor is greater than 1.0, the width of the distribution will be increased.
		//If the scale factor is less than 1.0, the width of the distribution will be decreased.
		//Add the original average value to every color value to either restore the distribution to its original location or to move it to a different location.

		int[][][] outPix = new int[imgHgt][imgWid][4];
		int val;
		int index = 0;
		long rmsSum = 0;
		for (int j = 0; j < imgHgt; j++) {
			for (int i = 0; i < imgWid; i++) {
				val = inPix[j][i][1];
				rmsSum += val * val;

				val = inPix[j][i][2];
				rmsSum += val * val;

				val = inPix[j][i][3];
				rmsSum += val * val;
				index++;
			}
		}
		int rms = (int) (Math.sqrt((int) (rmsSum / (index * 3))));

		index = 0;
		rmsSum = 0;
		float contrastScale[] = new float[4];
		contrastScale[0] = 1.0f;
		contrastScale[1] = redContrastScale;
		contrastScale[2] = greenContrastScale;
		contrastScale[3] = blueContrastScale;

		for (int j = 0; j < imgHgt; j++) {
			for (int i = 0; i < imgWid; i++) {
				outPix[j][i][0] = inPix[j][i][0];
				for (int k = 1; k < 4; k++) {
					val = (int) (((inPix[j][i][k] - rms) * contrastScale[k]) + (rms * brightness));
					if (val < 0) {
						val = 0;
					}
					if (val > 255) {
						val = 255;
					}
					rmsSum += val * val;
					outPix[j][i][k] = val;
				}
				origPix[index] = (outPix[j][i][0] << 24) + (outPix[j][i][1] << 16) + (outPix[j][i][2] << 8) + outPix[j][i][3];
				index++;
			}
		}


		outPix = null;
*/
	}
	
/*
	private void setupPostProcessOriginalPix() {
		inPix = new int[imgHgt][imgWid][4];
		int index = 0;
		for (int j = 0; j < imgHgt; j++) {
			for (int i = 0; i < imgWid; i++) {
				inPix[j][i][0] = (origPix[index] >>> 24);
				inPix[j][i][1] = ((origPix[index] >>> 16) & 0xFF);
				inPix[j][i][2] = ((origPix[index] >>> 8) & 0xFF);
				inPix[j][i][3] = ((origPix[index]) & 0xFF);
				index++;
			}
		}
	}
*/
	
	protected void updateConvertedImage() {

		int sz = 4;
		int startPlace = 0;
		boolean checkAlpha = false;
		if (paletteType == PPUConstants.SPRITE_PALETTE_TYPE) {
			checkAlpha = true;
		}
		for (int ncmIndex = 0; ncmIndex < 4; ncmIndex++) {
			byte r[] = new byte[sz];
			byte g[] = new byte[sz];
			byte b[] = new byte[sz];
			int index = 0;
			if (spriteMode) {
				for (int i = startPlace; i < 4; i++) {
					Color c = PPUConstants.NES_PALETTE[spritePalette[i + ncmIndex * 4]];
					r[index] = (byte) c.getRed();
					g[index] = (byte) c.getGreen();
					b[index] = (byte) c.getBlue();
					index++;
				}
			} else {
				for (int i = startPlace; i < 4; i++) {
					Color c = PPUConstants.NES_PALETTE[backgroundPalette[i + ncmIndex * 4]];
					r[index] = (byte) c.getRed();
					g[index] = (byte) c.getGreen();
					b[index] = (byte) c.getBlue();
					index++;
				}
			}
			nesColorModel[ncmIndex] = new IndexColorModel(8, sz, r, g, b);
		}

		// now we have 4 color models.
		// break the image into 16x16 pieces and determine the best color model for each
		// then assign the OAM, and create the tiles, etc..
		// store the TILES AND the OAM


		// break this into 16x16 pieces
		// check to see which region has the most hits and the closest average for pixels

		int startX = 0;
		int endX = numTilesWide;
		int startY = 0;
		int endY = numTilesHigh;

		int nesPixIndices[][] = new int[4][nesPix.length];

		int origPix[] = origImgPanel.getPixels();
		for (int i = 0; i < origPix.length; i++) {
			boolean found = false;
			if (checkAlpha) {
				int alpha = (origPix[i] >>> 24);
				if (alpha == 0) {
					nesPix[i] = 0x00000000;
					nesPixIndices[0][i] = 0;
					nesPixIndices[1][i] = 0;
					nesPixIndices[2][i] = 0;
					nesPixIndices[3][i] = 0;
					found = true;
				}
			}
			if (!found) {
				for (int j = 0; j < 4; j++) {
					byte pixel[] = (byte[]) nesColorModel[j].getDataElements(origPix[i], null);
					//      nesPix[i] = nesColorModel[j].getRGB(pixel[0]);
					nesPixIndices[j][i] = pixel[0];
				}
			}
		}

		// get the pattern table
		TreeMap<CHRTile, Integer> map = new TreeMap<CHRTile, Integer>();
		TreeMap<Integer, CHRTile> reverseMap = new TreeMap<Integer, CHRTile>();
		Vector<CHRTile> patternVector = new Vector<CHRTile>();
		int tileCount = 0;

		int numPages = 1;
		if (numTilesWide > PPUConstants.NAME_TABLE_WIDTH) {
			numPages = (numTilesWide / PPUConstants.NAME_TABLE_WIDTH);
			int mod = (numTilesWide % PPUConstants.NAME_TABLE_WIDTH);
			if (mod != 0) {
				numPages++;
			}
		}



		int oamTables[][] = new int[numPages][PPUConstants.NAME_TABLE_HEIGHT * PPUConstants.NAME_TABLE_WIDTH];
		int nameTables[][] = new int[numPages][PPUConstants.NAME_TABLE_HEIGHT * PPUConstants.NAME_TABLE_WIDTH];

		modelRef.setNumNameTablePages(numPages);
		modelRef.getCHRModel().assignImagePalette(backgroundPalette, 0);
		modelRef.getCHRModel().assignSpritePalette(spritePalette, 0);


		CHRImageTile imgTile[][][] = new CHRImageTile[4][2][2];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					imgTile[i][j][k] = new CHRImageTile((spriteMode) ? PPUConstants.SPRITE_PALETTE_TYPE : PPUConstants.IMAGE_PALETTE_TYPE, i);
				}
			}
		}


		for (int ty = startY; ty < endY; ty += 2) {
			for (int tx = startX; tx < endX; tx += 2) {
				// where the top left of the tile is
				int txMod = tx % PPUConstants.NAME_TABLE_WIDTH;
				int txDiv = tx / PPUConstants.NAME_TABLE_WIDTH;

				int scaleCount = 0;
				double score[] = new double[4];
				for (int q = 0; q < 4; q++) {
					for (int subY = 0; ((subY < 2) && ((ty + subY) < endY)); subY++) {
						for (int subX = 0; ((subX < 2) && ((tx + subX) < endX)); subX++) {
							imgTile[q][subX][subY].reset();
							int pixOffset = ((ty + subY) * imgWid * tileHgt) + ((tx + subX) * tileWid);
							imgTile[q][subX][subY].setAdditionalInfo(tx + subX, ty + subY);

							for (int i = 0; i < tileHgt; i++) {
								for (int j = 0; j < tileWid; j++) {
									int sub = pixOffset + i * imgWid + j;
									int pix = nesPixIndices[q][sub];
									imgTile[q][subX][subY].setPixelIndex(j, i, (pix & 0x3));
									imgTile[q][subX][subY].setOrigPixel(j, i, nesColorModel[q].getRGB(pix), origPix[sub]);
								}
							}
							scaleCount++;
							score[q] += imgTile[q][subX][subY].getMatchScore();
						}
					}
				}
				int oam = 0;
				double closest = score[0];
				for (int q = 1; q < 4; q++) {
					if (score[q] < closest) {
						closest = score[q];
						oam = q;
					}
				}

				for (int subY = 0; ((subY < 2) && ((ty + subY) < endY)); subY++) {
					for (int subX = 0; ((subX < 2) && ((tx + subX) < endX)); subX++) {
						int pixOffset = ((ty + subY) * imgWid * tileHgt) + ((tx + subX) * tileWid);
						oamTables[txDiv][(ty + subY) * PPUConstants.NAME_TABLE_WIDTH + (txMod + subX)] = oam;
						CHRTile tile = imgTile[oam][subX][subY].createCHRTile(modelRef);

						// Do this to get a similar key, when doing error equivalency
						Integer obj = null;
						if(map.containsKey(tile)){
							obj = map.get(tile);
							tile = reverseMap.get(obj);
						}
						// Do not clobber yet.   If we are using margin of error, we want the "similar" tile rather than this one.
						int clobber[] = tile.getPix();
						int l = 0;
						for (int y = 0; y < 8; y++) {
							int pixOffset2 = pixOffset + y * imgWid;
							for (int x = 0; x < 8; x++) {
								nesPix[pixOffset2 + x] = clobber[l++];
							}
						}

						// this will not eliminate duplicates
						// tile=key, Integer=value

						if (obj == null) {
							nameTables[txDiv][(ty + subY) * PPUConstants.NAME_TABLE_WIDTH + (txMod + subX)] = tileCount;
							patternVector.add(tile);
							map.put(tile, new Integer(tileCount));
							reverseMap.put(new Integer(tileCount), tile);
							tileCount++;
						} else {

							nameTables[txDiv][(ty + subY) * PPUConstants.NAME_TABLE_WIDTH + (txMod + subX)] = obj.intValue();
						}

					}
				}
			}
		}




		for (int ty = startY; ty < endY; ty++) {
			for (int tx = startX; tx < endX; tx++) {
				int txMod = tx % PPUConstants.NAME_TABLE_WIDTH;
				int txDiv = tx / PPUConstants.NAME_TABLE_WIDTH;
				int ntIndex = ty * 32 + txMod;

				int pixOffset = ((ty) * imgWid * tileHgt) + ((tx) * tileWid);
				int index = nameTables[txDiv][ntIndex];
				CHRTile tile = (CHRTile) patternVector.get(index);
				int clobber[] = tile.getPix();

				int l = 0;
				for (int y = 0; y < 8; y++) {
					int pixOffset2 = pixOffset + y * imgWid;
					for (int x = 0; x < 8; x++) {
						nesPix[pixOffset2 + x] = clobber[l++];
					}
				}
			}
		}


		// NOTE!!! nesMis is what is responsible for the display of the image
		nesMis.newPixels();

		tileCount = patternVector.size();

		setTileCount(tileCount, _tileStartOffset);



		// now we have a unique pattern table (enforced using the map)
		// we also have it ordered using the vector
		// lets store the values into the model
		while (tileCount > PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE) {
			// remove some.
			patternVector.remove(tileCount-1);
			tileCount = patternVector.size();
			//  System.err.println("Sorry. Too many patterns to store in one pattern page." + tileCount);
			//  return;
		}

		// to do: add support for outputting tiles in a grid format
		int gridWide = numTilesWide;

		byte patternTable0[] = new byte[PPUConstants.PATTERN_TABLE_PAGE_SIZE];
		if(keepOldData){
			System.arraycopy(modelRef.getCHRModel().patternTable[0],0,patternTable0,0, PPUConstants.PATTERN_TABLE_PAGE_SIZE);
		}
		Iterator<CHRTile> iter = patternVector.iterator();
		int offset = _tileStartOffset * 16;
		int tempIndex = 0;
		while (iter.hasNext()) {
			CHRTile tile = iter.next();
			System.arraycopy(tile.asMask(), 0, patternTable0, offset, 16);
			offset += 16;
			tempIndex++;
			if (tempIndex >= gridWide) {
				tempIndex = 0;
				int remainder = (16 - gridWide) * 16;
				if (remainder > 0) {
					offset += remainder;
				}
			}
			if(offset >= PPUConstants.PATTERN_TABLE_PAGE_SIZE ){
				offset = 0;
			}
		}
		modelRef.getCHRModel().assignPatternTable(0, patternTable0, 0);


		// OAM stuff is a real mess and I dont feel like cleaning it right now.
		byte oamVals[][] = new byte[numPages][PPUConstants.OAM_TABLE_SIZE];
		byte b[] = new byte[4];

		for (int p = 0; p < numPages; p++) {
			for (int relY = 0; relY < 30; relY += 2) {
				int oamGroupY = (relY / 2) % 2;
				int oamIndexY = relY / 4;
				for (int relX = 0; relX < 32; relX += 2) {
					int oamGroupX = (relX / 2) % 2; // gets us in terms of a 16 x 16 table
					int oamIndexX = relX / 4; // gets us in terms of an 8x8 table
					int oamSubIndex = oamGroupY * 2 + oamGroupX;
					int oamTableIndex = oamIndexY * PPUConstants.OAM_INDEX_GRID_SIZE + oamIndexX;
					byte oamVal = oamVals[p][oamTableIndex];
					byte newVal = (byte) (oamTables[p][relY * 32 + relX] & 0x03);
					for (int i = 0; i < 4; i++) {
						b[i] = (byte) ((oamVal >> (i * 2)) & 0x03);
						if (i == oamSubIndex) {
							b[i] = newVal;
						}
					}
					byte newOamVal = (byte) (((b[3] << 6) + (b[2] << 4) + (b[1] << 2) + b[0]) & 0xFF);
					oamVals[p][oamTableIndex] = newOamVal;
				}
			}
		}



		//      System.arraycopy(nameTable,0,modelRef.getCHRModel().nameTableIndexes,0,nameTable.length);
		CHRModel mod = modelRef.getCHRModel();
		mod.setNumPages(numPages);
		for (int p = 0; p < numPages; p++) {
			for (int i = 0; i < nameTables[p].length; i++) {
				mod.nameTableIndexes[p][i] = (byte) (nameTables[p][i] + _tileStartOffset);
			}
			System.arraycopy(oamVals[p], 0, modelRef.getCHRModel().oamValues[p], 0, oamVals[p].length);
		}
		notifyPatternTableChanged();

	}

	class Pairing implements Comparable<Pairing> {

		int id;
		int count;

		Pairing(int i, int c) {
			id = i;
			count = c;
		}
		;

		void inc() {
			count++;
		}

		public int compareTo(Pairing o) {
			return (new Integer(o.count).compareTo(new Integer(count)));
		}
	};

	class SpinItem {

		private String _label;
		private int _obj;

		public SpinItem(String name, int obj) {
			_label = name;
			_obj = obj;
		}

		public String toString() {
			return _label;
		}

		public int getInnerObj() {
			return _obj;
		}
	};

	/*
    private void lockUnlockPalette(){
    paletteLocked = lockMenuItem.isSelected();
    //        lockMenuItem.setSelected(paletteLocked);
    EnvironmentUtilities.updateBooleanEnvSetting(IMAGE_HELPER_PALETTE_LOCKED_PROPERTY, paletteLocked );
    }
	 */
	private void guessPalette() {
		if (paletteLocked) {
			return;
		}

		if (selectedFile == null) {
			return;
		}
		int origPix[] = origImgPanel.getPixels();

		int limPix[] = new int[origPix.length];

		int subIndex = 0;
		int subSet[] = new int[PPUConstants.NES_PALETTE.length];

		for (int i = 0; i < PPUConstants.NES_PALETTE.length; i++) {
			if (palettePanel.isSelectable(i)) {
				subSet[subIndex++] = i;
			}
		}

		byte r[] = new byte[subIndex];
		byte g[] = new byte[subIndex];
		byte b[] = new byte[subIndex];
		for (int i = 0; i < subIndex; i++) {
			r[i] = (byte) PPUConstants.NES_PALETTE[subSet[i]].getRed();
			g[i] = (byte) PPUConstants.NES_PALETTE[subSet[i]].getGreen();
			b[i] = (byte) PPUConstants.NES_PALETTE[subSet[i]].getBlue();
		}
		IndexColorModel fullNESColorModel = new IndexColorModel(8, r.length, r, g, b);

		for (int i = 0; i < limPix.length; i++) {
			byte pixel[] = (byte[]) fullNESColorModel.getDataElements(origPix[i], null);
			limPix[i] = subSet[pixel[0]];
		}


		int cnt[] = new int[PPUConstants.NES_PALETTE.length];
		for (int i = 0; i < limPix.length; i++) {
			cnt[limPix[i]]++;
		}

		Vector<Pairing> list = new Vector<Pairing>();
		for (int i = 0; i < cnt.length; i++) {
			list.add(new Pairing(i, cnt[i]));
		}
		Collections.<Pairing>sort(list);

		int repeatSize = guessPaletteRepeatSize;
		for (int i = 0; i < repeatSize; i++) {
			int initialID = ((Pairing) list.get(i)).id;
			updatePaletteIndex(0 + i, (byte) initialID, false);
			updatePaletteIndex(4 + i, (byte) initialID, false);
			updatePaletteIndex(8 + i, (byte) initialID, false);
			updatePaletteIndex(12 + i, (byte) initialID, false);
		}
		for (int i = 0; i < (16 - (4 * repeatSize)); i++) {
			int id = ((Pairing) list.get(i + repeatSize)).id;
			int section = i / (4 - repeatSize);
			int index = i % (4 - repeatSize);

			int j = (section * 4) + (index + repeatSize);
			//           System.out.println(j + ") " + id );
			updatePaletteIndex(j, (byte) id, false);
		}
		updatePalettes();
	}

	// NESModelListener
	public void notifyNameTableChanged() {
		// do nothing
	}

	public void notifyPatternTableSelected(int pageNum, int index) {
		// do nothing
	}
	public void notifyPatternTableToBeModified(int pageNum, int index) {
		// do nothing
	}
	public void notifyPatternTableChanged(){
		if(patternTableRegion != null) {
			patternTableRegion.updatePatternTable();
		}
	}
}
