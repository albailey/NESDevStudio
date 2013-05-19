/*
 * FontHelper.java
 *
 * Created on August 28, 2007, 2:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ui.fontHelper;

import gameTools.screenLayout.ScreenLayoutUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ui.chr.model.CHREditorModel;
import utilities.EnvironmentUtilities;
import utilities.GUIUtilities;

/**
 *
 * @author abailey
 */
public class FontHelper extends JInternalFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4351931087612664470L;
	public final static String FRAME_TITLE = "Font Helper";
    public final static int UTILITY_WIDTH = 256;
    public final static int UTILITY_HEIGHT = 240;
    public final static int FONT_LIST_WIDGET_WIDTH = 64;
    public final static int FONT_LIST_WIDGET_HEIGHT = 60;
    public final static int FONT_DISPLAY_WIDTH = 256;
    public final static int FONT_DISPLAY_HEIGHT = 240;
    // properties used by font helper
    public static final String FONT_HELPER_LAST_FONT_INDEX = "FONT_HELPER_LAST_FONT_INDEX";
    public static final int DEFAULT_FONT_HELPER_LAST_FONT_INDEX = 0;
    public static final String FONT_HELPER_LAST_FONT_SIZE = "FONT_HELPER_LAST_FONT_SIZE";
    public static final int DEFAULT_FONT_HELPER_LAST_FONT_SIZE = 10;
    public static final String FONT_HELPER_LAST_FONT_BOLD = "FONT_HELPER_LAST_FONT_BOLD";
    public static final boolean DEFAULT_FONT_HELPER_LAST_FONT_BOLD = false;
    public static final String FONT_HELPER_LAST_FONT_ITALIC = "FONT_HELPER_LAST_FONT_ITALIC ";
    public static final boolean DEFAULT_FONT_HELPER_LAST_FONT_ITALIC = false;
    public static final String FONT_HELPER_LAST_FONT_ANTI_ALIASING = "FONT_HELPER_LAST_FONT_ANTI_ALIASING";
    public static final boolean DEFAULT_FONT_HELPER_LAST_FONT_ANTI_ALIASING = false;
    public static final String FONT_HELPER_UPPER_CASE_CHECKED = "FONT_HELPER_UPPER_CASE_CHECKED";
    public static final boolean DEFAULT_FONT_HELPER_UPPER_CASE_CHECKED = true;
    public static final String FONT_HELPER_LOWER_CASE_CHECKED = "FONT_HELPER_LOWER_CASE_CHECKED";
    public static final boolean DEFAULT_FONT_HELPER_LOWER_CASE_CHECKED = true;
    public static final String FONT_HELPER_NUMBERS_CHECKED = "FONT_HELPER_NUMBERS_CHECKED";
    public static final boolean DEFAULT_FONT_HELPER_NUMBERS_CHECKED = true;
    public static final String FONT_HELPER_PUNCTUATION_CHECKED = "FONT_HELPER_PUNCTUATION_CHECKED";
    public static final boolean DEFAULT_FONT_HELPER_PUNCTUATION_CHECKED = true;
    public static final String FONT_HELPER_ASCII_FORMAT_CHECKED = "FONT_HELPER_ASCII_FORMAT_CHECKED";
    public static final boolean DEFAULT_FONT_HELPER_ASCII_FORMAT_CHECKED = true;
    public static final String FONT_HELPER_LAST_FONT_LEFT_SPACING = "FONT_HELPER_LAST_FONT_LEFT_SPACING";
    public static final int DEFAULT_FONT_HELPER_LAST_FONT_LEFT_SPACING = 1;
    public static final String FONT_HELPER_LAST_FONT_BOTTOM_SPACING = "FONT_HELPER_LAST_FONT_BOTTOM_SPACING";
    public static final int DEFAULT_FONT_HELPER_LAST_FONT_BOTTOM_SPACING = 1;
    public static final String FONT_HELPER_PER_CHARACTER_TILE_WIDTH_STRING = "PER_CHARACTER_TILE_WIDTH_STRING";
    public static final int DEFAULT_FONT_HELPER_PER_CHARACTER_TILE_WIDTH_STRING = 1;
    public static final String FONT_HELPER_PER_CHARACTER_TILE_HEIGHT_STRING = "PER_CHARACTER_TILE_HEIGHT_STRING";
    public static final int DEFAULT_FONT_HELPER_PER_CHARACTER_TILE_HEIGHT_STRING = 1;
    // display strings
    public static final String FONT_HELPER_SAMPLE_TEXT = "FONT_HELPER_SAMPLE_TEXT";
    public static final String DEFAULT_FONT_HELPER_SAMPLE_TEXT = "The Quick Brown Fox";
    private static final String FONT_HELPER_SAMPLE_TEXT_TOOLTIP = "The sample text to display with the selected font";
    private static final String FONT_HELPER_ACTUAL_TEXT_TOOLTIP = "This is the sample text using the ACTUAL font size. Un-editable";
    private static final String FONT_SIZE_TOOLTIP = "Size of the Font (point size)";
    private static final String FONT_BOLD_TOOLTIP = "Bold";
    private static final String FONT_ITALIC_TOOLTIP = "Italic";
    private static final String ANTIALIASING_TOOLTIP = "Antialiasing";
    private static final String FONT_LEFT_SPACING_STRING = "Font Left Spacing";
    private static final String FONT_LEFT_SPACING_STRING_TOOLTIP = "Amount of spacing added to the left of each character. For cursive, this should be zero";
    private static final String FONT_BOTTOM_SPACING_STRING = "Font Bottom Spacing";
    private static final String FONT_BOTTOM_SPACING_STRING_TOOLTIP = "Where the baseline of the character is situated.  Important when using lower case.";
    private static final String FONT_TILES_WIDE_STRING = "Tiles Wide per Character";
    private static final String FONT_TILES_WIDE_STRING_TOOLTIP = "Number of tiles WIDE each character is.  This would usually be 1.";
    private static final String FONT_TILES_HIGH_STRING = "Tiles High per Character";
    private static final String FONT_TILES_HIGH_STRING_TOOLTIP = "Number of tiles HIGH each character is.  This would usually be 1.";
    private static final String UPPER_CASE_STRING = "Upper Case";
    private static final String LOWER_CASE_STRING = "Lower Case";
    private static final String NUMBERS_STRING = "Numbers";
    private static final String PUNCTUATION_STRING = "Punctuation";
    private static final String ASCII_STRING = "ASCII Order";
    private static final String UPPER_CASE_STRING_TOOLTIP = "Whether to generate Upper Case characters";
    private static final String LOWER_CASE_STRING_TOOLTIP = "Whether to generate Lower Case characters";
    private static final String NUMBERS_STRING_TOOLTIP = "Whether to generate Number characters";
    private static final String PUNCTUATION_STRING_TOOLTIP = "Whether to generate Punctuation characters";
    private static final String ASCII_STRING_TOOLTIP = "Whether to generate the output in ASCII format and ASCII positions in the CHR";
    private CHREditorModel modelRef = null;
    // GUI update related stuff
    private boolean initializing = false;
    private Font[] fontList = null;
    private JList fontListWidget = null;
    private JTextField srcTextField = null;
    private JTextField actualTextField = null;
    private FontPanel destFontArea = null;
    private FontPanel scaledDestFontArea = null;
    private JSpinner sizeSpinner = null;
    private JSpinner leftSpacingSpinner = null;
    private JSpinner bottomSpacingSpinner = null;
    private JSpinner tilesWideSpinner = null;
    private JSpinner tilesHighSpinner = null;
    private JCheckBox boldCheckBox = null;
    private JCheckBox italicCheckBox = null;
    private JCheckBox antialiasingBox = null;
    private JCheckBox upperCaseCheckBox = null;
    private JCheckBox lowerCaseCheckBox = null;
    private JCheckBox numbersCheckBox = null;
    private JCheckBox punctuationCheckBox = null;
    private JCheckBox asciiOrderCheckBox = null;
    private int scale = 3;

    /** Creates a new instance of FontHelper */
    public FontHelper() {
        this(new CHREditorModel());
    }

    public FontHelper(CHREditorModel newModelRef) {
        super(FRAME_TITLE, true, true, false, false);
        modelRef = newModelRef;
        setupUI();
    }

    // here is the plan:
    // load in an image from file.
    // Dispay the image at 256*240 pixels (NTSC)
    // Display the image again: using the limited 64 color NES palette
    // Allow the user to generate it
    private void setupUI() {
        initializing = true; // this turns off reacting to changes to the fields
        getContentPane().setLayout(new BorderLayout());
        setupMenuBar();

        JPanel controlRegion = new JPanel();
        controlRegion.setLayout(new BorderLayout());

        controlRegion.add(setupFontList(), BorderLayout.NORTH);   // returns a scroll pane
        controlRegion.add(setupControls(), BorderLayout.CENTER);



        // now the sample text to display
        actualTextField = new JTextField();
        actualTextField.setToolTipText(FONT_HELPER_ACTUAL_TEXT_TOOLTIP);
        actualTextField.setEditable(false);

        srcTextField = new JTextField();
        String sampleText = EnvironmentUtilities.getStringEnvSetting(FONT_HELPER_SAMPLE_TEXT, DEFAULT_FONT_HELPER_SAMPLE_TEXT);
        srcTextField.setText(sampleText);
        srcTextField.setToolTipText(FONT_HELPER_SAMPLE_TEXT_TOOLTIP);

        srcTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String txt = srcTextField.getText();
                if (txt != null && txt.length() > 0) {
                    EnvironmentUtilities.updateStringEnvSetting(FONT_HELPER_SAMPLE_TEXT, txt);
                }
                updateFontHelper();
            }
        });
        JPanel txtPanel = new JPanel();
        txtPanel.setLayout(new BorderLayout());
        txtPanel.add(actualTextField, BorderLayout.NORTH);
        txtPanel.add(srcTextField, BorderLayout.CENTER);

        getContentPane().add(txtPanel, BorderLayout.SOUTH);   // returns a scroll pane
        getContentPane().add(controlRegion, BorderLayout.WEST);   // returns a scroll pane


        JPanel outerPanel1 = new JPanel();
        outerPanel1.setBorder(new TitledBorder("Font Set"));
        outerPanel1.setLayout(new BorderLayout());
        destFontArea = new FontPanel(1, FONT_DISPLAY_WIDTH, FONT_DISPLAY_HEIGHT);
        outerPanel1.add(destFontArea, BorderLayout.CENTER);

        JPanel outerPanel2 = new JPanel();
        outerPanel2.setBorder(new TitledBorder("Scaled Font Set"));
        outerPanel2.setLayout(new BorderLayout());
        scaledDestFontArea = new FontPanel(scale, FONT_DISPLAY_WIDTH, FONT_DISPLAY_HEIGHT);
        outerPanel2.add(scaledDestFontArea, BorderLayout.CENTER);
        JScrollPane scaleSrollRegion = new JScrollPane(outerPanel2);

        JPanel alphabetPanel = new JPanel();
        alphabetPanel.setMinimumSize(new Dimension(FONT_DISPLAY_WIDTH, FONT_DISPLAY_HEIGHT * 2));
        alphabetPanel.setPreferredSize(new Dimension(FONT_DISPLAY_WIDTH, FONT_DISPLAY_HEIGHT * 2));
        alphabetPanel.setLayout(new BorderLayout());
        alphabetPanel.add(outerPanel1, BorderLayout.NORTH);
        alphabetPanel.add(scaleSrollRegion, BorderLayout.CENTER);

        JScrollPane scrollRegion = new JScrollPane(alphabetPanel);


        getContentPane().add(scrollRegion, BorderLayout.CENTER);

        initializing = false; // this turns ON reacting to changes to the fields so we can call updateFontHelper and have it do something
        updateFontHelper();

        pack();

        //Set the window's location.
        setLocation(0, 0);
    }

    private int getFontStyle() {
        int fontStyle = Font.PLAIN;
        if (boldCheckBox.isSelected()) {
            fontStyle = fontStyle | Font.BOLD;
        }
        if (italicCheckBox.isSelected()) {
            fontStyle = fontStyle | Font.ITALIC;
        }
        return fontStyle;
    }

    private int getFontSize() {
        return ((Integer) sizeSpinner.getModel().getValue()).intValue();
    }

    private Font determineFont(boolean fixedSize) {
        Font f = fontList[fontListWidget.getSelectedIndex()];

        // apply other crap
        if (fixedSize) {
            return f.deriveFont(getFontStyle(), 20);
        } else {
            return f.deriveFont(getFontStyle(), getFontSize());
        }
    }

    private void drawFontCriteria(Font f) {
//        destFontArea.setFont(f, srcTextField.getText());
//        scaledDestFontArea.setFont(f, srcTextField.getText());
        // OK, here is the challenge.
        FontCriteriaModel fontModel = new FontCriteriaModel(f,
                upperCaseCheckBox.isSelected(),
                lowerCaseCheckBox.isSelected(),
                numbersCheckBox.isSelected(),
                punctuationCheckBox.isSelected(),
                asciiOrderCheckBox.isSelected(),
                ((Integer) leftSpacingSpinner.getModel().getValue()).intValue(),
                ((Integer) bottomSpacingSpinner.getModel().getValue()).intValue(),
                ((Integer) tilesWideSpinner.getModel().getValue()).intValue(),
                ((Integer) tilesHighSpinner.getModel().getValue()).intValue());
        fontModel.isAntiAliasing = antialiasingBox.isSelected();

        destFontArea.setFontModel(fontModel);
        scaledDestFontArea.setFontModel(fontModel);

    }

    private void updateFontHelper() {
        if (initializing) {
            return;
        }
        Font f = determineFont(true);
        if (f != null) {
            srcTextField.setFont(f);
        }
        f = determineFont(false);
        if (f != null) {
            actualTextField.setFont(f);
            actualTextField.setText(srcTextField.getText());
            drawFontCriteria(f);
        }
        repaint();
    }

    private Component setupFontList() {
        // construct the font list.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fontList = ge.getAllFonts();

        Arrays.sort(fontList, new Comparator<Font>() {

            public int compare(Font o1, Font o2) {
                return (o1.getFontName().compareToIgnoreCase(o2.getFontName()));
            }

            public boolean equals(Object obj) {
                return (obj == this);
            }
        });

        fontListWidget = new JList(
                new AbstractListModel() {

                    /**
					 * 
					 */
					private static final long serialVersionUID = 5114229287494902253L;

					public int getSize() {
                        return fontList.length;
                    }

                    public Object getElementAt(int i) {
                        return fontList[i].getFontName();
                    } // getName();
                });
        fontListWidget.setMaximumSize(new Dimension(FONT_LIST_WIDGET_WIDTH, FONT_LIST_WIDGET_HEIGHT));
        fontListWidget.setAutoscrolls(true);
        fontListWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane fontScrollPane = new JScrollPane(fontListWidget);
        fontScrollPane.setBorder(new TitledBorder("Font List"));

        int lastSelectedFontIndex = EnvironmentUtilities.getIntegerEnvSetting(FONT_HELPER_LAST_FONT_INDEX, DEFAULT_FONT_HELPER_LAST_FONT_INDEX);
        // protect ourselves from out of bounds problems
        if (lastSelectedFontIndex < 0) {
            lastSelectedFontIndex = 0;
        }
        if (lastSelectedFontIndex >= fontList.length) {
            lastSelectedFontIndex = 0;
        }
        fontListWidget.setSelectedIndex(lastSelectedFontIndex);
        if (lastSelectedFontIndex > 4) { // we can fit approximately 8 in the screen, so only scroll if we are beyond 4
            fontListWidget.scrollRectToVisible(fontListWidget.getCellBounds(lastSelectedFontIndex - 4, lastSelectedFontIndex));
        }

        fontListWidget.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int newIndex = fontListWidget.getSelectedIndex();
                EnvironmentUtilities.updateIntegerEnvSetting(FONT_HELPER_LAST_FONT_INDEX, newIndex);
                updateFontHelper();
            }
        });

        return fontScrollPane;
    }

    private JPanel setupControls() {
        // now the control region for font settings
        // font size
        JPanel controlsPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        controlsPanel.setLayout(gbl);

        int yPos = 0;
        // font Size (spinner and label)
        {
            JLabel fontSizeLabel = new JLabel("Font Size:");
            fontSizeLabel.setToolTipText(FONT_SIZE_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbl.setConstraints(fontSizeLabel, gbc);
            controlsPanel.add(fontSizeLabel);

            sizeSpinner = new JSpinner();
            int curSize = EnvironmentUtilities.getIntegerEnvSetting(FONT_HELPER_LAST_FONT_SIZE, DEFAULT_FONT_HELPER_LAST_FONT_SIZE);

            Integer value = new Integer(curSize);
            Integer min = new Integer(6);
            Integer max = new Integer(128);
            Integer step = new Integer(1);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
            sizeSpinner.setModel(spinnerModel);
            sizeSpinner.setToolTipText(FONT_SIZE_TOOLTIP);

            gbc.gridx = 1;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbl.setConstraints(sizeSpinner, gbc);
            controlsPanel.add(sizeSpinner);

            sizeSpinner.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    int newSize = ((Integer) sizeSpinner.getModel().getValue()).intValue();
                    EnvironmentUtilities.updateIntegerEnvSetting(FONT_HELPER_LAST_FONT_SIZE, newSize);
                    updateFontHelper();
                }
            });
        }

        {
            yPos++;
            boldCheckBox = new JCheckBox(FONT_BOLD_TOOLTIP);
            boldCheckBox.setToolTipText(FONT_BOLD_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(boldCheckBox, gbc);
            controlsPanel.add(boldCheckBox);
            boolean wasBoldChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_LAST_FONT_BOLD, DEFAULT_FONT_HELPER_LAST_FONT_BOLD);
            boldCheckBox.setSelected(wasBoldChecked);
            boldCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = boldCheckBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_LAST_FONT_BOLD, boxVal);
                    updateFontHelper();
                }
            });
        }
        {
            //  yPos++;
            antialiasingBox = new JCheckBox(ANTIALIASING_TOOLTIP);
            antialiasingBox.setToolTipText(ANTIALIASING_TOOLTIP);
            gbc.gridx = 2;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(antialiasingBox, gbc);
            controlsPanel.add(antialiasingBox);
            boolean wasAntialiasingChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_LAST_FONT_ANTI_ALIASING, DEFAULT_FONT_HELPER_LAST_FONT_ANTI_ALIASING);
            antialiasingBox.setSelected(wasAntialiasingChecked);
            antialiasingBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = antialiasingBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_LAST_FONT_ANTI_ALIASING, boxVal);
                    updateFontHelper();
                }
            });
        }


        {
            yPos++;
            italicCheckBox = new JCheckBox(FONT_ITALIC_TOOLTIP);
            italicCheckBox.setToolTipText(FONT_ITALIC_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(italicCheckBox, gbc);
            controlsPanel.add(italicCheckBox);
            boolean wasItalicChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_LAST_FONT_ITALIC, DEFAULT_FONT_HELPER_LAST_FONT_ITALIC);
            italicCheckBox.setSelected(wasItalicChecked);
            italicCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = italicCheckBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_LAST_FONT_ITALIC, boxVal);
                    updateFontHelper();
                }
            });
        }

        {
            yPos++;
            JLabel characterSetLabel = new JLabel("Character Set");
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(characterSetLabel, gbc);
            controlsPanel.add(characterSetLabel);
        }

        {
            yPos++;
            upperCaseCheckBox = new JCheckBox(UPPER_CASE_STRING);
            upperCaseCheckBox.setToolTipText(UPPER_CASE_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(upperCaseCheckBox, gbc);
            controlsPanel.add(upperCaseCheckBox);
            boolean wasUpCaseChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_UPPER_CASE_CHECKED, DEFAULT_FONT_HELPER_UPPER_CASE_CHECKED);
            upperCaseCheckBox.setSelected(wasUpCaseChecked);
            upperCaseCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = upperCaseCheckBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_UPPER_CASE_CHECKED, boxVal);
                    updateFontHelper();
                }
            });
        }
        {
            yPos++;
            lowerCaseCheckBox = new JCheckBox(LOWER_CASE_STRING);
            lowerCaseCheckBox.setToolTipText(LOWER_CASE_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(lowerCaseCheckBox, gbc);
            controlsPanel.add(lowerCaseCheckBox);
            boolean wasLowerCaseChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_LOWER_CASE_CHECKED, DEFAULT_FONT_HELPER_LOWER_CASE_CHECKED);
            lowerCaseCheckBox.setSelected(wasLowerCaseChecked);
            lowerCaseCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = lowerCaseCheckBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_LOWER_CASE_CHECKED, boxVal);
                    updateFontHelper();
                }
            });
        }
        {
            yPos++;
            numbersCheckBox = new JCheckBox(NUMBERS_STRING);
            numbersCheckBox.setToolTipText(NUMBERS_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(numbersCheckBox, gbc);
            controlsPanel.add(numbersCheckBox);
            boolean wasNumbersChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_NUMBERS_CHECKED, DEFAULT_FONT_HELPER_NUMBERS_CHECKED);
            numbersCheckBox.setSelected(wasNumbersChecked);
            numbersCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = numbersCheckBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_NUMBERS_CHECKED, boxVal);
                    updateFontHelper();
                }
            });
        }
        {
            yPos++;
            punctuationCheckBox = new JCheckBox(PUNCTUATION_STRING);
            punctuationCheckBox.setToolTipText(PUNCTUATION_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(punctuationCheckBox, gbc);
            controlsPanel.add(punctuationCheckBox);
            boolean wasPunctuationChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_PUNCTUATION_CHECKED, DEFAULT_FONT_HELPER_PUNCTUATION_CHECKED);
            punctuationCheckBox.setSelected(wasPunctuationChecked);
            punctuationCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = punctuationCheckBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_PUNCTUATION_CHECKED, boxVal);
                    updateFontHelper();
                }
            });
        }

        {
            yPos++;
            asciiOrderCheckBox = new JCheckBox(ASCII_STRING);
            asciiOrderCheckBox.setToolTipText(ASCII_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbl.setConstraints(asciiOrderCheckBox, gbc);
            controlsPanel.add(asciiOrderCheckBox);
            boolean wasAsciiChecked = EnvironmentUtilities.getBooleanEnvSetting(FONT_HELPER_ASCII_FORMAT_CHECKED, DEFAULT_FONT_HELPER_ASCII_FORMAT_CHECKED);
            asciiOrderCheckBox.setSelected(wasAsciiChecked);
            asciiOrderCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = asciiOrderCheckBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(FONT_HELPER_ASCII_FORMAT_CHECKED, boxVal);
                    updateFontHelper();
                }
            });
        }

        // now for forcing each character to fit inside an 8x8 (or 8x16, or whatever) tile region
        {
            yPos++;
            JLabel fontSpacingLabel = new JLabel(FONT_LEFT_SPACING_STRING);
            fontSpacingLabel.setToolTipText(FONT_LEFT_SPACING_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbl.setConstraints(fontSpacingLabel, gbc);
            controlsPanel.add(fontSpacingLabel);

            leftSpacingSpinner = new JSpinner();
            int curSpacing = EnvironmentUtilities.getIntegerEnvSetting(FONT_HELPER_LAST_FONT_LEFT_SPACING, DEFAULT_FONT_HELPER_LAST_FONT_LEFT_SPACING);

            Integer value = new Integer(curSpacing);
            Integer min = new Integer(0);
            Integer max = new Integer(8);
            Integer step = new Integer(1);
            SpinnerNumberModel spacingSpinnerModel = new SpinnerNumberModel(value, min, max, step);
            leftSpacingSpinner.setModel(spacingSpinnerModel);
            leftSpacingSpinner.setToolTipText(FONT_LEFT_SPACING_STRING_TOOLTIP);

            gbc.gridx = 1;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbl.setConstraints(leftSpacingSpinner, gbc);
            controlsPanel.add(leftSpacingSpinner);

            leftSpacingSpinner.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    int newSpacing = ((Integer) leftSpacingSpinner.getModel().getValue()).intValue();
                    EnvironmentUtilities.updateIntegerEnvSetting(FONT_HELPER_LAST_FONT_LEFT_SPACING, newSpacing);
                    updateFontHelper();
                }
            });
        }

        {
            yPos++;
            JLabel fontBottomSpacingLabel = new JLabel(FONT_BOTTOM_SPACING_STRING);
            fontBottomSpacingLabel.setToolTipText(FONT_BOTTOM_SPACING_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbl.setConstraints(fontBottomSpacingLabel, gbc);
            controlsPanel.add(fontBottomSpacingLabel);

            bottomSpacingSpinner = new JSpinner();
            int curHeightSpacing = EnvironmentUtilities.getIntegerEnvSetting(FONT_HELPER_LAST_FONT_BOTTOM_SPACING, DEFAULT_FONT_HELPER_LAST_FONT_BOTTOM_SPACING);

            Integer value = new Integer(curHeightSpacing);
            Integer min = new Integer(-8);
            Integer max = new Integer(8);
            Integer step = new Integer(1);
            SpinnerNumberModel heightSpacingSpinnerModel = new SpinnerNumberModel(value, min, max, step);
            bottomSpacingSpinner.setModel(heightSpacingSpinnerModel);
            bottomSpacingSpinner.setToolTipText(FONT_HELPER_LAST_FONT_BOTTOM_SPACING);

            gbc.gridx = 1;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbl.setConstraints(bottomSpacingSpinner, gbc);
            controlsPanel.add(bottomSpacingSpinner);

            bottomSpacingSpinner.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    int newSpacing = ((Integer) bottomSpacingSpinner.getModel().getValue()).intValue();
                    EnvironmentUtilities.updateIntegerEnvSetting(FONT_HELPER_LAST_FONT_BOTTOM_SPACING, newSpacing);
                    updateFontHelper();
                }
            });
        }

        {
            yPos++;
            JLabel tilesWideLabel = new JLabel(FONT_TILES_WIDE_STRING);
            tilesWideLabel.setToolTipText(FONT_TILES_WIDE_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbl.setConstraints(tilesWideLabel, gbc);
            controlsPanel.add(tilesWideLabel);

            tilesWideSpinner = new JSpinner();
            int curTilesWide = EnvironmentUtilities.getIntegerEnvSetting(FONT_HELPER_PER_CHARACTER_TILE_WIDTH_STRING, DEFAULT_FONT_HELPER_PER_CHARACTER_TILE_WIDTH_STRING);

            Integer value = new Integer(curTilesWide);
            Integer min = new Integer(0);
            Integer max = new Integer(8);
            Integer step = new Integer(1);
            SpinnerNumberModel tilesWideSpinnerModel = new SpinnerNumberModel(value, min, max, step);
            tilesWideSpinner.setModel(tilesWideSpinnerModel);
            tilesWideSpinner.setToolTipText(FONT_TILES_WIDE_STRING_TOOLTIP);

            gbc.gridx = 1;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbl.setConstraints(tilesWideSpinner, gbc);
            controlsPanel.add(tilesWideSpinner);

            tilesWideSpinner.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    int newVal = ((Integer) tilesWideSpinner.getModel().getValue()).intValue();
                    EnvironmentUtilities.updateIntegerEnvSetting(FONT_HELPER_PER_CHARACTER_TILE_WIDTH_STRING, newVal);
                    updateFontHelper();
                }
            });
        }

        {
            yPos++;
            JLabel tilesHighLabel = new JLabel(FONT_TILES_HIGH_STRING);
            tilesHighLabel.setToolTipText(FONT_TILES_HIGH_STRING_TOOLTIP);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbl.setConstraints(tilesHighLabel, gbc);
            controlsPanel.add(tilesHighLabel);

            tilesHighSpinner = new JSpinner();
            int curTilesHigh = EnvironmentUtilities.getIntegerEnvSetting(FONT_HELPER_PER_CHARACTER_TILE_HEIGHT_STRING, DEFAULT_FONT_HELPER_PER_CHARACTER_TILE_HEIGHT_STRING);

            Integer value = new Integer(curTilesHigh);
            Integer min = new Integer(0);
            Integer max = new Integer(8);
            Integer step = new Integer(1);
            SpinnerNumberModel tilesHighSpinnerModel = new SpinnerNumberModel(value, min, max, step);
            tilesHighSpinner.setModel(tilesHighSpinnerModel);
            tilesHighSpinner.setToolTipText(FONT_TILES_HIGH_STRING_TOOLTIP);

            gbc.gridx = 1;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbl.setConstraints(tilesHighSpinner, gbc);
            controlsPanel.add(tilesHighSpinner);

            tilesHighSpinner.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    int newVal = ((Integer) tilesHighSpinner.getModel().getValue()).intValue();
                    EnvironmentUtilities.updateIntegerEnvSetting(FONT_HELPER_PER_CHARACTER_TILE_HEIGHT_STRING, newVal);
                    updateFontHelper();
                }
            });
        }


        yPos++;
        JPanel filler = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = yPos;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 4;
        gbl.setConstraints(filler, gbc);
        controlsPanel.add(filler);
        return controlsPanel;
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');

        GUIUtilities.createMenuItem(fileMenu, "CHR Viewer", 'C', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // launch a NEW CHR Layout tool
                destFontArea.sendToCHR(modelRef);

                ScreenLayoutUI frame = new ScreenLayoutUI(modelRef);
                frame.setVisible(true); //necessary as of 1.3
                getParent().add(frame);
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
