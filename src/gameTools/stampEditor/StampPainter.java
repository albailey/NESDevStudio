/*
 * StampPainter.java
 *
 * Created on August 17, 2008, 10:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampEditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import model.NESModelListener;
import ui.chr.CHRDisplayControls;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.palette.PaletteClickListener;
import ui.chr.palette.PalettePanel;
import ui.chr.tileEditor.CHRMultiTilePanel;
import utilities.EnvironmentUtilities;
import utilities.GUIUtilities;

/**
 * Display Left controls/color
 * Display Right controls/color
 * Left click an action or a color
 * Right click an action or a color
 * Impose increments of 2 for stamps
 * Allow multi-oam
 * Allow palette loading, etc..
 * Allow CHR bank loading/selecting (advanced)
 * @author abailey
 */
public class StampPainter  extends JInternalFrame implements NESModelListener, StampSettings {
    
   
	/**
	 * 
	 */
	private static final long serialVersionUID = -8127199468485060569L;
	/**
	 * 
	 */

	public final static String FRAME_TITLE = "Stamp Painter";
    private final static String STAMP_PAINTER_LAST_FILE_PROPERTY = "STAMP_PAINTER_LAST_FILE_PROPERTY";
    
    public static final String SHOW_TILE_GRID_TITLE = "Show Tile Grid";
    public static final String SHOW_TILE_GRID_TOOLTIP = "Show grid for every tile";
    public static final String STAMP_PAINTER_SHOW_TILE_GRID = "STAMP_PAINTER_SHOW_TILE_GRID";
    public static final boolean DEFAULT_STAMP_PAINTER_SHOW_TILE_GRID = false;
    
    public static final String SHOW_OAM_GRID_TITLE = "Show OAM Grid";
    public static final String SHOW_OAM_GRID_TOOLTIP = "Show grid to indicate OAM region";
    public static final String STAMP_PAINTER_SHOW_OAM_GRID = "STAMP_PAINTER_SHOW_OAM_GRID";
    public static final boolean DEFAULT_STAMP_PAINTER_SHOW_OAM_GRID = true;
    
    public static final String STAMP_TILES_NUM_WIDE_TITLE = "Tiles Wide";
    public static final String STAMP_TILES_NUM_WIDE_TOOLTIP = "Width (in tiles) of the stamp.";
    public static final String STAMP_PAINTER_STAMP_TILES_NUM_WIDE = "STAMP_PAINTER_STAMP_TILES_NUM_WIDE";
    public static final int DEFAULT_STAMP_PAINTER_STAMP_TILES_NUM_WIDE = 2;
    public static final int MIN_STAMP_PAINTER_STAMP_TILES_NUM_WIDE = 1;
    public static final int MAX_STAMP_PAINTER_STAMP_TILES_NUM_WIDE = 32;
    public static final int STEP_STAMP_PAINTER_STAMP_TILES_NUM_WIDE = 1;
    
    public static final String STAMP_TILES_NUM_HIGH_TITLE = "Tiles High";
    public static final String STAMP_TILES_NUM_HIGH_TOOLTIP = "Height (in tiles) of the stamp.";
    public static final String STAMP_PAINTER_STAMP_TILES_NUM_HIGH = "STAMP_PAINTER_STAMP_TILES_NUM_HIGH";
    public static final int DEFAULT_STAMP_PAINTER_STAMP_TILES_NUM_HIGH = 2;
    public static final int MIN_STAMP_PAINTER_STAMP_TILES_NUM_HIGH = 1;
    public static final int MAX_STAMP_PAINTER_STAMP_TILES_NUM_HIGH = 30;
    public static final int STEP_STAMP_PAINTER_STAMP_TILES_NUM_HIGH = 1;
    
    public static final String STAMP_TILES_SCALE_TITLE = "Scale";
    public static final String STAMP_TILES_SCALE_TOOLTIP = "Amount to scale each tile to make editing easier. This is viewer only and does not affect the data.";
    public static final String STAMP_PAINTER_STAMP_TILES_SCALE = "STAMP_PAINTER_STAMP_TILES_SCALE";
    public static final int DEFAULT_STAMP_PAINTER_STAMP_TILES_SCALE = 1;
    public static final int MIN_STAMP_PAINTER_STAMP_TILES_SCALE = 1;
    public static final int MAX_STAMP_PAINTER_STAMP_TILES_SCALE = 10;
    public static final int STEP_STAMP_PAINTER_STAMP_TILES_SCALE = 1;
    
    private CHREditorModel _modelRef = null;
    private CHRMultiTilePanel _stamp = null;
    private CHRMultiTilePanel _masterStamp = null;
    
    private CHRDisplayControls _stampControls = null;
    private PalettePanel _palettePanel = null;
    private ButtonGroup buttonGroup  = new ButtonGroup();
    
    private final static int DO_NOTHING_MODE = 0;
    private final static int DRAW_PIXEL_MODE = 1;
    private final static int DRAW_LINE_MODE = 2;
    private final static int DRAW_RECT_MODE = 3;
    private final static int FILL_RECT_MODE = 4;
    private final static int DRAW_CIRCLE_MODE = 5;
    private final static int FILL_CIRCLE_MODE = 6;
    private final static int ERASE_MODE = 7;
    
