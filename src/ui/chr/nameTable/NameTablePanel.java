/*
 * NameTablePanel.java
 *
 * Created on October 3, 2006, 1:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ui.chr.nameTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputAdapter;

import model.NESModelListener;
import ui.chr.CHRDisplayControls;
import ui.chr.CHRDisplayInterface;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.tileEditor.CHRTile;
import ui.input.GridMouseConstraintsModel;
import ui.input.GridMouseResultsModel;
import utilities.ByteFormatter;
import utilities.EnvironmentUtilities;
import utilities.FileUtilities;

/**
 * To Do:
 *
 * Support Drag from CHR to NT
 * Support Drag from IMG to CHR
 * Support Drag from IMG to NT
 * Add OAM enhancements to IMG
 * Font Helper for CHR
 * Text Helper for NT
 * Add Help files
 *
 * @author abailey
 */
public class NameTablePanel extends JPanel implements CHRDisplayInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6684357905047184977L;
	public static final int NOTHING_MODE = 0;
    public static final int SELECT_MODE = 1;
    public static final int REPLACE_TILE_MODE = 2;
    public static final int CYCLE_OAM_MODE = 3;
    public static final int INSERT_STAMP_MODE = 4;
    public int leftMode = SELECT_MODE;
    public int leftControlMode = NOTHING_MODE;
    public int rightMode = REPLACE_TILE_MODE;
    public int rightControlMode = CYCLE_OAM_MODE;
    // Settings properties that are persisted
    public static final String NAME_TABLE_PROPERTY_GROUP = "NameTable";
    public static final String NAME_TABLE_SCALE_PROPERTY = "NameTableScale";
    public static final int DEFAULT_NAME_TABLE_SCALE_PROPERTY = 1;
    public final static int BG_IMG_LEVEL = 0;
    public final static int SPRITE_IMG_LEVEL = 1;
    public final static int GRID_IMG_LEVEL = 2;
    public final static int MAX_IMG_LEVELS = GRID_IMG_LEVEL + 1;
    private TransparentImageLevel imgLevels[] = new TransparentImageLevel[MAX_IMG_LEVELS];
    private int numImgLevels = 0;
    public final static int MIN_SCALE = 1;
    public final static int MAX_SCALE = 5; // I dont support scaling yet
    private int scale = DEFAULT_NAME_TABLE_SCALE_PROPERTY;
    protected CHREditorModel modelRef;
    private CHRDisplayControls controls = null;
    private int selectedIndex = 0;
    private int page = 0;
    private int transparentPix[] = null;
    private int bgPix[] = null;
    private int gridPix[] = null;
    private int currentPixelArrayWidth = 0;
    private int currentPixelArrayHeight = 0;
    private int currentScaledWidth = 0;
    private int currentScaledHeight = 0;
    
    public GridMouseResultsModel resultsModel = null;
    private boolean showSelectOutline = true;
    
    // for drag actions
    private GridMouseConstraintsModel constraintsModel = null;
    private MouseEvent firstMouseDragEvent = null;
    
    /** Creates a new instance of NameTablePanel */
    public NameTablePanel(NESModelListener callback, CHREditorModel model, int pg) {
        this(callback, model, pg, true);
    }

    public NameTablePanel(NESModelListener callback, CHREditorModel model, int pg, boolean doInitSetup) {
        page = pg;
        modelRef = model;
        controls = new CHRDisplayControls(NAME_TABLE_PROPERTY_GROUP);
        scale = EnvironmentUtilities.getIntegerEnvSetting(NAME_TABLE_SCALE_PROPERTY, DEFAULT_NAME_TABLE_SCALE_PROPERTY);
   	    
        if (doInitSetup) {
            initSetup();
        }
    }

    public void initSetup() {
        resetNameTable();
        setupUI();
    }

    public void notifyDisplayInterfaceUpdated() {
        int minPage = determineStartingPage();
        int maxPage = determineEndingPage();
        for (int pg = minPage; pg <= maxPage; pg++) {
            updateNameTable(pg);
        }
        repaint();
    }

    /* Override these 5 methods for panels that consist of multiple nametables */
    public int getTilesWide() {
        return PPUConstants.NAME_TABLE_WIDTH;
    }

    public int getTilesHigh() {
        return PPUConstants.NAME_TABLE_HEIGHT;
    }

    public int determineIndex(int tileX, int tileY) {
        return tileY * getTilesWide() + tileX;
    }

    public int determinePage(int tileX, int tileY) {
        return page;
    }

    public int determineStartingPage() {
        return page;
    }

    public int determineEndingPage() {
        return page;
    }

    public int getScale() {
        return scale;
    }

    public boolean getShowOAMGrid() {
        return controls.getShowOAMGrid();
    }

    public void setShowOAMGrid(boolean flag) {
        controls.setShowOAMGrid(flag);
    }

    public boolean getShowTileGrid() {
        return controls.getShowTileGrid();
    }

    public void setShowTileGrid(boolean flag) {
        controls.setShowTileGrid(flag);
    }

    public boolean getShowPageGrid() {
        return controls.getShowPageGrid();
    }

    public void setShowPageGrid(boolean flag) {
        controls.setShowPageGrid(flag);
    }

    public boolean getShowOAMSelection() {
        return controls.getShowOAMSelection();
    }

    public void setShowOAMSelection(boolean flag) {
        controls.setShowOAMSelection(flag);
    }

    public void setScale(int sc) {
        if (sc >= MIN_SCALE && sc <= MAX_SCALE && scale != sc) {
            scale = sc;
            EnvironmentUtilities.updateIntegerEnvSetting(NAME_TABLE_SCALE_PROPERTY, scale);
            updateScale();
        }
    }

    public void exportAsImage() {
        final JDialog dlog = new JDialog();
        dlog.setTitle("Select Output Image Format");
        dlog.getContentPane().setLayout(new BorderLayout());
        dlog.setModal(true);

        JPanel ctrlPanel = new JPanel();
        ctrlPanel.add(new JLabel("Format:"));
        String writerNames[] = ImageIO.getWriterFormatNames();
        final JList writeList = new JList(writerNames);
        writeList.setSelectedIndex(0);
        writeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scr = new JScrollPane(writeList);
        ctrlPanel.add(scr);
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
                dlog.dispose();
            }
        });

        JPanel butPanel = new JPanel();
        butPanel.add(closeMe);
        butPanel.add(okBut);
        dlog.getContentPane().add(butPanel, BorderLayout.SOUTH);
        dlog.pack();
        dlog.setVisible(true);

        String extensionToUse = (String) writeList.getSelectedValue();

        if (extensionToUse == null) {
            return;
        }

        File selectedFile = FileUtilities.selectFileForSave(this);
        if (selectedFile == null) {
            return;
        }


        int w = currentScaledWidth;
        int h = currentScaledHeight;
        int type = BufferedImage.TYPE_INT_RGB;  // other options
        BufferedImage dest = new BufferedImage(w, h, type);
        Graphics2D g2 = dest.createGraphics();

        // store level 0 only
        int maxLvls = 1;
        for (int i = 0; i < maxLvls; i++) {
            TransparentImageLevel level = getImageLevel(i);
            if (level != null && level.isVisible()) {
                level.reset();
                g2.drawImage(level.getImg(), 0, 0, w, h, null);
            }
        }
        g2.dispose();

        try {
            ImageIO.write(dest, extensionToUse, selectedFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // since we blew away the grid, etc..
        updateNameTableSelection(page);


    }

    public CHRDisplayControls getControls() {
        return controls;
    }

    public void resetNameTable() {
        selectedIndex = 0;
    }

    private String determineTooltipAtLocation(int xPosScaled, int yPosScaled) {
        String text = "";

        if (xPosScaled < 0 || xPosScaled >= currentScaledWidth) {
            // ignore it
            return text;
        }

        if (yPosScaled < 0 || yPosScaled >= currentScaledHeight) {
            // ignore it
            return text;
        }

        int xPos = xPosScaled / scale;
        int yPos = yPosScaled / scale;
        int xBoxSize = PPUConstants.CHR_WIDTH;
        int yBoxSize = PPUConstants.CHR_HEIGHT;

        int relX = xPos / xBoxSize;
        int relY = yPos / yBoxSize;

        int pg = determinePage(relX, relY);
        if (pg < 0 || pg >= modelRef.getCHRModel().nameTableIndexes.length) {
            return text;
        }
        int index = (relY * PPUConstants.NAME_TABLE_WIDTH) + relX;
        if (index < 0 || index >= modelRef.getCHRModel().nameTableIndexes[pg].length) {
            return text;
        }
        int chrVal = modelRef.getCHRModel().nameTableIndexes[pg][index];

        text = " Pos:[ " + xPos + "," + yPos + "]" +
                " CHR:[" + relX + "," + relY + "]" +
                " Index: [0x" + ByteFormatter.formatInt(index) + "]" +
                " Value: [0x" + ByteFormatter.formatInt(chrVal) + "]";
        if (controls.getShowOAMSelection()) {
            int oamIndexX = relX / 4; // gets us in terms of an 8x8 table
            int oamIndexY = relY / 4;
            int oamTableIndex = oamIndexY * PPUConstants.OAM_INDEX_GRID_SIZE + oamIndexX;
            if (oamTableIndex < 64) {
                int oamGroupX = (relX / 2) % 2; // gets us in terms of a 16 x 16 table
                int oamGroupY = (relY / 2) % 2;
                int subOAM = oamGroupY * 2 + oamGroupX;
                int oamVal = modelRef.getCHRModel().oamValues[pg][oamTableIndex];
                text = text + " OAM:[" + oamTableIndex + "(" + subOAM + ") Value= 0x" + ByteFormatter.formatInt(oamVal) + "]";
            }
        }
        return text;
    }

   
    
    private void processMouseActivity(int button, int xPosScaled, int yPosScaled, int modifiers) {

        if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) {
            if (xPosScaled < 0 || xPosScaled >= currentScaledWidth) {
                // ignore it
                return;
            }

            if (yPosScaled < 0 || yPosScaled >= currentScaledHeight) {
                // ignore it
                return;
            }

            int xPos = xPosScaled / scale;
            int yPos = yPosScaled / scale;
            int xBoxSize = PPUConstants.CHR_WIDTH;
            int yBoxSize = PPUConstants.CHR_HEIGHT;

            int overallX = xPos / xBoxSize;
            int overallY = yPos / yBoxSize;
            int pg = determinePage(overallX, overallY);

            int relX = overallX % PPUConstants.NAME_TABLE_WIDTH;
            int relY = overallY % PPUConstants.NAME_TABLE_HEIGHT;

            int index = (relY * PPUConstants.NAME_TABLE_WIDTH) + relX;

            // OAM (or attribute tables) are weird
            // the name table is 32x30 nameTableTiles
            // these get grouped into 16x15 2x2 nameTableTiles where 2 bits per tile-set specify most signifigant color bits
            // those 2x2 tilesets get grouped again (into a 4x4 tileset) giving a total of 8 bits
            // the bits get packed : bottomRight,BottomLeft,TopRght,TopLeft
            int oamGroupX = (relX / 2) % 2; // gets us in terms of a 16 x 16 table
            int oamGroupY = (relY / 2) % 2;
            int oamIndexX = relX / 4; // gets us in terms of an 8x8 table
            int oamIndexY = relY / 4;

            int oamSubIndex = oamGroupY * 2 + oamGroupX;
            int oamTableIndex = oamIndexY * PPUConstants.OAM_INDEX_GRID_SIZE + oamIndexX;
            if (button == MouseEvent.BUTTON1) {
                processLeftClick(pg, index, oamSubIndex, oamTableIndex, modifiers);
            }
            if (button == MouseEvent.BUTTON3) {
                processRightClick(pg, index, oamSubIndex, oamTableIndex, modifiers);
            }
        }
    }

    public void setImageLevel(TransparentImageLevel lev, int index) {
        if (index < 0 || index > MAX_IMG_LEVELS) {
            throw new IllegalArgumentException("Bad index for setImageLevel:" + index);
        }
        if (imgLevels[index] == null && lev != null) {
            // new one added
            numImgLevels++;
            imgLevels[index] = lev;
        } else if (imgLevels[index] != null && lev == null) {
            // old one replaced with null
            numImgLevels--;
            imgLevels[index] = lev;
        } else {
            // overwrite existing
            imgLevels[index] = lev;
        }
    }

    public void updateScale() {
        int tilesWide = getTilesWide();
        int tilesHigh = getTilesHigh();

        currentPixelArrayWidth = tilesWide * PPUConstants.CHR_WIDTH;
        currentPixelArrayHeight = tilesHigh * PPUConstants.CHR_HEIGHT;
        int overallPixelLength = currentPixelArrayWidth * currentPixelArrayHeight;

        currentScaledWidth = currentPixelArrayWidth * scale;
        currentScaledHeight = currentPixelArrayHeight * scale;
 
        transparentPix = new int[currentPixelArrayWidth];
        bgPix = new int[overallPixelLength];// all values are zero.
        gridPix = new int[overallPixelLength];// all values are zero.

        MemoryImageSource bgMIS = new MemoryImageSource(currentPixelArrayWidth, currentPixelArrayHeight, bgPix, 0, currentPixelArrayWidth);
        setImageLevel(new TransparentImageLevel(bgMIS, "Background", true), BG_IMG_LEVEL);

        MemoryImageSource gridMIS = new MemoryImageSource(currentPixelArrayWidth, currentPixelArrayHeight, gridPix, 0, currentPixelArrayWidth);
        setImageLevel(new TransparentImageLevel(gridMIS, "Grid", true), GRID_IMG_LEVEL);

        setPreferredSize(new Dimension(currentScaledWidth, currentScaledHeight));
   	 	constraintsModel = new GridMouseConstraintsModel(0, 0, currentScaledWidth, currentScaledHeight, 8*scale, 8*scale, PPUConstants.NAME_TABLE_WIDTH, PPUConstants.NAME_TABLE_HEIGHT);
   	 	resultsModel = new GridMouseResultsModel(0,0, PPUConstants.NAME_TABLE_WIDTH, PPUConstants.NAME_TABLE_HEIGHT, modelRef.getCHRModel());

   	 	revalidate();
        notifyDisplayInterfaceUpdated();
    }

    private void setupUI() {

        setBackground(Color.WHITE);

        updateScale();
        updateNameTableTiles(true);


        setMinimumSize(new Dimension(currentPixelArrayWidth * MIN_SCALE, currentPixelArrayHeight * MIN_SCALE));
        //       setPreferredSize(new Dimension(currentWidth, currentHeight));
        setMaximumSize(new Dimension(currentPixelArrayWidth * MAX_SCALE, currentPixelArrayHeight * MAX_SCALE));


        addMouseListener(new MouseInputAdapter() {

        	public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1){
                	firstMouseDragEvent = e;
                	resultsModel.resetBox();
                	resultsModel.assignPatternPage(0);                
                }
            }
        	
            public void mouseClicked(MouseEvent e) {
                processMouseActivity(e.getButton(), e.getX(), e.getY(), e.getModifiersEx());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {

        	private void updateDragDisplay() {
        		
        	        if (resultsModel == null) {
        	            return;
        	        }
        	    
        	        if (!resultsModel.isBoxValid()) {
        	            return;
        	        }

        	        int minX = constraintsModel.getPositionFromGridXIndex(resultsModel.startX);
        	        int minY = constraintsModel.getPositionFromGridYIndex(resultsModel.startY);
        	        int maxX = constraintsModel.getPositionFromGridXIndex(resultsModel.endX);
        	        int maxY = constraintsModel.getPositionFromGridYIndex(resultsModel.endY);

        	        if (resultsModel.startX > resultsModel.endX) {
        	            int tmp = minX;
        	            minX = maxX;
        	            maxX = tmp;
        	        }
        	        if (resultsModel.startY > resultsModel.endY) {
        	            int tmp = minY;
        	            minY = maxY;
        	            maxY = tmp;
        	        }

        	        // This is UGLY but it works
            		Graphics g = getGraphics();
            		g.setColor(Color.lightGray);
            		g.drawRect(minX, minY, maxX-minX, maxY-minY);
        	}

        	 private void leftDrag(MouseEvent firstEvent, MouseEvent e, GridMouseResultsModel results) {
                 int relX = constraintsModel.getGridXIndexFromPosition(e.getX(), e.getY());
                 boolean changed = results.assignBoxX(relX);

                 int relY = constraintsModel.getGridYIndexFromPosition(e.getX(), e.getY());
                 changed = (changed || results.assignBoxY(relY));

                 if(changed) {
                	 updateDragDisplay();
                 }
                 
             }
        	 
            public void mouseDragged(MouseEvent e) {
                if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
               		leftDrag(firstMouseDragEvent, e, resultsModel);
                } else if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
                    processMouseActivity(MouseEvent.BUTTON3, e.getX(), e.getY(), e.getModifiersEx());
                }
            }

            public void mouseMoved(MouseEvent e) {
                setToolTipText(determineTooltipAtLocation(e.getX(), e.getY()));
            }
        });

     }

    private void updateNameTableSelection(int pg) {
        int cnt = 0;
        for (int i = 0; i < currentPixelArrayHeight; i++) {
            System.arraycopy(transparentPix, 0, gridPix, cnt, transparentPix.length);
            cnt += transparentPix.length;
        }
        if (controls.getShowTileGrid()) {
            int gridSzY = PPUConstants.CHR_HEIGHT;
            int gridSzX = PPUConstants.CHR_WIDTH;
            int gridColor = controls.getGridColor().getRGB();
            for (int y = 0; y < currentPixelArrayHeight; y++) {
                if ((y % gridSzY) == 0) {
                    for (int x = 0; x < currentPixelArrayWidth; x++) {
                        gridPix[y * currentPixelArrayWidth + x] = gridColor;
                    }
                } else {
                    for (int x = 0; x < currentPixelArrayWidth; x += gridSzX) {
                        gridPix[y * currentPixelArrayWidth + x] = gridColor;
                    }
                }
                // draw right line
                gridPix[y * currentPixelArrayWidth + currentPixelArrayWidth - 1] = gridColor;
            }
            // draw bottom line
            for (int x = 0; x < currentPixelArrayWidth; x++) {
                gridPix[(currentPixelArrayHeight - 1) * currentPixelArrayWidth + x] = gridColor;
            }
        }
        if (controls.getShowOAMGrid()) {
            int gridSzY = PPUConstants.CHR_HEIGHT * 2;
            int gridSzX = PPUConstants.CHR_WIDTH * 2;
            int gridColor = controls.getOAMGridColor().getRGB();
            for (int y = 0; y < currentPixelArrayHeight; y++) {
                if ((y % gridSzY) == 0) {
                    for (int x = 0; x < currentPixelArrayWidth; x++) {
                        gridPix[y * currentPixelArrayWidth + x] = gridColor;
                    }
                } else {
                    for (int x = 0; x < currentPixelArrayWidth; x += gridSzX) {
                        gridPix[y * currentPixelArrayWidth + x] = gridColor;
                    }
                }
                // draw right line
                gridPix[y * currentPixelArrayWidth + currentPixelArrayWidth - 1] = gridColor;
            }
        }


        if (controls.getShowPageGrid()) {
            int gridSzY = PPUConstants.CHR_HEIGHT * 30;
            int gridSzX = PPUConstants.CHR_WIDTH * 32;
            int gridColor = controls.getPageGridColor().getRGB();
            for (int y = 0; y < currentPixelArrayHeight; y++) {
                if ((y % gridSzY) == 0) {
                    for (int x = 0; x < currentPixelArrayWidth; x++) {
                        gridPix[y * currentPixelArrayWidth + x] = gridColor;
                    }
                } else {
                    for (int x = 0; x < currentPixelArrayWidth; x += gridSzX) {
                        gridPix[y * currentPixelArrayWidth + x] = gridColor;
                    }
                }
                // draw right line
                gridPix[y * currentPixelArrayWidth + currentPixelArrayWidth - 1] = gridColor;
            }
        }
        if (controls.getShowSelection()) {
            int boxX = selectedIndex % PPUConstants.NAME_TABLE_WIDTH;
            int boxY = selectedIndex / PPUConstants.NAME_TABLE_WIDTH; // yes, we divide by width here.
            int startX = boxX * PPUConstants.CHR_WIDTH;
            int startY = boxY * PPUConstants.CHR_HEIGHT;
            int selectionColor = Color.lightGray.getRGB();
            // draw top and left line to make it appear selected
            for (int x = startX; x < startX + PPUConstants.CHR_WIDTH; x++) {
                gridPix[startY * currentPixelArrayWidth + x] = selectionColor;
            }
            for (int y = startY; y < startY + PPUConstants.CHR_HEIGHT; y++) {
                gridPix[y * currentPixelArrayWidth + startX] = selectionColor;
            }
        }
        // only show oam for selected area
        if (controls.getShowOAMSelection()) {
            int oamColor = controls.getOAMSelectionColor().getRGB();

            int boxX = selectedIndex % PPUConstants.NAME_TABLE_WIDTH;
            int boxY = selectedIndex / PPUConstants.NAME_TABLE_WIDTH; // yes, we divide by width here.
            int oamIndexX = boxX / 4; // gets us in terms of an 8x8 table
            int oamIndexY = boxY / 4;

            int oamGroupX = (boxX / 2) % 2; // gets us in terms of a 16 x 16 table
            int oamGroupY = (boxY / 2) % 2;

            int pageOffset = 0;
            if (pg > 0) {
                pageOffset = pg * PPUConstants.NAME_TABLE_WIDTH * PPUConstants.CHR_WIDTH;
            }
            int startX = pageOffset + oamIndexX * PPUConstants.CHR_WIDTH * 4;
            int startY = oamIndexY * PPUConstants.CHR_HEIGHT * 4;
            int midX = startX + PPUConstants.CHR_WIDTH * 2 - 1;
            int midY = startY + PPUConstants.CHR_HEIGHT * 2 - 1;
            int endX = startX + PPUConstants.CHR_WIDTH * 4 - 1;
            int endY = startY + PPUConstants.CHR_HEIGHT * 4 - 1;
            if (endX >= currentPixelArrayWidth - 1) {
                endX = currentPixelArrayWidth - 1;
            }
            if (endY >= currentPixelArrayHeight - 1) {
                endY = currentPixelArrayHeight - 1;
            }
            if (midX >= currentPixelArrayWidth - 1) {
                midX = currentPixelArrayWidth - 1;
            }
            if (midY >= currentPixelArrayHeight - 1) {
                midY = currentPixelArrayHeight - 1;
            }

            int sX = startX;
            int eX = midX;
            if (oamGroupX == 1) {
                sX = midX;
                eX = endX;
            }
            int sY = startY;
            int eY = midY;
            if (oamGroupY == 1) {
                sY = midY;
                eY = endY;
            }

            for (int x = sX; x < eX; x += 3) {
                //pix[startY*currentPixelArrayWidth+x] = oamColor;
                //pix[midY*currentPixelArrayWidth+x] = oamColor;
                //pix[endY*currentPixelArrayWidth+x] = oamColor;
                gridPix[sY * currentPixelArrayWidth + x] = oamColor;
                gridPix[eY * currentPixelArrayWidth + x] = oamColor;
            }
            for (int y = sY; y < eY; y += 3) {
                //pix[y*currentPixelArrayWidth+startX] = oamColor;
                //pix[y*currentPixelArrayWidth+midX] = oamColor;
                //pix[y*currentPixelArrayWidth+endX] = oamColor;
                gridPix[y * currentPixelArrayWidth + sX] = oamColor;
                gridPix[y * currentPixelArrayWidth + eX] = oamColor;
            }
        }
        // notify model of what has been selected

        if (showSelectOutline) {
            updateOutline();
        }

        refreshImageLevels();

    }

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

        int sx = minX;
        if (sx > currentPixelArrayWidth) {
            return;
        }
        int sy = minY;
        if (sy > currentPixelArrayHeight) {
            return;
        }
        int ex = maxX;
        if (ex > currentPixelArrayWidth) {
            ex = currentPixelArrayWidth - 1;
        }
        int ey = maxY;
        if (ey > currentPixelArrayHeight) {
            ey = currentPixelArrayHeight - 1;
        }
        int selectionColor = Color.white.getRGB();

        // draw top and left line to make it appear selected
        for (int x = sx; x < ex; x++) {
            gridPix[sy * currentPixelArrayWidth + x] = selectionColor;
            gridPix[ey * currentPixelArrayWidth + x] = selectionColor;
        }
        for (int y = sy; y < ey; y++) {
            gridPix[y * currentPixelArrayWidth + sx] = selectionColor;
            gridPix[y * currentPixelArrayWidth + ex] = selectionColor;
        }

    }

    private void processMode(int mode, int pg, int index, int oamSubIndex, int oamTableIndex, int modifiers) {
        switch (mode) {
            case NOTHING_MODE:
                break;
            case SELECT_MODE:
                selectedIndex = index;
                updateNameTableSelection(pg);
                repaint();
                break;
            case CYCLE_OAM_MODE:
                selectedIndex = index;
                processOAMChange(pg, index, oamSubIndex, oamTableIndex, modifiers);
                updateNameTable(pg);        // add to the name table
                repaint();
                break;
            case REPLACE_TILE_MODE:
                selectedIndex = index;
                modelRef.getCHRModel().nameTableIndexes[pg][selectedIndex] = (byte) modelRef.lastPatternIndex;
                modelRef.updateNameTableIndex(pg, selectedIndex, modelRef.getCHRModel().nameTableIndexes[pg][selectedIndex]);
                updateNameTable(pg);        // add to the name table
                repaint();
                break;
            case INSERT_STAMP_MODE:
                insertStamp(pg, index, oamSubIndex, oamTableIndex, modifiers);
                break;
            default:
                System.err.println("Unhandled left mode:" + leftMode);
        }
    }

    public void insertStamp(int pg, int index, int oamSubIndex, int oamTableIndex, int modifiers) {
        // do nothing
    }

    // set the name table to the active CHR
    private void processLeftClick(int pg, int index, int oamSubIndex, int oamTableIndex, int modifiers) {
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
            processMode(leftControlMode, pg, index, oamSubIndex, oamTableIndex, modifiers);
        } else {
            processMode(leftMode, pg, index, oamSubIndex, oamTableIndex, modifiers);
        }
    }

    // adjust the contents or the OAM value
    private void processRightClick(int pg, int index, int oamSubIndex, int oamTableIndex, int modifiers) {
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
            processMode(rightControlMode, pg, index, oamSubIndex, oamTableIndex, modifiers);
        } else {
            processMode(rightMode, pg, index, oamSubIndex, oamTableIndex, modifiers);
        }
    }

    private void processOAMChange(int pg, int index, int oamSubIndex, int oamTableIndex, int modifiers) {
        // its an oam right click
        byte oamVal = modelRef.getCHRModel().oamValues[pg][oamTableIndex];
        byte b[] = new byte[4];
        int oamPortion = 0;
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) ((oamVal >> (i * 2)) & 0x03);
            if (i == oamSubIndex) {
                b[i] = (byte) ((b[i] + 1) & 0x03);
                oamPortion = b[i];
            }
        }
        byte newOamVal = (byte) (((b[3] << 6) + (b[2] << 4) + (b[1] << 2) + b[0]) & 0xFF);
        //     System.out.println(oamTableIndex + " " + oamSubIndex + " Was: " + ByteFormatter.formatByte(oamVal) + " Now:" + ByteFormatter.formatByte(newOamVal));

        modelRef.getCHRModel().oamValues[pg][oamTableIndex] = newOamVal;

        // we need to update 4 tiles.
        int boxX = index % PPUConstants.NAME_TABLE_WIDTH;
        int boxY = index / PPUConstants.NAME_TABLE_WIDTH; // yes, we divide by width here.
        //     System.out.println("Assuming" + boxX + " " + boxY + " " + ByteFormatter.formatInt(index));
        if (boxX % 2 == 1) {
            boxX--;
        }
        if (boxY % 2 == 1) {
            boxY--;
        }
        //     System.out.println("Correcting" + boxX + " " + boxY + " " + ByteFormatter.formatInt(index));
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                int tempIndex = (boxY + i) * PPUConstants.NAME_TABLE_WIDTH + (boxX + j);
                if (tempIndex < modelRef.getCHRModel().nameTableIndexes[pg].length) {
                    //    System.out.println("Updating NT tile for index:" + ByteFormatter.formatInt(tempIndex));
                    modelRef.nameTableTiles[pg][tempIndex] = new CHRTile(modelRef.nameTableTiles[pg][tempIndex], oamPortion);
                }
            }
        }
    }

    public void refreshImageLevels() {
        int maxLvls = getNumImageLevels();
        for (int i = 0; i < maxLvls; i++) {
            TransparentImageLevel level = getImageLevel(i);
            if (level != null && level.isVisible()) {
                level.refresh();
            }
        }
    }

    public int getNumImageLevels() {
        return MAX_IMG_LEVELS;
    }

    public TransparentImageLevel getGridImageLevel() {
        return imgLevels[GRID_IMG_LEVEL];
    }

    public TransparentImageLevel getImageLevel(int index) {
        return imgLevels[index];
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);    // paints background
        // draw each level
        int maxLvls = getNumImageLevels();
        for (int i = 0; i < maxLvls; i++) {
            TransparentImageLevel level = getImageLevel(i);
            if (level != null && level.isVisible()) {
                g.drawImage(level.getImg(), 0, 0, currentScaledWidth, currentScaledHeight, this);
            }
        }
    }

    public void updateNameTableTiles(boolean updateTableMode) {
        int minPage = determineStartingPage();
        int maxPage = determineEndingPage();
        for (int pg = minPage; pg <= maxPage; pg++) {
            for (int i = 0; i < modelRef.getCHRModel().nameTableIndexes[pg].length; i++) {
                // since the name table index is a byte, we need to binary AND it to make it a positive integer
                int index = (modelRef.getCHRModel().nameTableIndexes[pg][i] & 0xFF);
                CHRTile orig = modelRef.getPatternTableTile(index);
                if (orig != null) {
                    int oamVal = modelRef.getCHRModel().getOAMFromNTIndex(pg, i);
                    modelRef.nameTableTiles[pg][i] = new CHRTile(orig, oamVal);
                }
            }
            if (updateTableMode) {
                updateNameTable(pg);
            }
        }
    }

    protected void updateNameTable(int suggestedPage) {
        int tilePix[] = null;
        int ySteps = currentPixelArrayWidth * PPUConstants.CHR_HEIGHT; // number of pixels to skip an entire level of horizontal tiles
        int tileOffset = 0;
        int pixOffset = 0;
        int tilePixOffset = 0;
        int ty = 0;
        int tx = 0;
        boolean megaTable = false;
        if (getTilesHigh() > PPUConstants.NAME_TABLE_HEIGHT || getTilesWide() > PPUConstants.NAME_TABLE_WIDTH) {
            megaTable = true;
        }
        int pg = suggestedPage;
        
        int tilesHigh = getTilesHigh();
        int tilesWide = getTilesWide();
        
        for (ty = 0; ty < tilesHigh; ty++) {
            for (tx = 0; tx < tilesWide; tx++) {
                pixOffset = (ty * ySteps) + (tx * PPUConstants.CHR_WIDTH);
                if (megaTable) {
                    pg = determinePage(tx, ty);
                    tileOffset = determineIndex(tx, ty);
                }

                CHRTile tile = modelRef.nameTableTiles[pg][tileOffset]; // broken
                if (tile == null) {
                    tilePix = new int[PPUConstants.CHR_HEIGHT * PPUConstants.CHR_WIDTH];
                } else {
                    tilePix = tile.getPix();
                }
                // draw the tile
                if (!megaTable) {
                    tileOffset++;
                }
                tilePixOffset = 0;
                for (int i = 0; i < PPUConstants.CHR_HEIGHT; i++) {
                    for (int q = 0; q < PPUConstants.CHR_WIDTH; q++) {
                        bgPix[pixOffset + q] = tilePix[tilePixOffset];
                        tilePixOffset++;
                    }
                    pixOffset += currentPixelArrayWidth;
                }
            }
        }
        updateNameTableSelection(pg);
    }
}
