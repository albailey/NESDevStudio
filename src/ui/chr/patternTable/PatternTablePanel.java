/*
 * PatternTablePanel.java
 *
 * Created on October 3, 2006, 5:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ui.chr.patternTable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import model.NESModelListener;
import ui.NES_UI_Constants;
import ui.chr.CHRDisplayControls;
import ui.chr.CHRDisplayInterface;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.tileEditor.CHRTile;
import ui.input.GridMouseConstraintsModel;
import ui.input.GridMouseResultsModel;
import ui.input.GridMouseResultsTransferHandler;
import ui.input.GridMouseResultsType;
import ui.input.dndOLD.GridMouseInputAndDragAdapter;
import ui.input.dndOLD.MouseActionCallback;
import ui.input.dndOLD.MouseDragCallback;
import ui.input.dndOLD.MouseMoveCallback;
import utilities.ByteFormatter;
import emulator.nes.PPU;

/**
 *
 * @author abailey
 */
public class PatternTablePanel extends JPanel implements CHRDisplayInterface, GridMouseResultsType {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8376594141785864757L;
	public static final String PATTERN_TABLE_PROPERTY_GROUP = "PatternTable";
    public final static int CHR_PIXELS_WIDE = 8;
    public final static int CHR_PIXELS_HIGH = 8;
    private CHREditorModel modelRef;
    private CHRDisplayControls controls = null;
    private MemoryImageSource mis = null;
    private NESModelListener listener = null;
    private Image img = null;
    private int pix[] = null;
    private int patternPix[] = null;
    private int pageNum;
    private int paletteNum;
    private int selectedIndex = 0;
    private boolean showSelectOutline = true;
    private int maxWidth;
    private int maxHeight;
    private GridMouseConstraintsModel constraintsModel = null;
    private GridMouseResultsModel resultsModel = null;
    private TransferHandler transferHandler = null;
    private GridMouseInputAndDragAdapter dragAdapter = null;
    public String panelTitle;

    /** Creates a new instance of PatternTablePanel */
    public PatternTablePanel(NESModelListener callback, CHREditorModel model, int pageNumber, String title) {
        this(callback, model, pageNumber, title, 0);
    }

