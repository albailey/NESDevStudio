/*
 * DisassemblerUIPanel.java
 *
 * Created on December 14, 2006, 3:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.romLoader.disassembler;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;

import utilities.GUIUtilities;

/**
 * Things to add:
 * View of the memory (zero page)
 * View of the entire rom contents
 * View of the code as its disassembled
 * Better view of the code as it proceeds (ability to customize what is shown)
 * @author abailey
 */
public class DisassemblerUIPanel extends JPanel implements DisassemblerUIInterface 
{
    
//    private int currentAction = DisassemblerUIInterface.UNDEFINED_DISASSEMBLER_ACTION;
//    private int defaultNextAction = DisassemblerUIInterface.UNDEFINED_DISASSEMBLER_ACTION;
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 8080254008978059542L;
	private int actionStopLocation = 0;
     private int currentAction = DisassemblerUIInterface.DISASSEMBLER_CONTINUE_ACTION;
     private int defaultNextAction = DisassemblerUIInterface.DISASSEMBLER_CONTINUE_ACTION;
     
     private DefaultListModel specialLabels;
     
    /** Creates a new instance of DisassemblerUIPanel */
    public DisassemblerUIPanel() {
         specialLabels = new DefaultListModel();
         setupUI();
//         specialLabels.addElement("Additional Addresses");
    }
    
    public int[] getSpecialAddresses(){
        int v[] = new int[specialLabels.size()];
        for(int i=0;i<v.length;i++){
            v[i] = ((Integer)specialLabels.get(i)).intValue();
        }
        return v;
    }
            
    // this REQUIRES a separate thread to query it...
    public int getNextUserAction(){
        while(currentAction == DisassemblerUIInterface.UNDEFINED_DISASSEMBLER_ACTION){
             try { Thread.sleep(100); } catch(Exception e) { e.printStackTrace(); }
             Thread.yield();
        }
        int tempAction = currentAction;
        currentAction = defaultNextAction;
        return tempAction;
    }
    
    public void pause(){
        currentAction = DisassemblerUIInterface.UNDEFINED_DISASSEMBLER_ACTION;
        defaultNextAction = DisassemblerUIInterface.UNDEFINED_DISASSEMBLER_ACTION;   
    }
    
    public int getSeekAddress(){
        return actionStopLocation;
    }
    
    private void setupUI(){
        setBorder(new TitledBorder("Disassembly Controls"));
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        
   

        add( 
                GUIUtilities.createButton("Step"
                , "Proceed to next step in the Disassembler"
                ,new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                         currentAction = DisassemblerUIInterface.DISASSEMBLER_CONTINUE_ACTION;
                         defaultNextAction = DisassemblerUIInterface.UNDEFINED_DISASSEMBLER_ACTION;
                    }
                }
                ,gbc, gbl, 0,0)
                );
                add( 
                GUIUtilities.createButton("Pause"
                , "Pause the Disassembler"
                ,new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        pause();
                    }
                }
                ,gbc, gbl, 1,0)
                );
        add( 
                GUIUtilities.createButton("Resume"
                , "Resume the Disassembler"
                ,new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentAction = DisassemblerUIInterface.DISASSEMBLER_CONTINUE_ACTION;
                        defaultNextAction = DisassemblerUIInterface.DISASSEMBLER_CONTINUE_ACTION;
                    }
                }
                ,gbc, gbl, 2,0)
                );
        add( 
                GUIUtilities.createButton("Abort"
                , "Abort the Disassembler"
                ,new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        currentAction = DisassemblerUIInterface.DISASSEMBLER_STOP_ACTION;
                        defaultNextAction = DisassemblerUIInterface.DISASSEMBLER_STOP_ACTION;
                    }
                }
                ,gbc, gbl, 3,0)
                );
       try {
            final MaskFormatter formatter = new MaskFormatter("0xHHHH");
            formatter.setPlaceholder("0x8000");
            final JFormattedTextField hexField = new JFormattedTextField(formatter);
            add( 
                GUIUtilities.createButton("Run To:"
                , "Stop the Disassembler at the following hex location"
                ,new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Integer intVal = Integer.decode(hexField.getText());
                            System.out.println( intVal );
                            actionStopLocation = intVal.intValue();
                            currentAction = DisassemblerUIInterface.DISASSEMBLER_SEEK_ACTION;
                            defaultNextAction = DisassemblerUIInterface.DISASSEMBLER_SEEK_ACTION;
                        } catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
                ,gbc, gbl, 4,0)
                );            
            GUIUtilities.addComponent(this,hexField, gbc, gbl, 5,0); 
            

        }catch (Exception e){
            e.printStackTrace();
        }

        add(GUIUtilities.createFillerWidth(gbc, gbl, 6,0));

        try {
            final MaskFormatter formatter = new MaskFormatter("0xHHHH");
            formatter.setPlaceholder("0x8000");
            final JFormattedTextField hexField = new JFormattedTextField(formatter);
            add( 
                GUIUtilities.createButton("Add Address to process:"
                , "Will start disassembling at this address in addition to the others."
                ,new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Integer intVal = Integer.decode(hexField.getText());
                            System.out.println( intVal );
                            specialLabels.addElement(intVal);
                        } catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
                ,gbc, gbl, 0,1)
                );            
            GUIUtilities.addComponent(this,hexField, gbc, gbl, 1,1); 
        }catch (Exception e){
            e.printStackTrace();
        }
         add( 
                GUIUtilities.createButton("Clear"
                , "Clears the Special Labels list"
                ,new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       specialLabels.clear();
                    }
                }
                ,gbc, gbl, 2,1)
                );
         {
            JList specialList = new JList(specialLabels);  
            JScrollPane scr = new JScrollPane(specialList);
            GUIUtilities.addComponent(this,scr, gbc, gbl, 3,1); 
         }
        add(GUIUtilities.createFillerWidth(gbc, gbl, 4,1));
        add(GUIUtilities.createFillerHeight(gbc, gbl, 0,2));
    }
}
