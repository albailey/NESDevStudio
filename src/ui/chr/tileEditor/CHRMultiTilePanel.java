/*
 * CHRMultiTilePanel.java
 *
 * Created on July 22, 2008, 10:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.tileEditor;

import gameTools.levelEditor.MetaTile;
import gameTools.stampEditor.StampUtilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.MemoryImageSource;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;

import ui.chr.CHRDisplayControls;
import ui.chr.CHRDisplayInterface;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.nameTable.TransparentImageLevel;
import utilities.FileUtilities;

/**
 * 
 * @author abailey
 */
public class CHRMultiTilePanel extends JPanel implements CHRDisplayInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5767960891673466143L;

	private CHRDisplayControls _controls = null;

	private int _tilesWide;
	private int _tilesHigh;
	private int _width;
	private int _height;
	private int _insertionPoint; // location in the pattern table where this
									// stamp is inserted
	private int _metaInsertionPoint;
	private int _insertionPoints[];
	private Vector<MetaTile> _metaTileList = null;
	private int _oam[];
	public int _objectTypes[];
	

	private int _pix[] = null;
	private int _gridPix[] = null;
	private byte _mask[][] = null;
	private String _fileName = null;
	private CHREditorModel _modelRef = null;
	private int _palette = PPUConstants.IMAGE_PALETTE_TYPE;

	private Vector<TransparentImageLevel> _imgLevels = new Vector<TransparentImageLevel>();
	private TransparentImageLevel _pixelLayer = null;
	private TransparentImageLevel _gridLayer = null;
	private String _description = null;

	private boolean _isGridDirty = true;
	private boolean _lastShowGrid = true;
	private boolean _lastShowOAMGrid = true;

	private Rectangle _rect = null;
	private Rectangle _oval = null;
	private Point _p1 = null;
	private Point _p2 = null;

	public CHRMultiTilePanel(int tilesWide, int tilesHigh,
			CHREditorModel model, CHRDisplayControls sharedControls) {
		_controls = sharedControls;
		_modelRef = model;
		_insertionPoint = -1;
		_metaInsertionPoint = -1;
		_insertionPoints = null;
		_description = "??";
		_tilesWide = tilesWide;
		_tilesHigh= tilesHigh;
		_oam = new int[tilesWide*tilesHigh];
		_objectTypes = new int[tilesWide*tilesHigh];
		
		// we are a blank stamp with a blank metatile initially
		_metaTileList = new Vector<MetaTile>();
		recreateModelData(tilesWide, tilesHigh, _oam, _objectTypes, null);
	}

	public CHRMultiTilePanel(CHRMultiTilePanel copy) {
		_controls = copy._controls;
		_modelRef = copy._modelRef;
		_insertionPoint = copy._insertionPoint;
		_metaInsertionPoint = copy._metaInsertionPoint;
		_insertionPoints = copy._insertionPoints;
		_description = copy._description;
		_oam = new int[copy._oam.length];
		System.arraycopy(copy._oam,0,_oam,0,copy._oam.length);
		_objectTypes = new int[copy._objectTypes.length];
		System.arraycopy(copy._objectTypes,0,_objectTypes,0,copy._objectTypes.length);		
		_mask = copy._mask;
		// we are a blank stamp with a blank meta tile initially
		_metaTileList = copy._metaTileList;
		_tilesWide = copy._tilesWide;
		_tilesHigh = copy._tilesHigh;
		_fileName = copy._fileName;
		recreateDisplayData();
	}

	public int[] getOAM() {
		return _oam;
	}
	
	
	public void copyIntoPalette(CHREditorModel model) {		
		model.getCHRModel().assignImagePalette(_modelRef.getCHRModel().imagePalette,0);
		model.getCHRModel().assignSpritePalette(_modelRef.getCHRModel().spritePalette,0);
	}
	
	public void setRegion(CHRMultiTilePanel multiRegion, int x, int y, int index){
		// x and y are in OAM indices, not tile. So multiply by 2 to get the tile index
		int outerPos = (y*2*multiRegion.getTilesWide()) + (x * 2);		
		System.arraycopy(multiRegion._mask[outerPos],0,_mask[0],0,16);
		System.arraycopy(multiRegion._mask[outerPos+1],0,_mask[1],0,16);
		System.arraycopy(multiRegion._mask[outerPos+multiRegion.getTilesWide()],0,_mask[2],0,16);
		System.arraycopy(multiRegion._mask[outerPos+multiRegion.getTilesWide()+1],0,_mask[3],0,16);		
		
		int oam = multiRegion.getOAM(index);
		_oam[0] = oam;
		_oam[1] = oam;
		_oam[2] = oam;
		_oam[3] = oam;
		
		int objType = multiRegion.getObjectType(index);
		_objectTypes[0] = objType;
		_objectTypes[1] = objType;
		_objectTypes[2] = objType;
		_objectTypes[3] = objType;
		
		notifyDisplayInterfaceUpdated();      
	}
	
	public int convertSectorToTileIndex(int sector){
		int tx = _tilesWide / 2;
		return (((sector % tx)*2) + ((sector / tx)*_tilesWide*2));
	}
	
	public int getOAM(int sector) {
		// sector means a 2x2 OAM region.
		// we store OAM per tile (a waste) so we need to convert to that mode
		return _oam[convertSectorToTileIndex(sector)];
	}
	public int getObjectType(int sector){
		return _objectTypes[convertSectorToTileIndex(sector)];
	}

	public int getTileOAM(int index) {
		// sector means a 2x2 OAM region.
		// we store OAM per tile (a waste) so we need to convert to that mode
		return _oam[index];
	}
	public int getTileObjectType(int index) {
		// sector means a 2x2 OAM region.
		// we store OAM per tile (a waste) so we need to convert to that mode
		return _objectTypes[index];
	}
	
	public void setSectorObjectType(int sector, int objType){
		int index = convertSectorToTileIndex(sector);
		_objectTypes[index] = objType;
		_objectTypes[index+1] = objType;
		_objectTypes[index+_tilesWide] = objType;
		_objectTypes[index+_tilesWide+1] = objType;	
		// no need to update the screen
	//	updateTiles();
	}
	
	public void setSectorOAM(int sector, int oam) {
		// sector means a 2x2 OAM region.
		// we store OAM per tile (a waste) so we need to convert to that mode
		int index = convertSectorToTileIndex(sector);
		_oam[index] = oam;
		_oam[index+1] = oam;
		_oam[index+_tilesWide] = oam;
		_oam[index+_tilesWide+1] = oam;	
		updateTiles();
	}	

	public void setPaletteType(int palette) {
		_palette = palette;
		updateTiles();
	}

	public void setAllOAM(int oam) {
		int len = _oam.length;
		for(int i=0;i<len;i++){
			_oam[i] = oam;
		}		
		updateTiles();
	}	
	
	public void cycleOAM() {
		int len = _oam.length;
		for(int i=0;i<len;i++){
			_oam[i] = (_oam[i] + 1) % 4;
		}
		updateTiles();
	}

	public void setPixel(int tileIndex, int pixelIndex, int color, boolean shouldUpdate) {
		byte lower = (byte) (color & 1);
		byte upper = (byte) ((color >> 1) & 1);
		int row = pixelIndex / 8;
		int column = pixelIndex % 8;
		_mask[tileIndex][row] = CHRTile.setBit(_mask[tileIndex][row], lower,
				column);
		_mask[tileIndex][row + 8] = CHRTile.setBit(_mask[tileIndex][row + 8],
				upper, column);
		if (shouldUpdate) {
			updateTiles();
		}
	}

	private void recreateModelData(int tilesWide, int tilesHigh, int oam[], int objectTypes[], byte[][] newMask) {
		_tilesWide = tilesWide;
		_tilesHigh = tilesHigh;
		_oam = new int[oam.length];
		System.arraycopy(oam,0,_oam,0,oam.length);
		_objectTypes = new int[objectTypes.length];
		System.arraycopy(objectTypes,0,_objectTypes,0,objectTypes.length);
		
		if (newMask == null) {
			_mask = new byte[_tilesWide * _tilesHigh][16]; // all zero initially
		} else if (newMask.length != _tilesWide * _tilesHigh) {
			_mask = new byte[_tilesWide * _tilesHigh][16]; // all zero initially
		} else {
			_mask = newMask;
		}
		_insertionPoints = new int[_tilesWide * _tilesHigh]; // all zero

		int index = 0;
		for (int y = 0; y < _tilesHigh; y++) {
			for (int x = 0; x < _tilesWide; x++) {
				if ((x % 2 == 0) && (y % 2 == 0)) {
					// new meta entry
					_metaTileList.add(new MetaTile(0, 0, 0, 0, oam[index],objectTypes[index]));
				}
				index++;
			}
		}
		recreateDisplayData();
	}

	public void setTilesWide(int val) {
		int newW = val;
		if (_tilesWide < val)
			newW = _tilesWide;
		int newDim = val * _tilesHigh;
		byte newMask[][] = new byte[newDim][16];
		for (int j = 0; j < _tilesHigh; j++) {
			for (int i = 0; i < newW; i++) {
				System.arraycopy(_mask[j * _tilesWide + i], 0, newMask[j * val+ i], 0, 16);
			}
		}
		_tilesWide = val;
		_mask = newMask;
		recreateDisplayData();
	}

	public void setTilesHigh(int val) {
		int newH = val;
		if (_tilesHigh < val)
			newH = _tilesHigh;
		int newDim = _tilesWide * val;
		int minDim = _tilesWide * newH;
		byte newMask[][] = new byte[newDim][16];
		for (int i = 0; i < minDim; i++) {
			System.arraycopy(_mask[i], 0, newMask[i], 0, 16);
		}
		_tilesHigh = val;
		_mask = newMask;
		recreateDisplayData();
	}

	private void recreateDisplayData() {
		if(_oam.length != _tilesWide * _tilesHigh){
			System.err.println("WEIRD OAM SIZE:" + _oam.length + " vs actual:" + ( _tilesWide * _tilesHigh));
			_oam = new int[_tilesWide * _tilesHigh];			
		}
		_width = _tilesWide * PPUConstants.CHR_WIDTH;
		_height = _tilesHigh * PPUConstants.CHR_HEIGHT;
		int overallPixelLength = _width * _height;

		_imgLevels.clear();
		_isGridDirty = true;

		_pix = new int[overallPixelLength];// all values are zero.
		MemoryImageSource pixMIS = new MemoryImageSource(_width, _height, _pix,
				0, _width);
		_pixelLayer = new TransparentImageLevel(pixMIS, "Tiles", true);
		_imgLevels.add(_pixelLayer);

		_gridPix = new int[overallPixelLength];// all values are zero.
		MemoryImageSource overlayMIS = new MemoryImageSource(_width, _height,
				_gridPix, 0, _width);
		_gridLayer = new TransparentImageLevel(overlayMIS, "Grid", true);
		_imgLevels.add(_gridLayer);

		setMinimumSize(new Dimension(_width, _height));
		setPreferredSize(new Dimension(_width * _controls.getScale(), _height
				* _controls.getScale()));
		setBackground(Color.WHITE);
		_rect = null;
		_oval = null;
		_p1 = null;
		_p2 = null;
	}

	
	
	public int getMetaTilesSize() {
		return (_tilesWide / 2) * (_tilesHigh / 2);
	}

	public int getTilesSize() {
		return _tilesWide * _tilesHigh;
	}

	public void setDescription(String d) {
		_description = d;
	}

	public String getDescription() {
		return _description;
	}

	public boolean loadFromFile(int startingOffset, int startingMetaOffset) {
		return loadFromFile(startingOffset, startingMetaOffset, null);
	}

	private boolean saveStampToFile(File f) {
		return StampUtilities.storeStampTiles(f, _tilesWide, _tilesHigh, _objectTypes, getTiles());
	}

	public boolean saveStampAs() {
		File selectedFile = FileUtilities.selectFileForSave(this);
		if (selectedFile == null) {
			return false;
		}
		return saveStampToFile(selectedFile);
	}

	public boolean saveStamp() {
		if (_fileName == null) {
			return saveStampAs();
		}
		try {
			File selectedFile = new File(_fileName);
			if (!selectedFile.canWrite()) {
				return saveStampAs();
			}
			return saveStampToFile(selectedFile);
		} catch (Exception e) {
			e.printStackTrace();
			return saveStampAs();
		}
	}

	public boolean loadFromFile(int startingOffset, int startingMetaOffset, String lastFile) {

		File selectedFile = null;
		if (lastFile == null) {
			selectedFile = FileUtilities.selectFileForOpen(this);
			if (selectedFile == null) {
				return false;
			}
		} else {
			selectedFile = new File(lastFile);
		}
		if (!selectedFile.canRead()) {
			return false;
		}
		_fileName = selectedFile.getAbsolutePath();
		if (reloadFromFile()) {
			setInsertionPoint(startingOffset, startingMetaOffset);
			_description = selectedFile.getName();
			if (_description.lastIndexOf(".") != -1) {
				_description = _description.substring(0,
						_description.lastIndexOf("."));
			}
			return true;
		} else {
			_insertionPoint = -1;
			_metaInsertionPoint = -1;
			return false;
		}
	}

	public int getInsertionPoint() {
		return _insertionPoint;
	}

	public int getMetaInsertionPoint() {
		return _metaInsertionPoint;
	}

	public Iterator<MetaTile> getMetaTiles() {
		return _metaTileList.iterator();
	}

	public int getInsertionPointAt(int index) {
		return _insertionPoints[index];
	}

	public void setSingleTilePattern(int tileVal) {
		// basically this is a special case for a stamp that is the same tile
		// repeated over and over
		_metaTileList = new Vector<MetaTile>();
		int i = 0;
		for (int y = 0; y < _tilesHigh; y++) {
			for (int x = 0; x < _tilesWide; x++) {
				_insertionPoints[i] = tileVal;
				if ((x % 2 == 0) && (y % 2 == 0)) {
					// new meta entry
					_metaTileList.add(new MetaTile(tileVal, tileVal, tileVal,
							tileVal, _oam[i], _objectTypes[i]));
				}
				i++;
			}
		}
	}

	public void setInsertionPoint(int val, int metaVal) {
		_insertionPoint = val;
		_metaInsertionPoint = metaVal;
		_metaTileList = new Vector<MetaTile>();

		int pt = _insertionPoint;
		int i = 0;
		for (int y = 0; y < _tilesHigh; y++) {
			for (int x = 0; x < _tilesWide; x++) {
				_insertionPoints[i] = pt;
				if ((x % 2 == 0) && (y % 2 == 0)) {
					// new meta entry
					_metaTileList.add(new MetaTile(pt, pt + _tilesWide, pt + 1,
							pt + 1 + _tilesWide, _oam[i], _objectTypes[i]));
				}
				pt++;
				i++;
			}
		}

	}

	public String getFileName() {
		return _fileName;
	}

	// file consists of
	// width
	// height
	// OAM index
	// wid x hgt x 16 bytes (each mask is 16 bytes) representation of the tiles
	public boolean reloadFromFile() {
		if (_fileName == null) {
			return false;
		}
		boolean success = true;
		int tilesWide = 0;
		int tilesHigh = 0;
		
		byte mask[][] = null;
		try {
			File f = new File(_fileName);
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);

			int oam[] = null;
			int objectTypes[] = null;
			try {
				tilesWide = (int) (dis.readByte() & 0xFF);
				tilesHigh = (int) (dis.readByte() & 0xFF);
				System.out.println("Stamp: " + _fileName + " => " + tilesWide + " x " + tilesHigh);
				oam = new int[tilesWide * tilesHigh];
				objectTypes = new int[tilesWide * tilesHigh];
				mask = new byte[tilesWide * tilesHigh][16];
				for (int i = 0; i < tilesWide * tilesHigh; i++) {
					int rd = dis.read(mask[i], 0, 16);
					if (rd != 16) {
						System.err.println("Missing some mask values");
						success = false;
						break;
					}
					oam[i] = (int) (dis.readByte() & 0xFF);
					objectTypes[i] = (int) (dis.readByte() & 0xFF);
				}
			} catch (Exception e) {
				e.printStackTrace();
				success = false;
			}
			dis.close();
			fis.close();
			if (success) {
				recreateModelData(tilesWide, tilesHigh, oam, objectTypes, mask);
				notifyDisplayInterfaceUpdated();
			}
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	public int getTilesWide() {
		return _tilesWide;
	}

	public int getTilesHigh() {
		return _tilesHigh;
	}

	public CHRTile[] getTiles() {
		CHRTile tiles[] = new CHRTile[getTilesSize()];
		int counter = 0;
		for (int y = 0; y < _tilesHigh; y++) {
			for (int x = 0; x < _tilesWide; x++) {
				tiles[counter] = new CHRTile(_mask[counter], _palette,_modelRef, _oam[counter]);
				counter++;
			}
		}
		return tiles;
	}

	public boolean enforceOAMBounds() {
		return true;
	}

	// This needs to be improved
	public void updateTiles() {
		int pixIndex[] = new int[64];
		int pix[] = new int[64];
		int oamIndex = 0;
		for (int y = 0; y < _tilesHigh; y++) {
			for (int x = 0; x < _tilesWide; x++) {
				int rowOffset = (y * _tilesWide * 64) + (x * 8);
				int maskIndex = (y * _tilesWide) + x;

				int pixOffset = 0;
				for (int i = 0; i < 8; i++) {
					byte tempVal = 0;
					byte m1 = _mask[maskIndex][i];
					byte m2 = _mask[maskIndex][i + 8];
					for (int q = 0; q < 8; q++) {
						tempVal = (byte) ((m1 & 1) + ((m2 & 1) << 1));
						pixIndex[pixOffset + 7 - q] = tempVal;
						pix[pixOffset + 7 - q] = _modelRef.getPaletteColor(
								_palette, _oam[oamIndex], tempVal).getRGB();
						m1 = (byte) (m1 >> 1);
						m2 = (byte) (m2 >> 1);
					}
					System.arraycopy(pix, pixOffset, _pix,
							(pixOffset * _tilesWide) + rowOffset, 8);
					pixOffset += 8;
				}
				oamIndex++;
			}
		}
		_pixelLayer.reset();
	}

	public CHRDisplayControls getControls() {
		return _controls;
	}

	public void updateGrid() {
		if (_isGridDirty || _lastShowGrid != _controls.getShowTileGrid()
				|| _lastShowOAMGrid != _controls.getShowOAMGrid()) {
			_isGridDirty = false;
			_lastShowGrid = _controls.getShowTileGrid();
			_lastShowOAMGrid = _controls.getShowOAMGrid();
			// first clear the old grid
			int cnt = 0;
			int transparentPix[] = new int[_width];
			for (int i = 0; i < _height; i++) {
				System.arraycopy(transparentPix, 0, _gridPix, cnt,
						transparentPix.length);
				cnt += transparentPix.length;
			}
			if (_controls.getShowTileGrid()) {
				int gridSzY = PPUConstants.CHR_HEIGHT;
				int gridSzX = PPUConstants.CHR_WIDTH;
				int gridColor = _controls.getGridColor().getRGB();
				for (int y = 0; y < _height; y++) {
					if ((y % gridSzY) == 0) {
						for (int x = 0; x < _width; x++) {
							_gridPix[y * _width + x] = gridColor;
						}
					} else {
						for (int x = 0; x < _width; x += gridSzX) {
							_gridPix[y * _width + x] = gridColor;
						}
					}
					// draw right line
					_gridPix[y * _width + _width - 1] = gridColor;
				}
				// draw bottom line
				for (int x = 0; x < _width; x++) {
					_gridPix[(_height - 1) * _width + x] = gridColor;
				}
			}
			if (_controls.getShowOAMGrid()) {
				int gridSzY = PPUConstants.CHR_HEIGHT * 2;
				int gridSzX = PPUConstants.CHR_WIDTH * 2;
				int gridColor = _controls.getOAMGridColor().getRGB();
				for (int y = 0; y < _height; y++) {
					if ((y % gridSzY) == 0) {
						for (int x = 0; x < _width; x++) {
							_gridPix[y * _width + x] = gridColor;
						}
					} else {
						for (int x = 0; x < _width; x += gridSzX) {
							_gridPix[y * _width + x] = gridColor;
						}
					}
					// draw right line
					_gridPix[y * _width + _width - 1] = gridColor;
				}
			}
			_gridLayer.reset();
		}

	}

	public void notifyDisplayInterfaceUpdated() {
		setPreferredSize(new Dimension(_width * _controls.getScale(), _height
				* _controls.getScale()));
		revalidate();
		updateTiles();
		updateGrid();
		repaint();
	}

	public void setOverlayRect(Rectangle r) {
		_p1 = null;
		_p2 = null;
		_oval = null;
		_rect = r;
		repaint();
	}

	public void setOverlayOval(Rectangle r) {
		_p1 = null;
		_p2 = null;
		_rect = null;
		_oval = r;
		repaint();
	}

	public void setOverlayLine(Point p1, Point p2) {
		_rect = null;
		_oval = null;
		_p1 = p1;
		_p2 = p2;
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g); // paints background
		Iterator<TransparentImageLevel> iter = _imgLevels.iterator();
		while (iter.hasNext()) {
			TransparentImageLevel layer = iter.next();
			if (layer != null && layer.isVisible()) {
				g.drawImage(layer.getImg(), 0, 0,
						_width * _controls.getScale(),
						_height * _controls.getScale(), this);
			}
		}
		if (_rect != null) {
			g.setColor(Color.BLACK);
			g.drawRect(_rect.x, _rect.y, _rect.width, _rect.height);
		}
		if (_oval != null) {
			g.setColor(Color.BLACK);
			g.drawOval(_oval.x, _oval.y, _oval.width, _oval.height);
		}
		if (_p1 != null && _p2 != null) {
			g.setColor(Color.BLACK);
			g.drawLine(_p1.x, _p1.y, _p2.x, _p2.y);
		}
	}

}
