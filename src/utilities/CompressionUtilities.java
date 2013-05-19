/*
 * CompressionUtilities.java
 *
 * Created on July 9, 2007, 1:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package utilities;


/**
 *
 * @author abailey
 */
public class CompressionUtilities {
    
    public final static int NO_COMPRESSION = 0;
    public final static int RLE_COMPRESSION = 1;
    
    /** Creates a new instance of CompressionUtilities */
    private CompressionUtilities() {
    }
    
    
    // returns null if it fails otherwise a new byte[] array
    public final static byte[] compressData(int compressionType, byte srcdata[]){
        if(srcdata == null){
            System.err.println("Null src data");
            return srcdata; // null
        }
        if(srcdata.length == 0){
            System.err.println("Empty src data");
            return srcdata;        // empty    
        }
        
        byte data[] = null;
        switch(compressionType){            
            case NO_COMPRESSION:
                data = new byte[srcdata.length];
                System.arraycopy(srcdata,0,data,0,srcdata.length);
                break;
            case RLE_COMPRESSION:
                data = rleCompress(srcdata);
                break;
            default:  
                System.err.println("Unsupported compression type:" + compressionType +" returning original data");
                data = new byte[srcdata.length];
                System.arraycopy(srcdata,0,data,0,srcdata.length);
                break;
        }
        /*
       String cr = System.getProperty("line.separator"); 
        StringBuffer sb = new StringBuffer();
            for(int i=0;i<data.length;i++){
                if(i > 0 && i % 64 == 0) {
                    sb.append(cr);
                }
                sb.append(ByteFormatter.formatByte(data[i]));
                sb.append(",");
            }
            System.out.println(sb);
            System.out.println("");
            System.out.flush();        
        */
        return data;
    }

    // returns null if it fails
    public final static byte[] decompressData(int compressionType, byte srcdata[]){
        if(srcdata == null){
            System.err.println("Null src data");
            return srcdata; // null
        }
        if(srcdata.length == 0){
            System.err.println("Empty src data");
            return srcdata;        // empty    
        }
        
        byte data[] = null;
        switch(compressionType){            
            case NO_COMPRESSION:
                data = new byte[srcdata.length];
                System.arraycopy(srcdata,0,data,0,srcdata.length);
                break;
            case RLE_COMPRESSION:
                data = rleDeCompress(srcdata);
                break;
            default:  
                System.err.println("Unsupported decompression type:" + compressionType +" returning original data");
                data = new byte[srcdata.length];
                System.arraycopy(srcdata,0,data,0,srcdata.length);
                break;
        }
        
        return data;
    }
    
 /*   
    public final static void testRLE(int tests, int seed, int dataSize){
        java.util.Random rand = new java.util.Random(seed);
        String cr = System.getProperty("line.separator");
        for(int n=0;n<tests;n++){
            System.out.println("");
            System.out.println("Test" + n);        
            int lastLen = 0;
            byte b[] = new byte[dataSize];
           // while(lastLen != dataSize){
           //     byte val = (byte)(rand.nextInt() & 0xFF);
           //     int len = rand.nextInt() & 0xFF;
           //     if(len > dataSize-lastLen){
           //         len = dataSize-lastLen;
           //     }
           //     for(int i=0;i<len;i++){
           //         b[lastLen++] = val;
           //     }
           // }
         
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<b.length;i++){
                if(i > 0 && i % 64 == 0) {
                    sb.append(cr);
                }
                sb.append(ByteFormatter.formatByte(b[i]));
                sb.append(",");
            }
       //     System.out.println(sb);
            System.out.println("");
            System.out.flush();
             
            byte b2[] = compressData(RLE_COMPRESSION, b);
            if(b2 == null){
                System.err.println("COMPRESSION failed");
                break;
            }
            System.out.println("Compressed Size:" + dataSize + "->" + b2.length);
            sb = new StringBuffer();
            for(int i=0;i<b2.length;i++){
                if(i > 0 && i % 64 == 0) {
                    sb.append(cr);
                }
                sb.append(ByteFormatter.formatByte(b2[i]));
                sb.append(",");
            }
      //      System.out.println(sb);
            System.out.println("");
            System.out.flush();
            
            byte b3[] = decompressData(RLE_COMPRESSION, b2);
            if(b3 == null){
                System.err.println("DECOMPRESSION failed");
                break;
            }
            sb = new StringBuffer();
            for(int i=0;i<b3.length;i++){
                if(i > 0 && i % 64 == 0) {
                    sb.append(cr);
                }
                sb.append(ByteFormatter.formatByte(b3[i]));
                sb.append(",");
            }
       //     System.out.println(sb); 
            System.out.println("");
            System.out.flush();
             
            if(b3.length != dataSize){
                System.err.println("DECOMPRESSION yielded wrong size");
                break;
            }
            
            for(int i=0;i<dataSize;i++){
                if(b[i] != b3[i]){
                    System.err.println("DECOMPRESSION MISMATCH. run: " + n + " index:" + i + " orig:" + b[i] + " new:" + b3[i]);
                }
            }

        }               
    }
   
  */
    
