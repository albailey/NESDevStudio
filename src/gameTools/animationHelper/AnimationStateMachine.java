/*
 * AnimationStateMachine.java
 *
 * Created on October 4, 2008, 11:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gameTools.animationHelper;

import ui.chr.model.CHREditorModel;

/**
 *
 * @author abailey
 */
public class AnimationStateMachine {
    
    public final static int PLAYHEAD_BACKWARD = -1;
    public final static int PLAYHEAD_STOP = 0;
    public final static int PLAYHEAD_FORWARD = 1;
    
    
    public final static int PLAYHEAD_PLAYONCE_MODE = 0;
    public final static int PLAYHEAD_LOOP_MODE = 1;
    public final static int PLAYHEAD_OSCILLATE_MODE = 2;
    
    private int _animationDelay = 1000/60; // 60 fps
    private boolean _animationRunning = false;
    private boolean _ntscMode = true;
    private Thread _animationThread = null;
    private Runnable _runnable = null;
    
    private int playHeadMode = PLAYHEAD_FORWARD;
    private int mode = PLAYHEAD_LOOP_MODE;
    
    /** Creates a new instance of AnimationStateMachine */
    public AnimationStateMachine() {
    }
    
    public int adjustState(int currentIndex){
        if(playHeadMode == PLAYHEAD_FORWARD){
            currentIndex++;
            if(currentIndex >= CHREditorModel.MAX_FRAMES){
                if(mode == PLAYHEAD_LOOP_MODE){
                    currentIndex = 0;
                } else if(mode == PLAYHEAD_OSCILLATE_MODE){
                    currentIndex--;
                    playHeadMode = PLAYHEAD_BACKWARD;
                } else {
                    currentIndex--;
                    playHeadMode = PLAYHEAD_STOP;
                    _animationRunning = false;
                }
            }
        } else if(playHeadMode == PLAYHEAD_BACKWARD){
            currentIndex--;
            if(currentIndex < 0){
                if(mode == PLAYHEAD_LOOP_MODE){
                    currentIndex = CHREditorModel.MAX_FRAMES;
                } else if(mode == PLAYHEAD_OSCILLATE_MODE){
                    currentIndex++;
                    playHeadMode = PLAYHEAD_FORWARD;
                } else {
                    currentIndex++;
                    playHeadMode = PLAYHEAD_STOP;
                    _animationRunning = false;
                }
            }
        }
        return currentIndex;
    }
    
    
    public void setRunnable(Runnable runnable){
        _runnable = runnable;
    }
    
    public boolean isAnimationMode(){
        return _animationRunning;
    }
    
    public boolean isNTSCMode(){
        return _ntscMode;
    }
    public int getAnimationDelay(){
        return _animationDelay;
    }
    public void setNTSCMode(boolean flag){
        _ntscMode =flag;
        if(_ntscMode){
            _animationDelay = 1000/60;
        } else {
            _animationDelay = 1000/50;
        }
    }
    
    public void setAnimationMode(boolean flag){
        if(_animationRunning == flag){
            return;
        }
        _animationRunning = flag;
        if(!_animationRunning){ // we just stopped it
            try {
                _animationThread.join();
            } catch(InterruptedException ie){
                ie.printStackTrace();
            }
            _animationThread = null;
        } else {
            // we need to start it
            _animationThread = new Thread(_runnable);
            _animationThread.start();
        }
    }
    
    
}
