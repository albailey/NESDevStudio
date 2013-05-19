/*
 * Operator6502.java
 *
 * Created on November 17, 2006, 2:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.core.CPU6502;

/**
 *
 * @author abailey
 */
public class Operator6502 {
    
    private final static int NOTHING_SPECIAL = 0x00;
    private final static int IS_BRANCH = 0x01; // BNE etc..
    private final static int IS_JUMP = 0x02; // Goes to a different location. ie: JSR, RTS, JMP, RTI
    private final static int IS_TERMINATOR = 0x04; // BRK NOP
    private final static int IS_RETURN = 0x08; // RTS
    private final static int ALTERS_STACK = 0x10; // PHA, RTS, JSR, etc..
    private final static int LOADS_DATA = 0x20; // LDA
    private final static int STORES_DATA = 0x40; // STA
 //   private final static int TRANSFERS_DATA = 0x80; // TAX
    
    private final static int USES_NO_REG = 0x00;
    private final static int USES_A_REG = 0x10;
    private final static int USES_X_REG = 0x20;
    private final static int USES_Y_REG = 0x40;
//    private final static int USES_MULTIPLE_REG = 0x80;
    
    // Load and Store Group
    public final static Operator6502 LDA = new Operator6502("LDA", "Load Accumulator"
            , Architecture6502.NEGATIVE_FLAG_MASK 
            | Architecture6502.ZERO_FLAG_MASK
            , LOADS_DATA
            , USES_A_REG);
    
    public final static Operator6502 LDX = new Operator6502("LDX", "Load X Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            , LOADS_DATA
            , USES_X_REG);
    
    public final static Operator6502 LDY = new Operator6502("LDY", "Load Y Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            , LOADS_DATA
            , USES_Y_REG);
    
    public final static Operator6502 STA = new Operator6502("STA", "Store Accumulator"
            , 0
            , STORES_DATA
            , USES_A_REG);
           
    
    public final static Operator6502 STX = new Operator6502("STX", "Store X Register"
             , 0
            , STORES_DATA
            , USES_X_REG);
    
