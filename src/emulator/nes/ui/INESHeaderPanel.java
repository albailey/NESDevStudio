/*
 * INESjava
 *
 * Created on November 8, 2007, 5:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import utilities.GUIUtilities;
import emulator.nes.INESHeader;

/**
 *
 * @author abailey
 */
public class INESHeaderPanel extends JPanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 9000059094250281076L;

	public final static String INES_HEADER_TITLE = "INES Header Info";
    
    private JTextField _fileNameField = null;
    private JTextField _mapperNumField = null;
    private JTextField _mapperNameField = null;
    private JTextField _numPRGsField = null;
    private JTextField _numCHRsField = null;
    
    /** Creates a new instance of INESHeaderPanel */
    public INESHeaderPanel() {
        super();
        setupUI();
    }
    
    
    
    private void setupUI(){
        setBorder(new TitledBorder(INES_HEADER_TITLE));
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        GUIUtilities.initializeGBC(gbc);
        setLayout(gbl);
        
        int yPos = 0;
        add(GUIUtilities.createLabel("Filename", "Name of the currently loaded ROM",gbc, gbl, 0,yPos));
        _fileNameField = GUIUtilities.createTextField("","Name of the currently loaded ROM",16, false, gbc, gbl, 1,yPos);
        add(_fileNameField);
        add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
        
        add(GUIUtilities.createLabel("Mapper Number", "iNES mapper number",gbc, gbl, 0,yPos));
        _mapperNumField = GUIUtilities.createTextField("", "iNES mapper number",10, false, gbc, gbl, 1,yPos);
        add(_mapperNumField);
        add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
        
        add(GUIUtilities.createLabel("Mapper Name", "Mapper Name",gbc, gbl, 0,yPos));
        _mapperNameField = GUIUtilities.createTextField("", "Mapper Name",10, false, gbc, gbl, 1,yPos);
        add(_mapperNameField);
        add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
        
        add(GUIUtilities.createLabel("Num PRG Banks", "Number of 16K PRG banks",gbc, gbl, 0,yPos));
        _numPRGsField = GUIUtilities.createTextField("", "Number of 16K PRG banks",10, false, gbc, gbl, 1,yPos);
        add(_numPRGsField);
        add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
        
        add(GUIUtilities.createLabel("Num CHR Banks", "Number of 8K CHR banks",gbc, gbl, 0,yPos));
        _numCHRsField = GUIUtilities.createTextField("", "Number of 8K CHR banks",10, false, gbc, gbl, 1,yPos);
        add(_numCHRsField);
        add(GUIUtilities.createFillerWidth(gbc, gbl, 2, yPos));
        yPos++;
        add(GUIUtilities.createFillerHeight(gbc, gbl, 0, yPos));
        
    }
    
    
    public void setHeader(INESHeader header, String filename){
        File fPortion = new File(filename);
        String fPart = fPortion.getName();
        if(fPart != null) {
            _fileNameField.setText(fPart);
        } else {
            _fileNameField.setText(filename);
        }
        _fileNameField.setToolTipText(filename);
        _numPRGsField.setText("" + header._numPrgBanks);
        _numCHRsField.setText("" + header._numChrBanks);
        _mapperNumField.setText(""+ header._baseMapperInfo);
        _mapperNameField.setText(header._mapperName);
    }
    
    
}

