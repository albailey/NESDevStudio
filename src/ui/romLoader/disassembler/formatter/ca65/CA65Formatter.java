/*
 * CA65Formatter.java
 *
 * Created on November 19, 2006, 4:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.romLoader.disassembler.formatter.ca65;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import ui.romLoader.disassembler.ROMDisassembler;
import ui.romLoader.disassembler.formatter.ASMFormatter;
import utilities.ByteFormatter;
import utilities.FileUtilities;
import emulator.core.CPU6502.Architecture6502;
import emulator.core.CPU6502.Instruction6502;
import emulator.core.CPU6502.OpCode6502;
import emulator.core.CPU6502.Utilities6502;
import emulator.nes.INES_ROM_INFO;
import emulator.nes.NESRom;

/**
 *
 * @author abailey
 */
public class CA65Formatter extends ASMFormatter {
    
     protected Properties envSettings = new Properties();
     private static final String CA65_BIN_DIR_PROP = "CA65_BIN_DIR";
     private static final String DEFAULT_CA65_BIN_DIR_PROP = "<Path to CA65 Bin Dir>";

     private static final String CA65_EXE_PROP = "CA65_EXE";
     private static final String DEFAULT_CA65_EXE_PROP = "ca65.exe";

     private static final String LD65_EXE_PROP = "LD65_EXE";
     private static final String DEFAULT_LD65_EXE_PROP = "ld65.exe";
     
     private static final String EMU_PROP = "EMU";
     private static final String DEFAULT_EMU_PROP = "<Path to emulator executable>";


     private static final String LINKER_FILE_PROP = "LINKER_FILE";
     private static final String DEFAULT_LINKER_FILE_PROP = "nes.ini"; 

     private static final String HEADER_PREFIX_PROP = "HEADER_PREFIX";
     private static final String DEFAULT_HEADER_PREFIX_PROP = "header"; 
     
     private static final String RM_PROP = "RM_COMMAND";
     private static final String DEFAULT_RM_PROP = "rm"; // command to remove files

     private static final String CONCAT_PROP = "CONCAT_COMMAND";
     private static final String DEFAULT_CONCAT_PROP = "cat"; // command to cat several files together

     private boolean codeHigh = true;
     /** Creates a new instance of CA65Formatter */
    public CA65Formatter() {
        super();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String propFileName = "ui/romLoader/disassembler/formatter/ca65/CA65Settings.properties"; 
            InputStream inStream = loader.getResourceAsStream(propFileName);
            envSettings.load(inStream);
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }

    public String getLabelPrefix(){
        return "Label_";
    }
    
    public String getVectorPrefix(){
        return ".addr";
    } 
    
    public void formatConstant(PrintStream ps, String address, String constantName){
        ps.println( constantName + " = " + address);
    }

    public void outputProject(File projectDir, NESRom nesRom, ROMDisassembler dis ) throws Exception {
        int mapping[] = dis.getTypes();
        byte romData[] = dis.getRomData();

        codeHigh = (nesRom.getNumPRGBanks() > 1);

        File theFile = null; 
        FileOutputStream fos = null;
        PrintStream dataStream = null;
           
        String prefix = "dis";
        
        // make the makefile
        formatMakefile(projectDir, nesRom, prefix);
        
        // now the linker file
        formatLinkerFile(projectDir, nesRom, prefix);

        // now create the header files(linker and header.asm)
        formatHeader(projectDir, nesRom, prefix);

        // the CHR files
        formatCHRFiles(projectDir, nesRom, prefix);
        
        // now some actual code....
        // the constants
        theFile = new File(projectDir,"constants.asm");
        fos = new FileOutputStream(theFile);
        dataStream = new PrintStream(fos);
        formatConstants(dataStream, constants);
        fos.close();
        dataStream.close();

        
        // the data files
        formatDataFiles(projectDir, romData, mapping);

        
        // the assembler code !!!!!
        theFile = new File(projectDir, prefix + ".asm");
        fos = new FileOutputStream(theFile);
        dataStream = new PrintStream(fos);
        outputASM(dataStream, dis);
        fos.close();
        dataStream.close();
        
        
    }
    
