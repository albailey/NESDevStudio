/*
 * CPU6502ModelPanel.java
 *
 * Created on December 6, 2006, 1:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import utilities.ByteFormatter;
import utilities.GUIUtilities;
import emulator.core.CPU6502.CPU6502;
import emulator.core.CPU6502.Instruction6502;
import emulator.core.CPU6502.mvc.CPU6502ViewInterface;
import emulator.nes.INESHeader;
import emulator.nes.NES;
import emulator.nes.PPU;

/**
 *
 * @author abailey
 */
public class CPU6502ModelPanel extends JPanel implements CPU6502ViewInterface, Runnable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -1611709511709413817L;
	private boolean headerMode = false;    
    private boolean intMode = false;
    private boolean hexMode = true;
    
    // registers
    private JTextField _accumulatorField = null;
    private JTextField _xRegisterField = null;
    private JTextField _yRegisterField = null;
    

    
    // flags
    private JCheckBox _negFlagCB = null;
    private JCheckBox _zeroFlagCB = null;
    private JCheckBox _overflowFlagCB = null;
    private JCheckBox _carryFlagCB = null;
    private JCheckBox _interruptFlagCB = null;
    private JCheckBox _breakFlagCB = null;
    private JCheckBox _decimalFlagCB = null;
    
    // state fields
    private JTextField _programCounterField = null;
    private JTextField _nextInstructionField = null;
    private JTextField _operandField = null;
    private JTextField _stackPointerField = null;
    private JTextField _cpuCycleField = null;
    
    private INESHeaderPanel headerPanel = null;
    private CPUVectorsPanel vectorsPanel = null;

    private CPU6502 model = null;
	private boolean _running = false;
	private Thread _thread = null;
    /**
     * Creates a new instance of CPU6502ModelPanel
     */
    public CPU6502ModelPanel() {
    	model = null;
    	_running = false;
    	_thread = null;
        setupUI();
    }
    public void startRunning() {
		if(_running){
			return;
		}
		_running = true;
		_thread = new Thread(this);
		_thread.start();		
	}
	public void stopRunning() {
		_running = false;
		model = null;
		if(_thread != null) {
			try { _thread.join(); } catch(Exception e){ e.printStackTrace(); }
			_thread = null;
		}
	}
    private void setupUI(){
        setBorder(new TitledBorder("6502 CPU"));
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        headerPanel = new INESHeaderPanel();
        gbl.setConstraints(headerPanel, gbc);
        add(headerPanel);

        JPanel statePanel = setupStatePanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbl.setConstraints(statePanel, gbc);
        add(statePanel);


        // a special controls mode panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        JPanel registersPanel = setupRegistersPanel();
        gbl.setConstraints(registersPanel, gbc);
        add(registersPanel);
        
        vectorsPanel = new CPUVectorsPanel();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(vectorsPanel, gbc);
        add(vectorsPanel);

        JPanel flagsPanel = setupFlagsPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbl.setConstraints(flagsPanel, gbc);
        add(flagsPanel);
        



        add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 0));
        add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 3));
        
    }
    public void visitRegistration(NES nes) {
        nes.addMemoryModelListener(vectorsPanel);
    }

    
     public void setHeader(INESHeader header, String filename){
        headerPanel.setHeader(header, filename);
     }

    private JPanel setupRegistersPanel(){
        JPanel regPanel = new JPanel();
        regPanel.setBorder(new TitledBorder("Registers"));
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        regPanel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 0;

        gbc.weightx = 0;
        regPanel.add(GUIUtilities.createLabel("A", "Accumulator",gbc, gbl, 0,0));
        _accumulatorField = GUIUtilities.createTextField("","Accumulator",10, false, gbc, gbl, 1,0);
        regPanel.add(_accumulatorField);
        regPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2,0));

        gbc.weightx = 0;
        regPanel.add(GUIUtilities.createLabel("X", "X Register",gbc, gbl, 0,1));
        _xRegisterField = GUIUtilities.createTextField("","X Register",10, false, gbc, gbl, 1,1);
        regPanel.add(_xRegisterField);
        regPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2,1));

        gbc.weightx = 0;
        regPanel.add(GUIUtilities.createLabel("Y", "Y Register",gbc, gbl, 0,2));
        _yRegisterField = GUIUtilities.createTextField("","Y Register",10, false, gbc, gbl, 1,2);
        regPanel.add(_yRegisterField);
        regPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2,2));
        
        gbc.weightx = 0.1;
        gbc.weighty = 1;
        gbc.gridwidth = 3;
        regPanel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0,3));
        return regPanel;
    }
    
    private JPanel setupFlagsPanel(){
        JPanel flagsPanel = new JPanel();
        flagsPanel.setBorder(new TitledBorder("Flags"));
        flagsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        _negFlagCB = GUIUtilities.createCheckBox("N", "Negative", false, null, null, 0, 0);
        flagsPanel.add(_negFlagCB);
        
        _zeroFlagCB = GUIUtilities.createCheckBox("Z", "Zero", false, null, null, 1, 0);
        flagsPanel.add(_zeroFlagCB);
        
        _overflowFlagCB = GUIUtilities.createCheckBox("V", "Overflow", false, null, null, 2, 0);
        flagsPanel.add(_overflowFlagCB);
        
        _carryFlagCB = GUIUtilities.createCheckBox("C", "Carry", false, null, null, 3, 0);
        flagsPanel.add(_carryFlagCB);        
        
        _interruptFlagCB = GUIUtilities.createCheckBox("I", "Interrupt", false, null, null, 0, 1);
        flagsPanel.add(_interruptFlagCB);
        
        _breakFlagCB = GUIUtilities.createCheckBox("B", "Break", false, null, null, 1, 1);
        flagsPanel.add(_breakFlagCB);
        
        _decimalFlagCB = GUIUtilities.createCheckBox("D", "Decimal", false, null, null, 2, 1);
        flagsPanel.add(_decimalFlagCB);
        
        return flagsPanel;
    }
    private JPanel setupStatePanel(){
        JPanel statePanel = new JPanel();
        statePanel.setBorder(new TitledBorder("State"));
        
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        statePanel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        statePanel.add(GUIUtilities.createLabel("Program Counter:", "Address of Program Counter",gbc, gbl, 0,0));
        _programCounterField = GUIUtilities.createTextField("","Address of Program Counter",5, false, gbc, gbl, 1,0);
        statePanel.add(_programCounterField);
        
        statePanel.add(GUIUtilities.createLabel("Instruction:", "Next Instruction to be executed",gbc, gbl, 0,1));
        _nextInstructionField = GUIUtilities.createTextField("","Next Instruction to be executed",14, false, gbc, gbl, 1,1);
        statePanel.add(_nextInstructionField);
        
        statePanel.add(GUIUtilities.createLabel("Operand:", "Value (if any) for operand of next instruction to be executed",gbc, gbl, 0,2));
        _operandField = GUIUtilities.createTextField("","Value (if any) for operand of next instruction to be executed",8, false, gbc, gbl, 1,2);
        statePanel.add(_operandField);
        
        statePanel.add(GUIUtilities.createLabel("Stack Pointer:", "Pointer to current top of stack (empty at $FF)",gbc, gbl, 0,3));
        _stackPointerField = GUIUtilities.createTextField("","Pointer to current top of stack",3, false, gbc, gbl, 1,3);
        statePanel.add(_stackPointerField);

        statePanel.add(GUIUtilities.createLabel("CPU Cycle:", "The current CPU cycle (gets reset each NMI).",gbc, gbl, 0,4));
        _cpuCycleField = GUIUtilities.createTextField("","The current CPU cycle (gets reset each NMI).",6, false, gbc, gbl, 1,4);
        statePanel.add(_cpuCycleField);
        

        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        statePanel.add(GUIUtilities.createFillerHeight(gbc,gbl,0,5));
        
        
        return statePanel;
    }
    
    private String formatMultiViewableByte(byte val){
        String prefix = "";
        if(intMode){
            int i = val & 0xFF;
            if(headerMode)
                prefix = prefix + "Int:";
            if(i < 10){
                prefix = prefix + "0";
            }
            prefix = prefix + i + " ";
        }
        if(hexMode){
            if(headerMode)
                prefix = prefix + "Hex:";
            prefix = prefix + "0x" + ByteFormatter.formatByte(val);
        }
        return prefix;
    }
    
    public void refreshFromCPU(CPU6502 refreshModel){
    	model = refreshModel;
    }
    public void run() {
    	while(_running){
    		if(model != null) {
    	        _accumulatorField.setText(formatMultiViewableByte(model.getAccumulator()));
    	        _xRegisterField.setText(formatMultiViewableByte(model.getXRegister()));
    	        _yRegisterField.setText(formatMultiViewableByte(model.getYRegister()));
    	    
    	        _negFlagCB.setSelected(model.getNegativeFlag());
    	        _zeroFlagCB.setSelected(model.getZeroFlag());
    	        _overflowFlagCB.setSelected(model.getOverflowFlag());
    	        _carryFlagCB.setSelected(model.getCarryFlag());
    	        _interruptFlagCB.setSelected(model.getInterruptFlag());
    	        _breakFlagCB.setSelected(model.getBreakFlag());
    	        _decimalFlagCB.setSelected(model.getDecimalFlag());
    	        
    	        _programCounterField.setText(ByteFormatter.formatInt(model.getProgramCounter() & 0xFFFF));
    	        Instruction6502 inst = model.getInstructionAtProcessCounter();
    	        if(inst == null){
    	            _nextInstructionField.setText("null");
    	            _operandField.setText("null");            
    	        } else {
    	            _nextInstructionField.setText(inst.toString());
    	            _operandField.setText(inst.getOperand());
    	        }
    	        _stackPointerField.setText(formatMultiViewableByte(model.getStackPointer()));
    	        _cpuCycleField.setText(""+model.getClock());
    	        model = null;
    	        repaint();
    		}
    		try { Thread.sleep(200); } catch(Exception e){ e.printStackTrace(); }
    		
    		
    	}
       
    }
}