    private final static int NUM_MODES = 8;
    
    private ImageIcon _loadedIcons[] = null;
    
    private int _leftClickAction = DRAW_PIXEL_MODE;
    private int _leftClickColor = 0;
    private int _leftSize = 1;
    private JLabel _leftControlPanel = null;
    private JPanel _leftColorPanel = null;    
    
    private int _rightClickAction = ERASE_MODE;
    private int _rightClickColor = 1;
    private int _rightSize = 1;
    private JLabel _rightControlPanel = null;
    private JPanel _rightColorPanel = null;    
    
    private boolean _guiSetup = false;
    private int _oam = 0;
    
    
    public StampPainter( CHRMultiTilePanel tilePanel) {
        super(FRAME_TITLE, true, true, false, false);
        _modelRef = new CHREditorModel();
        _masterStamp = tilePanel;
        
        if(tilePanel == null) {
            _stampControls = new CHRDisplayControls("STAMP_PAINTER");
            _stamp = new CHRMultiTilePanel(2,2, _modelRef, _stampControls);
            reloadPrefs();
        } else {
        	 _stamp = new CHRMultiTilePanel(tilePanel);
            _stampControls = _stamp.getControls();
        }
        _oam = _stamp.getOAM(0);
        setupMouseControls();
        setupUI();
        _guiSetup = true;
        updateMouseSettingsPanel();
        pack();
        setLocation(0,0);
        _stamp.notifyDisplayInterfaceUpdated();
    }
    
    protected void setupMouseControls(){
        EditorCallbackView leftControls = new EditorCallbackView(){
            public void processClear(){
                _stamp.setOverlayRect(null);
            }
            public void processClick(int tileIndex, int pixelIndex){
                clicked(_leftClickAction, _leftClickColor, _leftSize, tileIndex, pixelIndex);
            }
            public void processDrag(int sx, int sy, int ex, int ey){
                dragged(_leftClickAction, _leftClickColor, _leftSize, sx, sy, ex, ey);
            }
            public void updateVisual(int sx, int sy, int ex, int ey){
                dragging(_leftClickAction, _leftClickColor, _leftSize, sx, sy, ex, ey);
            }
        };
        
        EditorCallbackView rightControls = new EditorCallbackView(){
            public void processClear(){
                _stamp.setOverlayRect(null);
            }
            public void processClick(int tileIndex, int pixelIndex){
                clicked(_rightClickAction, _rightClickColor, _rightSize, tileIndex, pixelIndex);
            }
            public void processDrag(int sx, int sy, int ex, int ey){
                dragged(_rightClickAction, _rightClickColor, _rightSize, sx, sy, ex, ey);
            }
            public void updateVisual(int sx, int sy, int ex, int ey){
                dragging(_rightClickAction, _rightClickColor, _rightSize, sx, sy, ex, ey);
            }
        };
        
        _leftClickColor = 0;
        _rightClickColor = 1;
        MouseInputAdapter leftMia = new MultiTileEditorMouseAdapter(this, leftControls, MouseEvent.BUTTON1);
        _stamp.addMouseListener(leftMia);
        _stamp.addMouseMotionListener(leftMia);
        MouseInputAdapter rightMia = new MultiTileEditorMouseAdapter(this, rightControls, MouseEvent.BUTTON3);
        _stamp.addMouseListener(rightMia);
        _stamp.addMouseMotionListener(rightMia);
    }
    
    private void paintPixelArea(int color, int sz, Point p, boolean doUpdate){
        if(p != null){
            paintPixelArea(color, sz, p.x, p.y, doUpdate);
        }
    }
    
    
    // bresenham
    private void drawBresenhamLine(int color, int sz, int x0, int y0, int x1, int y1) {
        boolean steep = (Math.abs(y1 - y0) > Math.abs(x1 - x0));
        int _x0 = x0;
        int _x1 = x1;
        int _y0 = y0;
        int _y1 = y1;
        if(steep){
//       swap(x0, y0)
            int tmp = _y0;
            _y0 = _x0;
            _x0 = tmp;
//         swap(x1, y1)
            tmp = _y1;
            _y1 = _x1;
            _x1 = tmp;
        }
        if(_x0 > _x1){
//         swap(x0, x1)
            int tmp = _x1;
            _x1 = _x0;
            _x0 = tmp;
//         swap(y0, y1)
            tmp = _y0;
            _y0 = _y1;
            _y1 = tmp;
        }
        
        int deltax = (_x1 - _x0);
        int deltay = Math.abs(_y1 - _y0);
        int error = deltax / 2;
        int ystep;
        int y = _y0;
        if (y0 < y1){
            ystep = 1;
        } else {
            ystep = -1;
        }
        for(int x=_x0;x<=_x1;x++){
            if(steep){
                paintPixelAreaXY(color, sz, y,x);
            } else {
                paintPixelAreaXY(color, sz,x, y);
            }
            error = error - deltay;
            if (error < 0){
                y = y + ystep;
                error = error + deltax;
            }
        }
        
    }
    
