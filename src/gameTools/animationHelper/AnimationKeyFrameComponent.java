/*
 * AnimationKeyFrameComponent.java
 *
 * Created on September 29, 2008, 10:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.animationHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ui.chr.PPUConstants;
import ui.chr.model.CHREditorModel;
import ui.chr.nameTable.NameTablePanel;
import ui.chr.nameTable.TransparentImageLevel;
import ui.chr.tileEditor.CHRTile;

/**
 *
 * @author abailey
 */
public class AnimationKeyFrameComponent extends JComponent implements Runnable  {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 907913256273360326L;
	private int oldWidth = -1;
    private int oldHeight = -1;
    private int oldActualWidth = -1;
    private int oldActualHeight = -1;
    private int BORDER = 10;
    
    private Image offscreenImg = null;
    private Graphics offscreenGraphics = null;
    
    private JPopupMenu _popup = null;
    
    private JPanel settingsPanel = null;
    private JSpinner spriteIndexSpinner = null;
    private JSpinner xSpinner = null;
    private JSpinner ySpinner = null;
    private JSpinner oamIndexSpinner = null;
    private JCheckBox isFlippedHorizontalCB = null;
    private JCheckBox isFlippedVerticalCB = null;
    private JCheckBox isUnderCB = null;
    
    private JCheckBox _animCBCallback = null;
    
    private int currentIndex = 0;
    private int activeRow = 0;

    private int popupColumnIndex = -1;
    private int popupRowIndex = -1;
    
    private AnimationStateMachine _stateMachine = null;
    private NameTablePanel _ntp = null;
    private CHREditorModel modelRef = null;
    int currentPixelArrayWidth = 0;
    int currentPixelArrayHeight = 0;
    int transparentPix[] = null;
    int sprPix[] = null;
   
    /**
     * Creates a new instance of AnimationKeyFrameComponent
     */
    public AnimationKeyFrameComponent(CHREditorModel newModelRef) {
        super();
        _stateMachine = new AnimationStateMachine();
         modelRef = newModelRef;
        setupSettingsPanel();
        setDimensions();
        setupMouseStuff();
        setupPopupMenu();
    }
    
    public void setNameTablePanel( NameTablePanel ntp){
        _ntp = ntp;
       
        
        if(_ntp == null){
            return;
        }
        
        int tilesWide = _ntp.getTilesWide();
        int tilesHigh = _ntp.getTilesHigh();
        
        currentPixelArrayWidth = tilesWide * PPUConstants.CHR_WIDTH;
        currentPixelArrayHeight = tilesHigh * PPUConstants.CHR_HEIGHT;
        int overallPixelLength = currentPixelArrayWidth * currentPixelArrayHeight;
        transparentPix = new int[currentPixelArrayWidth]; // leave it as is...
        sprPix = new int[overallPixelLength];        
        MemoryImageSource sprMIS = new MemoryImageSource(currentPixelArrayWidth, currentPixelArrayHeight, sprPix, 0, currentPixelArrayWidth);
        _ntp.setImageLevel(new TransparentImageLevel(sprMIS, "Sprites", true), NameTablePanel.SPRITE_IMG_LEVEL);
        
    }
    public void setRunnable(Runnable runnable){
        _stateMachine.setRunnable(runnable);
    }
     public void setAnimationMode(boolean flag){
         _stateMachine.setAnimationMode(flag);
     }
     public boolean isAnimationMode(){
         return _stateMachine.isAnimationMode();
     }
       public void setNTSCMode(boolean flag){
         _stateMachine.setNTSCMode(flag);
     }
     public boolean isNTSCMode(){
         return _stateMachine.isNTSCMode();
     }
 

      private void updateSpriteIndex(int val){
         if(activeRow < 0 || activeRow >= modelRef.getNumAnimations()) { 
             return;
         }
         boolean updateIt = false;
         AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, currentIndex);
         
