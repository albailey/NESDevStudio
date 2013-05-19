/*
 * GUIUtilities.java
 *
 * Created on October 5, 2006, 11:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package utilities;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *
 * @author abailey
 */
public class GUIUtilities {
    
    /** Creates a new instance of GUIUtilities */
    private GUIUtilities() {
    }
  
    public static void initializeGBC(GridBagConstraints gbc){
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 1;
        gbc.ipadx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        gbc.insets.bottom = 1;
        gbc.insets.top = 1;
        gbc.insets.left = 1;
        gbc.insets.right = 1; 

    }
    // creates and adds a new Menu (and returns it for reference elsewhere)
    public static JMenu createMenu(JMenuBar menuBar, String menuTitle, char mnemonic){
        JMenu menu = new JMenu(menuTitle);
        if(mnemonic != 0){
            menu.setMnemonic(mnemonic);
        }
        menuBar.add(menu);
        return menu;
    }
    
    public static JMenu createSubMenu(JMenu parentMenu, String menuTitle, char mnemonic){
        JMenu menu = new JMenu(menuTitle);
        if(mnemonic != 0){
            menu.setMnemonic(mnemonic);
        }
        parentMenu.add(menu);
        return menu;
    }
    
    public static void reuseMenuItem(JMenu menu, JMenuItem menuItem, char mnemonic, ActionListener actListener){
        if(mnemonic != 0){
            menuItem.setMnemonic(mnemonic);
        }
        menuItem.addActionListener(actListener);
        menu.add(menuItem);
    }
    public static void createMenuItem(JMenu menu, String menuItemTitle, char mnemonic, ActionListener actListener){
        JMenuItem menuItem = new JMenuItem(menuItemTitle);
        if(mnemonic != 0){
            menuItem.setMnemonic(mnemonic);
        }
        menuItem.addActionListener(actListener);
        menu.add(menuItem);
/*        if(index == -1) {
            menu.add(menuItem);
        } else {
            menu.insert(menuItem, index);
        }
*/
    }
    public static void createCheckboxMenuItem(JMenu menu, String menuItemTitle, char mnemonic, boolean isSelected, ItemListener itemListener){
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(menuItemTitle);
        if(mnemonic != 0){
            menuItem.setMnemonic(mnemonic);
        }
        menuItem.setSelected(isSelected);
        menuItem.addItemListener(itemListener);
        menu.add(menuItem);
    }
    public static void createRadioMenuItem(JMenu menu, String menuItemTitle, char mnemonic, ButtonGroup buttonGroup, ItemListener itemListener){
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(menuItemTitle);
        if(mnemonic != 0){
            menuItem.setMnemonic(mnemonic);
        }
        buttonGroup.add(menuItem);
        if(buttonGroup.getButtonCount() == 1){ // first item
            buttonGroup.setSelected(menuItem.getModel(),true);
        }
        menuItem.addItemListener(itemListener);            
        menu.add(menuItem);
    }
    
    public static JLabel createLabel(String title, String tooltip, GridBagConstraints gbc, GridBagLayout gbl, int x, int y){
        JLabel theComponent = new JLabel(title);
        if(tooltip != null) {
            theComponent.setToolTipText(tooltip);
        }
	if(gbc != null && gbl != null) {
            gbc.gridx = x;
            gbc.gridy = y;
            gbl.setConstraints(theComponent, gbc); 
        }
        return theComponent;
    }
    
    public static void addComponent(JComponent parentComp, JComponent childComp, GridBagConstraints gbc, GridBagLayout gbl, int x, int y){
        if(gbc != null && gbl != null) {
            gbc.gridx = x;
            gbc.gridy = y;
            gbl.setConstraints(childComp, gbc); 
        }
        parentComp.add(childComp);
    }    
    
    public static JButton createButton(String title, String tooltip,  ActionListener actionListener, GridBagConstraints gbc, GridBagLayout gbl, int x, int y){
        JButton theComponent = new JButton(title);
        if(tooltip != null) {
            theComponent.setToolTipText(tooltip);
        }
        if(actionListener != null){
            theComponent.addActionListener(actionListener);
        }
        if(gbc != null && gbl != null) {
            gbc.gridx = x;
            gbc.gridy = y;
            gbl.setConstraints(theComponent, gbc); 
        }
        return theComponent;
    }    
    public static JTextField createTextField(String initialValue, String tooltip, int numColumns, boolean editable, GridBagConstraints gbc, GridBagLayout gbl, int x, int y){
        JTextField theComponent = new JTextField(initialValue,numColumns);
        if(tooltip != null) {
            theComponent.setToolTipText(tooltip);
        }
        theComponent.setEditable(editable);
        if(gbc != null && gbl != null) {
            gbc.gridx = x;
            gbc.gridy = y;
            gbl.setConstraints(theComponent, gbc); 
        }
        return theComponent;
    }

