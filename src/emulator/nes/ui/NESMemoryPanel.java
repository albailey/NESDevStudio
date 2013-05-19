/*
 * NESMemoryPanel.java
 *
 * Created on November 8, 2007, 5:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.fife.ui.hex.swing.HexEditor;

import emulator.core.CPU6502.mvc.CPU6502MemoryModelInterface;
import emulator.nes.PPU;

/**
 *
 * @author abailey
 */
public class NESMemoryPanel extends JPanel implements CPU6502MemoryModelInterface, Runnable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6514503053674880744L;
	private HexEditor hexEditor = null;

	private byte[] _memory = null;
	private boolean _running = false;
	private Thread _thread = null;
	
    /**
     * Creates a new instance of NESMemoryPanel
     */
    public NESMemoryPanel() {
        super();
        setBorder(new TitledBorder("Memory"));
        setLayout(new BorderLayout());

        hexEditor = new HexEditor();
        hexEditor.setReadOnly(true);
        add(hexEditor, BorderLayout.WEST);
        add(new JPanel(), BorderLayout.CENTER);
        
        _memory = null;
		_running = false;
		_thread = null;
    }
    public void startRunning() {
		if(_running){
			return;
		}
		_running = true;
		_thread = new Thread(this);
		_thread.start();		
	}
	public void stopRunning() {
		_running = false;
		if(_thread != null) {
			try { _thread.join(); } catch(Exception e){ e.printStackTrace(); }
			_thread = null;
		}
		_memory = null;
	}


    public void updateWriteMemory(int i, byte val) {
    	if(_memory != null){
    		_memory[i] = val;
    	}
        //hexEditor.replaceByte(i, val);
    }

    public void updateMemory(byte memory[]) {
    	_memory = memory;
    }
    public void run() {
		while(_running){
			if(_memory != null){
				doRefresh();
			//	_memory = null;
			}
    		try { Thread.sleep(500); } catch(Exception e){ e.printStackTrace(); }
		}
	
	}
    public void doRefresh() {
        hexEditor.replaceBytes(0, hexEditor.getByteCount(), _memory);
    }

}
