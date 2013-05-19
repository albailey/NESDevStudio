/*
 * NESPPUPanel.java
 *
 * Created on November 8, 2007, 5:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import utilities.ByteFormatter;
import utilities.GUIUtilities;
import emulator.nes.PPU;
import emulator.nes.debugger.NESDebuggerInterface;

/**
 *
 * @author abailey
 */
public class NESPPUPanel extends JPanel implements PPUViewInterface, Runnable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2485257314572882999L;
	// registers
    private JTextField _ctrlField = null;
    private JTextField _baseNameTableField = null;
    private JTextField _vramIncrementField = null;
    private JTextField _spriteTableAddressField = null;
    private JTextField _bgTableAddressField = null;
    private JTextField _spriteSizeField = null;
    private JCheckBox _nmiEnabledCB = null;
    private JTextField _maskField = null;
    private JCheckBox _intensifyBluesCB = null;
    private JCheckBox _intensifyGreensCB = null;
    private JCheckBox _intensifyRedsCB = null;
    private JCheckBox _enableSpriteRenderingCB = null;
    private JCheckBox _enableBGRenderingCB = null;
    private JCheckBox _displaySpritesLeftmostCB = null;
    private JCheckBox _displayBGLeftmostCB = null;
    private JCheckBox _grayscaleCB = null;
    private JTextField _statusField = null;
    private JCheckBox _vblStatusCB = null;
    private JCheckBox _spriteZeroCB = null;
    private JCheckBox _spriteOverflowCB = null;
    private JTextField _xScrollField = null;
    private JTextField _yScrollField = null;
    private JTextField _latchField = null;
    private JTextField _readBufferField = null;
    private JTextField _loopyVField = null;

    private JTextField _scanlineField = null;
    private JTextField _scanlineCycleField = null;
    private SpritePanel scanlineSpritePanel = null;
    private NESDebuggerInterface _debugger = null;

    private PPU _ppu = null;
	private boolean _running = false;
	private Thread _thread = null; 
	
    /**
     * Creates a new instance of NESPPUPanel
     */
    public NESPPUPanel(NESDebuggerInterface debugger) {
        super();
        _debugger = debugger;
        _ppu = null;
		_running = false;
		_thread = null;
        setupUI();
    }
    public NESDebuggerInterface getDebugger() {
		return _debugger;
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
		_ppu = null;
		if(_thread != null) {
			try { _thread.join(); } catch(Exception e){ e.printStackTrace(); }
			_thread = null;
		}
	}
    private void setupUI() {
        setBorder(new TitledBorder("PPU"));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // a special controls mode panel
        JPanel registersPanel = setupRegistersPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(registersPanel, gbc);
        add(registersPanel);

        add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 0));
        add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 1));

    }



    private JPanel setupScanlinePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Current Scanline"));
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        panel.add(GUIUtilities.createLabel("Sprites", "Scanline Sprites", gbc, gbl, 0, 0));
        scanlineSpritePanel = new SpritePanel(false, 8, 1);
        gbc.gridx = 1;
        gbl.setConstraints(scanlineSpritePanel, gbc);
        panel.add(scanlineSpritePanel);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 0));

        panel.add(GUIUtilities.createLabel("Scanline", "Current Scanline", gbc, gbl, 0, 1));
        _scanlineField = GUIUtilities.createTextField("", "Scanline", 10, false, gbc, gbl, 1, 1);
        panel.add(_scanlineField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 1));

        panel.add(GUIUtilities.createLabel("Cycle", "Cycle for Current Scanline", gbc, gbl, 0, 2));
        _scanlineCycleField = GUIUtilities.createTextField("", "Scanline Cycle", 10, false, gbc, gbl, 1, 2);
        panel.add(_scanlineCycleField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 2));

        panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 3));

        return panel;
    }


    private JPanel setupRegistersPanel() {
        JPanel regPanel = new JPanel();
        regPanel.setBorder(new TitledBorder("Registers"));
        regPanel.setLayout(new BorderLayout());
        regPanel.add(setupLeftRegistersPanel(),  BorderLayout.WEST);
        regPanel.add(setupRightRegistersPanel(), BorderLayout.CENTER);
        return regPanel;
    }

    private JPanel setupLeftRegistersPanel() {
        JPanel regPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        regPanel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JPanel flagsPanel = setupControlPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 7;
        gbl.setConstraints(flagsPanel, gbc);
        regPanel.add(flagsPanel);


        JPanel doublePanel = new JPanel();
        doublePanel.setLayout(new BorderLayout());

        JPanel statusPanel = setupStatusPanel();
        doublePanel.add(statusPanel, BorderLayout.NORTH);


        JPanel scanPanel = setupScanlinePanel();

        doublePanel.add(scanPanel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy += gbc.gridheight;
        gbc.gridheight = 4;
        gbl.setConstraints(doublePanel, gbc);
        regPanel.add(doublePanel);

        return regPanel;
    }

    private JPanel setupRightRegistersPanel() {
        JPanel regPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        regPanel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JPanel maskPanel = setupMaskPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 9;
        gbl.setConstraints(maskPanel, gbc);
        regPanel.add(maskPanel);

        JPanel remainderPanel = setupRemainderPanel();
        gbc.gridx = 1;
        gbc.gridy += gbc.gridheight;
        gbc.gridheight = 3;
        gbl.setConstraints(remainderPanel, gbc);
        regPanel.add(remainderPanel);

/*        JPanel dbgPanel = setupDebuggerPanel();
        gbc.gridx = 1;
        gbc.gridy += gbc.gridheight;
        gbc.gridheight = 1;
        gbl.setConstraints(dbgPanel, gbc);
        regPanel.add(dbgPanel);
*/
        return regPanel;
    }

    /*
    PPUCTRL ($2000)
    Various flags controlling PPU operation (write)
    76543210
    ||||||||
    ||||||++- Base nametable address
    ||||||    (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
    |||||+--- VRAM address increment per CPU read/write of PPUDATA
    |||||     (0: increment by 1, going across; 1: increment by 32, going down)
    ||||+---- Sprite pattern table address for 8x8 sprites (0: $0000; 1: $1000)
    |||+----- Background pattern table address (0: $0000; 1: $1000)
    ||+------ Sprite size (0: 8x8; 1: 8x16)
    |+------- PPU master/slave select (has no effect on the NES)
    +-------- Generate an NMI at the start of the
    vertical blanking interval (0: off; 1: on)
     */
    private JPanel setupControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(new TitledBorder("Control ($2000)"));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        controlPanel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;

        controlPanel.add(GUIUtilities.createLabel("PPUCTRL", "0x2000 PPU CTRL", gbc, gbl, 0, 0));
        _ctrlField = GUIUtilities.createTextField("", "0x2000 PPU CTRL", 10, false, gbc, gbl, 1, 0);
        controlPanel.add(_ctrlField);

        controlPanel.add(GUIUtilities.createLabel("Base Nametable", "Base Nametable", gbc, gbl, 0, 1));
        _baseNameTableField = GUIUtilities.createTextField("", "Base Nametable", 6, false, gbc, gbl, 1, 1);
        controlPanel.add(_baseNameTableField);

        controlPanel.add(GUIUtilities.createLabel("VRAM increment", "VRAM read/write increment", gbc, gbl, 0, 2));
        _vramIncrementField = GUIUtilities.createTextField("", "VRAM read/write increment", 6, false, gbc, gbl, 1, 2);
        controlPanel.add(_vramIncrementField);

        controlPanel.add(GUIUtilities.createLabel("Sprite Address", "Sprite Table Base Address", gbc, gbl, 0, 3));
        _spriteTableAddressField = GUIUtilities.createTextField("", "Sprite Table Base Address", 6, false, gbc, gbl, 1, 3);
        controlPanel.add(_spriteTableAddressField);

        controlPanel.add(GUIUtilities.createLabel("Background Address", "Background Base Address", gbc, gbl, 0, 4));
        _bgTableAddressField = GUIUtilities.createTextField("", "Background Base Address", 6, false, gbc, gbl, 1, 4);
        controlPanel.add(_bgTableAddressField);

        controlPanel.add(GUIUtilities.createLabel("Sprite Size", "Sprite Size", gbc, gbl, 0, 5));
        _spriteSizeField = GUIUtilities.createTextField("", "Sprite Size", 6, false, gbc, gbl, 1, 5);
        controlPanel.add(_spriteSizeField);

        _nmiEnabledCB = GUIUtilities.createCheckBox("NMI Enabled", "NMI Enabled", false, gbc, gbl, 0, 6);
        controlPanel.add(_nmiEnabledCB);

        controlPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 0));
        controlPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 1));
        controlPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 2));
        controlPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 3));
        controlPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 4));
        controlPanel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 5));
        controlPanel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 6));

        return controlPanel;
    }

    /*
    PPUMASK ($2001)
    Screen enable, masking, and intensity (write)

    76543210
    ||||||||
    |||||||+- Grayscale (0: normal color; 1: AND all palette entries
    |||||||   with 0x30, effectively producing a monochrome display;
    |||||||   note that colour emphasis STILL works when this is on!)
    ||||||+-- Enable background in leftmost 8 pixels of screen (0: clip; 1: display)
    |||||+--- Enable sprite in leftmost 8 pixels of screen (0: clip; 1: display)
    ||||+---- Enable background rendering
    |||+----- Enable sprite rendering
    ||+------ Intensify reds (and darken other colors)
    |+------- Intensify greens (and darken other colors)
    +-------- Intensify blues (and darken other colors)
     */
    private JPanel setupMaskPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Mask ($2001)"));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;

        JPanel miniPanel = new JPanel();
        miniPanel.setLayout(new BorderLayout(4, 4));
        JLabel maskLabel = new JLabel("PPUMASK");
        maskLabel.setToolTipText("0x2001 PPU Mask");
        miniPanel.add(maskLabel, BorderLayout.WEST);

        _maskField = new JTextField(10);
        _maskField.setToolTipText("0x2001 PPU Mask");
        _maskField.setEditable(false);
        miniPanel.add(_maskField, BorderLayout.CENTER);
        gbl.setConstraints(miniPanel, gbc);
        panel.add(miniPanel);

        _intensifyBluesCB = GUIUtilities.createCheckBox("Intensify Blues", "Intensify Blues", false, gbc, gbl, 0, 1);
        panel.add(_intensifyBluesCB);

        _intensifyGreensCB = GUIUtilities.createCheckBox("Intensify Greens", "Intensify Greens", false, gbc, gbl, 0, 2);
        panel.add(_intensifyGreensCB);

        _intensifyRedsCB = GUIUtilities.createCheckBox("Intensify Reds", "Intensify Reds", false, gbc, gbl, 0, 3);
        panel.add(_intensifyRedsCB);

        _enableSpriteRenderingCB = GUIUtilities.createCheckBox("Enable SPR Rendering", "Enable Sprite Rendering", false, gbc, gbl, 0, 4);
        panel.add(_enableSpriteRenderingCB);

        _enableBGRenderingCB = GUIUtilities.createCheckBox("Enable BG Rendering", "Enable Background Rendering", false, gbc, gbl, 0, 5);
        panel.add(_enableBGRenderingCB);

        _displaySpritesLeftmostCB = GUIUtilities.createCheckBox("Display SPR Leftmost", "Display Sprites in Leftmost 8 Pixels", false, gbc, gbl, 0, 6);
        panel.add(_displaySpritesLeftmostCB);

        _displayBGLeftmostCB = GUIUtilities.createCheckBox("Display BG Leftmost", "Display Background in Leftmost 8 Pixels", false, gbc, gbl, 0, 7);
        panel.add(_displayBGLeftmostCB);

        _grayscaleCB = GUIUtilities.createCheckBox("Grayscale", "Grayscale", false, gbc, gbl, 0, 8);
        panel.add(_grayscaleCB);

        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 0));
        panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 9));
        return panel;
    }

    private JPanel setupStatusPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Status ($2002)"));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.weightx = 0;


        panel.add(GUIUtilities.createLabel("PPU Status", "0x2002 PPU Status", gbc, gbl, 0, 0));
        _statusField = GUIUtilities.createTextField("", "0x2002 PPU Status", 8, false, gbc, gbl, 1, 0);
        panel.add(_statusField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 0));



        gbc.gridwidth = 2;
        _vblStatusCB = GUIUtilities.createCheckBox("VBlank (D7)", "VBlank (D7)", false, gbc, gbl, 0, 1);
        panel.add(_vblStatusCB);
        gbc.gridwidth = 1;
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 1));

        gbc.gridwidth = 2;
        _spriteZeroCB = GUIUtilities.createCheckBox("Sprite Zero (D6)", "VBlank (D6)", false, gbc, gbl, 0, 2);
        panel.add(_spriteZeroCB);
        gbc.gridwidth = 1;
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 2));

        gbc.gridwidth = 2;
        _spriteOverflowCB = GUIUtilities.createCheckBox("Sprite Overflow (D5)", "Sprite Overflow (D5)", false, gbc, gbl, 0, 3);
        panel.add(_spriteOverflowCB);
        gbc.gridwidth = 1;
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 1, 3));


        panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 4));

        return panel;
    }

    private JPanel setupRemainderPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Other"));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(gbl);
        GUIUtilities.initializeGBC(gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
      

        panel.add(GUIUtilities.createLabel("X Scroll", "X Scroll", gbc, gbl, 0, 0));
        _xScrollField = GUIUtilities.createTextField("", "X Scroll", 4, false, gbc, gbl, 1, 0);
        panel.add(_xScrollField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 0));

        panel.add(GUIUtilities.createLabel("Y Scroll", "Y Scroll", gbc, gbl, 0, 1));
        _yScrollField = GUIUtilities.createTextField("", "Y Scroll", 4, false, gbc, gbl, 1, 1);
        panel.add(_yScrollField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 1));

        panel.add(GUIUtilities.createLabel("2007 (latch)", "2007 (latch)", gbc, gbl, 0, 2));
        _latchField = GUIUtilities.createTextField("", "2007 (latch)", 4, false, gbc, gbl, 1, 2);
        panel.add(_latchField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 2));

        panel.add(GUIUtilities.createLabel("2007 (read Buffer)", "2007 (read Buffer)", gbc, gbl, 0, 3));
        _readBufferField = GUIUtilities.createTextField("", "2007 (read Buffer)", 4, false, gbc, gbl, 1, 3);
        panel.add(_readBufferField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 3));

        panel.add(GUIUtilities.createLabel("loopyV", "loopyV", gbc, gbl, 0, 4));
        _loopyVField = GUIUtilities.createTextField("", "loopyV", 6, false, gbc, gbl, 1, 4);
        panel.add(_loopyVField);
        panel.add(GUIUtilities.createFillerWidth(gbc, gbl, 2, 3));

        panel.add(GUIUtilities.createFillerHeight(gbc, gbl, 0, 3));

        return panel;
    }

    private void refreshControlPanel(byte dataByte) {
        _ctrlField.setText(ByteFormatter.formatBits(dataByte));
        _baseNameTableField.setText("" + (2000 + ((dataByte & 0x3) * 400)));
        _vramIncrementField.setText(((dataByte & 0x4) == 0x4) ? "32 (down)" : "1 (across)");
        _spriteTableAddressField.setText(((dataByte & 0x8) == 0x8) ? "0x1000" : "0x0000");
        _bgTableAddressField.setText(((dataByte & 0x10) == 0x10) ? "0x1000" : "0x0000");
        _spriteSizeField.setText(((dataByte & 0x20) == 0x20) ? "8x16" : "8x8");
        _nmiEnabledCB.setSelected((dataByte & 0x80) == 0x80);
    }

    private void refreshMaskPanel(byte dataByte) {
        _maskField.setText(ByteFormatter.formatBits(dataByte));
        _intensifyBluesCB.setSelected((dataByte & 0x80) == 0x80);
        _intensifyGreensCB.setSelected((dataByte & 0x40) == 0x40);
        _intensifyRedsCB.setSelected((dataByte & 0x20) == 0x20);
        _enableSpriteRenderingCB.setSelected((dataByte & 0x10) == 0x10);
        _enableBGRenderingCB.setSelected((dataByte & 0x8) == 0x8);
        _displaySpritesLeftmostCB.setSelected((dataByte & 0x4) == 0x4);
        _displayBGLeftmostCB.setSelected((dataByte & 0x2) == 0x2);
        _grayscaleCB.setSelected((dataByte & 0x1) == 0x1);
    }

    private void refreshStatusPanel(byte dataByte) {
        _statusField.setText(ByteFormatter.formatBits(dataByte));
        _vblStatusCB.setSelected((dataByte & 0x80) == 0x80);
        _spriteZeroCB.setSelected((dataByte & 0x40) == 0x40);
        _spriteOverflowCB.setSelected((dataByte & 0x20) == 0x20);
    }

    private void refreshRemainderPanel(PPU ppu) {
        _xScrollField.setText("0x" + ByteFormatter.formatByte(ppu.getXScrollDirect()));
        _yScrollField.setText("0x" + ByteFormatter.formatByte(ppu.getYScrollDirect()));
        _latchField.setText("0x" + ByteFormatter.formatByte(ppu.getLatchDirect()));
        _readBufferField.setText("0x" + ByteFormatter.formatByte(ppu.getReadBufferDirect()));
        _loopyVField.setText("0x" + ByteFormatter.formatInt(ppu.getLoopyVDirect()));
    }

    private void refreshScanlinePanel(PPU ppu) {
        _scanlineField.setText("" + ppu.getScanlineIndex());
        _scanlineCycleField.setText("" + ppu.getScanlineCycle());
        scanlineSpritePanel.refreshFromPPU(ppu);
    }

    public void refreshFromPPU(PPU ppu) {
		_ppu = ppu;
	}
	public void run() {
		while(_running){
			if(_ppu != null){
				doRefresh();
				_ppu = null;
			}
    		try { Thread.sleep(200); } catch(Exception e){ e.printStackTrace(); }

		}
	}
	public void doRefresh() {

        refreshControlPanel(_ppu.getPPUCTRLDirect());
        refreshMaskPanel(_ppu.getPPUMASKDirect());
        refreshStatusPanel(_ppu.getPPUStatusDirect());
        refreshRemainderPanel(_ppu);
        refreshScanlinePanel(_ppu);
    }
}
