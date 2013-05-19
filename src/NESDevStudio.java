/*
 * NESDevStudio.java
 *
 * Created on September 7, 2006, 10:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * Need to add Aliasing to the Font Helper
 * Need to get the Midi Helper working
 * Need a LOT of work on the Custom Fighter Tool
 *
 * @author abailey
 */
public class NESDevStudio {

	/** Creates a new instance of NESDevStudio */
	public NESDevStudio() {
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here
		ui.IDEView ide = new ui.IDEView();
		ide.show();

	}

}