    private void drawLine(int color, int sz, Point p0, Point p1){
        //       System.out.println("Draw Line:" + color + "," + sz + "  " + p0 + "::" + p1);
        if(p0 == null || p1 == null){
            return;
        }
        // break it back into pixels, and paint each using paintPixelArea
        
        int tx0 = p0.x % getNumBricksX();
        int ty0 = p0.x / getNumBricksX();
        int x0 = (tx0 * getBrickSizeX()) + (p0.y % getBrickSizeX()) ;
        int y0 = (ty0 * getBrickSizeY()) + (p0.y / getBrickSizeX());
        
        int tx1 = p1.x % getNumBricksX();
        int ty1 = p1.x / getNumBricksX();
        int x1 = (tx1 * getBrickSizeX()) + (p1.y % getBrickSizeX()) ;
        int y1 = (ty1 * getBrickSizeY()) + (p1.y / getBrickSizeX());
        
        
        drawBresenhamLine(color, sz, x0,y0,x1,y1);
        _stamp.updateTiles();
    }
    
    private void drawRect(int color, int sz, Point p0, Point p1){
        if(p0 == null || p1 == null){
            return;
        }
        // break it back into pixels
        int tx0 = p0.x % getNumBricksX();
        int ty0 = p0.x / getNumBricksX();
        int x0 = (tx0 * getBrickSizeX()) + (p0.y % getBrickSizeX()) ;
        int y0 = (ty0 * getBrickSizeY()) + (p0.y / getBrickSizeX());
        
        int tx1 = p1.x % getNumBricksX();
        int ty1 = p1.x / getNumBricksX();
        int x1 = (tx1 * getBrickSizeX()) + (p1.y % getBrickSizeX()) ;
        int y1 = (ty1 * getBrickSizeY()) + (p1.y / getBrickSizeX());
        
        // we already have the rect organized properly
        for(int x=x0;x<=x1;x++){
            paintPixelAreaXY(color, sz, x, y0);
            paintPixelAreaXY(color, sz, x, y1);
        }
        for(int y=y0+1;y<y1;y++){
            paintPixelAreaXY(color, sz, x0, y);
            paintPixelAreaXY(color, sz, x1, y);
        }
        _stamp.updateTiles();
    }
    private void fillRect(int color, int sz, Point p0, Point p1){
        if(p0 == null || p1 == null){
            return;
        }
        
        // break it back into pixels
        int tx0 = p0.x % getNumBricksX();
        int ty0 = p0.x / getNumBricksX();
        int x0 = (tx0 * getBrickSizeX()) + (p0.y % getBrickSizeX()) ;
        int y0 = (ty0 * getBrickSizeY()) + (p0.y / getBrickSizeX());
        
        int tx1 = p1.x % getNumBricksX();
        int ty1 = p1.x / getNumBricksX();
        int x1 = (tx1 * getBrickSizeX()) + (p1.y % getBrickSizeX()) ;
        int y1 = (ty1 * getBrickSizeY()) + (p1.y / getBrickSizeX());
        
        for(int y=y0;y<=y1;y++){
            paintRow(color, x0, y, (x1-x0)+1);
        }
        _stamp.updateTiles();        
    }
    
    private void drawCircle(int color, int sz, Point p0, Point p1){
        // break it back into pixels
        int tx0 = p0.x % getNumBricksX();
        int ty0 = p0.x / getNumBricksX();
        int x0 = (tx0 * getBrickSizeX()) + (p0.y % getBrickSizeX()) ;
        int y0 = (ty0 * getBrickSizeY()) + (p0.y / getBrickSizeX());
        
        int tx1 = p1.x % getNumBricksX();
        int ty1 = p1.x / getNumBricksX();
        int x1 = (tx1 * getBrickSizeX()) + (p1.y % getBrickSizeX()) ;
        int y1 = (ty1 * getBrickSizeY()) + (p1.y / getBrickSizeX());
        
        int cx = (x1 + x0) / 2;
        int cy = (y1 + y0) / 2;
        int rx = cx - x0;
        int ry = cy - y0;
        drawEllipse(color, sz, cx,cy,rx,ry);
        _stamp.updateTiles();
    }
    private void fillCircle(int color, int sz, Point p0, Point p1){
        // break it back into pixels
        int tx0 = p0.x % getNumBricksX();
        int ty0 = p0.x / getNumBricksX();
        int x0 = (tx0 * getBrickSizeX()) + (p0.y % getBrickSizeX()) ;
        int y0 = (ty0 * getBrickSizeY()) + (p0.y / getBrickSizeX());
        
        int tx1 = p1.x % getNumBricksX();
        int ty1 = p1.x / getNumBricksX();
        int x1 = (tx1 * getBrickSizeX()) + (p1.y % getBrickSizeX()) ;
        int y1 = (ty1 * getBrickSizeY()) + (p1.y / getBrickSizeX());
        
        int cx = (x1 + x0) / 2;
        int cy = (y1 + y0) / 2;
        int rx = cx - x0;
        int ry = cy - y0;
        fillEllipse(color, cx,cy,rx,ry);    
        _stamp.updateTiles();          
    }
    
