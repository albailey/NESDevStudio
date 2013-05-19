/*
 * GameGenieCodeHelper.java
 *
 * Created on June 20, 2007, 10:49 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.utils.gameGenie;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;

/**
 *
 A = 0000
P = 0001
Z = 0010
L = 0011
G = 0100
I = 0101
T = 0110
Y = 0111
E = 1000
O = 1001
X = 1010
U = 1011
K = 1100
S = 1101
V = 1110
N = 1111
 *
 * 6 digit
Char # |   1   |   2   |   3   |   4   |   5   |   6   |
Bit  # |3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|
maps to|1|6|7|8|H|2|3|4|-|I|J|K|L|A|B|C|D|M|N|O|5|E|F|G|

note char 3 bit 3 is used by the game genie to determine the length
of the code.

The value is made of 12345678 of the maps to line.
The address is made of ABCDEFGHIJKLMNO of the maps to line.

 * 8 digit
Char # |   1   |   2   |   3   |   4   |   5   |   6   |   7   |   8   |
Bit  # |3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|
maps to|1|6|7|8|H|2|3|4|-|I|J|K|L|A|B|C|D|M|N|O|%|E|F|G|!|^|&|*|5|@|#|$|

Once again char 3 bit 3 denotes the code length.

Once again:
The value is made of 12345678 of the maps to line.
The address is made of ABCDEFGHIJKLMNO of the maps to line.
The compare value is made of !@#$%^&* of the maps to line.

It then checks the value to be replaced with the compare
value, if they are the same it replaces the original value with the new
value if not the value remains the same.

 * @author abailey
 */
public class GameGenieCodeHelper extends JInternalFrame{
  
    /**
	 * 
	 */
	private static final long serialVersionUID = -2063450819443233120L;

	public final static String FRAME_TITLE = "Game Genie Code Helper";

    private char CODE_TABLE[] = {
        'A','P','Z','L','G','I','T','Y',
        'E','O','X','U','K','S','V','N'
    };

    /** Creates a new instance of GameGenieCodeHelper */
    public GameGenieCodeHelper() {
        super(FRAME_TITLE, true, true, false, false);       
        setupUI();        
    }
    
    private String updateCode6Field(String addressText, String valText){
    /*
     * 6 digit
    Char # |   1   |   2   |   3   |   4   |   5   |   6   |
    Bit  # |3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|
    maps to|1|6|7|8|H|2|3|4|-|I|J|K|L|A|B|C|D|M|N|O|5|E|F|G|

    note char 3 bit 3 is used by the game genie to determine the length
    of the code.

    The value is made of 1234 5678 of the maps to line. (val_b1 val_b2)
    The address is made of _ABC DEFG HIJK LMNO of the maps to line.        
     */
        
        int addrVal = (Integer.parseInt(addressText,16) & 0xFFFF);
        // if we assume the address is 15 bits, we will ignore the top bit
        int lowAddr = addrVal & 0xFF;
        int addr_b4 = (lowAddr & 0xF);
        int addr_b3 = ((lowAddr >> 4) & 0xF);
        int highAddr = (addrVal >> 8)& 0xFF;
        int addr_b2 = (highAddr & 0xF);
        int addr_b1 = ((highAddr >> 4) & 0xF);
        
        int val = (Integer.parseInt(valText,16) & 0xFF);
        int val_b2 = (val & 0xF);
        int val_b1 = ((val >> 4) & 0xF);
        

        // 1678
        byte char1 = (byte)(((val_b1 & 0x08) | (val_b2 & 0x04) | (val_b2 & 0x02) | (val_b2 & 0x01)) & 0xF);
        // H234
        byte char2 = (byte)(((addr_b3 & 0x08) | (val_b1 & 0x04) | (val_b1 & 0x02) | (val_b1 & 0x01))&0xF);
        // -IJK (- is always zero)
        byte char3 = (byte)(((0x00) | (addr_b3 & 0x04) | (addr_b3 & 0x02) | (addr_b3 & 0x01))&0xF);
        // LABC 
        byte char4 = (byte)(((addr_b4 & 0x08) | (addr_b1 & 0x04) | (addr_b1 & 0x02) | (addr_b1 & 0x01))&0xF);
        // DMNO 
        byte char5 = (byte)(((addr_b2 & 0x08) | (addr_b4 & 0x04) | (addr_b4 & 0x02) | (addr_b4 & 0x01))&0xF);
        // 5EFG 
        byte char6 = (byte)(((val_b2 & 0x08) | (addr_b2 & 0x04) | (addr_b2 & 0x02) | (addr_b2 & 0x01))&0xF);
        
        StringBuffer sb = new StringBuffer("");
        sb.append(CODE_TABLE[char1]);
        sb.append(CODE_TABLE[char2]);
        sb.append(CODE_TABLE[char3]);
        sb.append(CODE_TABLE[char4]);
        sb.append(CODE_TABLE[char5]);
        sb.append(CODE_TABLE[char6]);
        return sb.toString();
    }
    
