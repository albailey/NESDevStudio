/*
 * NES.java
 *
 * Created on October 25, 2007, 12:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Vector;

import utilities.ByteFormatter;
import utilities.FileUtilities;
import emulator.core.CPU6502.Architecture6502;
import emulator.core.CPU6502.CPU6502;
import emulator.core.CPU6502.MemoryReadObserver;
import emulator.core.CPU6502.MemoryWriteObserver;
import emulator.core.CPU6502.mvc.CPU6502ControllerInterface;
import emulator.core.CPU6502.mvc.CPU6502MemoryModelInterface;
import emulator.core.CPU6502.mvc.CPU6502ViewInterface;
import emulator.nes.controllers.Controllers;
import emulator.nes.controllers.JoypadInterface;
import emulator.nes.debugger.NESDebugWatch;
import emulator.nes.debugger.NESDebuggerInterface;
import emulator.nes.ui.PPUViewInterface;
import emulator.tv.Display;
import emulator.tv.NullDisplay;

/**
 *
 * @author abailey
 */
public final class NES implements Runnable, CPU6502ControllerInterface, MemoryReadObserver, MemoryWriteObserver, NESDebuggerInterface {

    private final static int UPDATE_PERIOD = 300;
    private final static boolean NES_DEV_MODE = false;
    // since these arrays are accessed a lot, I'll use a normal object array
    private Vector<MemoryReadObserver>  _memReadObservers = new Vector<MemoryReadObserver>();
    private Vector<MemoryWriteObserver> _memWriteObservers = new Vector<MemoryWriteObserver>();
    // I should add support for multiple displays...
    private boolean _running = false;
    private Display _display = null; // callback to the display. This CANNOT be NULL
    public PPU _ppu = null;
    private APU _apu = null;
    private CPU6502 _cpu = null;
    public MemoryManager _memoryManager = null;
    private Thread _thread = null;
    public NESCartridge _cart = null;
    private Vector<CPU6502ViewInterface> _cpuViewVector = null;
    private Vector<PPUViewInterface> _ppuViewVector = null;
    private Vector<CPU6502MemoryModelInterface> _memViewVector = null;
    private Vector<NESDebugWatch> _watchList = null;
    private Vector<Integer> _breakpointList = null;
    private Vector<Byte> _opcodeBreakpointList = null;
    private long _delayPeriod = 0;
    private long _lastFrame = 0;
    private long _nextFrame = 0;
    private long _frameTotal = 0;
    private long _frameCount = 0;
    // move this into DebugState later
    private final Object condVar = new Object();
    private boolean _stepCPUCycle = false;
    private boolean _stepCPUInstruction = false;
    private boolean _stepPPUFrame = false;
    private boolean _stepPPULine = false;
    private boolean _stepPPUInstruction = false;
    private boolean _override = false;
    private boolean _ppuOverride = false;
    private boolean _captureMode = false;
    private boolean _memoryWatchMode = false;
    private PrintStream _captureOut = null;
    private JoypadInterface _joypad = null;
    private Component _comp = null;
    private boolean hasBreakpoints = false;

    private boolean _irqTriggered = false;
    private boolean _nmiTriggered = false;
    private boolean _delayNMI = false;
    private boolean _singleDelayNMI = false;

    private int _masterIncrement = 15;
    private int _masterCycles = 0;

    /** Creates a new instance of NES */
    public NES(Component comp) {
        _comp = comp;
        _running = false;
        _display = new NullDisplay();
        _ppu = new PPU();
        _ppu.setNESCallback(this, _display, true);
        _apu = new APU();
        _joypad = new Controllers();
        _memoryManager = new MemoryManager(_ppu, _apu, _joypad);
        _memoryManager.setObservers(this, this);
        _cpu = new CPU6502(_memoryManager);
        _apu.setNES(this);
        setNTSCMode();
        _cart = null;
        _delayPeriod = FrameRateUtilities.calculateFrameRateDelay(FrameRateUtilities.NTSC_MODE, FrameRateUtilities.FULL_SPEED_MODE);
        _thread = new Thread(this);


        _cpuViewVector = new Vector<CPU6502ViewInterface>();
        _ppuViewVector = new Vector<PPUViewInterface>();
        _memViewVector = new Vector<CPU6502MemoryModelInterface>();
        _watchList = new Vector<NESDebugWatch>();
        _breakpointList = new Vector<Integer>();
        _opcodeBreakpointList = new Vector<Byte>();
        hasBreakpoints = false;

    }