    // McIlroy's Algorithm
    void drawEllipse(int color, int sz, int xc, int yc, int a, int b) {
        
        /* e(x,y) = b^2*x^2 + a^2*y^2 - a^2*b^2 */
        int x = 0, y = b;
        long a2 = (long)a*a;
        long b2 = (long)b*b;
        long crit1 = -(a2/4 + a%2 + b2);
        long crit2 = -(b2/4 + b%2 + a2);
        long crit3 = -(b2/4 + b%2);
        long t = -a2*y; /* e(x+1/2,y-1/2) - (a^2+b^2)/4 */
        long dxt = 2*b2*x;
        long dyt = -2*a2*y;
        long d2xt = 2*b2;
        long d2yt = 2*a2;
        
        while (y>=0 && x<=a) {
            paintPixelAreaXY(color, sz, xc+x, yc+y);
            if (x!=0 || y!=0)
                paintPixelAreaXY(color, sz, xc-x, yc-y);
            if (x!=0 && y!=0) {
                paintPixelAreaXY(color, sz, xc+x, yc-y);
                paintPixelAreaXY(color, sz, xc-x, yc+y);
            }
            if (t + b2*x <= crit1 ||   /* e(x+1,y-1/2) <= 0 */
                    t + a2*y <= crit3) {     /* e(x+1/2,y) <= 0 */
                x++;
                dxt += d2xt;
                t += dxt;
            } else if (t - a2*y > crit2) { /* e(x+1/2,y-1) > 0 */
                y--;
                dyt += d2yt;
                t += dyt;
            } else {
                x++;
                dxt += d2xt;
                t += dxt;
                y--;
                dyt += d2yt;
                t += dyt;
            }
        }
    }
    