         if(kf == null){
             updateIt = true;
             kf = modelRef.constructKeyFrame(activeRow, currentIndex);
             kf.spriteIndex = (byte)(val & 0xFF);
         } else {
             if(kf.spriteIndex != (byte)(val & 0xFF)){
                updateIt = true;
                kf.spriteIndex = (byte)(val & 0xFF);
             }
         }
          if(updateIt){
            System.out.println("[ " + activeRow + " ] [" + currentIndex + " ] => Sprite Value: [ " + val + " ]");
             updateStuff();
          }
     }
      
     private void updateXValue(int val){
         if(activeRow < 0 || activeRow >= modelRef.getNumAnimations()) { 
             return;
         }
         boolean updateIt = false;
         AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, currentIndex);
         
         if(kf == null){
             updateIt = true;
             kf = modelRef.constructKeyFrame(activeRow, currentIndex);
             kf.xPos = (byte)(val & 0xFF);
         } else {
             if(kf.xPos != (byte)(val & 0xFF)){
                updateIt = true;
                kf.xPos = (byte)(val & 0xFF);
             }
         }
          if(updateIt){
            System.out.println("[ " + activeRow + " ] [" + currentIndex + " ] => xPos Value: [ " + val + " ]");
             updateStuff();
          }
     }
     
     private void updateYValue(int val){
          if(activeRow < 0 || activeRow >= modelRef.getNumAnimations()) { 
             return;
         }
         boolean updateIt = false;
         AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, currentIndex);
         
         if(kf == null){
             updateIt = true;
             kf = modelRef.constructKeyFrame(activeRow, currentIndex);
             kf.yPos = (byte)(val & 0xFF);
         } else {
             if(kf.yPos != (byte)(val & 0xFF)){
                updateIt = true;
                kf.yPos = (byte)(val & 0xFF);
             }
         }
          if(updateIt){
            System.out.println("[ " + activeRow + " ] [" + currentIndex + " ] => yPos Value: [ " + val + " ]");
             updateStuff();
          }
     }
     private void updateOAMIndex(int val){
         if(activeRow < 0 || activeRow >= modelRef.getNumAnimations()) { 
             return;
         }
         boolean updateIt = false;
         AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, currentIndex);
         
         if(kf == null){
             updateIt = true;
             kf = modelRef.constructKeyFrame(activeRow, currentIndex);
             kf.oamIndex = (byte)(val & 0xFF);
         } else {
             if(kf.oamIndex != (byte)(val & 0xFF)){
                updateIt = true;
                kf.oamIndex = (byte)(val & 0xFF);
             }
         }
          if(updateIt){
            System.out.println("[ " + activeRow + " ] [" + currentIndex + " ] => oamIndex Value: [ " + val + " ]");
             updateStuff();
          }
     }
     private void updateHorizontalAxis(boolean val){
         if(activeRow < 0 || activeRow >= modelRef.getNumAnimations()) { 
             return;
         }
         boolean updateIt = false;
         AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, currentIndex);
         
         if(kf == null){
             updateIt = true;
             kf = modelRef.constructKeyFrame(activeRow, currentIndex);
             kf.isFlippedHorizontal = val;
         } else {
             if(kf.isFlippedHorizontal != val){
                updateIt = true;
                kf.isFlippedHorizontal = val;
             }
         }
          if(updateIt){
            System.out.println("[ " + activeRow + " ] [" + currentIndex + " ] => isFlippedHorizontal Value: [ " + val + " ]");
             updateStuff();
          }

     }
     private void updateVerticalAxis(boolean val){
         if(activeRow < 0 || activeRow >= modelRef.getNumAnimations()) { 
             return;
         }
         boolean updateIt = false;
         AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, currentIndex);
         
         if(kf == null){
             updateIt = true;
             kf = modelRef.constructKeyFrame(activeRow, currentIndex);
             kf.isFlippedVertical = val;
         } else {
             if(kf.isFlippedVertical != val){
                updateIt = true;
                kf.isFlippedVertical = val;
             }
         }
          if(updateIt){
            System.out.println("[ " + activeRow + " ] [" + currentIndex + " ] => isFlippedVertical Value: [ " + val + " ]");
             updateStuff();
          }
         
     }
     private void updateUnderBG(boolean val){
        if(activeRow < 0 || activeRow >= modelRef.getNumAnimations()) { 
             return;
         }
         boolean updateIt = false;
         AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, currentIndex);
         
         if(kf == null){
             updateIt = true;
             kf = modelRef.constructKeyFrame(activeRow, currentIndex);
             kf.isUnderBG = val;
         } else {
             if(kf.isUnderBG != val){
                updateIt = true;
                kf.isUnderBG = val;
             }
         }
          if(updateIt){
            System.out.println("[ " + activeRow + " ] [" + currentIndex + " ] => isUnderBG Value: [ " + val + " ]");
             updateStuff();
          }
     }
     
     private void updateSettingsPanel(){
         if(modelRef.getAnimationDuration(activeRow) > 0){           
            for(int j=currentIndex;j>=0;j--){
                AnimationKeyframeData kf = modelRef.getKeyFrame(activeRow, j);
                if(kf != null){
                    int tempActiveRow = activeRow;
                    activeRow = -1;
                   spriteIndexSpinner.getModel().setValue(new Integer(kf.spriteIndex & 0xFF));
                   xSpinner.getModel().setValue(new Integer(kf.xPos & 0xFF));
                   ySpinner.getModel().setValue(new Integer(kf.yPos & 0xFF));
                   oamIndexSpinner.getModel().setValue(new Integer(kf.oamIndex & 0xFF));
                   isFlippedHorizontalCB.setSelected(kf.isFlippedHorizontal);
                   isFlippedVerticalCB.setSelected(kf.isFlippedVertical);
                   isUnderCB.setSelected(kf.isUnderBG);
                   activeRow = tempActiveRow;
                   return;
                }
            }
         }
        spriteIndexSpinner.getModel().setValue(new Integer(0));
        xSpinner.getModel().setValue(new Integer(0));
        ySpinner.getModel().setValue(new Integer(0)); 
        oamIndexSpinner.getModel().setValue(new Integer(0));
       isFlippedHorizontalCB.setSelected(false);
       isFlippedVerticalCB.setSelected(false);
       isUnderCB.setSelected(false);
     }
     
    private void setupSettingsPanel(){
       settingsPanel = new JPanel();   
       settingsPanel.setLayout(new GridLayout(7,1,0,1));
       {
        spriteIndexSpinner = new JSpinner();
        final JSpinner spinner = spriteIndexSpinner;
        Integer value = new Integer(0);
        Integer min = new Integer(0);
        Integer max = new Integer(255);
        Integer step = new Integer(1);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
        spinner.setModel(spinnerModel);
        spinner.setToolTipText("Sprite Index");  
        
        spinner.addChangeListener( new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int val = ((Integer)spinner.getModel().getValue()).intValue();
                    updateSpriteIndex(val);
                }
            });    
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Sprite Index:"), BorderLayout.CENTER);
        panel.add(spinner, BorderLayout.EAST);
        settingsPanel.add(panel);
       }
       {
        xSpinner = new JSpinner();
        final JSpinner spinner = xSpinner;
        Integer value = new Integer(0);
        Integer min = new Integer(0);
        Integer max = new Integer(255);
        Integer step = new Integer(1);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
        spinner.setModel(spinnerModel);
        spinner.setToolTipText("X Value");  
        
        spinner.addChangeListener( new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int val = ((Integer)spinner.getModel().getValue()).intValue();
                    updateXValue(val);
                }
            });        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("X Pos:"), BorderLayout.CENTER);
        panel.add(spinner, BorderLayout.EAST);
        settingsPanel.add(panel);

       }
     {

        // Y position of top of sprite 
        // Sprite data is delayed by one scanline; you must subtract 1 from the sprite's Y coordinate before writing it here. Hide a sprite by writing any values in $EF-$FF here. 
           
        ySpinner = new JSpinner();
        final JSpinner spinner = ySpinner;
        Integer value = new Integer(0);
        Integer min = new Integer(0);
        Integer max = new Integer(255);
        Integer step = new Integer(1);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
        spinner.setModel(spinnerModel);
        spinner.setToolTipText("Y Value");  
        
        spinner.addChangeListener( new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int val = ((Integer)spinner.getModel().getValue()).intValue();
                    updateYValue(val);
                }
            });        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Y Pos:"), BorderLayout.CENTER);
        panel.add(spinner, BorderLayout.EAST);
        settingsPanel.add(panel);

       }       
       {
        oamIndexSpinner = new JSpinner();
        final JSpinner spinner = oamIndexSpinner;
        Integer value = new Integer(0);
        Integer min = new Integer(0);
        Integer max = new Integer(3);
        Integer step = new Integer(1);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(value, min, max, step);
        spinner.setModel(spinnerModel);
        spinner.setToolTipText("OAM Index");  
        
        spinner.addChangeListener( new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int val = ((Integer)spinner.getModel().getValue()).intValue();
                    updateOAMIndex(val);
                }
            });
         JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("OAM Index:"), BorderLayout.CENTER);
        panel.add(spinner, BorderLayout.EAST);
        settingsPanel.add(panel);            
    
       }
       {
        isFlippedHorizontalCB = new JCheckBox("Flip Horizontal");        
        isFlippedHorizontalCB.setToolTipText("Flip the Sprite on the Horizontal Axis");  
        final JCheckBox cb = isFlippedHorizontalCB;
        isFlippedHorizontalCB.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    updateHorizontalAxis(cb.isSelected());
            }
        });
        settingsPanel.add(isFlippedHorizontalCB);
       }
       {
        isFlippedVerticalCB = new JCheckBox("Flip Vertical");        
        isFlippedVerticalCB.setToolTipText("Flip the Sprite on the Vertical Axis");  
        final JCheckBox cb = isFlippedVerticalCB;
        isFlippedVerticalCB.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    updateVerticalAxis(cb.isSelected());
            }
        });     
        settingsPanel.add(isFlippedVerticalCB);
       }
       {
        isUnderCB = new JCheckBox("Under Background");        
        isUnderCB.setToolTipText("Whether the sprite should be displayed under the background");  
        final JCheckBox cb = isUnderCB;
        isUnderCB.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    updateUnderBG(cb.isSelected());
            }
        });                       
            
        settingsPanel.add(isUnderCB);
       }
       

       
    }
    
    public int getSpriteBank(){
        return 1;
    }
     public void drawKeyFrame(AnimationKeyframeData kf){
     if(kf == null){
             return;
         }
        CHRTile spr = new CHRTile(modelRef.getCHRModel().getPatternTableBytesForTile(getSpriteBank(), kf.spriteIndex), PPUConstants.SPRITE_PALETTE_TYPE, modelRef, kf.oamIndex);
        int  pix[] = spr.getPix();
        
        for(int y=0;y<8;y++){
            int curY = (y + kf.yPos) & 0xFF;
            int newY = (kf.isFlippedHorizontal) ? (7-y) : y;
            if(curY < 240) {
                int offY = curY * 256;
                for(int x=0;x<8;x++){
                    int curX = (x + kf.xPos) & 0xFF;
                    int newX = (kf.isFlippedVertical) ? (7-x) : x;
                    sprPix[offY+curX] = pix[newY*8+newX];
                }
            }
        }
        
     }
     
    public void updateAnimationView(){
        if(_ntp != null){
            // clear the old
            int cnt = 0;
            for(int i=0;i<currentPixelArrayHeight;i++){
                System.arraycopy(transparentPix,0,sprPix,cnt,transparentPix.length);
                cnt += transparentPix.length;
            } 
            // write the new
            AnimationKeyframeData applicableKF = null;
            for(int i=modelRef.getNumAnimations()-1 ; i>=0 ;i-- ){
                applicableKF = null;            
                for(int j=currentIndex;j>=0;j--){
                    if(modelRef.getKeyFrame(i,j) != null){
                        applicableKF = modelRef.getKeyFrame(i,j);
                        break;
                    }
                }
                drawKeyFrame(applicableKF);
            }
           _ntp.refreshImageLevels();
        }
    }

    private void setDimensions(){
        int numWide = modelRef.getAnimationDuration(0); 
        if(numWide == 0){
            numWide = CHREditorModel.MAX_FRAMES;
        }
        int numHigh = modelRef.getNumAnimations();
        setMinimumSize(new Dimension(numWide*10,numHigh*20 + BORDER));
        setPreferredSize(new Dimension(numWide*10,numHigh*20 + BORDER));
    }
    
    private void setupMouseStuff(){
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                setToolTipText(getTooltipString(e.getX(), e.getY()));
            }
            public void mouseDragged(MouseEvent e) {
                doClick(e.getX()
                , e.getY()
                ,(e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK
                        ,false, e ); // (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK);
                
            }
        });
        addMouseListener(new MouseAdapter() {
            //   public void mouseClicked(MouseEvent e) {
            //   }
            //   public void mouseEntered(MouseEvent e) {
            //   }
            //   public void mouseExited(MouseEvent e) {
            //   }
            //   public void mousePressed(MouseEvent e) {
            //   }
            public void mouseReleased(MouseEvent e) {
                doClick(e.getX()
                , e.getY()
                ,(e.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK
                ,(e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK
                ,e);
            }
        });
    }
    public void setAnimationModeController( JCheckBox animCB ) {
        _animCBCallback = animCB;
    }
    
    private void doClick(int xVal, int yVal, boolean leftClick, boolean rightClick,MouseEvent e){
        int columnIndex = determineColumn(xVal);
        int rowIndex = determineRow(yVal);
        
        if(rowIndex == -1 && columnIndex != -1){
            updateController(rowIndex, columnIndex);
        }
        // break out here if its invalid or a header update
        if(rowIndex < 0 || columnIndex < 0){
            return;
        }
        
        if(rowIndex >= modelRef.getNumAnimations()){
            return;
        }
        if(columnIndex >= modelRef.getAnimationDuration(rowIndex)){
            return;
        }   
        if(_animCBCallback != null ) {
            _animCBCallback.setSelected(false); // this wont trigger the event for some reason??
            setAnimationMode(false);
        }
         if (e.isPopupTrigger()) {            
            popupColumnIndex = columnIndex;
            popupRowIndex = rowIndex;
            _popup.show(e.getComponent(), e.getX(), e.getY());
        } else {
            processClick(rowIndex, columnIndex, leftClick, rightClick);
        }
    }
    
    public void addAnimation(){
        modelRef.addAnimation();
        setDimensions();
        revalidate();
        updateStuff();
    }
    public void removeAnimation(int index){
        if(modelRef.getNumAnimations() <= 0){
            return;
        }
        modelRef.removeAnimation(index);
        setDimensions();
        revalidate();
        updateStuff();
    }
    
    public int determineRow(int yVal){
        int numAnim = modelRef.getNumAnimations();
        int hgt = oldHeight / (numAnim + 1);
        if(yVal < 0 || yVal >= oldHeight){
            return -2;
        }
        if(yVal < hgt){
            return -1;
        }
        return (yVal / hgt)-1;
    }
    
    public int determineColumn(int xVal){
        int numWide = modelRef.getAnimationDuration(activeRow);
        if(numWide <= 0){
            numWide = CHREditorModel.MAX_FRAMES;
        }
        int step = oldWidth / numWide;
        if(step <= 0){
            step = 3;
        }
        if(xVal >= step * numWide){
            return -1;
        }
        if(xVal < 0){
            return -1;
        }
        return (xVal / step);
    }
    
    public void updateController(int row, int column){
       boolean requiresUpdate = false;
       if(row >= 0 && row < modelRef.getNumAnimations() && activeRow != row){
            activeRow = row;
            requiresUpdate = true;
        }
        if(currentIndex != column){            
            currentIndex = column;
            requiresUpdate = true;
        }
       if(requiresUpdate) {
           updateSettingsPanel();
           updateStuff();
        }
    }
    public void updateStuff(){
         updateOffscreenImage();
         repaint();
         updateAnimationView();        
    }
    
    public void movePlayhead(){
        int oldIndex = currentIndex;
        currentIndex = _stateMachine.adjustState(currentIndex);
        if(oldIndex != currentIndex){
            updateStuff();
        }
    }
    
    public JPanel getSettingsPanel(){
        return settingsPanel;
    }
    
    private void setupPopupMenu(){
        _popup = new JPopupMenu();
                
        JMenuItem removeItem = new JMenuItem("Remove Animation");
        removeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeAnimation(popupRowIndex);
                popupRowIndex = -1;
            }
        }
        );
        _popup.add(removeItem);

        JMenuItem kfItem = new JMenuItem("Add/Remove Keyframe");
        kfItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(modelRef.getKeyFrame(popupRowIndex,popupColumnIndex) == null){
                    modelRef.constructKeyFrame(popupRowIndex,popupColumnIndex);
                    activeRow = popupRowIndex;
                    currentIndex = popupColumnIndex;
                } else {
                    modelRef.removeKeyFrame(popupRowIndex,popupColumnIndex);
                    activeRow = popupRowIndex;
                    currentIndex = popupColumnIndex;
                }
                popupRowIndex = -1;
                updateStuff();
            }
        }
        );
        _popup.add(kfItem);
        
        
    }
    
    
    public void processClick(int row, int column, boolean leftClick, boolean rightClick){
        updateController(row, column);
    }
    
    public boolean isActiveKeyframe(int row, int index){
        return ( modelRef.getKeyFrame(row,index) != null);
    }
    
    public String getTooltipString(int x, int y){
        int rowIndex = determineRow(y);
        int columnIndex = determineColumn(x);
        
        
        if(rowIndex < 0 || columnIndex < 0){
            return "";
        }
        if(columnIndex >= modelRef.getAnimationDuration(rowIndex)) {
            return "";
        }
        return "Sprite: " + rowIndex + "Frame: " + columnIndex + " " + (isActiveKeyframe(rowIndex,columnIndex) ? " Has Keyframe" : "");
    }
    
    
    public void updateOffscreenImage(){
        if(offscreenGraphics == null){
            return;
        }
        offscreenGraphics.setColor(Color.WHITE);
        offscreenGraphics.fillRect(0,0,oldActualWidth, oldActualHeight);
        
        int numWide = modelRef.getAnimationDuration(activeRow);
        if(numWide <= 0){
            numWide = CHREditorModel.MAX_FRAMES;
        }
        
        int step = oldWidth / numWide;
        if(step <= 0){
            step = 3;
        }
        int mx = step * numWide;
        int numAnim = modelRef.getNumAnimations();
        int hgt = oldHeight / (numAnim + 1);
        int centeredHgt = (hgt - step)/2;
        int yPos = hgt;
        
        // draw upper controller
        offscreenGraphics.setColor(Color.GREEN);
        offscreenGraphics.drawLine(0,hgt-step,mx,hgt-step);
        offscreenGraphics.fillRect(currentIndex*step,hgt-step,step,step);
        
        for(int row=0;row<numAnim;row++){
            int pos = 0;
            if(row == activeRow) {
                offscreenGraphics.setColor(Color.CYAN);
                offscreenGraphics.fillRect(0,yPos,mx, hgt);
            }
            for(int column=0;column<modelRef.getAnimationDuration(row);column++){
                
                if(currentIndex == column){
                    offscreenGraphics.setColor(Color.RED);
                    offscreenGraphics.drawLine(pos+(step/2),yPos,pos+(step/2),yPos+hgt);
                }
                if(isActiveKeyframe(row,column)){
                    offscreenGraphics.setColor(Color.BLUE);
                    offscreenGraphics.fillOval(pos,yPos+centeredHgt,step,step);
                }
                offscreenGraphics.setColor(Color.LIGHT_GRAY);
                offscreenGraphics.drawLine(pos,yPos,pos,yPos+hgt);
                
                pos += step;
            }
            offscreenGraphics.setColor(Color.BLACK);
            offscreenGraphics.drawLine(0,yPos,mx, yPos);
            offscreenGraphics.drawLine(0,yPos,0, yPos+hgt);
            offscreenGraphics.drawLine(mx,yPos,mx, yPos+hgt);
            
            yPos += hgt;
        }
        offscreenGraphics.setColor(Color.BLACK);
        offscreenGraphics.drawLine(0,yPos,mx, yPos);
        
        offscreenGraphics.setColor(Color.DARK_GRAY);
        offscreenGraphics.drawRect(0,0,oldActualWidth-1, oldActualHeight-1);
    }
    
    public void paintComponent(Graphics g){
        Dimension d = getSize();
        if(offscreenImg == null || d.width != oldActualWidth || d.height != oldActualHeight){
            oldActualWidth  = d.width;
            oldActualHeight = d.height;
            oldWidth = oldActualWidth;
            oldHeight = oldActualHeight - BORDER;
            offscreenImg = createImage(oldActualWidth, oldActualHeight);
            if(offscreenImg != null){
                offscreenGraphics = offscreenImg.getGraphics();
                updateOffscreenImage();
            }
        }
        super.paintComponent(g);    // paints background
        if(offscreenImg != null){
            g.drawImage(offscreenImg,0,0,this);
        }
        
    }
    public void run() {
        
     
        long lastTimeStamp =  0 ;
        while(_stateMachine.isAnimationMode()){
            lastTimeStamp = System.currentTimeMillis();
             movePlayhead();                    
             try {
                long remainder = (lastTimeStamp + _stateMachine.getAnimationDelay()) - System.currentTimeMillis();
                if(remainder > 0){
                    Thread.sleep(remainder);
                } else {
                  Thread.yield();      
                }
            } catch(InterruptedException ie){
                ie.printStackTrace();
                _stateMachine.setAnimationMode(false);
            }
                            
         }  
     }
}
