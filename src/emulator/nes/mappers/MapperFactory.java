/*
 * MapperFactory.java
 *
 * Created on October 25, 2007, 1:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes.mappers;

import javax.swing.JOptionPane;

import emulator.nes.Mapper;

/**
 *
 * @author abailey
 */
public class MapperFactory {
    
    /** Creates a new instance of MapperFactory */
    private MapperFactory() {
    }
    
    // likely need more info for this factory in determining which mapper to make and how to make it in case mirroring is important
    public static Mapper constructMapperFromHeaderInfo(int iNESNumber) {
        int mapperType = (iNESNumber & 0xFF) / 16;
        Mapper mapper = null;
        switch(mapperType) {
            case 0: // NROM
                mapper = new Mapper_000_NROM();
                break;
            case 1: // MMC1
                mapper = new Mapper_001_SxROM_MMC1();
                break;
            case 2: // UNROM
                 mapper = new Mapper_002_UxROM();
                break;
            case 3: // CNROM
                 mapper = new Mapper_003_CNROM();
                break;
            case 4: // MMC1
                mapper = new Mapper_004_MMC3();
                break;
             case 7: // AxROM
                 mapper = new Mapper_007_AxROM();
                break;
            case 9: // PxROM MMC2
                 mapper = new Mapper_009_PxROM_MMC2();
                 break;
            default:
                System.err.println("Mapper: #" + mapperType + " ( " + iNESNumber + " ) not yet supported");
                JOptionPane.showMessageDialog(null, "Mapper: #" + mapperType + " ( " + iNESNumber + " ) not yet supported", "Error", JOptionPane.ERROR_MESSAGE);

        }
        return mapper; // this may be null
    }
    
}