    // L. Patrick's Algorithm 
    void fillEllipse(int color, int xc, int yc, int a, int b){
        /* e(x,y) = b^2*x^2 + a^2*y^2 - a^2*b^2 */
        int x = 0;
        int y = b;
        int width = 1;
        long a2 = (long)a*a;
        long b2 = (long)b*b;
        long crit1 = -(a2/4 + a%2 + b2);
        long crit2 = -(b2/4 + b%2 + a2);
        long crit3 = -(b2/4 + b%2);
        long t = -a2*y; /* e(x+1/2,y-1/2) - (a^2+b^2)/4 */
        long dxt = 2*b2*x;
        long dyt = -2*a2*y;
        long d2xt = 2*b2;
        long d2yt = 2*a2;
        
        while (y>=0 && x<=a) {
            if (t + b2*x <= crit1 /* e(x+1,y-1/2) <= 0 */ || t + a2*y <= crit3) /* e(x+1/2,y) <= 0 */  {
                x++;
                dxt += d2xt;
                t += dxt;
                width += 2;
            } else if (t - a2*y > crit2) { /* e(x+1/2,y-1) > 0 */
                paintRow(color, xc-x, yc-y, width);
                if (y!=0) {
                    paintRow(color, xc-x, yc+y, width);
                }
                y--;
                dyt += d2yt;
                t += dyt;
            } else {
                paintRow(color, xc-x, yc-y, width);
                if (y!=0) {
                    paintRow(color, xc-x, yc+y, width);
                }
                x++;
                dxt += d2xt;
                t += dxt;
                y--;
                dyt += d2yt;
                t += dyt;
                width += 2;
            }
        }
        if (b == 0) {
            paintRow(color, xc-a, yc, 2*a+1);
        }
    }


private void paintRow(int color, int startingX, int y, int width) {
    int ty = y / getBrickSizeY();
    int h = y % getBrickSizeY();
    
    for(int x=startingX;x<startingX+width;x++){
        int tx = x / getBrickSizeX();
        int w = x % getBrickSizeX();
        _stamp.setPixel(ty*getNumBricksX()+tx, (h*getBrickSizeX()+w), color, false);
    }
}

/*
private void paintPixelXY(int color, int x, int y) {
    int tx = x / getBrickSizeX();
    int ty = y / getBrickSizeY();
    int w = x % getBrickSizeX();
    int h = y % getBrickSizeY();
    _stamp.setPixel(ty*getNumBricksX()+tx, (h*getBrickSizeX()+w), color, false);
}
*/

private void paintPixelAreaXY(int color, int sz, int x, int y) {
    int tx = x / getBrickSizeX();
    int ty = y / getBrickSizeY();
    int w = x % getBrickSizeX();
    int h = y % getBrickSizeY();
    paintPixelArea(color, sz, tx,ty,w,h, false);
}


private void paintPixelArea(int color, int sz, int tileIndex, int pixelIndex, boolean doUpdate){
    int tx = tileIndex % getNumBricksX();
    int ty = tileIndex / getNumBricksX();
    int w = pixelIndex % getBrickSizeX();
    int h = pixelIndex / getBrickSizeX();
    paintPixelArea(color, sz, tx,ty,w,h, doUpdate);
}

private void paintPixelArea(int color, int sz, int tilex, int tiley, int curw, int curh, boolean doUpdate){
    
    //       System.out.println("Attempt:" + color + " sz=" + sz + ",t:" + tileIndex + " p:" + pixelIndex);
    for(int y=0;y<sz;y++){
        for(int x=0;x<sz;x++){
            int tx = tilex;
            int ty = tiley;
            int w = curw;
            int h = curh;
            
            int remX = (w+x)/getBrickSizeX();
            int remY = (h+y)/getBrickSizeY();
            w = (w+x)%getBrickSizeX();
            h = (h+y)%getBrickSizeY();
            //             System.out.println("Innards: tile:[" + tx + "," + ty + "] pixel[" + w +"," + y + "] rem:[" + remX +"," + remY +"]");
            
            if(remX > 0){
                tx += remX;
                if(tx >= getNumBricksX()){
                    continue; // out of bounds
                }
            }
            if(remY > 0){
                ty+=remY;
                if(ty >= getNumBricksY()){
                    continue; // out of bounds
                }
            }
            //               System.out.println("Attempt:" + color + " t:" + (ty*getNumBricksX()+tx) + " p:" + ((h*getBrickSizeX()+w)));
            _stamp.setPixel(ty*getNumBricksX()+tx, (h*getBrickSizeX()+w), color, false);
        }
    }
    if(doUpdate) {
        _stamp.updateTiles();
    }
}

public void clicked(int mode, int color, int sz, int tileIndex, int pixelIndex){
    switch(mode){
        case DO_NOTHING_MODE:
            // nothing
            break;
        case DRAW_PIXEL_MODE:
            paintPixelArea(color, sz, tileIndex, pixelIndex, true);
            break;
        case DRAW_LINE_MODE:
            // nothing
            break;
        case DRAW_RECT_MODE:
            // nothing
            break;
        case FILL_RECT_MODE:
            // nothing
            break;
        case DRAW_CIRCLE_MODE:
            // nothing
            break;
        case FILL_CIRCLE_MODE:
            // nothing
            break;
        case ERASE_MODE:
            paintPixelArea(0, sz, tileIndex, pixelIndex, true);
            break;
        default:
            break;
    }
}

public void dragged(int mode, int color,  int size, int sx, int sy, int ex, int ey){
//        System.out.println("Dragged: " + sx +"," + sy + " :: " + ex + ","  + ey);
    switch(mode){
        case DO_NOTHING_MODE:
            // nothing
            break;
        case DRAW_PIXEL_MODE:
            // nothing
            break;
        case DRAW_LINE_MODE:
            drawLine(color, size, MultiTileEditorMouseAdapter.getTilePointFromXY(sx,sy, this), MultiTileEditorMouseAdapter.getTilePointFromXY(ex,ey, this));
            break;
        case DRAW_RECT_MODE:
            drawRect(color, size, MultiTileEditorMouseAdapter.getTilePointFromXY(sx,sy, this), MultiTileEditorMouseAdapter.getTilePointFromXY(ex,ey, this));
            break;
        case FILL_RECT_MODE:
            fillRect(color, size, MultiTileEditorMouseAdapter.getTilePointFromXY(sx,sy, this), MultiTileEditorMouseAdapter.getTilePointFromXY(ex,ey, this));
            break;
        case DRAW_CIRCLE_MODE:
            drawCircle(color, size, MultiTileEditorMouseAdapter.getTilePointFromXY(sx,sy, this), MultiTileEditorMouseAdapter.getTilePointFromXY(ex,ey, this));
            break;
        case FILL_CIRCLE_MODE:
            fillCircle(color, size, MultiTileEditorMouseAdapter.getTilePointFromXY(sx,sy, this), MultiTileEditorMouseAdapter.getTilePointFromXY(ex,ey, this));
            break;
        case ERASE_MODE:
            // nothing
            break;
        default:
            break;
    }
}


public boolean dragging(int mode, int color,  int size, int sx, int sy, int ex, int ey){
//        System.out.println("Dragging: " + sx +"," + sy + " :: " + ex + ","  + ey);
    
    switch(mode){
        case DO_NOTHING_MODE:
            break;
        case DRAW_PIXEL_MODE:
            paintPixelArea(color, size, MultiTileEditorMouseAdapter.getTilePointFromXY(ex,ey, this), true);
            break;
        case DRAW_LINE_MODE:
            _stamp.setOverlayLine( new Point(sx,sy), new Point(ex,ey));
            break;
        case DRAW_RECT_MODE:
            _stamp.setOverlayRect(new Rectangle(((sx<ex)?sx:ex),((sy<ey)?sy:ey),Math.abs(ex-sx),Math.abs(ey-sy)));
            break;
        case FILL_RECT_MODE:
            _stamp.setOverlayRect(new Rectangle(((sx<ex)?sx:ex),((sy<ey)?sy:ey),Math.abs(ex-sx),Math.abs(ey-sy)));
            break;
        case DRAW_CIRCLE_MODE:
            _stamp.setOverlayOval(new Rectangle(((sx<ex)?sx:ex),((sy<ey)?sy:ey),Math.abs(ex-sx),Math.abs(ey-sy)));
            break;
        case FILL_CIRCLE_MODE:
            _stamp.setOverlayOval(new Rectangle(((sx<ex)?sx:ex),((sy<ey)?sy:ey),Math.abs(ex-sx),Math.abs(ey-sy)));
            break;
        case ERASE_MODE:
            paintPixelArea(0, size, MultiTileEditorMouseAdapter.getTilePointFromXY(ex,ey, this), true);
            break;
        default:
            break;
    }
    return false;
}


private void reloadPrefs(){
    String tmp = EnvironmentUtilities.getStringEnvSetting(STAMP_PAINTER_LAST_FILE_PROPERTY, null);
    if(tmp != null){
        if(!_stamp.loadFromFile(0, 0, tmp)){
            EnvironmentUtilities.updateStringEnvSetting(STAMP_PAINTER_LAST_FILE_PROPERTY, "");
        } else {
            postLoadStamp();
        }
    }
}
private void postLoadStamp(){
    EnvironmentUtilities.updateStringEnvSetting(STAMP_PAINTER_LAST_FILE_PROPERTY, _stamp.getFileName());
    _oam = _stamp.getOAM(0);
    updateGUIFromOAM();
    updateTitle();
}

private void updateTitle(){
    String fullString = _stamp.getFileName();
    if(fullString.length() > 10){
        if(fullString.lastIndexOf(File.separatorChar) != -1){
            String subString =  fullString.substring(fullString.lastIndexOf(File.separatorChar)+1);
            fullString = subString;
        }
    }
    setTitle(FRAME_TITLE + " for: " + fullString);
}
// I think if it looks a bit like Paint, I'd be happy
private void setupUI(){
    setLayout(new BorderLayout());
    setupMenuBar();
    JPanel holderPanel = new JPanel();
    holderPanel.setLayout(new BorderLayout());
    
    JPanel imagesPanel = setupImageControlsPanel();
    holderPanel.add(imagesPanel, BorderLayout.CENTER);
    JPanel settingsPanel = setupSettingsPanel();
    holderPanel.add(settingsPanel, BorderLayout.SOUTH);
    add(holderPanel, BorderLayout.WEST);
    
    JPanel panel1 = new JPanel();
    panel1.setBorder(new EmptyBorder(1,1,1,1));
    panel1.setLayout(new BorderLayout());
    
    JPanel panel2 = new JPanel();
    panel2.setBorder(new EmptyBorder(1,1,1,1));
    panel2.setLayout(new BorderLayout());
    
    panel1.add(panel2, BorderLayout.NORTH);
    panel1.add(new JPanel(), BorderLayout.CENTER);
    panel2.add(_stamp, BorderLayout.WEST);
    panel2.add(new JPanel(), BorderLayout.CENTER);
    panel2.add(setupMouseSettingsPanel(), BorderLayout.SOUTH);
    
    add(panel1,BorderLayout.CENTER);
    
    JPanel southPanel = new JPanel();
    southPanel.setLayout(new BorderLayout());
    JPanel radioPanel = new JPanel();
    {
        JRadioButton b0 = new JRadioButton("OAM 0");
        buttonGroup.add(b0);
        radioPanel.add(b0);
        
        JRadioButton b1 = new JRadioButton("OAM 1");
        buttonGroup.add(b1);
        radioPanel.add(b1);
        
        JRadioButton b2 = new JRadioButton("OAM 2");
        buttonGroup.add(b2);
        radioPanel.add(b2);
        
        JRadioButton b3 = new JRadioButton("OAM 3");
        buttonGroup.add(b3);
        radioPanel.add(b3);
        
        
        b0.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()== ItemEvent.SELECTED){
                    setSelectedOam(0);
                }
            }
        });
        b1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()== ItemEvent.SELECTED){
                    setSelectedOam(1);
                }
            }
        });
        b2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()== ItemEvent.SELECTED){
                    setSelectedOam(2);
                }
            }
        });
        b3.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()== ItemEvent.SELECTED){
                    setSelectedOam(3);
                }
            }
        });
        updateGUIFromOAM();
    }
    southPanel.add(radioPanel, BorderLayout.NORTH);
    JPanel subPanel = new JPanel();
    southPanel.add(subPanel, BorderLayout.CENTER);
    
    _palettePanel =  new PalettePanel(this, _modelRef.getCHRModel(), true, false, 16, 0, false,true,true);
    _palettePanel.setBorder(new EmptyBorder(0,0,0,0));
    _palettePanel.removeClickListener(_palettePanel);
    
    _palettePanel.addClickListener(new PaletteClickListener() {
        public void processPaletteClick(int componentIndex, int panelSet, boolean wasRightClicked) {
            int subIndex = componentIndex % 4;
            int oamIndex = componentIndex / 4;
            if(wasRightClicked){
                _rightClickColor = subIndex;
            } else {
                _leftClickColor = subIndex;
            }
            setSelectedOam(oamIndex);
            updateGUIFromOAM();
        }
    });
    
    subPanel.add(_palettePanel, BorderLayout.NORTH);
    JPanel trueSouthPanel = new JPanel();
    trueSouthPanel.setLayout(new BorderLayout());
    trueSouthPanel.add(southPanel, BorderLayout.WEST);
    add(trueSouthPanel, BorderLayout.SOUTH);
}

