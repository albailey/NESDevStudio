/*
 * ROMDisassembler.java
 *
 * Created on November 16, 2006, 12:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.romLoader.disassembler;

import java.awt.Component;
import java.io.File;
import java.io.PrintStream;

import ui.romLoader.disassembler.formatter.ASMFormatter;
import ui.romLoader.disassembler.formatter.ca65.CA65Formatter;
import utilities.ByteFormatter;
import utilities.FileUtilities;
import emulator.core.CPU6502.Architecture6502;
import emulator.core.CPU6502.CPU6502;
import emulator.core.CPU6502.Instruction6502;
import emulator.core.CPU6502.Utilities6502;
import emulator.core.CPU6502.mvc.CPU6502ControllerInterface;
import emulator.nes.APU;
import emulator.nes.INES_ROM_INFO;
import emulator.nes.MemoryManager;
import emulator.nes.NESRom;
import emulator.nes.PPU;
import emulator.nes.controllers.Controllers;

/**
 * Here is how Disassembly works.
 * Input PRG ROM(s)
 * The way in which code is executed in the NES is driven through its handler vectors
 * NMI Handler defined at 0x3FFA
 * Reset Handler defined at 0x3FFC
 * IRQ Handler defined at 0x3FFE
 *
 * So we need to walk the ROM starting at the values specified at those 3 pointers.
 * You then need all the OPCode data, etc..   These are available online (or in a 6502 book)
 * Afterwards, walk the code...
 *
 * Hopefully, the output will be re-assembleable though ca65
 *
 * @author abailey
 */
public class ROMDisassembler implements Runnable {
    
    public final static int MAX_TRAVERSALS = 8;
    
    // all romData entries are initially zero: ie: unknown
    public final static int UNKNOWN_TYPE = 0x00;
    // if the entry is a length 1 opcode (ie: SEI)
    //public final static int UNARY_OPCODE_TYPE = 0x01;
    // if the entry is a length 2 opcode (ie: BNE $35)
    //public final static int BINARY_OPCODE_TYPE = 0x02;
    // if the entry is a length 3 opcode (ie: JMP $2020)
    //public final static int TRINARY_OPCODE_TYPE = 0x04;
    
    // I dont need that level of detail for my opcodes
    public final static int OPCODE_TYPE = 0x01;
    
    // if the entry is an address for a binary or trinary opcode
    public final static int OPERAND_ADDR_TYPE = 0x08;
    // if the entry is a hex value for a binary or trinary opcode
    public final static int OPERAND_VALUE_TYPE = 0x10;
    // if the entry is a vector value
    public final static int VECTOR_TYPE = 0x20;
    // if the entry is pure data
    public final static int DATA_TYPE = 0x40;
    // if the entry should be flagged as a label
    public final static int LABEL_TYPE = 0x80;
    
    // input
    private byte _romData[] = null;
    // classification of the romData input
    private int _types[];
    private int _count[];
    private int _memSets[];
    private int _lowLabels[];
    private int _highLabels[];
//    private int _jsrRets[];
    
    private final static int MEMSET_A_REG = 0xFF+1;
    private final static int MEMSET_X_REG = 0xFF+2;
    private final static int MEMSET_Y_REG = 0xFF+3;
    
    
    private ASMFormatter _formatter;
    
    private Thread _thread = null;
    private DisassemblerUIInterface _uiInterface = null;
    private CPU6502ControllerInterface _controller = null;
    private File _selectedDir = null;
    private boolean _runCompleted = false;
    private boolean _running = false;
    private NESRom _rom = null;
    
    
    /** Creates a new instance of ROMDisassembler */
    public ROMDisassembler() {
        _formatter = new CA65Formatter();
    }
    
    public byte[] getRomData(){
        return _romData;
    }
    public int[] getTypes(){
        return _types;
    }
    public int[] getLowLabelAddresses(){
        return _lowLabels;
    }
    public int[] getHighLabelAddresses(){
        return _highLabels;
    }
    
    public void setDisassemblerUI(DisassemblerUIInterface ui){
        _uiInterface = ui;
    }
    
