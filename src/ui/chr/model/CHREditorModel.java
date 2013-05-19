/*
 * CHREditorModel.java
 *
 * Created on October 3, 2006, 3:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.model;

import gameTools.animationHelper.AnimationKeyframeData;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import model.CHRModel;
import model.NESModelListener;
import model.NESPaletteListener;
import ui.chr.PPUConstants;
import ui.chr.tileEditor.CHRTile;
import ui.testrom.TestRomHelper;
import utilities.ByteFormatter;
import utilities.CompressionUtilities;
import utilities.EnvironmentUtilities;
import utilities.FileUtilities;


/**
 *
 * @author abailey
 */
public class CHREditorModel {

	public final static String CHR_EDITOR_MODEL_LAST_PROJECT_PROPERTY = "CHR_EDITOR_MODEL_LAST_PROJECT_PROPERTY";

	public final static int CONTROL_MODE_NORMAL = 0;
	public final static int CONTROL_MODE_DRAG = 1;

	private final static int DEFAULT_CONTROL_MODE = CONTROL_MODE_NORMAL;
	private final static int MIN_CONTROL_MODE = CONTROL_MODE_NORMAL;
	private final static int MAX_CONTROL_MODE = CONTROL_MODE_DRAG;

	private int controlMode = DEFAULT_CONTROL_MODE;

	public int lastPatternIndex;
	public int lastPageNum;

	public CHRTile patternTableTiles[][] = null;
	public CHRTile nameTableTiles[][] = null;


	public final static int MAX_FRAMES = 60;
	private AnimationKeyframeData keyframes[][] = null;

	private CHRModel modelRef;
	private CHRProject project;
	private int activeImagePaletteIndex;
	private int activeSpritePaletteIndex;

	private String projectFileName = null;


	public CHREditorModel() {
		this(null);
	}

	/** Creates a new instance of CHREditorModel */
	public CHREditorModel(String prjFileName) {
		modelRef = new CHRModel();
		project = new CHRProject();
		activeImagePaletteIndex = 0;
		activeSpritePaletteIndex = 0;
		lastPageNum = 0;
		lastPatternIndex = 0;
		patternTableTiles = new CHRTile[PPUConstants.NUM_PATTERN_PAGES][PPUConstants.COLUMNS_PER_PATTERN_PAGE * PPUConstants.ROWS_PER_PATTERN_PAGE];
		nameTableTiles = new CHRTile[1][PPUConstants.NAME_TABLE_WIDTH * PPUConstants.NAME_TABLE_HEIGHT];
		projectFileName = prjFileName;
	}


	public boolean getNameTableAsciiMode(){
		if(project != null){
			return project.getNameTableAsciiMode();
		} else {
			return true;
		}
	}
	public void setNameTableAsciiMode(boolean val){
		if(project != null){
			project.setNameTableAsciiMode(val);
		}
	}

	public void setNameTableCompression(int val){
		if(project != null){
			project.setNameTableCompressionType(val);
		}
	}
	public int getNameTableCompression(){
		if(project != null){
			return project.getNameTableCompressionType();
		} else {
			return CompressionUtilities.NO_COMPRESSION;
		}
	}

	public void setControlMode(int newMode){
		if((newMode < MIN_CONTROL_MODE) || (newMode > MAX_CONTROL_MODE)){
			System.err.println("Invalid mode:" + newMode +" Using default:" + DEFAULT_CONTROL_MODE);
			controlMode = DEFAULT_CONTROL_MODE;
		} else {
			controlMode = newMode;
		}
	}
	
	public CHRTile getPatternTableTile(int index){
		return getPatternTableTile(lastPageNum, index);
	}
	public CHRTile getPatternTableTile(int pageNum, int index){
		return patternTableTiles[pageNum][index];
	}
	public void resetTiles(int pageNum){
		patternTableTiles[pageNum] = new CHRTile[PPUConstants.COLUMNS_PER_PATTERN_PAGE * PPUConstants.ROWS_PER_PATTERN_PAGE];
	}
	public CHRModel getCHRModel(){
		return modelRef;
	}

	public int getActiveImagePaletteIndex() {
		return activeImagePaletteIndex;
	}
	public int getActiveSpritePaletteIndex() {
		return activeSpritePaletteIndex;
	}


	public Color getPaletteColor(int paletteIndex, int preOam, int preVal){
		int oam = preOam;
		if(oam >= 4 || oam < 0){
			oam = 0;
		}
		int val = (oam * 4) + preVal;
		if(paletteIndex == PPUConstants.IMAGE_PALETTE_TYPE){
			return PPUConstants.NES_PALETTE[modelRef.imagePalette[val]];
		}
		if(paletteIndex == PPUConstants.SPRITE_PALETTE_TYPE){
			return PPUConstants.NES_PALETTE[modelRef.spritePalette[val]];
		}
		// some unknown type. Use the palette
		return PPUConstants.NES_PALETTE[val];
	}