private void updateGUIFromOAM(){
    Enumeration<AbstractButton> e = buttonGroup.getElements();
    int curOam = 0;
    while(e.hasMoreElements()){
        JRadioButton r = (JRadioButton)e.nextElement();
        if(curOam == _oam){
            buttonGroup.setSelected(r.getModel(), true);
            break;
        }
        curOam++;
    }
    updateMouseSettingsPanel();
}
private void setSelectedOam(int oamIndex){
    if(oamIndex != _oam){
        _oam = oamIndex;
        if(_stamp != null){
            _stamp.setAllOAM(_oam);
        }
        if(_masterStamp != null){
        	_masterStamp.setAllOAM(_oam);
        }
    }
}
private void setupMenuBar(){
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    
    final Component parent = this;
    final NESModelListener list = this;
    
    JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');
    GUIUtilities.createMenuItem(fileMenu, "Load", 'L', new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            loadStamp();
        }
    });
    GUIUtilities.createMenuItem(fileMenu, "Save", 'S', new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            saveStamp();
        }
    });
    GUIUtilities.createMenuItem(fileMenu, "Save As", 'A', new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            saveStampAs();
        }
    });
    
    JMenu paletteMenu = GUIUtilities.createMenu(menuBar, "Palette", 'P');
    GUIUtilities.createMenuItem(paletteMenu, "Load", 'L', new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
            _modelRef.loadPalette(parent, list);
        }
    }
    );
        
}

