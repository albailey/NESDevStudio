/*
 * FileUtilities.java
 *
 * Created on October 5, 2006, 10:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package utilities;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;


/**
 *
 * @author abailey
 */
public class FileUtilities {

    private static String sPathOnly = null;

    /** Creates a new instance of FileUtilities */
    private FileUtilities() {
    }

    public static File checkFile(String fName, boolean verifyReadable) {
        File f = new File(fName);
        if (verifyReadable) {
            if (!(f.exists() && f.canRead())) {
                return null;
            }
        }

        String fPath = f.getAbsolutePath();
        sPathOnly = fPath.substring(0, (fPath.length() - f.getName().length()));
        EnvironmentUtilities.updateStringEnvSetting(EnvironmentUtilities.LAST_PROJECT_DIR_SETTING, sPathOnly);
        return f;
    }

    public static File selectFile(Component c, int selectMode, int fileType) {
        if (sPathOnly == null) {
            // Try to use a NON existant file in the current working directory to figure what the current working directory actually is
            String fPath = (new File("junk")).getAbsolutePath();
            sPathOnly = fPath.substring(0, (fPath.length() - "junk".length()));
        }
        // If the last location no longer exists, use the parent.
        // If the parent no longer exists, use the default
        File lastPath = new File(sPathOnly);
        if(!lastPath.isDirectory()){
            // try a parent folder
            File f = lastPath.getParentFile();
            if(f.isDirectory()){
                lastPath = f;
            }
        }
        JFileChooser fc = new JFileChooser(lastPath);
        fc.setFileSelectionMode(fileType);
        if (selectMode == JFileChooser.SAVE_DIALOG) {
            fc.showSaveDialog(c);
        } else {
            fc.showOpenDialog(c);
        }
        File f = fc.getSelectedFile();
        if (f != null) {
            String fPath = f.getAbsolutePath();
            sPathOnly = fPath.substring(0, (fPath.length() - f.getName().length()));
            EnvironmentUtilities.updateStringEnvSetting(EnvironmentUtilities.LAST_PROJECT_DIR_SETTING, sPathOnly);
        }
        return f;
    }

    public static File selectFileForOpen(Component c) {
        return selectFile(c, JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY);
    }

    public static File selectFileForSave(Component c) {
        return selectFile(c, JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY);
    }

    public static File selectDirectoryForSave(Component c) {
        return selectFile(c, JFileChooser.SAVE_DIALOG, JFileChooser.DIRECTORIES_ONLY);
    }