    public final static Operator6502 STY = new Operator6502("STY", "Store Y Register"
             , 0
             , STORES_DATA
             , USES_Y_REG);
    
    
    
    
    // Arithmetic Group
    public final static Operator6502 ADC = new Operator6502("ADC", "Add With Carry"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.OVERFLOW_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 SBC = new Operator6502("SBC", "Subtract With Carry"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.OVERFLOW_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    
    
    // Increment/Decrement Group
    public final static Operator6502 INC = new Operator6502("INC", "Increment a Memory Location"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 INX = new Operator6502("INX", "Increment X Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 INY = new Operator6502("INY", "Increment Y Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 DEC = new Operator6502("DEC", "Decrement a Memory Location"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 DEX = new Operator6502("DEX", "Decrement X Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 DEY = new Operator6502("DEY", "Decrement Y Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    
    // Register Transfer Group
    public final static Operator6502 TAX = new Operator6502("TAX", "Transfer Accumulator to X Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 TAY = new Operator6502("TAY", "Transfer Accumulator to Y Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 TXA = new Operator6502("TXA", "Transfer X Register to Accumulator"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 TYA = new Operator6502("TYA", "Transfer Y Register to Accumulator"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    
    // Logical Group
    public final static Operator6502 AND = new Operator6502("AND", "Logical AND"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 EOR = new Operator6502("EOR", "Exclusive OR"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    public final static Operator6502 ORA = new Operator6502("ORA", "Logical Inclusive OR"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK);
    
    
    // Compare/Bit Test Group
    public final static Operator6502 CMP = new Operator6502("CMP", "Compare Accumulator"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 CPX = new Operator6502("CPX", "Compare X Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 CPY = new Operator6502("CPY", "Compare Y Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 BIT = new Operator6502("BIT", "Bit Test"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.OVERFLOW_FLAG_MASK);
    
    
    
    // Shift and Rotate Group
    public final static Operator6502 ASL = new Operator6502("ASL", "Arithmetic Shift Left"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 LSR = new Operator6502("LSR", "Logical Shift Right"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 ROL = new Operator6502("ROL", "Rotate Left"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 ROR = new Operator6502("ROR", "Rotate Right"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);
    
    
    
    // Jump and Branch Group
    public final static Operator6502 JMP = new Operator6502("JMP", "Jump to Location"
            , 0
            , IS_JUMP); 
    
    public final static Operator6502 BCC = new Operator6502("BCC", "Branch if Carry Flag Clear"
            , 0
            , IS_BRANCH); 
    
    public final static Operator6502 BCS = new Operator6502("BCS", "Branch if Carry Flag Set"
            , 0
            , IS_BRANCH); 
    
    public final static Operator6502 BEQ = new Operator6502("BEQ", "Branch if Zero Flag Set"
            , 0
            , IS_BRANCH); 
    
    public final static Operator6502 BNE = new Operator6502("BNE", "Branch if Zero Flag Clear"
            , 0
            , IS_BRANCH); 
    
    public final static Operator6502 BMI = new Operator6502("BMI", "Branch if Negative Flag Set"
            , 0
            , IS_BRANCH); 
    
    public final static Operator6502 BPL = new Operator6502("BPL", "Branch if Negative Flag Clear"
            , 0
            , IS_BRANCH); 
    
    public final static Operator6502 BVS = new Operator6502("BVS", "Branch if Overflow Flag Set"
            , 0
            , IS_BRANCH); 
    
    public final static Operator6502 BVC = new Operator6502("BVC", "Branch if Overflow Flag Clear"
            , 0
            , IS_BRANCH); 
    
    
    // Stack Group
    public final static Operator6502 TSX = new Operator6502("TSX", "Transfer Stack Pointer to X Register"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            , ALTERS_STACK);
    
    public final static Operator6502 TXS = new Operator6502("TXS", "Transfer X Register to Stack Pointer"
            , 0
            , ALTERS_STACK);
    
    public final static Operator6502 PHA = new Operator6502("PHA", "Push Accumulator On Stack"
            , 0
            , ALTERS_STACK);
    
    public final static Operator6502 PHP = new Operator6502("PHP", "Push Processor Status On Stack"
            , 0
            , ALTERS_STACK);
    
    public final static Operator6502 PLA = new Operator6502("PLA", "Pull (Pop) Accumulator From Stack"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            , ALTERS_STACK);
    
    public final static Operator6502 PLP = new Operator6502("PLP", "Pull (Pop) Processor Status From Stack"
            , Architecture6502.CARRY_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.INTERRUPT_DISABLE_FLAG_MASK
            | Architecture6502.DECIMAL_MODE_FLAG_MASK
            | Architecture6502.BREAK_COMMAND_FLAG_MASK
            | Architecture6502.OVERFLOW_FLAG_MASK
            | Architecture6502.NEGATIVE_FLAG_MASK
            , ALTERS_STACK); 
    
    
    
    // Status Flag Change Group
    public final static Operator6502 CLC = new Operator6502("CLC", "Clear Carry Flag"
            , Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 CLD = new Operator6502("CLD", "Clear Decimal Mode Flag"
            , Architecture6502.DECIMAL_MODE_FLAG_MASK );
    
    public final static Operator6502 CLI = new Operator6502("CLI", "Clear Interrupt Disable Flag"
            , Architecture6502.INTERRUPT_DISABLE_FLAG_MASK);
    
    public final static Operator6502 CLV = new Operator6502("CLV", "Clear Overflow Flag"
            , Architecture6502.OVERFLOW_FLAG_MASK);
    
    public final static Operator6502 SEC = new Operator6502("SEC", "Set Carry Flag"
            , Architecture6502.CARRY_FLAG_MASK);
    
    public final static Operator6502 SED = new Operator6502("SED", "Set Decimal Mode Flag"
            , Architecture6502.DECIMAL_MODE_FLAG_MASK );
    
    public final static Operator6502 SEI = new Operator6502("SEI", "Set Interrupt Disable Flag"
            , Architecture6502.INTERRUPT_DISABLE_FLAG_MASK);
    
    
    
    // Subroutine/Interrupt Group
    public final static Operator6502 JSR = new Operator6502("JSR", "Jump to Subroutine"
            , 0
            , IS_JUMP | ALTERS_STACK);
    
    public final static Operator6502 RTS = new Operator6502("RTS", "Return From Subroutine"
            , 0
            , IS_RETURN | ALTERS_STACK);
    
    public final static Operator6502 BRK = new Operator6502("BRK", "Force an Interrupt"
            , Architecture6502.INTERRUPT_DISABLE_FLAG_MASK
            | Architecture6502.BREAK_COMMAND_FLAG_MASK
            , IS_TERMINATOR); 
    
    public final static Operator6502 RTI = new Operator6502("RTI", "Return From Interrupt"
            , Architecture6502.CARRY_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.INTERRUPT_DISABLE_FLAG_MASK
            | Architecture6502.DECIMAL_MODE_FLAG_MASK
            | Architecture6502.BREAK_COMMAND_FLAG_MASK
            | Architecture6502.OVERFLOW_FLAG_MASK
            | Architecture6502.NEGATIVE_FLAG_MASK // ALL
            , IS_TERMINATOR);
    
    public final static Operator6502 NOP = new Operator6502("NOP", "No Operation"
            , 0 );
    
    
     // DOP
    public final static Operator6502 DOP = new Operator6502("DOP", "I do not know what DOP is"
            , 0 );

    
    public final static Operator6502 AAC = new Operator6502("AAC", "AND byte with accumulator"
            , Architecture6502.CARRY_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.NEGATIVE_FLAG_MASK);

    public final static Operator6502 ARR = new Operator6502("ARR", "AND byte with accumulator, then rotate one bit right in accumulator and check bit 5 and 6"
            , Architecture6502.CARRY_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.OVERFLOW_FLAG_MASK
            | Architecture6502.NEGATIVE_FLAG_MASK);

    public final static Operator6502 ASR = new Operator6502("ASR", "AND byte with accumulator, then shift right one bit in accumulator"
            , Architecture6502.CARRY_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.NEGATIVE_FLAG_MASK);

    public final static Operator6502 ATX = new Operator6502("ATX", "AND byte with accumulator, then transfer accumulator to X register"
            , Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.NEGATIVE_FLAG_MASK);

    public final static Operator6502 AXS = new Operator6502("AXS", "Also called ASX. AND X register with accumulator and store result in X register, then subtract byte from X register (without borrow)"
            , Architecture6502.CARRY_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.NEGATIVE_FLAG_MASK);

    public final static Operator6502 SLO = new Operator6502("SLO", "Also called ASO. ASLs the contents of a memory location and then ORs the result with the accumulator"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);


    //Not done yet
    public final static Operator6502 RLA  = new Operator6502("RLA ", "RLA"
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);

    public final static Operator6502 SRE   = new Operator6502("SRE  ", "SRE "
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);

    public final static Operator6502 RRA    = new Operator6502("RRA   ", "RRA  "
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);

    public final static Operator6502 AAX     = new Operator6502("AAX    ", "AAX   "
            , 0);
    
    public final static Operator6502 LAX      = new Operator6502("LAX     ", "LAX    "
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);

    public final static Operator6502 DCP       = new Operator6502("DCP      ", "DCP     "
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);

    public final static Operator6502 ISC        = new Operator6502("ISC       ", "ISC      "
            , Architecture6502.NEGATIVE_FLAG_MASK
            | Architecture6502.ZERO_FLAG_MASK
            | Architecture6502.CARRY_FLAG_MASK);

  public final static Operator6502 SYA        = new Operator6502("SYA       ", "AND Y register with high byte of target address of arg + 1. Store result in memory."
            , 0);
  public final static Operator6502 SXA        = new Operator6502("SXA       ", "AND X register with high byte of target address of arg + 1. Store result in memory."
            , 0);

      public final static Operator6502 KIL        = new Operator6502("KIL       ", "KIL (JAM) (HLT)"
            , 0);
    /**
     * Creates a new instance of Operator6502
     */
    public Operator6502(String token, String description, int flags) {
        this(token, description, flags, NOTHING_SPECIAL);
    }
    public Operator6502(String token, String description, int flags, int additionalSettings){
        this(token, description, flags, additionalSettings, USES_NO_REG);
    }
    public Operator6502(String token, String description, int flags, int additionalSettings, int regUsed) {
        _token = token;
        _description = description;
        _flags = flags;
        _additionalSettings = additionalSettings;
        _regUsed = regUsed;
    }
    
    public String getToken(){
        return _token;
    }
    public boolean isBranch() { // multiple direction
        return ((_additionalSettings & IS_BRANCH) == IS_BRANCH);
    }
    public boolean isJump() { // single direction
        return ((_additionalSettings & IS_JUMP) == IS_JUMP);
    }
    public boolean loadsData() {
        return ((_additionalSettings & LOADS_DATA) == LOADS_DATA);
    }
    public boolean storesData() {
        return ((_additionalSettings & STORES_DATA) == STORES_DATA);
    }
    public boolean isTerminator() { // no further direction
        return ((_additionalSettings & IS_TERMINATOR) == IS_TERMINATOR);
    }
    public boolean isAltersStack() { 
        return ((_additionalSettings & ALTERS_STACK) == ALTERS_STACK);
    }
    public boolean isReturn() {
        return ((_additionalSettings & IS_RETURN) == IS_RETURN);
    }
    
     public boolean usesARegister() {
        return ((_regUsed & USES_A_REG) == USES_A_REG);
    }
     public boolean usesXRegister() {
        return ((_regUsed & USES_X_REG) == USES_X_REG);
    }
     public boolean usesYRegister() {
        return ((_regUsed & USES_Y_REG) == USES_Y_REG);
    }
     
     
     public String getDescription() {
    	 return _description;
     }
     public int getFlags() {
    	 return _flags;
     }
    	    
    private String _token;
    private String _description;
    private int _flags;
    private int _additionalSettings;
    private int _regUsed;
}
