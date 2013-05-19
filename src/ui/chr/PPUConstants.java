/*
 * PPUConstants.java
 *
 * Created on September 29, 2006, 3:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.chr;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.InputStream;

/**
 *
 * @author abailey
 */
public class PPUConstants {
    
    public final static int NUM_PATTERN_PAGES = 2;
    
    public final static int NES_IMAGE_PALETTE_SIZE = 16;
    public final static int NES_SPRITE_PALETTE_SIZE = 16;
    public final static int NES_FIXED_PALETTE_SIZE = 64;
    
    // RGB value information provided by: http://nesdev.parodius.com/NESTechFAQ.htm
    // there should be 64 entries
    
    public final static Color[] NES_PALETTE = {
        new Color(0x80,0x80,0x80),     new Color(0x00,0x3D,0xA6),     new Color(0x00,0x12,0xB0),     new Color(0x44,0x00,0x96),
        new Color(0xA1,0x00,0x5E),     new Color(0xC7,0x00,0x28),     new Color(0xBA,0x06,0x00),     new Color(0x8C,0x17,0x00),
        new Color(0x5C,0x2F,0x00),     new Color(0x10,0x45,0x00),     new Color(0x05,0x4A,0x00),     new Color(0x00,0x47,0x2E),
        new Color(0x00,0x41,0x66),     new Color(0x00,0x00,0x00),     new Color(0x05,0x05,0x05),     new Color(0x05,0x05,0x05),
        new Color(0xC7,0xC7,0xC7),     new Color(0x00,0x77,0xFF),     new Color(0x21,0x55,0xFF),     new Color(0x82,0x37,0xFA),
        new Color(0xEB,0x2F,0xB5),     new Color(0xFF,0x29,0x50),     new Color(0xFF,0x22,0x00),     new Color(0xD6,0x32,0x00),
        new Color(0xC4,0x62,0x00),     new Color( 0x35,0x80,0x00),     new Color(  0x05,0x8F,0x00),     new Color(  0x00,0x8A,0x55),
        new Color(0x00,0x99,0xCC),     new Color( 0x21,0x21,0x21),     new Color(  0x09,0x09,0x09),     new Color(  0x09,0x09,0x09),
        new Color(0xFF,0xFF,0xFF),     new Color( 0x0F,0xD7,0xFF),     new Color(  0x69,0xA2,0xFF),     new Color(  0xD4,0x80,0xFF),
        new Color(0xFF,0x45,0xF3),     new Color( 0xFF,0x61,0x8B),     new Color(  0xFF,0x88,0x33),    new Color(  0xFF,0x9C,0x12),
        new Color(0xFA,0xBC,0x20),     new Color(  0x9F,0xE3,0x0E),     new Color(  0x2B,0xF0,0x35),   new Color(  0x0C,0xF0,0xA4),
        new Color(0x05,0xFB,0xFF),     new Color(  0x5E,0x5E,0x5E),       new Color(  0x0D,0x0D,0x0D),       new Color(  0x0D,0x0D,0x0D),
        new Color(0xFF,0xFF,0xFF),     new Color(  0xA6,0xFC,0xFF),       new Color(  0xB3,0xEC,0xFF),       new Color(  0xDA,0xAB,0xEB),
        new Color(0xFF,0xA8,0xF9),     new Color(  0xFF,0xAB,0xB3),       new Color(  0xFF,0xD2,0xB0),       new Color(  0xFF,0xEF,0xA6),
        new Color(0xFF,0xF7,0x9C),     new Color(  0xD7,0xE8,0x95),       new Color(  0xA6,0xED,0xAF),       new Color(  0xA2,0xF2,0xDA),
        new Color(0x99,0xFF,0xFC),     new Color(  0xDD,0xDD,0xDD),       new Color(  0x11,0x11,0x11),       new Color(  0x11,0x11,0x11)
    };
     
    
    public final static int IMAGE_PALETTE_TYPE = 0;
    public final static int SPRITE_PALETTE_TYPE = 1;
    public final static int NES_PALETTE_TYPE = 2;
   
    
    public final static int PATTERN_TABLE_PAGE_SIZE = 4096;
    public final static int BYTES_PER_PATTERN_TABLE_ENTRY = 16;
    public final static int ENTRIES_PER_PATTERN_TABLE_PAGE = PATTERN_TABLE_PAGE_SIZE/BYTES_PER_PATTERN_TABLE_ENTRY;  // 256
    
    public final static int COLUMNS_PER_PATTERN_PAGE = 16;
    public final static int ROWS_PER_PATTERN_PAGE = ENTRIES_PER_PATTERN_TABLE_PAGE / COLUMNS_PER_PATTERN_PAGE;
 
    
    // Name Table Constants
    public final static int CHR_WIDTH = 8;
    public final static int CHR_HEIGHT = 8;
    
    public final static int NAME_TABLE_WIDTH = 32;
    public final static int NAME_TABLE_HEIGHT = 30;
    public final static int NUM_NAME_TABLE_ENTRIES = NAME_TABLE_WIDTH * NAME_TABLE_HEIGHT;
    
    public final static int OAM_TABLE_SIZE = 64;
    public final static int OAM_GROUP_SIZE = 16; // there are 16x16 OAM groupings of 2 bits each
    public final static int OAM_INDEX_GRID_SIZE = 8; // there are 8x8 OAM bytes in the oam attribute table

    public final static int TOTAL_NUM_NAME_TABLE_ENTRIES = NUM_NAME_TABLE_ENTRIES +  OAM_TABLE_SIZE;

    /*
     * OAM data is represented like this:
     * -----------------------------------
     *| t00   t10  |  t20 t30  |
     *|    s0      |     s1    |
     *| t01    t11 |  t21 t31  |
     *--------------------------
     *| t02   t12  |  t22 t32  |
     *|    s2      |     s3    |
     *| t03    t13 |  t23 t33  |
     *--------------------------
     *
     * Tiles are labelled t00 etc..
     * 2x2 squares are labelled s0 etc..  And consist of 2 bits each for the upper 2 color bits
     * That entire collection of 4x4 nameTableTiles would have its OAM data stored in one OAM byte
     * The format for that byte is s3,s2,s1,s0 where s3 etc.. are the 2 color bits
     */
    
    static {
        // overwrite the default palette 
        DataInputStream dis = null;
        InputStream inStream = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String ntscPalletteFile = "ui/chr/Palette.pal";
            inStream = loader.getResourceAsStream(ntscPalletteFile);
            if(inStream == null){
                throw new Exception("Unable to load:" + ntscPalletteFile);
            }
            dis = new DataInputStream(inStream);
           
            int count = 0;
            while(true){
                byte r = dis.readByte();
                byte g = dis.readByte();
                byte b = dis.readByte();
       //         System.out.println(count + ") [" + ByteFormatter.formatByte(r) + "," + ByteFormatter.formatByte(g) + "," + ByteFormatter.formatByte(b) + "]");
                NES_PALETTE[count] = new Color( r & 0xff, g & 0xff, b & 0xff);
                count++;
                if(count >= 64){                  
                    break;
                }
            }
            
            dis.close();
            inStream.close();            
        } catch(Exception ex){
        	System.err.println(ex.getLocalizedMessage());
        //    ex.printStackTrace();
            try {
                if(dis != null){
                    dis.close();
                    dis =  null;
                }
            } catch(Exception e){ e.printStackTrace(); }
            try {
                if(inStream != null){
                    inStream.close();
                    inStream = null;
                }
            } catch(Exception e){ e.printStackTrace(); }
        }
    }
    /** Creates a new instance of PPUConstants */
    private PPUConstants() {
    }
    
}
