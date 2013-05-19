/*
 * StampMetaDataPanel.java
 *
 * Created on September 1, 2008, 8:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.metaEditor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import utilities.GUIUtilities;

/**
 *
 * @author abailey
 */
public class StampMetaDataPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8822217982326960813L;
	public final static String BOOLEAN_KEY = ".boolean";
    public final static String INT_KEY = ".int";
    
    private Properties _defaults = null;
    private Properties _settings = null;
    private JPanel _uiPanel = null;
    private HashMap<String,JComponent> _uiMap = null;
    
    /** Creates a new instance of StampMetaDataPanel */
    public StampMetaDataPanel() {
        super();
        setLayout(new BorderLayout());
        _uiPanel = new JPanel();
        _uiMap = new HashMap<String,JComponent>();
        add(_uiPanel, BorderLayout.CENTER);
    }
    
    // example:
    // "gameTools/metaEditor/bg.level.stamp.properties""
    // "gameTools/metaEditor/sprite.level.stamp.properties""
    public void loadDefaultBGProperties() {
        loadPropertiesFile("gameTools/metaEditor/bg.level.stamp.properties") ;
    }
    public void loadDefaultSpriteProperties() {
        loadPropertiesFile("gameTools/metaEditor/sprite.level.stamp.properties") ;
    }
    public void loadPropertiesFile(String propFileName) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream inStream = loader.getResourceAsStream(propFileName);
            _defaults = new Properties();
            _defaults.load(inStream);
            _settings = new Properties(_defaults);
        } catch(Exception e){
            e.printStackTrace();
        }
        rebuildUI();
    }
    public void loadSettingsFile(String propFileName) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream inStream = loader.getResourceAsStream(propFileName);
            _settings.load(inStream);
        } catch(Exception e){
            e.printStackTrace();
        }
        rebuildUI();
    }
    
    private JComponent[] constructUIComponentFromProperty(String pName){
        final String propName = pName;
        if(propName.indexOf(BOOLEAN_KEY) != -1){
            String prefix = propName.substring(0,propName.lastIndexOf(BOOLEAN_KEY));
            final JCheckBox cb = new JCheckBox(prefix, Boolean.parseBoolean(_settings.getProperty(propName)));
            cb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _settings.setProperty(propName, "" + cb.isSelected());
                }
            });
            JComponent ret[] = new JComponent[1];
            ret[0] = cb;
            return ret;
        }
        if(propName.indexOf(INT_KEY) != -1){
            String prefix = propName.substring(0,propName.lastIndexOf(INT_KEY));
            final JSpinner spinner = new JSpinner();
            Integer value = new Integer(0);
            Integer min = new Integer(0);
            Integer max = new Integer(3);
            Integer step = new Integer(1);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
            spinner.setModel(spinnerModel);
            
            spinner.addChangeListener( new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int val = ((Integer)spinner.getModel().getValue()).intValue();
                    _settings.setProperty(propName, "" + val);
                }
            });    
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(new JLabel(prefix), BorderLayout.WEST);
            panel.add(spinner, BorderLayout.CENTER);
            
            JComponent ret[] = new JComponent[2];
            ret[0] = panel;
            ret[1] = spinner;
            return ret;
        }
       
        System.err.println("Unsupported property:" + pName);
        return null;
    }
    
    private void rebuildUI(){
        _uiPanel.removeAll();
        _uiMap.clear();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        GUIUtilities.initializeGBC(gbc);
        _uiPanel.setLayout(gbl);
        // iterate over the properties, and update accordingly
        int row = 0;
        int yPos = 0;
        Enumeration<?> propEnum = _settings.propertyNames();
        while(propEnum.hasMoreElements()){
            String propertyName = (String)propEnum.nextElement();
            JComponent[] comp = constructUIComponentFromProperty(propertyName);
            _uiMap.put(propertyName, comp[comp.length-1]);
            GUIUtilities.addComponent(_uiPanel, comp[0], gbc,gbl,row*2,yPos);
            _uiPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, row*2+1, yPos));
            if(row == 1){
                yPos++;
                row = 0;
            } else {
                row = 1;
            }
        }
        _uiPanel.add(GUIUtilities.createFillerHeight(gbc, gbl, 2, yPos));
        revalidate();
    }
    
    public Properties getDefaultPropertiesCopy(){
        return new Properties(_defaults);
    }
    public Properties getPropertiesCopy(){
        return new Properties(_settings);
    }
    
    public void updateUIFields(Properties newProps){
        _settings = newProps;
        Iterator<String> keyItor = _uiMap.keySet().iterator();
        while(keyItor.hasNext()){
            String key = keyItor.next();
            if(key.indexOf(BOOLEAN_KEY) != -1){
                ((JCheckBox)_uiMap.get(key)).setSelected(Boolean.parseBoolean(_settings.getProperty(key)));
            }
            if(key.indexOf(INT_KEY) != -1){
                ((JSpinner)_uiMap.get(key)).getModel().setValue(new Integer(_settings.getProperty(key)));
            }
        }
    }
    
}
