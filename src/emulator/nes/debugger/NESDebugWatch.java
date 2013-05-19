/*
 * NESDebugWatch.java
 *
 * Created on January 28, 2009, 6:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.debugger;

/**
 *
 * @author abailey
 */
public class NESDebugWatch implements Comparable<NESDebugWatch> {

    public final static int NONE_MODE = 0x00;
    public final static int READ_MODE = 0x01;
    public final static int WRITE_MODE = 0x02;
    public final static int BOTH_MODE = READ_MODE | WRITE_MODE;
    
    
    private int _address;
    private int _watchType;
    
    /** Creates a new instance of NESDebugWatch */
    public NESDebugWatch(int addr, boolean isRead, boolean isWrite) {
        _address = addr;
        _watchType = NONE_MODE;
        if(isRead) {
            _watchType |= READ_MODE;
        }
        if(isWrite){
            _watchType |= WRITE_MODE;
        }        
    }
    public boolean equals(Object o){
        if(o == null)
            return false;
        if(o instanceof NESDebugWatch) {
            return (((NESDebugWatch)o)._address == _address);
        } else {
            return false;
        }        
    }
    
    public int compareTo(NESDebugWatch o){
        if(o == null) {
            return -1;
        }
         return new Integer(_address).compareTo(new Integer(o._address));
     }
    
    
}
