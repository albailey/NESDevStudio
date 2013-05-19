package gameTools.levelEditor;

import ui.chr.model.CHREditorModel;

public class HeckLifterLevelEditor extends LevelEditor {
	
	private static final long serialVersionUID = 5073431189556837415L;
	
	public final static String FRAME_TITLE = "Hecklifter Level Editor";
	
	public HeckLifterLevelEditor() {
		this(null,null);
	}
	// customized to remove settings that are invalid.
	public HeckLifterLevelEditor(CHREditorModel existingModel, LevelEditorModel levelModel) {
		super(existingModel, levelModel);
		customizeHecklifterGUI();		
	}
	
	// by overriding these methods we can disable visibility of controls we do not want the user to alter
	protected boolean tilesWideVisible() {
		return false;
	}
	
	protected boolean tilesHighVisible() {
		return false;
	}
	
	protected boolean screensHighVisible() {
		return false;
	}
	
	protected boolean isViewControlsEnabled() {
		return true;
	}

	public String getFrameTitle() {
		return FRAME_TITLE;
	}

	private void customizeHecklifterGUI(){
		updateLevelTilesWide(32);
		updateLevelTilesHigh(22);
		updateLevelTilesWide(32);
		updateLevelScreensHigh(1);
//		setShowTileGrid(false);
//		setShowOAMGrid(true);
//		setShowScreenBounds(true);
		updateGUIControlsBasedOnEditorModel();
	}

}
