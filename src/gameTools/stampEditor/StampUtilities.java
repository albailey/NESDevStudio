/*
 * StampUtilities.java
 *
 * Created on August 18, 2008, 10:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.stampEditor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import ui.chr.tileEditor.CHRTile;

/**
 *
 * @author abailey
 */
public class StampUtilities {
    
    /** Creates a new instance of StampUtilities */
    public StampUtilities() {
    }
    
    
    public static boolean storeStampTiles(File theFile, int wid, int hgt, int objectTypes[], CHRTile[] t){
        try {
            FileOutputStream fos = new FileOutputStream(theFile);
            DataOutputStream dos = new DataOutputStream(fos);            
            dos.writeByte(wid);
            dos.writeByte(hgt);
            
            
            for(int j=0;j<hgt;j++){
                for(int i=0;i<wid;i++){
                	int index = j*wid + i;
                    byte maskData[] = t[index].asMask();
                    dos.write(maskData,0,maskData.length);
                    dos.writeByte(t[index].getOAM());
                    dos.writeByte(objectTypes[index]);
                }
            }
            fos.close();
            dos.close();
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;        
        
    }
    
    public static boolean storeStampTiles(File theFile, CHRTile[][] t, int defaultObjectType){
        try {
            FileOutputStream fos = new FileOutputStream(theFile);
            DataOutputStream dos = new DataOutputStream(fos);
            
            int wid = t.length;
            int hgt = t[0].length;
            dos.writeByte(wid);
            dos.writeByte(hgt);
            
            for(int j=0;j<hgt;j++){
                for(int i=0;i<wid;i++){
                    byte maskData[] = t[i][j].asMask();
                    dos.write(maskData,0,maskData.length);
                    dos.writeByte(t[i][j].getOAM());
                    dos.writeByte((byte)(defaultObjectType & 0xFF));
                }
            }
            fos.close();
            dos.close();
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;        
    }
    
    
}