    public static boolean isLabel(int val) {
        return ((val & LABEL_TYPE) ==  LABEL_TYPE);
    }
    public static boolean isDataType(int val) {
        return ((val & DATA_TYPE) ==  DATA_TYPE);
    }
    public static boolean isOpCode(int val) {
        return ((val & OPCODE_TYPE) ==  OPCODE_TYPE);
    }
    public static boolean isVector(int val) {
        return ((val & VECTOR_TYPE) ==  VECTOR_TYPE);
    }
    public static boolean hasBeenProcessed(int val) {
        // if its anything other than a label type, its been processed
        // 0111111 = 7F
        return ((val & 0x7F) > UNKNOWN_TYPE);
    }
    
    
    public boolean spewPRGBank(PrintStream outStream, byte inputData[], Component parentComponent){
        if(inputData.length != INES_ROM_INFO.PRG_BANK_SIZE){
            System.err.println("Only able to disassemble individual PRG banks ");
            return false;
        }
        outStream.println("PRG Contents");
        for(int i=0;i<INES_ROM_INFO.PRG_BANK_SIZE;i+=16){
            System.out.print(ByteFormatter.formatInt(i) + "\t");
            for(int j=0;j<16;j++){
                outStream.print("[" + ByteFormatter.formatByte(inputData[i+j]) + "]");
            }
            outStream.println("");
        }
        return true;
    }