    public APU getAPU() {
        return _apu;
    }
    
    public boolean processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_PRESSED) {
            return _joypad.processKeyEvent(evt.getKeyCode(), true);
        } else if (evt.getID() == KeyEvent.KEY_RELEASED) {
            return _joypad.processKeyEvent(evt.getKeyCode(), false);
        } else {
            return false;
        }
    }

    public NESDebuggerInterface getDebugger() {
        return this;
    }

    public void setCaptureMode(boolean flag, Component comp) {
        if (flag) {
            File captureLog = FileUtilities.selectFileForSave(comp);
            if (captureLog != null) {
                try {
                    FileOutputStream fos = new FileOutputStream(captureLog);
                    _captureOut = new PrintStream(fos);
                    _captureMode = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            _captureMode = false;
            Thread.yield();
            if (_captureOut != null) {
                try {
                    _captureOut.flush();
                    _captureOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void setPalMode() {
        _masterIncrement = 16;
    }
    public void setNTSCMode() {
        _masterIncrement = 15;
    }
    public boolean isPalMode() {
        return !isNTSCMode();
    }
    public boolean isNTSCMode() {
        return (_masterIncrement == 15);
    }

    public int getMasterIncrement() {
        return _masterIncrement;
    }


    public String getStateString() {
        return "NES Master Cycles: " + _masterCycles + " PPU:" + _ppu.getStateString() +" CPU: " + _cpu.getStateString();
    }

    public void triggerEarlyNMI() {

    }

    public void preTriggerNMI(int ppuCycles) {
        _nmiTriggered = true;
        
        _masterCycles -= ppuCycles;
        _cpu.setClock(0);
        
//        System.out.println("Master changed to:" + _masterCycles);
        
        if (_stepPPULine || _stepPPUFrame ) {
            forcePartialUpdates();
            try {
                synchronized (condVar) {
                    condVar.wait();
                }
            } catch (Exception e) {
                setDebuggingOff();
                e.printStackTrace();
            }
        } else if(_ppuOverride) {
            notifyPPUChanged();
        }
    }

    public void notifyScanlineChanged() {
        if (_stepPPULine) {
            forcePartialUpdates();
            try {
                synchronized (condVar) {
                    condVar.wait();
                }
            } catch (Exception e) {
                setDebuggingOff();
                e.printStackTrace();
            }
        }
    }

    private boolean isNMINextInstruction() {
        return (_cpu.getNextOpcodeAddress() == 0x2002);
    }

    public void calculateFrameRate() {
        _lastFrame = System.currentTimeMillis();
        if (_nextFrame != 0) {
            _frameTotal += (_lastFrame - _nextFrame);
            _frameCount++;
            if (_frameTotal > UPDATE_PERIOD) {
                _display.setFPS((int) (_frameCount * 1000.0 / _frameTotal));
                _frameTotal = 0;
                _frameCount = 0;
                Thread.yield();
            }
        }
        _nextFrame = _lastFrame; // System.currentTimeMillis();
    }
    public void performNMI() {
       if (NES_DEV_MODE) {
           System.out.println("NMI actual: " + getStateString() + " APU " + _apu.toString());
       }
        if(_ppu.isNMIEnabled()) {

            if( isNMINextInstruction() && (_masterCycles <= 5)) {
               if (_captureMode && _captureOut != null) {
                    _captureOut.println("");
                    _captureOut.println("NMI SUPPRESS due to $2002 access on zero cycle:" + _cpu.getCurrentInstructionDescription());
                }
            } else {
                if (_captureMode && _captureOut != null) {
                    _captureOut.println("");
                    _captureOut.println("NMI");
                }
                _cpu.doInterrupt(_memoryManager.determineAddress(Architecture6502.NMI_VECTOR_VALUE));
            }
        } 
        //_apu.finishframe();
        calculateFrameRate();
    }

    public boolean addCPU6502View(CPU6502ViewInterface view) {
        if (view == null) {
            return false;
        }
        _cpuViewVector.add(view);
        view.refreshFromCPU(_cpu);
        _cpu.setCPUController(this);
        return true;
    }

    public boolean removeCPU6502View(CPU6502ViewInterface view) {
        return _cpuViewVector.remove(view);
    }

    public boolean setCPU6502(CPU6502 cpu) {
        throw new IllegalArgumentException("The cpu is implicitly built into the NES");
    //  return false;
    }

    public CPU6502 getCPU6502() {
        return _cpu;
    }

   
    public void notifyCPUModelChanged() {
        notifyCPUModelChanged(0);
    }

    public void notifyCPUModelChanged(int changeType) {
        if (_stepCPUCycle || _override) {
            if (_cpu != null && _cpuViewVector.size() > 0 && _cpu.getControllerUpdatesMode()) {
                for (int i = 0; i < _cpuViewVector.size(); i++) {
                    ((CPU6502ViewInterface) _cpuViewVector.elementAt(i)).refreshFromCPU(_cpu);
                }
            }
        }
    }

    public void notifyPPUChanged() {
        notifyPPUChanged(0);
    }

    public void notifyPPUChanged(int changeType) {
        if ( _ppuOverride || _stepPPUFrame || _stepPPULine || _stepPPUInstruction || _override) {
            if (_ppu != null && _ppuViewVector.size() > 0) {
                for (int i = 0; i < _ppuViewVector.size(); i++) {
                    ((PPUViewInterface) _ppuViewVector.elementAt(i)).refreshFromPPU(_ppu);
                }
            }
        }
    }

    public void notifyCPUMemoryChanged() {
        if (_stepCPUCycle || _override) {
            if (_memViewVector.size() > 0) {
                for (int i = 0; i < _memViewVector.size(); i++) {
                    ((CPU6502MemoryModelInterface) _memViewVector.elementAt(i)).updateMemory(_memoryManager.getMemoryPointer());
                }
            }
        }
    }

    public void forceUpdates() {
        _override = true;
        notifyCPUMemoryChanged();
        notifyPPUChanged();
        notifyCPUModelChanged();
        _apu.notifyAPUChanged();
        _override = false;
    }

    public void forcePartialUpdates() {
        _override = true;
        notifyPPUChanged();
        notifyCPUModelChanged();
        _override = false;
    }

    public void loadCartridge(NESCartridge cart) {
        _cart = cart;
        _memoryManager.clearMemory();
        _memoryManager.setMapper(_cart.getMapper());
        _memoryManager.assignPRGMemory(_cart.getPRGData());
        _memoryManager.assignCHRMemory(_cart.getCHRData());
        if(_cart.getHorizontalMirroring()){
            _ppu.setHorizontalMirroringMode();    
        } else if(_cart.getVerticalMirroring()){
            _ppu.setVerticalMirroringMode();    
        } else {
            _ppu.setFourScreenMirroringMode();    
        }
        _memoryManager.initMapper(); // this will set the mirroring for this mapper
        setDebuggingOff();
        synchronized (condVar) {
            condVar.notifyAll();
        }
       
    }

    public void setDisplay(Display display) {
        if (display != null) {
            _display = display;
            _ppu.setNESCallback(this, _display, true);
        }
    }

    public void addMemoryModelListener(CPU6502MemoryModelInterface view) {
        if (view == null) {
            return;
        }
        _memViewVector.add(view);
        addMemoryWriteObserver(view);
        _override = true;
        notifyCPUMemoryChanged();
        _override = false;
    }

    public boolean addPPUView(PPUViewInterface view) {
        if (view == null) {
            return false;
        }
        _ppuViewVector.add(view);
        view.refreshFromPPU(_ppu);
        return true;
    }

    public boolean removeCPU6502View(PPUViewInterface view) {
        return _ppuViewVector.remove(view);
    }

    public void addMemoryReadObserver(MemoryReadObserver observer) {
        if (observer != null) {
            if(!_memReadObservers.contains(observer)){
                _memReadObservers.add(observer);
            }
        }
    }

    public void addMemoryWriteObserver(MemoryWriteObserver observer) {
        if (observer != null) {
            if(!_memWriteObservers.contains(observer)){
                _memWriteObservers.add(observer);
            }
        }
    }

    public void removeMemoryReadObserver(MemoryReadObserver observer) {
        if (observer != null) {
            _memReadObservers.remove(observer);
        }
    }

    public void removeMemoryWriteObserver(MemoryWriteObserver observer) {
        if (observer != null) {
             _memWriteObservers.remove(observer);
        }
    }

    public void updateReadMemory(int address) {
        for (int i = 0; i < _memReadObservers.size(); i++) {
            _memReadObservers.elementAt(i).updateReadMemory(address);
        }
    }

    public void updateWriteMemory(int address, byte val) {
        if (_watchList.contains(new NESDebugWatch(address, true, false))) {
            pause();
        }
        for (int i = 0; i < _memWriteObservers.size(); i++) {
            _memWriteObservers.elementAt(i).updateWriteMemory(address, val);
        }
    }

    public void powerOff() {
        _running = false;
        setDebuggingOff();
        synchronized (condVar) {
            condVar.notifyAll();
        }
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        clearAllStates();
        _memoryManager.clearMemory();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }    }

    public void powerOn() {
        clearAllStates();
        _memoryManager.clearMemory();
        if (_cart != null) {
            // likely need to set the CPU based on the mapper
             _ppu.clear();
             _apu.powerOn();
             _cpu.setInterruptFlag(true);
            _memoryManager.setMapper(_cart.getMapper());
            _memoryManager.assignPRGMemory(_cart.getPRGData());
            _memoryManager.assignCHRMemory(_cart.getCHRData());
            if(_cart.getHorizontalMirroring()){
                _ppu.setHorizontalMirroringMode();
            } else if(_cart.getVerticalMirroring()){
                _ppu.setVerticalMirroringMode();
            } else {
                _ppu.setFourScreenMirroringMode();
            }
            _memoryManager.initMapper(); // this will set the mirroring for this mapper
            if (_captureMode && _captureOut != null) {
                _captureOut.println("");
                _captureOut.println("RESET");
            }
            _cpu.setProgramCounter(_memoryManager.determineAddress(Architecture6502.RESET_VECTOR_VALUE));
            forceUpdates();
            setDebuggingOff();
            synchronized (condVar) {
                condVar.notifyAll();
            }
            pause();
            startRunning();
        }
    }

    public void hardReset() {
        boolean wasPaused = isPaused();

        powerOff();

        startCPUCapture();
        
        clearAllStates();
        
        powerOn();
        
        forceUpdates();
        
        if(wasPaused){
            pause();
        }
        
        
    }

    // be aware of CLI latency
    // http://nesdev.parodius.com/bbs/viewtopic.php?p=19655
    public void doIRQ(boolean flag) {
    	_irqTriggered = flag;
    }
    
    public void performIRQ() {
    	
    	if (_captureMode && _captureOut != null) {
            _captureOut.println("");
            _captureOut.println("IRQ");
        }
        if (NES_DEV_MODE) {
            System.out.println("IRQ actual" + getStateString() + " APU " + _apu.toString());
        }

        //If the CPU's /IRQ input is 0 at the end of an instruction,
        // the CPU pushes the program counter and the processor status register
        // does SEI to ignore further IRQs
        // and finally JMP ($FFFE).
     
        _irqTriggered = false;
        _cpu.doInterrupt(_memoryManager.determineAddress(Architecture6502.IRQ_VECTOR_VALUE), true);
    }

    public void softReset() {
        _apu.reset();
        _cpu.setInterruptFlag(true);    	
        _display.clearDisplay();    	
        _cpu.doInterrupt(_memoryManager.determineAddress(Architecture6502.RESET_VECTOR_VALUE), false); // reset does not write to stack
    }
  
    
    private void clearAllStates() {
    	_irqTriggered = false;
    	_ppu.clear();
        _apu.powerOn();
        _cpu.clear();
    	_display.clearDisplay();
    }

    private void startRunning() {
        if (_running == false) {
            _running = true;
            setDebuggingOff();
            _thread = new Thread(this);
            _thread.start();
        }
    }

    private void setDebuggingOff() {
        _stepCPUCycle = false;
        _stepCPUInstruction = false;
        _stepPPUFrame = false;
        _stepPPULine = false;
    }

    // basing this on Disch's approach  http://nesdev.parodius.com/bbs/viewtopic.php?t=505
    // namely this (master cycles)
    // - For every 1 NTSC CPU cycle that passes, I increment the CPU timestamp by 15
    // - For every 1 PAL CPU cycle that passes, I increment the CPU timestamp by 16
    // - For every 1 PPU cycle that passes (NTSC or PAL), I increment the PPU timestamp by 5
    // So: after doing a CPU instruction, update the master cycles, and run PPU to catchup.
    public void run() {
        if (NES_DEV_MODE) {
            System.out.println("Starting execution");
        }

        // because the PPU and CPU do not run at the same cycle rate, I need to use a different hyper-clock cycle
        // PAL PPU = 50 fps
        // NTSC PPU = 60 fps
        // CPU
        
        _masterCycles = 0;

        while (_running) {

            // Do PPU first, that way CPU may have its NMI invoked
            if (_cpu.isNewInstruction()) {
                preCheckInstruction(); // interface with debugger, logger, etc..
               if(_nmiTriggered){
                    _nmiTriggered = false;
                    _delayNMI = false;
                    _singleDelayNMI = false;
                    performNMI(); // this sets the cycles to 7
                } else if(_irqTriggered && !_cpu.getInterruptFlag() ) { // && ! _cpu.isCLILatency()) {
                        performIRQ(); // this sets the cycles to 7
                } else {
                    if(!_cpu.processNextInstruction()) { // basically does a prefetch
                        System.err.println("Aborting");
                        _running = false;
                    }
                    if(_singleDelayNMI){
                        _singleDelayNMI = false;
                        _ppu.triggerNMI();
                    }
                    if(_delayNMI) {
                        _delayNMI = false;
                        _singleDelayNMI = true;
                    }
                }
            }  else {
            	boolean catchup = _cpu.isIncrementingClock();
            	if(! _cpu.processNextInstructionCycle()) {  // 15 or 16 master cycles for this
            		System.err.println("Aborting");
            		_running = false;
            	}
            	if(catchup) {            		
            		_masterCycles += _masterIncrement;
            		_ppu.processInstructionsUntil(_masterCycles); // ppu catchup
                    _apu.clockAPU();
                    if(_singleDelayNMI){
                        _singleDelayNMI = false;
                        _ppu.triggerNMI();
                    }
                    if(_delayNMI) {
                        _delayNMI = false;
                        _singleDelayNMI = true;
                    }
            	}
            }
        }
        _running = false;
        if (NES_DEV_MODE) {
            System.out.println("Stopping");
        }
    }

    protected boolean isDelayTriggerNMI() {
        return _delayNMI;
    }
    protected void suppressDelayTriggerNMI() {
        _delayNMI = false;
    }

    protected void delayTriggerNMI() {
        _delayNMI = true;
    }

    private void preCheckInstruction() {
        if (_captureMode && _captureOut != null) {
            try {
                // log in the same format as other emulators use to make an easier side-by-side comparison
                // format is: ProcessCounter 3 bytes for Opcode Instruction (readable) (after value if storing)   A: X: Y: P: SP: CYC: SL:
                _captureOut.println(
                        _cpu.getCurrentInstructionDescription()
                        + " A:"
                        + ByteFormatter.formatByte(_cpu.getAccumulator())
                        + " X:"
                        + ByteFormatter.formatByte(_cpu.getXRegister())
                        + " Y:"
                        + ByteFormatter.formatByte(_cpu.getYRegister())
                        + " P:"
                        + ByteFormatter.formatByte(_cpu.getFlags())
                        + " SP:"
                        + ByteFormatter.formatByte(_cpu.getStackPointer())
                        + " CYC:"
                        + ByteFormatter.formatThreePlaces(_ppu.getScanlineCycle())
                        + " SL:"
                        + _ppu.getScanlineIndex()
                  );
               // _captureOut.println(" Before. [A=" + ByteFormatter.formatByte(_cpu.getAccumulator()) + "] [X=" + ByteFormatter.formatByte(_cpu.getXRegister()) + "] [Y=" + ByteFormatter.formatByte(_cpu.getYRegister()) + "] [Flags=" + ByteFormatter.formatBits(_cpu.getFlags()) + "]");
            } catch (Exception e) {
                e.printStackTrace();
                setCaptureMode(false, null);
            }
        }
        if (isBreakpoint(_cpu.getProgramCounter(), _cpu.getOpcode())) {
            pause();
        }
        if (_stepCPUInstruction) {

            forcePartialUpdates();
            try {
                synchronized (condVar) {
                    condVar.wait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                setDebuggingOff();
            }
        }
    }

    public boolean isPaused() {
        return (_stepCPUInstruction || _stepCPUCycle);
    }
    //NESDebuggerInterface
    public void pause() {
        _stepCPUInstruction = true;
        _stepCPUCycle = true;
        forceUpdates();
        _cpu.setMemoryWatchMode(true);
        synchronized (condVar) {
            condVar.notifyAll();
        }
    }

    public void stepNextFrame() {
        _stepPPUFrame = true;
        _stepPPULine = false;
        _stepCPUCycle = false;
        _stepCPUInstruction = false;
        synchronized (condVar) {
            condVar.notifyAll();
        }
    }

    public void stepNextScanline() {
        _stepPPUFrame = false;
        _stepPPULine = true;
        _stepCPUCycle = false;
        _stepCPUInstruction = false;
        synchronized (condVar) {
            condVar.notifyAll();
        }
    }

    public void stepCPUCycle() {
        _stepCPUCycle = true;
        _stepCPUInstruction = false;
        synchronized (condVar) {
            condVar.notifyAll();
        }
    }

    public void stepCPUInstruction() {
        _stepCPUInstruction = true;
        _stepCPUCycle = false;
        synchronized (condVar) {
            condVar.notifyAll();
        }
    }
    public void setMemoryWatchMode(boolean b){
        _memoryWatchMode = b;
        _cpu.setMemoryWatchMode(_breakpointList.size() > 0 || _opcodeBreakpointList.size() > 0 || _watchList.size() > 0 || _memoryWatchMode) ;
    }

    public void setPPUWatchMode(boolean b){
        _ppuOverride = b;
        forceUpdates();
    }

    public void resume() {
        setDebuggingOff();
        _cpu.setMemoryWatchMode(_breakpointList.size() > 0 || _opcodeBreakpointList.size() > 0 || _watchList.size() > 0 || _memoryWatchMode) ;
        synchronized (condVar) {
            condVar.notifyAll();
        }
    }

    public boolean isBreakpoint(int pc, byte opcode) {
        if (hasBreakpoints) {
        	// check pc
        	if(_breakpointList.contains(new Integer(pc))){
        		return true;
        	}
        	// check opcode
        	if(_opcodeBreakpointList.contains(new Byte(opcode))){
        		return true;
        	}
        
        }
        return false;
    }

    private void updateHasBreakpoints() {
    	hasBreakpoints = ((_breakpointList.size() > 0) || (_opcodeBreakpointList.size() > 0));
    }
    public void addCPUBreakpoint(int programCounter) {
        Integer iVal = new Integer(programCounter);
        if (!_breakpointList.contains(iVal)) {
            _breakpointList.add(iVal);
        }
        updateHasBreakpoints();
    }

    public void removeCPUBreakpoint(int programCounter) {
        Integer iVal = new Integer(programCounter);
        if (_breakpointList.contains(iVal)) {
            _breakpointList.remove(iVal);
        }
        updateHasBreakpoints();
    }

    public void addOpcodeBreakpoint(byte opcode) {
        Byte b = new Byte(opcode);
        if (!_opcodeBreakpointList.contains(b)) {
        	_opcodeBreakpointList.add(b);
        }
        updateHasBreakpoints();
    }

    public void removeOpcodeBreakpoint(byte opcode) {
        Byte b = new Byte(opcode);
        if (_opcodeBreakpointList.contains(b)) {
        	_opcodeBreakpointList.remove(b);
        }
        updateHasBreakpoints();
    }

    public void addWatch(int address, boolean isRead, boolean isWrite) {
        NESDebugWatch watch = new NESDebugWatch(address, isRead, isWrite);
        if (_watchList.contains(watch)) {
            // they are not EXACTLY the same. Remove the old, insert the new.
            _watchList.remove(watch);
        }
        _watchList.add(watch);     
        _cpu.setMemoryWatchMode(true);
    }

    public void removeWatch(int address) {
        NESDebugWatch watch = new NESDebugWatch(address, false, false);
        if (_watchList.contains(watch)) {
            // they are not EXACTLY the same. Remove the old, insert the new.
            _watchList.remove(watch);
        }
//        _cpu.setMemoryWatchMode(true);
        _cpu.setMemoryWatchMode(_breakpointList.size() > 0 || _watchList.size() > 0 || _memoryWatchMode);
    }

    public void startCPUCapture() {
        setCaptureMode(true, _comp);
    }

    public void endCPUCapture() {
        setCaptureMode(false, _comp);
    }
}
