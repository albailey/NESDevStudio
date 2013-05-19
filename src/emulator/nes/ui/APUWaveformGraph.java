/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emulator.nes.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Modification of CapturePlayback in the java sound demo source
 * Here's the copyright:
 *
 * @(#)CapturePlayback.java	1.11	99/12/03
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.

 */
public class APUWaveformGraph extends JPanel implements Runnable, APUBufferListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7865158229825399135L;
	private final Object syncObject = new Object();
    private Thread thread;
    private Color lineColor = Color.BLACK;
    private Color positionColor = Color.RED;
    private Color bgColor = Color.PINK;
    private double duration = 0;
    private double seconds = 0;
    private AudioFormat format = null;
    private SourceDataLine apuLine = null;

    // this should be redone
    private Vector<Line2D.Double> lines = new Vector<Line2D.Double>();

    public APUWaveformGraph(AudioFormat newFormat, SourceDataLine soundInputLine) {
        setBorder(new TitledBorder("Waveform"));
        setBackground(bgColor);

        setSize(new Dimension(256, 256));
        setMinimumSize(new Dimension(256, 256));
        setPreferredSize(new Dimension(256, 256));


        format = newFormat;
        apuLine = soundInputLine;
    }

    public void notifyBufferUpdates(byte[] soundBuffer, int offset, int updateSize) {
        createWaveForm(soundBuffer);
    }

    public int[] create8BitWaveform(byte[] audioBytes) {
        int[] audioData = null;

        int nlengthInSamples = audioBytes.length;
        audioData = new int[nlengthInSamples];
        if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
            for (int i = 0; i < audioBytes.length; i++) {
                audioData[i] = audioBytes[i];
            }
        } else {
            for (int i = 0; i < audioBytes.length; i++) {
                audioData[i] = audioBytes[i] - 128;
            }
        }

        return audioData;
    }

    public int[] create16BitWaveform(byte[] audioBytes) {
        int[] audioData = null;
        int nlengthInSamples = audioBytes.length / 2;
        audioData = new int[nlengthInSamples];
        if (format.isBigEndian()) {
            for (int i = 0; i < nlengthInSamples; i++) {
                /* First byte is MSB (high order) */
                int MSB = (int) audioBytes[2 * i];
                /* Second byte is LSB (low order) */
                int LSB = (int) audioBytes[2 * i + 1];
                audioData[i] = MSB << 8 | (255 & LSB);
            }
        } else {
            for (int i = 0; i < nlengthInSamples; i++) {
                /* First byte is LSB (low order) */
                int LSB = (int) audioBytes[2 * i];
                /* Second byte is MSB (high order) */
                int MSB = (int) audioBytes[2 * i + 1];
                audioData[i] = MSB << 8 | (255 & LSB);
            }
        }
        return audioData;

    }

    public void createWaveForm(byte[] audioBytes) {

        if (audioBytes == null) {
            return;
        }

        int[] audioData = null;
        if (format.getSampleSizeInBits() == 16) {
            audioData = create16BitWaveform(audioBytes);
        } else {
            audioData = create8BitWaveform(audioBytes);
        }
        Dimension d = getSize();
        int w = d.width;
        int h = d.height - 15;


  /*      double minY = 0;
        double maxY = 0;
        byte minByte = 0;
        byte maxByte = 0;
*/
        synchronized (syncObject) {

            lines.removeAllElements();  // clear the old vector





            int frames_per_pixel = audioBytes.length / format.getFrameSize() / w;
            byte my_byte = 0;
            double y_last = 0;
            int numChannels = format.getChannels();
            for (double x = 0; x < w && audioData != null; x++) {
                int idx = (int) (frames_per_pixel * numChannels * x);
                if (format.getSampleSizeInBits() == 8) {
                    my_byte = (byte) audioData[idx];
                } else {
                    my_byte = (byte) (128 * audioData[idx] / 32768);
                }
                double y_new = (double) (h * (128 - my_byte) / 256);
/*
                if(y_new < minY){
                    minY = y_new;
                }
                if(y_new > maxY){
                    maxY = y_new;
                }
                if(my_byte < minByte){
                    minByte = my_byte;
                }
                if(my_byte > maxByte){
                    maxByte = my_byte;
                }
*/
                lines.add(new Line2D.Double(x, y_last, x, y_new));
                y_last = y_new;
            }
        }
  //      System.out.println("Range:" + minY + " , " + maxY + " bytes:" + minByte +" , " + maxByte);
        repaint();
    }

    public void paint(Graphics g) {

        Dimension d = getSize();
        int w = d.width;
        int h = d.height;
        int INFOPAD = 15;

        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(getBackground());
        g2.clearRect(0, 0, w, h);
        g2.setColor(Color.white);
        g2.fillRect(0, h - INFOPAD, w, INFOPAD);

        synchronized (syncObject) {
            // .. render sampling graph ..
            g2.setColor(lineColor);
            for (int i = 1; i < lines.size(); i++) {
                g2.draw((Line2D) lines.get(i));
            }
        }



        // .. draw current position ..
        if (seconds != 0) {
            double loc = seconds / duration * w;
            g2.setColor(positionColor);
            g2.setStroke(new BasicStroke(3));
            g2.draw(new Line2D.Double(loc, 0, loc, h - INFOPAD - 2));
        }


    }

    public void start() {
        thread = new Thread(this);
        thread.setName("SamplingGraph");
        thread.start();
        seconds = 0;
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
    }

    public void run() {
        seconds = 0;
        while (thread != null) {
            if ((apuLine != null) && (apuLine.isOpen())) {
                long milliseconds = (long) (apuLine.getMicrosecondPosition() / 1000);
                seconds = milliseconds / 1000.0;
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                break;
            }

            repaint();

            while ((apuLine != null && !apuLine.isOpen())) {
                try {
                	Thread.sleep(10);
                } catch (Exception e) {
                    break;
                }
            }
        }
        seconds = 0;
        repaint();
    }
}
