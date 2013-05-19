/*
 * ScreenLayoutUI.java
 *
 * Created on September 24, 2006, 1:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package gameTools.screenLayout;

import gameTools.animationHelper.AnimationKeyFrameComponent;
import gameTools.stampEditor.StampUtilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.NESModelListener;
import ui.chr.CHRDisplayInterface;
import ui.chr.CHRModelControlPanel;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.nameTable.NameTablePanel;
import ui.chr.palette.PalettePanel;
import ui.chr.patternTable.PatternTablePanel;
import ui.chr.tileEditor.CHRTile;
import ui.chr.tileEditor.CHRTileEditorPanel;
import ui.input.GridMouseResultsModel;
import ui.testrom.TestRomHelper;
import utilities.CompressionUtilities;
import utilities.EnvironmentUtilities;
import utilities.FileUtilities;
import utilities.GUIUtilities;

/**
 *
 * @author abailey
 */
public class ScreenLayoutUI extends JInternalFrame implements NESModelListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2211614956508788294L;
	public final static String SCREEN_LAYOUT_TITLE = "Screen Layout Tool";
    public static final String CHR_BANK_OFFSET = "CHR_BANK_OFFSET";
    public static final int DEFAULT_CHR_BANK_OFFSET = 0;
    public static final String CHR_BANK_DURATION = "CHR_BANK_DURATION";
    public static final int DEFAULT_CHR_BANK_DURATION = PPUConstants.PATTERN_TABLE_PAGE_SIZE;
    protected CHREditorModel modelRef = null;
    private CHRTileEditorPanel editorRegion = null; // may be null
    protected NameTablePanel nameTableRegions[] = null; // may be null
    private PatternTablePanel patternTableRegions[] = null; // may be null
    private PalettePanel paletteRegion = null; // may be null
    private AnimationKeyFrameComponent animationGrid = null;
    private CHRModelControlPanel controlRegion = null; // may be null
    private ButtonGroup bg = null;
    private JPanel lowerPanel = null;
    private int defaultObjectType = 0;
    public boolean setupDragAndDrop = false;
    public boolean layoutControlsMode = false;

    /** Creates a new instance of ScreenLayoutUI */
    public ScreenLayoutUI() {
        super("", true, true, false, false);
        setTitle(getTitleBar());
        modelRef = new CHREditorModel();
        setupUI(true);
    }

    public ScreenLayoutUI(CHREditorModel newModelRef) {
        super("", true, true, false, false);
        setTitle(getTitleBar());
        modelRef = newModelRef;
        setupUI(false);
        notifyImagePaletteChanged(); // this refreshed everything
    }

    public String getTitleBar() { // gets overridden
        return SCREEN_LAYOUT_TITLE;
    }

    private void setupMenuBar() {
        final Component comp = this;
        final NESModelListener listener = this;
        bg = new ButtonGroup();
        JMenuBar menuBar = new JMenuBar();
        getContentPane().add(menuBar, BorderLayout.NORTH);

        JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');

        JMenu projMenu = GUIUtilities.createSubMenu(fileMenu, "Project", 'P');

        GUIUtilities.createMenuItem(projMenu, "New", 'N', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.newProject(comp, listener);

                int compressVal = modelRef.getNameTableCompression();
                Enumeration<AbstractButton> modelEnum = bg.getElements();
                AbstractButton tempButtonModel = modelEnum.nextElement();
                while (compressVal > 0 && modelEnum.hasMoreElements()) {
                    compressVal--;
                    tempButtonModel = modelEnum.nextElement();
                }
                if (tempButtonModel != null) {
                    tempButtonModel.setSelected(true);
                }
            }
        });
        GUIUtilities.createMenuItem(projMenu, "Load", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.loadProject(comp, listener);
                updateCompressionInfo();
            }
        });
        GUIUtilities.createMenuItem(projMenu, "Save", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveProject(comp, listener);
            }
        });
        GUIUtilities.createMenuItem(projMenu, "Save As...", 'A', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveAsProject(comp, listener);
            }
        });



        JMenu patternMenu = GUIUtilities.createSubMenu(fileMenu, "Pattern Tables (Tiles)", 'T');

        GUIUtilities.createMenuItem(patternMenu, "Load", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.loadPatternTables(comp, listener);
            }
        });
        GUIUtilities.createMenuItem(patternMenu, "Save", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.savePatternTables(comp, listener);
            }
        });
        GUIUtilities.createMenuItem(patternMenu, "Save As...", 'A', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveAsPatternTables(comp, listener);
            }
        });

        JMenu partialMenu = GUIUtilities.createSubMenu(patternMenu, "Partial", 'P');

        GUIUtilities.createMenuItem(partialMenu, "Load 4K Bank ", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.loadPatternTablesPortion(comp, listener);
            }
        });
       

        GUIUtilities.createMenuItem(partialMenu, "Save Active Bank", 'A', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.savePatternTableBankPortion(comp, listener);
            }
        });
        GUIUtilities.createMenuItem(partialMenu, "Save Portion of Bank ", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getPartialBank();
            }
        });




        JMenu nameTableMenu = GUIUtilities.createSubMenu(fileMenu, "Name Table (Background)", 'N');

        GUIUtilities.createCheckboxMenuItem(nameTableMenu, "ASCII", 'A', modelRef.getNameTableAsciiMode(), new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                modelRef.setNameTableAsciiMode(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        JMenu compressionMenu = GUIUtilities.createSubMenu(nameTableMenu, "Compression", 'C');


        GUIUtilities.createRadioMenuItem(compressionMenu, "None", 'N', bg, new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    modelRef.setNameTableCompression(CompressionUtilities.NO_COMPRESSION);
                }
            }
        });
        GUIUtilities.createRadioMenuItem(compressionMenu, "Run Length Encoding (RLE)", 'R', bg, new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    modelRef.setNameTableCompression(CompressionUtilities.RLE_COMPRESSION);
                }
            }
        });

        GUIUtilities.createMenuItem(nameTableMenu, "New", 'N', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.newNameTable(comp, listener);
            }
        });

        GUIUtilities.createMenuItem(nameTableMenu, "Load", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.loadNameTable(comp, listener);
                updateCompressionInfo();
            }
        });
        GUIUtilities.createMenuItem(nameTableMenu, "Save", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveNameTable(comp, listener);
            }
        });
        GUIUtilities.createMenuItem(nameTableMenu, "Save As...", 'A', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveAsNameTable(0, comp, listener);
            }
        });


        JMenu paletteMenu = GUIUtilities.createSubMenu(fileMenu, "Palette", 'L');
        GUIUtilities.createMenuItem(paletteMenu, "Load", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.loadPalette(comp, listener);
            }
        });

        GUIUtilities.createMenuItem(paletteMenu, "Save", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.savePalette(comp);
            }
        });
        GUIUtilities.createMenuItem(paletteMenu, "Save As...", 'A', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveAsPalette(comp);
            }
        });

        JMenu partialPalMenu = GUIUtilities.createSubMenu(paletteMenu, "Partial", 'P');
        GUIUtilities.createMenuItem(partialPalMenu, "Save Background Palette", 'B', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveSpecificPalette(comp, listener, PPUConstants.IMAGE_PALETTE_TYPE, 0, PPUConstants.NES_IMAGE_PALETTE_SIZE);
            }
        });
        GUIUtilities.createMenuItem(partialPalMenu, "Save Sprite Palette", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveSpecificPalette(comp, listener, PPUConstants.SPRITE_PALETTE_TYPE, 0, PPUConstants.NES_SPRITE_PALETTE_SIZE);
            }
        });

        JMenu animationMenu = GUIUtilities.createSubMenu(fileMenu, "Animation", 'A');
        GUIUtilities.createMenuItem(animationMenu, "Load", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.loadAnimations(comp, listener);
            }
        });
        GUIUtilities.createMenuItem(animationMenu, "Save", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveAnimations(comp);
            }
        });
        GUIUtilities.createMenuItem(animationMenu, "Save As...", 'A', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                modelRef.saveAsAnimations(comp);
            }
        });

        JMenu guiMenu = GUIUtilities.createMenu(menuBar, "GUI", 'G');

        GUIUtilities.createMenuItem(guiMenu, "View Animation Panel", 'V', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setupAnimationPanel();
            }
        });
        GUIUtilities.createMenuItem(guiMenu, "Hide Animation Panel", 'H', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                clearAnimationPanel();
            }
        });

        JMenu selectionMenu = GUIUtilities.createMenu(menuBar, "Selection", 'S');
        GUIUtilities.createMenuItem(selectionMenu, "Reorder Selected Tiles", 'R', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (nameTableRegions[0] != null) {
                	launchNameTableReorder();
                }
            }
        });

        GUIUtilities.createMenuItem(selectionMenu, "Create Stamp", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (nameTableRegions[0] != null) {
                	createStamp();
                }
            }
        });

        
        JMenu utilsMenu = GUIUtilities.createMenu(menuBar, "Utils", 'U');

        GUIUtilities.createMenuItem(utilsMenu, "Save Background as Image", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (nameTableRegions[0] != null) {
                    nameTableRegions[0].exportAsImage();
                }
            }
        });
        
       
        
        GUIUtilities.createMenuItem(utilsMenu, "Create Test ROM", 'C', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveResultsToROM();
            }
        });
        GUIUtilities.createMenuItem(utilsMenu, "Extract from Test ROM", 'X', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                extractResultsFromTestROM();
            }
        });


    }

    private boolean getPartialBank() {
        final JDialog dlog = new JDialog();
        dlog.getContentPane().setLayout(new BorderLayout());
        dlog.setModal(false);

        final JSpinner bankSpinner = new JSpinner();
        {
            Integer value = new Integer(0);
            Integer min = new Integer(0);
            Integer max = new Integer(1);
            Integer step = new Integer(1);
            SpinnerNumberModel spinModel = new SpinnerNumberModel(value, min, max, step);
            bankSpinner.setModel(spinModel);
        }
        bankSpinner.setToolTipText("CHR Bank (BG=0), (Sprite=1)");

        final JSpinner startPosSpinner = new JSpinner();
        int curStart = EnvironmentUtilities.getIntegerEnvSetting(CHR_BANK_OFFSET, DEFAULT_CHR_BANK_OFFSET);
        {
            if (curStart < 0) {
                curStart = 0;
            }
            if (curStart >= PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE) {
                curStart = PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE - 1;
            }
            Integer value = new Integer(curStart);
            Integer min = new Integer(0);
            Integer max = new Integer(PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE - 1);
            Integer step = new Integer(1);
            SpinnerNumberModel spinModel = new SpinnerNumberModel(value, min, max, step);
            startPosSpinner.setModel(spinModel);
        }
        startPosSpinner.setToolTipText("Starting Position");

        final JSpinner durSpinner = new JSpinner();
        int curDur = EnvironmentUtilities.getIntegerEnvSetting(CHR_BANK_DURATION, DEFAULT_CHR_BANK_DURATION);
        {
            if (curDur < 1) {
                curDur = 1;
            }
            if (curDur >= PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE) {
                curDur = PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE;
            }
            Integer value = new Integer(curDur);
            Integer min = new Integer(1);
            Integer max = new Integer(PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE);
            Integer step = new Integer(1);
            SpinnerNumberModel spinModel = new SpinnerNumberModel(value, min, max, step);
            durSpinner.setModel(spinModel);
        }
        durSpinner.setToolTipText("The number of tiles to save (16 bytes each)");

        JPanel ctrlPanel = new JPanel();
        ctrlPanel.add(new JLabel("Bank (BG=0), (Sprite=1):"));
        ctrlPanel.add(bankSpinner);
        ctrlPanel.add(new JLabel("Starting Tile:"));
        ctrlPanel.add(startPosSpinner);
        ctrlPanel.add(new JLabel("Number of Tiles to Copy:"));
        ctrlPanel.add(durSpinner);

        dlog.getContentPane().add(ctrlPanel, BorderLayout.CENTER);

        JButton closeMe = new JButton("Close");
        closeMe.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dlog.dispose();
            }
        });

        JButton okBut = new JButton("OK");
        okBut.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // int bankOffset = ((Integer)bankSpinner.getModel().getValue()).intValue()*PPUConstants.PATTERN_TABLE_PAGE_SIZE;
                int bank = ((Integer) bankSpinner.getModel().getValue()).intValue();
                int tileOffset = ((Integer) startPosSpinner.getModel().getValue()).intValue() * PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY;
                int dataDuration = ((Integer) durSpinner.getModel().getValue()).intValue() * PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY;
                savePortion(tileOffset, dataDuration, bank);
                dlog.dispose();
            }
        });

        JPanel butPanel = new JPanel();
        butPanel.add(closeMe);
        butPanel.add(okBut);
        dlog.getContentPane().add(butPanel, BorderLayout.SOUTH);
        dlog.pack();
        dlog.setVisible(true);

        return true;
    }

    private boolean savePortion(int offset, int duration, int bank) {
        return modelRef.savePatternTableBankPortion(
                this,
                this,
                offset,
                duration,
                bank);

    }

    protected JPanel setupNameTableControlRegion() {
        if (layoutControlsMode) {
            JPanel ntControls = new JPanel();
            ntControls.setBorder(new TitledBorder(""));

            {
                String tooltip = "Draw";
                String imagePath = "ui/chr/nameTable/drawIcon.png";
                ActionListener actListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        //  nameTableActionMode = DRAW_MODE;
                    }
                };
                JButton button = null;
                ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
                if (icon == null) {
                    button = new JButton(tooltip);
                } else {
                    button = new JButton(icon);
                }
                button.setToolTipText(tooltip);
                button.addActionListener(actListener);
                ntControls.add(button);

            }
            return ntControls;
        } else {
            return null;
        }
    }

    protected JPanel setupNameTablePanel() {
        JPanel nameTablePanel = new JPanel();
        nameTablePanel.setBorder(new TitledBorder("Name Table Editor"));
        nameTablePanel.setLayout(new BorderLayout());

        nameTableRegions = new NameTablePanel[modelRef.getCHRModel().nameTableIndexes.length];
        JTabbedPane nameTablesTabbedPane = new JTabbedPane();
        for (int i = 0; i < nameTableRegions.length; i++) {
            nameTableRegions[i] = new NameTablePanel(this, modelRef, i);
            nameTablesTabbedPane.add("Background " + i, nameTableRegions[i]);
        }

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());

        JPanel ntControls = setupNameTableControlRegion();
        if (ntControls != null) {
            ntControls.setBorder(new TitledBorder("Controls"));
            tempPanel.add(ntControls, BorderLayout.WEST);
        }

        tempPanel.add(nameTablesTabbedPane, BorderLayout.CENTER);

        nameTablePanel.add(tempPanel, BorderLayout.WEST);
        return nameTablePanel;
    }

    // this can be overridden
    protected JPanel setupEntryEditorPanel() {
        JPanel editorPanel = new JPanel();
        editorPanel.setBorder(new TitledBorder("Tile Editor"));
        editorPanel.setLayout(new BorderLayout());
        editorRegion = new CHRTileEditorPanel(modelRef, PPUConstants.IMAGE_PALETTE_TYPE, 8, 8);
        editorPanel.add(editorRegion, BorderLayout.WEST);
        return editorPanel;
    }

    protected JPanel setupPatternTablePanel() {
        patternTableRegions = new PatternTablePanel[2];
        patternTableRegions[0] = new PatternTablePanel(this, modelRef, 0, "BG");
        patternTableRegions[1] = new PatternTablePanel(this, modelRef, 1, "Sprites");
        controlRegion = new CHRModelControlPanel();
        controlRegion.addPatternTable(patternTableRegions[0]);
        controlRegion.addPatternTable(patternTableRegions[1]);
        return controlRegion;
    }

    private JPanel setupCHRPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(java.awt.Color.DARK_GRAY);
        JPanel namePanel = setupNameTablePanel();
        panel.add(namePanel, BorderLayout.WEST);

        JPanel remainder = new JPanel();
        panel.add(remainder, BorderLayout.CENTER);
        remainder.setLayout(new BorderLayout());

        JPanel edPanel = setupEntryEditorPanel();
        if (edPanel != null) {
            remainder.add(edPanel, BorderLayout.NORTH);
        }
        JPanel chrPanel = setupPatternTablePanel();
        if (chrPanel != null) {
            remainder.add(chrPanel, BorderLayout.CENTER);
        }


        return panel;
    }

    private JPanel setupPalettePanel() {
        paletteRegion = new PalettePanel(this, modelRef.getCHRModel(), true, true);
        return paletteRegion;
    }

    // the parts are:
    // 1) Pattern Table (one for $0000 and one for $1000) his is the 8K file (does not have to be big. Should be able to scroll)
    // 2) Individual CHR Editor 8x8 viewer/editor (should be fixed size)- Needs 16x16 support someday
    // 3) Nametable Layout  (PAL or NTSC)  (should be fixed. May allow scrolling) affects OAM too
    // 4) Palette viewer/editor (can be an external window)
    // 5) Control panel (turn on/off parts, settings, etc..)
    private JPanel setupControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(new TitledBorder("Controls"));
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        controlPanel.setLayout(gbl);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 0;
        gbc.ipadx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        // a special controls mode panel
        if (setupDragAndDrop) {
            JPanel panel00 = setupControlMode();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbl.setConstraints(panel00, gbc);
            controlPanel.add(panel00);
        }


        // next row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        {
            // To DO: change setupControls here...
            JPanel panel01 = setupControls(nameTableRegions[0], "Name Table");
            final JCheckBox oamCB = new JCheckBox("Show OAM Grid");
            oamCB.setSelected(nameTableRegions[0].getShowOAMGrid());
            oamCB.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    nameTableRegions[0].setShowOAMGrid(oamCB.isSelected());
                    for (int i = 0; i < nameTableRegions.length; i++) {
                        nameTableRegions[i].notifyDisplayInterfaceUpdated();
                    }
                }
            });
            panel01.add(oamCB);

            {
                JPanel spinnerPanel = new JPanel();
                spinnerPanel.setLayout(new FlowLayout());
                final JSpinner spinner = new JSpinner();
                Integer value = new Integer(nameTableRegions[0].getScale());
                Integer min = new Integer(NameTablePanel.MIN_SCALE);
                Integer max = new Integer(NameTablePanel.MAX_SCALE);
                Integer step = new Integer(1);
                SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
                spinner.setModel(spinnerModel);
                spinner.setToolTipText("Scale for the Name Table");
                spinner.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        int newVal = ((Integer) spinner.getModel().getValue()).intValue();
                        for (int i = 0; i < nameTableRegions.length; i++) {
                            nameTableRegions[i].setScale(newVal);
                        }
                    }
                });
                spinnerPanel.add(new JLabel("Scale:"));
                spinnerPanel.add(spinner);
                panel01.add(spinnerPanel);
            }

            gbl.setConstraints(panel01, gbc);
            controlPanel.add(panel01);
        }
        if (editorRegion != null) {
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            JPanel panel11 = setupControls(editorRegion, "Entry Editor");
            gbl.setConstraints(panel11, gbc);
            controlPanel.add(panel11);
        }

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        {
            JPanel panel02 = setupControls(patternTableRegions[0], "Background Pattern Table");

            ButtonGroup buttonGroup = new ButtonGroup();
            JRadioButton b1 = new JRadioButton("Background Palette");
            buttonGroup.add(b1);
            panel02.add(b1);

            JRadioButton b2 = new JRadioButton("Sprite Palette");
            buttonGroup.add(b2);
            panel02.add(b2);

            buttonGroup.setSelected(b1.getModel(), true);

            b1.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        patternTableRegions[0].setPaletteType(0);
                    }
                }
            });
            b2.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        patternTableRegions[0].setPaletteType(1);
                    }
                }
            });
            gbl.setConstraints(panel02, gbc);
            controlPanel.add(panel02);
        }

        gbc.gridx = 1;
        gbc.gridy = 2;
        {
            JPanel panel12 = setupControls(patternTableRegions[1], "Sprite Pattern Table");
            ButtonGroup buttonGroup = new ButtonGroup();
            JRadioButton b1 = new JRadioButton("Background Palette");
            buttonGroup.add(b1);
            panel12.add(b1);

            JRadioButton b2 = new JRadioButton("Sprite Palette");
            buttonGroup.add(b2);
            panel12.add(b2);

            buttonGroup.setSelected(b2.getModel(), true);

            b1.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        patternTableRegions[1].setPaletteType(0);
                    }
                }
            });
            b2.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        patternTableRegions[1].setPaletteType(1);
                    }
                }
            });

            gbl.setConstraints(panel12, gbc);
            controlPanel.add(panel12);
        }

        // put it in the top left corner (when scaling)
        JPanel daOtherPanel = new JPanel();
        daOtherPanel.setLayout(new BorderLayout());
        daOtherPanel.add(controlPanel, BorderLayout.NORTH);
        JPanel daPanel = new JPanel();
        daPanel.setLayout(new BorderLayout());
        daPanel.add(daOtherPanel, BorderLayout.WEST);

        return daPanel;
    }

    protected void clearAnimationPanel() {
        if (lowerPanel != null) {
            lowerPanel.removeAll();
            lowerPanel.setBorder(null);
            animationGrid = null;
            if (nameTableRegions != null && nameTableRegions.length > 0) {
                nameTableRegions[0].setImageLevel(null, NameTablePanel.SPRITE_IMG_LEVEL);
            }
        }
        revalidate();
        pack();
        notifyPatternTableChanged();

    }

    private void reorderTiles(int tileOffset, int sx, int sy, int wid, int hgt){
    	System.out.println("Attempting to re-order tiles to group at:" + tileOffset + ":: " +sx + "," + sy + " ->" + wid + "," + hgt);
    	if(wid>16 || hgt > 16){
    		System.err.println("Can only re-order up to a 16x16 block");
    		return;
    	}
    	 
    	
    	// I do not need to do anything to OAM
    	// just the nametables and the tiles themselves
    	
    	// first step, backup the nametables
    	int numTables = modelRef.getCHRModel().nameTableIndexes.length;
    	byte origTables[][] = new byte[numTables][PPUConstants.NUM_NAME_TABLE_ENTRIES];
    	for(int i=0;i<numTables;i++){
    		System.arraycopy(modelRef.getCHRModel().nameTableIndexes[i],0,origTables[i],0,PPUConstants.NUM_NAME_TABLE_ENTRIES);
    	}
    	// back up the tiles.
    	byte origTiles[] = new byte[PPUConstants.PATTERN_TABLE_PAGE_SIZE];
    	System.arraycopy(modelRef.getCHRModel().patternTable[0],0,origTiles,0,PPUConstants.PATTERN_TABLE_PAGE_SIZE);
    	
    	// now re-order the tiles
    	// move the region to be starting at the tileOffset
    	for(int y=0;y<hgt;y++){
    		for(int x=0;x<wid;x++){
    			int ntIndex = (x+sx) + (y+sy)*PPUConstants.NAME_TABLE_WIDTH;
    			int origTileIndexInt = (int)((origTables[0][ntIndex]) & 0xFF);
    			byte origTileIndexByte = (byte)origTileIndexInt;
    			
    			int newTileIndexInt =  (int)((tileOffset + x + y*16)  & 0xFF);
    			byte newTileIndexByte = (byte)newTileIndexInt;
    			
    			if(origTileIndexInt != newTileIndexInt) {
        			System.out.println("Swapping " + x + "," + y + " from:" + origTileIndexInt +  " to:" + newTileIndexInt);
    				// swap nametables
    				for(int i=0;i<numTables;i++){
    					for(int j=0;j<origTables[i].length;j++) {
    						if(origTables[i][j] == origTileIndexByte){
    							origTables[i][j] = newTileIndexByte;
    						} else if(origTables[i][j] == newTileIndexByte){
    							origTables[i][j] = origTileIndexByte;
    						}
    						
    					}
    				}
    				// swap tiles
    				byte tmpBytes[] = new byte[PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY];
    				System.arraycopy(origTiles,origTileIndexInt*PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY, tmpBytes,0,PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY);
    				System.arraycopy(origTiles,newTileIndexInt*PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY, origTiles,origTileIndexInt*PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY,PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY);
    				System.arraycopy(tmpBytes,0,origTiles,newTileIndexInt*PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY, PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY);
    			}
    		}
    	}
    	// load em back into the model
    	// nametables
    	for(int i=0;i<numTables;i++){
    		System.arraycopy(origTables[i],0,modelRef.getCHRModel().nameTableIndexes[i],0,PPUConstants.NUM_NAME_TABLE_ENTRIES);
    	}
    	
    	// tiles
    	System.arraycopy(origTiles,0,modelRef.getCHRModel().patternTable[0],0,PPUConstants.PATTERN_TABLE_PAGE_SIZE);
    	notifyPatternTableChanged();

    }
    
    protected void createStamp() {
    	createStamp(0);
    }
    protected void createStamp(int ntIndex) {
    	GridMouseResultsModel activeRegion = nameTableRegions[ntIndex].resultsModel;
    	if(activeRegion.isBoxValid()){
            File stampFile = FileUtilities.selectFileForSave(this);
            if (stampFile == null) {
                return;
            }
            CHRTile[][] t = getStampTiles(ntIndex, activeRegion.startX, activeRegion.startY, activeRegion.endX, activeRegion.endY);
            StampUtilities.storeStampTiles(stampFile, t, defaultObjectType);
    	}
    }
    
    private CHRTile[][] getStampTiles(int ntIndex, int sx, int sy, int ex, int ey) {
        CHRTile[][] t = new CHRTile[ex - sx][ey - sy];

        for (int i = sx; i < ex; i++) {
            for (int j = sy; j < ey; j++) {
                int off = j * 32 + i;
                int index = (modelRef.getCHRModel().nameTableIndexes[ntIndex][off] & 0xFF);
                int oamVal = modelRef.getCHRModel().getOAMFromNTIndex(0, off);
                int pixOffset = 16 * index;
                byte mask[] = new byte[16];
                System.arraycopy(modelRef.getCHRModel().patternTable[0], pixOffset, mask, 0, 16);
                t[i - sx][j - sy] = new CHRTile(mask, oamVal, modelRef);
            }
        }
        return t;

    }
    protected void launchNameTableReorder() {
    	if(nameTableRegions[0].resultsModel.isBoxValid()){
    	        final JDialog dlog = new JDialog();
    	        dlog.getContentPane().setLayout(new BorderLayout());
    	        dlog.setModal(false);

    	        final JSpinner startPosSpinner = new JSpinner();
    	        int curStart = EnvironmentUtilities.getIntegerEnvSetting(CHR_BANK_OFFSET, DEFAULT_CHR_BANK_OFFSET);
    	        {
    	            if (curStart < 0) {
    	                curStart = 0;
    	            }
    	            if (curStart >= PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE) {
    	                curStart = PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE - 1;
    	            }
    	            Integer value = new Integer(curStart);
    	            Integer min = new Integer(0);
    	            Integer max = new Integer(PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE - 1);
    	            Integer step = new Integer(1);
    	            SpinnerNumberModel spinModel = new SpinnerNumberModel(value, min, max, step);
    	            startPosSpinner.setModel(spinModel);
    	        }
    	        startPosSpinner.setToolTipText("Starting Position");

    	        JPanel ctrlPanel = new JPanel();
    	        ctrlPanel.add(new JLabel("New Starting Tile:"));
    	        ctrlPanel.add(startPosSpinner);

    	        dlog.getContentPane().add(ctrlPanel, BorderLayout.CENTER);

    	        JButton closeMe = new JButton("Close");
    	        closeMe.addActionListener(new ActionListener() {

    	            public void actionPerformed(ActionEvent e) {
    	                dlog.dispose();
    	            }
    	        });

    	        JButton okBut = new JButton("OK");
    	        okBut.addActionListener(new ActionListener() {

    	            public void actionPerformed(ActionEvent e) {
    	                int tileOffset = ((Integer) startPosSpinner.getModel().getValue()).intValue() * PPUConstants.BYTES_PER_PATTERN_TABLE_ENTRY;
    	                reorderTiles(tileOffset, nameTableRegions[0].resultsModel.getStartX(), nameTableRegions[0].resultsModel.getStartY(), nameTableRegions[0].resultsModel.getWidth(), nameTableRegions[0].resultsModel.getHeight());
    	                dlog.dispose();
    	            }
    	        });

    	        JPanel butPanel = new JPanel();
    	        butPanel.add(closeMe);
    	        butPanel.add(okBut);
    	        dlog.getContentPane().add(butPanel, BorderLayout.SOUTH);
    	        dlog.pack();
    	        dlog.setVisible(true);
    	}
    }
    
    protected void setupAnimationPanel() {
        if (lowerPanel != null) {
            lowerPanel.removeAll();
            animationGrid = null;
        } else {
            lowerPanel = new JPanel();
            getContentPane().add(lowerPanel, BorderLayout.SOUTH);
        }
        lowerPanel.setBorder(new TitledBorder("Animation"));
        lowerPanel.setLayout(new BorderLayout());
        // Need a slider that allows moving along the frames
        // Likely need to label it, or add a tooltip to indicate frames
        // Likely need a scrollpane since it may get large
        // Need row's to correspond to each tile
        // Need a status area to show settings per tile
        // Need to graphically show where changes occur (keyframe points)
        // Need checkbox to indicate if a sprite is on or not

        animationGrid = new AnimationKeyFrameComponent(modelRef);
        JScrollPane animationPane = new JScrollPane(animationGrid);

        animationGrid.setRunnable(animationGrid);
        animationGrid.setNameTablePanel(nameTableRegions[0]);
        final AnimationKeyFrameComponent ag = animationGrid;

        JPanel controlsPanel = new JPanel();
        final JCheckBox animCB = new JCheckBox("Enable Animation");
        animCB.setSelected(ag.isAnimationMode());
        animCB.setToolTipText("Enable Animation to view any sprite animations added to this screen");
        animCB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ag.setAnimationMode(animCB.isSelected());
            }
        });

        ag.setAnimationModeController(animCB);

        controlsPanel.add(animCB);

        final JCheckBox ntscCB = new JCheckBox("NTSC Mode?");
        ntscCB.setSelected(ag.isNTSCMode());
        ntscCB.setToolTipText("Specifies the animation framerate. Select to use NTSC (60 fps). De-select to use PAL (50 fps)");
        ntscCB.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                ag.setNTSCMode(ntscCB.isSelected());
            }
        });
        controlsPanel.add(ntscCB);



        JButton addButton = new JButton("Add Animation");
        addButton.setToolTipText("Add an animation entry to provide a sprite animation");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ag.addAnimation();
            }
        });
        controlsPanel.add(addButton);
        lowerPanel.add(controlsPanel, BorderLayout.NORTH);

        animationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        animationPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        lowerPanel.add(animationPane, BorderLayout.CENTER);

        lowerPanel.add(animationGrid.getSettingsPanel(), BorderLayout.EAST);

        animationPane.setPreferredSize(new Dimension(180, 120));
        revalidate();
        pack();
        notifyPatternTableChanged();
    }

    private void setupUI(boolean autoLoad) {

        getContentPane().setLayout(new BorderLayout());
        setupMenuBar();

        JTabbedPane bgPanel = new JTabbedPane();
        JScrollPane scrollPane = new JScrollPane(bgPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JPanel chrPanel = setupCHRPanel();
        bgPanel.addTab("Backgrounds", chrPanel);

        JPanel palettePanel = setupPalettePanel();
        bgPanel.addTab("Palette", palettePanel);

        JPanel controlsPanel = setupControlPanel();
        bgPanel.addTab("Settings", controlsPanel);

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        pack();

        //Set the window's location.
        setLocation(0, 0);

        if (autoLoad) {
            modelRef.loadLastProject(this);
            updateCompressionInfo();
        }
    }

    protected JPanel setupControlMode() {
        JPanel controlModePanel = new JPanel();
        controlModePanel.setBorder(new TitledBorder("All (Mode)"));
        controlModePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton b1 = new JRadioButton("Normal");
        b1.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    modelRef.setControlMode(CHREditorModel.CONTROL_MODE_NORMAL);
                }
            }
        });
        controlModePanel.add(b1);
        buttonGroup.add(b1);


        JRadioButton b2 = new JRadioButton("Drag and Drop");
        b2.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    modelRef.setControlMode(CHREditorModel.CONTROL_MODE_DRAG);
                }
            }
        });
        controlModePanel.add(b2);
        buttonGroup.add(b2);

        buttonGroup.setSelected(b1.getModel(), true);
        return controlModePanel;

    }

    private JPanel setupControls(final CHRDisplayInterface controllableType, String title) {
        JPanel subControlsPanel = new JPanel();
        subControlsPanel.setBorder(new TitledBorder(title));
        subControlsPanel.setLayout(new BoxLayout(subControlsPanel, BoxLayout.Y_AXIS));

        final JCheckBox gridCB = new JCheckBox("Show Grid");
        gridCB.setSelected(controllableType.getControls().getShowTileGrid());
        gridCB.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                controllableType.getControls().setShowTileGrid(gridCB.isSelected());
                controllableType.notifyDisplayInterfaceUpdated();
            }
        });
        subControlsPanel.add(gridCB);

        final JCheckBox selectionCB = new JCheckBox("Show Selection");
        selectionCB.setSelected(controllableType.getControls().getShowSelection());
        selectionCB.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                controllableType.getControls().setShowSelection(selectionCB.isSelected());
                controllableType.notifyDisplayInterfaceUpdated();
            }
        });
        subControlsPanel.add(selectionCB);
        return subControlsPanel;
    }

    public void notifyPatternTableChanged() {
        for (int i = 0; i < patternTableRegions.length; i++) {
            if (patternTableRegions[i] != null) {
                patternTableRegions[i].updatePatternTable();
            }
        }
        for (int i = 0; i < nameTableRegions.length; i++) {
            if (nameTableRegions[i] != null) {
                nameTableRegions[i].updateNameTableTiles(true);
            }
        }
        if (animationGrid != null) {
            animationGrid.updateAnimationView();
        }
    }

    public void notifyNameTableChanged() {
        for (int i = 0; i < nameTableRegions.length; i++) {
            if (nameTableRegions[i] != null) {
                nameTableRegions[i].updateNameTableTiles(true);
            }
        }
    }

    public void notifyImagePaletteChanged() {
        if (paletteRegion != null) {
            paletteRegion.resetPalette();
        }
        for (int i = 0; i < patternTableRegions.length; i++) {
            if (patternTableRegions[i] != null) {
                patternTableRegions[i].updatePatternTable();
            }
        }
        for (int i = 0; i < nameTableRegions.length; i++) {
            if (nameTableRegions[i] != null) {
                nameTableRegions[i].updateNameTableTiles(true);
            }
        }
        if (editorRegion != null) {
            editorRegion.repaint();
        }
    }

    public void notifySpritePaletteChanged() {
        if (paletteRegion != null) {
            paletteRegion.resetPalette();
        }
        for (int i = 0; i < patternTableRegions.length; i++) {
            if (patternTableRegions[i] != null) {
                patternTableRegions[i].updatePatternTable();
            }
        }
        for (int i = 0; i < nameTableRegions.length; i++) {
            if (nameTableRegions[i] != null) {
                nameTableRegions[i].updateNameTableTiles(true);
            }
        }
        if (editorRegion != null) {
            editorRegion.repaint();
        }
        if (animationGrid != null) {
            animationGrid.updateAnimationView();
        }

    }

    public boolean supportPageSwitch(int pageNum) {
        return (pageNum == 0);
    }

    public void notifyPatternTableSelected(int pageNum, int index) {

        if (supportPageSwitch(pageNum)) {
            modelRef.lastPageNum = pageNum;
        }
        modelRef.lastPatternIndex = index;
        if (editorRegion != null) {
            editorRegion.setTile(index, modelRef.patternTableTiles[pageNum][index].asMask());
        }
        for (int i = 0; i < nameTableRegions.length; i++) {
            if (nameTableRegions[i] != null) {
                nameTableRegions[i].updateNameTableTiles(true);
            }
        }
    }

    public void notifyPatternTableToBeModified(int pageNum, int index) {
        if (editorRegion != null) {
            byte b[] = editorRegion.getTileMask();
            int byteOffset = index * 16;
            System.arraycopy(b, 0, modelRef.getCHRModel().patternTable[pageNum], byteOffset, 16);
        }

        if (supportPageSwitch(pageNum)) {
            modelRef.lastPageNum = pageNum;
        }
        modelRef.lastPatternIndex = index;

        // redundant
        if (editorRegion != null) {
            editorRegion.setTile(index, modelRef.patternTableTiles[pageNum][index].asMask());
        }

        for (int i = 0; i < nameTableRegions.length; i++) {
            if (nameTableRegions[i] != null) {
                nameTableRegions[i].updateNameTableTiles(true);
            }
        }
        if (animationGrid != null) {
            animationGrid.updateAnimationView();
        }

    }

    private void updateCompressionInfo() {
        if (modelRef == null) {
            return;
        }
        int compressVal = modelRef.getNameTableCompression();
        Enumeration<AbstractButton> modelEnum = bg.getElements();
        AbstractButton tempButtonModel = modelEnum.nextElement();
        while (compressVal > 0 && modelEnum.hasMoreElements()) {
            compressVal--;
            tempButtonModel = modelEnum.nextElement();
        }
        if (tempButtonModel != null) {
            tempButtonModel.setSelected(true);
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
}
