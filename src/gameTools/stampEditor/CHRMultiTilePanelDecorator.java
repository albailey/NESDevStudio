/*
 * CHRMultiTilePanelDecorator.java
 *
 * Created on August 12, 2008, 11:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampEditor;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ui.chr.CHRDisplayControls;
import ui.chr.CHRDisplayInterface;
import ui.chr.tileEditor.CHRMultiTilePanel;

/**
 *
 * @author abailey
 */
public class CHRMultiTilePanelDecorator extends JPanel implements CHRDisplayInterface {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3301995389294230169L;
	private CHRMultiTilePanel _tilePanel = null;
    private boolean _isRemovable = true;
    private boolean _isAdded = false;
    private Properties _metaProperties = null;
    
    /** Creates a new instance of CHRMultiTilePanelDecorator */
    public CHRMultiTilePanelDecorator(CHRMultiTilePanel tilePanel, int palette) {
        _tilePanel = tilePanel;
        _tilePanel.setPaletteType(palette);
        setLayout(new BorderLayout());
        String title = tilePanel.getDescription();
        
        JPanel titledPanel = new JPanel();
        if(title != null){
            titledPanel.setBorder(new TitledBorder(title));
        }
        add(titledPanel, BorderLayout.CENTER);
        titledPanel.setLayout(new BorderLayout());
        titledPanel.add(tilePanel, BorderLayout.CENTER);
    }
    
    /*
    public void setPaletteType(int paletteBank, int oam){
        _tilePanel.setPaletteType(paletteBank, oam);
    }
    */
    
    public CHRMultiTilePanel getTilePanel(){
        return _tilePanel;
    }
    
    public void setRemoveable(boolean flag){
        _isRemovable = flag;
    }
    public boolean isRemoveable(){
        return _isRemovable;
    }
    public void setAdded(boolean flag){
        _isAdded = flag;
    }
    public boolean isAdded(){
        return _isAdded;
    }
    public void setMetaProperties(Properties props){
        _metaProperties = props;
    }
    
    public Properties getMetaProperties(){
        return _metaProperties;
    }
    
    public void notifyDisplayInterfaceUpdated(){
        _tilePanel.notifyDisplayInterfaceUpdated();
    }
    
    public CHRDisplayControls getControls() {
        return _tilePanel.getControls();
    }
    
}