    // The guts of creating a CA65 project....
    
    
    // prefix is the prefix for all relevant files for this project.  It should not contain spaces.  
    // prefix should not be the same as the header prefix either or bad stuff happens
    public void formatMakefile(File projectDir, NESRom nesRom, String prefix) throws Exception {
        // makefile consists of some very bare items
        // get stuff from the properties CA65 properties file. Most things do not have to be in there.  Bare necessessities are:
        // 1) installation location for properly installed CA65
        // 2) Path to the emulator you plan on running with
        //
        String ca65BinPath = envSettings.getProperty(CA65_BIN_DIR_PROP, DEFAULT_CA65_BIN_DIR_PROP);
        String ca65ExePath = ca65BinPath + envSettings.getProperty(CA65_EXE_PROP, DEFAULT_CA65_EXE_PROP);
        String ld65ExePath = ca65BinPath + envSettings.getProperty(LD65_EXE_PROP, DEFAULT_LD65_EXE_PROP);
        String emuExePath = envSettings.getProperty(EMU_PROP, DEFAULT_EMU_PROP);
        String linkerFile = envSettings.getProperty(LINKER_FILE_PROP, DEFAULT_LINKER_FILE_PROP);        
        String rmCommand = envSettings.getProperty(RM_PROP, DEFAULT_RM_PROP);
        String concatCommand = envSettings.getProperty(CONCAT_PROP, DEFAULT_CONCAT_PROP);

        File theFile = new File(projectDir, "makefile");
        FileOutputStream fos = new FileOutputStream(theFile);
        PrintStream ps = new PrintStream(fos);
        
        // 1) Paths to tools (needs to be setup in properties file)
        ps.println("# Tools required. Update these to point to where they are installed");
        ps.println("# Make sure CA65 is setup properly so you have your CFLAGS setting");
        ps.println("#");
        ps.println("AS = " + ca65ExePath);
        ps.println("LD = " + ld65ExePath);
        ps.println("EMU = " + emuExePath);
        ps.println("RM = " + rmCommand);
        ps.println("CONCAT = " + concatCommand);
        
        // 2) Other variables
        ps.println("# Variables");
        ps.println("MAIN = " + prefix);
        ps.println("# Intermediate Object Files");
        ps.println("OBJS = $(MAIN).o  header.o");
        ps.println("");
 
        ps.println("");
        ps.println("# Final result (all)");
        ps.println("all:\t$(MAIN).nes");
        ps.println("");
         
        ps.println("");
        ps.println("clean:");
        ps.println("\t$(RM) $(OBJS) $(MAIN).nes $(MAIN).prg");
        ps.println("");

        ps.println("");
        ps.println("run:\t$(MAIN).nes");
        ps.println("\t$(EMU) $(MAIN).nes");
        ps.println("");
        
  /*      ps.println("");
        ps.println("# Steps for making the header");
        ps.println("");
        ps.println(headerFile + ":\t$(HEADER_OBJS)");
        ps.println("\t$(LD) $(HEADER_OBJS) -C " + headerLinkerFile + "  -o " + headerFile);
        ps.println("");
        ps.println("$(HEADER_OBJS):\t%.o: %.asm");
        ps.println("\t$(AS) $(CFLAGS) $< -o $@");
*/
        ps.println("");
        ps.println("# Steps for making the other intermediate files");
        ps.println("");
        ps.println("$(OBJS):\t%.o: %.asm");
        ps.println("\t$(AS) $(CFLAGS) $< -o $@");
        ps.println("");
        ps.println("$(MAIN).prg:\t$(OBJS)");
        ps.println("\t$(LD) $(OBJS) -C " + linkerFile + " -o $(MAIN).prg");
        ps.println("");
        ps.println("$(MAIN).nes:\t$(MAIN).prg $(MAIN).chr ");
        ps.println("\t$(CONCAT) $(MAIN).prg $(MAIN).chr > $(MAIN).nes");
        ps.println("");
        
        fos.close();
        ps.close();
    }

