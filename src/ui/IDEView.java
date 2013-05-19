/*
 * IDEView.java
 *
 * Created on September 7, 2006, 10:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

import ui.fontHelper.FontHelper;
import ui.romLoader.RomLoader;
import ui.utils.gameGenie.GameGenieCodeHelper;
import utilities.EnvironmentUtilities;
import utilities.GUIUtilities;
import emulator.nes.ui.NesDevEmulator;
import gameTools.imageHelper.ImageHelper;
import gameTools.levelEditor.HeckLifterLevelEditor;
import gameTools.playerAnimations.PlayerAnimator;
import gameTools.screenLayout.ScreenLayoutUI;
import gameTools.stampEditor.StampEditor;
import gameTools.stampSetEditor.StampSetEditor;

/**
 *
 * @author abailey
 */
public class IDEView {

	public final static String IDEVIEW_TITLE = "NES DEV Studio";
	private JFrame frame;
	private JDesktopPane desktop;

	//  CHREditorModel chrModel = new CHREditorModel();
	/** Creates a new instance of IDEView */
	public IDEView() {
		//new Foo();
		EnvironmentUtilities.initializeEnvironment();
		setupGUI();
		restoreSettings();
	}

	public void show() {
		frame.setVisible(true);
	}

	private void restoreSettings() {
	}


	public static void setUIFont (javax.swing.plaf.FontUIResource f){
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put (key, f);
		}
	}

	private void setupGUI() {
		frame = new JFrame(IDEVIEW_TITLE);

		desktop = new JDesktopPane();
		setupMenuBar();

		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				exitOperation();
			}
		});
		frame.getContentPane().add(desktop, BorderLayout.CENTER);
		frame.setSize(1024, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setupFileMenu(menuBar);
		setupGraphicsMenu(menuBar);
		//      setupMusicMenu(menuBar);
		setupUtilitiesMenu(menuBar);
		//      setupCustomGameToolsMenu(menuBar);
		setupHelpMenu(menuBar);
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);
	}

	private void setupFileMenu(JMenuBar menuBar) {
		JMenu fileMenu = GUIUtilities.createMenu(menuBar, "File", 'F');

		// setup File->Exit
		GUIUtilities.createMenuItem(fileMenu, "Exit", 'x', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				exitOperation();
			}
		});
	}

	private void setupUtilitiesMenu(JMenuBar menuBar) {
		JMenu utilsMenu = GUIUtilities.createMenu(menuBar, "Utilities", 'U');


		GUIUtilities.createMenuItem(utilsMenu, "Emulator", 'E', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchEmulator();
			}
		});



		/*        GUIUtilities.createMenuItem(utilsMenu, "Rom Loader", 'R', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                launchRomLoader();
            }
        });
		 */

		GUIUtilities.createMenuItem(utilsMenu, "Hecklifter Level Editor", 'H', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchLevelEditor();
			}
		});



		GUIUtilities.createMenuItem(utilsMenu, "Stamp Set Editor", 'H', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchStampSetEditor();
			}
		});

		GUIUtilities.createMenuItem(utilsMenu, "Stamp Editor", 'S', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchStampEditor();
			}
		});

		GUIUtilities.createMenuItem(utilsMenu, "Player Animator", 'P', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchPlayerAnimator();
			}
		});



		GUIUtilities.createMenuItem(utilsMenu, "Game Genie Code Helper", 'G', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchGameGenieTool();
			}
		});


	}

	private void setupGraphicsMenu(JMenuBar menuBar) {
		JMenu menu = GUIUtilities.createMenu(menuBar, "Graphics", 'G');

		// Graphics->CHR Screen Layout
		GUIUtilities.createMenuItem(menu, "Screen Layout Tool", 'S', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				invokeCHRLayoutOperation();
			}
		});

		GUIUtilities.createMenuItem(menu, "Image Helper Tool", 'I', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchImageToNES();
			}
		});

		
		GUIUtilities.createMenuItem(menu, "Font Helper", 'F', new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				launchFontHelper();
			}
		});


	}

	/*
    private void setupMusicMenu(JMenuBar menuBar){
    JMenu menu = GUIUtilities.createMenu(menuBar, "Music", 'M');

    // Graphics->CHR Screen Layout
    GUIUtilities.createMenuItem(menu, "MIDI Tool", 'M', new ActionListener()  {
    public void actionPerformed(ActionEvent ae) {
    JInternalFrame frame = new MidiExtractor();
    frame.setVisible(true); //necessary as of 1.3
    desktop.add(frame);
    try {
    frame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {
    }
    }
    }

    );

    }
    private void setupCustomGameToolsMenu(JMenuBar menuBar){
    JMenu gamesMenu = GUIUtilities.createMenu(menuBar, "Custom Game Tools", 'C');

    GUIUtilities.createMenuItem(gamesMenu, "Fight Game Editor", 'F', new ActionListener()  {
    public void actionPerformed(ActionEvent ae) {
    JInternalFrame frame = new FightingGameTool();
    frame.setVisible(true); //necessary as of 1.3
    desktop.add(frame);
    try {
    frame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {
    }
    }
    }
    );
    }
	 */
	private void setupHelpMenu(JMenuBar menuBar) {
		JMenu gamesMenu = GUIUtilities.createMenu(menuBar, "Help", 'H');

		GUIUtilities.createMenuItem(gamesMenu, "About", 'A', new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				JInternalFrame frame = new AboutFrame();
				frame.setVisible(true); //necessary as of 1.3

				desktop.add(frame);
				frame.pack();
				try {
					frame.setSelected(true);
				} catch (java.beans.PropertyVetoException e) {
				}
			}
		});
	}

	// returns false if cancel is pressed
	public boolean promptToSaveChanges() {
		// The previous changes will be lost. save it? (yes, no, cancel)
		// if choice was yes:   nesProject.saveRom(); return true;
		// if choice was no: return true;
		// if choice was cancel: return false;

		return false;
	}

	public void exitOperation() {
		EnvironmentUtilities.storeSettings();
		System.out.println("To DO.  Emit the shutdown method to each internal frame");
		System.exit(0);
	}

	private void launchInternalFrame(JInternalFrame internalFrame) {
		internalFrame.setVisible(true); //necessary as of 1.3
		desktop.add(internalFrame);
		try {
			internalFrame.setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
		}
	}

	public void launchEmulator() {
		launchInternalFrame(new NesDevEmulator());
	}

	public void launchRomLoader() {
		launchInternalFrame(new RomLoader());
	}

	private void launchImageToNES() {
		launchInternalFrame(new ImageHelper());
	}



	private void launchFontHelper() {
		launchInternalFrame(new FontHelper());
	}

	private void launchLevelEditor() {
		launchInternalFrame(new HeckLifterLevelEditor());
	}


	private void launchStampSetEditor() {
		launchInternalFrame(new StampSetEditor());
	}

	private void launchStampEditor() {
		launchInternalFrame(new StampEditor(null,null));
	}

	private void launchPlayerAnimator() {
		launchInternalFrame(new PlayerAnimator());
	}

	private void launchGameGenieTool() {
		launchInternalFrame(new GameGenieCodeHelper());
	}



	public void invokeCHRLayoutOperation() {
		launchInternalFrame(new ScreenLayoutUI());
	}
}
