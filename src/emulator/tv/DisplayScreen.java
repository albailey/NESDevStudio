/*
 * DisplayScreen.java
 *
 * Created on October 25, 2007, 12:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.tv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

import ui.chr.PPUConstants;

/**
 *
 * @author abailey
 */
public class DisplayScreen extends JPanel implements Display {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5608211239357931759L;
	private int pix[] = null;
    private MemoryImageSource mis = null;
    private Image img = null;
    private int currentScaledWidth = 0;
    private int currentScaledHeight = 0;
    private String _fps = "";
    //   private int rgb[] = null;

    /** Creates a new instance of DisplayScreen */
    public DisplayScreen() {
        super();
        setBackground(new Color(0, 0, 0, 0)); // transparent
        setMinimumSize(new Dimension(SCREEN_WIDTH / 8, SCREEN_HEIGHT / 8));
        setPreferredSize(new Dimension(SCREEN_WIDTH * 2, SCREEN_HEIGHT * 2));
        setFocusable(true);
        currentScaledWidth = SCREEN_WIDTH * 2;
        currentScaledHeight = SCREEN_HEIGHT * 2;

        pix = new int[SCREEN_WIDTH * SCREEN_HEIGHT];// all values are zero.
        mis = new MemoryImageSource(SCREEN_WIDTH, SCREEN_HEIGHT, pix, 0, SCREEN_WIDTH);
        mis.setAnimated(true);
        final Component focusable = this;
        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                focusable.requestFocusInWindow();
            }
        });
    //      setupColors();
    }

    private int intensifyColor(Color c, float rS, float gS, float bS) {
        int r = (int) ((c.getRed() * rS ) + 0.5);
        int g = (int) ((c.getGreen() * gS ) + 0.5);
        int b = (int) ((c.getBlue() * bS ) + 0.5);
        return (new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255))).getRGB();
    }

    public void plot(int x, int y, int val, boolean isMonochrome, boolean intensifyRed, boolean intensifyGreen, boolean intensifyBlue) {
        if (isMonochrome) {
            pix[y * SCREEN_WIDTH + x] = PPUConstants.NES_PALETTE[(val & 0x30)].getRGB();
        } else if (intensifyRed || intensifyGreen || intensifyBlue) {

            // http://wiki.nesdev.com/w/index.php/NTSC_video
            // relative = relative * 0.746
            // normalized = normalized * 0.746 - 0.0912

            float scaleRed = 0.7f;
            float scaleGreen = 0.7f;
            float scaleBlue = 0.7f;
            if (intensifyRed) {
                scaleRed = 1.3f;
            } else if (intensifyGreen) {
                scaleGreen = 1.3f;
            } else if (intensifyBlue) {
                scaleBlue = 1.3f;
            }
            pix[y * SCREEN_WIDTH + x] = intensifyColor(PPUConstants.NES_PALETTE[val], scaleRed, scaleGreen, scaleBlue);

        } else {
            pix[y * SCREEN_WIDTH + x] = PPUConstants.NES_PALETTE[val].getRGB();
        }
    }

    public void refreshDisplay() {
        mis.newPixels();       
    }

    public void clearDisplay() {
    	final int color = Color.black.getRGB(); // 0x00;
        for (int i = 0; i < pix.length; i++) {
            pix[i] = color;        	
        }
        mis.newPixels();    
        paintImmediately(getBounds());
    }

    public void setFPS(double fps) {
        _fps = "" + fps;
    }

    public void paintComponent(Graphics g) {
        if (img == null) {
            mis.newPixels();
            img = createImage(mis);
        }
        super.paintComponent(g);    // paints background
        g.drawImage(img, 0, 0, currentScaledWidth, currentScaledHeight, this);
        g.setColor(Color.WHITE);
        g.drawString(_fps, currentScaledWidth - 100, 20);
    }
}
