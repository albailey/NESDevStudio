/*
 * MemoryManager.java
 *
 * Created on October 25, 2007, 1:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes;

import emulator.core.CPU6502.MemoryInterface;
import emulator.core.CPU6502.MemoryReadObserver;
import emulator.core.CPU6502.MemoryWriteObserver;
import emulator.core.CPU6502.Utilities6502;
import emulator.nes.mappers.MapperFactory;

/**
 *
 * @author abailey
 */
public final class MemoryManager implements MemoryInterface {

    public final static int MEMORY_FOOTPRINT = 0x10000; // maximum really is 32 K
    public final static int PPU_MEM_FOOTPRINT = 0x4000;
    private Mapper _mapper = null;
    protected byte[] _prgData = null;
    protected byte[] _memoryData = null;
    protected byte[] _chrData = null;

    private final static byte blarggDefaultPalette[] = {
      0x09,0x01,0x00,0x01,0x00,0x02,0x02,0x0D,0x08,0x10,0x08,0x24,0x00,0x00,0x04,0x2C,
      0x09,0x01,0x34,0x03,0x00,0x04,0x00,0x14,0x08,0x3A,0x00,0x02,0x00,0x20,0x2C,0x08
    };

    // 0x0000 = Pattern Table 0
    // 0x1000 = Pattern Table 1
    // 0x2000 =  Nametable 0
    // 0x2400 =  Nametable 1
    // 0x2800 =  Nametable 2
    // 0x2C00 =  Nametable 3
    // 0x3000 = Mirror of $2000-$2eff
    // 0x3F00 = Palette
    // 0x3F20-$3FFF = mirror of 3F00-3F1F
    // WARNING: Palette is NOT externally queryable
    protected byte[] _ppuMem = null;

    private PPU _ppu = null;
    private IOMappedMemory _controllers = null;
    private IOMappedMemory _apu = null;
    private MemoryReadObserver _readObserver = null;
    private MemoryWriteObserver _writeObserver = null;

    /** Creates a new instance of MemoryManager */
    public MemoryManager( PPU ppu, IOMappedMemory apu, IOMappedMemory controllers) {
        ppu.setMemoryInterface(this);
        _ppu = ppu;
        _apu = apu;
        _controllers = controllers;
        _memoryData = new byte[MEMORY_FOOTPRINT];
        _ppuMem = new byte[PPU_MEM_FOOTPRINT];
        _mapper = MapperFactory.constructMapperFromHeaderInfo(0);
    }

    public void setObservers(MemoryReadObserver readObserver, MemoryWriteObserver writeObserver) {
        _readObserver = readObserver;
        _writeObserver = writeObserver;
    }

    public byte[] getMemoryPointer() {
        return _memoryData; // returns this as a reference
    }

    public MemoryInterface makeCopy() {
        MemoryManager copy = new MemoryManager( _ppu, _apu, _controllers);
        System.arraycopy(_memoryData, 0, copy._memoryData, 0, _memoryData.length);
        System.arraycopy(_ppuMem, 0, copy._ppuMem, 0, _ppuMem.length);
        if (_prgData != null) {
            System.arraycopy(_prgData, 0, copy._prgData, 0, _prgData.length);
        }
        if (_chrData != null) {
            System.arraycopy(_chrData, 0, copy._chrData, 0, _chrData.length);
        }
        return copy;
    }

    public void clearMemory() {
        for (int i = 0; i < _memoryData.length; i++) {
            _memoryData[i] = (byte) 0x00;
        }
         for (int i = 0; i < _ppuMem.length; i++) {
            _ppuMem[i] = (byte) 0x00;
        }
        // assign the blargg default palette
        System.arraycopy(blarggDefaultPalette,0,_ppuMem,0x3F00,32);
    }

    public void randomizeMemory() {
        for (int i = 0; i < _memoryData.length; i++) {
            _memoryData[i] = (byte) (Math.random() * 256);
        }
        for (int i = 0; i < _ppuMem.length; i++) {
            _ppuMem[i] = (byte) (Math.random() * 256);
        }
        // assign the blargg default palette
        System.arraycopy(blarggDefaultPalette,0,_ppuMem,0x3F00,32);
    }

    public int determineAddress(int address) {
        return Utilities6502.determineAddress(address, _memoryData);
    }