    private String updateCode8Field(String addressText, String valText, String compareText){
 /* 8 digit
Char # |   1   |   2   |   3   |   4   |   5   |   6   |   7   |   8   |
Bit  # |3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|3|2|1|0|
maps to|1|6|7|8|H|2|3|4|-|I|J|K|L|A|B|C|D|M|N|O|%|E|F|G|!|^|&|*|5|@|#|$|

Once again char 3 bit 3 denotes the code length.

Once again:
The value is made of 12345678 of the maps to line.
The address is made of ABCDEFGHIJKLMNO of the maps to line.
The compare value is made of !@#$%^&* of the maps to line.
*/
        
        int addrVal = (Integer.parseInt(addressText,16) & 0xFFFF);
        // if we assume the address is 15 bits, we will ignore the top bit
        int lowAddr = addrVal & 0xFF;
        int addr_b4 = (lowAddr & 0xF);
        int addr_b3 = ((lowAddr >> 4) & 0xF);
        int highAddr = (addrVal >> 8)& 0xFF;
        int addr_b2 = (highAddr & 0xF);
        int addr_b1 = ((highAddr >> 4) & 0xF);
        
        int val = (Integer.parseInt(valText,16) & 0xFF);
        int val_b2 = (val & 0xF);
        int val_b1 = ((val >> 4) & 0xF);
        
        int comp = (Integer.parseInt(compareText,16) & 0xFF);
        int comp_b2 = (comp & 0xF);
        int comp_b1 = ((comp >> 4) & 0xF);
        

        // 1678
        byte char1 = (byte)(((val_b1 & 0x08) | (val_b2 & 0x04) | (val_b2 & 0x02) | (val_b2 & 0x01)) & 0xF);
        // H234
        byte char2 = (byte)(((addr_b3 & 0x08) | (val_b1 & 0x04) | (val_b1 & 0x02) | (val_b1 & 0x01))&0xF);
        // -IJK (- is always zero)
        byte char3 = (byte)(((0x00) | (addr_b3 & 0x04) | (addr_b3 & 0x02) | (addr_b3 & 0x01))&0xF);
        // LABC 
        byte char4 = (byte)(((addr_b4 & 0x08) | (addr_b1 & 0x04) | (addr_b1 & 0x02) | (addr_b1 & 0x01))&0xF);
        // DMNO 
        byte char5 = (byte)(((addr_b2 & 0x08) | (addr_b4 & 0x04) | (addr_b4 & 0x02) | (addr_b4 & 0x01))&0xF);
        // %EFG 
        byte char6 = (byte)(((comp_b2 & 0x08) | (addr_b2 & 0x04) | (addr_b2 & 0x02) | (addr_b2 & 0x01))&0xF);
        // !^&* 
        byte char7 = (byte)(((comp_b1 & 0x08) | (comp_b2 & 0x04) | (comp_b2 & 0x02) | (comp_b2 & 0x01))&0xF);
        // 5@#$ 
        byte char8 = (byte)(((val_b2 & 0x08) | (comp_b1 & 0x04) | (comp_b1 & 0x02) | (comp_b1 & 0x01))&0xF);
        
        StringBuffer sb = new StringBuffer("");
        sb.append(CODE_TABLE[char1]);
        sb.append(CODE_TABLE[char2]);
        sb.append(CODE_TABLE[char3]);
        sb.append(CODE_TABLE[char4]);
        sb.append(CODE_TABLE[char5]);
        sb.append(CODE_TABLE[char6]);
        sb.append(CODE_TABLE[char7]);
        sb.append(CODE_TABLE[char8]);
        return sb.toString();

    }
    
