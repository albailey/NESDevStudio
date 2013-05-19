/*
 * FontCriteriaModel.java
 *
 * Created on September 4, 2007, 3:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ui.fontHelper;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author abailey
 */
public class FontCriteriaModel {
    public final static char upperCase[] = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    public final static char lowerCase[] = { 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    public final static char numbers[] = { '0','1','2','3','4','5','6','7','8','9'};
    
    public Font theFont;
    public boolean isUpper;
    public boolean isLower;
    public boolean isNumbers;
    public boolean isPunctuation;
    public boolean isAscii;
    public int leftSpacing;
    public int bottomSpacing;
    public int tilesWide;
    public int tilesHigh;
    public boolean isAntiAliasing = true;

    
    /** Creates a new instance of FontCriteriaModel */
    public FontCriteriaModel(Font f, boolean isUpp, boolean isLow, boolean isNum, boolean isPunc, boolean isAsc, int lSpacing, int bSpacing, int tilesWid, int tilesHgt) {
        theFont = f;
        isUpper = isUpp;
        isLower = isLow;
        isNumbers = isNum;
        isPunctuation = isPunc;
        isAscii = isAsc;
        leftSpacing = lSpacing;
        bottomSpacing = bSpacing;
        tilesWide = tilesWid;
        tilesHigh = tilesHgt;
        
    }
    
    public Color getColor(){
        return Color.red;
    }
    
    public char[] determineText(){
        int sz = 0;
        if(isUpper){
            sz+= upperCase.length;
        }
        if(isLower){
            sz+= lowerCase.length;
        }
        if(isNumbers) {
            sz+= numbers.length;
        }

        char chars[] = new char[sz];
        int offset = 0;
        if(isUpper){
            System.arraycopy(upperCase,0,chars,offset,upperCase.length);
            offset+=upperCase.length;
        }
        if(isLower){
            System.arraycopy(lowerCase,0,chars,offset,lowerCase.length);
            offset+=lowerCase.length;
        }
        if(isNumbers){
            System.arraycopy(numbers,0,chars,offset,numbers.length);
            offset+=numbers.length;
        }
        return chars;
    }
}
