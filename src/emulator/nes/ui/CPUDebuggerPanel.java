/*
 * CPUDebuggerPanel.java
 *
 * Created on January 20, 2009, 8:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;

import utilities.GUIUtilities;
import emulator.nes.debugger.NESDebuggerInterface;

/**
 *
 * @author abailey
 */
public class CPUDebuggerPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7738767362699460918L;
	/** Creates a new instance of CPUDebuggerPanel */
    NESDebuggerInterface _debugger = null;

    public CPUDebuggerPanel(NESDebuggerInterface debugger) {
        super();
        _debugger = debugger;
        setupUI();
    }

    private void setupUI() {
        setBorder(new TitledBorder("Debugger"));
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        fl.setAlignOnBaseline(true);
        setLayout(fl);

        JPanel buttonPanel = constructButtonPanel();
        add(buttonPanel);

        JPanel ppuPanel = setupPPUDebuggerPanel();
        add(ppuPanel);

        JPanel breakpointPanel = constructBreakpointPanel();
        add(breakpointPanel);

        JPanel watchPanel = constructWatchPanel();
        add(watchPanel);

        JPanel loggingPanel = constructLoggingPanel();
        add(loggingPanel);
    }


    private JPanel constructButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("CPU"));
        //panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setLayout(new GridLayout(2,2));
        {
            String tooltip = "Pause";
            String imagePath = "emulator/nes/ui/pause.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _debugger.pause();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            panel.add(button);
        }

        {
            String tooltip = "Resume";
            String imagePath = "emulator/nes/ui/resume.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _debugger.resume();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            panel.add(button);
        }
        {
            String tooltip = "Step";
            String imagePath = "emulator/nes/ui/step.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _debugger.stepCPUInstruction();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            panel.add(button);
        }
        return panel;
    }

    private JPanel setupPPUDebuggerPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("PPU"));
     //   panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setLayout(new GridLayout(2,1));
        {
            String tooltip = "Frame Step";
            String imagePath = "emulator/nes/ui/frameStep.png";
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            JButton button = null;
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
           
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _debugger.stepNextFrame();
                }
            };
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            panel.add(button);
        }

        {
            String tooltip = "Line Step";
            String imagePath = "emulator/nes/ui/lineStep.png";
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            JButton button = null;
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _debugger.stepNextScanline();
                }
            };
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            panel.add(button);
        }
        return panel;
    }

    private JPanel constructWatchPanel() {
        JPanel panel = new JPanel();

        try {

            panel.setBorder(new TitledBorder("Watches"));

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            panel.setLayout(gbl);
            GUIUtilities.initializeGBC(gbc);
            gbc.anchor = GridBagConstraints.NORTHWEST;

            final MaskFormatter formatter = new MaskFormatter("0xHHHH");
            formatter.setPlaceholder("0x0000");
            final JFormattedTextField hexField = new JFormattedTextField(formatter);
            final JCheckBox readBox = new JCheckBox("Read");
            final JCheckBox writeBox = new JCheckBox("Write");

            final DefaultListModel listModel = new DefaultListModel();
            final JList watchList = new JList(listModel);
            watchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            watchList.setVisibleRowCount(4);
            JScrollPane scrPane = new JScrollPane(watchList);
            scrPane.setPreferredSize(new Dimension(80, 40));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbl.setConstraints(scrPane, gbc);
            panel.add(scrPane);

            gbc.gridheight = 1;
            JLabel label = new JLabel("Address:");
            gbc.gridx = 1;
            gbl.setConstraints(label, gbc);
            panel.add(label);

            gbc.gridx = 2;
            gbl.setConstraints(hexField, gbc);
            panel.add(hexField);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbl.setConstraints(readBox, gbc);
            panel.add(readBox);

            gbc.gridx = 2;
            gbl.setConstraints(writeBox, gbc);
            panel.add(writeBox);


            {
                String title = "Add";
                String tooltip = "Add Watch";
                ActionListener actListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String hexVal = hexField.getText();
                        Integer intVal = Integer.decode(hexVal);
                        _debugger.addWatch(intVal.intValue(), readBox.isSelected(), writeBox.isSelected());
                        if (!listModel.contains(hexVal)) {
                            listModel.addElement(hexVal);
                        }
                    }
                };
                JButton button = new JButton(title);
                button.setToolTipText(tooltip);
                button.addActionListener(actListener);
                gbc.gridx = 1;
                gbc.gridy = 2;
                gbl.setConstraints(button, gbc);
                panel.add(button);
            }
            {
                String title = "Remove";
                String tooltip = "Remove Watch";
                ActionListener actListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String hexVal = hexField.getText();
                        Integer intVal = Integer.decode(hexVal);
                        _debugger.removeWatch(intVal.intValue());
                        if (listModel.contains(hexVal)) {
                            listModel.removeElement(hexVal);
                        }
                    }
                };
                JButton button = new JButton(title);
                button.setToolTipText(tooltip);
                button.addActionListener(actListener);
                gbc.gridx = 0;
                 gbc.gridwidth = 1;
                gbc.gridy = 2;
                gbl.setConstraints(button, gbc);
                panel.add(button);
            }





        } catch (Exception e) {
            e.printStackTrace();
        }
        return panel;
    }

    private JPanel constructBreakpointPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new TitledBorder("Breakpoints"));
        mainPanel.setLayout(new GridLayout(1,2));
        try {

            JPanel panel = new JPanel();
            mainPanel.add(panel);
            panel.setBorder(new TitledBorder("Program Counter"));
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            panel.setLayout(gbl);
            GUIUtilities.initializeGBC(gbc);
            gbc.anchor = GridBagConstraints.NORTHWEST;

            final DefaultListModel listModel = new DefaultListModel();
            final JList bpList = new JList(listModel);
            bpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            bpList.setVisibleRowCount(4);
            JScrollPane scrPane = new JScrollPane(bpList);
            scrPane.setPreferredSize(new Dimension(80, 40));

            final MaskFormatter formatter = new MaskFormatter("0xHHHH");
            formatter.setPlaceholder("0x8000");
            final JFormattedTextField hexField = new JFormattedTextField(formatter);
            hexField.setBorder(new BevelBorder(BevelBorder.LOWERED));
            {
                String title = "Add";
                String tooltip = "Add CPU Breakpoint";
                ActionListener actListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String hexVal = hexField.getText();
                        Integer intVal = Integer.decode(hexVal);
                        _debugger.addCPUBreakpoint(intVal.intValue());
                        if (!listModel.contains(hexVal)) {
                            listModel.addElement(hexVal);
                        }
                    }
                };
                JButton button = new JButton(title);
                button.setToolTipText(tooltip);
                button.addActionListener(actListener);
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.gridx = 1;
                gbc.gridy = 2;
                gbl.setConstraints(button, gbc);
                panel.add(button);
            }
            {
                String title = "Remove";
                String tooltip = "Remove CPU Breakpoint";
                ActionListener actListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String hexVal = hexField.getText();
                        Integer intVal = Integer.decode(hexVal);
                        _debugger.removeCPUBreakpoint(intVal.intValue());
                        if (listModel.contains(hexVal)) {
                            listModel.removeElement(hexVal);
                        }
                    }
                };
                JButton button = new JButton(title);
                button.setToolTipText(tooltip);
                button.addActionListener(actListener);
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbl.setConstraints(button, gbc);
                panel.add(button);
            }

            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridx = 1;
            gbc.gridy = 0;
            JLabel label = new JLabel("Address:");
            gbl.setConstraints(label, gbc);
            panel.add(label);

            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbl.setConstraints(hexField, gbc);
            panel.add(hexField);


            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbl.setConstraints(scrPane, gbc);
            panel.add(scrPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Opcode breakpoints
        try {
        	JPanel panel = new JPanel();
            mainPanel.add(panel);
            panel.setBorder(new TitledBorder("Opcode"));
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            panel.setLayout(gbl);
            GUIUtilities.initializeGBC(gbc);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            
            final DefaultListModel listModel = new DefaultListModel();
            final JList bpList = new JList(listModel);
            bpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            bpList.setVisibleRowCount(4);
            JScrollPane scrPane = new JScrollPane(bpList);
            scrPane.setPreferredSize(new Dimension(80, 40));

            final MaskFormatter formatter = new MaskFormatter("0xHH");
            formatter.setPlaceholder("0x00");
            final JFormattedTextField hexField = new JFormattedTextField(formatter);
            hexField.setBorder(new BevelBorder(BevelBorder.LOWERED));
            {
                String title = "Add";
                String tooltip = "Add Opcode Breakpoint";
                ActionListener actListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String hexVal = hexField.getText();
                        Integer intVal = Integer.decode(hexVal);
                        _debugger.addOpcodeBreakpoint(intVal.byteValue());
                        if (!listModel.contains(hexVal)) {
                            listModel.addElement(hexVal);
                        }
                    }
                };
                JButton button = new JButton(title);
                button.setToolTipText(tooltip);
                button.addActionListener(actListener);
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.gridx = 1;
                gbc.gridy = 2;
                gbl.setConstraints(button, gbc);
                panel.add(button);
            }
            {
                String title = "Remove";
                String tooltip = "Remove Opcode Breakpoint";
                ActionListener actListener = new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String hexVal = hexField.getText();
                        Integer intVal = Integer.decode(hexVal);
                        _debugger.removeOpcodeBreakpoint(intVal.byteValue());
                        if (listModel.contains(hexVal)) {
                            listModel.removeElement(hexVal);
                        }
                    }
                };
                JButton button = new JButton(title);
                button.setToolTipText(tooltip);
                button.addActionListener(actListener);
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbl.setConstraints(button, gbc);
                panel.add(button);
            }

            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridx = 1;
            gbc.gridy = 0;
            JLabel label = new JLabel("Opcode:");
            gbl.setConstraints(label, gbc);
            panel.add(label);

            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbl.setConstraints(hexField, gbc);
            panel.add(hexField);


            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 2;
            gbc.fill = GridBagConstraints.BOTH;
            gbl.setConstraints(scrPane, gbc);
            panel.add(scrPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainPanel;
    }
    private JPanel constructLoggingPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Logging"));
//        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setLayout(new GridLayout(2,1));
        {
            String tooltip = "Start Logging";
            String imagePath = "emulator/nes/ui/capture.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _debugger.startCPUCapture();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            panel.add(button);
        }
        // Should add icon buttons for frequent actions like reset, etc..
        {
            String tooltip = "Stop Logging";
            String imagePath = "emulator/nes/ui/capture.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _debugger.endCPUCapture();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            panel.add(button);
        }
        return panel;
    }

}
