/*
 * RomLoader.java
 *
 * Created on November 14, 2006, 4:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.romLoader;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ui.romLoader.disassembler.DisassemblerUIPanel;
import utilities.GUIUtilities;
import emulator.nes.NESRom;
import emulator.nes.ui.CPU6502ModelPanel;

/**
 *
 * @author abailey
 */
   
public class RomLoader  extends JInternalFrame  {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4004227847683043370L;
	public final static int UTILITY_WIDTH = 256;
    public final static int UTILITY_HEIGHT = 240;
    
    public final static String FRAME_TITLE = "ROM Loader";
    
    private JTextField _fileNameField = null;
    private JTextField _mapperNumField = null;
    private JTextField _mapperNameField = null;    
    private JTextField _numPRGsField = null;
    private JTextField _numCHRsField = null;
   
    private NESRom _rom;  // controller
    private CPU6502ModelPanel _cpuView = null; // view
    private DisassemblerUIPanel _disassemblerUI = null;
    
    public RomLoader() {
        super(FRAME_TITLE, true, true, false, false);       
        _rom = new NESRom();
        setupUI();
    }
    
        

    // here is the plan:
    // load in an image from file.
    // Dispay the image at 256*240 pixels (NTSC)
    // Display the image again: using the limited 64 color NES palette
    // Allow the user to generate it
    private void setupUI(){
        getContentPane().setLayout(new GridLayout(3,1,2,2));

        _disassemblerUI = new DisassemblerUIPanel();
        _cpuView = new CPU6502ModelPanel();
        _rom.addCPU6502View(_cpuView);
        
        setupMenuBar();
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBorder(new TitledBorder("Header Info"));
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        GUIUtilities.initializeGBC(gbc);

        headerPanel.setLayout(gbl);
        
        int yPos = 0;
        headerPanel.add(GUIUtilities.createLabel("Filename", "Name of the currently loaded ROM",gbc, gbl, 0,yPos));
        _fileNameField = GUIUtilities.createTextField("","Name of the currently loaded ROM",10, false, gbc, gbl, 1,yPos);
        headerPanel.add(_fileNameField);
        headerPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
        
        headerPanel.add(GUIUtilities.createLabel("Mapper Number", "iNES mapper number",gbc, gbl, 0,yPos));
        _mapperNumField = GUIUtilities.createTextField("", "iNES mapper number",10, false, gbc, gbl, 1,yPos);
        headerPanel.add(_mapperNumField);
         headerPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;

        headerPanel.add(GUIUtilities.createLabel("Mapper Name", "Mapper Name",gbc, gbl, 0,yPos));
        _mapperNameField = GUIUtilities.createTextField("", "Mapper Name",10, false, gbc, gbl, 1,yPos);
        headerPanel.add(_mapperNameField);
         headerPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
        
        headerPanel.add(GUIUtilities.createLabel("Num PRG Banks", "Number of 16K PRG banks",gbc, gbl, 0,yPos));
        _numPRGsField = GUIUtilities.createTextField("", "Number of 16K PRG banks",10, false, gbc, gbl, 1,yPos);
        headerPanel.add(_numPRGsField);
         headerPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;

        headerPanel.add(GUIUtilities.createLabel("Num CHR Banks", "Number of 8K CHR banks",gbc, gbl, 0,yPos));
        _numCHRsField = GUIUtilities.createTextField("", "Number of 8K CHR banks",10, false, gbc, gbl, 1,yPos);
        headerPanel.add(_numCHRsField);
         headerPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
         headerPanel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, yPos));
        
        getContentPane().add(headerPanel);     
        getContentPane().add(_cpuView);   
        getContentPane().add(_disassemblerUI);   
        pack();        
        //Set the window's location.
        setLocation(0,0);                
    }
    
    
  private void updateUIFieldForROM(){
    _fileNameField.setText(_rom.getRomFileNameOnly());
    _numPRGsField.setText("" + _rom.getNumPRGBanks());
    _numCHRsField.setText("" + _rom.getNumCHRBanks());
    _mapperNumField.setText(""+ _rom.getBaseMapperInfo());
    _mapperNameField.setText(_rom.getMapperName());
  }
  
    private void setupMenuBar(){     
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        final Component comp = this;
        final DisassemblerUIPanel disUI = _disassemblerUI;
        
        // Setup File Menu
        JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');

        GUIUtilities.createMenuItem(fileMenu, "Load ROM", 'L', new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    _rom.loadROM(comp);
                    updateUIFieldForROM();
                }
            }
        );
        
        GUIUtilities.createMenuItem(fileMenu, "Save ROM", 'S', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
                _rom.saveROM(comp);
                 updateUIFieldForROM();
           }
        }
        );
        GUIUtilities.createMenuItem(fileMenu, "Save ROM As...", 'A', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {                
                _rom.saveAsROM(comp);
                 updateUIFieldForROM();
            }
        }
        );

        // Utils Menu
        
        JMenu utilsMenu = GUIUtilities.createMenu(menuBar, "Utils", 'U');

        GUIUtilities.createMenuItem(utilsMenu, "Split ROM", 'P', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {                
                _rom.splitROM(comp);
            }
        }
        );

        GUIUtilities.createMenuItem(utilsMenu, "Disassemble ROM", 'D', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {  
                _rom.disassemble(comp, disUI);
            }
        }
        );


        GUIUtilities.createMenuItem(utilsMenu, "View PRG Hex Dump", 'G', new ActionListener()  {
            public void actionPerformed(ActionEvent e) {                
                _rom.generatePRGHexDump(comp);
            }
        }
        );

        GUIUtilities.createMenuItem(utilsMenu, "View PRG Hex Diff", 'L', new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _rom.generatePRGHexDumpDiff(comp);
                
            }
        }
        );

    }

}
