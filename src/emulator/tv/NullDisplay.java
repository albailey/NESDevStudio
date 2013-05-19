/*
 * NullDisplay.java
 *
 * Created on October 25, 2007, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.tv;


/**
 * This does NOTHING.  Its to support a headless display
 * @author abailey
 */
public class NullDisplay implements Display {
    
    /** Creates a new instance of NullDisplay */
    public NullDisplay() {
    }

    public void setFPS(double fps) {

    }
   public void refreshDisplay() {

   }
   
    public void plot(int x,int y, int colorIndexVal, boolean isMonochrome, boolean intensifyRed, boolean intensifyGreen, boolean intensifyBlue){
        
    }
     
    public void clearDisplay(){
        
    }
}
