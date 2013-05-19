/*
 * NESRom.java
 *
 * Created on September 7, 2006, 3:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import ui.romLoader.disassembler.DisassemblerUIInterface;
import ui.romLoader.disassembler.ROMDisassembler;
import utilities.FileUtilities;
import emulator.AbstractRomFile;
import emulator.core.CPU6502.CPU6502;
import emulator.core.CPU6502.mvc.CPU6502ControllerInterface;
import emulator.core.CPU6502.mvc.CPU6502ViewInterface;

/**
 *
 * @author abailey
 */
public class NESRom extends AbstractRomFile implements CPU6502ControllerInterface {
     
 
    private int _numPrgBanks;
    private int _numChrBanks;
    private int _baseMapperInfo;
    private int _additionalMapperInfo;
    private String _mapperName;
      
    private byte _prgBanks[][];
    private byte _chrBanks[][];
    
    
    private Vector<CPU6502ViewInterface> _cpuViewVector = null;
    private CPU6502 _cpu = null;
    
    /** Creates a new instance of NESRom */
    public NESRom() {
        super();
        _numPrgBanks = 0;
        _numChrBanks = 0;
        _baseMapperInfo = 0;
        _additionalMapperInfo = 0;       
        _mapperName = null;
        _prgBanks = new byte[0][0];
        _chrBanks = new byte[0][0];
        _cpuViewVector = new Vector<CPU6502ViewInterface>();
        _cpu = null;
    }
    
    public void reAssign(int numPRG, int numCHR, int mapper) {
        _numPrgBanks = numPRG;
        _numChrBanks = numCHR;
        _baseMapperInfo = mapper;
        _additionalMapperInfo = 0;       
	int mapperType = _baseMapperInfo / 16;
//	int mapperFlags = _baseMapperInfo % 16;
        _mapperName = (mapperType < 0 || mapperType > INES_ROM_INFO.MAX_MAPPER) ? "???" : INES_ROM_INFO.MAPPER_STRINGS[mapperType];
        _prgBanks = new byte[_numPrgBanks][INES_ROM_INFO.PRG_BANK_SIZE];
        _chrBanks = new byte[_numChrBanks][INES_ROM_INFO.CHR_BANK_SIZE];
        _cpuViewVector.removeAllElements();
        _cpu = null;
    }
    
    public String getFullRomFileName() {
        return _fullROMFileName;        
    }
    public String getRomFileNameOnly() {
        return _romFileNameOnly;        
    }
    public int getNumPRGBanks(){
        return _numPrgBanks;
    }

    public int getNumCHRBanks(){
        return _numChrBanks;
    }
    
    public int getBaseMapperInfo(){
        return _baseMapperInfo;
    }
    public String getMapperName(){
        return _mapperName;
    }

    public int getAdditionalMapperInfo(){
        return _additionalMapperInfo;
    }
    
    public byte[] getPRGBank(int index){
        byte b[] = new byte[INES_ROM_INFO.PRG_BANK_SIZE];
        if(index < 0 || index >= _numPrgBanks){
            System.err.println("Invalid PRG bank:" + index);
        } else {
            System.arraycopy(_prgBanks[index],0,b,0,INES_ROM_INFO.PRG_BANK_SIZE);
        }
        return b;
    }
    public void setPRGBank(int index,  byte[] b){
        if(index < 0 || index >= _numPrgBanks){
            System.err.println("Invalid PRG bank:" + index);
        } else {
            System.arraycopy(b,0,_prgBanks[index],0,INES_ROM_INFO.PRG_BANK_SIZE);
        }       
    }

