/*
 * OpCode6502.java
 *
 * Created on November 16, 2006, 11:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

/**
 * opcode info can be obtained from:
 * http://www.6502.org/tutorials/6502opcodes.html
 * Cross-referenced against the docs provided with the 6502 simulator
 * An extra cycle is used when there is a carry to/from the low byte of an address to the high byte.  For example:
 *       LDA &900,Y
 * never crosses a page boundary (since the maximum value of Y is &FF)
 *       LDA &910,Y
 * only crosses a page boundary for Y>=&F0 
 *       LDA &9FF,Y
 * always crosses a boundary unless Y=&00
 * A little more complicated for indirect indexed...
 * @author abailey
 */
public class OpCode6502 {
    
    public final static OpCode6502[] OPCODES = new OpCode6502[0x100]; // all elements are initially null
    
    static {
        
        OpCode6502 actualOpcodes[] = {
        // -=-=- Load and Store entries -=-=-=
        // LDA = 8 entries
        new OpCode6502(Operator6502.LDA, Architecture6502.IMMEDIATE_MODE         , 0xA9, 2, 2),
        new OpCode6502(Operator6502.LDA, Architecture6502.ZP_MODE                , 0xA5, 2, 3),
        new OpCode6502(Operator6502.LDA, Architecture6502.ZP_INDEXED_X_MODE      , 0xB5, 2, 4),
        new OpCode6502(Operator6502.LDA, Architecture6502.ABSOLUTE_MODE          , 0xAD, 3, 4),
        new OpCode6502(Operator6502.LDA, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0xBD, 3, 4, true),
        new OpCode6502(Operator6502.LDA, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0xB9, 3, 4, true),
        new OpCode6502(Operator6502.LDA, Architecture6502.INDEXED_INDIRECT_X_MODE, 0xA1, 2, 6),
        new OpCode6502(Operator6502.LDA, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0xB1, 2, 5, true),        
        // LDX = 5 entries
        new OpCode6502(Operator6502.LDX, Architecture6502.IMMEDIATE_MODE         , 0xA2, 2, 2),
        new OpCode6502(Operator6502.LDX, Architecture6502.ZP_MODE                , 0xA6, 2, 3),
        new OpCode6502(Operator6502.LDX, Architecture6502.ZP_INDEXED_Y_MODE      , 0xB6, 2, 4),
        new OpCode6502(Operator6502.LDX, Architecture6502.ABSOLUTE_MODE          , 0xAE, 3, 4),
        new OpCode6502(Operator6502.LDX, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0xBE, 3, 4, true),        
        // LDY = 5 entries
        new OpCode6502(Operator6502.LDY, Architecture6502.IMMEDIATE_MODE         , 0xA0, 2, 2),
        new OpCode6502(Operator6502.LDY, Architecture6502.ZP_MODE                , 0xA4, 2, 3),
        new OpCode6502(Operator6502.LDY, Architecture6502.ZP_INDEXED_X_MODE      , 0xB4, 2, 4),
        new OpCode6502(Operator6502.LDY, Architecture6502.ABSOLUTE_MODE          , 0xAC, 3, 4),
        new OpCode6502(Operator6502.LDY, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0xBC, 3, 4, true),
        // STA = 7 entries
        new OpCode6502(Operator6502.STA, Architecture6502.ZP_MODE                , 0x85, 2, 3),
        new OpCode6502(Operator6502.STA, Architecture6502.ZP_INDEXED_X_MODE      , 0x95, 2, 4),
        new OpCode6502(Operator6502.STA, Architecture6502.ABSOLUTE_MODE          , 0x8D, 3, 4),
        new OpCode6502(Operator6502.STA, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x9D, 3, 5),
        new OpCode6502(Operator6502.STA, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0x99, 3, 5),
        new OpCode6502(Operator6502.STA, Architecture6502.INDEXED_INDIRECT_X_MODE, 0x81, 2, 6),
        new OpCode6502(Operator6502.STA, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0x91, 2, 6),        
        // STX = 3 entries
        new OpCode6502(Operator6502.STX, Architecture6502.ZP_MODE                , 0x86, 2, 3),
        new OpCode6502(Operator6502.STX, Architecture6502.ZP_INDEXED_Y_MODE      , 0x96, 2, 4),
        new OpCode6502(Operator6502.STX, Architecture6502.ABSOLUTE_MODE          , 0x8E, 3, 4),        
        // STY  = 3 entries
        new OpCode6502(Operator6502.STY, Architecture6502.ZP_MODE                , 0x84, 2, 3),
        new OpCode6502(Operator6502.STY, Architecture6502.ZP_INDEXED_X_MODE      , 0x94, 2, 4),
        new OpCode6502(Operator6502.STY, Architecture6502.ABSOLUTE_MODE          , 0x8C, 3, 4),
        // -=-=- Arithmetic -=-=-=
        // ADC = 8 entries
        new OpCode6502(Operator6502.ADC, Architecture6502.IMMEDIATE_MODE         , 0x69, 2, 2),
        new OpCode6502(Operator6502.ADC, Architecture6502.ZP_MODE                , 0x65, 2, 3),
        new OpCode6502(Operator6502.ADC, Architecture6502.ZP_INDEXED_X_MODE      , 0x75, 2, 4),
        new OpCode6502(Operator6502.ADC, Architecture6502.ABSOLUTE_MODE          , 0x6D, 3, 4),
        new OpCode6502(Operator6502.ADC, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x7D, 3, 4, true),
        new OpCode6502(Operator6502.ADC, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0x79, 3, 4, true),
        new OpCode6502(Operator6502.ADC, Architecture6502.INDEXED_INDIRECT_X_MODE, 0x61, 2, 6),
        new OpCode6502(Operator6502.ADC, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0x71, 2, 5, true),
       // SBC = 8 entries
        new OpCode6502(Operator6502.SBC, Architecture6502.IMMEDIATE_MODE         , 0xE9, 2, 2),
        new OpCode6502(Operator6502.SBC, Architecture6502.ZP_MODE                , 0xE5, 2, 3),
        new OpCode6502(Operator6502.SBC, Architecture6502.ZP_INDEXED_X_MODE      , 0xF5, 2, 4),
        new OpCode6502(Operator6502.SBC, Architecture6502.ABSOLUTE_MODE          , 0xED, 3, 4),
        new OpCode6502(Operator6502.SBC, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0xFD, 3, 4, true),
        new OpCode6502(Operator6502.SBC, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0xF9, 3, 4, true),
        new OpCode6502(Operator6502.SBC, Architecture6502.INDEXED_INDIRECT_X_MODE, 0xE1, 2, 6),
        new OpCode6502(Operator6502.SBC, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0xF1, 2, 5, true),

        // -=-=- Increment/Decrement -=-=-= 
        // INC = 4 entries
        new OpCode6502(Operator6502.INC, Architecture6502.ABSOLUTE_MODE          , 0xEE, 3, 6),
        new OpCode6502(Operator6502.INC, Architecture6502.ZP_MODE                , 0xE6, 2, 5),
        new OpCode6502(Operator6502.INC, Architecture6502.ZP_INDEXED_X_MODE      , 0xF6, 2, 6),
        new OpCode6502(Operator6502.INC, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0xFE, 3, 7),
        // INX = 1 entry
        new OpCode6502(Operator6502.INX, Architecture6502.IMPLICIT_MODE          , 0xE8, 1, 2),
        // INY = 1 entry
        new OpCode6502(Operator6502.INY, Architecture6502.IMPLICIT_MODE          , 0xC8, 1, 2),
        // DEC = 4 entries
        new OpCode6502(Operator6502.DEC, Architecture6502.ABSOLUTE_MODE          , 0xCE, 3, 6),
        new OpCode6502(Operator6502.DEC, Architecture6502.ZP_MODE                , 0xC6, 2, 5),
        new OpCode6502(Operator6502.DEC, Architecture6502.ZP_INDEXED_X_MODE      , 0xD6, 2, 6),
        new OpCode6502(Operator6502.DEC, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0xDE, 3, 7),
        // DEX = 1 entry
        new OpCode6502(Operator6502.DEX, Architecture6502.IMPLICIT_MODE          , 0xCA, 1, 2),
        // DEY = 1 entry
        new OpCode6502(Operator6502.DEY, Architecture6502.IMPLICIT_MODE          , 0x88, 1, 2),
        // -=-=- Register Transfer -=-=-= 
        // TAX = 1 entry
        new OpCode6502(Operator6502.TAX, Architecture6502.IMPLICIT_MODE          , 0xAA, 1, 2),
        // TAY = 1 entry
        new OpCode6502(Operator6502.TAY, Architecture6502.IMPLICIT_MODE          , 0xA8, 1, 2),
        // TXA = 1 entry
        new OpCode6502(Operator6502.TXA, Architecture6502.IMPLICIT_MODE          , 0x8A, 1, 2),
        // TYA = 1 entry
        new OpCode6502(Operator6502.TYA, Architecture6502.IMPLICIT_MODE          , 0x98, 1, 2),
        // -=-=- Logical -=-=-=
        // AND = 8 entries
        new OpCode6502(Operator6502.AND, Architecture6502.IMMEDIATE_MODE         , 0x29, 2, 2),
        new OpCode6502(Operator6502.AND, Architecture6502.ZP_MODE                , 0x25, 2, 3),
        new OpCode6502(Operator6502.AND, Architecture6502.ZP_INDEXED_X_MODE      , 0x35, 2, 4),
        new OpCode6502(Operator6502.AND, Architecture6502.ABSOLUTE_MODE          , 0x2D, 3, 4),
        new OpCode6502(Operator6502.AND, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x3D, 3, 4, true),
        new OpCode6502(Operator6502.AND, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0x39, 3, 4, true),
        new OpCode6502(Operator6502.AND, Architecture6502.INDEXED_INDIRECT_X_MODE, 0x21, 2, 6),
        new OpCode6502(Operator6502.AND, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0x31, 2, 5, true),
        // EOR = 8 entries
        new OpCode6502(Operator6502.EOR, Architecture6502.IMMEDIATE_MODE         , 0x49, 2, 2),
        new OpCode6502(Operator6502.EOR, Architecture6502.ZP_MODE                , 0x45, 2, 3),
        new OpCode6502(Operator6502.EOR, Architecture6502.ZP_INDEXED_X_MODE      , 0x55, 2, 4),
        new OpCode6502(Operator6502.EOR, Architecture6502.ABSOLUTE_MODE          , 0x4D, 3, 4),
        new OpCode6502(Operator6502.EOR, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x5D, 3, 4, true),
        new OpCode6502(Operator6502.EOR, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0x59, 3, 4, true),
        new OpCode6502(Operator6502.EOR, Architecture6502.INDEXED_INDIRECT_X_MODE, 0x41, 2, 6),
        new OpCode6502(Operator6502.EOR, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0x51, 2, 5, true),
        // ORA = 8 entries
        new OpCode6502(Operator6502.ORA, Architecture6502.IMMEDIATE_MODE         , 0x09, 2, 2),
        new OpCode6502(Operator6502.ORA, Architecture6502.ZP_MODE                , 0x05, 2, 3),
        new OpCode6502(Operator6502.ORA, Architecture6502.ZP_INDEXED_X_MODE      , 0x15, 2, 4),
        new OpCode6502(Operator6502.ORA, Architecture6502.ABSOLUTE_MODE          , 0x0D, 3, 4),
        new OpCode6502(Operator6502.ORA, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x1D, 3, 4, true),
        new OpCode6502(Operator6502.ORA, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0x19, 3, 4, true),
        new OpCode6502(Operator6502.ORA, Architecture6502.INDEXED_INDIRECT_X_MODE, 0x01, 2, 6),
        new OpCode6502(Operator6502.ORA, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0x11, 2, 5, true),
        // -=-=- Compare and BitTest -=-=-=-
        // CMP = 8 entries
        new OpCode6502(Operator6502.CMP, Architecture6502.ABSOLUTE_MODE          , 0xCD, 3, 4),
        new OpCode6502(Operator6502.CMP, Architecture6502.ZP_MODE                , 0xC5, 2, 3),
        new OpCode6502(Operator6502.CMP, Architecture6502.IMMEDIATE_MODE         , 0xC9, 2, 2),
        new OpCode6502(Operator6502.CMP, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0xDD, 3, 4, true),
        new OpCode6502(Operator6502.CMP, Architecture6502.ABSOLUTE_INDEXED_Y_MODE, 0xD9, 3, 4, true),
        new OpCode6502(Operator6502.CMP, Architecture6502.ZP_INDEXED_X_MODE      , 0xD5, 2, 4),
        new OpCode6502(Operator6502.CMP, Architecture6502.INDEXED_INDIRECT_X_MODE, 0xC1, 2, 6),
        new OpCode6502(Operator6502.CMP, Architecture6502.INDIRECT_INDEXED_Y_MODE, 0xD1, 2, 5, true),
        // CPX = 3 entries
        new OpCode6502(Operator6502.CPX, Architecture6502.ABSOLUTE_MODE          , 0xEC, 3, 4),
        new OpCode6502(Operator6502.CPX, Architecture6502.ZP_MODE                , 0xE4, 2, 3),
        new OpCode6502(Operator6502.CPX, Architecture6502.IMMEDIATE_MODE         , 0xE0, 2, 2),
        // CPY = 3 entries
        new OpCode6502(Operator6502.CPY, Architecture6502.ABSOLUTE_MODE          , 0xCC, 3, 4),
        new OpCode6502(Operator6502.CPY, Architecture6502.ZP_MODE                , 0xC4, 2, 3),
        new OpCode6502(Operator6502.CPY, Architecture6502.IMMEDIATE_MODE         , 0xC0, 2, 2),
        // BIT = 2 entries
        new OpCode6502(Operator6502.BIT, Architecture6502.ZP_MODE                , 0x24, 2, 3),
        new OpCode6502(Operator6502.BIT, Architecture6502.ABSOLUTE_MODE          , 0x2C, 3, 4),
        // -=-=-= Shift and Rotate -=-=-=-
        // ASL = 5 entries
        new OpCode6502(Operator6502.ASL, Architecture6502.ACCUMULATOR_MODE       , 0x0A, 1, 2),
        new OpCode6502(Operator6502.ASL, Architecture6502.ZP_MODE                , 0x06, 2, 5),
        new OpCode6502(Operator6502.ASL, Architecture6502.ZP_INDEXED_X_MODE      , 0x16, 2, 6),
        new OpCode6502(Operator6502.ASL, Architecture6502.ABSOLUTE_MODE          , 0x0E, 3, 6),
        new OpCode6502(Operator6502.ASL, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x1E, 3, 7),
        // LSR = 5 entries
        new OpCode6502(Operator6502.LSR, Architecture6502.ACCUMULATOR_MODE       , 0x4A, 1, 2),
        new OpCode6502(Operator6502.LSR, Architecture6502.ZP_MODE                , 0x46, 2, 5),
        new OpCode6502(Operator6502.LSR, Architecture6502.ZP_INDEXED_X_MODE      , 0x56, 2, 6),
        new OpCode6502(Operator6502.LSR, Architecture6502.ABSOLUTE_MODE          , 0x4E, 3, 6),
        new OpCode6502(Operator6502.LSR, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x5E, 3, 7),
        // ROL = 5 entries
        new OpCode6502(Operator6502.ROL, Architecture6502.ACCUMULATOR_MODE       , 0x2A, 1, 2),
        new OpCode6502(Operator6502.ROL, Architecture6502.ZP_MODE                , 0x26, 2, 5),
        new OpCode6502(Operator6502.ROL, Architecture6502.ZP_INDEXED_X_MODE      , 0x36, 2, 6),
        new OpCode6502(Operator6502.ROL, Architecture6502.ABSOLUTE_MODE          , 0x2E, 3, 6),
        new OpCode6502(Operator6502.ROL, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x3E, 3, 7),
        // ROR = 5 entries
        new OpCode6502(Operator6502.ROR, Architecture6502.ACCUMULATOR_MODE       , 0x6A, 1, 2),
        new OpCode6502(Operator6502.ROR, Architecture6502.ZP_MODE                , 0x66, 2, 5),
        new OpCode6502(Operator6502.ROR, Architecture6502.ZP_INDEXED_X_MODE      , 0x76, 2, 6),
        new OpCode6502(Operator6502.ROR, Architecture6502.ABSOLUTE_MODE          , 0x6E, 3, 6),
        new OpCode6502(Operator6502.ROR, Architecture6502.ABSOLUTE_INDEXED_X_MODE, 0x7E, 3, 7),
        //-=-=- Jump/ Branch -=-=-=-= 
        // JMP = 2 entries
        new OpCode6502(Operator6502.JMP, Architecture6502.ABSOLUTE_MODE          , 0x4C, 3, 3),
        new OpCode6502(Operator6502.JMP, Architecture6502.INDIRECT_ABSOLUTE_MODE , 0x6C, 3, 5),
        // BCC = 1 entry
        new OpCode6502(Operator6502.BCC, Architecture6502.RELATIVE_MODE          , 0x90, 2, 2, true, true),
        // BCS = 1 entry
        new OpCode6502(Operator6502.BCS, Architecture6502.RELATIVE_MODE          , 0xB0, 2, 2, true, true),
        // BEQ = 1 entry
        new OpCode6502(Operator6502.BEQ, Architecture6502.RELATIVE_MODE          , 0xF0, 2, 2, true, true),
        // BMI = 1 entry
        new OpCode6502(Operator6502.BMI, Architecture6502.RELATIVE_MODE          , 0x30, 2, 2, true, true),
        // BNE = 1 entry
        new OpCode6502(Operator6502.BNE, Architecture6502.RELATIVE_MODE          , 0xD0, 2, 2, true, true),
        // BPL = 1 entry
        new OpCode6502(Operator6502.BPL, Architecture6502.RELATIVE_MODE          , 0x10, 2, 2, true, true),
        // BVC = 1 entry
        new OpCode6502(Operator6502.BVC, Architecture6502.RELATIVE_MODE          , 0x50, 2, 2, true, true),
        // BVS = 1 entry
        new OpCode6502(Operator6502.BVS, Architecture6502.RELATIVE_MODE          , 0x70, 2, 2, true, true),        
        // -=-=-=-= STACK -=-=-=-=
        // TSX = 1 entry
        new OpCode6502(Operator6502.TSX, Architecture6502.IMPLICIT_MODE          , 0xBA, 1, 2),        
        // TXS = 1 entry
        new OpCode6502(Operator6502.TXS, Architecture6502.IMPLICIT_MODE          , 0x9A, 1, 2),        
        // PHA = 1 entry
        new OpCode6502(Operator6502.PHA, Architecture6502.IMPLICIT_MODE          , 0x48, 1, 3),        
        // PHP = 1 entry
        new OpCode6502(Operator6502.PHP, Architecture6502.IMPLICIT_MODE          , 0x08, 1, 3),        
        // PLA = 1 entry
        new OpCode6502(Operator6502.PLA, Architecture6502.IMPLICIT_MODE          , 0x68, 1, 4),        
        // PLP = 1 entry
        new OpCode6502(Operator6502.PLP, Architecture6502.IMPLICIT_MODE          , 0x28, 1, 4),        
        // -=-=-=-= STATUS Flag Change -=-=-==
        // CLC = 1 entry
        new OpCode6502(Operator6502.CLC, Architecture6502.IMPLICIT_MODE          , 0x18, 1, 2),        
        // CLD = 1 entry
        new OpCode6502(Operator6502.CLD, Architecture6502.IMPLICIT_MODE          , 0xD8, 1, 2),        
        // CLI = 1 entry
        new OpCode6502(Operator6502.CLI, Architecture6502.IMPLICIT_MODE          , 0x58, 1, 2),        
        // CLV = 1 entry
        new OpCode6502(Operator6502.CLV, Architecture6502.IMPLICIT_MODE          , 0xB8, 1, 2),        
        // SEC = 1 entry
        new OpCode6502(Operator6502.SEC, Architecture6502.IMPLICIT_MODE          , 0x38, 1, 2),        
        // SED = 1 entry
        new OpCode6502(Operator6502.SED, Architecture6502.IMPLICIT_MODE          , 0xF8, 1, 2),        
        // SEI = 1 entry
        new OpCode6502(Operator6502.SEI, Architecture6502.IMPLICIT_MODE          , 0x78, 1, 2),        
        // -=-=-=-= Subroutines and Interrupts -=-=-==
        // JSR = 1 entry
        new OpCode6502(Operator6502.JSR, Architecture6502.ABSOLUTE_MODE          , 0x20, 3, 6),        
        // RTS = 1 entry
        new OpCode6502(Operator6502.RTS, Architecture6502.IMPLICIT_MODE          , 0x60, 1, 6),        
        // BRK = 1 entry
        new OpCode6502(Operator6502.BRK, Architecture6502.IMPLICIT_MODE          , 0x00, 1, 7), // setting length to 2 breaks things
        // RTI = 1 entry
        new OpCode6502(Operator6502.RTI, Architecture6502.IMPLICIT_MODE          , 0x40, 1, 6),                
        // NOP = 1 entry
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0xEA, 1, 2),
        
        // additional Illegal Instructions
         // Illegal SBC
         new OpCode6502(Operator6502.SBC, Architecture6502.IMMEDIATE_MODE , 0xEB, 2, 2, false,false,true),
         // Illegal NOP
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x1A, 1, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x3A, 1, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x5A, 1, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x7A, 1, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0xDA, 1, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0xFA, 1, 2),

