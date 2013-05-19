package gameTools.imageHelper;

import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;

public class ImageFilter {

	public final static int ENABLED_INDEX = 0;
	public final static int DESCRIPTION_INDEX = 1;
	public final static int SETTINGS_INDEX = 2;
	
	private LookupTable _lookupTable;
	private LookupOp _lop;
	private String _description;
	private boolean _isEnabled;
	
	public ImageFilter(String description, LookupTable lookupTable) {
		_description = description;
		_lookupTable = lookupTable;
		_lop = new LookupOp(_lookupTable, null);	
		_isEnabled = true;
	}
	
	public Object getColumnValue(int index) {
		if(index == ENABLED_INDEX) {
			return Boolean.valueOf(_isEnabled);
		}
		if(index == DESCRIPTION_INDEX) {
			return _description;
		}
		if(index == SETTINGS_INDEX) {
			return getSettings();
		}
		return null;
	}
	public boolean setColumnData(int index, Object aValue){
		if(index == ENABLED_INDEX) {
			_isEnabled = (Boolean)aValue;
			return true;
		}
		// nothign changed
		return false;
		
	}

	public void setEnabled(boolean flag){
		_isEnabled = flag;
	}
	public String getDescription() {
		return _description;
	}
	
	public String getSettings() {
		return "Settings...";
	}
	
	public boolean isEnabled() {
		return _isEnabled;
	}
	
	public void applyFilter(BufferedImage src, BufferedImage dest) {
        _lop.filter (src, dest);
    }
}