    public boolean spewPRGBankDIFF(PrintStream outStream, int offset, byte inputData[], byte inputData2[], Component parentComponent){
        if(inputData.length != INES_ROM_INFO.PRG_BANK_SIZE || inputData2.length != inputData.length ){
            System.err.println("Only able to disassemble individual PRG banks ");
            return false;
        }
        outStream.println("PRG Contents");
        for(int i=0;i<INES_ROM_INFO.PRG_BANK_SIZE;i+=16){
            outStream.print(ByteFormatter.formatInt(i+offset) + "        ");
            for(int j=0;j<16;j++){
                outStream.print("[" + ByteFormatter.formatByte(inputData[i+j]) + "]");
            }
            outStream.print("        ");
            for(int j=0;j<16;j++){
                outStream.print("[" + ByteFormatter.formatByte(inputData2[i+j]) + "]");
            }
           outStream.print("        ");
            for(int j=0;j<16;j++){
                if(inputData[i+j] != inputData2[i+j]){
                    outStream.print("{" + ByteFormatter.formatSingleByteInt(j) + "}");
                }
            }
            
            outStream.println("");
        }
        return true;
    }
    
    
    public boolean disassembleROM(CPU6502ControllerInterface controller, NESRom rom , Component parentComponent){
        if(_running) {
            stopRunning();
            try { Thread.sleep(500); } catch(Exception e) { e.printStackTrace(); }
            if(!_runCompleted){
                System.err.println("Preview Disassemble still running");
                return false;
            }
        }
        
        _selectedDir = null;
        _rom = rom;
        if(rom.getNumPRGBanks() < 1) {
            System.err.println("Nothing to disassemble ");
            return false;
        }
        if(rom.getNumPRGBanks() > 2) {
            System.err.println("Unable to disassemble ROMs with more than 2 16K PRG banks ");
            return false;
        }
        _selectedDir = FileUtilities.selectDirectoryForSave(parentComponent);
        
        if(_selectedDir == null){
            System.err.println("No directory selected");
            return false;
        }
        
        
        System.out.println("Starting disassembly");
        
        // make a copy for the class
        _romData = new byte[0xFFFF + 1];
        if(rom.getNumPRGBanks() == 1){
            byte b[] = rom.getPRGBank(0);
            System.arraycopy(b,0,_romData,Architecture6502.PRG_BANK0_OFFSET,b.length);
            System.arraycopy(b,0,_romData,Architecture6502.PRG_BANK1_OFFSET,b.length);
        } else {
            byte b[] = rom.getPRGBank(0);
            System.arraycopy(b,0,_romData,Architecture6502.PRG_BANK0_OFFSET,b.length);
            b = rom.getPRGBank(1);
            System.arraycopy(b,0,_romData,Architecture6502.PRG_BANK1_OFFSET,b.length);
            
        }
        _types = new int[_romData.length]; // array is initially set to zero: ie: unknown
        _count = new int[_romData.length];  // initially zero
        _lowLabels= new int[_romData.length]; // array is initially set to zero: ie: unused
        _highLabels= new int[_romData.length]; // array is initially set to zero: ie: unused
//        _jsrRets = new int[_romData.length];
        _memSets = new int[0xFF + 4];
        _controller = controller;
        _thread = new Thread(this);
        _thread.start();
        return true;
    }
    
        
    private boolean recursivelyDisassemble(CPU6502 cpu, boolean labelMode ){
        boolean running =  true;
        boolean setAsLabel = labelMode;

        while(_running && running){
            int address = cpu.getProgramCounter();

            if(_uiInterface != null){
                int action = _uiInterface.getNextUserAction();
                switch(action){
                    case DisassemblerUIInterface.DISASSEMBLER_STOP_ACTION:
                        _running = false;
                        continue;
                    case DisassemblerUIInterface.DISASSEMBLER_SEEK_ACTION:
                        if (address == _uiInterface.getSeekAddress()){
                           _uiInterface.pause();
                           continue;
                        }
                        break;
                    case DisassemblerUIInterface.DISASSEMBLER_CONTINUE_ACTION:
                        break;
                    default: System.err.println("Unsupported UI action:" + action);
                    _running = false;
                    continue;
                }
            }
            // even if we have processed the data at that address, we still flag it as a label
            // this is because we could be branching/jumping to a point we have already processed (but not labelled as a branch)
            if(setAsLabel){
                _types[address] |= LABEL_TYPE;
            }
            setAsLabel = false;

            // determine the instruction at the current process counter
            Instruction6502 result = cpu.getInstructionAtProcessCounter();
            
            if(result == null) {
                System.err.println("Terminating Disassembly of this branch due to invalid instruction at: " + ByteFormatter.formatInt(address) + " value was:" + ByteFormatter.formatByte(_romData[address]));
                // _running = false;
                return false;
            }

            // FLAW ALERT!!!
            // If I have already processed this, I need to skip OVER it in the case of a JSR.
            // the reason being, the JSR may have a RTS, so I should skip to the next instruction
            // if we have already processed this, we can stop
/*            if(hasBeenProcessed(_types[address]) ) {

                if((result.getOpCode().isJump() && result.getOpCode().isAltersStack()) || (result.getOpCode().isReturn() && result.getOpCode().isAltersStack())){
                    _processCnt[address]++;
                    if(_processCnt[address] > MAX_TRAVERSALS) {
                        System.out.println("Already processed:" + ByteFormatter.formatInt(address) + result.toString());
                        return true;                        
                    }
                } else {
                    if(result.getOpCode().isBranch() || result.getOpCode().isJump()) {
                        System.out.println("Already processed:" + ByteFormatter.formatInt(address) + result.toString());
                        return true;                        
                    }
                }
 
                System.out.println("Already processed:" + ByteFormatter.formatInt(address) + result.toString());
                return true;
            }
*/
            

            _types[address] |= OPCODE_TYPE;
            boolean loadsData = result.getOpCode().loadsData();
            boolean storesData = result.getOpCode().storesData();
            boolean usesA = result.getOpCode().usesARegister();
            boolean usesX = result.getOpCode().usesXRegister();
            boolean usesY = result.getOpCode().usesYRegister();
            
            // maybe it has an operand with it
            // better process info about that too....
            int operand = address;
            switch (result.getOpCode().getAddressMode()) {
                case Architecture6502.ABSOLUTE_MODE:
                    operand = Utilities6502.calculate16BitAddress(_romData[address+1], _romData[address+2]);
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    _types[address+2] |= OPERAND_ADDR_TYPE;
                    _types[operand] |= LABEL_TYPE;
                    if(loadsData && operand >= 0x8000){
                        _types[operand] |= DATA_TYPE;
                    }
                    break;
                case Architecture6502.IMMEDIATE_MODE:
                    for(int i=1;i<result.getEntireLength();i++){
                        _types[address+i] |= OPERAND_VALUE_TYPE;
                    }
                    if(loadsData){
                        if(usesA){
                            _memSets[MEMSET_A_REG] = address;
                        }
                        if(usesX){
                            _memSets[MEMSET_X_REG] = address;
                        }
                        if(usesY){
                            _memSets[MEMSET_Y_REG] = address;
                        }
                    }
                    break;
                case Architecture6502.IMPLICIT_MODE:
                    // len = 1
                    break;
                case Architecture6502.ACCUMULATOR_MODE:
                    // len = 1
                    break;
                case Architecture6502.ABSOLUTE_INDEXED_X_MODE:
                    operand = Utilities6502.calculate16BitAddress(_romData[address+1], _romData[address+2]);
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    _types[address+2] |= OPERAND_ADDR_TYPE;
                    _types[operand] |= LABEL_TYPE;
                    if(loadsData && operand >= 0x8000){
                        _types[operand] |= DATA_TYPE;
                    }
                    break;
                case Architecture6502.ABSOLUTE_INDEXED_Y_MODE:
                    operand = Utilities6502.calculate16BitAddress(_romData[address+1], _romData[address+2]);
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    _types[address+2] |= OPERAND_ADDR_TYPE;
                    _types[operand] |= LABEL_TYPE;
                    if(loadsData && operand >= 0x8000){
                        _types[operand] |= DATA_TYPE;
                    }
                    break;
                case Architecture6502.ZP_MODE:
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    if(storesData){
                        int dest = _romData[address+1] & 0xFF;
                        if(usesA){
                            _memSets[dest] = _memSets[MEMSET_A_REG];
                        }
                        if(usesX){
                            _memSets[dest] = _memSets[MEMSET_X_REG];
                        }
                        if(usesY){
                            _memSets[dest] = _memSets[MEMSET_Y_REG];
                        }
                    }
                    break;
                case Architecture6502.ZP_INDEXED_X_MODE:
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    break;
                case Architecture6502.ZP_INDEXED_Y_MODE:
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    break;
                case Architecture6502.INDIRECT_ABSOLUTE_MODE:
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    _types[address+2] |= OPERAND_ADDR_TYPE;
                    break;
                case Architecture6502.INDEXED_INDIRECT_X_MODE:
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    break;
                case Architecture6502.INDIRECT_INDEXED_Y_MODE:
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    int destByte = _romData[address+1] & 0xFF;
                    operand = cpu.getAddressFromROMData(_romData[address+1] & 0xFF);
        //            System.err.println("INDIRECT_INDEXED_Y_MODE operating on:" + ByteFormatter.formatSingleByteInt(destByte) + " value of " + ByteFormatter.formatInt(operand));
                    if(loadsData && operand >= 0x8000){
                        _types[operand] |= DATA_TYPE;
                        _types[operand] |= LABEL_TYPE;
                        int updateAddrLow = _memSets[destByte];
                        int updateAddrHigh = _memSets[destByte+1];
                        if(updateAddrLow == 0 || updateAddrHigh == 0){
                            System.err.println("INDIRECT_INDEXED_Y_MODE unaccounted for memory:" + ByteFormatter.formatSingleByteInt(destByte));
                        } else {
                            _lowLabels[updateAddrLow] = operand;
                            _highLabels[updateAddrHigh] = operand;
                        }
                    }
                    break;
                case Architecture6502.RELATIVE_MODE:
                    _types[address+1] |= OPERAND_ADDR_TYPE;
                    operand = address + _romData[address+1] + result.getEntireLength(); // entireLength is 2
                    _types[operand] |= LABEL_TYPE;
                    break;
                default:
                    break;
            }
            
            if(result.getOpCode().isBranch()) {
                _count[operand]++;
                _count[address + result.getEntireLength()]++; 
    
                CPU6502 curState = null; 
                CPU6502ControllerInterface controller = null;
    
                if( _count[operand] < MAX_TRAVERSALS ) {
                    curState = (CPU6502)cpu.makeCopy();
                    controller = cpu.getCPUController();
                }
                cpu.setProgramCounter(address + result.getEntireLength());
                recursivelyDisassemble(cpu, true); 

                if( curState != null ) {                
            //        System.err.println("Branch at:" + ByteFormatter.formatInt(address) + " to: " + ByteFormatter.formatInt(operand));
                    cpu = curState;
                    controller.setCPU6502(cpu);
                    cpu.setProgramCounter(operand);
                    recursivelyDisassemble(cpu, true); // recursively process the branch
                }
                
         //   } else if(result.getOpCode().isJump() && result.getOpCode().isAltersStack() && hasBeenProcessed(_types[operand])){ 
            } else if(result.getOpCode().isJump()) { //  && result.getOpCode().isAltersStack() ) {
                if(result.getOpCode().getAddressMode() == Architecture6502.INDIRECT_ABSOLUTE_MODE){
                    byte op0 = _romData[address+1];
                    byte op1 = _romData[address+2] ;
                    int testOp = Utilities6502.calculate16BitAddress(op0,op1);
                    byte lowByte = _romData[testOp];
                    byte highByte = _romData[testOp+1] ;  
                    int testOp2 =  Utilities6502.calculate16BitAddress(lowByte, highByte);    
                    if(testOp < 0xFF){
                        // we need to query from memory
                        System.err.println("JSR indirect based on zero page");
                        testOp2 = cpu.getAddressFromROMData(testOp);
                    }
                     _count[testOp2]++;
                    if(_count[testOp2] > MAX_TRAVERSALS) {
                        return false;
                    }
                    System.err.println("JSR indirect with address:" +  ByteFormatter.formatInt(testOp) + " to location:" +  ByteFormatter.formatInt(testOp2));
                } else {
                    _count[operand]++;
                    if(_count[operand] > MAX_TRAVERSALS) {
                        return false;
                    }   
                }
                running = cpu.processNextInstruction();

            } 
  /*           else if(result.getOpCode().isJump() ) {
                 if( hasBeenProcessed(_types[operand])){
                    return false;
                }
                 System.err.println("JUMP at:" + ByteFormatter.formatInt(address));
                 running = cpu.processNextInstruction();
            }*/  else {
               running = cpu.processNextInstruction();
            }
        }
        return false;
    }
    
