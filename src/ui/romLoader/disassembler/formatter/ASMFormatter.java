/*
 * ASMFormatter.java
 *
 * Created on November 16, 2006, 10:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.romLoader.disassembler.formatter;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Properties;

import ui.romLoader.disassembler.ROMDisassembler;
import utilities.ByteFormatter;
import emulator.core.CPU6502.Architecture6502;
import emulator.core.CPU6502.Instruction6502;
import emulator.core.CPU6502.Utilities6502;
import emulator.nes.NESRom;

/**
 *
 * @author abailey
 */
public abstract class ASMFormatter  {
    
    protected Properties constants = new Properties();
    private boolean supportingConstants = false;
    /**
     * Creates a new instance of ASMFormatter
     */
    public ASMFormatter() {
        if(supportingConstants) {
            try {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                String propFileName = "utilities/rom/formatter/DisassemblerConstants.properties"; 
                InputStream inStream = loader.getResourceAsStream(propFileName);
                constants.load(inStream);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public abstract void outputProject(File projectDir, NESRom nesRom, ROMDisassembler dis ) throws Exception;
   
    public abstract void outputASM(PrintStream ps, ROMDisassembler dis) throws Exception;
    
    public String getLabelPrefix() {
        return "L";
    }
    
    public String getVectorPrefix() {
        return ".addr";
    }
    
    // does nothing. Most formatters will override this
    public void formatConstant(PrintStream ps, String address, String constantName){
    }
    
    // does nothing. Most formatters will override this
    public void formatHeader(PrintStream ps){
        
    }
    
    public void formatConstants(PrintStream ps, Properties prop){
      
        Object keys[] = prop.keySet().toArray();
        Arrays.sort(keys);
      
        for(int i=0;i<keys.length;i++) {
            String address = (String)keys[i];
            String constantName = prop.getProperty(address);
            if(constantName != null){
                formatConstant(ps, address, constantName);
            } else {
                formatConstant(ps, address, address);
            }
        }
    }

    public String asLabel(int i){
        return asLabel(i,false);
    }
    
    public String asLabel(int i, boolean forceSixteenBit){
        String hexVal = "$" + ByteFormatter.formatInt(i);
        String retVal = constants.getProperty(hexVal);
        if(retVal == null){
            if(i >= 0x8000 && i < 0xFFFA) {
              retVal =  getLabelPrefix() + ByteFormatter.formatInt(i);
            } else {
                retVal = hexVal;
                if(i <= 0xFF && forceSixteenBit){
                    retVal = "a:" + hexVal;
                }
            }
        }
        return retVal;        
    }

    public boolean isBinary(int address){
        return false;
    }   
    
    public String formatOperand(int baseAddress, Instruction6502 op){
        int addressMode = op.getOpCode().getAddressMode();
        byte b[] = op.getRawData();
        String retVal = "";
        switch(addressMode) {
            case Architecture6502.ABSOLUTE_MODE:
                retVal = asLabel(Utilities6502.calculate16BitAddress(b[1], b[2]), true);
                //retVal = "$" + ByteFormatter.formatByte(b[2]) + ByteFormatter.formatByte(b[1]);
                break;
            case Architecture6502.ZP_MODE:
                retVal = "$" + ByteFormatter.formatByte(b[1]);
                break;
            case Architecture6502.IMMEDIATE_MODE:
                // hex is: #$
                // binary is: #%
                if(isBinary(baseAddress+1)){
                    retVal = "#%" + ByteFormatter.formatBinaryByte(b[1]);
                } else {
                    retVal = "#$" + ByteFormatter.formatByte(b[1]);
                }
                break;
            case Architecture6502.IMPLICIT_MODE:
                // nothing
                break;
            case Architecture6502.ACCUMULATOR_MODE:
                retVal = "A";
                break;
            case Architecture6502.ABSOLUTE_INDEXED_X_MODE:
                retVal = asLabel(Utilities6502.calculate16BitAddress(b[1], b[2])) + ",X";                
                //retVal = "$" + ByteFormatter.formatByte(b[2]) + ByteFormatter.formatByte(b[1]) + ",X";                
                break;
            case Architecture6502.ABSOLUTE_INDEXED_Y_MODE:
                retVal = asLabel(Utilities6502.calculate16BitAddress(b[1], b[2])) + ",Y";    
                //retVal = "$" + ByteFormatter.formatByte(b[2]) + ByteFormatter.formatByte(b[1]) + ",Y";                
                break;
            case Architecture6502.ZP_INDEXED_X_MODE:
                retVal = "$" + ByteFormatter.formatByte(b[1]) + ",X";                
                break;
            case Architecture6502.ZP_INDEXED_Y_MODE:
                retVal = "$" + ByteFormatter.formatByte(b[1]) + ",Y";                                
                break;
            case Architecture6502.INDIRECT_ABSOLUTE_MODE:   
                retVal = "(" +  asLabel(Utilities6502.calculate16BitAddress(b[1], b[2])) + ")";          
                break;
            case Architecture6502.INDEXED_INDIRECT_X_MODE:
                 retVal = "($" + ByteFormatter.formatByte(b[1]) + ",X)";   
                break;
            case Architecture6502.INDIRECT_INDEXED_Y_MODE:
                 retVal = "($" + ByteFormatter.formatByte(b[1]) + "),Y";  
                 break;
            case Architecture6502.RELATIVE_MODE:
                 int tempAddress = baseAddress + b[1] + op.getEntireLength();
                 retVal = asLabel(tempAddress);
                break;
            default: System.err.println("Developer error.  An unexpected addressing mode received:" + addressMode);
                break;               
        }
        return retVal;
    }
 
    public String formatResult(int baseAddress, Instruction6502 op){
            return "\t" + op.getOpCode().getInstruction().getToken() + "\t" + formatOperand(baseAddress, op);
    }
    
}
