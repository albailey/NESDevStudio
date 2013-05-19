/*
 * CPU6502.java
 *
 * Created on December 11, 2006, 9:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.core.CPU6502;

import utilities.ByteFormatter;
import emulator.core.CPU6502.mvc.CPU6502ControllerInterface;

/**
 *
 * @author abailey
 */
public final class CPU6502 implements PageBoundaryObserver {

    private MemoryInterface _mem;
    private static final int ACCUMULATOR_ADDRESS = -1;
    private int _numCyclesRemaining = 0;
    private boolean memoryWatchMode = true; // false;
    private byte _aRegister = 0x00;
    private byte _xRegister = 0x00;
    private byte _yRegister = 0x00;
    private int _programCounter = 0xC000;
    private byte _flags = 0x00;
    private int _clock = 0; // dont really need this
    private byte _stackPointer = (byte) 0xFF;
    private CPU6502ControllerInterface _controller;
    private boolean _controllerUpdates = true;

    private boolean cliLatency = false; // special case so I do not need to implement prefetch for instructions

    public CPU6502(MemoryInterface mem) {
        _mem = mem;
        _numCyclesRemaining = 0;
        clear();
    }

    public boolean isCLILatency() {
        return cliLatency;
    }
    public void clear() {
        _aRegister = 0x00;
        _xRegister = 0x00;
        _yRegister = 0x00;
        _programCounter = 0xC000;
        _flags = 0x00 | Architecture6502.UNUSED_FLAG_MASK | Architecture6502.BREAK_COMMAND_FLAG_MASK | Architecture6502.INTERRUPT_DISABLE_FLAG_MASK;
        // supposed to be $34 on startup
        _clock = 0; // dont really need this
        //     _stack = new byte[0xFF + 1];
        _stackPointer = (byte) 0xFD;
    }

    public void setMemoryWatchMode(boolean val) {
        memoryWatchMode = val;
    }

    public String getStateString() {
        return "Clock: " + getClock() + " Program Counter:" + getProgramCounter() + " Accumulator:" + getAccumulator() + " X Register: " + getXRegister() + " Y Register: " + getYRegister();
    }

    public void notifyPageCrossed() {
        _numCyclesRemaining++;
    }

    public final boolean isNewInstruction() {
        return (_numCyclesRemaining <= 0);
    }

    public CPU6502 makeCopy() {
        CPU6502 copy = new CPU6502(_mem.makeCopy());
        copy._aRegister = _aRegister;
        copy._xRegister = _xRegister;
        copy._yRegister = _yRegister;
        copy._programCounter = _programCounter;
        copy._flags = _flags;
        copy._clock = _clock;
        copy._controller = null;
        copy._controllerUpdates = _controllerUpdates;
        copy._stackPointer = _stackPointer;
        //       System.arraycopy(_stack, 0, copy._stack, 0, _stack.length);
        return copy;
    }

    public void setCPUController(CPU6502ControllerInterface controller) {
        _controller = controller;
    }

    public CPU6502ControllerInterface getCPUController() {
        return _controller;
    }

    public void setControllerUpdatesMode(boolean flag) {
        _controllerUpdates = flag;
        if (flag) {
            if (_controller != null) {
                _controller.notifyCPUModelChanged(CPU6502ControllerInterface.MODEL_CHANGED);
            }
        }
    }

    public boolean getControllerUpdatesMode() {
        return _controllerUpdates;
    }

    public byte getFlags() {
        return _flags;
    }

    public void setFlags(byte f) {
        _flags = (byte) (f | Architecture6502.UNUSED_FLAG_MASK);
        notifyCPUFlagChangeListeners();
    }

    public byte getStackPointer() {
        return _stackPointer;
    }

    public void setStackPointer(byte b) {
        _stackPointer = b;
        notifyCPUStateChangeListeners();
    }


    // the flags
    public void setNegativeFlag(boolean flag) {
        alterFlag(flag, Architecture6502.NEGATIVE_FLAG_MASK);
    }

    public boolean getNegativeFlag() {
        return isFlagSet(Architecture6502.NEGATIVE_FLAG_MASK);
    }

    public void setZeroFlag(boolean flag) {
        alterFlag(flag, Architecture6502.ZERO_FLAG_MASK);
    }

    public boolean getZeroFlag() {
        return isFlagSet(Architecture6502.ZERO_FLAG_MASK);
    }

    public void setOverflowFlag(boolean flag) {
        alterFlag(flag, Architecture6502.OVERFLOW_FLAG_MASK);
    }

    public boolean getOverflowFlag() {
        return isFlagSet(Architecture6502.OVERFLOW_FLAG_MASK);
    }

    public void setCarryFlag(boolean flag) {
        alterFlag(flag, Architecture6502.CARRY_FLAG_MASK);
    }

    public boolean getCarryFlag() {
        return isFlagSet(Architecture6502.CARRY_FLAG_MASK);
    }

    public void setInterruptFlag(boolean flag) {
        alterFlag(flag, Architecture6502.INTERRUPT_DISABLE_FLAG_MASK);
    }

    public boolean getInterruptFlag() {
        return isFlagSet(Architecture6502.INTERRUPT_DISABLE_FLAG_MASK);
    }

    public void setBreakFlag(boolean flag) {
        alterFlag(flag, Architecture6502.BREAK_COMMAND_FLAG_MASK);
    }

    public boolean getBreakFlag() {
        return isFlagSet(Architecture6502.BREAK_COMMAND_FLAG_MASK);
    }

    public void setDecimalFlag(boolean flag) {
        alterFlag(flag, Architecture6502.DECIMAL_MODE_FLAG_MASK);
    }

    public boolean getDecimalFlag() {
        return isFlagSet(Architecture6502.DECIMAL_MODE_FLAG_MASK);
    }

    // the registers
    public void setAccumulator(byte b) {
        _aRegister = b;
        notifyCPURegisterChangeListeners();
    }

    public byte getAccumulator() {
        return _aRegister;
    }

    public void setXRegister(byte b) {
        _xRegister = b;
        notifyCPURegisterChangeListeners();
    }

    public byte getXRegister() {
        return _xRegister;
    }

    public void setYRegister(byte b) {
        _yRegister = b;
        notifyCPURegisterChangeListeners();
    }

    public byte getYRegister() {
        return _yRegister;
    }

    // the states
    public void setProgramCounter(int address) {
        _programCounter = address;
        notifyCPUStateChangeListeners();
    }

    public int getProgramCounter() {
        return _programCounter;
    }
    public byte getOpcode() {
    	return _mem.getMemoryDirect(_programCounter);
    }
    public void incrementProgramCounter(int delta) {
        _programCounter += delta;
        notifyCPUStateChangeListeners();
    }

    public int getClock() {
        return _clock;
    }

    public void setClock(int newClock) {
        _clock = newClock;
        notifyCPUStateChangeListeners();
    }

    public void incrementClock(int delta) {
        _clock += delta;
    }

    private boolean isFlagSet(int flagMask) {
        return ((_flags & flagMask) == flagMask);
    }

    private boolean setFlag(int flagMask) {
        if (isFlagSet(flagMask)) {
            // flag was already set. do nothing
            return false;
        } else {
            // flag was clear, set it
            _flags |= flagMask;
            return true;
        }
    }

    private boolean clearFlag(int flagMask) {
        if (isFlagSet(flagMask)) {
            // flag was already set. clear it
            _flags ^= flagMask;
            return false;
        } else {
            // flag was clear, do nothing
            return false;
        }
    }

    private void alterFlag(boolean flag, int flagMask) {
        boolean hasChanged = false;
        if (flag) {
            hasChanged = setFlag(flagMask);
        } else {
            hasChanged = clearFlag(flagMask);
        }
        if (hasChanged) {
            notifyCPUFlagChangeListeners();
        }
    }

    private void notifyCPUFlagChangeListeners() {
        if (_controller != null && _controllerUpdates) {
            _controller.notifyCPUModelChanged(CPU6502ControllerInterface.MODEL_FLAGS_CHANGED);
        }
    }

    private void notifyCPUStateChangeListeners() {
        if (_controller != null && _controllerUpdates) {
            _controller.notifyCPUModelChanged(CPU6502ControllerInterface.MODEL_STATES_CHANGED);
        }
    }

    private void notifyCPURegisterChangeListeners() {
        if (_controller != null && _controllerUpdates) {
            _controller.notifyCPUModelChanged(CPU6502ControllerInterface.MODEL_REGISTERS_CHANGED);
        }
    }

    public void decrementStackPointer() {
        int sp = getStackPointer() & 0xFF;
        sp--;
        setStackPointer((byte) (sp & 0xFF));
    }

    public void pushByteOnStack(byte val) {
        int sp = getStackPointer() & 0xFF;
        _mem.setMemory(0x0100 + sp, val, memoryWatchMode);
        sp--;
        setStackPointer((byte) (sp & 0xFF));
    }

    public byte popByteFromStack() {
        int sp = (getStackPointer() & 0xFF) + 1;
        if (sp > 0xFF) {
            sp = sp & 0xFF;
        }

        byte b = _mem.getMemory(0x0100 + sp, memoryWatchMode);
        setStackPointer((byte) sp);
        return b;
    }