    public void initMapper() {
        _mapper.setPPURef(_ppu);
        byte b[] = _mapper.getInitialPRGData();
        System.arraycopy(b, 0, _memoryData, MEMORY_FOOTPRINT - b.length, b.length);
    }

    public void setMapper(Mapper m) {
        _mapper = m;
    }

    public void assignPRGMemory(byte[] prgData) {
        _prgData = new byte[prgData.length];
        System.arraycopy(prgData, 0, _prgData, 0, _prgData.length);
        _mapper.setPRGMemory(_prgData);
    }

    public void assignCHRMemory(byte[] chrData) {
    
        int len = 0x2000;
        int chrLen = chrData.length;
        if(chrLen < len){
            chrLen = len;
        }
        _chrData = new byte[chrLen];
        System.arraycopy(chrData, 0, _chrData, 0, chrData.length);
        _mapper.setCHRMemory(_chrData);
        System.arraycopy(_chrData, 0, _ppuMem, 0, 0x2000);
 
    }


    // Memory Interface
    // get CONTROLLERS before APU
    public byte getMemory(int address, boolean shouldNotify) {
        if (shouldNotify && _readObserver != null) {
            _readObserver.updateReadMemory(address);
        }
        if (isCPUMapped(address)) {
            return getCPUMappedMemory(address);
        } else if (_ppu.isReadMapped(address)) {
            return _ppu.getMappedMemory(address);
        } else if (_controllers.isReadMapped(address)) {
            return _controllers.getMappedMemory(address);
        } else if (_apu.isReadMapped(address)) {
            return _apu.getMappedMemory(address);
        } else if (_mapper.isReadMapped(address)) {
            return _mapper.getMappedMemory(address);
        } else {
            return _memoryData[address];
        }
    }

    // get CONTROLLERS before APU
    public byte getMemoryDirect(int address) {
        if (isCPUMapped(address)) {
            return getCPUMappedMemoryDirect(address);
        } else if (_ppu.isReadMapped(address)) {
            return _ppu.getMappedMemoryDirect(address);
        } else if (_controllers.isReadMapped(address)) {
            return _controllers.getMappedMemoryDirect(address);
        } else if (_apu.isReadMapped(address)) {
            return _apu.getMappedMemoryDirect(address);
        } else if (_mapper.isReadMapped(address)) {
            return _mapper.getMappedMemoryDirect(address);
        } else {
            return _memoryData[address];
        }
    }

    // *CPU RAM Mirroring:
    // $0000-$07FF are mirrored three times at $0800-$1FFF. This means that, for example, any
    // data written to $0000 will also be written to $0800, $1000 and $1800.
    public boolean isCPUMapped(int address){
        return (address >= 0x0800 && address <=0x1FFF);
    }

    public byte getCPUMappedMemory(int address){
        return _memoryData[address & 0x07FF];
    }
    public byte getCPUMappedMemoryDirect(int address){
        return _memoryData[address & 0x07FF];
    }

    public void setCPUMappedMemory(int address, byte val){
        _memoryData[address & 0x07FF] = val;
    }

    // set CONTROLLERS before APU
    public void setMemory(int address, byte val, boolean shouldNotify) {
        boolean processed = false;
        if (isCPUMapped(address)) {
            setCPUMappedMemory(address, val);
            processed = true;
        }
        if (_ppu.isWriteMapped(address)) {
            _ppu.setMappedMemory(address, val);
            processed = true;
        }
        if (_controllers.isWriteMapped(address)) {
            _controllers.setMappedMemory(address, val);
            processed = true;
        }
        if (_apu.isWriteMapped(address)) {
            _apu.setMappedMemory(address, val);
            processed = true;
        }
        if (_mapper.isWriteMapped(address)) {
            _mapper.setMappedMemory(address, val);
        }
        if(!processed) {
            _memoryData[address] = val;
        }
        if (shouldNotify && _writeObserver != null) {
            _writeObserver.updateWriteMemory(address, val);
        }
    }
    
    // Memory Interface
    public final byte getCHRMemory(int address) {
        return (_mapper.isCHRReadMapped(address)) ? _mapper.getCHRMappedMemory(address) : _ppuMem[address];
    }

    public void setCHRMemory(int address, byte val, boolean shouldNotify) {
        if (_mapper.isCHRWriteMapped(address)) {
            _mapper.setCHRMappedMemory(address, val);
        } else {
            _ppuMem[address]  = val;
        }
    }
}
