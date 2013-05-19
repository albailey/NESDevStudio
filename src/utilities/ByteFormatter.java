/*
 * ByteFormatter.java
 *
 * Created on November 19, 2006, 8:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package utilities;

/**
 *
 * @author abailey
 */
public class ByteFormatter {
    
    /** Creates a new instance of ByteFormatter */
    private ByteFormatter() {
    }
    
    private static String HEX_VALS[] = {
        "0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"
    };
 
    public static final String formatBits(byte b){
        String foo = "";
        for(int i=7;i>=0;i--){
            int val = ((b >> i)& 0x01);
            foo = foo + val;
        }
        return foo;
    }
    public static final String formatByte(byte b){
        return HEX_VALS[(b & 0xFF)/ 16] + HEX_VALS[(b & 0xFF) % 16]; 
    }

    public static final String formatThreePlaces(int i){
        if(i<10) return "  "+ i;
        if(i<100) return " " + i;
        return "" + i;
    }

    public static final String formatBinaryByte(byte b){
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<8;i++){
            sb.append( ((((b & 0xFF) << i) & 0x80) == 0x80)  ? "1" : "0");
        }
        return sb.toString();
    }

    public static final String formatSingleByteInt(int i){
       return formatByte((byte)(i&0xFF));
    }
    public static final String formatInt(int i){
       return formatByte((byte)(i>>8)) + formatByte((byte)(i&0xFF));
    }
    /*
    public static final String format4ByteInt(int i){
       return formatByte((byte)((i>>24)&0xFF)) + formatByte((byte)((i>>16)&0xFF)) + formatByte((byte)((i>>8)&0xFF)) + formatByte((byte)(i&0xFF));
    }
     */
    
    // MSB,LSB
    public static final int get2ByteInt(byte b[], int offset){
        if(offset < 0 || offset + 2 > b.length){
            System.err.println("Invalid size byte buffer.");
            return -1;
        }
        return ((b[offset] & 0xff) << 8) | ((b[offset+1] & 0xff));
    }
    // MSB...LSB
    public static final int get4ByteInt(byte b[], int offset){
        if(offset < 0 || offset + 4 > b.length){
            System.err.println("Invalid size byte buffer.");
            return -1;
        }
        return (((b[offset] & 0xff) << 24) | ((b[offset+1] & 0xff) << 16) |  ((b[offset+2] & 0xff) << 8) | (b[offset+3] & 0xff));
    }
}