	public Color getSpecificPaletteColor(int pal, int val){
		if(pal == 1){
			return PPUConstants.NES_PALETTE[modelRef.spritePalette[val]];
		} else {
			return PPUConstants.NES_PALETTE[modelRef.imagePalette[val]];
		}
	}

	public Color getImagePaletteColor(int val){
		return PPUConstants.NES_PALETTE[modelRef.imagePalette[val]];
	}
	public Color getSpritePaletteColor(int val){
		return PPUConstants.NES_PALETTE[modelRef.spritePalette[val]];
	}
	public Color getNESPaletteColor(int val){
		return PPUConstants.NES_PALETTE[val];
	}

	public void setNumNameTablePages(int numNTPages){
		if(numNTPages != nameTableTiles.length){
			CHRTile newTiles[][] = new CHRTile[numNTPages][PPUConstants.NAME_TABLE_WIDTH * PPUConstants.NAME_TABLE_HEIGHT];
			int max = numNTPages;
			if(nameTableTiles.length < max){
				max = nameTableTiles.length;
			}
			for(int i=0;i<max;i++){
				System.arraycopy(nameTableTiles[i],0,newTiles[i],0,PPUConstants.NAME_TABLE_WIDTH * PPUConstants.NAME_TABLE_HEIGHT);
			}
			if(max < numNTPages ){
				if(getPatternTableTile(0,0) != null){
					CHRTile tileZero = getPatternTableTile(0,0);
					for(int i=max;i<numNTPages;i++){
						for(int j=0;j<PPUConstants.NAME_TABLE_WIDTH * PPUConstants.NAME_TABLE_HEIGHT;j++){
							newTiles[i][j] = new CHRTile(tileZero,0);
						}
					}
				}
			}
			nameTableTiles = newTiles;
		}
	}
	public void relocateTilesAndNametables(int startingValue, int endingValue, int newStartingValue, boolean asGrid) {

	}

	public void setNameTableTileAndOAM(int page, int index, byte chrVal, byte oam){
		updateNameTableOAM(page, index, oam);
		getCHRModel().nameTableIndexes[page][index] = chrVal;
		updateNameTableIndex(page, index, chrVal);
	}
	public void updateNameTableOAM(int page, int index, byte newOAM){

		int relX = index % PPUConstants.NAME_TABLE_WIDTH;
		int relY = index / PPUConstants.NAME_TABLE_WIDTH;
		int oamIndexX = relX  / 4; // gets us in terms of an 8x8 table
		int oamIndexY = relY / 4;
		int oamTableIndex = oamIndexY * PPUConstants.OAM_INDEX_GRID_SIZE + oamIndexX;

		int oamGroupX = (relX / 2) % 2; // gets us in terms of a 16 x 16 table
		int oamGroupY = (relY / 2) % 2;
		int oamSubIndex = oamGroupY * 2 + oamGroupX;

		byte oamVal = getCHRModel().oamValues[page][oamTableIndex];
		byte b[] = new byte[4];
		for(int i=0;i<4;i++){
			b[i] = (byte)((oamVal >> (i*2)) & 0x03);
			if(i == oamSubIndex){
				b[i] = newOAM;
			}
		}
		byte newOamVal = (byte)(((b[3] << 6) + (b[2] << 4) + (b[1] << 2) + b[0]) & 0xFF);
		getCHRModel().oamValues[page][oamTableIndex] = newOamVal;
	}

	// for Name Tables
	// get the pattern table tile for the value and assign it to the name table at that index
	public void updateNameTableIndex(int page, int index, byte byteValue){
		int value = (byteValue & 0xFF);
		CHRTile orig = getPatternTableTile(value);
		if(orig != null) {
			int oamVal = getCHRModel().getOAMFromNTIndex(page, index);
			nameTableTiles[page][index] = new CHRTile(orig, oamVal);
		}
	}

	// Stuff for animations
	public void addAnimation(){
		int oldNum = getNumAnimations();
		AnimationKeyframeData newkeyframes[][] = new AnimationKeyframeData[oldNum+1][MAX_FRAMES];
		for(int i=0;i<oldNum;i++){
			System.arraycopy(keyframes[i],0,newkeyframes[i],0,keyframes[i].length);
		}
		newkeyframes[oldNum][0] = new AnimationKeyframeData();
		keyframes = newkeyframes;
	}
	public void removeAnimation(int index){
		AnimationKeyframeData newkeyframes[][] = new AnimationKeyframeData[keyframes.length-1][MAX_FRAMES];
		int destIndex = 0;
		for(int i=0;i<keyframes.length;i++){
			if(i!=index){
				System.arraycopy(keyframes[i],0,newkeyframes[destIndex],0,keyframes[i].length);
				destIndex++;
			}
		}
		keyframes = newkeyframes;
	}

