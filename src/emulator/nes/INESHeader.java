/*
 * INESHeader.java
 *
 * Created on November 8, 2007, 4:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes;

import emulator.nes.mappers.MapperFactory;

/**
 *
 * @author abailey
 */
public class INESHeader {

    public int _numPrgBanks = 0;
    public int _numChrBanks = 0;
    public int _baseMapperInfo = 0;
    public int _additionalMapperInfo = 0;
    public String _mapperName = "UNINITIALIZED";
    public boolean isValid = false;
    public Mapper mapper = null;

    /** Creates a new instance of INESHeader */
    public INESHeader() {
    }

    public Mapper getMapper() {
        if (mapper == null) {
            mapper = MapperFactory.constructMapperFromHeaderInfo(_baseMapperInfo);
        }
        return mapper;
    }

    protected boolean validateHeader(byte[] header) {
        if (header.length != 16) {
            System.err.println("Invalid header. Not 16 bytes???");
            return false;
        }
        // Now we validate the Header based on this document: http://nesdev.parodius.com/neshdr20.txt
        // Bytes:
        // 0 = 0x4E (charcter N)
        // 1 = 0x45 (character E)
        // 2 = 0x53 (character S)
        // 3 = 0x1A (Character Break)
        // 4 = number of prg banks
        // 5 = number of chr banks
        // 6 = Mapper, mirroring, etc..
        // 7 = More mapper info
        // 8 to 15 all zeroes
        if ((header[0] != 0x4E) ||
                (header[1] != 0x45) ||
                (header[2] != 0x53) ||
                (header[3] != 0x1A)) {
            System.err.println("Not a valid ROM file. Not an iNES header");
            return false;
        }
        int prgBanks = (int) (header[4] & 0xFF);
        int chrBanks = (int) (header[5] & 0xFF);
        int baseMapper = (int) (header[6] & 0xFF);
        int additionalMapper = (int) (header[7] & 0xFF);
        /*	for(int i=8;i<=15;i++){
        if(header[i] != 0){
        //	System.err.println("The remaining portion of the header (byte) " + i + " was not zero. Invalid iNES header");
        // return false;
        }
        }
         */
        _numPrgBanks = prgBanks;
        _numChrBanks = chrBanks;
        _baseMapperInfo = baseMapper;
        _additionalMapperInfo = additionalMapper;

        //     System.out.println("Header Information:");
        //     System.out.println("\tNum PRG Banks:" + _numPrgBanks);
        //     System.out.println("\tNum CHR Banks:" +  _numChrBanks);

        int mapperType = _baseMapperInfo / 16;
        _mapperName = (mapperType < 0 || mapperType > INES_ROM_INFO.MAX_MAPPER) ? "???" : INES_ROM_INFO.MAPPER_STRINGS[mapperType];

//        int mapperFlags = _baseMapperInfo % 16;
//        String mapFlag = (mapperFlags < 0 || mapperFlags > INES_ROM_INFO.MAX_MAPPER_FLAGS) ? "???" : INES_ROM_INFO.MAPPER_FLAGS[mapperFlags];
//        System.out.println("\tMapper Entry:" + _baseMapperInfo);
//        System.out.println("\tMapper:" + mapperType);
//        System.out.println("\tType:" + _mapperName);
//        System.out.println("\tFlags:" + mapFlag);
//        System.out.println("\tExtended:" + _additionalMapperInfo);      
//        _additionalMapperInfo = 0;
        return true;
    }
}