    private void setupUI(){
        MaskFormatter address6Formatter = null;
        MaskFormatter value6Formatter = null;
        MaskFormatter address8Formatter = null;
        MaskFormatter value8Formatter = null;
        MaskFormatter compare8Formatter = null;
        try {
            address6Formatter = new MaskFormatter("HHHH");
            value6Formatter = new MaskFormatter("HH");
            address8Formatter = new MaskFormatter("HHHH");
            value8Formatter = new MaskFormatter("HH");
            compare8Formatter = new MaskFormatter("HH");
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        final JFormattedTextField address6Field = new JFormattedTextField(address6Formatter);
        address6Field.setValue(new String("0000"));
        address6Field.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        
        final JFormattedTextField value6Field = new JFormattedTextField(value6Formatter);
        value6Field.setValue("00");
        value6Field.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        
        final JTextField code6Field         = new JTextField(10);
        code6Field.setEditable(false);
        
        address6Field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                code6Field.setText(updateCode6Field(address6Field.getText(), value6Field.getText()));
            }
        });
        value6Field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                  code6Field.setText(updateCode6Field(address6Field.getText(), value6Field.getText()));
            }
        });
        
        code6Field.setText(updateCode6Field(address6Field.getText(), value6Field.getText()));
        
        
        final JFormattedTextField address8Field = new JFormattedTextField(address8Formatter);
        address8Field.setValue("0000");
        address8Field.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        
        final JFormattedTextField value8Field = new JFormattedTextField(value8Formatter);
        value8Field.setValue("00");
        value8Field.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

        final JFormattedTextField compare8Field = new JFormattedTextField(compare8Formatter);
        compare8Field.setValue("00");
        compare8Field.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

        final JTextField code8Field         = new JTextField(10);
        code8Field.setEditable(false);
        address8Field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                code8Field.setText(updateCode8Field(address8Field.getText(), value8Field.getText(), compare8Field.getText()));
            }
        });
        value8Field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               code8Field.setText(updateCode8Field(address8Field.getText(), value8Field.getText(), compare8Field.getText()));
            }
        });
        
        compare8Field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               code8Field.setText(updateCode8Field(address8Field.getText(), value8Field.getText(), compare8Field.getText()));
            }
        });
        
         code8Field.setText(updateCode8Field(address8Field.getText(), value8Field.getText(), compare8Field.getText()));
        
        getContentPane().setLayout(new BorderLayout());
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new GridLayout(1,2));
        getContentPane().add(westPanel,BorderLayout.WEST);

        // the 6 code panel
        {
            JPanel thePanel = new JPanel();
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            thePanel.setBorder(new TitledBorder("6 Digit Code"));
            thePanel.setLayout(gbl);

            gbc.weightx = 0;
            gbc.weighty = 0;            
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipady = 0;
            gbc.ipadx = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
        
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            JLabel label1 = new JLabel("Memory Address:");
            gbl.setConstraints(label1, gbc);
            thePanel.add(label1);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbl.setConstraints(address6Field, gbc);
            thePanel.add(address6Field);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            JLabel label2 = new JLabel("Value:");
            gbl.setConstraints(label2, gbc);
            thePanel.add(label2);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbl.setConstraints(value6Field, gbc);
            thePanel.add(value6Field);
            
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            JLabel label3 = new JLabel("CODE:");
            gbl.setConstraints(label3, gbc);
            thePanel.add(label3);

            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbl.setConstraints(code6Field, gbc);
            thePanel.add(code6Field);
            
            
            westPanel.add(thePanel);
        }

        // the 8 code panel
        {
            JPanel thePanel = new JPanel();
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            thePanel.setBorder(new TitledBorder("8 Digit Code"));
            thePanel.setLayout(gbl);
            
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipady = 0;
            gbc.ipadx = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
        
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            JLabel label1 = new JLabel("Memory Address:");
            gbl.setConstraints(label1, gbc);
            thePanel.add(label1);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbl.setConstraints(address8Field, gbc);
            thePanel.add(address8Field);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            JLabel label2 = new JLabel("Value:");
            gbl.setConstraints(label2, gbc);
            thePanel.add(label2);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbl.setConstraints(value8Field, gbc);
            thePanel.add(value8Field);

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            JLabel label3 = new JLabel("Compare:");
            gbl.setConstraints(label3, gbc);
            thePanel.add(label3);

            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbl.setConstraints(compare8Field, gbc);
            thePanel.add(compare8Field);
           
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            JLabel label4 = new JLabel("CODE:");
            gbl.setConstraints(label4, gbc);
            thePanel.add(label4);

            gbc.gridx = 1;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            gbl.setConstraints(code8Field, gbc);
            thePanel.add(code8Field);
            
            
            westPanel.add(thePanel);
        }
        
        pack();
        
        //Set the window's location.
        setLocation(0,0);
        
    }
}
