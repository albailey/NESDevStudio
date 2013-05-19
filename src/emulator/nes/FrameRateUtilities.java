/*
 * FrameRateUtilities.java
 *
 * Created on April 1, 2008, 5:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes;

/**
 *
 * @author abailey
 */
public class FrameRateUtilities {
    
    public final static int NTSC_MODE = 0;
    public final static int PAL_MODE = 1;
    public final static int DEFAULT_REGION_MODE = NTSC_MODE;
    
    public final static float NTSC_FPS = 60;
    public final static float PAL_FPS = 50;
    public final static long MILLIS_PER_SECOND = 1000;
    
    public final static int FULL_SPEED_MODE = 1;
    public final static int HALF_SPEED_MODE = 2;
    public final static int QUARTER_SPEED_MODE = 4;
    public final static int TENTH_SPEED_MODE = 10;
    
    public final static int DEFAULT_SPEED_MODE = FULL_SPEED_MODE;
    
    /** Creates a new instance of FrameRateUtilities */
    private FrameRateUtilities() {
    }

    // this is NOT accurate
    public static long calculateFrameRateDelay(int regionMode, int speedMode){
        long actualSpeed = speedMode * MILLIS_PER_SECOND;
        if(speedMode <= 0){
            actualSpeed = DEFAULT_SPEED_MODE * MILLIS_PER_SECOND;
        }
        float val = (regionMode == PAL_MODE) ? (actualSpeed /  NTSC_FPS): (actualSpeed /  PAL_FPS);
        return (long)(val + 0.5); // rounds UP
    }
}