    public String getCurrentInstructionDescription() {
        // not propagated to memory observers
        int programCounter = getProgramCounter();
        int hexVal = _mem.getMemoryDirect(programCounter) & 0xFF;
        OpCode6502 opcode = OpCode6502.OPCODES[hexVal];
        if (opcode == null) {
            return ByteFormatter.formatInt(programCounter) + "  " + "Unsupported Opcode: " + ByteFormatter.formatByte((byte) hexVal);
        }
        byte rawData[] = new byte[opcode.getLength()];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = _mem.getMemoryDirect(programCounter + i);
        }
        Instruction6502 instr = new Instruction6502(opcode, rawData);
        String operand = "";

        
        switch (opcode.getAddressMode()) {
            case Architecture6502.ABSOLUTE_MODE:
                if (opcode.isJump()) {
                    operand = " $" + ByteFormatter.formatInt(getAbsoluteAddress_direct()); // makes no sense to show an = for a JMP
                } else {
                    operand = " $" + ByteFormatter.formatInt(getAbsoluteAddress_direct()) + " = " + ByteFormatter.formatByte(getAbsoluteResult_direct());
                }
                break;
            case Architecture6502.ZP_MODE:
                operand = " $" + ByteFormatter.formatSingleByteInt(getZeroPageAddress_direct()) + " = " + ByteFormatter.formatByte(getZeroPageResult_direct());
                break;
            case Architecture6502.IMMEDIATE_MODE:
                operand = " #$" + ByteFormatter.formatByte(getImmediateResult_direct());
                break;
            case Architecture6502.IMPLICIT_MODE:
                operand = ""; // do not show anything for these (like TXA)
                break;
            case Architecture6502.ACCUMULATOR_MODE:
                operand = " A";
                break;
            case Architecture6502.ABSOLUTE_INDEXED_X_MODE:
                operand = " $" + ByteFormatter.formatInt(getAbsoluteAddress_direct()) + ",X @ " + ByteFormatter.formatInt(getAbsoluteXAddress_direct(null)) + " = " + ByteFormatter.formatByte(getAbsoluteXResult_direct(null));
                break;
            case Architecture6502.ABSOLUTE_INDEXED_Y_MODE:
                operand = " $" + ByteFormatter.formatInt(getAbsoluteAddress_direct()) + ",Y @ " + ByteFormatter.formatInt(getAbsoluteYAddress_direct(null)) + " = " + ByteFormatter.formatByte(getAbsoluteYResult_direct(null));
                break;
            case Architecture6502.ZP_INDEXED_X_MODE:
                operand = " $" + ByteFormatter.formatSingleByteInt(getZeroPageAddress_direct()) + ",X @ " + ByteFormatter.formatInt(getZeroPageXAddress_direct()) + " = " + ByteFormatter.formatByte(getZeroPageXResult_direct());
                break;
            case Architecture6502.ZP_INDEXED_Y_MODE:
                operand = " $" + ByteFormatter.formatSingleByteInt(getZeroPageAddress_direct()) + ",Y @ " + ByteFormatter.formatInt(getZeroPageYAddress_direct()) + " = " + ByteFormatter.formatByte(getZeroPageYResult_direct());
                break;
            case Architecture6502.INDIRECT_ABSOLUTE_MODE:
                operand = " ($" + ByteFormatter.formatInt(getAbsoluteAddress_direct()) + ") " + ByteFormatter.formatInt(getIndirectAbsoluteAddress_direct());
                break;
            case Architecture6502.INDEXED_INDIRECT_X_MODE:
                operand = " ($" + ByteFormatter.formatSingleByteInt(getZeroPageAddress_direct()) + ",X) = " + ByteFormatter.formatInt(getIndirectXAddress_direct(0)) + " @ " + ByteFormatter.formatInt(getIndirectXAddress_direct()) + " = " + ByteFormatter.formatByte(getIndirectXResult_direct());
                break;
            case Architecture6502.INDIRECT_INDEXED_Y_MODE:
                operand = " ($" + ByteFormatter.formatSingleByteInt(getZeroPageAddress_direct()) + "),Y = " + ByteFormatter.formatInt(getIndirectYAddress_direct(0, null)) + " @ " + ByteFormatter.formatInt(getIndirectYAddress_direct(null)) + " = " + ByteFormatter.formatByte(getIndirectYResult_direct(null));
                break;
            case Architecture6502.RELATIVE_MODE:
                operand = " $" + ByteFormatter.formatInt(getRelativeAddress_direct());
                break;
            default:
                break;
        }
        String retString = ByteFormatter.formatInt(getProgramCounter()) + "  " + instr.getByteRepresentation() + "  " + opcode.getBasicDescription() + operand + "                                                  ";
        // Add 50 spaces, and then return only a subset  (allows the output to line up cleanly)
        return retString.substring(0, 47);
    }

    public Instruction6502 getInstructionAtProcessCounter() {
        // not propagated to memory observers
        int programCounter = getProgramCounter();
        int hexVal = _mem.getMemory(programCounter, memoryWatchMode) & 0xFF;
        OpCode6502 opcode = OpCode6502.OPCODES[hexVal];
        if (opcode == null) {
            System.err.println("Invalid opcode: " + ByteFormatter.formatByte(_mem.getMemory(programCounter, memoryWatchMode)) + " encountered at address:" + ByteFormatter.formatInt(programCounter));
            return null;
        } else {
            byte rawData[] = new byte[opcode.getLength()];
            for (int i = 0; i < rawData.length; i++) {
                rawData[i] = _mem.getMemory(programCounter + i, memoryWatchMode);
            }
            return new Instruction6502(opcode, rawData);
        }
    }

    public boolean isIncrementingClock() {
    	return (_numCyclesRemaining>0);
    }
    public boolean processNextInstructionCycle() {
        if (_numCyclesRemaining == 0) {
            return processNextInstruction();
        }
        _numCyclesRemaining--;
        incrementClock(1);
        return true;
    }

    public boolean processNextInstruction() {
        // not propagated to memory observers (here)
        cliLatency = false;
        int programCounter = getProgramCounter();
        int hexVal = _mem.getMemory(programCounter, memoryWatchMode) & 0xFF;
        OpCode6502 opcode = OpCode6502.OPCODES[hexVal];
        if (opcode == null) {
            System.err.println("Invalid opcode: " + ByteFormatter.formatByte(_mem.getMemory(programCounter, memoryWatchMode)) + " encountered at address:" + ByteFormatter.formatInt(programCounter));
            return false;
        }
        _numCyclesRemaining = opcode.getCycles();
        PageBoundaryObserver pbo = null;
        if (opcode.isExtraCycleForPageBoundaryCross()) {
            pbo = this;
        }
        
        switch (hexVal) {
            case 0x00:  //BRK IMPLICIT     example:BRK
                doBRK(opcode);
                break;// return false;
            case 0x01:  //ORA INDIRECT_X   example:ORA ($44,X)                
                doORA(getIndirectXResult(), opcode); // bitwise OR with accumulator
                break;
            case 0x05:  //ORA ZP           example:ORA $44
                doORA(getZeroPageResult(), opcode); // bitwise OR with accumulator
                break;
            case 0x06:  //ASL ZP           example:ASL $44
                doASL(getZeroPageAddress(), opcode);
                break;
            case 0x08:  //PHP IMPLICIT     example:PHP
                doPHP(opcode);
                break;
            case 0x09:  //ORA IMMEDIATE    example:ORA #$44
                doORA(getImmediateResult(), opcode); // bitwise OR with accumulator
                break;
            case 0x0A:  //ASL ACCUMULATOR  example:ASL A
                doASL(ACCUMULATOR_ADDRESS, opcode);
                break;
            case 0x0D:  //ORA ABSOLUTE     example:ORA $4400
                doORA(getAbsoluteResult(), opcode); // bitwise OR with accumulator
                break;
            case 0x0E:  //ASL ABSOLUTE     example:ASL $4400
                doASL(getAbsoluteAddress(), opcode); // bitwise OR with accumulator
                break;
            case 0x10:  //BPL RELATIVE     example:BPL LABEL
                doBPL(getRelativeAddress(), opcode);
                break;
            case 0x11:  //ORA INDIRECT_Y   example:ORA ($44),Y
                doORA(getIndirectYResult(pbo), opcode); // bitwise OR with accumulator
                break;
            case 0x15:  //ORA ZP_X         example:ORA $44,X
                doORA(getZeroPageXResult(), opcode); // bitwise OR with accumulator
                break;
            case 0x16:  //ASL ZP_X         example:ASL $44,X
                doASL(getZeroPageXAddress(), opcode);
                break;
            case 0x18:  //CLC IMPLICIT     example:CLC
                doCLC(opcode);
                break;
            case 0x19:  //ORA ABSOLUTE_Y   example:ORA $4400,Y
                doORA(getAbsoluteYResult(pbo), opcode); // bitwise OR with accumulator
                break;
            case 0x1D:  //ORA ABSOLUTE_X   example:ORA $4400,X
                doORA(getAbsoluteXResult(pbo), opcode); // bitwise OR with accumulator
                break;
            case 0x1E: //ASL ABSOLUTE_X   example:ASL $4400,X
                doASL(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x20:  //JSR ABSOLUTE     example:JSR $4400
                doJSR(getAbsoluteAddress(), opcode);
                break;
            case 0x21: //AND INDIRECT_X   example:AND ($44,X)
                doAND(getIndirectXResult(), opcode);
                break;
            case 0x24:  //BIT ZP           example:BIT $44
                doBIT(getZeroPageResult(), opcode);
                break;
            case 0x25:  //AND ZP           example:AND $44
                doAND(getZeroPageResult(), opcode);
                break;
            case 0x26:  //ROL ZP           example:ROL $44
                doROL(getZeroPageAddress(), opcode);
                break;
            case 0x28:  //PLP IMPLICIT     example:PLP
                doPLP(opcode);
                break;
            case 0x29:  //AND IMMEDIATE    example:AND #$44
                doAND(getImmediateResult(), opcode);
                break;
            case 0x2A:  //ROL ACCUMULATOR  example:ROL A
                doROL(ACCUMULATOR_ADDRESS, opcode);
                break;
            case 0x2C:  //BIT ABSOLUTE     example:BIT $4400
                doBIT(getAbsoluteResult(), opcode);
                break;
            case 0x2D: //AND ABSOLUTE     example:AND $4400
                doAND(getAbsoluteResult(), opcode);
                break;
            case 0x2E: //ROL ABSOLUTE     example:ROL $4400
                doROL(getAbsoluteAddress(), opcode);
                break;
            case 0x30: //BMI RELATIVE     example:BMI LABEL
                doBMI(getRelativeAddress(), opcode);
                break;
            case 0x31: //AND INDIRECT_Y   example:AND ($44),Y
                doAND(getIndirectYResult(pbo), opcode);
                break;
            case 0x35: //AND ZP_X         example:AND $44,X
                doAND(getZeroPageXResult(), opcode);
                break;
            case 0x36: //ROL ZP_X         example:ROL $44,X
                doROL(getZeroPageXAddress(), opcode);
                break;
            case 0x38: //SEC IMPLICIT     example:SEC
                doSEC(opcode);
                break;
            case 0x39: //AND ABSOLUTE_Y   example:AND $4400,Y
                doAND(getAbsoluteYResult(pbo), opcode);
                break;
            case 0x3D: //AND ABSOLUTE_X   example:AND $4400,X
                doAND(getAbsoluteXResult(pbo), opcode);
                break;
            case 0x3E: //ROL ABSOLUTE_X   example:ROL $4400,X
                doROL(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x40: //RTI IMPLICIT     example:RTI
                doRTI(opcode);
                //    return false;
                break;
            case 0x41: //EOR INDIRECT_X   example:EOR ($44,X)
                doEOR(getIndirectXResult(), opcode);
                break;
            case 0x45: //EOR ZP           example:EOR $44
                doEOR(getZeroPageResult(), opcode);
                break;
            case 0x46: //LSR ZP           example:LSR $44
                doLSR(getZeroPageAddress(), opcode);
                break;
            case 0x48: //PHA IMPLICIT     example:PHA
                doPHA(opcode);
                break;
            case 0x49: //EOR IMMEDIATE    example:EOR #$44
                doEOR(getImmediateResult(), opcode);
                break;
            case 0x4A: //LSR ACCUMULATOR  example:LSR A
                doLSR(ACCUMULATOR_ADDRESS, opcode);
                break;
            case 0x4C: //JMP ABSOLUTE     example:JMP $4400
                doJMP(getAbsoluteAddress(), opcode);
                break;
            case 0x4D: //EOR ABSOLUTE     example:EOR $4400
                doEOR(getAbsoluteResult(), opcode);
                break;
            case 0x4E: //LSR ABSOLUTE     example:LSR $4400
                doLSR(getAbsoluteAddress(), opcode);
                break;
            case 0x50: //BVC RELATIVE     example:BVC LABEL
                doBVC(getRelativeAddress(), opcode);
                break;
            case 0x51: //EOR INDIRECT_Y   example:EOR ($44),Y
                doEOR(getIndirectYResult(pbo), opcode);
                break;
            case 0x55: //EOR ZP_X         example:EOR $44,X
                doEOR(getZeroPageXResult(), opcode);
                break;
            case 0x56: //LSR ZP_X         example:LSR $44,X
                doLSR(getZeroPageXAddress(), opcode);
                break;
            case 0x58: //CLI IMPLICIT     example:CLI
                doCLI(opcode);
                break;
            case 0x59: //EOR ABSOLUTE_Y   example:EOR $4400,Y
                doEOR(getAbsoluteYResult(pbo), opcode);
                break;
            case 0x5D: //EOR ABSOLUTE_X   example:EOR $4400,X
                doEOR(getAbsoluteXResult(pbo), opcode);
                break;
            case 0x5E: //LSR ABSOLUTE_X   example:LSR $4400,X
                doLSR(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x60: //RTS IMPLICIT     example:RTS
                doRTS(opcode);
                break;
            case 0x61: //ADC INDIRECT_X   example:ADC ($44,X)
                doADC(getIndirectXResult(), opcode);
                break;
            case 0x65: //ADC ZP           example:ADC $44
                doADC(getZeroPageResult(), opcode);
                break;
            case 0x66: //ROR ZP           example:ROR $44
                doROR(getZeroPageAddress(), opcode);
                break;
            case 0x68: //PLA IMPLICIT     example:PLA
                doPLA(opcode);
                break;
            case 0x69: //ADC IMMEDIATE    example:ADC #$44
                doADC(getImmediateResult(), opcode);
                break;
            case 0x6A: //ROR ACCUMULATOR  example:ROR A
                doROR(ACCUMULATOR_ADDRESS, opcode);
                break;
            case 0x6C: //JMP INDIRECT_ABS example:JMP ($4400)
                doJMP(getIndirectAbsoluteAddress(), opcode);
                break;
            case 0x6D: //ADC ABSOLUTE     example:ADC $4400
                doADC(getAbsoluteResult(), opcode);
                break;
            case 0x6E: //ROR ABSOLUTE     example:ROR $4400
                doROR(getAbsoluteAddress(), opcode);
                break;
            case 0x70: //BVS RELATIVE     example:BVS LABEL
                doBVS(getRelativeAddress(), opcode);
                break;
            case 0x71: //ADC INDIRECT_Y   example:ADC ($44),Y
                doADC(getIndirectYResult(pbo), opcode);
                break;
            case 0x75: //ADC ZP_X         example:ADC $44,X
                doADC(getZeroPageXResult(), opcode);
                break;
            case 0x76: //ROR ZP_X         example:ROR $44,X
                doROR(getZeroPageXAddress(), opcode);
                break;
            case 0x78: //SEI IMPLICIT     example:SEI
                doSEI(opcode);
                break;
            case 0x79: //ADC ABSOLUTE_Y   example:ADC $4400,Y
                doADC(getAbsoluteYResult(pbo), opcode);
                break;
            case 0x7D: //ADC ABSOLUTE_X   example:ADC $4400,X
                doADC(getAbsoluteXResult(pbo), opcode);
                break;
            case 0x7E: //ROR ABSOLUTE_X   example:ROR $4400,X
                doROR(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x81: //STA INDIRECT_X   example:STA ($44,X)
                doSTA(getIndirectXAddress(), opcode);
                break;
            case 0x84: //STY ZP           example:STY $44
                doSTY(getZeroPageAddress(), opcode);
                break;
            case 0x85: //STA ZP           example:STA $44
                doSTA(getZeroPageAddress(), opcode);
                break;
            case 0x86: //STX ZP           example:STX $44
                doSTX(getZeroPageAddress(), opcode);
                break;
            case 0x88: //DEY IMPLICIT     example:DEY
                doDEY(opcode);
                break;
            case 0x8A: //TXA IMPLICIT     example:TXA
                doTXA(opcode);
                break;
            case 0x8C: //STY ABSOLUTE     example:STY $4400
                doSTY(getAbsoluteAddress(), opcode);
                break;
            case 0x8D: //STA ABSOLUTE     example:STA $4400
                doSTA(getAbsoluteAddress(), opcode);
                break;
            case 0x8E: //STX ABSOLUTE     example:STX $4400
                doSTX(getAbsoluteAddress(), opcode);
                break;
            case 0x90: //BCC RELATIVE     example:BCC LABEL
                doBCC(getRelativeAddress(), opcode);
                break;
            case 0x91: //STA INDIRECT_Y   example:STA ($44),Y
                doSTA(getIndirectYAddress(pbo), opcode);
                break;
            case 0x94: //STY ZP_X         example:STY $44,X
                doSTY(getZeroPageXAddress(), opcode);
                break;
            case 0x95: //STA ZP_X         example:STA $44,X
                doSTA(getZeroPageXAddress(), opcode);
                break;
            case 0x96: //STX ZP_Y         example:STX $44,Y
                doSTX(getZeroPageYAddress(), opcode);
                break;
            case 0x98: //TYA IMPLICIT     example:TYA
                doTYA(opcode);
                break;
            case 0x99: //STA ABSOLUTE_Y   example:STA $4400,Y
                doSTA(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0x9A: //TXS IMPLICIT     example:TXS
                doTXS(opcode);
                break;
            case 0x9D: //STA ABSOLUTE_X   example:STA $4400,X
                doSTA(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0xA0: //LDY IMMEDIATE    example:LDY #$44
                doLDY(getImmediateResult(), opcode);
                break;
            case 0xA1: //LDA INDIRECT_X   example:LDA ($44,X)
                doLDA(getIndirectXResult(), opcode);
                break;
            case 0xA2: //LDX IMMEDIATE    example:LDX #$44
                doLDX(getImmediateResult(), opcode);
                break;
            case 0xA4: //LDY ZP           example:LDY $44
                doLDY(getZeroPageResult(), opcode);
                break;
            case 0xA5: //LDA ZP           example:LDA $44
                doLDA(getZeroPageResult(), opcode);
                break;
            case 0xA6: //LDX ZP           example:LDX $44
                doLDX(getZeroPageResult(), opcode);
                break;
            case 0xA8: //TAY IMPLICIT     example:TAY
                doTAY(opcode);
                break;
            case 0xA9: //LDA IMMEDIATE    example:LDA #$44
                doLDA(getImmediateResult(), opcode);
                break;
            case 0xAA: //TAX IMPLICIT     example:TAX
                doTAX(opcode);
                break;
            case 0xAC: //LDY ABSOLUTE     example:LDY $4400
                doLDY(getAbsoluteResult(), opcode);
                break;
            case 0xAD: //LDA ABSOLUTE     example:LDA $4400
                doLDA(getAbsoluteResult(), opcode);
                break;
            case 0xAE: //LDX ABSOLUTE     example:LDX $4400
                doLDX(getAbsoluteResult(), opcode);
                break;
            case 0xB0: //BCS RELATIVE     example:BCS LABEL
                doBCS(getRelativeAddress(), opcode);
                break;
            case 0xB1: //LDA INDIRECT_Y   example:LDA ($44),Y
                doLDA(getIndirectYResult(pbo), opcode);
                break;
            case 0xB4: //LDY ZP_X         example:LDY $44,X
                doLDY(getZeroPageXResult(), opcode);
                break;
            case 0xB5: //LDA ZP_X         example:LDA $44,X
                doLDA(getZeroPageXResult(), opcode);
                break;
            case 0xB6: //LDX ZP_Y         example:LDX $44,Y
                doLDX(getZeroPageYResult(), opcode);
                break;
            case 0xB8: //CLV IMPLICIT     example:CLV
                doCLV(opcode);
                break;
            case 0xB9: //LDA ABSOLUTE_Y   example:LDA $4400,Y
                doLDA(getAbsoluteYResult(pbo), opcode);
                break;
            case 0xBA: //TSX IMPLICIT     example:TSX
                doTSX(opcode);
                break;
            case 0xBC: //LDY ABSOLUTE_X   example:LDY $4400,X
                doLDY(getAbsoluteXResult(pbo), opcode);
                break;
            case 0xBD: //LDA ABSOLUTE_X   example:LDA $4400,X
                doLDA(getAbsoluteXResult(pbo), opcode);
                break;
            case 0xBE: //LDX ABSOLUTE_Y   example:LDX $4400,Y
                doLDX(getAbsoluteYResult(pbo), opcode);
                break;
            case 0xC0: //CPY IMMEDIATE    example:CPY #$44
                doCPY(getImmediateResult(), opcode);
                break;
            case 0xC1: //CMP INDIRECT_X   example:CMP ($44,X)
                doCMP(getIndirectXResult(), opcode);
                break;
            case 0xC4: //CPY ZP           example:CPY $44
                doCPY(getZeroPageResult(), opcode);
                break;
            case 0xC5: //CMP ZP           example:CMP $44
                doCMP(getZeroPageResult(), opcode);
                break;
            case 0xC6: //DEC ZP           example:DEC $44
                doDEC(getZeroPageAddress(), opcode);
                break;
            case 0xC8: //INY IMPLICIT     example:INY
                doINY(opcode);
                break;
            case 0xC9: //CMP IMMEDIATE    example:CMP #$44
                doCMP(getImmediateResult(), opcode);
                break;
            case 0xCA: //DEX IMPLICIT     example:DEX
                doDEX(opcode);
                break;
            case 0xCC: //CPY ABSOLUTE     example:CPY $4400
                doCPY(getAbsoluteResult(), opcode);
                break;
            case 0xCD: //CMP ABSOLUTE     example:CMP $4400
                doCMP(getAbsoluteResult(), opcode);
                break;
            case 0xCE: //DEC ABSOLUTE     example:DEC $4400
                doDEC(getAbsoluteAddress(), opcode);
                break;
            case 0xD0: //BNE RELATIVE     example:BNE LABEL
                doBNE(getRelativeAddress(), opcode);
                break;
            case 0xD1: //CMP INDIRECT_Y   example:CMP ($44),Y
                doCMP(getIndirectYResult(pbo), opcode);
                break;
            case 0xD5: //CMP ZP_X         example:CMP $44,X
                doCMP(getZeroPageXResult(), opcode);
                break;
            case 0xD6: //DEC ZP_X         example:DEC $44,X
                doDEC(getZeroPageXAddress(), opcode);
                break;
            case 0xD8: //CLD IMPLICIT     example:CLD
                doCLD(opcode);
                break;
            case 0xD9: //CMP ABSOLUTE_Y   example:CMP $4400,Y
                doCMP(getAbsoluteYResult(pbo), opcode);
                break;
            case 0xDD: //CMP ABSOLUTE_X   example:CMP $4400,X
                doCMP(getAbsoluteXResult(pbo), opcode);
                break;
            case 0xDE: //DEC ABSOLUTE_X   example:DEC $4400,X
                doDEC(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0xE0: //CPX IMMEDIATE    example:CPX #$44
                doCPX(getImmediateResult(), opcode);
                break;
            case 0xE1: //SBC INDIRECT_X   example:SBC ($44,X)
                doSBC(getIndirectXResult(), opcode);
                break;
            case 0xE4: //CPX ZP           example:CPX $44
                doCPX(getZeroPageResult(), opcode);
                break;
            case 0xE5: //SBC ZP           example:SBC $44
                doSBC(getZeroPageResult(), opcode);
                break;
            case 0xE6: //INC ZP           example:INC $44
                doINC(getZeroPageAddress(), opcode);
                break;
            case 0xE8: //INX IMPLICIT     example:INX
                doINX(opcode);
                break;
            case 0xE9: //SBC IMMEDIATE    example:SBC #$44
                doSBC(getImmediateResult(), opcode);
                break;
            case 0xEB: //CPX ABSOLUTE     example:CPX $4400
                doSBC(getImmediateResult(), opcode);
                break;
            case 0xEC: //CPX ABSOLUTE     example:CPX $4400
                doCPX(getAbsoluteResult(), opcode);
                break;
            case 0xED: //SBC ABSOLUTE     example:SBC $4400
                doSBC(getAbsoluteResult(), opcode);
                break;
            case 0xEE: //INC ABSOLUTE     example:INC $4400
                doINC(getAbsoluteAddress(), opcode);
                break;
            case 0xF0: //BEQ RELATIVE     example:BEQ LABEL
                doBEQ(getRelativeAddress(), opcode);
                break;
            case 0xF1: //SBC INDIRECT_Y   example:SBC ($44),Y
                doSBC(getIndirectYResult(pbo), opcode);
                break;
            case 0xF5: //SBC ZP_X         example:SBC $44,X
                doSBC(getZeroPageXResult(), opcode);
                break;
            case 0xF6: //INC ZP_X         example:INC $44,X
                doINC(getZeroPageXAddress(), opcode);
                break;
            case 0xF8: //SED IMPLICIT     example:SED
                doSED(opcode);
                break;
            case 0xF9: //SBC ABSOLUTE_Y   example:SBC $4400,Y
                doSBC(getAbsoluteYResult(pbo), opcode);
                break;
            case 0xFD: //SBC ABSOLUTE_X   example:SBC $4400,X
                doSBC(getAbsoluteXResult(pbo), opcode);
                break;
            case 0xFE: //INC ABSOLUTE_X   example:INC $4400,X
                doINC(getAbsoluteXAddress(pbo), opcode);
                break;

            // unoffical opcodes here
            // all the NOPs
            // 1 byte NOPs
            case 0x1A:
            case 0x3A:
            case 0x5A:
            case 0x7A:
            case 0xDA:
            case 0xEA: //NOP IMPLICIT     example:NOP
            case 0xFA:
            // 2 byte NOPs
            case 0x04:
            case 0x14:
            case 0x34:
            case 0x44:
            case 0x54:
            case 0x64:
            case 0x74:
            case 0x80:
            case 0x82:
            case 0x89:
            case 0xC2:
            case 0xD4:
            case 0xE2:
            case 0xF4:
            // 3 byte NOP
            case 0x0C:
                doNOP(opcode);
                break;
            case 0x1C:
            case 0x3C:
            case 0x5C:
            case 0x7C:
            case 0xDC:
            case 0xFC:
                getAbsoluteXResult(pbo); // this will add a cycle on a page boundary cross
                doNOP(opcode);
                break;
            case 0x0B:
            case 0x2B:
                doAAC(getImmediateResult(), opcode);
                break;
            case 0x4B:
                doASR(getImmediateResult(), opcode);
                break;
            case 0x6B:
                doARR(getImmediateResult(), opcode);
                break;
            case 0xAB:
                doATX(getImmediateResult(), opcode);
                break;
            case 0xCB:
                doAXS(getImmediateResult(), opcode);
                break;
            case 0x07:
                doSLO(getZeroPageAddress(), opcode);
                break;
            case 0x17:
                doSLO(getZeroPageXAddress(), opcode);
                break;
            case 0x0F:
                doSLO(getAbsoluteAddress(), opcode);
                break;
            case 0x1F:
                doSLO(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x1B:
                doSLO(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0x03:
                doSLO(getIndirectXAddress(), opcode);
                break;
            case 0x13:
                doSLO(getIndirectYAddress(pbo), opcode);
                break;
                
            case 0x9C:
                doSYA(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x9E:
                doSXA(getAbsoluteYAddress(pbo), opcode);
                break;

            case 0x27:
                doRLA(getZeroPageAddress(), opcode);
                break;
            case 0x37:
                doRLA(getZeroPageXAddress(), opcode);
                break;
            case 0x2F:
                doRLA(getAbsoluteAddress(), opcode);
                break;
            case 0x3F:
                doRLA(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x3B:
                doRLA(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0x23:
                doRLA(getIndirectXAddress(), opcode);
                break;
            case 0x33:
                doRLA(getIndirectYAddress(pbo), opcode);
                break;



            case 0x47:
                doSRE(getZeroPageAddress(), opcode);
                break;
            case 0x57:
                doSRE(getZeroPageXAddress(), opcode);
                break;
            case 0x4F:
                doSRE(getAbsoluteAddress(), opcode);
                break;
            case 0x5F:
                doSRE(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x5B:
                doSRE(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0x43:
                doSRE(getIndirectXAddress(), opcode);
                break;
            case 0x53:
                doSRE(getIndirectYAddress(pbo), opcode);
                break;


            case 0x67:
                doRRA(getZeroPageAddress(), opcode);
                break;
            case 0x77:
                doRRA(getZeroPageXAddress(), opcode);
                break;
            case 0x6F:
                doRRA(getAbsoluteAddress(), opcode);
                break;
            case 0x7F:
                doRRA(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0x7B:
                doRRA(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0x63:
                doRRA(getIndirectXAddress(), opcode);
                break;
            case 0x73:
                doRRA(getIndirectYAddress(pbo), opcode);
                break;

            case 0x87:
                doAAX(getZeroPageAddress(), opcode);
                break;
            case 0x97:
                doAAX(getZeroPageYAddress(), opcode);
                break;
            case 0x83:
                doAAX(getIndirectXAddress(), opcode);
                break;
            case 0x8F:
                doAAX(getAbsoluteAddress(), opcode);
                break;

            case 0xA7:
                doLAX(getZeroPageAddress(), opcode);
                break;
            case 0xB7:
                doLAX(getZeroPageYAddress(), opcode);
                break;
            case 0xAF:
                doLAX(getAbsoluteAddress(), opcode);
                break;
            case 0xBF:
                doLAX(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0xA3:
                doLAX(getIndirectXAddress(), opcode);
                break;
            case 0xB3:
                doLAX(getIndirectYAddress(pbo), opcode);
                break;

            case 0xC7:
                doDCP(getZeroPageAddress(), opcode);
                break;
            case 0xD7:
                doDCP(getZeroPageXAddress(), opcode);
                break;
            case 0xCF:
                doDCP(getAbsoluteAddress(), opcode);
                break;
            case 0xDF:
                doDCP(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0xDB:
                doDCP(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0xC3:
                doDCP(getIndirectXAddress(), opcode);
                break;
            case 0xD3:
                doDCP(getIndirectYAddress(pbo), opcode);
                break;

            case 0xE7:
                doISC(getZeroPageAddress(), opcode);
                break;
            case 0xF7:
                doISC(getZeroPageXAddress(), opcode);
                break;
            case 0xEF:
                doISC(getAbsoluteAddress(), opcode);
                break;
            case 0xFF:
                doISC(getAbsoluteXAddress(pbo), opcode);
                break;
            case 0xFB:
                doISC(getAbsoluteYAddress(pbo), opcode);
                break;
            case 0xE3:
                doISC(getIndirectXAddress(), opcode);
                break;
            case 0xF3:
                doISC(getIndirectYAddress(pbo), opcode);
                break;

            case 0x02:
                doKIL(opcode);
                break;
                
            default:
                System.err.println("Developer error processing opcode: " + ByteFormatter.formatByte(_mem.getMemory(programCounter, memoryWatchMode)) + " encountered at address:" + ByteFormatter.formatInt(programCounter));
                return false;
        }

        return true;
    }

    public int getNextOpcodeAddress() {
        int address = 0;
        int programCounter = getProgramCounter();
        int hexVal = _mem.getMemory(programCounter, memoryWatchMode) & 0xFF;
        OpCode6502 opcode = OpCode6502.OPCODES[hexVal];
        if (opcode == null) {
            new Exception("Invalid opcode: " + ByteFormatter.formatByte(_mem.getMemory(programCounter, memoryWatchMode)) + " encountered at address:" + ByteFormatter.formatInt(programCounter)).printStackTrace();
            return address;
        }
        PageBoundaryObserver pbo = null;
        switch (opcode.getAddressMode()) {
            case Architecture6502.ABSOLUTE_MODE:
                address = getAbsoluteAddress();
                break;
            case Architecture6502.ZP_MODE:
                address = getZeroPageAddress();
                break;
            case Architecture6502.IMMEDIATE_MODE:
                break; // no address
            case Architecture6502.IMPLICIT_MODE:
                break; // no address
            case Architecture6502.ACCUMULATOR_MODE:
                break; // no address
            case Architecture6502.ABSOLUTE_INDEXED_X_MODE:
                address = getAbsoluteXAddress(pbo);
                break;
            case Architecture6502.ABSOLUTE_INDEXED_Y_MODE:
                address = getAbsoluteYAddress(pbo);
                break;
            case Architecture6502.ZP_INDEXED_X_MODE:
                address = getZeroPageXAddress();
                break;
            case Architecture6502.ZP_INDEXED_Y_MODE:
                address = getZeroPageYAddress();
                break;
            case Architecture6502.INDIRECT_ABSOLUTE_MODE:
                break; // no address
            case Architecture6502.INDEXED_INDIRECT_X_MODE:
                address = getIndirectXAddress();
                break;
            case Architecture6502.INDIRECT_INDEXED_Y_MODE:
                address = getIndirectYAddress(pbo);
                break;
            case Architecture6502.RELATIVE_MODE:
                break; // no address
            default:
                break; // no address
        }
        return address;
    }

    public int getAddressFromROMData(int zpMemoryLocation) {
        return Utilities6502.calculate16BitAddress(_mem.getMemory(zpMemoryLocation, memoryWatchMode), _mem.getMemory(zpMemoryLocation + 1, memoryWatchMode));
    }

    public int getIndirectXAddress(int operand, int xVal) {
        // memory is stored within the ROM in this case...
        byte lowByte = _mem.getMemory((operand + xVal) & 0xFF, memoryWatchMode);
        byte highByte = _mem.getMemory((operand + xVal + 1) & 0xFF, memoryWatchMode);
        return Utilities6502.calculate16BitAddress(lowByte, highByte);
    }

    public int getIndirectXAddress_direct(int operand, int xVal) {
        // memory is stored within the ROM in this case...
        byte lowByte = _mem.getMemoryDirect((operand + xVal) & 0xFF);
        byte highByte = _mem.getMemoryDirect((operand + xVal + 1) & 0xFF);
        return Utilities6502.calculate16BitAddress(lowByte, highByte);
    }

    private int getAbsoluteAddress() {
        return Utilities6502.calculate16BitAddress(_mem.getMemory(_programCounter + 1, memoryWatchMode), _mem.getMemory(_programCounter + 2, memoryWatchMode));
    }

    private int getAbsoluteAddress_direct() {
        return Utilities6502.calculate16BitAddress(_mem.getMemoryDirect(_programCounter + 1), _mem.getMemoryDirect(_programCounter + 2));
    }

    private int getRelativeAddress() {
        return ((_programCounter + (_mem.getMemory(_programCounter + 1, memoryWatchMode)) + 2) & 0xFFFF);
    }

    private int getRelativeAddress_direct() {
        return (_programCounter + (_mem.getMemoryDirect(_programCounter + 1)) + 2) & 0xFFFF;
    }

    private int getAbsoluteXAddress(PageBoundaryObserver callback) {
        return Utilities6502.calculate16BitAddressWithOffset(_mem.getMemory(_programCounter + 1, memoryWatchMode), _mem.getMemory(_programCounter + 2, memoryWatchMode), (_xRegister & 0xFF), callback);
    }

    private int getAbsoluteXAddress_direct(PageBoundaryObserver callback) {
        return Utilities6502.calculate16BitAddressWithOffset(_mem.getMemoryDirect(_programCounter + 1), _mem.getMemoryDirect(_programCounter + 2), (_xRegister & 0xFF), callback);
    }

    private int getAbsoluteYAddress(PageBoundaryObserver callback) {
        return Utilities6502.calculate16BitAddressWithOffset(_mem.getMemory(_programCounter + 1, memoryWatchMode), _mem.getMemory(_programCounter + 2, memoryWatchMode), (_yRegister & 0xFF), callback);
    }

    private int getAbsoluteYAddress_direct(PageBoundaryObserver callback) {
        return Utilities6502.calculate16BitAddressWithOffset(_mem.getMemoryDirect(_programCounter + 1), _mem.getMemoryDirect(_programCounter + 2), (_yRegister & 0xFF), callback);
    }

    private int getZeroPageAddress() {
       return (_mem.getMemory(_programCounter + 1, memoryWatchMode))& 0xFF;
    }

    private int getZeroPageAddress_direct() {
        return (_mem.getMemoryDirect(_programCounter + 1) ) & 0xFF;
    }

    private int getZeroPageXAddress() {
        return (((_mem.getMemory(_programCounter + 1, memoryWatchMode)) + (_xRegister & 0xFF)) & 0xFF);
    }

    private int getZeroPageXAddress_direct() {
        return (((_mem.getMemoryDirect(_programCounter + 1)) + (_xRegister & 0xFF)) & 0xFF);
    }

    private int getZeroPageYAddress() {
        int programCounter = getProgramCounter();
        int yVal = getYRegister() & 0xFF;
        byte operand = _mem.getMemory(programCounter + 1, memoryWatchMode);
        return ((operand + yVal) & 0xFF);
    }

    private int getZeroPageYAddress_direct() {
        int programCounter = getProgramCounter();
        int yVal = getYRegister() & 0xFF;
        byte operand = _mem.getMemoryDirect(programCounter + 1);
        return ((operand + yVal) & 0xFF);
    }

    private int getIndirectXAddress() {
        int xVal = getXRegister() & 0xFF;
        return getIndirectXAddress(xVal);
    }

    private int getIndirectXAddress(int xVal) {
        int programCounter = getProgramCounter();
        byte operand = _mem.getMemory(programCounter + 1, memoryWatchMode);
        return getIndirectXAddress(operand, xVal);
    }

    private int getIndirectXAddress_direct() {
        int xVal = getXRegister() & 0xFF;
        return getIndirectXAddress(xVal);
    }

    private int getIndirectXAddress_direct(int xVal) {
        int programCounter = getProgramCounter();
        byte operand = _mem.getMemoryDirect(programCounter + 1);
        return getIndirectXAddress_direct(operand, xVal);
    }

    private int getIndirectYAddress(PageBoundaryObserver callback) {
        int yVal = getYRegister() & 0xFF;
        return getIndirectYAddress(yVal, callback);
    }

    private int getIndirectYAddress(int yVal, PageBoundaryObserver callback) {
        int programCounter = getProgramCounter();
        byte operand = _mem.getMemory(programCounter + 1, memoryWatchMode);
        // memory is stored within the ROM in this case...
        byte lowByte = _mem.getMemory((operand) & 0xFF, memoryWatchMode);
        byte highByte = _mem.getMemory((operand + 1) & 0xFF, memoryWatchMode);
        return Utilities6502.calculate16BitAddressWithOffset(lowByte, highByte, yVal, callback);
    }

    private int getIndirectYAddress_direct(PageBoundaryObserver callback) {
        int yVal = getYRegister() & 0xFF;
        return getIndirectYAddress_direct(yVal, callback);
    }

    private int getIndirectYAddress_direct(int yVal, PageBoundaryObserver callback) {
        int programCounter = getProgramCounter();
        byte operand = _mem.getMemoryDirect(programCounter + 1);
        // memory is stored within the ROM in this case...
        byte lowByte = _mem.getMemoryDirect((operand) & 0xFF);
        byte highByte = _mem.getMemoryDirect((operand + 1) & 0xFF);
        return Utilities6502.calculate16BitAddressWithOffset(lowByte, highByte, yVal, callback);
    }

    private int getIndirectAbsoluteAddress() {
        // An original 6502 has does not correctly fetch the target address if the indirect vector falls on a page boundary (e.g. $xxFF where xx is and value from $00 to $FF). 
        // In this case fetches the LSB from $xxFF as expected but takes the MSB from $xx00. 
        // This is fixed in some later chips like the 65SC02 so for compatibility always ensure the indirect vector is not at the end of the page.


        int programCounter = getProgramCounter();
        byte op0 = _mem.getMemory(programCounter + 1, memoryWatchMode);
        byte op1 = _mem.getMemory(programCounter + 2, memoryWatchMode);
        int address = Utilities6502.calculate16BitAddress(op0, op1);
        int highaddress = address + 1;
        if (op0 == (byte) 0xFF) {
            // boundary case
            highaddress = Utilities6502.calculate16BitAddress((byte) 0x00, op1);
        }

        byte lowByte = _mem.getMemory(address, memoryWatchMode);
        byte highByte = _mem.getMemory(highaddress, memoryWatchMode);
        return Utilities6502.calculate16BitAddress(lowByte, highByte);
    }

    private int getIndirectAbsoluteAddress_direct() {
        // An original 6502 has does not correctly fetch the target address if the indirect vector falls on a page boundary (e.g. $xxFF where xx is and value from $00 to $FF).
        // In this case fetches the LSB from $xxFF as expected but takes the MSB from $xx00.
        // This is fixed in some later chips like the 65SC02 so for compatibility always ensure the indirect vector is not at the end of the page.


        int programCounter = getProgramCounter();
        byte op0 = _mem.getMemoryDirect(programCounter + 1);
        byte op1 = _mem.getMemoryDirect(programCounter + 2);
        int address = Utilities6502.calculate16BitAddress(op0, op1);
        int highaddress = address + 1;
        if (op0 == (byte) 0xFF) {
            // boundary case
            highaddress = Utilities6502.calculate16BitAddress((byte) 0x00, op1);
        }

        byte lowByte = _mem.getMemoryDirect(address);
        byte highByte = _mem.getMemoryDirect(highaddress);
        return Utilities6502.calculate16BitAddress(lowByte, highByte);
    }

    private byte getImmediateResult() {
        int programCounter = getProgramCounter();
        return _mem.getMemory(programCounter + 1, memoryWatchMode);
    }

    private byte getImmediateResult_direct() {
        int programCounter = getProgramCounter();
        return _mem.getMemoryDirect(programCounter + 1);
    }

    private byte getAbsoluteResult() {
        return _mem.getMemory(getAbsoluteAddress(), memoryWatchMode);
    }

    private byte getAbsoluteResult_direct() {
        return _mem.getMemoryDirect(getAbsoluteAddress());
    }

    private byte getAbsoluteXResult(PageBoundaryObserver callback) {
        return _mem.getMemory(getAbsoluteXAddress(callback), memoryWatchMode);
    }

    private byte getAbsoluteXResult_direct(PageBoundaryObserver callback) {
        return _mem.getMemoryDirect(getAbsoluteXAddress(callback));
    }

    private byte getAbsoluteYResult(PageBoundaryObserver callback) {
        return _mem.getMemory(getAbsoluteYAddress(callback), memoryWatchMode);
    }

    private byte getAbsoluteYResult_direct(PageBoundaryObserver callback) {
        return _mem.getMemoryDirect(getAbsoluteYAddress_direct(callback));
    }

    private byte getZeroPageResult() {
        return _mem.getMemory(getZeroPageAddress(), memoryWatchMode);
    }

    private byte getZeroPageResult_direct() {
        return _mem.getMemoryDirect(getZeroPageAddress_direct());
    }

    private byte getZeroPageXResult() {
        return _mem.getMemory(getZeroPageXAddress(), memoryWatchMode);
    }

    private byte getZeroPageXResult_direct() {
        return _mem.getMemoryDirect(getZeroPageXAddress_direct());
    }

    private byte getZeroPageYResult() {
        return _mem.getMemory(getZeroPageYAddress(), memoryWatchMode);
    }

    private byte getZeroPageYResult_direct() {
        return _mem.getMemoryDirect(getZeroPageYAddress_direct());
    }

    private byte getIndirectXResult() {
        return _mem.getMemory(getIndirectXAddress(), memoryWatchMode);
    }

    private byte getIndirectYResult(PageBoundaryObserver callback) {
        return _mem.getMemory(getIndirectYAddress(callback), memoryWatchMode);
    }

    private byte getIndirectXResult_direct() {
        return _mem.getMemoryDirect(getIndirectXAddress_direct());
    }

    private byte getIndirectYResult_direct(PageBoundaryObserver callback) {
        return _mem.getMemoryDirect(getIndirectYAddress_direct(callback));
    }

    // bit 7 = 0x80 
    private boolean isBitSet(byte val, int bit) {
        return (((val >> bit) & 0x01) == 0x01);
    }

    /*
     *Ignore this
    Logic:
    t = A + M + P.C
    P.V = (A.7!=t.7) ? 1:0
    P.N = A.7
    P.Z = (t==0) ? 1:0
    IF (P.D)
    t = bcd(A) + bcd(M) + P.C
    P.C = (t>99) ? 1:0
    ELSE
    P.C = (t>255) ? 1:0
    A = t & 0xFF
     *
     *According to   http://www.obelisk.demon.co.uk/6502/reference.html#ADC
     *
    This instruction adds the contents of a memory location to the accumulator together with the carry bit. If overflow occurs the carry bit is set, this enables multiple byte addition to be performed.

    Processor Status after use:

    C Carry Flag Set if overflow in bit 7
    Z Zero Flag Set if A = 0
    I Interrupt Disable Not affected
    D Decimal Mode Flag Not affected
    B Break Command Not affected
    V Overflow Flag Set if sign bit is incorrect
    N Negative Flag Set if bit 7 set

    c966

     */
    private void doADC(byte value, OpCode6502 opcode) {
        byte oldA = getAccumulator();
        boolean oldCarry = getCarryFlag();
        int newVal = oldA + value; // Note: the bytes auto convert using 2s complement which helps for the overflow check
        if (oldCarry) {
            newVal++;
        }
        int carryCheck = (oldA & 0xFF) + (value & 0xFF) + (oldCarry ? 1 : 0);
        setCarryFlag(carryCheck > 0xFF);
        byte newB = (byte) (newVal & 0xFF);
        setOverflowFlag(newVal < -128 || newVal > 127); // since range is -128 to +127
        setNegativeFlag(isBitSet(newB, 7));
        setZeroFlag(newB == 0);

        setAccumulator((byte) (newVal & 0xFF));
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    A = A & M
    P.N = A.7
    P.Z = (A==0) ? 1:0
     */
    private void doAND(byte value, OpCode6502 opcode) {
        byte newA = (byte) (getAccumulator() & value);
        setAccumulator(newA);
        setNegativeFlag(isBitSet(newA, 7));
        setZeroFlag(newA == 0);
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    P.C = B.7
    B = (B << 1) & $FE
    P.N = B.7
    P.Z = (B==0) ? 1:0
     * Basically this alters the contents at the address. the address can be the accumulator
     */
    private void doASL(int destAddress, OpCode6502 opcode) {
        byte oldVal = 0;
        if (destAddress == ACCUMULATOR_ADDRESS) {
            oldVal = getAccumulator();
        } else {
            oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        }
        setCarryFlag(isBitSet(oldVal, 7));
        byte newVal = (byte) ((oldVal << 1) & 0xFE);
        setNegativeFlag(isBitSet(newVal, 7));
        setZeroFlag(newVal == 0);
        if (destAddress == ACCUMULATOR_ADDRESS) {
            setAccumulator(newVal);
        } else {
            _mem.setMemory(destAddress, newVal, memoryWatchMode);
        }
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    t = A & M
    P.N = t.7
    P.V = t.6
    P.Z = (t==0) ? 1:0

     */
    private void doBIT(byte value, OpCode6502 opcode) {
        byte temp = (byte) (getAccumulator() & value);
        setNegativeFlag(isBitSet(value, 7));
        setOverflowFlag(isBitSet(value, 6));
        setZeroFlag(temp == 0);
        incrementProgramCounter(opcode.getLength());
    }

    private void doBranchOp(boolean doBranch, int address, OpCode6502 opcode) {
        /* According to Blargg
        Branch Timing Summary
        ---------------------
        An untaken branch takes 2 clocks. A taken branch takes 3 clocks. A taken
        branch that crosses a page takes 4 clocks. Page crossing occurs when the
        high byte of the branch target address is different than the high byte
        of address of the next instruction:
         */
    	

        if (doBranch) {
            // handle page boundary cross
            if (((getProgramCounter() + opcode.getLength()) & 0xFF00) != (address & 0xFF00)) {
                _numCyclesRemaining++; // crossed a page so increment cycles by 1
            }
            setProgramCounter(address);
            _numCyclesRemaining++; // We took the branch so increment the cycles by 1
        } else {
            incrementProgramCounter(opcode.getLength());
        }
    }
    /*
     *Branch if carry flag set
    if (P.C == 1) GOTO (PC+M)
     */

    private void doBCS(int address, OpCode6502 opcode) {
        doBranchOp(getCarryFlag() == true, address, opcode);
    }
    /*
     *Branch if carry flag clear
    if (P.C == 0) GOTO (PC+M)
     */

    private void doBCC(int address, OpCode6502 opcode) {
        doBranchOp(getCarryFlag() == false, address, opcode);
    }
    /* Branch if ZERO flag is set
     *if (P.Z == 1) GOTO (PC+M)
     */

    private void doBEQ(int address, OpCode6502 opcode) {
        doBranchOp(getZeroFlag() == true, address, opcode);
    }
    /*
     *Branch if negative flag set
    if (P.N == 1) GOTO (PC+M)
     */

    private void doBMI(int address, OpCode6502 opcode) {
        doBranchOp(getNegativeFlag() == true, address, opcode);
    }
    /*
     *Branch if zero flag clear
    if (P.Z == 0) GOTO (PC+M)
     */

    private void doBNE(int address, OpCode6502 opcode) {
        doBranchOp(getZeroFlag() == false, address, opcode);
    }
    /*
     */

    private void doBPL(int address, OpCode6502 opcode) {
        doBranchOp(getNegativeFlag() == false, address, opcode);
    }
    /*
     *Branch if overflow flag is clear
    if (P.V == 0) GOTO (PC+M)
     */

    private void doBVC(int address, OpCode6502 opcode) {
        doBranchOp(getOverflowFlag() == false, address, opcode);
    }
    /*
     *Branch if overflow flag is set
    if (P.V == 1) GOTO (PC+M)
     */

    private void doBVS(int address, OpCode6502 opcode) {
        doBranchOp(getOverflowFlag() == true, address, opcode);
    }

    /*
     * Clear carry flag
    P.C = 0
     */
    private void doCLC(OpCode6502 opcode) {
        setCarryFlag(false);
        incrementProgramCounter(opcode.getLength());
    }
    /*
     * Clear decimal flag
    P.D = 0
     */

    private void doCLD(OpCode6502 opcode) {
        setDecimalFlag(false);
        incrementProgramCounter(opcode.getLength());
    }
    /*
     *Clear interrupts flag
    P.I = 0
     */

    private void doCLI(OpCode6502 opcode) {
        setInterruptFlag(false);
        incrementProgramCounter(opcode.getLength());
        cliLatency = true;
    }
    /*
     *Clear overflow flag
    P.V = 0
     */

    private void doCLV(OpCode6502 opcode) {
        setOverflowFlag(false);
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Compare A with Memory
    Logic:
    t = A - M
    P.N = t.7
    P.C = (A>=M) ? 1:0 (unsigned)
    P.Z = (t==0) ? 1:0
     */
    private void doCMP(byte value, OpCode6502 opcode) {
        byte regVal = (byte) getAccumulator();
        byte diff = (byte) ((regVal - value) & 0xFF);
        setCarryFlag((regVal & 0xFF) >= (value & 0xFF));
        setZeroFlag(regVal == value);
        setNegativeFlag(isBitSet(diff, 7));
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Compare X with Memory
    Logic:
    t = X - M
    P.N = t.7
    P.C = (X>=M) ? 1:0
    P.Z = (t==0) ? 1:0
     */
    private void doCPX(byte value, OpCode6502 opcode) {
        byte regVal = (byte) getXRegister();
        byte diff = (byte) ((regVal - value) & 0xFF);
        setCarryFlag((regVal & 0xFF) >= (value & 0xFF));
        setZeroFlag(regVal == value);
        setNegativeFlag(isBitSet(diff, 7));
        incrementProgramCounter(opcode.getLength());
    }
    /*
    Compare Y with Memory
    Logic:
    t = Y - M
    P.N = t.7
    P.C = (Y>=M) ? 1:0
    P.Z = (t==0) ? 1:0
     */

    private void doCPY(byte value, OpCode6502 opcode) {
        byte regVal = (byte) getYRegister();
        byte diff = (byte) ((regVal - value) & 0xFF);
        setCarryFlag((regVal & 0xFF) >= (value & 0xFF));
        setZeroFlag(regVal == value);
        setNegativeFlag(isBitSet(diff, 7));
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Decrement Memory by one
    Logic:
    M = (M - 1) & $FF
    P.N = M.7
    P.Z = (M==0) ? 1:0
     * DEC does NOT affect the Carry Flag (P.C) or oVerflow Flag (P.V)
     */
    private void doDEC(int address, OpCode6502 opcode) {
        byte oldVal = _mem.getMemory(address, memoryWatchMode);
        byte newVal = (byte) ((oldVal - 1) & 0xFF);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        _mem.setMemory(address, newVal, memoryWatchMode);
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    X = X - 1
    P.Z = (X==0) ? 1:0
    P.N = X.7
     *DEX does NOT affect the Carry Flag (P.C) or oVerflow Flag (P.V)
     */
    private void doDEX(OpCode6502 opcode) {
        byte oldVal = getXRegister();
        byte newVal = (byte) ((oldVal - 1) & 0xFF);
        setXRegister(newVal);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        incrementProgramCounter(opcode.getLength());
    }
    /*
    Logic:
    Y = Y - 1
    P.Z = (Y==0) ? 1:0
    P.N = Y.7
     *DEY does NOT affect the Carry Flag (P.C) or oVerflow Flag (P.V)
     */

    private void doDEY(OpCode6502 opcode) {
        byte oldVal = getYRegister();
        byte newVal = (byte) ((oldVal - 1) & 0xFF);
        setYRegister(newVal);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        incrementProgramCounter(opcode.getLength());
    }
    /*
    Logic:
    A = A ^ M
    P.N = A.7
    P.Z = (A==0) ? 1:0
     */

    private void doEOR(byte value, OpCode6502 opcode) {
        byte oldA = getAccumulator();
        byte newA = (byte) (oldA ^ value);
        setAccumulator(newA);
        setNegativeFlag(isBitSet(newA, 7));
        setZeroFlag(newA == 0);
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Increment Memory by one
     *Logic:
    M = (M + 1) & $FF
    P.N = M.7
    P.Z = (M==0) ? 1:0
    INC does NOT affect the Carry Flag (P.C) or oVerflow Flag (P.V)
     */
    private void doINC(int address, OpCode6502 opcode) {
        byte oldVal = _mem.getMemory(address, memoryWatchMode);
        byte newVal = (byte) ((oldVal + 1) & 0xFF);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        _mem.setMemory(address, newVal, memoryWatchMode);
        incrementProgramCounter(opcode.getLength());
    }
    /*
     *Logic:
    X = X + 1
    P.Z = (X==0) ? 1:0
    P.N = X.7
    INX does NOT affect the Carry Flag (P.C) or oVerflow Flag (P.V)
     */

    private void doINX(OpCode6502 opcode) {
        byte oldVal = getXRegister();
        byte newVal = (byte) ((oldVal + 1) & 0xFF);
        setXRegister(newVal);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        incrementProgramCounter(opcode.getLength());
    }
    /*
     *Logic:
    Y = Y + 1
    P.Z = (Y==0) ? 1:0
    P.N = Y.7
    INY does NOT affect the Carry Flag (P.C) or oVerflow Flag (P.V)
     */

    private void doINY(OpCode6502 opcode) {
        byte oldVal = getYRegister();
        byte newVal = (byte) ((oldVal + 1) & 0xFF);
        setYRegister(newVal);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    t = PC - 1
    bPoke(SP,t.h)
    SP = SP - 1
    bPoke(SP,t.l)
    SP = SP - 1
    PC = $A5B6
     */
    private void doJSR(int address, OpCode6502 opcode) {
        // push current program counter (after instruction) -1 onto stack (high byte first)
        int tempPC = getProgramCounter() + opcode.getLength() - 1; // address of next instruction (-1)
        byte lowByte = (byte) (tempPC & 0xFF);
        byte highByte = (byte) ((tempPC >> 8) & 0xFF);
        pushByteOnStack(highByte);
        pushByteOnStack(lowByte);
        setProgramCounter(address);
    }

    /*
    Logic:
    PC = M
     */
    private void doJMP(int address, OpCode6502 opcode) {
        setProgramCounter(address);
    }

    /* Load A with Memory
    Logic:
    A = M
    P.N = A.7
    P.Z = (A==0) ? 1:0
     */
    private void doLDA(byte value, OpCode6502 opcode) {
        setAccumulator(value);
        setNegativeFlag(isBitSet(value, 7));
        setZeroFlag(value == 0);
        incrementProgramCounter(opcode.getLength());
    }
    /* Load X with Memory
    Logic:
    X = M
    P.N = X.7
    P.Z = (X==0) ? 1:0
     */

    private void doLDX(byte value, OpCode6502 opcode) {
        setXRegister(value);
        setNegativeFlag(isBitSet(value, 7));
        setZeroFlag(value == 0);
        incrementProgramCounter(opcode.getLength());
    }
    /* Load Y with Memory
    Logic:
    Y = M
    P.N = Y.7
    P.Z = (Y==0) ? 1:0
     */

    private void doLDY(byte value, OpCode6502 opcode) {
        setYRegister(value);
        setNegativeFlag(isBitSet(value, 7));
        setZeroFlag(value == 0);
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    P.N = 0
    P.C = B.0
    B = (B >> 1) & $7F
    P.Z = (B==0) ? 1:0
     * Basically this alters the contents at the address. the address can be the accumulator
     */
    private void doLSR(int destAddress, OpCode6502 opcode) {
        byte oldVal = 0;
        if (destAddress == ACCUMULATOR_ADDRESS) {
            oldVal = getAccumulator();
        } else {
            oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        }
        setNegativeFlag(false);
        setCarryFlag(isBitSet(oldVal, 0));
        byte newVal = (byte) ((oldVal >> 1) & 0x7F);
        setZeroFlag(newVal == 0);
        if (destAddress == ACCUMULATOR_ADDRESS) {
            setAccumulator(newVal);
        } else {
            _mem.setMemory(destAddress, newVal, memoryWatchMode);
        }
        incrementProgramCounter(opcode.getLength());
    }

    private void doNOP(OpCode6502 opcode) {
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    A = A | M
    P.N = A.7
    P.Z = (A==0) ? 1:0
     */
    private void doORA(byte value, OpCode6502 opcode) {
        byte newA = (byte) (getAccumulator() | value);
        setAccumulator(newA);
        setNegativeFlag(isBitSet(newA, 7));
        setZeroFlag(newA == 0);
        incrementProgramCounter(opcode.getLength());
    }

    /*
    push(A)
    decrement StackPointer
     */
    private void doPHA(OpCode6502 opcode) {
        pushByteOnStack((byte) getAccumulator());
        incrementProgramCounter(opcode.getLength());
    }
    /*
    push(P)
    decrement StackPointer
     */

    private void doPHP(OpCode6502 opcode) {
        // PHP "adds the Break flag bit to the existing flags when it is pushed"
        pushByteOnStack((byte) (getFlags() | Architecture6502.BREAK_COMMAND_FLAG_MASK));
        incrementProgramCounter(opcode.getLength());
    }
    /*
    increment stack pointer
    A = pop()
     */

    private void doPLA(OpCode6502 opcode) {
        setAccumulator(popByteFromStack());
        byte oldA = getAccumulator();
        setZeroFlag(oldA == 0);
        setNegativeFlag(isBitSet(oldA, 7));
        incrementProgramCounter(opcode.getLength());
    }
    
    /*
    increment stack pointer
    P = pop()
     */
    
    private void doPLP(OpCode6502 opcode) {
        setFlags(popByteFromStack());
        incrementProgramCounter(opcode.getLength());
        cliLatency = true;
    }

    /*
    t = B.7
    B = (B << 1) & $FE
    B = B | P.C
    P.C = t
    P.Z = (B==0) ? 1:0
    P.N = B.7
     */
    private void doROL(int destAddress, OpCode6502 opcode) {
        byte oldVal = 0;
        if (destAddress == ACCUMULATOR_ADDRESS) {
            oldVal = getAccumulator();
        } else {
            oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        }
        boolean oldBit7 = isBitSet(oldVal, 7);
        boolean oldCarry = getCarryFlag();
        byte newVal = (byte) ((oldVal << 1) & 0xFE); // clears the last bit
        if (oldCarry) {
            newVal = (byte) (newVal | 0x01); // set the last bit
        }
        setCarryFlag(oldBit7);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        if (destAddress == ACCUMULATOR_ADDRESS) {
            setAccumulator(newVal);
        } else {
            _mem.setMemory(destAddress, newVal, memoryWatchMode);
        }
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Logic:
    t = B.0
    B = (B >> 1) & $7F
    B = B | ((P.C) ? $80:$00)
    P.C = t
    P.Z = (B==0) ? 1:0
    P.N = B.7
     */
    private void doROR(int destAddress, OpCode6502 opcode) {
        byte oldVal = 0;
        if (destAddress == ACCUMULATOR_ADDRESS) {
            oldVal = getAccumulator();
        } else {
            oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        }
        boolean oldBit0 = isBitSet(oldVal, 0);
        byte newVal = (byte) ((oldVal >> 1) & 0x7F);
        if (getCarryFlag()) {
            newVal = (byte) (newVal | 0x80);
        }
        setCarryFlag(oldBit0);
        setZeroFlag(newVal == 0);
        setNegativeFlag(isBitSet(newVal, 7));
        if (destAddress == ACCUMULATOR_ADDRESS) {
            setAccumulator(newVal);
        } else {
            _mem.setMemory(destAddress, newVal, memoryWatchMode);
        }
        incrementProgramCounter(opcode.getLength());
    }

    /*
     * Return from Interrupt. Similar to RTS but +1 is not added to PC
     *Logic:
    SP = SP - 1
    P = bPeek(SP)
    SP = SP - 1
    l = bPeek(SP)
    SP = SP - 1
    h = bPeek(SP)<<8
    PC = h|l
     */
    private void doRTI(OpCode6502 opcode) {
        // set process state flags
        setFlags(popByteFromStack());
        int lowByte = popByteFromStack() & 0xFF;
        int highByte = popByteFromStack() & 0xFF;
        int address = ((highByte << 8) | lowByte) & 0xFFFF;
        setProgramCounter(address);
    }
    /* Return from subreoutine
    Logic:
    SP = SP + 1
    l = bPeek(SP)
    SP = SP + 1
    h = bPeek(SP)<<8
    PC = (h|l) +1
     *A word (16-bits) is PulLed from the top of the Stack; this value is then incremented by one and placed in the Program Counter (PC).
     * RTS is normally used to return from a Subroutine called by the JSR instruction. This way they act as the classic "GOSUB" and "RETURN" statements.
     */

    private void doRTS(OpCode6502 opcode) {
        int lowByte = popByteFromStack() & 0xFF;
        int highByte = popByteFromStack() & 0xFF;
        int address = ((highByte << 8) | lowByte) + 1;
        if (address > 0xFFFF) {
            System.err.println("RTS address wraparound occurred");
        }
        address = address & 0xFFFF;
        setProgramCounter(address);
    }

    /* Subtract Memory from A with Borrow
     *Logic:
    IF (P.D)
    t = bcd(A) - bcd(M) - !P.C
    P.V = (t>99 OR t<0) ? 1:0
    ELSE
    t = A - M - !P.C
    P.V = (t>127 OR t<-128) ? 1:0
    P.C = (t>=0) ? 1:0
    P.N = t.7
    P.Z = (t==0) ? 1:0
    A = t & 0xFF
     */
    // 0C,OK,75,45,49,4D,70,AD,E7
    private void doSBC(byte value, OpCode6502 opcode) {
        doADC((byte) (value ^ 0xFF), opcode);
    }

    /*
     *Set carry flag
    Logic:
    P.C = 1
     */
    private void doSEC(OpCode6502 opcode) {
        setCarryFlag(true);
        incrementProgramCounter(opcode.getLength());
    }
    /*
     *Set decimal flag
    Has no effect on a NES
    Logic: P.D = 1
     */

    private void doSED(OpCode6502 opcode) {
        setDecimalFlag(true);
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Set Interrupt (disable) Flag (P.I)
     */
    private void doSEI(OpCode6502 opcode) {
        setInterruptFlag(true);
        incrementProgramCounter(opcode.getLength());
        cliLatency = true;
    }

    /*
     *Store A in Memory
     */
    private void doSTA(int address, OpCode6502 opcode) {
        _mem.setMemory(address, getAccumulator(), memoryWatchMode);
        incrementProgramCounter(opcode.getLength());
    }
    /*
     *Store X in Memory
     */

    private void doSTX(int address, OpCode6502 opcode) {
        _mem.setMemory(address, getXRegister(), memoryWatchMode);
        incrementProgramCounter(opcode.getLength());
    }
    /*
     *Store Y in Memory
     */

    private void doSTY(int address, OpCode6502 opcode) {
        _mem.setMemory(address, getYRegister(), memoryWatchMode);
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Transfer A to X
    Logic:
    X = A
    P.N = X.7
    P.Z = (X==0) ? 1:0
     */
    private void doTAX(OpCode6502 opcode) {
        byte val = (byte) getAccumulator();
        setXRegister(val);
        setNegativeFlag(isBitSet(val, 7));
        setZeroFlag(val == 0);
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Transfer A to Y
    Logic:
    Y = A
    P.N = Y.7
    P.Z = (Y==0) ? 1:0
     */
    private void doTAY(OpCode6502 opcode) {
        byte val = (byte) getAccumulator();
        setYRegister(val);
        setNegativeFlag(isBitSet(val, 7));
        setZeroFlag(val == 0);
        incrementProgramCounter(opcode.getLength());
    }

    /*
     *Transfer Stack Pointer to X
    Logic:
    X = SP
    P.N = X.7
    P.Z = (X==0) ? 1:0
    
     * TSX is the only way to retrieve the current position of the Stack Pointer.  
     *The Stack can ONLY exist in Page 1 of memory (addresses $01'00..$01'FF)  
     */
    private void doTSX(OpCode6502 opcode) {
        byte x = getStackPointer();
        setXRegister(x);
        setNegativeFlag(isBitSet(x, 7));
        setZeroFlag(x == 0);
        incrementProgramCounter(opcode.getLength());
    }

    /*
    Logic:
    A = X
    P.N = A.7
    P.Z = (A==0) ? 1:0
     */
    private void doTXA(OpCode6502 opcode) {
        byte srcVal = (byte) getXRegister();
        setAccumulator(srcVal);
        setNegativeFlag(isBitSet(srcVal, 7));
        setZeroFlag(srcVal == 0);
        incrementProgramCounter(opcode.getLength());
    }
    /*
    SP = X
    Although many instructions modify the value of the Stack Pointer, TXS is the only way to set it to a specified value.
    The Stack can ONLY exist in Page 1 of memory (addresses $01'00..$01'FF)
     */

    private void doTXS(OpCode6502 opcode) {
        byte srcVal = (byte) getXRegister();
        setStackPointer(srcVal);
        incrementProgramCounter(opcode.getLength());
    }
    /*
    Logic:
    A = Y
    P.N = A.7
    P.Z = (A==0) ? 1:0
     */

    private void doTYA(OpCode6502 opcode) {
        byte srcVal = (byte) getYRegister();
        setAccumulator(srcVal);
        setNegativeFlag(isBitSet(srcVal, 7));
        setZeroFlag(srcVal == 0);
        incrementProgramCounter(opcode.getLength());
    }

    public void doBRK(OpCode6502 opcode) {
        int address = _mem.determineAddress(Architecture6502.IRQ_VECTOR_VALUE);
        setControllerUpdatesMode(false);
        int tempPC = getProgramCounter() + opcode.getLength() + 1; // address of next instruction (-1)
        byte lowByte = (byte) (tempPC & 0xFF);
        byte highByte = (byte) ((tempPC >> 8) & 0xFF);
        pushByteOnStack(highByte);
        pushByteOnStack(lowByte);
        pushByteOnStack((byte) (getFlags() | Architecture6502.BREAK_COMMAND_FLAG_MASK));
        // set interrupt flag
        setInterruptFlag(true);
        setProgramCounter(address);
        setControllerUpdatesMode(true);
        _numCyclesRemaining = 7;
    }

    // unofficial
    private void doAAC(byte value, OpCode6502 opcode) {
        byte newA = (byte) (getAccumulator() & value);
        setAccumulator(newA);
        setNegativeFlag(isBitSet(newA, 7));
        setZeroFlag(newA == 0);
        setCarryFlag(isBitSet(newA, 7));
        incrementProgramCounter(opcode.getLength());
    }

    // unofficial
    private void doASR(byte value, OpCode6502 opcode) {
        byte newA = (byte) (getAccumulator() & value);
        boolean isCarry = ((newA & 0x1) == 0x1);
        byte newVal = (byte) ((newA >> 1) & 0x7F);
        setAccumulator(newVal);
        setCarryFlag(isCarry);
        setNegativeFlag(isBitSet(newVal, 7));
        setZeroFlag(newVal == 0);
        incrementProgramCounter(opcode.getLength());
    }
    // unofficial
    /*
     * AND byte with accumulator, then rotate one bit right in accumulator and check bit 5 and 6:
    If both bits are 1: set C, clear V.
    If both bits are 0: clear C and V.
    If only bit 5 is 1: set V, clear C.
    If only bit 6 is 1: set C and V.
    Status flags: N,V,Z,C
     */

    private void doARR(byte value, OpCode6502 opcode) {
        byte newVal = (byte) ((((getAccumulator() & value) >> 1) & 0x7F) | ((getCarryFlag()) ? 0x80 : 0x00));
        setAccumulator(newVal);
        int v = ((newVal >> 5) & 0x3);
        switch (v) {
            case 0: // bit5 and bit 6 are clear
                setCarryFlag(false);
                setOverflowFlag(false);
                break;
            case 1: // bit5 set and bit 6 clear
                setCarryFlag(false);
                setOverflowFlag(true);
                break;
            case 2: // bit5 clear and bit 6 set
                setCarryFlag(true);
                setOverflowFlag(true);
                break;
            case 3: // bit5 and bit 6 are clear
                setCarryFlag(true);
                setOverflowFlag(false);
                break;
        }
        setNegativeFlag(isBitSet(newVal, 7));
        setZeroFlag(newVal == 0);
        incrementProgramCounter(opcode.getLength());
    }

    // unofficial
    //  AND byte with accumulator, then transfer accumulator to X register. Status flags: N,Z
    private void doATX(byte value, OpCode6502 opcode) {
        // ATX on the NES simply takes the value and stores it in both A and X.
        // there is no AND involved
        setAccumulator(value);
        setXRegister(value);
        setNegativeFlag(isBitSet(value, 7));
        setZeroFlag(value == 0);
        incrementProgramCounter(opcode.getLength());
    }

    // AXS, SBX, ASX
    // unofficial
    // http://www.cc65.org/doc/ca65-4.html
    // http://www.ffd2.com/fridge/docs/6502-NMOS.extra.opcodes
    // http://nocash.emubase.de/everynes.htm
        /*
    SBX #$5A        ;CB 5A
    Equivalent instructions:
    1) STA $02
    2) TXA
    3) AND $02
    4) SEC
    5) SBC #$5A
    6) TAX
    7) LDA $02
     */
    private void doAXS(byte value, OpCode6502 opcode) {
        int newVal = ((getXRegister() & getAccumulator()) & 0xFF) + ((value ^ 0xFF) & 0xFF) + 1;
        byte newX = (byte) (newVal & 0xFF);

        boolean cFlag = (newVal > 0xFF);
        boolean nFlag = isBitSet(newX, 7);
        boolean zFlag = (newX == 0);

        setXRegister(newX);

        setCarryFlag(cFlag);
        setNegativeFlag(nFlag);
        setZeroFlag(zFlag);
        incrementProgramCounter(opcode.getLength());
    }

    private void doSLO(int destAddress, OpCode6502 opcode) {
        // ASLs the contents of a memory location and then ORs the result with the accumulator
        // SLO: {adr}:={adr}*2; A:=A or {adr};
        byte oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        byte newVal = (byte) ((oldVal << 1) & 0xFE);
        byte newA = (byte) (getAccumulator() | newVal);

        _mem.setMemory(destAddress, newVal, memoryWatchMode);
        setAccumulator(newA);

        setCarryFlag(isBitSet(oldVal, 7));
        setNegativeFlag(isBitSet(newA, 7));
        setZeroFlag(newA == 0);
        incrementProgramCounter(opcode.getLength());
    }
    private void doSYA(int destAddress, OpCode6502 opcode) {
        //AND Y register with the high byte of the target address of the
        // argument + 1. Store the result in memory.
        // M = Y AND HIGH(arg) + 1
        byte newVal = (byte)((getYRegister() & ((destAddress>>8) + 1))& 0xFF);
        _mem.setMemory(destAddress, newVal, memoryWatchMode);
        incrementProgramCounter(opcode.getLength());

        // Note: this test fails due to improper handling of a page boundary case
    }
    private void doSXA(int destAddress, OpCode6502 opcode) {
        //AND X register with the high byte of the target address of the
        // argument + 1. Store the result in memory.
        // M = X AND HIGH(arg) + 1
        byte newVal = (byte)((getXRegister() & ((destAddress>>8) + 1))& 0xFF);
        _mem.setMemory(destAddress, newVal, memoryWatchMode);
        incrementProgramCounter(opcode.getLength());

        // Note: this test fails due to improper handling of a page boundary case
    }

    private void doRLA(int destAddress, OpCode6502 opcode) {
        // RLA  {adr}:={adr}rol; A:=A and {adr};
        byte oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        boolean oldBit7 = isBitSet(oldVal, 7);
        boolean oldCarry = getCarryFlag();
        byte newVal = (byte) ((oldVal << 1) & 0xFE); // clears the last bit
        if (oldCarry) {
            newVal = (byte) (newVal | 0x01); // set the last bit
        }
        byte newA = (byte) (getAccumulator() & newVal);

        _mem.setMemory(destAddress, newVal, memoryWatchMode);
        setAccumulator(newA);

        setCarryFlag(oldBit7);
//        setZeroFlag(newVal == 0);
//        setNegativeFlag(isBitSet(newVal, 7));

        setNegativeFlag(isBitSet(newA, 7));
        setZeroFlag(newA == 0);

        incrementProgramCounter(opcode.getLength());
    }

    private void doSRE(int destAddress, OpCode6502 opcode) {
        // SRE: {adr}:={adr}/2; A:=A xor {adr};
        byte oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        boolean oldBit1 = ((oldVal & 0x1) == 0x1);
        byte newVal = (byte) ((oldVal >> 1) & 0x7F);
        byte newA = (byte) (getAccumulator() ^ newVal);

        _mem.setMemory(destAddress, newVal, memoryWatchMode);
        setAccumulator(newA);

        setCarryFlag(oldBit1);
        setNegativeFlag(isBitSet(newA, 7));
        setZeroFlag(newA == 0);

        incrementProgramCounter(opcode.getLength());
    }

    private void doRRA(int destAddress, OpCode6502 opcode) {
        // RRA: {adr}:={adr}ror; A:=A adc {adr};
        byte oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        boolean oldCarry = getCarryFlag();
        byte newByte = (byte) ((((oldVal&0xFF) >> 1) | (oldCarry ? 0x80 : 0x00)) & 0xFF);



        _mem.setMemory(destAddress, newByte, memoryWatchMode);
        setCarryFlag(((oldVal & 0x1) == 0x1));

        // do an ADC
        byte oldA = getAccumulator();
        oldCarry = getCarryFlag();
        int newVal = oldA + newByte; // Note: the bytes auto convert using 2s complement which helps for the overflow check
        if (oldCarry) {
            newVal++;
        }
        int carryCheck = (oldA & 0xFF) + (newByte & 0xFF) + (oldCarry ? 1 : 0);
        setCarryFlag(carryCheck > 0xFF);
        byte newB = (byte) (newVal & 0xFF);
        setOverflowFlag(newVal < -128 || newVal > 127); // since range is -128 to +127
        setNegativeFlag(isBitSet(newB, 7));
        setZeroFlag(newB == 0);
        setAccumulator((byte) (newVal & 0xFF));

        incrementProgramCounter(opcode.getLength());
    }

    private void doAAX(int destAddress, OpCode6502 opcode) {
        // AAX: {adr}:=A and X;
        _mem.setMemory(destAddress, (byte) (getAccumulator() & getXRegister()), memoryWatchMode);
        incrementProgramCounter(opcode.getLength());
    }

    private void doLAX(int destAddress, OpCode6502 opcode) {
        // LAX: A,X:={adr};
        byte newVal = _mem.getMemory(destAddress, memoryWatchMode);
        setAccumulator(newVal);
        setXRegister(newVal);
        setNegativeFlag(isBitSet(newVal, 7));
        setZeroFlag(newVal == 0);
        incrementProgramCounter(opcode.getLength());
    }

    private void doDCP(int destAddress, OpCode6502 opcode) {
        // DCP: {adr}:={adr}-1; CMP{adr};
        
        byte oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        byte newVal = (byte)(((oldVal & 0xFF) - 1) & 0xFF);
        _mem.setMemory(destAddress, newVal, memoryWatchMode);

        byte regVal = (byte) getAccumulator();
        byte diff = (byte) ((regVal - newVal) & 0xFF);
        setCarryFlag((regVal & 0xFF) >= (newVal & 0xFF));
        setZeroFlag(regVal == newVal);
        setNegativeFlag(isBitSet(diff, 7));


        incrementProgramCounter(opcode.getLength());
    }

    private void doISC(int destAddress, OpCode6502 opcode) {
        // ISC: {adr}:={adr}+1; SBC(adr)
        byte oldVal = _mem.getMemory(destAddress, memoryWatchMode);
        byte newMem = (byte)(((oldVal & 0xFF) + 1) & 0xFF);
        _mem.setMemory(destAddress, newMem, memoryWatchMode);

        // invert for the SBC to an ADC
        byte tmpByte = (byte)(newMem ^ 0xFF);
        byte oldA = getAccumulator();
        boolean oldCarry = getCarryFlag();
        int newVal = oldA + tmpByte; // Note: the bytes auto convert using 2s complement which helps for the overflow check
        if (oldCarry) {
            newVal++;
        }
        int carryCheck = (oldA & 0xFF) + (tmpByte & 0xFF) + (oldCarry ? 1 : 0);
        setCarryFlag(carryCheck > 0xFF);
        byte newB = (byte) (newVal & 0xFF);
        setOverflowFlag(newVal < -128 || newVal > 127); // since range is -128 to +127
        setNegativeFlag(isBitSet(newB, 7));
        setZeroFlag(newB == 0);

        setAccumulator((byte) (newVal & 0xFF));
        incrementProgramCounter(opcode.getLength());

    }

    private void doKIL(OpCode6502 opcode) {
        setProgramCounter((_programCounter - 1) & 0xFFFF);
    }


    public void doInterrupt(int address) {
        doInterrupt(address, true);
    }

    public void doInterrupt(int address, boolean writeToStack) {
        setControllerUpdatesMode(false);
        if (writeToStack) {
            int tempPC = getProgramCounter();
            byte lowByte = (byte) (tempPC & 0xFF);
            byte highByte = (byte) ((tempPC >> 8) & 0xFF);
            pushByteOnStack(highByte);
            pushByteOnStack(lowByte);
            pushByteOnStack(getFlags());
        } else {
            // special case since RESET interrupt is not like normal interrupts, since it does not write to stack, but does change stack pointer
            decrementStackPointer();
            decrementStackPointer();
            decrementStackPointer();
        }
        setInterruptFlag(true);
        setProgramCounter(address);
        setControllerUpdatesMode(true);
        _numCyclesRemaining = 7;
    }
}