    public static JCheckBox createCheckBox(String title, String tooltip, boolean editable, GridBagConstraints gbc, GridBagLayout gbl, int x, int y){
        JCheckBox theComponent = new JCheckBox(title);
        theComponent.setHorizontalTextPosition(SwingConstants.LEFT);
        theComponent.setMargin(new Insets(1,1,1,1));
        theComponent.setFocusPainted(false);
        theComponent.setEnabled(editable);
        //theCB.setBorderPainted(true);
        if(tooltip != null) {
            theComponent.setToolTipText(tooltip);
        }
        if(gbc != null && gbl != null) {
            gbc.gridx = x;
            gbc.gridy = y;
            gbl.setConstraints(theComponent, gbc); 
        }
        return theComponent;
    }
     public static JPanel createFillerWidth(GridBagConstraints gbc, GridBagLayout gbl, int x, int y){
        JPanel thePanel = new JPanel();
         if(gbc != null && gbl != null){
            double oldWeightX = gbc.weightx;
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.weightx = 1.0;
            gbl.setConstraints(thePanel, gbc); 
            gbc.weightx = oldWeightX;            
        }
        return thePanel;
     }
     public static JPanel createFillerHeight(GridBagConstraints gbc, GridBagLayout gbl, int x, int y){
        JPanel thePanel = new JPanel();
         if(gbc != null && gbl != null){
            double oldWeightY = gbc.weighty;
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.weighty = 1.0;
            gbl.setConstraints(thePanel, gbc); 
            gbc.weighty = oldWeightY;            
        }
        return thePanel;
     }
     
    public static ImageIcon createImageIcon(String path, String description) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        java.net.URL imgURL = loader.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    
        public static JCheckBox addCustomCheckBox(final Object thisObj, GridBagConstraints gbc, GridBagLayout gbl, JPanel panel, int yPos, String title, String tooltip, String propX, boolean defaultProp, Method callbackMethod){
            final JCheckBox checkBox = new JCheckBox(title);
            checkBox.setToolTipText(tooltip);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbl.setConstraints(checkBox,gbc);
            final String prop = propX;
            final Method cbMethod = callbackMethod;
            panel.add(checkBox);
            boolean flag = EnvironmentUtilities.getBooleanEnvSetting(prop, defaultProp );
            checkBox.setSelected(flag);
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean boxVal = checkBox.isSelected();
                    EnvironmentUtilities.updateBooleanEnvSetting(prop, boxVal );
                    Object cbArgs[] = { new Boolean(boxVal)};
                    try {
                        cbMethod.invoke(thisObj, cbArgs);
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
            return checkBox; // already added
 
    }
    public static JSpinner addCustomSpinner(final Object thisObj, GridBagConstraints gbc, GridBagLayout gbl, JPanel panel, int yPos, String title, String tooltip, int initialVal, int minInt, int maxInt, int stepInt, Method callbackMethod){
    	return addCustomSpinner(thisObj, gbc, gbl, panel, true, yPos, title, tooltip, initialVal, minInt, maxInt, stepInt, callbackMethod);
    }
    public static JSpinner addCustomSpinner(final Object thisObj, GridBagConstraints gbc, GridBagLayout gbl, JPanel panel, boolean isVisible, int yPos, String title, String tooltip, int initialVal, int minInt, int maxInt, int stepInt, Method callbackMethod){
        JLabel label = new JLabel(title);
        label.setToolTipText(tooltip);
        gbc.gridx = 0;
        gbc.gridy = yPos;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.setConstraints(label,gbc);
        panel.add(label);
        
        label.setEnabled(isVisible);        
        // label.setVisible(isVisible);
        
        final JSpinner spinner = new JSpinner();
        final Method cbMethod = callbackMethod;
        
        Integer value = new Integer(initialVal);
        Integer min = new Integer(minInt);
        Integer max = new Integer(maxInt);
        Integer step = new Integer(stepInt);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
        spinner.setModel(spinnerModel);
        spinner.setToolTipText(tooltip);
        
        gbc.gridx = 1;
        gbc.gridy = yPos;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbl.setConstraints(spinner,gbc);
        panel.add(spinner);
        
        spinner.setEnabled(isVisible);
        //spinner.setVisible(isVisible);
        
        spinner.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int newVal = ((Integer)spinner.getModel().getValue()).intValue();
                Object cbArgs[] = { new Integer(newVal)};
                try {
                    cbMethod.invoke(thisObj, cbArgs);
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        return spinner; // already added
    }

}
