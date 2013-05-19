package emulator.nes.ui;

import emulator.nes.APU;

public interface APUListener {
	    void notifyUpdates(APU apu);
}