    // RLE compression in a PackBITS format
    // works like this:
    // write a 1 byte header
    // write the data
    // the header indicates an uncompressed stream or a compressed stream of characters
    // header byte value n=0..127 means write n+1 bytes as they are
    // header values n=-128..0 means write the next value 2-n times
    // notice that a header value of 0 doesnt matter which way its done.
    // ABCDE -> 4ABCDE meaning no savings
    // AABBCC -> 5AABBCC so no savings
    // AAABC -> -1A1BC so no saavings (I write it as -1 cause its easier to read)
    // AAAAAA -> -4A so big savings
    private final static byte[] rleCompress(byte srcData[]){
        
        // approach to take:
        // create a run/non run buffer, then write it to the result buffer as things change
        byte b[] = new byte[srcData.length];
        byte runBuffer[] = new byte[srcData.length];
        int resultPos = 0;
        int runPos = 0;
        int srcPos = 0;
        
        boolean runMode = false;
        while(srcPos < srcData.length){
            runBuffer[runPos++] = srcData[srcPos++];
            
            boolean oldRun = runMode;
            
            if(runPos >= 3) { // lets see if we have ANY runs of 3...
                if((runBuffer[runPos-1] == runBuffer[runPos-2]) && (runBuffer[runPos-1] == runBuffer[runPos-3])){
                    // its a run
                    runMode = true;
                } else {
                    runMode = false;                    
                }
            }
            if(runMode && runPos > 3){
                // we are in a run
                if(!oldRun){
                    // we just went into the run, lets fix this
                    b[resultPos++] = (byte)((runPos - 4)  & 0xFF);
                    int len = runPos-3;
                    System.arraycopy(runBuffer,0,b,resultPos,len);
                    resultPos += len;
                    for(int i=0;i<3;i++){
                        runBuffer[i] = runBuffer[runPos-1];
                    }
                    runPos = 3;                    
                    continue;
                } else {
                    // we were in a run before, do nothing...
                }
            }
            if(!runMode && oldRun){
                // we were in a run and it ended...
                int hedSize = runPos -1;
                byte hedVal = (byte)((2-hedSize)&0xFF);
                b[resultPos++] = hedVal;
                b[resultPos++] = runBuffer[0];
                runBuffer[0] = runBuffer[runPos-1];
                runPos = 1;
                continue;
            }
            
            // exhausted our max run size...
            if(runPos == 127){
                if(runMode){
                    int hedSize = runPos;
                    byte hedVal = (byte)((2-hedSize)&0xFF);
                    b[resultPos++] = hedVal;
                    b[resultPos++] = runBuffer[0];
                    runPos = 0;  
                    runMode = false;
                } else {
                    // write out a big ugly block of crap
                    b[resultPos++] = (byte)(runPos & 0xFF);
                    System.arraycopy(runBuffer,0,b,resultPos,runPos);
                    resultPos += runPos;
                    runPos = 0;
                    runMode = false;
                }
            }
        }
        // OK, write out the leftovers.
        if(runPos > 0){
            if(runMode){
                int hedSize = runPos;
                byte hedVal = (byte)((2-hedSize)&0xFF);
                b[resultPos++] = hedVal;
                b[resultPos++] = runBuffer[0];            
            } else {
                // not runmode
                int hedSize = runPos-1;
                byte hedVal = (byte)(hedSize & 0xFF);
                b[resultPos++] = hedVal;
                for(int i=0;i<runPos;i++)
                    b[resultPos++] = runBuffer[i];
            }
        }
        byte results[] = new byte[resultPos];
        System.arraycopy(b,0,results,0,resultPos);
        
        return results;
    }
    private final static byte[] rleDeCompress(byte srcdata[]){
        byte b[] = new byte[1024*1024]; // support 1 MEG
        int resultPos = 0;
        int srcPos = 0;
        while(srcPos < srcdata.length){
            int hedVal = (int)srcdata[srcPos++]; // may be negative
            try {
            if(hedVal >= 0){
                int len = hedVal + 1;
                System.arraycopy(srcdata,srcPos,b, resultPos,len);
                resultPos += len;
                srcPos += len;
            } else {
                // decode a run
                int len = 2 - hedVal;
                byte val = srcdata[srcPos++];
                for(int i=0;i<len;i++){
                    b[resultPos++] = val;
                }
            }
            }catch(Exception e){
                System.err.println("Failure");
                break;
            }
        }
        
        byte results[] = new byte[resultPos];
        System.arraycopy(b,0,results,0,resultPos);
        
        return results;
        
    }
    
    
}