	public int getNumAnimations(){
		if(keyframes == null){
			return 0;
		} else {
			return keyframes.length;
		}
	}
	public int getAnimationDuration(int row){
		if(row < 0 || row >= getNumAnimations()){
			return 0;
		} else {
			return keyframes[row].length;
		}
	}
	public AnimationKeyframeData getKeyFrame(int row, int col){
		if(col < 0 || col >= getAnimationDuration(row)){
			return null;
		}
		return keyframes[row][col];
	}

	public AnimationKeyframeData constructKeyFrame(int row, int col){
		if(col < 0 || col >= getAnimationDuration(row)){
			return null;
		}

		keyframes[row][col] = new AnimationKeyframeData();
		for(int i=col-1; i>=0;i--){
			if(keyframes[row][i] != null){
				keyframes[row][i].copyInto(keyframes[row][col]);
				break;
			}
		}
		return keyframes[row][col];
	}
	public AnimationKeyframeData removeKeyFrame(int row, int col){
		if(col < 0 || col >= getAnimationDuration(row)){
			return null;
		}
		AnimationKeyframeData ret = keyframes[row][col];
		keyframes[row][col] = null;
		return ret;
	}


	public boolean newProject(Component parentComponent, NESModelListener listener)    {
		projectFileName = null;
		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		projectFileName = selectedFile.getAbsolutePath();
		project = new CHRProject();
		return true;
	}

	public boolean loadLastProject(NESModelListener listener){
		String lastProjPath = EnvironmentUtilities.getStringEnvSetting(CHR_EDITOR_MODEL_LAST_PROJECT_PROPERTY, null);
		if(lastProjPath == null){
			return false;
		}
		File selectedFile = new File(lastProjPath);
		return loadProject(selectedFile, listener);
	}

	public boolean loadProject(Component parentComponent, NESModelListener listener)    {
		File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
		return loadProject(selectedFile, listener);
	}

	public boolean loadProject(File selectedFile, NESModelListener listener)    {
		if(selectedFile == null){
			return false;
		}
		projectFileName = null;
		project = new CHRProject();
		try {
			project.loadFile(selectedFile);
		} catch(Exception e){
			//      e.printStackTrace();
			System.err.println("Unable to load project: " + selectedFile.getAbsolutePath());
			return false;
		}
		projectFileName = selectedFile.getAbsolutePath();
		EnvironmentUtilities.updateStringEnvSetting(CHR_EDITOR_MODEL_LAST_PROJECT_PROPERTY, projectFileName);

		if(project == null) {
			project = new CHRProject();
			return false;
		}

		// load in those sub components
		if(project.getPaletteFileName() != null){
			loadPaletteFromFile(new File(selectedFile.getParentFile(),project.getPaletteFileName()), listener);
		}
		if(project.getPatternTableFileName() != null){
			loadPatternTablesFromFile(new File(selectedFile.getParentFile(),project.getPatternTableFileName()), listener);
		}
		if(project.getNameTableFileName() != null){
			loadNameTableFromFile(0, new File(selectedFile.getParentFile(),project.getNameTableFileName()), listener);
		}
		if(project.getAnimationsFileName() != null){
			loadAnimationsFromFile(new File(selectedFile.getParentFile(),project.getAnimationsFileName()), listener);
		}
		System.out.println("Loaded Project:" + selectedFile.getAbsolutePath());
		System.out.println("\tPattern Table:" + project.getPatternTableFileName());
		System.out.println("\tPalette:" + project.getPaletteFileName());
		System.out.println("\tName Table (BG):" + project.getNameTableFileName());
		System.out.println("\t\tCompression Mode:" + project.getNameTableCompressionType());
		System.out.println("\tAnimations:" + project.getAnimationsFileName());

		return true;
	}
	public boolean saveProject(Component parentComponent, NESModelListener listener){
		if(projectFileName == null){
			return saveAsProject(parentComponent, listener);
		}
		File selectedFile  = new File(projectFileName);

		boolean ret = saveProjectToFile(selectedFile);
		if(ret) {
			projectFileName = selectedFile.getAbsolutePath();
			EnvironmentUtilities.updateStringEnvSetting(CHR_EDITOR_MODEL_LAST_PROJECT_PROPERTY, projectFileName);
			if(project.getPaletteFileName() != null){
				savePalette(parentComponent);
			}
			if(project.getPatternTableFileName() != null){
				savePatternTables(parentComponent, listener);
			}
			if(project.getNameTableFileName() != null){
				saveNameTable(parentComponent, listener);
			}
			if(project.getAnimationsFileName() != null){
				saveAnimations(parentComponent);
			}
		}
		return ret;
	}
	public boolean saveAsProject(Component parentComponent, NESModelListener listener){
		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		boolean ret = saveProjectToFile(selectedFile);
		projectFileName = selectedFile.getAbsolutePath();

		if(ret) {
			projectFileName = selectedFile.getAbsolutePath();
			EnvironmentUtilities.updateStringEnvSetting(CHR_EDITOR_MODEL_LAST_PROJECT_PROPERTY, projectFileName);
			if(project.getPaletteFileName() != null){
				savePalette(parentComponent);
			}
			if(project.getPatternTableFileName() != null){
				savePatternTables(parentComponent, listener);
			}
			if(project.getNameTableFileName() != null){
				saveNameTable(parentComponent, listener);
			}
			if(project.getAnimationsFileName() != null){
				saveAnimations(parentComponent);
			}
		}
		return ret;
	}

