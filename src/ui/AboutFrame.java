/*
 * AboutFrame.java
 *
 * Created on April 16, 2008, 4:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author abailey
 */
public class AboutFrame extends JInternalFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6623064363718766659L;
	public final static String FRAME_TITLE = "About";
    public final static int FRAME_WIDTH = 140;
    public final static int FRAME_HEIGHT = 80;
    public final static String PRODUCT_NAME = "NES Dev Studio";
    public final static int PRODUCT_VERSION_MAJOR = 0;
    public final static int PRODUCT_VERSION_MINOR = 0;
    public final static int PRODUCT_VERSION_MICRO = 19; // incremented May 24, 2011
    
    // Remeber:
    // Project Properties -> Build -> Compiling ->Generate Debugging Info
    // to reduce overall jarsize
    //
    /** Creates a new instance of AboutFrame */
    public AboutFrame() {
        super(FRAME_TITLE, true, true, false, false);
        setupUI();
    }

    private void setupUI() {
        getContentPane().setLayout(new BorderLayout());

        JPanel aboutRegion = new JPanel();
        aboutRegion.setLayout(new BorderLayout());
        aboutRegion.add(new JLabel("NES Dev Studio " + PRODUCT_VERSION_MAJOR + "." + PRODUCT_VERSION_MINOR + "." + PRODUCT_VERSION_MICRO), BorderLayout.NORTH);
        getContentPane().add(aboutRegion, BorderLayout.CENTER);
        setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setMaximumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        pack();
        //Set the window's location.
        setLocation(0, 0);
    }
}