    public static boolean serializeObjectToFile(File f, Serializable obj) {
        boolean status = true;
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            try {
                oos.writeObject((Object) obj);
            } catch (Exception e) {
                e.printStackTrace();
                status = false;
            }
            oos.close();
            fos.close();
        } catch (Exception e) {
            System.err.println("Could not save file: " + f);
            e.printStackTrace();
            status = false;
        }
        return status;

    }

    public static Object deserializeObjectFromFile(File f) {
        Object obj = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try {
                obj = ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ois.close();
            fis.close();
        } catch (Exception e) {
            System.err.println("Could not load file: " + f);
        }
        return obj;

    }

    public static int loadBytes(File f, byte destBuffer[], int max) {
        return loadBytes(f, destBuffer, 0, max);
    }

    public static byte[] loadAsciiBytes(File f) {
        byte tmpBuf[] = new byte[1024];
        int curMax = 1024;
        int count = 0;

        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            StreamTokenizer st = new StreamTokenizer(br);

            st.lowerCaseMode(true);
            st.eolIsSignificant(false); // end of line separates tokens, and thats all
            st.wordChars('0', '9');
            st.ordinaryChar('$');
            st.wordChars(':',':');
            st.wordChars('_','_');  
            st.wordChars('a','z');
            st.wordChars('A','Z');
            
            st.whitespaceChars('.', '.');
            st.whitespaceChars(',', ',');
            
            try {
                int index = 0;
                byte b = 0;
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    String s;
                    switch (st.ttype) {
                        case StreamTokenizer.TT_EOL:
                            s = new String("EOL");
                            break;
                        case StreamTokenizer.TT_NUMBER:
                            s = Integer.toString((int)st.nval);
                            break;
                        case StreamTokenizer.TT_WORD:
                            s = st.sval; // Already a String
                            break;
                        default: // single character in ttype
                            s = String.valueOf((char) st.ttype);
                    }
                    if (count >= curMax) {
                        int newMax = curMax * 2;
                        byte b2[] = new byte[newMax];
                        System.arraycopy(tmpBuf, 0, b2, 0, curMax);
                        curMax = newMax;
                        tmpBuf = b2;
                    }
                    if (s.equals(".")) {
                        continue;
                    }
                    if (s.equals(",")) {
                        continue;
                    }
                    if (s.equals("byt")) {
                        continue;
                    }
                    if(s.contains(":")){
                    	continue;
                    }
                    if(s.contains("_")){
                    	continue;
                    }
                    // three ways to parse a byte
                    // one character at a time (ie: $00, ) where the 0s are parsed individually
                    // two characters at a time (ie: $bb,) where the bb comes as a single token
                    // weird hand coded madness (ie: $b, )

                    if (s.equals("$")) {
                        if (index > 0) {
                            // weird hand coded madness (ie: $b, )
                            tmpBuf[count] = b;
                            count++;
                        }
                        index = 0;
                        b = 0;
                        continue;
                    }
                    
                    index++;
                    b = (byte) ((b * 16) + Integer.parseInt(s, 16));
                    if (index == 2) {
                        // one character at a time (ie: $00, ) where the 0s are parsed individually
                        index = 0;
                        tmpBuf[count] = b;
                        count++;
                        continue;
                    }
                }
            } catch (Exception e) {
            	e.printStackTrace();
                // all done..
            }
            fr.close();
        } catch (Exception e) {
            System.err.println("Could not load file: " + f);
        }
        byte b3[] = new byte[count];
        System.arraycopy(tmpBuf, 0, b3, 0, count);
        return b3;
    }

    public static byte[] loadAllBytes(File f) {
        byte tmpBuf[] = new byte[1024];
        int curMax = 1024;
        int count = 0;
        try {
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);

            try {
                while (true) {
                    byte b = dis.readByte();
                    if (count >= curMax) {
                        int newMax = curMax * 2;
                        byte b2[] = new byte[newMax];
                        System.arraycopy(tmpBuf, 0, b2, 0, curMax);
                        curMax = newMax;
                        tmpBuf = b2;
                    }
                    tmpBuf[count] = b;
                    count++;
                }
            } catch (Exception e) {
                // all done..
            }
            dis.close();
            fis.close();
        } catch (Exception e) {
            System.err.println("Could not load file: " + f);
        }
        byte b3[] = new byte[count];
        System.arraycopy(tmpBuf, 0, b3, 0, count);
        return b3;
    }

    public static int loadBytes(File f, byte destBuffer[], int skip, int max) {
        int count = 0;
        try {
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);

            try {
                int preSkip = 0;
                while (preSkip < skip) {
                    dis.readByte();
                }

                while (count < max) {
                    byte b = dis.readByte();
                    destBuffer[count] = b;
                    // System.out.println("Loading: " + paletteCount + ") " + b );
                    count++;
                }
            } catch (Exception e) {
                // all done..                
            }
            dis.close();
            fis.close();
        } catch (Exception e) {
            System.err.println("Could not load file: " + f);
        }
        return count;
    }

    public static byte[] loadUnknownBytes(File f) {
        int count = 0;
        int max = 1024 * 32; // 32 K max supported by this method
        byte tempBuffer[] = new byte[max];
        try {
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);

            try {
                while (count < max) {
                    byte b = dis.readByte();
                    tempBuffer[count] = b;
                    // System.out.println("Loading: " + paletteCount + ") " + b );
                    count++;
                }
            } catch (Exception e) {
                // all done..                
            }

            dis.close();
            fis.close();
        } catch (Exception e) {
            System.err.println("Could not load file: " + f);
        }
        byte destBuffer[] = new byte[count];
        System.arraycopy(tempBuffer, 0, destBuffer, 0, count);
        return destBuffer;
    }

    public static byte[] loadUnknownBytesWithEmbeddedSize(File f) {
        int count = 0;
        byte destBuffer[] = null;
        try {
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);

            try {
                int max = dis.readInt();
                if (max > 4096) {
                    throw new java.lang.NumberFormatException("Larger than 4096 bytes is not supported.  Attempting to load:" + max);
                }
                destBuffer = new byte[max];
                while (count < max) {
                    byte b = dis.readByte();
                    destBuffer[count] = b;
                    // System.out.println("Loading: " + paletteCount + ") " + b );
                    count++;
                }
            } catch (Exception e) {
                // all done..                
            }

            dis.close();
            fis.close();
        } catch (Exception e) {
            System.err.println("Could not load file: " + f);
        }
        return destBuffer;
    }

    public static boolean saveAsciiBytes(File f, byte destBuffer[], int max, int entriesPerLine) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            for (int i = 0; i < max; i++) {
                if (i % entriesPerLine == 0) {
                    ps.println("");
                    ps.print(".byt $");
                    ps.print(ByteFormatter.formatSingleByteInt(destBuffer[i]));
                } else {
                    ps.print(",$");
                    ps.print(ByteFormatter.formatSingleByteInt(destBuffer[i]));
                }
            }
            ps.println("");
            ps.close();
            fos.close();
        } catch (Exception e) {
            System.err.println("Could not save file: " + f);
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public static boolean saveBytes(File f, byte destBuffer[], int max) {
        return saveBytes(f, destBuffer, 0, max);
    }

    public static boolean saveBytes(File f, byte destBuffer[], int start, int max) {
        return saveBytes(f, destBuffer, start, max, false);
    }

    public static boolean saveBytes(File f, byte destBuffer[], int start, int max, boolean includeSize) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            DataOutputStream dos = new DataOutputStream(fos);
            if (includeSize) {
                dos.writeInt(max - start);
            }
            for (int i = start; i < start + max; i++) {
                dos.writeByte(destBuffer[i]);
            }
            dos.close();
            fos.close();
        } catch (Exception e) {
            System.err.println("Could not save file: " + f);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean saveBytes(File f, byte destBuffer1[], int max1, byte destBuffer2[], int max2) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            DataOutputStream dos = new DataOutputStream(fos);

            for (int i = 0; i < max1; i++) {
                dos.writeByte(destBuffer1[i]);
            }
            for (int i = 0; i < max2; i++) {
                dos.writeByte(destBuffer2[i]);
            }
            dos.close();
            fos.close();
        } catch (Exception e) {
            System.err.println("Could not save file: " + f);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Generate a list of elements that make up the file.
     * Meaning:  c:\dir\foo.txt   gets added on the list like: foo.txt, dir, c:\  where foo.txt is the first element in the list
     */
    private static Vector<String> extractPathComponents(File origFile) {
        Vector<String> retList = new Vector<String>();
        try {
            File workingFile = origFile;
            while (workingFile != null && workingFile.getName().length() != 0) {
                //       System.out.println("Extracting:" + workingFile.getName() + " " + workingFile.getName().length()) ;
                retList.add(workingFile.getName());
                workingFile = workingFile.getParentFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            retList.clear();
        }
        return retList;
    }

    /**
     * Match the two ArrayLists to determine the relative nature of one to the other
     */
    private static String calculateRelativePath(Vector<String> homeList, Vector<String> unknownList) {

        // Step 1: Walk the ends of the lists to find the common root between the two lists
        // Step 2: Walk what remains of the homeList do determine how much to add backwards traversals (ie: ../ )
        // Step 3: Add the remains of the unknownList

        int homeIndex = homeList.size() - 1;
        int unknownIndex = unknownList.size() - 1;
        StringBuffer returnVal = new StringBuffer("");

        // Step 1: determine common root.
        while ((homeIndex >= 0) && (unknownIndex >= 0) && homeList.get(homeIndex).equals(unknownList.get(unknownIndex))) {
            // the root matches, move past it
            homeIndex--;
            unknownIndex--;
        }

        // Step 2: walk remainder of homeList (but ignore the filename)
        for (int i = homeIndex; i >= 1; i--) {
            returnVal.append("..");
            returnVal.append(File.separator);
        }

        // Step 3: Stick the remains of unknownList onto returnVal
        for (int i = unknownIndex; i >= 0; i--) {
            returnVal.append(unknownList.get(i).toString());
            if (i > 0) {
                returnVal.append(File.separator);
            }
        }

        //   System.out.println("Relative Path:" + returnVal);

        return returnVal.toString();
    }

    public static String generateRelativePath(File homeFile, File unknownFile) {
        return calculateRelativePath(extractPathComponents(homeFile), extractPathComponents(unknownFile));
    }
}
