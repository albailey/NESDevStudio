package gameTools.imageHelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import ui.chr.PPUConstants;
import ui.input.GridMouseResultsModel;

public class Image2DPanel extends JPanel {

	private static final long serialVersionUID = 2803629253472063548L;
	private int posX;
	private int posY;
	private float _scaleX;
	private float _scaleY;
	
	private int displayScale = 1;

	private int wid;
	private int hgt;
	private BufferedImage origImg = null;
	private BufferedImage filteredImg = null;

	private GridMouseResultsModel resultsModel = null;
	private int[] pix = new int[0];
	private Vector<ImageFilter> _filters;
	
	public Image2DPanel(int newWid, int newHgt, int scale) {
		super();
		posX = 0;
		posY = 0;
		_scaleX = 1.0f;
		_scaleY = 1.0f;				
		setDimensions(newWid, newHgt);
		setScaleFactor(scale); // this updates the pixel array
	}
	public void reload(int w, int h, int px, int py, File inputFile){
		posX = px;
		posY = py;
		// load image

		try {
			origImg = ImageIO.read(inputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setDimensions(w, h);
		applyFilters();
	}
	public void setFilters(Vector<ImageFilter> filters){
		_filters = filters;
		applyFilters();
	}

	public void setDimensions(int newWid, int newHgt){
		wid = newWid;
		hgt = newHgt;
		applyFilters();
	}
	
	public void setScale(int scaleX, int scaleY){
		_scaleX = scaleX / 100.0f;
		_scaleY = scaleY / 100.0f;
		applyFilters();
	}

	public void setScaleFactor(int val){
		displayScale = val;
		setPreferredSize(new Dimension(wid*displayScale,hgt*displayScale));	 
		revalidate();
		applyFilters();
	}

	public void applyFilters(){
		pix = new int[wid*hgt];
		if(origImg != null) {
			BufferedImage srcImage = origImg; 
			
			if(_filters != null) {
				BufferedImage destImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), origImg.getType()); 
				Iterator<ImageFilter> iter = _filters.iterator();
				while(iter.hasNext()){
					ImageFilter filter = iter.next();
					if(filter.isEnabled()) {
						filter.applyFilter(srcImage, destImage); 
						srcImage = destImage;
					}
				}
			}

			// scale and reposition 
			AffineTransform transformer = new AffineTransform();
			transformer.translate(posX, posY);
			transformer.scale(_scaleX,_scaleY);
			filteredImg =  new BufferedImage(wid, hgt, origImg.getType()); 
			Graphics2D g2 = filteredImg.createGraphics();
			g2.setTransform(transformer);       	
			g2.drawImage(srcImage, 0, 0, wid, hgt, this);
			
			// get the pixels
			filteredImg.getRGB(0, 0, wid, hgt, pix, 0, wid);
		}
		repaint();
	}

	public int[] getPixels() {
		return pix;
	}

	public void reallyRepaint(){
		repaint();
	}

	public void paintComponent(Graphics g){	        
		super.paintComponent(g);  // paints background
		g.setColor(Color.white);
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(Color.lightGray);
		int upper = getWidth();
		if(getHeight() > upper){
			upper = getHeight();
		}
		for(int i=0;i<upper*2;i+=10){
			g.drawLine(i,0,0,i);
		}

		if(filteredImg != null) {
			g.drawImage (filteredImg, 0, 0, getWidth(), getHeight(), this);
		}

		if(resultsModel != null){
			if(resultsModel.isBoxValid()){
				g.setColor(Color.GREEN);
				int stx = (resultsModel.startX*PPUConstants.CHR_WIDTH);
				int sty = (resultsModel.startY*PPUConstants.CHR_HEIGHT);
				int wx = ((resultsModel.endX-resultsModel.startX)*PPUConstants.CHR_WIDTH);
				int wy = ((resultsModel.endY-resultsModel.startY)*PPUConstants.CHR_HEIGHT);                
				g.drawRect(stx * displayScale, sty * displayScale, wx * displayScale, wy * displayScale);
			}
		}
	}

}
