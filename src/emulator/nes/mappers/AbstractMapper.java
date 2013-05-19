/*
 * AbstractMapper.java
 *
 * Created on January 8, 2009, 8:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package emulator.nes.mappers;

import emulator.nes.Mapper;
import emulator.nes.PPU;

/**
 *
 * @author abailey
 */
public abstract class AbstractMapper implements Mapper {

    protected byte prgData[] = null;
    protected byte chrData[] = null;
    protected PPU _ppuRef = null;

    /** Creates a new instance of AbstractMapper */
    public AbstractMapper() {
        prgData = new byte[0x8000];
        chrData = new byte[0x2000];
        _ppuRef = null;
    }

    public void setPPURef(PPU ppuRef){
        _ppuRef = ppuRef;
        initMapper();
    }

   public void initMapper() {
   }

   public byte[] getInitialPRGData() {
        // nothing is done by mapper 0, however the data IS doubled up if theres only 1 PRG bank
        byte b[] = new byte[0x8000];
        if ((prgData.length < 0x8000)) {
            System.err.println("Invalid PRG data");
        }
        if (prgData.length == 0x4000) {
            System.arraycopy(prgData, 0, b, 0, 0x4000);
            System.arraycopy(prgData, 0, b, 0x4000, 0x4000);
        } else {
            // load in the inital 8K
            System.arraycopy(prgData, 0, b, 0, 0x8000);
        }
        return b;
    }


    public void setPRGMemory(byte[] data) {
        prgData = data;
    }

    public void setCHRMemory(byte[] data) {
        chrData = data;
    }

    public boolean isWriteMapped(int address) {
        return false;
    }

    public boolean isReadMapped(int address) {
        return false;
    }

    public byte getMappedMemory(int address, boolean isDirect) {
        return prgData[(address - 0x8000)];
    }
    public byte getMappedMemory(int address) {
        return getMappedMemory(address, false);
    }

    public byte getMappedMemoryDirect(int address) {
        return getMappedMemory(address, true);
    }

    public void setMappedMemory(int address, byte val) {
        prgData[(address - 0x8000)] = val;
    }

   public boolean isCHRReadMapped(int address) {
        return false;
    }

    public boolean isCHRWriteMapped(int address) {
        return false;
    }

    public byte getCHRMappedMemory(int address, boolean isDirect) {
        return chrData[address];
    }
    public byte getCHRMappedMemory(int address) {
        return getCHRMappedMemory(address, false);
    }
    public byte getCHRMappedMemoryDirect(int address) {
        return getCHRMappedMemory(address, true);
    }

    public void setCHRMappedMemory(int address, byte val) {
        chrData[address] = val;
    }
}