private void loadStamp(){
    if(_stamp.loadFromFile(0,0)){
        postLoadStamp();
    }
}

private void saveStamp(){
    _stamp.saveStamp();
}
private void saveStampAs(){
    _stamp.saveStampAs();
}

private void instantiateIcon(String tooltip, String resource, int mode){
    try {
        _loadedIcons[mode] = GUIUtilities.createImageIcon(resource, tooltip);
     }catch (Exception e){
        System.err.println(resource + " resource not found");
    }   
}

private void setupIcons(){
    if(_loadedIcons == null){
        _loadedIcons = new ImageIcon[NUM_MODES];
        _loadedIcons[DO_NOTHING_MODE] = null;
        instantiateIcon("Paint", "ui/icons/draw.png", DRAW_PIXEL_MODE );
        instantiateIcon("Erase", "ui/icons/eraser.png" , ERASE_MODE );
        instantiateIcon("Draw Line", "ui/icons/line.png", DRAW_LINE_MODE );
        instantiateIcon("Draw Square", "ui/icons/square.png", DRAW_RECT_MODE );
        instantiateIcon("Fill Square", "ui/icons/filledSquare.png", FILL_RECT_MODE );
        instantiateIcon("Draw Circle", "ui/icons/circle.png", DRAW_CIRCLE_MODE );
        instantiateIcon("Fill Circle", "ui/icons/filledCircle.png", FILL_CIRCLE_MODE );        
    }
}
private JButton setupControlButton(String tooltip, String imagePath, int mode ){
    setupIcons();
    JButton button = null;
    ImageIcon icon = _loadedIcons[mode];
    if(icon == null) {
        button = new JButton(tooltip);
    } else {
        button = new JButton(icon);
    }
    
    button.setToolTipText(tooltip);
    final int buttonMode = mode;
    button.addMouseListener(new MouseInputAdapter() {
        // we only care about released 
        public void mouseReleased(MouseEvent e) {            
             if( e.getButton() == MouseEvent.BUTTON1){
                _leftClickAction = buttonMode;
            }
            if( e.getButton() == MouseEvent.BUTTON3){
                _rightClickAction = buttonMode;
            }
             updateMouseSettingsPanel();
        }
     /*   public void mouseClicked(MouseEvent e) {
            System.out.println("Clicked");
            if( e.getButton() == MouseEvent.BUTTON1){
                _leftClickAction = buttonMode;
            }
            if( e.getButton() == MouseEvent.BUTTON3){
                _rightClickAction = buttonMode;
            }
        }
      */
    });
    return button;
}


private JPanel setupImageControlsPanel(){
    JPanel p = new JPanel();
    p.setBorder(new TitledBorder("Controls"));
    p.setLayout(new GridLayout(4,2,0,0));
    p.add(setupControlButton("Paint", "ui/icons/draw.png", DRAW_PIXEL_MODE ));
    p.add(setupControlButton("Erase", "ui/icons/eraser.png" , ERASE_MODE ));
    p.add(setupControlButton("Draw Line", "ui/icons/line.png", DRAW_LINE_MODE ));
    p.add(setupControlButton("Draw Square", "ui/icons/square.png", DRAW_RECT_MODE ));
    p.add(setupControlButton("Fill Square", "ui/icons/filledSquare.png", FILL_RECT_MODE ));
    p.add(setupControlButton("Draw Circle", "ui/icons/circle.png", DRAW_CIRCLE_MODE ));
    p.add(setupControlButton("Fill Circle", "ui/icons/filledCircle.png", FILL_CIRCLE_MODE ));
    
    return p;
}

private void updateMouseSettingsPanel(){
    if(_guiSetup) {
        if(_leftClickAction == ERASE_MODE){
            _leftClickColor = 0;
        }
        if(_rightClickAction == ERASE_MODE){
            _rightClickColor = 0;
        }
      _leftColorPanel.setBackground(_palettePanel.getColor(0, _oam*4+_leftClickColor));
      _leftControlPanel.setIcon(_loadedIcons[_leftClickAction]);
      _rightControlPanel.setIcon(_loadedIcons[_rightClickAction]);
      _rightColorPanel.setBackground(_palettePanel.getColor(0, _oam*4+_rightClickColor));
    }
}