    public void formatLinkerFile(File projectDir, NESRom nesRom, String prefix) throws Exception {
        String linkerFile = envSettings.getProperty(LINKER_FILE_PROP, DEFAULT_LINKER_FILE_PROP);        

        File theFile = new File(projectDir, linkerFile);
        FileOutputStream fos = new FileOutputStream(theFile);
        PrintStream ps = new PrintStream(fos);        
      
        ps.println("MEMORY {");
        ps.println("\tHEADER: start = $00,  size = $0010, type = ro, file = %O;");
        ps.println("\tZP:         start = $00,    size = $100,    type = rw;");                              
        ps.println("\tRAM:        start = $200,   size = $400,    type = rw;");
        if(nesRom.getNumPRGBanks() == 1){
             ps.println("\tROM: start = $C000, size = $4000, type = ro, file = %O;");
        } else {
            ps.println("\tROM: start = $8000, size = $8000, type = ro, file = %O;");
        }
        ps.println("}");
        ps.println("");
        ps.println("SEGMENTS {");
        ps.println("\tINES_HEADER:  load = HEADER, type = ro, align = $10;");
        ps.println("\tCODE:     load = ROM, type = ro; # , align = $100;");

        if(codeHigh){
            ps.println("\tCODE_HIGH:     load = ROM, type = ro, start = $C000;");
        }
        ps.println("\tVECTORS:  load = ROM, type = ro, start = $FFFA;");
        ps.println("}");
        ps.println("");
        ps.println("FILES {");
        ps.println("\t%O: format = bin;");
        ps.println("}");
        ps.println("");

        
        fos.close();
        ps.close();
    }
    
    public void formatHeader(File projectDir, NESRom nesRom, String prefix) throws Exception {
        String headerPrefix = envSettings.getProperty(HEADER_PREFIX_PROP, DEFAULT_HEADER_PREFIX_PROP);    
        
        File theFile = new File(projectDir, headerPrefix + ".asm");
        FileOutputStream fos = new FileOutputStream(theFile);
        PrintStream ps = new PrintStream(fos);

        // first the header assembler.  This is an unneccessary step, but its nice to have a view of the header instead of just the pure binary representation
        ps.println("");
        String inesHeaderSegmentName = "INES_HEADER";
        ps.println(".segment \"" + inesHeaderSegmentName + "\"");
        ps.println(".byt \"NES\", 26"); // NES
        ps.println(".byt " + nesRom.getNumPRGBanks() + " ; number of 16 KB program segments"); 
        ps.println(".byt " + nesRom.getNumCHRBanks() + " ; number of 8 KB chr segments"); 
        ps.println(".byt " + nesRom.getBaseMapperInfo() + " ; mapper, mirroring, etc"); 
        ps.println(".byt " + nesRom.getAdditionalMapperInfo() + " ; extended mapper info"); 
        ps.println(".byt 0,0,0,0,0,0,0,0  ; the rest of the header is empty");
        ps.println("");
        fos.close();
        ps.close();
/*
        // Second the header ini file
        theFile = new File(projectDir, headerPrefix + ".ini");
        fos = new FileOutputStream(theFile);
        ps = new PrintStream(fos);
        ps.println("MEMORY {");
        ps.println("\tHEADER: start = $00, size = $0010, type = ro, file = %O;");
        ps.println("}");
        ps.println("");
        ps.println("SEGMENTS {");
        ps.println("\tINES_HEADER:  load = HEADER, type = ro, align = $10;");
        ps.println("}");
        ps.println("");
        ps.println("FILES {");
        ps.println("\t%O: format = bin;");
        ps.println("}");
        ps.println("");
        fos.close();
        ps.close();
*/
    }

    public void formatCHRFiles(File projectDir, NESRom nesRom, String prefix) throws Exception {
        // might be useful to allow the separate CHR files to be stored separately
        // not doing that now though
        
        File theFile = new File(projectDir, prefix + ".chr");
        int numChrBanks = nesRom.getNumCHRBanks();
        // what a memory waster I am...
        byte allCHR[] = new byte[INES_ROM_INFO.CHR_BANK_SIZE * numChrBanks];
        int curPos = 0;
        for(int i=0;i<numChrBanks;i++){
            System.arraycopy(nesRom.getCHRBank(i),0,allCHR, curPos, INES_ROM_INFO.CHR_BANK_SIZE); 
            curPos+=INES_ROM_INFO.CHR_BANK_SIZE;
        }
        
        FileUtilities.saveBytes(theFile,allCHR,allCHR.length);       
    }
    
    public void formatDataFiles(File projectDir, byte romData[], int mapping[]) throws Exception {
        File theFile = new File(projectDir,"data.bin");
        FileOutputStream fos = new FileOutputStream(theFile);
        PrintStream dataStream = new PrintStream(fos);
        
        System.out.println("TO DO: Implement creation of DATA files used by disassembled ASM");        
        
        fos.close();
        dataStream.close();
    }

