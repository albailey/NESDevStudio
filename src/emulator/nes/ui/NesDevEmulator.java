/*
 * NesDevEmulator.java
 *
 * Created on October 23, 2007, 1:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.DefaultSingleSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import ui.chr.model.CHREditorModel;
import ui.testrom.TestRomHelper;
import utilities.EnvironmentUtilities;
import utilities.FileUtilities;
import utilities.GUIUtilities;
import emulator.nes.NES;
import emulator.nes.NESCartridge;
import emulator.tv.DisplayScreen;
import gameTools.screenLayout.ScreenLayoutUI;

/**
 *
 * @author abailey
 */
public class NesDevEmulator extends JInternalFrame implements KeyEventDispatcher {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5880127781576381069L;
	
	public final static String FRAME_TITLE = "Nes Dev Emulator";
    private final static String NES_EMULATOR_LAST_ROM_FILE_PROPERTY = "NES_EMULATOR_LAST_ROM_FILE_PROPERTY";
    private final static String NES_EMULATOR_RECENT_ROM_FILE_PROPERTY = "NES_EMULATOR_RECENT_ROM_FILE_PROPERTY";
    private final static String RECENT_SEPARATOR_TOKEN = "<RECENT->";

    private final static boolean DEV_MODE = true;
    private String _romFileName;
    private NES _nes;
    private DisplayScreen _screen;
    private NESMemoryPanel _memPanel = null;
    private CPU6502ModelPanel _cpuPanel = null;
    private NESPPUPanel _ppuPanel = null;
    private NESPPUMemoryPanel _ppuMemPanel = null;
    private NESAPUPanel _apuPanel = null;
    private KeyboardFocusManager _kbdManager = null;
    private JMenu recentFilesMenu = null;

    public NesDevEmulator() {
        super(FRAME_TITLE, true, true, false, false);
        _romFileName = null;
        _nes = new NES(this);
        _screen = new DisplayScreen();
        _nes.setDisplay(_screen);
        setupUI(true);
        addInternalFrameListener(new InternalFrameAdapter() {

            public void internalFrameClosing(InternalFrameEvent e) {
                powerOff();
            }

            public void internalFrameActivated(InternalFrameEvent e) {
                resumeRunning();
            }

            public void internalFrameDeactivated(InternalFrameEvent e) {
                pauseRunning();
            }
        });
    }

