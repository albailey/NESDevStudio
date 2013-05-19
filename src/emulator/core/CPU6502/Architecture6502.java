/*
 * Architecture6502.java
 *
 * Created on November 17, 2006, 9:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

/**
 * Operator6502 = LDX, etc
 * OpCode6502 = Operator6502 + addressing mode
 * Instruction6502 = OpCode6502 + operands (values)
 *
 *
 * Based on the Architecture 6502 doc included in the 6502 Simulator 
 * @author abailey
 */
public interface Architecture6502 {

    //  Bit 5 is always set, and bit 4 is set for BRK and PHP, clear for an interrupt (IRQ/NMI).
    // Process status Register  NV-BDIZC (ie: Flags Register)
    public final static int NUM_FLAGS                   = 8; // really only 7 since there is one bit unused
    public final static int CARRY_FLAG              = 0; // C
    public final static int ZERO_FLAG               = 1; // Z
    public final static int INTERRUPT_DISABLE_FLAG  = 2; // I
    public final static int DECIMAL_MODE_FLAG       = 3; // D
    public final static int BREAK_COMMAND_FLAG      = 4; // B
    public final static int UNUSED_FLAG             = 5; // -
    public final static int OVERFLOW_FLAG           = 6; // V
    public final static int NEGATIVE_FLAG           = 7; // N
    
    
    // Masks for them (dont need one for the unused flag
    public final static int CARRY_FLAG_MASK              = 0x01; // C
    public final static int ZERO_FLAG_MASK               = 0x02; // Z
    public final static int INTERRUPT_DISABLE_FLAG_MASK  = 0x04; // I
    public final static int DECIMAL_MODE_FLAG_MASK       = 0x08; // D
    public final static int BREAK_COMMAND_FLAG_MASK      = 0x10; // B
    //skip unused flag
    public final static int UNUSED_FLAG_MASK                = 0x20; // Must be 1
    public final static int OVERFLOW_FLAG_MASK           = 0x40; // V
    public final static int NEGATIVE_FLAG_MASK           = 0x80; // N
    
    
    
    public final static int ZERO_PAGE_MEMORY_START  = 0x0000;
    public final static int ZERO_PAGE_MEMORY_END    = 0x00FF;
    public final static int STACK_MEMORY_START      = 0x0100;
    public final static int STACK_MEMORY_END        = 0x01FF;
     
    public final static int NUM_ADDRESSING_MODES        = 13;    
    public final static int ABSOLUTE_MODE           = 0;
    public final static int ZP_MODE                 = 1;
    public final static int IMMEDIATE_MODE          = 2;
    public final static int IMPLICIT_MODE           = 3;
    public final static int ACCUMULATOR_MODE        = 4;    
    public final static int ABSOLUTE_INDEXED_X_MODE = 5;
    public final static int ABSOLUTE_INDEXED_Y_MODE = 6;
    public final static int ZP_INDEXED_X_MODE       = 7;
    public final static int ZP_INDEXED_Y_MODE       = 8;
    public final static int INDIRECT_ABSOLUTE_MODE  = 9; // only used by JMP
    public final static int INDEXED_INDIRECT_X_MODE   = 10;
    public final static int INDIRECT_INDEXED_Y_MODE   = 11;
    public final static int RELATIVE_MODE           = 12; // only used by branch instructions
    
    
    public final static int PRG_BANK0_OFFSET = 0x8000;
    public final static int PRG_BANK1_OFFSET = 0xC000;
    
    public final static int NMI_VECTOR_VALUE = 0xFFFA;
    public final static int RESET_VECTOR_VALUE = 0xFFFC;
    public final static int IRQ_VECTOR_VALUE = 0xFFFE;
      
    
}
