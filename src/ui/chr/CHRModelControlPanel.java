/*
 * CHRModelControlPanel.java
 *
 * Created on October 9, 2006, 2:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import ui.chr.patternTable.PatternTablePanel;

/**
 *
 * @author abailey
 */
public class CHRModelControlPanel extends JPanel {
   
    /**
	 * 
	 */
	private static final long serialVersionUID = -3121593651824388740L;
	private ButtonGroup buttonGroup = null;
    private JPanel visiblePatternPanel = null;
    private JPanel radioPanel = null;
    private CardLayout cardLayout = null;
    private boolean isEmpty = true;
    /** Creates a new instance of CHRModelControlPanel */
    public CHRModelControlPanel() {       
        setupUI();
    }
    
    private void setupUI(){
        setBorder(new TitledBorder("Pattern Table (CHR)"));
        setLayout(new BorderLayout());
        buttonGroup  = new ButtonGroup();
        radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
       
        add(radioPanel,BorderLayout.NORTH);
        visiblePatternPanel = new JPanel();
        cardLayout = new CardLayout();
        visiblePatternPanel.setLayout(cardLayout);
       
        add(visiblePatternPanel,BorderLayout.CENTER);
    }
    
    public void addPatternTable(final PatternTablePanel patternPanel) {

        JRadioButton b1 = new JRadioButton(patternPanel.panelTitle);
             
        
        b1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()== ItemEvent.SELECTED){
                    cardLayout.show(visiblePatternPanel, patternPanel.panelTitle);
                    patternPanel.setActive(); 
                } else {
                    patternPanel.setInActive(); 
                }
            }
        }
        );
        radioPanel.add(b1);
        buttonGroup.add(b1);             
        visiblePatternPanel.add(patternPanel, patternPanel.panelTitle);
        if(isEmpty){
            isEmpty = false;
            buttonGroup.setSelected(b1.getModel(),true);         
        }
    }
    
}