    public boolean generateOutput(){
        
        try {
            _formatter.outputProject(_selectedDir,  _rom, this);
        } catch(Exception e){
            System.err.println("Could not save project: " + _selectedDir);
            return false;
        }
        
        return true;
    }
    
    
    public void run() {
        _running = true;
        _runCompleted = false;
        // Here is how this works.
        // every byte in the ENTIRE rom is initially UNKNOWN
        
        // I can flag some stuff as STACK, MEMORY, CHR, etc...
        
        // the three VECTORs (IRQ, NMI, RESET) are the starting points
        // so I recursively traverse from those.
        
        // Get destination for the vectors
        int nmiAddress = Utilities6502.determineAddress(Architecture6502.NMI_VECTOR_VALUE, _romData);
        System.out.println("NMI address:" + ByteFormatter.formatInt(nmiAddress));
        int resetAddress = Utilities6502.determineAddress(Architecture6502.RESET_VECTOR_VALUE,  _romData);
        System.out.println("Reset address:" + ByteFormatter.formatInt(resetAddress));
        int irqAddress = Utilities6502.determineAddress(Architecture6502.IRQ_VECTOR_VALUE,  _romData);
        System.out.println("IRQ address:" + ByteFormatter.formatInt(irqAddress));
        
        PPU ppu = new PPU();
        APU apu = new APU();
        Controllers joypad = new Controllers();
        
        MemoryManager memoryManager = new MemoryManager(ppu, apu, joypad);
        
        CPU6502 cpu = new CPU6502(memoryManager); // contructs a new model as well
        cpu.setControllerUpdatesMode(false);
        _controller.setCPU6502(cpu);
        CPU6502 origCPU = cpu.makeCopy();
        
        

            for(int i=0;i<_types.length;i++){
                _types[i] = UNKNOWN_TYPE;
            }
            
            // Since those 3 vectors are KNOWN to be vectors, I can assign them right away
            _types[Architecture6502.NMI_VECTOR_VALUE] |= VECTOR_TYPE;
             _types[Architecture6502.NMI_VECTOR_VALUE+1] |= OPERAND_ADDR_TYPE;
            _types[Architecture6502.RESET_VECTOR_VALUE] |= VECTOR_TYPE;
             _types[Architecture6502.RESET_VECTOR_VALUE+1] |= OPERAND_ADDR_TYPE;
            _types[Architecture6502.IRQ_VECTOR_VALUE] |= VECTOR_TYPE;
             _types[Architecture6502.IRQ_VECTOR_VALUE+1] |= OPERAND_ADDR_TYPE;
            
            cpu = (CPU6502)origCPU.makeCopy();
            cpu.setControllerUpdatesMode(false);
            _controller.setCPU6502(cpu);
            cpu.doInterrupt(resetAddress);
            cpu.setControllerUpdatesMode(true);
            recursivelyDisassemble(cpu, true);

            cpu = (CPU6502)origCPU.makeCopy();
            cpu.setControllerUpdatesMode(false);
            _controller.setCPU6502(cpu);    // replace the model with a copy
            cpu.doInterrupt(nmiAddress);
            cpu.setControllerUpdatesMode(true);
            recursivelyDisassemble(cpu, true);
            
            cpu = (CPU6502)origCPU.makeCopy();
            cpu.setControllerUpdatesMode(false);
            _controller.setCPU6502(cpu);
            cpu.doInterrupt(irqAddress);
            cpu.setControllerUpdatesMode(true);
            recursivelyDisassemble(cpu, true);
            
            int addrs[] = _uiInterface.getSpecialAddresses();
            for(int i=0;i<addrs.length;i++){
                cpu = (CPU6502)origCPU.makeCopy();
                cpu.setControllerUpdatesMode(false);
                _controller.setCPU6502(cpu);
                cpu.setControllerUpdatesMode(true);
                cpu.setProgramCounter(addrs[i]);
                recursivelyDisassemble(cpu, true);  //auto sets it as a label
            }
        
        // done disassembling, lets format the output
        System.out.println("Saving to FILE");
        generateOutput();
        _running = false;
        _runCompleted = true;
        
        System.out.println("All DONE");
        
    }
    public void stopRunning(){
        _running = false;
    }
    
    
}
