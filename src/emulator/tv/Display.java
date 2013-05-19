/*
 * Display.java
 *
 * Created on October 25, 2007, 1:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.tv;

/**
 *
 * @author abailey
 */
public interface Display {

    public final static int SCREEN_WIDTH = 256;
    public final static int SCREEN_HEIGHT = 240;

    public void setFPS(double fps);

    public void clearDisplay();

    public void refreshDisplay();

    public void plot(int x, int y, int colorIndex, boolean isMonochrome, boolean intensifyRed, boolean intensifyGreen, boolean intensifyBlue);
}