    public PatternTablePanel(NESModelListener callback, CHREditorModel model, int pageNumber, String title, int palette) {
        panelTitle = title;
        listener = callback;
        modelRef = model;
        pageNum = pageNumber;
        paletteNum = palette;
        maxWidth = PPUConstants.COLUMNS_PER_PATTERN_PAGE * CHR_PIXELS_WIDE;
        maxHeight = PPUConstants.ROWS_PER_PATTERN_PAGE * CHR_PIXELS_HIGH;
        controls = new CHRDisplayControls(PATTERN_TABLE_PROPERTY_GROUP);
        transferHandler = null;
        constraintsModel = null;
        setupUI();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setActive() {
        processLeftClick(0, false);
    }

    public void setPaletteType(int val) {
        if (val == 0 || val == 1) {
            paletteNum = val;
        }
        updatePatternTable();
    }

    public void setInActive() {
    }

    public void notifyDisplayInterfaceUpdated() {
        updatePatternTable();
        repaint();
    }

    public CHRDisplayControls getControls() {
        return controls;
    }

    private void setupUI() {
        setBackground(Color.WHITE);
        if (modelRef != null) {
            modelRef.resetTiles(pageNum);
        }
        pix = new int[maxWidth * maxHeight];// all values are zero.
        patternPix = new int[maxWidth * maxHeight];// all values are zero.
        mis = new MemoryImageSource(maxWidth, maxHeight, pix, 0, maxWidth);
        mis.setAnimated(true);

        updatePatternTable();

        setMinimumSize(new Dimension(maxWidth, maxHeight));

        setPreferredSize(new Dimension(maxWidth, maxHeight));

        setMaximumSize(new Dimension(maxWidth, maxHeight));

        setupMouseCallbacks();

    }

    // not very efficient
    public void updatePatternTable() {
        if (modelRef == null) {
            return;
        }

        // int offset = PATTERN_TABLE_PAGE_SIZE * pageNum;
        byte mask[] = new byte[16];
        int offset = 0;
        int pixelsWide = PPUConstants.COLUMNS_PER_PATTERN_PAGE * CHR_PIXELS_WIDE;
        int ySteps = pixelsWide * CHR_PIXELS_HIGH;
        int tileOffset = 0;
        int tilePix[] = null;
        int pixOffset = 0;
        int tilePixOffset = 0;
        int ty = 0;
        int tx = 0;
        for (ty = 0; ty < PPUConstants.ROWS_PER_PATTERN_PAGE; ty++) {
            for (tx = 0; tx < PPUConstants.COLUMNS_PER_PATTERN_PAGE; tx++) {
                pixOffset = (ty * ySteps) + (tx * CHR_PIXELS_WIDE);
                // get the tile.
                System.arraycopy(modelRef.getCHRModel().patternTable[pageNum], offset, mask, 0, 16);
                offset += 16;
                CHRTile tile = new CHRTile(mask, paletteNum, modelRef);
                modelRef.patternTableTiles[pageNum][tileOffset] = tile;
                tilePix = tile.getPix();
                tileOffset++;
                // draw the tile
                tilePixOffset = 0;
                for (int i = 0; i < CHR_PIXELS_HIGH; i++) {
                    for (int q = 0; q < CHR_PIXELS_WIDE; q++) {
                        patternPix[pixOffset + q] = tilePix[tilePixOffset];
                        tilePixOffset++;
                    }
                    pixOffset += pixelsWide;
                }
            }
        }
        updatePatternTableSelection();
    }

    void updatePatternTableSelection() {
        System.arraycopy(patternPix, 0, pix, 0, pix.length);
        if (controls.getShowTileGrid()) {
            int gridColor = Color.black.getRGB();
            for (int y = 0; y < maxHeight; y++) {
                if ((y % CHR_PIXELS_HIGH) == 0) {
                    for (int x = 0; x < maxWidth; x++) {
                        pix[y * maxWidth + x] = gridColor;
                    }
                } else {
                    for (int x = 0; x < maxWidth; x += CHR_PIXELS_WIDE) {
                        pix[y * maxWidth + x] = gridColor;
                    }
                }
                // draw right line
                pix[y * maxWidth + maxWidth - 1] = gridColor;
            }
            // draw bottom line
            for (int x = 0; x < maxWidth; x++) {
                pix[(maxHeight - 1) * maxWidth + x] = gridColor;
            }
        }
        if (controls.getShowSelection()) {
            int boxX = selectedIndex % PPUConstants.COLUMNS_PER_PATTERN_PAGE;
            int boxY = selectedIndex / PPUConstants.COLUMNS_PER_PATTERN_PAGE; // yes we divide by columns here
            int startX = boxX * CHR_PIXELS_WIDE;
            int startY = boxY * CHR_PIXELS_HIGH;
            int selectionColor = Color.lightGray.getRGB();
            // draw top and left line to make it appear selected
            for (int x = startX; x < startX + CHR_PIXELS_WIDE; x++) {
                pix[startY * maxWidth + x] = selectionColor;
            }
            for (int y = startY; y < startY + CHR_PIXELS_HIGH; y++) {
                pix[y * maxWidth + startX] = selectionColor;
            }
        }
        if (showSelectOutline) {
            updateOutline();
        }
        mis.newPixels();
    }

    /*
    public void updateBox(GridMouseResultsModel results){
    updatePatternTableSelection();
    }
     */
    // TO DO:  Eliminate the WRAP around.
    // TO DO: Eliminate the arrayOutofBounds error
    private void updateOutline() {
        if (resultsModel == null) {
            return;
        }
        if (!resultsModel.isBoxValid()) {
            return;
        }

        int minX = resultsModel.startX;
        int minY = resultsModel.startY;
        int maxX = resultsModel.endX;
        int maxY = resultsModel.endY;

        if (resultsModel.startX > resultsModel.endX) {
            minX = resultsModel.endX;
            maxX = resultsModel.startX;
        }
        if (resultsModel.startY > resultsModel.endY) {
            minY = resultsModel.endY;
            maxY = resultsModel.startY;
        }

        int sx = minX * CHR_PIXELS_WIDE;
        if (sx > maxWidth) {
            return;
        }
        int sy = minY * CHR_PIXELS_HIGH;
        if (sy > maxHeight) {
            return;
        }
        int ex = (maxX + 1) * CHR_PIXELS_WIDE - 1;
        if (ex > maxWidth) {
            ex = maxWidth - 1;
        }
        int ey = (maxY + 1) * CHR_PIXELS_HIGH - 1;
        if (ey > maxHeight) {
            ey = maxHeight - 1;
        }
        int selectionColor = Color.lightGray.getRGB();

        // draw top and left line to make it appear selected
        for (int x = sx; x < ex; x++) {
            pix[sy * maxWidth + x] = selectionColor;
            pix[ey * maxWidth + x] = selectionColor;
        }
        for (int y = sy; y < ey; y++) {
            pix[y * maxWidth + sx] = selectionColor;
            pix[y * maxWidth + ex] = selectionColor;
        }

    }

    public void processLeftClick(int index) {
        processLeftClick(index, true);
    }

    public void processLeftClick(int index, boolean tellListener) {
        selectedIndex = index;
        updatePatternTableSelection();
        if (tellListener && listener != null) {
            listener.notifyPatternTableSelected(pageNum, selectedIndex);
        }
        repaint();
    }

    public void processRightClick(int index) {
        selectedIndex = index;
        // replace the pattern table at this index with what is in the editor panel
        if (listener != null) {
            listener.notifyPatternTableToBeModified(pageNum, selectedIndex);
        }
        updatePatternTable();
        if (listener != null) {
            listener.notifyPatternTableSelected(pageNum, selectedIndex);
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
        if (img == null) {
            updatePatternTable();
            img = createImage(mis);
        }
        if (img == null) {
            System.err.println("Still null??");
        }
        super.paintComponent(g);    // paints background
        g.drawImage(img, 0, 0, this);
    }

    private void setupMouseCallbacks() {
        transferHandler = new GridMouseResultsTransferHandler();
        constraintsModel = new GridMouseConstraintsModel(0, 0, maxWidth, maxHeight, CHR_PIXELS_WIDE, CHR_PIXELS_HIGH, PPUConstants.COLUMNS_PER_PATTERN_PAGE, PPUConstants.ROWS_PER_PATTERN_PAGE);

        setTransferHandler(transferHandler);

        if (modelRef == null) {
            // add a generic mouse motion listener to help with tooltips
            addMouseMotionListener(new MouseMotionAdapter() {


                public void mouseMoved(MouseEvent e) {
                    String text = "";
                    int index = constraintsModel.getArrayIndexFromPosition(e.getX(), e.getY());
                    if (index != NES_UI_Constants.INVALID_VALUE) {
                        int xIndex = constraintsModel.getGridXIndexFromPosition(e.getX(), e.getY());
                        int yIndex = constraintsModel.getGridYIndexFromPosition(e.getX(), e.getY());
                        text = "[" + xIndex + "," + yIndex + "] 0x" + ByteFormatter.formatSingleByteInt(index);
                    }
                    // determine position
                    setToolTipText(text);
                }
            });

            return;
        }


        resultsModel = new GridMouseResultsModel(0, 0, PPUConstants.COLUMNS_PER_PATTERN_PAGE, PPUConstants.ROWS_PER_PATTERN_PAGE, modelRef.getCHRModel());
        dragAdapter = new GridMouseInputAndDragAdapter(resultsModel);

        addMouseListener(dragAdapter);
        addMouseMotionListener(dragAdapter);

        dragAdapter.refreshCTD(TransferHandler.NONE, getDropTarget());

        dragAdapter.assignMousePressedCallback(MouseEvent.BUTTON1, new MouseActionCallback() {

            public void doCallback(MouseEvent e, GridMouseResultsModel resultsModel) {
                resultsModel.resetBox();
            }
        });
        dragAdapter.assignMouseClickedCallback(MouseEvent.BUTTON1, new MouseActionCallback() {

            public void doCallback(MouseEvent e, GridMouseResultsModel resultsModel) {
                int index = constraintsModel.getArrayIndexFromPosition(e.getX(), e.getY());
                if (index == NES_UI_Constants.INVALID_VALUE) {
                    return;
                }
                int xIndex = constraintsModel.getGridXIndexFromPosition(e.getX(), e.getY());
                int yIndex = constraintsModel.getGridYIndexFromPosition(e.getX(), e.getY());
                resultsModel.assignIndexX(xIndex);
                resultsModel.assignIndexY(yIndex);
                resultsModel.assignIndex(index);
                processLeftClick(resultsModel.getIndex());
            }
        });
        dragAdapter.assignMouseClickedCallback(MouseEvent.BUTTON3, new MouseActionCallback() {

            public void doCallback(MouseEvent e, GridMouseResultsModel resultsModel) {
                int index = constraintsModel.getArrayIndexFromPosition(e.getX(), e.getY());
                if (index == NES_UI_Constants.INVALID_VALUE) {
                    return;
                }
                int xIndex = constraintsModel.getGridXIndexFromPosition(e.getX(), e.getY());
                int yIndex = constraintsModel.getGridYIndexFromPosition(e.getX(), e.getY());
                resultsModel.assignIndexX(xIndex);
                resultsModel.assignIndexY(yIndex);
                resultsModel.assignIndex(index);
                processRightClick(resultsModel.getIndex());
            }
        });

        dragAdapter.assignMouseMovedCallback(new MouseMoveCallback() {

            public void doMoveCallback(MouseEvent e) {
                String text = "";
                int index = constraintsModel.getArrayIndexFromPosition(e.getX(), e.getY());
                if (index != NES_UI_Constants.INVALID_VALUE) {
                    int xIndex = constraintsModel.getGridXIndexFromPosition(e.getX(), e.getY());
                    int yIndex = constraintsModel.getGridYIndexFromPosition(e.getX(), e.getY());
                    text = "[" + xIndex + "," + yIndex + "] 0x" + ByteFormatter.formatSingleByteInt(index);
                }
                // determine position
                setToolTipText(text);
            }
        });
        dragAdapter.assignMouseDraggedCallback(new MouseDragCallback() {

            private void leftDrag(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel results) {
                results.assignPatternPage(pageNum);

                int relX = constraintsModel.getGridXIndexFromPosition(e.getX(), e.getY());
                results.assignBoxX(relX);

                int relY = constraintsModel.getGridYIndexFromPosition(e.getX(), e.getY());
                results.assignBoxY(relY);

                System.out.println(e.getX() + "," + e.getY() + " <> " + relX + "," + relY);
                updatePatternTableSelection();

            }

            private void rightDrag(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel resultsModel) {
                // you can ONLY drag a results box
                if (!resultsModel.isBoxValid()) {
                    return;
                }
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                //Tell the transfer handler to initiate the drag.
                int action = ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) ? TransferHandler.COPY : TransferHandler.MOVE;
                handler.exportAsDrag(c, firstEvent, action);
                dragAdapter.refreshCTD(action, c.getDropTarget());
            }

            public void doDragCallback(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel resultsModel) {
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    leftDrag(firstEvent, e, resultsModel);
                }
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    rightDrag(firstEvent, e, resultsModel);
                }
            }
        });

    }

    public GridMouseResultsModel getGridMouseResults() {
        return resultsModel;
    }

    public void moveGridData(GridMouseResultsModel results) {
        if (results.modelRef == resultsModel.modelRef) {
            // the import already handled this
        } else {
            //         System.out.println("Move grid Data not yet implemented" + results);
            //results.modelRef.nukePatternContents(results);
        }
    }

    public boolean importGridData(GridMouseResultsModel results) {
        if (modelRef == null) {
            return false;
        }
        Point pos = dragAdapter.getDropPoint();
        int mode = dragAdapter.dragMode;
        if (pos != null) {
            int index = constraintsModel.getArrayIndexFromPosition(pos.x, pos.y);
            int szX = (results.endX - results.startX) + 1;
            int szY = (results.endY - results.startY) + 1;

            byte emptyMask[][][] = new byte[szY][szX][16];
            // for a single square the start and end values would be equal which is why I use <= for the loops
            int yIndex = 0;
            for (int y = results.startY; y <= results.endY; y++, yIndex++) {
                int xIndex = 0;

                for (int x = results.startX; x <= results.endX; x++, xIndex++) {
                    int tileOffset = y * PPUConstants.COLUMNS_PER_PATTERN_PAGE + x;
                    int offPixIndex = tileOffset * 16;
                    System.arraycopy(results.modelRef.patternTable[results.pageNum], offPixIndex, emptyMask[yIndex][xIndex], 0, 16);
                    // only support MOVE within a single component
                    if (mode == TransferHandler.MOVE && results.modelRef == modelRef.getCHRModel()) {
                        System.arraycopy(emptyMask[yIndex][xIndex], 0, results.modelRef.patternTable[results.pageNum], offPixIndex, 16);
                    }
                }
            }
            int counter = 0;
            for (int y = 0; y < szY; y++) {
                for (int x = 0; x < szX; x++) {
                    int tileOffset = index + y * PPUConstants.COLUMNS_PER_PATTERN_PAGE + x;
                    int offPixIndex = tileOffset * 16;
                    if (tileOffset < PPUConstants.ENTRIES_PER_PATTERN_TABLE_PAGE) {

                        System.arraycopy(emptyMask[y][x], 0, modelRef.getCHRModel().patternTable[pageNum], offPixIndex, 16);
                    }
                    counter++;
                }
            }


            selectedIndex = index;
            resultsModel.resetBox();
            updatePatternTable();
            if (listener != null) {
                listener.notifyPatternTableSelected(pageNum, index);
            }
            return true;
        } else {
            System.out.println("Position is null");
            return false;
        }
    }

    public byte calculatePixel(int tile, int x, int y, PPU ppu) {
        return ppu.getCHRTilePixelDirect(tile, x, y, pageNum * 0x1000);
    }

    public void refreshFromPPU(PPU ppu) {

        int col = Color.GREEN.getRGB();
        if (ppu == null) {
            col = Color.PINK.getRGB();
        }
        for (int i = 0; i < pix.length; i++) {
            pix[i] = col;
        }

        int minX = -1;
        int minY = -1;
        int maxX = -1;
        int maxY = -1;

        if (ppu != null) {
            int prev = 0;
            int pos = 0;
            int tile = 0;
            for (int y = 0; y < maxHeight; y++) {
                pos = y * maxWidth;
                for (int x = 0; x < maxWidth; x++) {
                    tile = ((y / 8) * 16) + (x / 8);
                    prev = PPUConstants.NES_PALETTE[calculatePixel(tile, x, y, ppu) & 0xFF].getRGB();
                    if (pix[pos + x] != prev) {
                        pix[pos + x] = prev;
                        if (minX == -1) {
                            minX = x;
                            maxX = x;
                            minY = y;
                            maxY = y;
                        }
                        if (x < minX) {
                            minX = x;
                        }
                        if (x > maxX) {
                            maxX = x;
                        }
                        if (y < minY) {
                            minY = y;
                        }
                        if (y > maxY) {
                            maxY = y;
                        }
                    }
                }
            }
        }
        if (minX == -1) {
            mis.newPixels();
        } else {
            mis.newPixels(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }


    }
}