    public void outputASM(PrintStream ps,  ROMDisassembler dis) throws Exception {
       
        int mapping[] = dis.getTypes();
        byte romData[] = dis.getRomData();
  //      int lowLabels[] = dis.getLowLabelAddresses();
  //      int highLabels[] = dis.getHighLabelAddresses();
        
                
  //      System.out.println("TO DO: Integrate with data files for disassembled ASM");        

        ps.println();
//        ps.println(".include \"constants.asm\"");
        
        
        int codeSegment = Architecture6502.PRG_BANK0_OFFSET;
        int codeHighSegment = Architecture6502.PRG_BANK1_OFFSET;
        if(!codeHigh){
            codeSegment = codeHighSegment;
        }
        int vectorSegment = Architecture6502.NMI_VECTOR_VALUE;
        int startingPosition = codeSegment;
                
        
        
       boolean dataMode = false;
        int dataCount = 0;
        for(int i=startingPosition;i<mapping.length;i++){
            if(i == codeSegment) {
                 if(dataMode) {
                    ps.println("");
                    ps.println("");
                    dataCount = 0;
                }
                ps.println();
                ps.println(".segment \"CODE\"");
            }
            if(i == codeHighSegment && codeHigh) {
                 if(dataMode) {
                    ps.println("");
                    ps.println("");
                    dataCount = 0;
                }
                ps.println();
                ps.println(".segment \"CODE_HIGH\"");
            }
            if(i == vectorSegment) {
                 if(dataMode) {
                    ps.println("");
                    ps.println("");
                    dataCount = 0;
                }
                ps.println();
                ps.println(".segment \"VECTORS\"");
            }
            
     
         if(ROMDisassembler.isLabel(mapping[i])){
                if(dataMode) {
                    ps.println("");
                    dataCount = 0;
                    dataMode = false;
                }
                ps.println("");
                ps.println(getLabelPrefix() + ByteFormatter.formatInt(i) + ":");     
         }
            
         if((mapping[i] == ROMDisassembler.LABEL_TYPE) ||( mapping[i] == ROMDisassembler.UNKNOWN_TYPE) || (ROMDisassembler.isDataType(mapping[i]))){
                if(!dataMode){
                    ps.println("");
                    ps.print(".byt ");
                } else {
                      if(dataCount % 16 == 0){
                        dataCount = 0;
                        ps.println(" ; DATA ");
                        ps.print(".byt ");
                    }
                }
                dataMode = true;
                if(dataCount > 0)
                    ps.print(", ");
                ps.print("$" + ByteFormatter.formatByte(romData[i]));                
                dataCount++;
                continue;
         }
            
       
            if(ROMDisassembler.isOpCode(mapping[i])) {
                if(dataMode) {
                    ps.println("");
                    ps.println("");
                    dataCount = 0;
                    dataMode = false;
                }
                int hexVal =  romData[i] & 0xFF;
                OpCode6502 opcode = OpCode6502.OPCODES[hexVal];
                Instruction6502 result = new Instruction6502(opcode,  romData);
              
                	ps.println(formatResult(i, result));
                    i+= (result.getEntireLength() -1);
                    continue;
            }
            if(ROMDisassembler.isVector(mapping[i])) {
                if(dataMode) {
                    ps.println("");
                    ps.println("");
                    dataCount = 0;
                    dataMode = false;
                }
              ps.println(getVectorPrefix() 
                + "\t" + getLabelPrefix() 
                + ByteFormatter.formatInt(Utilities6502.determineAddress(i, romData))
              );     
              System.out.println(getVectorPrefix() 
                + "\t" + getLabelPrefix() 
                + ByteFormatter.formatInt(Utilities6502.determineAddress(i, romData))
              ); 
              i++;
              continue;
            }
            System.out.println("Woops:" + ByteFormatter.formatInt(i) + " " + ByteFormatter.formatInt(mapping[i]) );
        }
     
        ps.println();
    }

    public String formatLabelDataResult(int baseAddress, Instruction6502 op, int dataLabelAddress, boolean isHigh) {
        String labelVal = ((isHigh) ? "#>" : "#<") + asLabel(dataLabelAddress);
        return "\t" + op.getOpCode().getInstruction().getToken() + "\t" + labelVal;
    }
 
}
