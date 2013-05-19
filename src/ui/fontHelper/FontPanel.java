/*
 * FontPanel.java
 *
 * Created on August 31, 2007, 12:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.fontHelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelGrabber;

import javax.swing.JPanel;

import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.tileEditor.CHRTile;

/**
 *
 * @author abailey
 */
public class FontPanel extends JPanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3822895843982296932L;
	private int initialWid = 0;
    private int initialHgt = 0;

    private int scaleAmount = 1;

    private FontCriteriaModel fontModel;
    private Image offImg = null;
    
    /** Creates a new instance of FontPanel */
    public FontPanel(int scale, int wid, int hgt) {
        initialWid = wid;
        initialHgt = hgt;
        offImg = null;
        setDoubleBuffered(true);
        setMinimumSize(new Dimension(initialWid, initialHgt));
        setScale(scale);
    }

    public void setScale(int scale){
        scaleAmount = scale;
        setPreferredSize(new Dimension(initialWid*scale, initialHgt*scale));
        offImg = null;
        repaint();
    }
    
    
    public void setFontModel(FontCriteriaModel fModel){
        fontModel = fModel;
        offImg = null;
        repaint();
    }
    
    // in typical ascii these are the values (in hex)
    // Space (starts at 0x20)
    // ! (0x21 - 0x2F are punctuation)
    // 0 (0x30 to 0x39 are numbers)
    // : (0x3A to 0x40 are more punctuation)
    // A (0x41 to 0x5A are upper case)
    // [ (0x5B to 0x60 are more punctuation)
    // a (0x61 to 0x7A are lower case)
    // { (0x7B to 0x7D are more punctuation        
    
    public void update(Graphics g) {
        paintComponent(g); 
    }
    public void sendToCHR(CHREditorModel modelRef){
    
        // now draw each char individually so that its CROPPED
        int charWid = 8 * fontModel.tilesWide;
        int charHgt = 8 * fontModel.tilesHigh;
            
        Image charImg = createImage(charWid,charHgt );
        Graphics2D charG = (Graphics2D)charImg.getGraphics();
        FontRenderContext frc = charG.getFontRenderContext();
        if(fontModel.isAntiAliasing){
            charG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        int pix[] = new int[charHgt * charWid];
        char theChars[] = fontModel.determineText();
        CHRTile tile = new CHRTile(new byte[16], 0, modelRef);
        int sz = 4;
        byte red[] = new byte[sz];
        byte green[] = new byte[sz];
        byte blue[] = new byte[sz];
       // byte grayscale[] = { 0x3D, 0x2D,0x1D,0x0F};
         byte grayscale[] = { 0x0F, 0x1D,0x2D,0x3D};
        for(int i=0;i<4;i++){
            modelRef.getCHRModel().imagePalette[i] = grayscale[i];
            modelRef.getCHRModel().imagePalette[i+4] = grayscale[i];
            modelRef.getCHRModel().imagePalette[i+8] = grayscale[i];
            modelRef.getCHRModel().imagePalette[i+12] = grayscale[i];
            
            Color c = PPUConstants.NES_PALETTE[grayscale[i]];
            red[i] = (byte)c.getRed();
            green[i] = (byte)c.getGreen();
            blue[i] = (byte)c.getBlue();            
        }
        IndexColorModel colorModel =  new IndexColorModel(8,sz,red,green,blue);
     
    
        for(int i=0;i<theChars.length;i++){
            charG.setColor(PPUConstants.NES_PALETTE[grayscale[0]]);
            charG.fillRect(0,0, charWid,charHgt);
            charG.setColor(PPUConstants.NES_PALETTE[grayscale[3]]);
            TextLayout layout = new TextLayout(""+theChars[i], fontModel.theFont, frc);            
            layout.draw(charG, fontModel.leftSpacing, charHgt-fontModel.bottomSpacing);
            try {
                PixelGrabber pg = new PixelGrabber(charImg, 0, 0, charWid, charHgt, pix, 0, charWid);
                pg.grabPixels();
            } catch(Exception e){
                e.printStackTrace();
                break;
            }
            for(int p=0;p<pix.length;p++){
                byte pixel[] = (byte[])colorModel.getDataElements(pix[p], null);
                pix[p] = pixel[0];
            }
            for(int r=0;r<fontModel.tilesHigh;r++){
                for(int q=0;q<fontModel.tilesWide;q++){
                    int chrPos = (i*fontModel.tilesWide)+q;
                    int xOff = chrPos % 16;
                    int yOff = chrPos / 16;
                    yOff = yOff * fontModel.tilesHigh + r;
                    // add 16 to skip the first tile
                    int index = (yOff * 16 + xOff)*16 + 16;
                    if(index + 16 > PPUConstants.PATTERN_TABLE_PAGE_SIZE ){
                        System.err.println("Not enough room");
                        continue;
                    }                        
                    for(int py = 0; py<8; py++){
                        for(int px = 0; px<8; px++){
                            tile.setPixelIndex(px,py,pix[(((r*8)+py)*charWid)+ (q*8)+px ],false);
                        }
                    }
                    tile.updatePixels();
                    System.arraycopy(tile.asMask(),0,modelRef.getCHRModel().patternTable[0],index,16);
                }
            }
                
        }
   
        
    }
    
    
     public void paintComponent(Graphics g){
        super.paintComponent(g);    // paints background
        // OK,  paint it ourselves
        // we need the font...
        if(fontModel == null){
            return;
        }        
        if(offImg == null){
            offImg = createImage(initialWid, initialHgt);
            Graphics2D offG = (Graphics2D)offImg.getGraphics();

            offG.setColor(Color.lightGray);
            for(int i=8;i<initialWid;i+=8)
                offG.drawLine(i,0,i,initialHgt);
            for(int i=8;i<initialHgt;i+=8)
                offG.drawLine(0,i,initialWid,i);

            
            // now draw each char individually so that its CROPPED
            int charWid =8*fontModel.tilesWide;
            int charHgt = 8*fontModel.tilesHigh;
            
            Image charImg = createImage(charWid,charHgt );
            Graphics2D charG = (Graphics2D)charImg.getGraphics();
            FontRenderContext frc = charG.getFontRenderContext();
            if(fontModel.isAntiAliasing){
                charG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // KEY_DITHERING has no effect
                // KEY_INTERPOLATION has no effect
                // KEY_RENDERING has no effect
                // KEY_TEXT_ANTIALIASING has no effect
                // KEY_FRACTIONALMETRICS has no effect
                // KEY_COLOR_RENDERING has no effect
            }

            char theChars[] = fontModel.determineText();
            for(int i=0;i<theChars.length;i++){
                charG.setColor(Color.BLACK);
                charG.fillRect(0,0, charWid,charHgt);
                charG.setColor(fontModel.getColor());
                TextLayout layout = new TextLayout(""+theChars[i], fontModel.theFont, frc);            
                layout.draw(charG, fontModel.leftSpacing, charHgt-fontModel.bottomSpacing);
                int semiX = i % 16;
                int semiY = i / 16;
                offG.drawImage(charImg,semiX*charWid,semiY*charHgt,null);
            }
        }
        if(offImg != null) {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform tx = AffineTransform.getScaleInstance(scaleAmount,scaleAmount);
            g2d.drawImage(offImg, tx, null);
        }
    }
   
}