        new OpCode6502(Operator6502.NOP, Architecture6502.ZP_MODE                , 0x04, 2, 3),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x14, 2, 4),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x34, 2, 4),
        new OpCode6502(Operator6502.NOP, Architecture6502.ZP_MODE                , 0x44, 2, 3),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x54, 2, 4),
        new OpCode6502(Operator6502.NOP, Architecture6502.ZP_MODE                , 0x64, 2, 3),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x74, 2, 4),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x80, 2, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x82, 2, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x89, 2, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0xC2, 2, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0xD4, 2, 4),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0xE2, 2, 2),
        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0xF4, 2, 4),

        new OpCode6502(Operator6502.NOP, Architecture6502.IMPLICIT_MODE          , 0x0C, 3, 4),

        // unoffical opcodes
        // http://members.chello.nl/taf.offenga/illopc31.txt
        new OpCode6502(Operator6502.NOP, Architecture6502.ABSOLUTE_INDEXED_X_MODE          , 0x1C, 3, 4, true),
        new OpCode6502(Operator6502.NOP, Architecture6502.ABSOLUTE_INDEXED_X_MODE          , 0x3C, 3, 4, true),
        new OpCode6502(Operator6502.NOP, Architecture6502.ABSOLUTE_INDEXED_X_MODE          , 0x5C, 3, 4, true),
        new OpCode6502(Operator6502.NOP, Architecture6502.ABSOLUTE_INDEXED_X_MODE          , 0x7C, 3, 4, true),
        new OpCode6502(Operator6502.NOP, Architecture6502.ABSOLUTE_INDEXED_X_MODE          , 0xDC, 3, 4, true),
        new OpCode6502(Operator6502.NOP, Architecture6502.ABSOLUTE_INDEXED_X_MODE          , 0xFC, 3, 4, true),

        new OpCode6502(Operator6502.AAC, Architecture6502.IMMEDIATE_MODE          , 0x0B, 2, 2, false, false, true),
        new OpCode6502(Operator6502.AAC, Architecture6502.IMMEDIATE_MODE          , 0x2B, 2, 2, false, false, true),
        new OpCode6502(Operator6502.ASR, Architecture6502.IMMEDIATE_MODE          , 0x4B, 2, 2, false, false, true),
        new OpCode6502(Operator6502.ARR, Architecture6502.IMMEDIATE_MODE          , 0x6B, 2, 2, false, false, true),
        new OpCode6502(Operator6502.ATX, Architecture6502.IMMEDIATE_MODE          , 0xAB, 2, 2, false, false, true),
        new OpCode6502(Operator6502.AXS, Architecture6502.IMMEDIATE_MODE          , 0xCB, 2, 2, false, false, true),

        new OpCode6502(Operator6502.SYA, Architecture6502.ABSOLUTE_INDEXED_X_MODE , 0x9C, 3, 5, false, false, true),
        new OpCode6502(Operator6502.SXA, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0x9E, 3, 5, false, false, true),

        new OpCode6502(Operator6502.SLO, Architecture6502.ZP_MODE                 , 0x07, 2, 5, false, false, true),
        new OpCode6502(Operator6502.SLO, Architecture6502.ZP_INDEXED_X_MODE       , 0x17, 2, 6, false, false, true),
        new OpCode6502(Operator6502.SLO, Architecture6502.ABSOLUTE_MODE           , 0x0F, 3, 6, false, false, true),
        new OpCode6502(Operator6502.SLO, Architecture6502.ABSOLUTE_INDEXED_X_MODE , 0x1F, 3, 7, false, false, true),
        new OpCode6502(Operator6502.SLO, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0x1B, 3, 7, false, false, true),
        new OpCode6502(Operator6502.SLO, Architecture6502.INDEXED_INDIRECT_X_MODE , 0x03, 2, 8, false, false, true),
        new OpCode6502(Operator6502.SLO, Architecture6502.INDIRECT_INDEXED_Y_MODE , 0x13, 2, 8, false, false, true),


        // not done
        new OpCode6502(Operator6502.RLA, Architecture6502.ZP_MODE                 , 0x27, 2, 5, false, false, true),
        new OpCode6502(Operator6502.RLA, Architecture6502.ZP_INDEXED_X_MODE       , 0x37, 2, 6, false, false, true),
        new OpCode6502(Operator6502.RLA, Architecture6502.ABSOLUTE_MODE           , 0x2F, 3, 6, false, false, true),
        new OpCode6502(Operator6502.RLA, Architecture6502.ABSOLUTE_INDEXED_X_MODE , 0x3F, 3, 7, false, false, true),
        new OpCode6502(Operator6502.RLA, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0x3B, 3, 7, false, false, true),
        new OpCode6502(Operator6502.RLA, Architecture6502.INDEXED_INDIRECT_X_MODE , 0x23, 2, 8, false, false, true),
        new OpCode6502(Operator6502.RLA, Architecture6502.INDIRECT_INDEXED_Y_MODE , 0x33, 2, 8, false, false, true),

        new OpCode6502(Operator6502.SRE, Architecture6502.ZP_MODE                 , 0x47, 2, 5, false, false, true),
        new OpCode6502(Operator6502.SRE, Architecture6502.ZP_INDEXED_X_MODE       , 0x57, 2, 6, false, false, true),
        new OpCode6502(Operator6502.SRE, Architecture6502.ABSOLUTE_MODE           , 0x4F, 3, 6, false, false, true),
        new OpCode6502(Operator6502.SRE, Architecture6502.ABSOLUTE_INDEXED_X_MODE , 0x5F, 3, 7, false, false, true),
        new OpCode6502(Operator6502.SRE, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0x5B, 3, 7, false, false, true),
        new OpCode6502(Operator6502.SRE, Architecture6502.INDEXED_INDIRECT_X_MODE , 0x43, 2, 8, false, false, true),
        new OpCode6502(Operator6502.SRE, Architecture6502.INDIRECT_INDEXED_Y_MODE , 0x53, 2, 8, false, false, true),
        
        new OpCode6502(Operator6502.RRA, Architecture6502.ZP_MODE                 , 0x67, 2, 5, false, false, true),
        new OpCode6502(Operator6502.RRA, Architecture6502.ZP_INDEXED_X_MODE       , 0x77, 2, 6, false, false, true),
        new OpCode6502(Operator6502.RRA, Architecture6502.ABSOLUTE_MODE           , 0x6F, 3, 6, false, false, true),
        new OpCode6502(Operator6502.RRA, Architecture6502.ABSOLUTE_INDEXED_X_MODE , 0x7F, 3, 7, false, false, true),
        new OpCode6502(Operator6502.RRA, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0x7B, 3, 7, false, false, true),
        new OpCode6502(Operator6502.RRA, Architecture6502.INDEXED_INDIRECT_X_MODE , 0x63, 2, 8, false, false, true),
        new OpCode6502(Operator6502.RRA, Architecture6502.INDIRECT_INDEXED_Y_MODE , 0x73, 2, 8, false, false, true),

        new OpCode6502(Operator6502.AAX, Architecture6502.ZP_MODE                 , 0x87, 2, 3, false, false, true),
        new OpCode6502(Operator6502.AAX, Architecture6502.ZP_INDEXED_Y_MODE       , 0x97, 2, 4, false, false, true),
        new OpCode6502(Operator6502.AAX, Architecture6502.ABSOLUTE_MODE           , 0x83, 2, 6, false, false, true),
        new OpCode6502(Operator6502.AAX, Architecture6502.INDEXED_INDIRECT_X_MODE , 0x8F, 3, 4, false, false, true),

        new OpCode6502(Operator6502.LAX, Architecture6502.ZP_MODE                 , 0xA7, 2, 3, false, false, true),
        new OpCode6502(Operator6502.LAX, Architecture6502.ZP_INDEXED_Y_MODE       , 0xB7, 2, 4, false, false, true),
        new OpCode6502(Operator6502.LAX, Architecture6502.ABSOLUTE_MODE           , 0xAF, 3, 4, false, false, true),
        new OpCode6502(Operator6502.LAX, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0xBF, 3, 4, false, true, true),
        new OpCode6502(Operator6502.LAX, Architecture6502.INDEXED_INDIRECT_X_MODE , 0xA3, 2, 6, false, false, true),
        new OpCode6502(Operator6502.LAX, Architecture6502.INDIRECT_INDEXED_Y_MODE , 0xB3, 2, 5, false, true, true),

        new OpCode6502(Operator6502.DCP, Architecture6502.ZP_MODE                 , 0xC7, 2, 5, false, false, true),
        new OpCode6502(Operator6502.DCP, Architecture6502.ZP_INDEXED_X_MODE       , 0xD7, 2, 6, false, false, true),
        new OpCode6502(Operator6502.DCP, Architecture6502.ABSOLUTE_MODE           , 0xCF, 3, 6, false, false, true),
        new OpCode6502(Operator6502.DCP, Architecture6502.ABSOLUTE_INDEXED_X_MODE , 0xDF, 3, 7, false, false, true),
        new OpCode6502(Operator6502.DCP, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0xDB, 3, 7, false, false, true),
        new OpCode6502(Operator6502.DCP, Architecture6502.INDEXED_INDIRECT_X_MODE , 0xC3, 2, 8, false, false, true),
        new OpCode6502(Operator6502.DCP, Architecture6502.INDIRECT_INDEXED_Y_MODE , 0xD3, 2, 8, false, false, true),

        new OpCode6502(Operator6502.ISC, Architecture6502.ZP_MODE                 , 0xE7, 2, 5, false, false, true),
        new OpCode6502(Operator6502.ISC, Architecture6502.ZP_INDEXED_X_MODE       , 0xF7, 2, 6, false, false, true),
        new OpCode6502(Operator6502.ISC, Architecture6502.ABSOLUTE_MODE           , 0xEF, 3, 6, false, false, true),
        new OpCode6502(Operator6502.ISC, Architecture6502.ABSOLUTE_INDEXED_X_MODE , 0xFF, 3, 7, false, false, true),
        new OpCode6502(Operator6502.ISC, Architecture6502.ABSOLUTE_INDEXED_Y_MODE , 0xFB, 3, 7, false, false, true),
        new OpCode6502(Operator6502.ISC, Architecture6502.INDEXED_INDIRECT_X_MODE , 0xE3, 2, 8, false, false, true),
        new OpCode6502(Operator6502.ISC, Architecture6502.INDIRECT_INDEXED_Y_MODE , 0xF3, 2, 8, false, false, true),

        new OpCode6502(Operator6502.KIL, Architecture6502.IMPLICIT_MODE , 0x02, 1, 1, false, false, true),
 //       new OpCode6502(Operator6502.KIL, Architecture6502.IMPLICIT_MODE , 0x02, 1, 1, false, false, true),
        };

        for(int i=0; i< actualOpcodes.length;i++) {
            OpCode6502 op = actualOpcodes[i];
            if(op == null) { // it should NEVER be null 
                System.err.println("NULL value in actualOpcodes???");
                continue;
            }
            int hexVal = op._hex & 0XFF;
            if(OPCODES[hexVal] != null) {
                System.err.println("Developer error. Duplicate opcode hex value found in table for: " + hexVal);
                continue;                
            }
            OPCODES[hexVal] = op;            
        }
       /*
        int blargTable1[] = {
7,6,0,8,3,3,5,5,3,2,2,2,4,4,6,6,
0,5,0,8,4,4,6,6,2,4,2,7,4,4,7,7,
6,6,0,8,3,3,5,5,4,2,2,2,4,4,6,6,
0,5,0,8,4,4,6,6,2,4,2,7,4,4,7,7,
6,6,0,8,3,3,5,5,3,2,2,2,3,4,6,6,
0,5,0,8,4,4,6,6,2,4,2,7,4,4,7,7,
6,6,0,8,3,3,5,5,4,2,2,2,5,4,6,6,
0,5,0,8,4,4,6,6,2,4,2,7,4,4,7,7,
2,6,2,6,3,3,3,3,2,2,2,2,4,4,4,4,
0,6,0,6,4,4,4,4,2,5,2,5,5,5,5,5,
2,6,2,6,3,3,3,3,2,2,2,2,4,4,4,4,
0,5,0,5,4,4,4,4,2,4,2,4,4,4,4,4,
2,6,2,8,3,3,5,5,2,2,2,2,4,4,6,6,
0,5,0,8,4,4,6,6,2,4,2,7,4,4,7,7,
2,6,2,8,3,3,5,5,2,2,2,2,4,4,6,6,
0,5,0,8,4,4,6,6,2,4,2,7,4,4,7,7
        };

        int blargTable2[] = {
7,6,0,8,3,3,5,5,3,2,2,2,4,4,6,6,
0,6,0,8,4,4,6,6,2,5,2,7,5,5,7,7,
6,6,0,8,3,3,5,5,4,2,2,2,4,4,6,6,
0,6,0,8,4,4,6,6,2,5,2,7,5,5,7,7,
6,6,0,8,3,3,5,5,3,2,2,2,3,4,6,6,
0,6,0,8,4,4,6,6,2,5,2,7,5,5,7,7,
6,6,0,8,3,3,5,5,4,2,2,2,5,4,6,6,
0,6,0,8,4,4,6,6,2,5,2,7,5,5,7,7,
2,6,2,6,3,3,3,3,2,2,2,2,4,4,4,4,
0,6,0,6,4,4,4,4,2,5,2,5,5,5,5,5,
2,6,2,6,3,3,3,3,2,2,2,2,4,4,4,4,
0,6,0,6,4,4,4,4,2,5,2,5,5,5,5,5,
2,6,2,8,3,3,5,5,2,2,2,2,4,4,6,6,
0,6,0,8,4,4,6,6,2,5,2,7,5,5,7,7,
2,6,2,8,3,3,5,5,2,2,2,2,4,4,6,6,
0,6,0,8,4,4,6,6,2,5,2,7,5,5,7,7
        };

        int regTable[][] = new int[16][16];
        int advTable[][] = new int[16][16];
        int hex = 0;
        for(int y=0;y<16;y++){
            for(int x=0;x<16;x++){
                if(OPCODES[hex] == null){
                    regTable[x][y] = 0;
                    advTable[x][y] = 0;
                }  else {
                        regTable[x][y] = OPCODES[hex].getCycles();
                        advTable[x][y] = regTable[x][y];
                        if(OPCODES[hex].isExtraCycleForPageBoundaryCross()){
                            advTable[x][y]  = regTable[x][y] + 1;
                        }
                       // if(blargTable1[hex] != regTable[x][y] ) {
                       //     System.out.println("Different:" + ByteFormatter.formatSingleByteInt(hex) + " Mine:" + regTable[x][y] + " His:" + blargTable1[hex]);
                       // }
                        if(blargTable2[hex] != advTable[x][y] ) {
                            System.out.println("Different ALT:" + ByteFormatter.formatSingleByteInt(hex) + " Mine:" + advTable[x][y] + " His:" + blargTable2[hex]);
                        }
                }
                hex++;
            }
        }
        
        System.out.println("Regular:");
        for(int y=0;y<16;y++){
            for(int x=0;x<16;x++){
                System.out.print("," + regTable[x][y]);
            }
            System.out.println(" )" + ByteFormatter.formatSingleByteInt(y));
        }

        System.out.println("Cross:");
        for(int y=0;y<16;y++){
            for(int x=0;x<16;x++){
                System.out.print("," + advTable[x][y]);
            }
            System.out.println(" )" + ByteFormatter.formatSingleByteInt(y));
        }
*/
    }
    
    
    
    /** Creates a new instance of OpCode6502 */
    public OpCode6502(Operator6502 instruction, int addressMode, int hex, int len, int cycles) {
        this(instruction, addressMode, hex,len,cycles, false, false, false);
    }
    // special case for instructions that use addresses that cross page boundaries
    public OpCode6502(Operator6502 instruction, int addressMode, int hex, int len, int cycles, boolean addForPageBoundaryCross) {
        this(instruction, addressMode, hex,len,cycles, addForPageBoundaryCross, false, false);
    }
    // special case for instructions that use addresses that cross page boundaries and branches crossing page boundaries
    public OpCode6502(Operator6502 instruction, int addressMode, int hex, int len, int cycles, boolean addForPageBoundaryCross, boolean addForBranchTaken) {
       this(instruction, addressMode, hex,len,cycles, addForPageBoundaryCross, addForBranchTaken, false);
    }

     public OpCode6502(Operator6502 instruction, int addressMode, int hex, int len, int cycles, boolean addForPageBoundaryCross, boolean addForBranchTaken,  boolean isIllegal) {
        _instruction = instruction;
        _addressMode = addressMode;
        _hex = (byte)hex;
        _len = len;
        _cycles = cycles;
        _addForPageBoundaryCross = addForPageBoundaryCross;
        _isIllegal = isIllegal;
     }
    
    
    public byte getHex() {
        return _hex;
    }
    public int getHexInt(){
        return _hex & 0xFF;
    }
    public Operator6502 getInstruction(){
        return _instruction;
    }
    public int getLength() {
        return _len;        
    }
    public int getCycles() {
        return _cycles;
    }
    public boolean isBranch() { // multiple direction
        return _instruction.isBranch();
    }
    public boolean isJump() { // single direction
        return _instruction.isJump();
    }    
    public boolean isAltersStack(){
        return _instruction.isAltersStack();
    }    
    public boolean isReturn() { // no further direction
        return _instruction.isReturn();
    }
    public boolean isTerminator() { // no further direction
        return _instruction.isTerminator();
    }
    public int getAddressMode(){
        return _addressMode;
    }
    public boolean loadsData(){
        return _instruction.loadsData();
    }
    public boolean storesData(){
        return _instruction.storesData();
    }
    public boolean usesARegister(){
        return _instruction.usesARegister();
    }
    public boolean usesXRegister(){
        return _instruction.usesXRegister();
    }
    public boolean usesYRegister(){
        return _instruction.usesYRegister();
    }
     
    private String getAddressingDescription(){
       switch  (_addressMode) {
           case Architecture6502.ABSOLUTE_MODE:                 return "ABSOLUTE    ";
           case Architecture6502.ZP_MODE:                       return "ZP          ";
           case Architecture6502.IMMEDIATE_MODE:                return "IMMEDIATE   ";
           case Architecture6502.IMPLICIT_MODE:                 return "IMPLICIT    ";
           case Architecture6502.ACCUMULATOR_MODE:              return "ACCUMULATOR ";  
           case Architecture6502.ABSOLUTE_INDEXED_X_MODE:       return "ABSOLUTE_X  ";
           case Architecture6502.ABSOLUTE_INDEXED_Y_MODE:       return "ABSOLUTE_Y  ";
           case Architecture6502.ZP_INDEXED_X_MODE:             return "ZP_X        ";
           case Architecture6502.ZP_INDEXED_Y_MODE:             return "ZP_Y        ";
           case Architecture6502.INDIRECT_ABSOLUTE_MODE:        return "INDIRECT_ABS"; // only used by JMP
           case Architecture6502.INDEXED_INDIRECT_X_MODE:       return "INDIRECT_X  ";
           case Architecture6502.INDIRECT_INDEXED_Y_MODE:       return "INDIRECT_Y  ";
           case Architecture6502.RELATIVE_MODE:                 return "RELATIVE    ";
           default: return "ERROR";
       }
    }    
    private String getAddressingExample(){
       switch  (_addressMode) {
           case Architecture6502.ABSOLUTE_MODE:                 return "$4400       ";
           case Architecture6502.ZP_MODE:                       return "$44         ";
           case Architecture6502.IMMEDIATE_MODE:                return "#$44        ";
           case Architecture6502.IMPLICIT_MODE:                 return "            ";
           case Architecture6502.ACCUMULATOR_MODE:              return "A           ";  
           case Architecture6502.ABSOLUTE_INDEXED_X_MODE:       return "$4400,X     ";
           case Architecture6502.ABSOLUTE_INDEXED_Y_MODE:       return "$4400,Y     ";
           case Architecture6502.ZP_INDEXED_X_MODE:             return "$44,X       ";
           case Architecture6502.ZP_INDEXED_Y_MODE:             return "$44,Y       ";
           case Architecture6502.INDIRECT_ABSOLUTE_MODE:        return "($4400)     "; // only used by JMP
           case Architecture6502.INDEXED_INDIRECT_X_MODE:       return "($44,X)     ";
           case Architecture6502.INDIRECT_INDEXED_Y_MODE:       return "($44),Y     ";
           case Architecture6502.RELATIVE_MODE:                 return "LABEL       ";
           default: return "ERROR";
       }
    }    
    public String toString(){
        return getDescription();
    }

    public String getBasicDescription(){
        return _instruction.getToken();
    }
    public String getDescription(){
        return _instruction.getToken() + " " + getAddressingDescription();
    }
    public String getExampleDescription(){
        return _instruction.getToken() + " " + getAddressingExample();
    }
    
    public boolean isExtraCycleForPageBoundaryCross() {
        return _addForPageBoundaryCross;
    }

    public boolean isIllegalOpcode() {
        return _isIllegal;
    }

    private Operator6502 _instruction;
    private int _addressMode;
    private byte _hex;
    private int _len;
    private int _cycles;
    private boolean _addForPageBoundaryCross;
    private boolean _isIllegal;
}
