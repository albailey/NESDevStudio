/*
 * TransparentImageLevel.java
 *
 * Created on August 13, 2008, 11:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr.nameTable;

import java.awt.Image;
import java.awt.image.MemoryImageSource;

import javax.swing.JComponent;

/**
 *
 * @author abailey
 */
public class TransparentImageLevel extends JComponent {
   
    /**
	 * 
	 */
	private static final long serialVersionUID = 4394424235635263951L;
	private MemoryImageSource _mis = null;
    private Image _img = null;
    private String _description = null;
    private boolean _isVisible = false;
    
    /** Creates a new instance of TransparentImageLevel */
    public TransparentImageLevel(MemoryImageSource mis, String description, boolean isVisible) {
        _mis = mis;
        _description = description;
        _isVisible = isVisible;
        _mis.setAnimated(true);
        _img = null;        
    }
    public String getDescription() {
    	return _description;
    }
    public boolean isVisible(){
        return _isVisible;
    }
    public void setVisible(boolean flag){
        _isVisible = flag;
        refresh();
    }
     public void refresh(){
         _mis.newPixels();
     }
    public void reset(){
        _mis.newPixels();
        _img = createImage(_mis);        
    }
    public Image getImg(){
        if(_img == null){
            reset();
        }        
        return _img;
    }
}