    private void setupUI(boolean autoLoad) {
        getContentPane().setLayout(new BorderLayout());

        setupDebuggerPanel();

        // I need:
        // 1) Display
        // 2) Memory View
        // 3) CPU flags
        // 4) PPU stuff
        JPanel mainControls = new JPanel();
        mainControls.setLayout(new BorderLayout());
        mainControls.add(_screen, BorderLayout.CENTER);
        JPanel iconPanel = setupControlsPanel();
        mainControls.add(iconPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(mainControls, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        JScrollPane scrollPane2 = new JScrollPane(tabbedPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getContentPane().add(scrollPane2, BorderLayout.EAST);

        if (DEV_MODE) {
            _cpuPanel = new CPU6502ModelPanel();
            _nes.addCPU6502View(_cpuPanel);
            tabbedPane.add("CPU", _cpuPanel);
            _cpuPanel.visitRegistration(_nes);

            _memPanel = new NESMemoryPanel();
            _nes.addMemoryModelListener(_memPanel);
            tabbedPane.add("Memory", _memPanel);

            _ppuPanel = new NESPPUPanel(_nes.getDebugger());
            _nes.addPPUView(_ppuPanel);
            tabbedPane.add("PPU", _ppuPanel);

            _ppuMemPanel = new NESPPUMemoryPanel(_nes.getDebugger());
            _nes.addPPUView(_ppuMemPanel);
            tabbedPane.add("PPU Memory", _ppuMemPanel);

       //     if(APU_SUPPORTED){
       //     _apuPanel = new NESAPUPanel(_nes.getAPU());
       //     tabbedPane.add("APU", _apuPanel);
       //     }


            final DefaultSingleSelectionModel tabModel = (DefaultSingleSelectionModel) tabbedPane.getModel();
            tabModel.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    _nes.setMemoryWatchMode(tabModel.getSelectedIndex() == 2); // memory tab
                    _nes.setPPUWatchMode(tabModel.getSelectedIndex() == 3 || tabModel.getSelectedIndex() == 4); // memory tab
                }
            });
        }
        _kbdManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        _kbdManager.addKeyEventDispatcher(this);


        pack();

        //Set the window's location.
        setLocation(0, 0);

        if (autoLoad) {
            loadLastROM();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent e) {
        if (_screen.isFocusOwner()) {
            return _nes.processKeyEvent(e);
        } else {
            return false;
        }
    }

    private void clearOpenRecentList() {
        EnvironmentUtilities.updateStringEnvSetting(NES_EMULATOR_RECENT_ROM_FILE_PROPERTY, "");
        recentFilesMenu.removeAll();
    }

     private void addToOpenRecentList(String newEntry) {
        String recentROMFiles = EnvironmentUtilities.getStringEnvSetting(NES_EMULATOR_RECENT_ROM_FILE_PROPERTY, null);
        boolean entryFound = false;

        if(recentROMFiles != null  && recentROMFiles.length() > 0) {
            if(recentROMFiles.indexOf(newEntry) != -1){
                // it may contain the newEntry. I'm not picky.
                entryFound = true;
            }
        }

        if(!entryFound) {
            if (newEntry != null && newEntry.length() > 0) {
                final File selectedFile = new File(newEntry);
                if(selectedFile.canRead()) {
                    GUIUtilities.createMenuItem(recentFilesMenu, newEntry,(char)0, new ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                            loadROM(selectedFile);
                        }
                    });
                }
            }
            if(recentROMFiles != null && recentROMFiles.length() > 0) {
                recentROMFiles = RECENT_SEPARATOR_TOKEN + newEntry +  recentROMFiles; //this inserts it at the front
            } else {
                recentROMFiles = RECENT_SEPARATOR_TOKEN + newEntry; //this inserts it at the front
            }
        }
        EnvironmentUtilities.updateStringEnvSetting(NES_EMULATOR_RECENT_ROM_FILE_PROPERTY, recentROMFiles);
     }

     
     private void openROMEnclosingFolder() {
         try {
             Desktop desktop = Desktop.getDesktop();
             if(_romFileName != null && _romFileName.length() > 0) {
                 File romFile = new File(_romFileName);
                 if(romFile != null && romFile.canRead()) {
                     File dirFile = romFile.getParentFile();
                     if(dirFile != null && dirFile.isDirectory()) {
                         desktop.open(dirFile);
                     }
                 }
             }
         } catch(Exception e) {
             e.printStackTrace();
         }
     }

     private void extractGraphics() {
        	 try {
                 CHREditorModel newModelRef = new CHREditorModel();
                 if (TestRomHelper.extractFromEmulator(newModelRef, _nes)) {
                     ScreenLayoutUI frame = new ScreenLayoutUI(newModelRef);
                     frame.setVisible(true); //necessary as of 1.3
                     getParent().add(frame);
                     frame.setSelected(true);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }      	 
     }

     private void populateOpenRecentList(JMenu menu) {
        recentFilesMenu = menu;
        String recentROMFiles = EnvironmentUtilities.getStringEnvSetting(NES_EMULATOR_RECENT_ROM_FILE_PROPERTY, null);
        clearOpenRecentList();
        if(recentROMFiles != null && recentROMFiles.length() > 0) {
            int offset = recentROMFiles.indexOf(RECENT_SEPARATOR_TOKEN);
            String remainder = recentROMFiles;
            String newEntry = null;
            while(offset != -1){
               offset += RECENT_SEPARATOR_TOKEN.length();
               remainder = remainder.substring(offset);
               offset = remainder.indexOf(RECENT_SEPARATOR_TOKEN);
               if(offset == -1){
                    newEntry = remainder;
               } else {
                    newEntry = remainder.substring(0, offset);
               }
               addToOpenRecentList(newEntry);
            }
        }
    }

    private JMenuBar setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        //      this.setJMenuBar(menuBar);

        JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');
        GUIUtilities.createMenuItem(fileMenu, "Load ROM", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadROM();
            }
        });


        JMenu recentMenu = GUIUtilities.createSubMenu(fileMenu, "Open Recent", 'O');

        populateOpenRecentList(recentMenu);
        GUIUtilities.createMenuItem(fileMenu, "Clear Open Recent List", 'C', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                clearOpenRecentList();
            }
        });

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                GUIUtilities.createMenuItem(fileMenu, "Open ROM's Folder", 'F', new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                            openROMEnclosingFolder();
                    }
                });
            }

        }

        JMenu extractMenu = GUIUtilities.createMenu(menuBar, "Extract", 'E');
        GUIUtilities.createMenuItem(extractMenu, "Extract Graphics", 'E', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                extractGraphics();
            }
        });
        
 
        return menuBar;
    }

    private void setupDebuggerPanel() {
        if (DEV_MODE) {
            CPUDebuggerPanel debugPanel = new CPUDebuggerPanel(_nes.getDebugger());
            getContentPane().add(debugPanel, BorderLayout.SOUTH);
        }
    }

    private JPanel setupControlsPanel() {

        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BorderLayout());

        JMenuBar menuBar = setupMenuBar();
        combinedPanel.add(menuBar, BorderLayout.NORTH);

        JPanel iconPanel = new JPanel();
        combinedPanel.add(iconPanel, BorderLayout.CENTER);

        iconPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        // Should add icon buttons for frequent actions like reset, etc..
        {
            String tooltip = "Hard Reset";
            String imagePath = "emulator/nes/ui/reset.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _nes.hardReset();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            iconPanel.add(button);

        }
        {
            String tooltip = "Soft Reset";
            String imagePath = "emulator/nes/ui/reset.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    _nes.softReset();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            iconPanel.add(button);

        }
        // Should add icon buttons for frequent actions like reset, etc..
        {
            String tooltip = "Power Off";
            String imagePath = "emulator/nes/ui/powerOff.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                	powerOff();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            iconPanel.add(button);
        }
        // Should add icon buttons for frequent actions like reset, etc..
        {
            String tooltip = "Power On";
            String imagePath = "emulator/nes/ui/powerOn.png";
            ActionListener actListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    powerOn();
                }
            };
            JButton button = null;
            ImageIcon icon = GUIUtilities.createImageIcon(imagePath, tooltip);
            if (icon == null) {
                button = new JButton(tooltip);
            } else {
                button = new JButton(icon);
            }
            button.setToolTipText(tooltip);
            button.addActionListener(actListener);
            iconPanel.add(button);
        }

        return combinedPanel;
    }

    private void powerOff() {
    	_nes.powerOff();
    }
    private void powerOn() {
    	_nes.powerOn();
    }
	private void pauseRunning(){
		if(DEV_MODE) {
			 _cpuPanel.stopRunning();
			 _memPanel.stopRunning();
			 _ppuPanel.stopRunning();
			 _ppuMemPanel.stopRunning();			 
		}
		_nes.pause();
	}
	private void resumeRunning(){
    	_nes.resume();
		if(DEV_MODE) {
			 _cpuPanel.startRunning();
			 _memPanel.startRunning();
			 _ppuPanel.startRunning();
			 _ppuMemPanel.startRunning();
		}
	}

	
    private void loadLastROM() {
        String lastROMFile = EnvironmentUtilities.getStringEnvSetting(NES_EMULATOR_LAST_ROM_FILE_PROPERTY, null);
        try {
            if (lastROMFile != null && lastROMFile.length() > 0) {
                File selectedFile = new File(lastROMFile);
                loadROM(selectedFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            EnvironmentUtilities.updateStringEnvSetting(NES_EMULATOR_LAST_ROM_FILE_PROPERTY, "");
        }
    }

    private void loadROM() {
    	_nes.pause();
        File selectedFile = FileUtilities.selectFileForOpen(this);
        _nes.resume();
        loadROM(selectedFile);
    }

    private boolean loadROM(File selectedFile) {
        if (selectedFile == null) {
            return false;
        }
        powerOff();
        _romFileName = selectedFile.getAbsolutePath();
        EnvironmentUtilities.updateStringEnvSetting(NES_EMULATOR_LAST_ROM_FILE_PROPERTY, _romFileName);
        NESCartridge cart = NESCartridge.createCartridge(_romFileName);
        if (cart != null) {
            if (_cpuPanel != null) {
                _cpuPanel.setHeader(cart.header, _romFileName);
            }
            _nes.loadCartridge(cart);
            addToOpenRecentList(_romFileName);
        }
        powerOn();
        return true;
    }
}
