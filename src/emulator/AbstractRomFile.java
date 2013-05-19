/*
 * AbstractRomFile.java
 *
 * Created on January 3, 2007, 4:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator;

/**
 *
 * @author abailey
 */
public abstract class AbstractRomFile {
    
    protected  boolean _isDirty;   
    protected String _fullROMFileName;
    protected String _romFileNameOnly;
    
    /** Creates a new instance of AbstractRomFile */
    public AbstractRomFile() {
            _isDirty = false;
            _fullROMFileName = null;
            _romFileNameOnly = null;
    }
    
    public boolean isDirty() {
        return _isDirty;
    }
    
    protected abstract boolean  validateHeader(byte[] header);
}