    public byte[] getCHRBank(int index){
        byte b[] = new byte[INES_ROM_INFO.CHR_BANK_SIZE];
        if(index < 0 || index >= _numChrBanks){
            System.err.println("Invalid CHR bank:" + index);
        } else {
            System.arraycopy(_chrBanks[index],0,b,0,INES_ROM_INFO.CHR_BANK_SIZE);
        }
        return b;
    }
    public void setCHRBank(int index,  byte[] b){
        if(index < 0 || index >= _numChrBanks){
            System.err.println("Invalid CHR bank:" + index);
        } else {
            System.arraycopy(b,0,_chrBanks[index],0,INES_ROM_INFO.CHR_BANK_SIZE);
        }       
    }
  
    
    protected boolean validateHeader(byte[] header){
        if(header.length != 16){
            System.err.println("Invalid header. Not 16 bytes???");
            return false;
        }
        // Now we validate the Header based on this document: http://nesdev.parodius.com/neshdr20.txt
	// Bytes:
	// 0 = 0x4E (charcter N)
	// 1 = 0x45 (character E)
	// 2 = 0x53 (character S)
	// 3 = 0x1A (Character Break)
	// 4 = number of prg banks
	// 5 = number of chr banks
	// 6 = Mapper, mirroring, etc..
	// 7 = More mapper info
	// 8 to 15 all zeroes
       if((header[0] != 0x4E) ||
	(header[1] != 0x45) ||
	(header[2] != 0x53) ||
	(header[3] != 0x1A))
	{
            System.err.println("Not a valid ROM file. Not an iNES header");
            return false;
       }
       	int prgBanks = (int)header[4];
	int chrBanks = (int)header[5];
	int baseMapper = header[6];
	int additionalMapper = header[7];
	for(int i=8;i<=15;i++){
		if(header[i] != 0){
			System.err.println("The remaining portion of the header (byte) " + i + " was not zero. Invalid iNES header");
                        // return false;
                }
	}
        
        _numPrgBanks = prgBanks;
        _numChrBanks = chrBanks;
        _baseMapperInfo = baseMapper;
        _additionalMapperInfo = additionalMapper;
        
   //     System.out.println("Header Information:");
   //     System.out.println("\tNum PRG Banks:" + _numPrgBanks);
   //     System.out.println("\tNum CHR Banks:" +  _numChrBanks);
	
	int mapperType = _baseMapperInfo / 16;
//	int mapperFlags = _baseMapperInfo % 16;
        _mapperName = (mapperType < 0 || mapperType > INES_ROM_INFO.MAX_MAPPER) ? "???" : INES_ROM_INFO.MAPPER_STRINGS[mapperType];
//        String mapFlag = (mapperFlags < 0 || mapperFlags > INES_ROM_INFO.MAX_MAPPER_FLAGS) ? "???" : INES_ROM_INFO.MAPPER_FLAGS[mapperFlags];

//	System.out.println("\tMapper Entry:" + _baseMapperInfo);
//        System.out.println("\tMapper:" + mapperType);
 //       System.out.println("\tType:" + _mapperName);        
//        System.out.println("\tFlags:" + mapFlag);
//        System.out.println("\tExtended:" + _additionalMapperInfo);      
        _additionalMapperInfo = 0;
        return true;
    }
    
