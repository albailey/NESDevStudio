/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emulator.nes.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ui.chr.palette.PalettePanel;
import ui.chr.patternTable.PatternTablePanel;
import utilities.GUIUtilities;
import emulator.nes.PPU;
import emulator.nes.debugger.NESDebuggerInterface;

/**
 * 
 * @author abailey
 */
public class NESPPUMemoryPanel extends JPanel implements PPUViewInterface, Runnable {

	
	private static final long serialVersionUID = 8644105800556433586L;
	private MultiNameTablePanel ntPanel = null;
	private SpritePanel spritePanel = null;

	private PatternTablePanel _tilePanel1 = null;
	private PatternTablePanel _tilePanel2 = null;

	private PalettePanel _bgpaletteRegion = null;
	private PalettePanel _sprpaletteRegion = null;

	private NESDebuggerInterface _debugger = null;
	private PPU _ppu = null;
	private boolean _running = false;
	private Thread _thread = null;
	
	public NESPPUMemoryPanel(NESDebuggerInterface debugger) {
		super();
		_debugger = debugger;
		_ppu = null;
		_running = false;
		_thread = null;
		setupUI();
		
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
		_ppu = null;
	}

	public NESDebuggerInterface getDebugger() {
		return _debugger;
	}
	
	private void setupUI() {
		setLayout(new BorderLayout());
		JPanel mainPanel = setupNameTablePanel();
		add(mainPanel, BorderLayout.WEST);

		JPanel sidePanel = new JPanel();
		add(sidePanel, BorderLayout.EAST);
		sidePanel.setLayout(new BorderLayout());

		JPanel sprPanel = setupSpritesPanel();
		sidePanel.add(sprPanel, BorderLayout.NORTH);

		JPanel chrPanel = setupCHRPanel();
		sidePanel.add(chrPanel, BorderLayout.CENTER);

		JPanel palPanel = setupPalettePanel();
		add(palPanel, BorderLayout.SOUTH);

		add(new JPanel(), BorderLayout.CENTER); // empty center padding
	}

	private JPanel setupNameTablePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Name Tables"));
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout(gbl);
		GUIUtilities.initializeGBC(gbc);

		ntPanel = new MultiNameTablePanel(2 * 32 * 8, 2 * 30 * 8, 0.85f /* scale */);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbl.setConstraints(ntPanel, gbc);
		panel.add(ntPanel);
		panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 0));
		panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 1));
		return panel;
	}

	private JPanel setupCHRPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Tiles"));
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout(gbl);
		GUIUtilities.initializeGBC(gbc);

		_tilePanel1 = new PatternTablePanel(null, null, 0, "0x0000");
		_tilePanel2 = new PatternTablePanel(null, null, 1, "0x1000");

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbl.setConstraints(_tilePanel1, gbc);
		panel.add(_tilePanel1);

		gbc.gridy = 1;
		gbl.setConstraints(_tilePanel2, gbc);
		panel.add(_tilePanel2);

		panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 0));
		panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 2));
		return panel;
	}

	private JPanel setupSpritesPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Sprites"));
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout(gbl);
		GUIUtilities.initializeGBC(gbc);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		spritePanel = new SpritePanel(true, 8, 8);
		gbl.setConstraints(spritePanel, gbc);
		panel.add(spritePanel);
		panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 0));
		panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 1));

		return panel;
	}

	private JPanel setupPalettePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		_bgpaletteRegion = new PalettePanel(null, null, true, false, 16, 16,
				false, false, false);
		_bgpaletteRegion.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.add(_bgpaletteRegion);

		_sprpaletteRegion = new PalettePanel(null, null, false, true, 16, 16,
				false, false, false);
		_sprpaletteRegion.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.add(_sprpaletteRegion);
		return panel;
	}

	public void refreshFromPPU(PPU ppu) {
		_ppu = ppu;
	}
	public void run() {
		while(_running){
			if(_ppu != null){
				ntPanel.refreshFromPPU(_ppu);
				spritePanel.refreshFromPPU(_ppu);
				
				_tilePanel1.refreshFromPPU(_ppu);
				_tilePanel2.refreshFromPPU(_ppu);
				
				_bgpaletteRegion.refreshFromPPU(_ppu);
				_sprpaletteRegion.refreshFromPPU(_ppu);
				_ppu = null;
			}
    		try { Thread.sleep(200); } catch(Exception e){ e.printStackTrace(); }

		}
	
	}

}