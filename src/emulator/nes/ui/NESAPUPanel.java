/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utilities.GUIUtilities;
import emulator.nes.APU;

/**
 *
 * @author abailey
 */
public class NESAPUPanel extends JPanel implements APUListener  {

    // private APUWaveformGraph waveformGraph = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3005508006629394558L;
	
	private JCheckBox _ntscModeCB = null;
    private JCheckBox _fourStepModeCB = null;
    private JCheckBox _irqInhibitModeCB = null;
    
    private JCheckBox _frameIRQCB = null;
    private JCheckBox _dmcIRQCB = null;
    
    private JCheckBox _square1CB = null;
    private JCheckBox _square2CB = null;
    private JCheckBox _triCB = null;
    private JCheckBox _noiseCB = null;
    
    private JTextField _clocksField = null;
    private JTextField _clockCountField = null;
    private JTextField _sequencerField = null;
    private JTextField _internalStepField = null;
    
     /**
     * Creates a new instance of NESMemoryPanel
     */
    public NESAPUPanel(final APU apu) {
        super();
        setLayout(new BorderLayout());

  //      waveformGraph = new APUWaveformGraph(apu.getFormat(), apu.getLine());
  //      add(waveformGraph, BorderLayout.NORTH);
  //      apu.addAPUBufferListener(waveformGraph);

        add(setupRegisterPanel(), BorderLayout.NORTH);
        add(new JPanel(), BorderLayout.CENTER);
        apu.addAPUListener(this);
        JButton refreshButton  = new JButton("Refresh");
        add(refreshButton,  BorderLayout.SOUTH);
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	notifyUpdates(apu);
            }
        });
    }
    
    public void notifyUpdates(APU apu) {
    	
    	_ntscModeCB.setSelected(apu.isNTSCMode());

    	_fourStepModeCB.setSelected(apu.isFourStepMode());
    	_irqInhibitModeCB.setSelected(apu.isIRQInhibitMode());
    	
    	_frameIRQCB.setSelected(apu.getFrameIRQFlagDirect());
    	_dmcIRQCB.setSelected(apu.getDMCInterruptFlagDirect());
    	
    	_square1CB.setSelected(apu.hasSquare1Sound());
       	_square2CB.setSelected(apu.hasSquare2Sound());
       	_triCB.setSelected(apu.hasTriangleSound());
       	_noiseCB.setSelected(apu.hasNoiseSound());
		
       	_clocksField.setText("" + apu.getClocks());
       	_clockCountField.setText("" + apu.getClocks());
       	_sequencerField.setText("" + apu.getSequencerValue());
       	_internalStepField.setText("" + apu.getInternalStep());
       	
    }
    
    private JPanel setupRegisterPanel() {
    	JPanel panel = new JPanel();
        

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
      
        int x = 0;
        int y = 0;
        
        _ntscModeCB = GUIUtilities.createCheckBox("NTSC Mode?", "Checked means NTSC Mode. Unchecked means PAL mode", false, gbc, gbl, x, y);
        panel.add(_ntscModeCB);
        x++;
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, x, y));

        x=0;
        y++;
        _fourStepModeCB = GUIUtilities.createCheckBox("4 Step Mode", "4 Step Mode", false, gbc, gbl, x, y);
        panel.add(_fourStepModeCB);
        x++;

        _irqInhibitModeCB = GUIUtilities.createCheckBox("IRQ Inhibit", "IRQ Inhibit Mode", false, gbc, gbl, x, y);
        panel.add(_irqInhibitModeCB);
        x++;
 
        _frameIRQCB = GUIUtilities.createCheckBox("Frame IRQ", "Frame IRQ", false, gbc, gbl, x, y);
        panel.add(_frameIRQCB);
        x++;
        
        _dmcIRQCB = GUIUtilities.createCheckBox("DMC IRQ", "DMC IRQ", false, gbc, gbl, x, y);
        panel.add(_dmcIRQCB);
        x++;
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, x, y));

        
        x=0;
        y++;
        _square1CB = GUIUtilities.createCheckBox("Square 1", "Square 1 Enabled", false, gbc, gbl, x, y); 
        panel.add(_square1CB);
 
        x=0;
        y++;
        _square2CB = GUIUtilities.createCheckBox("Square 2", "Square 2 Enabled", false, gbc, gbl, x, y);    
        panel.add(_square2CB);

        x=0;
        y++;
        _triCB = GUIUtilities.createCheckBox("Triangle", "Triangle Enabled", false, gbc, gbl, x, y);
        panel.add(_triCB);

        x=0;
        y++;
        _noiseCB = GUIUtilities.createCheckBox("Noise", "Noise Enabled", false, gbc, gbl, x, y);
        panel.add(_noiseCB);


        x=0;
        y++;        
        JLabel clocksLabel = GUIUtilities.createLabel("Clocks", "Clocks", gbc, gbl, x, y);
        panel.add(clocksLabel);
        x++;
        _clocksField = GUIUtilities.createTextField("", "Clocks", 6, false, gbc, gbl, x, y);
        panel.add(_clocksField);

        x=0;
        y++;        
        JLabel clockCountLabel = GUIUtilities.createLabel("Clock Count", "Clock Count", gbc, gbl, x, y);
        panel.add(clockCountLabel);
        x++;
        _clockCountField = GUIUtilities.createTextField("", "Clock Count", 6, false, gbc, gbl, x, y);
        panel.add(_clockCountField);

        x=0;
        y++;        
        JLabel sequencerLabel = GUIUtilities.createLabel("Sequencer", "Sequencer", gbc, gbl, x, y);
        panel.add(sequencerLabel);
        x++;
        _sequencerField = GUIUtilities.createTextField("", "Sequencer", 6, false, gbc, gbl, x, y);
        panel.add(_sequencerField);

        x=0;
        y++;        
        JLabel internalLabel = GUIUtilities.createLabel("Internal Step", "Internal Step", gbc, gbl, x, y);
        panel.add(internalLabel);
        x++;
        _internalStepField = GUIUtilities.createTextField("", "Internal Step", 6, false, gbc, gbl, x, y);
        panel.add(_internalStepField);
        
        x++;
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, x, y));

        x=0;
        y++;
        panel.add(GUIUtilities.createFillerHeight(gbc, gbl, x, y));

        return panel;
    }
 
}
