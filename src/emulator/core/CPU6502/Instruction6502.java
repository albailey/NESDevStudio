/*
 * Instruction6502.java
 *
 * Created on November 19, 2006, 10:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

import utilities.ByteFormatter;

/**
 *
 * @author abailey
 */
public class Instruction6502  {
    
    private OpCode6502 _opcode;
    private byte _rawData[];
    
    /**
     * Creates a new instance of Instruction6502
     */
     public Instruction6502(OpCode6502 opcode, byte rawData[]) {
        _opcode = opcode;
        _rawData = new byte[opcode.getLength()];
        System.arraycopy(rawData,0,_rawData,0,_rawData.length);
     }

     public String getByteRepresentation(){
        return ByteFormatter.formatByte( _rawData[0] ) + " "  + getOperand();
     }

     public String toString(){        
       return _opcode.toString();
    }

    public String getOperand(){
        String remainder = "     ";
        if(_rawData.length >= 2){
            remainder = ByteFormatter.formatByte(_rawData[1]) + "   ";
            if(_rawData.length == 3){
                remainder = ByteFormatter.formatByte(_rawData[1]) + " " + ByteFormatter.formatByte(_rawData[2]);
            }
        }
        return remainder;
    }
    
    public byte[] getRawData(){
         return _rawData;
    }
    
    public OpCode6502 getOpCode(){
         return _opcode;
    }
    
    public int getEntireLength(){
        return _opcode.getLength(); // length of instruction hex and its args        
    }
      
    public boolean usesDirectLabel() {
        int addressMode = _opcode.getAddressMode();
        // any time an absolute or relative addressing mode is used, a label can be substituted
        // relative mode only used by branches
        // absolute mode(s) used by jumps and also by data accessors
        return ((addressMode == Architecture6502.RELATIVE_MODE) ||
                (addressMode == Architecture6502.ABSOLUTE_MODE) ||
                (addressMode == Architecture6502.ABSOLUTE_INDEXED_X_MODE) ||
                (addressMode == Architecture6502.ABSOLUTE_INDEXED_Y_MODE) );
    }
    
    public boolean usesLabel() {
        int addressMode = _opcode.getAddressMode();
        return (usesDirectLabel() || (addressMode == Architecture6502.INDIRECT_ABSOLUTE_MODE));
       //  return (_opcode.isBranch() || _opcode.isJump());
    }
    
   
}