    public boolean loadROM(Component parentComponent){    
        if(isDirty()){
            // prompt for save
        }
        File selectedFile = FileUtilities.selectFileForOpen( parentComponent );
        if(selectedFile == null){
            return false;
        }
        byte header[] = new byte[INES_ROM_INFO.INES_HEADER_SIZE];
        int headerCount = FileUtilities.loadBytes(selectedFile, header, INES_ROM_INFO.INES_HEADER_SIZE);
        
        if(headerCount < INES_ROM_INFO.INES_HEADER_SIZE){
            System.err.println("Header too small" + headerCount + " < " + INES_ROM_INFO.INES_HEADER_SIZE);
            return false;
        }
        if(!validateHeader(header)){
            System.err.println("Header not in INES format");
            return false;            
        }
        // store the data thats in the CHR and PRG banks into memory (what a pig I am)
        // I am such a memory PIG :)
        int entireRomContentsSize = (INES_ROM_INFO.INES_HEADER_SIZE) + (_numPrgBanks * INES_ROM_INFO.PRG_BANK_SIZE) + (_numChrBanks * INES_ROM_INFO.CHR_BANK_SIZE);
        byte entireRomContents[] = new byte[entireRomContentsSize];
        int romLoadedVal = FileUtilities.loadBytes(selectedFile, entireRomContents, entireRomContentsSize);
        if(romLoadedVal != entireRomContentsSize ){
            System.err.println("Rom data does not correspond to the header. Unable to load the data");
            return false;       
        }
        _prgBanks = new byte[_numPrgBanks][INES_ROM_INFO.PRG_BANK_SIZE];
        _chrBanks = new byte[_numChrBanks][INES_ROM_INFO.CHR_BANK_SIZE];
        int curPos = INES_ROM_INFO.INES_HEADER_SIZE;
        for(int i=0;i<_numPrgBanks;i++){
            System.arraycopy(entireRomContents, curPos, _prgBanks[i], 0,  INES_ROM_INFO.PRG_BANK_SIZE);
            curPos+=INES_ROM_INFO.PRG_BANK_SIZE;
        }
        for(int i=0;i<_numChrBanks;i++){
            System.arraycopy(entireRomContents, curPos, _chrBanks[i], 0,  INES_ROM_INFO.CHR_BANK_SIZE);
            curPos+=INES_ROM_INFO.CHR_BANK_SIZE;
        }
         
        _fullROMFileName = selectedFile.getAbsolutePath();
        _romFileNameOnly = selectedFile.getName();
        _isDirty = false;
        notifyCPUModelChanged(MODEL_CHANGED);
        return true;
    }
   public boolean saveROM(Component parentComponent){        
        if(_fullROMFileName == null){
            return saveAsROM(parentComponent);
        }
        File selectedFile  = new File(_fullROMFileName);
 
        boolean ret = saveRomToFile(selectedFile);
        if(ret) {
            _fullROMFileName = selectedFile.getAbsolutePath();
            _romFileNameOnly = selectedFile.getName();
        }
        return ret; 
    }
    public boolean saveAsROM(Component parentComponent){        
        File selectedFile = FileUtilities.selectFileForSave(parentComponent);
        if(selectedFile == null){
            return false;
        }
        boolean ret = saveRomToFile(selectedFile);
        if(ret) {
            _fullROMFileName = selectedFile.getAbsolutePath();
            _romFileNameOnly = selectedFile.getName();
        }
        return ret; 
    }
    public boolean disassemble(Component parentComponent, DisassemblerUIInterface disUI){            
        if(_numPrgBanks == 0){
            System.err.println("Nothing to disassemble");
            return false;
        }
        if(_prgBanks.length != _numPrgBanks || _chrBanks.length != _numChrBanks){
           System.err.println("Bank size mismatch");
            return false;
        }        
        ROMDisassembler disassembler = new ROMDisassembler();
        disassembler.setDisassemblerUI(disUI);
        return disassembler.disassembleROM(this, this, parentComponent);
    }

 
     public boolean generatePRGHexDumpDiff(Component parentComponent){            
        if(_numPrgBanks == 0){
            System.err.println("Nothing to Dump");
            return false;
        }
        if(_prgBanks.length != _numPrgBanks || _chrBanks.length != _numChrBanks){
           System.err.println("Bank size mismatch");
            return false;
        }
        NESRom secondROM = new NESRom();
        secondROM.loadROM(parentComponent);
        
        if(_numPrgBanks != secondROM._numPrgBanks || _numChrBanks != secondROM._numChrBanks) {
           System.err.println("ROM dimension mismatch");
            return false;
        }
        
        File selectedFile = FileUtilities.selectFileForSave(parentComponent);
        if(selectedFile == null){
            return false;
        }
        
        boolean retValue = true;
        FileOutputStream fos = null;
        PrintStream printStream = null;
        try {
            fos = new FileOutputStream(selectedFile);
            printStream = new PrintStream(fos);
        
            ROMDisassembler disassembler = new ROMDisassembler();
            for(int i=0;i<_prgBanks.length ;i++){
                printStream.println("PRG Bank:" + i);
                disassembler.spewPRGBankDIFF(printStream, 0x8000+(0x4000*i), _prgBanks[i], secondROM._prgBanks[i], parentComponent);
            }
        } catch(Exception e) {
            e.printStackTrace();
            retValue = false;
        }
        try {
            if(printStream != null){
                printStream.flush();
            }
            if(fos != null){
                fos.flush();
                fos.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
            retValue = false;
        }
        return retValue;
     }
     
     public boolean generatePRGHexDump(Component parentComponent){            
        if(_numPrgBanks == 0){
            System.err.println("Nothing to Dump");
            return false;
        }
        if(_prgBanks.length != _numPrgBanks || _chrBanks.length != _numChrBanks){
           System.err.println("Bank size mismatch");
            return false;
        }

        File selectedFile = FileUtilities.selectFileForSave(parentComponent);
        if(selectedFile == null){
            return false;
        }
        boolean retValue = true;
        FileOutputStream fos = null;
        PrintStream printStream = null;
        try {
            fos = new FileOutputStream(selectedFile);
            printStream = new PrintStream(fos);
        
            ROMDisassembler disassembler = new ROMDisassembler();
            for(int i=0;i<_prgBanks.length ;i++){
                printStream.println("PRG Bank:" + i);
                disassembler.spewPRGBank(printStream, _prgBanks[i], parentComponent);
            }
        } catch(Exception e) {
            e.printStackTrace();
            retValue = false;
        }
        try {
            if(printStream != null){
                printStream.flush();
            }
            if(fos != null){
                fos.flush();
                fos.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
            retValue = false;
        }
        return retValue;
     }
    
    
    public boolean splitROM(Component parentComponent){            
        boolean retVal = true;

        if(_fullROMFileName == null){
            System.err.println("Cannot split something since it was never loaded");
            return false;
        }
        if(_prgBanks.length != _numPrgBanks || _chrBanks.length != _numChrBanks){
           System.err.println("Bank size mismatch");
            return false;
        }
        
        File selectedFile = FileUtilities.selectFileForSave(parentComponent);
        if(selectedFile == null){
            System.err.println("Nothing selected");
            return false;
        }
        String curFileName = selectedFile.getParentFile().getAbsolutePath();
        String curFileNameOnly = selectedFile.getName();
        int lastPeriod =  selectedFile.getName().lastIndexOf(".");
        String partialFileName = curFileNameOnly;
        if(lastPeriod > 0){
            partialFileName = curFileNameOnly.substring(0,lastPeriod);
        }
        String prefixFileName = curFileName + File.separator + partialFileName;


        for(int i=0;i<_numPrgBanks;i++){
            File prgFileName = new File(prefixFileName + i + ".prg");
            System.out.println("Storing to file:" + prgFileName.getAbsolutePath());
            if(! FileUtilities.saveBytes(prgFileName, _prgBanks[i], _prgBanks[i].length)) {
                System.err.println("Failed to save PRG " + i + " to file:" + prgFileName.getAbsolutePath());
                retVal = false;
                break;
            }
        }

        if(retVal) {
            for(int i=0;i<_numChrBanks;i++){
                File chrFileName = new File(prefixFileName + i + ".chr");
                System.out.println("Storing to file:" + chrFileName.getAbsolutePath());
                if(! FileUtilities.saveBytes(chrFileName, _chrBanks[i], _chrBanks[i].length)) {
                    System.err.println("Failed to save CHR " + i + " to file:" + chrFileName.getAbsolutePath());
                    retVal = false;
                    break;
                }            
            }
        }
        System.out.println("Its split");
        return retVal;
    }
    
    private boolean saveRomToFile(File selectedFile) {
        if(_prgBanks.length != _numPrgBanks || _chrBanks.length != _numChrBanks){
            System.err.println("Unable to save this file. Data is incomplete");
        }
        int entireRomContentsSize = (INES_ROM_INFO.INES_HEADER_SIZE) + (_numPrgBanks * INES_ROM_INFO.PRG_BANK_SIZE) + (_numChrBanks * INES_ROM_INFO.CHR_BANK_SIZE);
        byte entireRomContents[] = new byte[entireRomContentsSize];
        // setup the header
        entireRomContents[0] = 0x4E;
	entireRomContents[1] = 0x45;
	entireRomContents[2] = 0x53;
	entireRomContents[3] = 0x1A;
       	entireRomContents[4] = (byte)_numPrgBanks;
        entireRomContents[5] = (byte)_numChrBanks;
        entireRomContents[6] = (byte)_baseMapperInfo;
        entireRomContents[7] = (byte)_additionalMapperInfo;
	for(int i=8;i<=15;i++){
            entireRomContents[i] = 0;
        }
        int curPos = 16;
        for(int i=0;i<_numPrgBanks;i++){
            System.arraycopy(_prgBanks[i],0,entireRomContents,curPos,INES_ROM_INFO.PRG_BANK_SIZE);
            curPos+=INES_ROM_INFO.PRG_BANK_SIZE;
        }
        for(int i=0;i<_numChrBanks;i++){
            System.arraycopy(_chrBanks[i],0,entireRomContents,curPos,INES_ROM_INFO.CHR_BANK_SIZE);
            curPos+=INES_ROM_INFO.CHR_BANK_SIZE;
        }
        return FileUtilities.saveBytes(selectedFile
                 ,entireRomContents
                 ,entireRomContents.length
        );
    }
    
    public boolean addCPU6502View(CPU6502ViewInterface view) {
        if(!_cpuViewVector.contains(view)){
                _cpuViewVector.add(view);
                return true;
        }
        return false;
    }
    public boolean removeCPU6502View(CPU6502ViewInterface view){
        return _cpuViewVector.remove(view);
    }
    
    public boolean setCPU6502(CPU6502 cpu){
        _cpu = cpu;
        _cpu.setCPUController(this);
        notifyCPUModelChanged(CPU6502ControllerInterface.MODEL_CHANGED);
        return true;
    }
     public CPU6502 getCPU6502(){
       return _cpu;
    }
     
    public void notifyCPUMemoryChanged(){
    }
    
    public void notifyCPUModelChanged(int changeType){
        if(_cpu != null && _cpuViewVector.size() > 0 &&  _cpu.getControllerUpdatesMode()) {
            for(int i=0;i<_cpuViewVector.size();i++){
                ((CPU6502ViewInterface)_cpuViewVector.elementAt(i)).refreshFromCPU(_cpu);
            }
        }
    }   
}