private JPanel setupMouseSettingsPanel() {
    setupIcons();
    JPanel p = new JPanel();
    p.setBorder(new TitledBorder("Current Actions"));
    p.setLayout(new GridLayout(2,1));
    
    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new GridLayout(1,4));
    leftPanel.add(new JLabel("Left Mouse:"));
    _leftControlPanel = new JLabel();
    _leftColorPanel = new JPanel();
    leftPanel.add(_leftControlPanel);
    leftPanel.add(_leftColorPanel);
    p.add(leftPanel);
    
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new GridLayout(1,4));
    rightPanel.add(new JLabel("Right Mouse:"));
    _rightControlPanel = new JLabel();
    _rightColorPanel = new JPanel();
    rightPanel.add(_rightControlPanel);
    rightPanel.add(_rightColorPanel);    
    p.add(rightPanel);
    
    return p;
}

private JPanel setupSettingsPanel(){
    JPanel p = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    p.setLayout(gbl);
    p.setBorder(new TitledBorder("Settings"));
    
    gbc.anchor = GridBagConstraints.WEST;
    Class<?> intClass[] = {Integer.class};
    Class<?> boolClass[] = {Boolean.class};
    int yPos = 0;
    try {
        JCheckBox cb = GUIUtilities.addCustomCheckBox(this, gbc, gbl, p, yPos,
                SHOW_TILE_GRID_TITLE, SHOW_TILE_GRID_TOOLTIP,
                STAMP_PAINTER_SHOW_TILE_GRID, DEFAULT_STAMP_PAINTER_SHOW_TILE_GRID,
                getClass().getMethod("updateMetaTilesGrid",boolClass));
        cb.setSelected(_stampControls.getShowTileGrid());
        yPos++;
        
        cb = GUIUtilities.addCustomCheckBox(this, gbc, gbl, p, yPos,
                SHOW_OAM_GRID_TITLE, SHOW_OAM_GRID_TOOLTIP,
                STAMP_PAINTER_SHOW_OAM_GRID, DEFAULT_STAMP_PAINTER_SHOW_OAM_GRID,
                getClass().getMethod("updateMetaTilesOAMGrid",boolClass));
        cb.setSelected(_stampControls.getShowOAMGrid());
        yPos++;
        
        GUIUtilities.addCustomSpinner(this, gbc, gbl, p, yPos,
                STAMP_TILES_SCALE_TITLE, STAMP_TILES_SCALE_TOOLTIP,
                _stampControls.getScale(),
                MIN_STAMP_PAINTER_STAMP_TILES_SCALE, MAX_STAMP_PAINTER_STAMP_TILES_SCALE, STEP_STAMP_PAINTER_STAMP_TILES_SCALE,
                getClass().getMethod("updateMetaTilesScale",intClass));
        yPos++;
        
        GUIUtilities.addCustomSpinner(this, gbc, gbl, p, yPos,
                STAMP_TILES_NUM_WIDE_TITLE, STAMP_TILES_NUM_WIDE_TOOLTIP,
                _stamp.getTilesWide(),
                MIN_STAMP_PAINTER_STAMP_TILES_NUM_WIDE, MAX_STAMP_PAINTER_STAMP_TILES_NUM_WIDE, STEP_STAMP_PAINTER_STAMP_TILES_NUM_WIDE,
                getClass().getMethod("updateNumTilesWide",intClass));
        
        yPos++;
        
        GUIUtilities.addCustomSpinner(this, gbc, gbl, p, yPos,
                STAMP_TILES_NUM_HIGH_TITLE, STAMP_TILES_NUM_HIGH_TOOLTIP,
                _stamp.getTilesHigh(),
                MIN_STAMP_PAINTER_STAMP_TILES_NUM_HIGH, MAX_STAMP_PAINTER_STAMP_TILES_NUM_HIGH, STEP_STAMP_PAINTER_STAMP_TILES_NUM_HIGH,
                getClass().getMethod("updateNumTilesHigh",intClass));
        yPos++;
    } catch(Exception e){
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
public void notifyImagePaletteChanged(){
    if(_palettePanel != null) {
        _palettePanel.resetPalette();
    }    
    _stamp.notifyDisplayInterfaceUpdated();
}
public void notifySpritePaletteChanged(){
    if(_palettePanel != null) {
        _palettePanel.resetPalette();
    }    
    _stamp.notifyDisplayInterfaceUpdated();
}

public void notifyPatternTableChanged(){
    _stamp.notifyDisplayInterfaceUpdated();
}

public void notifyNameTableChanged(){
    System.out.println("Name Table Changed. Ignored.");
}

public void notifyPatternTableSelected(int pageNum, int index) {
    System.out.println("notifyPatternTableSelected " + pageNum + " " + index + " Ignored by Painter");
}

public void notifyPatternTableToBeModified(int pageNum, int index){
    System.out.println("notifyPatternTableToBeModified " + pageNum + " " + index + "Ignored by Painter");
}

// StampSettings
public int getNumBricksX(){
    return _stamp.getTilesWide();
}
public int getNumBricksY(){
    return _stamp.getTilesHigh();
}
public int getBrickSizeX(){
    return PPUConstants.CHR_WIDTH;
}
public int getBrickSizeY(){
    return PPUConstants.CHR_HEIGHT;
}
public int getScaleX(){
    return _stampControls.getScale();
}
public int getScaleY(){
    return _stampControls.getScale();
}


}
