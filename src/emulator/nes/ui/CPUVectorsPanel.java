/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import utilities.ByteFormatter;
import utilities.GUIUtilities;
import emulator.core.CPU6502.Architecture6502;
import emulator.core.CPU6502.Utilities6502;
import emulator.core.CPU6502.mvc.CPU6502MemoryModelInterface;

/**
 *
 * @author abailey
 */
public class CPUVectorsPanel extends JPanel implements CPU6502MemoryModelInterface {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1000011636249766168L;
	private JTextField _nmiField = null;
    private JTextField _resetField = null;
    private JTextField _irqField = null;
    private byte vectors[] = new byte[6];


    public CPUVectorsPanel() {
        super();
        setupUI();
    }


    public void setupUI() {
        setBorder(new TitledBorder("Vectors"));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.ipadx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nmiLabel = new JLabel("NMI:");
        gbl.setConstraints(nmiLabel, gbc);
        add(nmiLabel);

        gbc.gridx = 1;
        _nmiField = new JTextField(6);
        _nmiField.setEditable(false);
        gbl.setConstraints(_nmiField, gbc);
        add(_nmiField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel resetLabel = new JLabel("Reset:");
        gbl.setConstraints(resetLabel, gbc);
        add(resetLabel);

        gbc.gridx = 1;
        _resetField = new JTextField(6);
        _resetField.setEditable(false);
        gbl.setConstraints(_resetField, gbc);
        add(_resetField);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel irqLabel = new JLabel("IRQ:");
        gbl.setConstraints(irqLabel, gbc);
        add(irqLabel);

        gbc.gridx = 1;
        _irqField = new JTextField(6);
        _irqField.setEditable(false);

        gbl.setConstraints(_irqField, gbc);
        add(_irqField);

        add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 0));
        add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 3));

    }


    public void updateMemory(byte memory[]) {
        for (int i = Architecture6502.NMI_VECTOR_VALUE; i < Architecture6502.NMI_VECTOR_VALUE + 6; i++) {
            updateVectors(i, memory[i]);
        }
    }

    public void updateWriteMemory(int i, byte val) {
        updateVectors(i, val);
    }

    private void updateVectors(int i, byte val) {
        if (i >= Architecture6502.NMI_VECTOR_VALUE && i <= 0xFFFF) {
            vectors[i - Architecture6502.NMI_VECTOR_VALUE] = val;
            int nmiAddress = Utilities6502.calculate16BitAddress(vectors[0], vectors[1]);
            _nmiField.setText("0x" + ByteFormatter.formatInt(nmiAddress));
            int resetAddress = Utilities6502.calculate16BitAddress(vectors[2], vectors[3]);
            _resetField.setText("0x" + ByteFormatter.formatInt(resetAddress));
            int irqAddress = Utilities6502.calculate16BitAddress(vectors[4], vectors[5]);
            _irqField.setText("0x" + ByteFormatter.formatInt(irqAddress));
        }
    }
}
