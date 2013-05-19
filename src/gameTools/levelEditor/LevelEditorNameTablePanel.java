/*
 * LevelEditorNameTablePanel.java
 *
 * Created on June 30, 2008, 8:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package gameTools.levelEditor;

import gameTools.stampSetEditor.ActiveStampObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import model.NESModelListener;
import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.nameTable.NameTablePanel;
import ui.chr.tileEditor.CHRMultiTilePanel;
import ui.chr.tileEditor.CHRTile;
import utilities.FileUtilities;

/**
 *
 * @author abailey
 */
public class LevelEditorNameTablePanel extends NameTablePanel implements ActiveStampObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2767687228186529155L;
	private LevelEditorModel levelModel = null;
	private LevelEditorSettings settings = null;
	private CHRMultiTilePanel _activeStamp = null;
	private Hashtable<Integer ,MetaTile> _metaTileTable = null;
	private ArrayList<CHRTile> _tileList = null;
	private int metaTile[][] = null;
	
	/** Creates a new instance of LevelEditorNameTablePanel */
	public LevelEditorNameTablePanel(NESModelListener callback, CHREditorModel model, LevelEditorModel levModel, LevelEditorSettings newSettings) {
		super(callback, model, -1, false);
		leftMode = NOTHING_MODE;
		leftControlMode = NOTHING_MODE;
		rightMode = NOTHING_MODE;
		rightControlMode = NOTHING_MODE;

		levelModel = levModel;
		settings = newSettings;

		int minPage = determineStartingPage();
		int maxPage = determineEndingPage();
		model.setNumNameTablePages((maxPage - minPage) + 1);
		metaTile = new int[determineNumPages()][256];
		_metaTileTable = new Hashtable<Integer, MetaTile>();
		_tileList = new ArrayList<CHRTile>();
		updateNameTableTiles(false);
		initSetup();
		setupMouse();
	}

	/* Override these 6 methods for panels that consist of multiple nametables */
	public int getTilesWide() {
		return levelModel.getScreensWide() * levelModel.getTilesWide();
	}

	public int getTilesHigh() {
		return levelModel.getScreensHigh() * levelModel.getTilesHigh();
	}

	public int determineIndex(int tileX, int tileY) {
		return (tileY % PPUConstants.NAME_TABLE_HEIGHT) * PPUConstants.NAME_TABLE_WIDTH + (tileX % PPUConstants.NAME_TABLE_WIDTH);
	}

	public int determinePage(int tileX, int tileY) {
		if (levelModel.getScreensHigh() > 1) {
			int pageWide = tileX / PPUConstants.NAME_TABLE_WIDTH;
			int pageHigh = tileY / PPUConstants.NAME_TABLE_HEIGHT;
			return pageHigh * levelModel.getScreensWide() + pageWide;
		} else {
			return tileX / PPUConstants.NAME_TABLE_WIDTH;
		}
	}

	public int determineStartingPage() {
		return 0;
	}

	private int determineNumPages() {
		// same as (determineEndingPage() -  determineStartingPage() + 1;
		return (levelModel.getScreensHigh() * levelModel.getScreensWide());
	}
	
	public int determineEndingPage() {
		return (levelModel.getScreensHigh() * levelModel.getScreensWide()) - 1;
	}

	public void setActiveStamp(CHRMultiTilePanel stamp, int bank) {
		if (bank == 0) {
			if (stamp == null) {
				leftMode = NOTHING_MODE;
				_activeStamp = null;
			} else {
				leftMode = INSERT_STAMP_MODE;
				_activeStamp = stamp;
				Iterator<MetaTile> iter = _activeStamp.getMetaTiles();
				int mIndex = _activeStamp.getMetaInsertionPoint();
				while(iter.hasNext()){
					_metaTileTable.put(mIndex, iter.next());
					mIndex++;
				}
				
			}
		}
	}

	public int getNumTiles() {
		return _tileList.size();
	}
	public Iterator<CHRTile> getTiles() {
		return _tileList.iterator();
	}
	public Iterator<MetaTile> getMetaTiles() {
		Vector<MetaTile> v = new Vector<MetaTile>();
		int sz = _metaTileTable.size();
		for(int i=0;i<sz;i++){
			MetaTile t =_metaTileTable.get(i);
			if(t == null){
				System.err.println("Missing the metatile at metatile index:  " + i);
			}
			v.add(t);
		}
		return v.iterator();
	}
	private void repairMetaTileArray(int newLen, int oldLen){
		if(oldLen != newLen) {
			// change it
			int smallerLen = newLen;
			if(oldLen < smallerLen){
				smallerLen = oldLen;
			}
			int tmp[][] = new int[newLen][256];
			for(int i=0;i<smallerLen;i++){
				System.arraycopy(metaTile[i][0],0,tmp[i][0],0,256);				
			}
			metaTile = tmp;
		}
	}
	public int getMetaTileIndex(int pg, int x, int y) {
		repairMetaTileArray(determineNumPages(), metaTile.length);
		int mtOffset = x + y*16;
		return metaTile[pg][mtOffset];
	}
	public boolean loadAsciiColumnsFromFile(File selectedFile)    {
		if(selectedFile == null){
			return false;
		}
		byte tempBytes[] = FileUtilities.loadAsciiBytes(selectedFile);
		int len = tempBytes.length;
		int numPages = (int)Math.ceil(len / 256.0);
		
		metaTile = new int[numPages][256];
		for(int i=0;i<len;i++){
			int pg = i / 256;
			int index = i % 256;

			int x = (index / 16);
			int y = (index % 16);

			metaTile[pg][y*16+x] = tempBytes[i];
			int mtIndex = (int)(tempBytes[i] & 0xFF);
			
			MetaTile t =_metaTileTable.get(mtIndex);
			if(t == null) {
				continue;
			}
			x = (index / 16)*2;
			y = (index % 16)*2;
			if(y < 30){
				modelRef.setNameTableTileAndOAM(pg, x + (y * 32), t._topLeftTile, t._oam);
				modelRef.setNameTableTileAndOAM(pg, (x+1) + (y * 32), t._topRightTile, t._oam);
				modelRef.setNameTableTileAndOAM(pg, x + ((y+1) * 32), t._bottomLeftTile, t._oam);
				modelRef.setNameTableTileAndOAM(pg, (x+1) + ((y+1) * 32), t._bottomRightTile, t._oam);
			}
		}
		return true;
	}
	
	public void refreshNameTableIndex(int pg, int x, int y, MetaTile t){
		if(t == null) {
			return;
		}
		if(y < 30){
			modelRef.setNameTableTileAndOAM(pg, x + (y * 32), t._topLeftTile, t._oam);
			modelRef.setNameTableTileAndOAM(pg, (x+1) + (y * 32), t._topRightTile, t._oam);
			modelRef.setNameTableTileAndOAM(pg, x + ((y+1) * 32), t._bottomLeftTile, t._oam);
			modelRef.setNameTableTileAndOAM(pg, (x+1) + ((y+1) * 32), t._bottomRightTile, t._oam);
		}
	}
	public void stampChanged(CHRMultiTilePanel stamp) {
		int pt = stamp.getMetaInsertionPoint();
		int sz = stamp.getMetaTilesSize();
		
		int metatiles[] = new int[sz];
		for(int i=0;i<sz;i++){
			int mtIndex = pt+i;
			metatiles[i] = mtIndex; 
			MetaTile t =_metaTileTable.get(mtIndex);
			t._oam = (byte)(stamp.getOAM(i) & 0xFF);
			t._objectType = stamp.getObjectType(i);
		}
		updateNameTableForMetaTiles(metatiles);
	}
	 
	public void updateNameTableForMetaTiles(int metatiles[])    {
		int numPages = metaTile.length;
		for(int pg=0;pg<numPages;pg++){
			for(int q=0;q<metatiles.length;q++){
				int mtIndex = metatiles[q];
				MetaTile t =_metaTileTable.get(mtIndex);
				int i = 0;
				for(int y=0;y<30;y+=2){					
					for(int x=0;x<32;x+=2){
						if(metaTile[pg][i] == mtIndex) {
							refreshNameTableIndex(pg,x,y, t);
						}
						i++;
					}
				}
			}
			updateNameTable(pg); 
		}
	}
	
	
	public void insertStamp(int pg, int index, int oamSubIndex, int oamTableIndex, int modifiers) {
		// do nothing
		if (_activeStamp == null) {
			return;
		}
	
		if (_activeStamp.enforceOAMBounds()) {
			int newIndex = index;
			if (newIndex % 2 == 1) {
				newIndex--;
			}
			if (newIndex % 64 != newIndex % 32) {
				newIndex -= 32;
			}
			// System.out.println("Stamp being inserted:" + pg +"," + newIndex +"," + oamSubIndex + "," + oamTableIndex + "," + modifiers );
			insertTileArray(pg, newIndex, _activeStamp.getOAM(), _activeStamp.getTilesWide(), _activeStamp.getTilesHigh(), _activeStamp.getTiles());
		} else {
			insertTileArray(pg, index, _activeStamp.getOAM(), _activeStamp.getTilesWide(), _activeStamp.getTilesHigh(), _activeStamp.getTiles());
		}
	}

	public void insertTileArray(int pg, int index, int oam[], int wid, int hgt, CHRTile[] tiles) {
		if ((index % 32 + wid) > 32) {
			// adjust wid so we dont wrap around
			wid -= ((index % 32 + wid) - 32);
		}
		if ((index + (32 * hgt) > (30 * 32))) {
			// adjust hgt so we dont mess up
			hgt -= ((index + (32 * hgt)) / 32) - 30;
		}
		// the index is in tiles (32 wide)
		// TO DO: fix this so I can stamp across pages.
		int tileCounter = 0;
		for (int y = 0; y < hgt; y++) {
			for (int x = 0; x < wid; x++) {
				int ntOffset = index + x + y * 32;
				modelRef.setNameTableTileAndOAM(pg, ntOffset, (byte) _activeStamp.getInsertionPointAt(tileCounter), (byte) oam[tileCounter]);
				tileCounter++;
			}
		}

		int mw = wid/2;
		int mh = hgt/2;
		
		int mtIndex = (index/64 * 16) + ((index%64)/2);
		
		repairMetaTileArray(determineNumPages(), metaTile.length);
		//System.out.println("Inserting meta starting at page:" + pg + " and index:" + mtIndex );
		// We also need to insert the metatile info into the metatile set and array.
		int metaCounter = _activeStamp.getMetaInsertionPoint();
		for(int y=0;y<mh;y++){
			for(int x=0;x<mw;x++){
				int mtOffset = mtIndex + x + y*16;				
				metaTile[pg][mtOffset] = metaCounter;
				metaCounter++;
			}
		}
		
		updateNameTable(pg);        // add to the name table

	}

	private void setupMouse() {
		//    MouseAdapterHelper ma = new MouseAdapterHelper ();
		//    addMouseListener(ma);
	}
}