	private boolean saveProjectToFile(File selectedFile){
		try {
			String full = selectedFile.getAbsolutePath();
			String partial = full;
			int index =  full.lastIndexOf('.');
			int index2 =  full.lastIndexOf(File.separatorChar);
			if(index != -1 && index2 != -1 && index > index2){
				partial = full.substring(0,index);
			}
			if(project.getPaletteFileName() == null){
				project.setPaletteFileName( FileUtilities.generateRelativePath(selectedFile, new File(partial+".pal")));
			}
			if(project.getPatternTableFileName() == null){
				project.setPatternTableFileName( FileUtilities.generateRelativePath(selectedFile, new File(partial+".chr")));
			}
			if(project.getNameTableFileName() == null){
				project.setNameTableFileName( FileUtilities.generateRelativePath(selectedFile, new File(partial+".nam")));
			}
			if(project.getAnimationsFileName() == null){
				project.setAnimationsFileName( FileUtilities.generateRelativePath(selectedFile, new File(partial+".anim")));
			}
			project.storeFile(selectedFile);

			System.out.println("Saved Project:" + selectedFile.getAbsolutePath());
			System.out.println("\tPattern Table:" + project.getPatternTableFileName());
			System.out.println("\tPalette:" + project.getPaletteFileName());
			System.out.println("\tName Table (BG):" + project.getNameTableFileName());
			System.out.println("\t\tCompression Mode:" + project.getNameTableCompressionType());
			System.out.println("\tAnimations:" + project.getAnimationsFileName());

		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// load/save animation info to disk
	public boolean loadAnimations(Component parentComponent, NESModelListener listener)    {
		File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
		return loadAnimationsFromFile(selectedFile, listener);
	}
	public boolean loadAnimationsFromFile(File selectedFile, NESModelListener listener)    {
		if(selectedFile == null){
			return false;
		}
		try {
			byte buffer[] = FileUtilities.loadUnknownBytes(selectedFile);
			keyframes = null;
			if(buffer != null) {
				System.out.println("Animation bytes being loaded:" + buffer.length);
				TestRomHelper.decodeAnimationBytes(buffer,0,this);
			}
			return true;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean saveAnimations(Component parentComponent){
		if(project.getAnimationsFileName() == null){
			return saveAsAnimations(parentComponent);
		}
		File selectedFile = null;
		if(projectFileName == null){
			selectedFile  = new File( project.getAnimationsFileName());
		} else {
			selectedFile  = new File(new File(projectFileName).getParentFile(), project.getAnimationsFileName());
		}
		boolean ret = saveAnimationsToFile(selectedFile);
		if(ret) {
			if(projectFileName != null){
				project.setAnimationsFileName(FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
			}
		}
		return ret;
	}
	public boolean saveAsAnimations(Component parentComponent){
		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		boolean ret = saveAnimationsToFile(selectedFile);
		if(ret) {
			if(projectFileName != null){
				project.setAnimationsFileName( FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
			}
		}
		return ret;
	}

	private boolean saveAnimationsToFile(File selectedFile){        
		byte b[] = TestRomHelper.encodeAnimationBytes(this);
		System.out.println("Animation bytes being saved:" + b.length);
		return FileUtilities.saveBytes(selectedFile, b, 0, b.length, false);
	}


	// load/save palette to disk
	public boolean loadPalette(Component parentComponent, NESPaletteListener listener)    {
		File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
		return loadPaletteFromFile(selectedFile, listener);
	}
	public CHRProject getProject(){
		return project;
	}

	public void setPaletteValue(int palType, int index, byte val, NESPaletteListener listener){
		if(index >=0 && index < PPUConstants.NES_SPRITE_PALETTE_SIZE){
			if(palType == PPUConstants.SPRITE_PALETTE_TYPE)   { 
				getCHRModel().spritePalette[index] = val;    
				if(listener != null) {
					listener.notifySpritePaletteChanged();
				}
			} else {
				getCHRModel().imagePalette[index] = val;   
				if(listener != null) {
					listener.notifyImagePaletteChanged();
				}
			}
		}
	}
	public boolean loadAsciiPaletteFromFile(File selectedFile, NESPaletteListener listener)    {
		return loadPaletteFromFile(selectedFile, listener, true);
	}
	public boolean loadPaletteFromFile(File selectedFile, NESPaletteListener listener)    {
		return loadPaletteFromFile(selectedFile, listener, false);
	}
	
	public boolean loadPaletteFromFile(File selectedFile, NESPaletteListener listener, boolean isAscii)    {
		if(selectedFile == null){
			return false;
		}
		int maxPalette = 32;
		byte tempPalette[] = new byte[maxPalette];
		// load in the palette

		int paletteCount = 0;
		if(isAscii) {
			tempPalette = FileUtilities.loadAsciiBytes(selectedFile);
			paletteCount = tempPalette.length;
		} else {
			paletteCount = FileUtilities.loadBytes(selectedFile, tempPalette, maxPalette);
			
		}

		if(paletteCount < 16){
			System.err.println("Palette too small");
			return false;
		}
		if(paletteCount == 16){
			getCHRModel().assignImagePalette(tempPalette, 0);
			getCHRModel().assignSpritePalette(tempPalette, 0);
		} else {
			// first 16 are image palette,
			getCHRModel().assignImagePalette(tempPalette, 0);
			// next 16 are sprite
			getCHRModel().assignSpritePalette(tempPalette, 16);
		}
		if(projectFileName != null){
			project.setPaletteFileName(FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
		}
		if(listener != null) {
			listener.notifyImagePaletteChanged();
			listener.notifySpritePaletteChanged();
		}
		return true;
	}


	public boolean savePalette(Component parentComponent){
		if(project.getPaletteFileName() == null){
			return saveAsPalette(parentComponent);
		}
		File selectedFile = null;
		if(projectFileName == null){
			selectedFile  = new File( project.getPaletteFileName());
		} else {
			selectedFile  = new File(new File(projectFileName).getParentFile(), project.getPaletteFileName());
		}

		boolean ret = savePaletteToFile(selectedFile);
		if(ret) {
			if(projectFileName != null){
				project.setPaletteFileName(FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
			}
		}
		return ret;
	}
	public boolean saveAsPalette(Component parentComponent){
		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		boolean ret = savePaletteToFile(selectedFile);
		if(ret) {
			if(projectFileName != null){
				project.setPaletteFileName( FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
			}
		}
		return ret;
	}


	private boolean savePaletteToFile(File selectedFile){
		return FileUtilities.saveBytes(selectedFile
				, getCHRModel().imagePalette
				, getCHRModel().imagePalette.length
				, getCHRModel().spritePalette
				, getCHRModel().spritePalette.length
		);
	}
	public boolean saveSpecificPalette(Component parentComponent, NESPaletteListener listener, int palType, int start, int dur){
		if(palType != PPUConstants.IMAGE_PALETTE_TYPE && palType != PPUConstants.SPRITE_PALETTE_TYPE){
			System.err.println("Invalid palType:" + palType);
			return false;
		}
		if(start < 0 || start >= 16){
			System.err.println("Invalid palette starting point:" + start +" needs to be between 0<=N<16");
			return false;
		}
		if(dur <= 0 || dur > 16){
			System.err.println("Invalid palette duration:" + dur +" needs to be 0 < N <=16");
			return false;
		}

		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}

		return FileUtilities.saveBytes(selectedFile,
				((palType ==  PPUConstants.IMAGE_PALETTE_TYPE) ?  (getCHRModel().imagePalette) :   (getCHRModel().spritePalette)),
				start,
				dur
		);
	}
	// load/save name tables to disk
	public boolean newNameTable(Component parentComponent, NESModelListener listener)    {
		File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
		return newNameTableFromFile(selectedFile, listener);
	}

	public boolean newNameTableFromFile(File selectedFile, NESModelListener listener)    {
		if(selectedFile == null){
			return false;
		}

		CHRModel mod = getCHRModel();
		int numPages = mod.nameTableIndexes.length;
		for(int p=0;p<numPages;p++){
			for(int i=0;i<PPUConstants.NUM_NAME_TABLE_ENTRIES;i++){
				mod.nameTableIndexes[p][i] = 0;
			}
			for(int i=0; i< PPUConstants.OAM_TABLE_SIZE; i++){
				mod.oamValues[p][i] = 0;
			}
		}
		if(projectFileName != null){
			project.setNameTableFileName(FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
		}

		if(listener != null) {
			listener.notifyNameTableChanged();
		}
		return true;
	}

	// load/save name tables to disk
	public boolean loadNameTable(Component parentComponent, NESModelListener listener)    {
		File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
		return loadNameTableFromFile(0, selectedFile, listener);
	}

	public boolean loadNameTableFromFile(int page, File selectedFile, NESModelListener listener)    {

		if(selectedFile == null){
			return false;
		}

		int compressionType = CompressionUtilities.NO_COMPRESSION;

		// load in the name table
		byte tempRawBytes[] = FileUtilities.loadAllBytes(selectedFile);
		int tempLoadCount = tempRawBytes.length;

		System.out.println("File Size:" + tempLoadCount);
		int headerOffset = 0;
		if(tempLoadCount == PPUConstants.TOTAL_NUM_NAME_TABLE_ENTRIES){ // its just a 1K nametable
			headerOffset = 0;
			compressionType = 0;
		} else if(tempRawBytes[0] == 0 || tempRawBytes[0] == 1) {
			// either No compression or RLE compression
			headerOffset = 3;
			compressionType = tempRawBytes[0];
		} else {
			// likely an ascii version
			tempRawBytes = FileUtilities.loadAsciiBytes(selectedFile);
			tempLoadCount = tempRawBytes.length;
			if(tempLoadCount == PPUConstants.TOTAL_NUM_NAME_TABLE_ENTRIES){ // its just a 1K nametable
				headerOffset = 0;
				compressionType = 0;
			} else if(tempRawBytes[0] == 0 || tempRawBytes[0] == 1) {
				// either No compression or RLE compression
				headerOffset = 3;
				compressionType = tempRawBytes[0];
			}   else {
				System.err.println("Unexpected nametable format.");
				return false;
			}
		}
		byte moreTempBytes[] = new byte[tempLoadCount-headerOffset];
		System.arraycopy(tempRawBytes,headerOffset,moreTempBytes,0,tempLoadCount-headerOffset);

		byte rawBytes[] = CompressionUtilities.decompressData( compressionType, moreTempBytes);
		int loadCount = rawBytes.length;
		System.out.println("Extracted bytes" + loadCount);


		if(loadCount < PPUConstants.TOTAL_NUM_NAME_TABLE_ENTRIES){
			System.err.println("Name Table too small. Loaded:" + loadCount + " need at least:" + PPUConstants.TOTAL_NUM_NAME_TABLE_ENTRIES + " preferrably:" + rawBytes.length);
			return false;
		}
		setNameTableCompression(compressionType);
		CHRModel mod = getCHRModel();
		for(int i=0;i<PPUConstants.NUM_NAME_TABLE_ENTRIES;i++){
			mod.nameTableIndexes[page][i] = (byte)(rawBytes[i] & 0xFF);
		}
		int offset = 0;
		for(int i=PPUConstants.NUM_NAME_TABLE_ENTRIES; i < PPUConstants.TOTAL_NUM_NAME_TABLE_ENTRIES; i++){
			mod.oamValues[page][offset] = (byte)(rawBytes[i] & 0xFF);
			offset++;
		}
		if(projectFileName != null){
			project.setNameTableFileName(FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
		}
		if(listener != null) {
			listener.notifyNameTableChanged();
		}
		return true;
	}
	public boolean saveNameTable(Component parentComponent, NESModelListener listener){
		if(project.getNameTableFileName() == null){
			return saveAsNameTable(0, parentComponent, listener);
		}
		File selectedFile  = new File(new File(projectFileName).getParentFile(), project.getNameTableFileName());

		boolean ret = saveNameTableToFile(0, selectedFile);
		if(ret) {
			project.setNameTableFileName( FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
		}
		return ret;
	}
	public boolean saveAsNameTable(int page, Component parentComponent, NESModelListener listener){
		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		boolean ret = saveNameTableToFile(page, selectedFile);
		if(ret) {
			if(project != null && projectFileName != null) {
				project.setNameTableFileName( FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
			}
		}
		return ret;
	}

	// TO DO: Add ASCII support for nametable saves.

	private boolean saveNameTableToFile(int pg, File selectedFile){
		int compressionType = CompressionUtilities.RLE_COMPRESSION;
		boolean isAscii = false;
		int asciiEntriesPerLine = 16;
		if(project != null){
			compressionType =  project.getNameTableCompressionType();
			isAscii =  project.getNameTableAsciiMode();
			asciiEntriesPerLine =  project.getNameTableAsciiEntries();
		}

		byte tempRawBytes[] = new byte[getCHRModel().nameTableIndexes[pg].length + getCHRModel().oamValues[pg].length];
		System.arraycopy(getCHRModel().nameTableIndexes[pg],0,tempRawBytes,0,getCHRModel().nameTableIndexes[pg].length);
		System.arraycopy(getCHRModel().oamValues[pg],0,tempRawBytes,getCHRModel().nameTableIndexes[pg].length,getCHRModel().oamValues[pg].length);

		if(compressionType == CompressionUtilities.NO_COMPRESSION) {
			if(isAscii){
				return FileUtilities.saveAsciiBytes(selectedFile
						, tempRawBytes
						, tempRawBytes.length
						, asciiEntriesPerLine
				);

			} else {
				return FileUtilities.saveBytes(selectedFile
						, tempRawBytes
						, tempRawBytes.length
				);
			}
		} else {
			byte compressedBytes[] = CompressionUtilities.compressData(compressionType,tempRawBytes);
			byte totalBytes[] = new byte[compressedBytes.length+3];
			totalBytes[0] = (byte)(compressionType & 0xFF);
			totalBytes[1] = (byte)(compressedBytes.length & 0xFF);
			totalBytes[2] = (byte)((compressedBytes.length >> 8) & 0xFF);
			System.arraycopy(compressedBytes,0,totalBytes,3,compressedBytes.length);

			if(isAscii) {
				return FileUtilities.saveAsciiBytes(selectedFile
						, totalBytes
						, totalBytes.length
						, asciiEntriesPerLine
				);
			} else {
				return FileUtilities.saveBytes(selectedFile
						, totalBytes
						, totalBytes.length
				);
			}
		}
	}


	// load/save pattern tables (CHR files) to disk
	public boolean loadPatternTables(Component parentComponent, NESModelListener listener)    {
		File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
		return loadPatternTablesFromFile(selectedFile, listener);
	}
	public boolean loadPatternTablesPortion(Component parentComponent, NESModelListener listener)    {
		File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
		if(selectedFile == null){
			return false;
		}
		byte tempData[] = new byte[PPUConstants.PATTERN_TABLE_PAGE_SIZE*64];
		int dataLoaded = FileUtilities.loadBytes(selectedFile, tempData, tempData.length);
		int numPortions = dataLoaded / PPUConstants.PATTERN_TABLE_PAGE_SIZE;
		if(numPortions * PPUConstants.PATTERN_TABLE_PAGE_SIZE != dataLoaded){
			System.err.println("Warning. An unusual CHR data file was processed (not 4K chunks) of size:" + ByteFormatter.formatInt(dataLoaded));
		}
		if(numPortions == 0){
			return false;
		}
		// choose bank to override
		int bank = 0;
		{
			Object[] possibleValues = { "Bank 0", "Bank 1", "Cancel" };
			Object selectedValue = JOptionPane.showInputDialog(parentComponent,
					"Choose CHR Bank to override", "Choosing which bank to overwrite",
					JOptionPane.INFORMATION_MESSAGE, null,
					possibleValues, possibleValues[0]);
			if(selectedValue == null){
				return false;
			}
			if(selectedValue == possibleValues[2]){
				return false;
			}
			if(selectedValue == possibleValues[0]){
				bank = 0;
			}
			if(selectedValue == possibleValues[1]){
				bank = 1;
			}
		}
		{
			Object possibleValues[] = new String[numPortions+1];
			for(int i=0;i<numPortions;i++){
				possibleValues[i] = "Bank " + i;
			}
			possibleValues[numPortions] = "Cancel";
			Object selectedValue = JOptionPane.showInputDialog(parentComponent,
					"Choose CHR Bank to load from the file", "Choosing which bank to load",
					JOptionPane.INFORMATION_MESSAGE, null,
					possibleValues, possibleValues[0]);
			if(selectedValue == null){
				return false;
			}
			int counter = 0;
			for(counter = 0; counter < possibleValues.length; counter++){
				if(selectedValue == possibleValues[counter]){
					break;
				}
			}
			if(counter >= numPortions){
				System.err.println("Cancel pressed");
				return false;
			}

			getCHRModel().assignPatternTable(bank, tempData,PPUConstants.PATTERN_TABLE_PAGE_SIZE*counter); // offset of 0
		}
		if(listener != null) {
			listener.notifyPatternTableChanged();
		}
		return true;
	}
	public boolean loadPatternTablesFromFile(File selectedFile, NESModelListener listener)    {
		if(selectedFile == null){
			return false;
		}
		int dataSize =  PPUConstants.PATTERN_TABLE_PAGE_SIZE * 2;
		byte tempData[] = new byte[dataSize];
		// load in the palette
		int dataLoaded = FileUtilities.loadBytes(selectedFile, tempData, dataSize);

		if(dataLoaded != dataSize && dataLoaded != PPUConstants.PATTERN_TABLE_PAGE_SIZE){
			System.err.println("Loaded a subset of the pattern table. Only loaded:" + dataLoaded + " from file:" + selectedFile);
		}

		// write the data to the pages. Note: Page 1 may be blank
		getCHRModel().assignPatternTable(0, tempData,0); // offset of 0
		getCHRModel().assignPatternTable(1,tempData, PPUConstants.PATTERN_TABLE_PAGE_SIZE); // offset of 4096
		if(projectFileName != null) {
			project.setPatternTableFileName(FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
		}

		if(listener != null) {
			listener.notifyPatternTableChanged();
		}
		return true;
	}

	public boolean savePatternTables(Component parentComponent, NESModelListener listener){
		if(project.getPatternTableFileName() == null){
			return saveAsPatternTables(parentComponent, listener);
		}
		File selectedFile  = new File(new File(projectFileName).getParentFile(), project.getPatternTableFileName());


		boolean ret = savePatternTablesToFile(selectedFile);
		if(ret) {
			if(projectFileName != null) {
				project.setPatternTableFileName( FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
			}
		}
		return ret;
	}
	public boolean saveAsPatternTables(Component parentComponent, NESModelListener listener){
		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		boolean ret = savePatternTablesToFile(selectedFile);
		if(ret) {
			if(projectFileName != null) {
				project.setPatternTableFileName( FileUtilities.generateRelativePath(new File(projectFileName), selectedFile));
			}
		}
		return ret;
	}
	private boolean savePatternTablesToFile(File selectedFile){
		return FileUtilities.saveBytes(selectedFile
				, getCHRModel().patternTable[0]
				                             , getCHRModel().patternTable[0].length
				                             , getCHRModel().patternTable[1]
				                                                          , getCHRModel().patternTable[1].length
		);
	}

	private boolean savePatternTableBankToFile(File selectedFile, int chrBank, int offset, int duration){
		return FileUtilities.saveBytes(selectedFile,
				getCHRModel().patternTable[chrBank]
				                           , offset
				                           , duration
		);
	}

	public boolean savePatternTableBankPortion(Component parentComponent, NESModelListener listener, int offset, int duration, int page){
		if(offset < 0 || offset >= PPUConstants.PATTERN_TABLE_PAGE_SIZE ) {
			System.err.println("Invalid starting offset: " + offset  + " needs to be between 0 and " + PPUConstants.PATTERN_TABLE_PAGE_SIZE);
			return false;
		}
		if(duration < 0 || duration + offset > PPUConstants.PATTERN_TABLE_PAGE_SIZE ) {
			System.err.println("Invalid starting offset and duration: [" + offset  + ":" + duration + "] needs to combine to stay between 0 and " + PPUConstants.PATTERN_TABLE_PAGE_SIZE);
			return false;
		}
		if(page < 0 || page >= PPUConstants.NUM_PATTERN_PAGES) {
			System.err.println("Invalid CHR page  [" + page +  "] needs to be between 0 and " + PPUConstants.NUM_PATTERN_PAGES);
			return false;
		}

		File selectedFile = FileUtilities.selectFileForSave(parentComponent);
		if(selectedFile == null){
			return false;
		}
		return savePatternTableBankToFile(selectedFile, page, offset, duration);
	}
	public boolean savePatternTableBankPortion(Component parentComponent, NESModelListener listener, int offset, int duration){
		return savePatternTableBankPortion(parentComponent, listener, offset, duration, lastPageNum);
	}

	public boolean savePatternTableBankPortion(Component parentComponent, NESModelListener listener){
		return savePatternTableBankPortion(parentComponent, listener, 0, PPUConstants.PATTERN_TABLE_PAGE_SIZE);
	}

	public boolean savePatternTableBankByIndex(Component parentComponent, NESModelListener listener, int pageIndex){
		return savePatternTableBankPortion(parentComponent, listener, 0, PPUConstants.PATTERN_TABLE_PAGE_SIZE, pageIndex);
	}

}
