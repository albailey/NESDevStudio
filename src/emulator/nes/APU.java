/*
 * APU.java
 *
 * Created on October 25, 2007, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package emulator.nes;

import java.util.Iterator;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import emulator.nes.ui.APUBufferListener;
import emulator.nes.ui.APUListener;

/**
 * Sound Hardware in the NES
 * http://wiki.nesdev.com/w/index.php/APU_basics
 * http://wiki.nesdev.com/w/index.php/APU_Envelope
 * @author abailey
 */
public class APU implements IOMappedMemory, Runnable {

	public final static boolean SQUARE1_ENABLED = true;
	public final static boolean SQUARE2_ENABLED = true;
	public final static boolean TRIANGLE_ENABLED = true;
	public final static boolean NOISE_ENABLED = true;
	public final static boolean  TEST_CHANNEL_MODE = true;
	
	// constants
	private static final boolean APU_DEV_MODE = false;
	private static final boolean DISABLE_SOUND = true; // turn off audio
	
	// indexes into the sounds arrays
	private static final int SQUARE1 = 0;
	private static final int SQUARE2 = 1;
	private static final int TRIANGLE = 2;
	private static final int NOISE = 3;
	private static final int DMC = 3;

	
	private boolean oldWay = false;
	
	private static final int LENGTH_TABLE[] =
	{ 10, 254, 20,  2, 40,  4, 80,  6,
		160,  8, 60, 10, 14, 12, 26, 14,
		12,  16, 24, 18, 48, 20, 96, 22,
		192, 24, 72, 26, 16, 28, 32, 30 
	};

	// 32 entry lookup table for square 1 and 2
	private static float pulse_table[] = null;

	// 203 entry lookup table for triangle, noise, dmc
	private static float tnd_table[] = null;
	private boolean _justSet = false;
	private boolean drainMode = false;
	private int triggerCount = 0;
	private int lastCount = 0;
	// variables

	private int volumeDuty[] = { 0,0,0,0};
	private boolean volumeLengthCounterHalt[] = { false, false, false, false};
	private int volumeEnvelope[] = { 0,0,0,0};
	private int volume[] = { 0,0,0,0};
	private int sweep[] = { 0,0,0,0};
	private int periodLow[] = { 0,0,0,0};
	private int periodHigh[] = { 0,0,0,0};
	private int volumeLengthCounter[] = { 0,0,0,0};
	private boolean flags[] = { false, false, false, false, false};


	byte[] apu_register = new byte[ 0x18 ];

	private boolean _fourStepSequencerMode   = false;
	private int _dmcBytesRemaining         = 0;


	private boolean _dmcInterruptFlag       = false;
	private boolean _frameInterruptFlag    = false;
	private boolean _inhibitInterrupt = false;

	private int fCPU = 11025; // ???
			private int square1TVal = 0;
			private int square1Freq = 0;
			private int square2TVal = 0;
			private int square2Freq = 0;

			// triangle stuff
			private int tri_phase = 0;
			private int tri_timer = 0;
			private int tri_sequencer_val = 0;
			private int triangleCounterReloadVal = 0;

			private final static int NTSC_MODE = 0;
			private final static int PAL_MODE = 1;
			private int _mode = NTSC_MODE;
			private int _clockCount = 0;
			private int _clocks = 0;
			private int internal_step = 1;
			private int sequencer = 0;
			// the following statics are based on BLARGG's post
			// first part is NTSC, second is PAL
			public final static int SEQUENCER_4STEP[][] = {
				{ 7459, 7456, 7458, 7458, 7458 }
				,{ 8315, 8314, 8312, 8314, 8314 }
			};

			public final static int SEQUENCER_5STEP[][] = {
				{ 1, 7458, 7456, 7458, 7458, 7452 }
				,{ 1, 8314, 8314, 8312, 8314, 8312 }
			};

			// Blargg's post: http://nesdev.parodius.com/bbs/viewtopic.php?t=6603
			/*
 Mode 0: 4-step sequence

Action      Envelopes &     Length Counter& Interrupt   Delay to next
            Linear Counter  Sweep Units     Flag        NTSC     PAL
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
$4017=$00   -               -               -           7459    8315
Step 1      Clock           -               -           7456    8314
Step 2      Clock           Clock           -           7458    8312
Step 3      Clock           -               -           7458    8314
Step 4      Clock           Clock       Set if enabled  7458    8314


Mode 1: 5-step sequence

Action      Envelopes &     Length Counter& Interrupt   Delay to next
            Linear Counter  Sweep Units     Flag        NTSC     PAL
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
$4017=$80   -               -               -              1       1
Step 1      Clock           Clock           -           7458    8314
Step 2      Clock           -               -           7456    8314
Step 3      Clock           Clock           -           7458    8312
Step 4      Clock           -               -           7458    8314
Step 5      -               -               -           7452    8312
			 */

			private boolean _soundSupported = false;
			private SourceDataLine soundInputLine = null;
			private AudioFormat _format = null;

			private Vector<APUListener> _listeners = new Vector<APUListener>();
			private Vector<APUBufferListener> _bufferListeners = new Vector<APUBufferListener>();

			// supported sample rates
			// 8000, 11025, 16000. 22050, 44100

			// 60 fps. 735 samples per frame = 44100 Hz audio
			private final static int sampleRate = 4410;
			private final static int soundBufferChunkSize = 735;
			private final static int soundBufferNumFrames = 6;
			private final static int SOUND_BUFFER_SIZE = soundBufferChunkSize * soundBufferNumFrames ;

			private int soundBufferOffset = 0;

			private int actualBufferSize = SOUND_BUFFER_SIZE;
			private byte soundBuffer[] = new byte[SOUND_BUFFER_SIZE];
			private int soundBufferIndex = 0;
			private boolean bufferReady = false;
			private NES _nes = null;


			/** Creates a new instance of APU */
			public APU() {
				try
				{
					initLookupTables();
					// Create 44100 Hz 8-bit Audio Format.
					//_format = new AudioFormat(sampleRate,8,1,false,false);
					// Note: Nintendulator NTSC audio uses: 44100Hz 16 bit audio buffer, 4 frames (1470 bytes per frame)

					float rate = sampleRate;
					int sampleSize = 8;
					boolean signed = false;
					AudioFormat.Encoding encoding =  (signed) ? AudioFormat.Encoding.PCM_SIGNED : AudioFormat.Encoding.PCM_UNSIGNED;
					boolean bigEndian = false;
					int channels = 1; // 1 means mono.  2 means stereo

					_format = new AudioFormat(encoding, rate, sampleSize,
							channels, (sampleSize/8)*channels, rate, bigEndian);


					_format = new AudioFormat(sampleRate, 8, 1, false, false);


					// Check if the Format is Supported on this System
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, _format);
					if (AudioSystem.isLineSupported(info) ) {
						soundInputLine = (SourceDataLine) AudioSystem.getLine(info);
						//    System.out.println("SourceDataLine buffer:" + soundInputLine.getBufferSize() );
						//    actualBufferSize = soundInputLine.getBufferSize();
						//    soundBuffer = new byte[actualBufferSize];

						//        soundInputLine.open(_format, soundInputLine.getBufferSize());
						soundInputLine.open(_format);
						soundInputLine.start();
						_soundSupported = true;
						
						for(int i=0;i<soundBuffer.length;i++){
							soundBuffer[i] = 64;
						}
						
						Thread audioWriterThread = new Thread(this);
						audioWriterThread.start();

						//     System.out.println("Data line size:" + soundInputLine.getBufferSize());

					} 
				} catch(Exception e) {
					System.err.println("Audio apparently is not supported");
					_soundSupported = false;
				}
				
				if(DISABLE_SOUND) {
					// TURNING OFF AUDIO UNTIL I GET IT WORKING
					_soundSupported = false;
					System.err.println("AUDIO support disabled");
				} else {
					System.err.println("APU NOISE not implemented");
					System.err.println("APU DMC not implemented");
				}
			}
			
			public void finishFrame() {
			}
			
			private static void initLookupTables() {
				pulse_table = new float[32];
				for(int i=0;i<32;i++){
					pulse_table[i] = 95.52f / (8128.0f / (float)i + 100);
				}

				tnd_table = new float[203];
				for(int i=0;i<203;i++){
					tnd_table[i] = 163.67f / (24329.0f / (float)i + 100);
				}
			}

			public AudioFormat getFormat() {
				return _format;
			}

			public SourceDataLine getLine() {
				return soundInputLine;
			}


			public String toString() {
				notifyBufferUpdates( soundBufferOffset, soundBufferChunkSize);
				return "Clocks: " + _clocks + " IRQ:" + _frameInterruptFlag;
			}
			// http://wiki.nesdev.com/w/index.php/APU_Mixer_Emulation
			private float apu_mix(int pulse1, int pulse2, int triangle, int noise, int dmc) {

				// linear approximation
				// return ( 0.00752f * (pulse1 + pulse2)) + (0.00851f * triangle + 0.00494f * noise + 0.00335f * dmc);

				// using a lookup table
				return pulse_table [pulse1 + pulse2] + tnd_table [3 * triangle + 2 * noise + dmc];
			}

			public void setNES(NES nes){
				_nes = nes;
			}

			public void notifyAPUChanged() {
				if(_listeners.size() > 0){
					Iterator<APUListener> itor = _listeners.iterator();
					while(itor.hasNext()){
						itor.next().notifyUpdates(this);
					}
				}      
			}
			public void addAPUListener(APUListener listener){
				_listeners.add(listener);
			} 
			public void addAPUBufferListener(APUBufferListener listener){
				_bufferListeners.add(listener);
			}
			private void notifyBufferUpdates(int offset, int updateSize) {
				if(_bufferListeners.size() > 0){
					Iterator<APUBufferListener> itor = _bufferListeners.iterator();
					while(itor.hasNext()){
						itor.next().notifyBufferUpdates(soundBuffer, offset, updateSize);
					}
				}            // pass soundBuffer to anything that cares.
			}

			public void run() {
				int frameCount =  0;
				while(_soundSupported){

	//				if(bufferReady) {
						if(oldWay) {
							soundInputLine.write(soundBuffer, soundBufferOffset, soundBufferChunkSize);
							notifyBufferUpdates( soundBufferOffset, soundBufferChunkSize);
							soundBufferOffset += soundBufferChunkSize;
							if(soundBufferOffset >= soundBuffer.length) {
									soundBufferOffset = 0;
							}						
							System.out.println("Count:" + lastCount + "  Index: " + soundBufferIndex + " Written:" + soundInputLine.write(soundBuffer, 0, soundBuffer.length));
							lastCount = 0;
							if(drainMode) {
								frameCount++;
								if(frameCount == 10){
									frameCount = 0;
									soundInputLine.drain();                    
								}
							}
							
						} else if(soundInputLine.available() > soundBufferChunkSize) {
//								if(soundInputLine.available() >= soundBuffer.length){
								int written = soundInputLine.write(soundBuffer, soundBufferOffset, soundBufferChunkSize);
								//int written = soundInputLine.write(soundBuffer, 0, soundBuffer.length);
								System.out.println("Wrote:" + written + " had:" + lastCount);
								lastCount -= soundBufferChunkSize;
								if(lastCount > actualBufferSize){
									soundInputLine.write(soundBuffer, 0, actualBufferSize);
									soundInputLine.drain();        
									lastCount = 0;
								}
						}
					 	
					}
				}
//			}

			public void setNTSCMode(boolean flag){
				_mode = (flag) ? NTSC_MODE : PAL_MODE; // setting true means NTSC, false means PAL mode
			}

			// http://wiki.nesdev.com/w/index.php/CPU_power_up_state
			public void powerOn(){
				// $4017 = $00 (frame irq enabled)
				// $4015 = $00 (all channels disabled)
				// $4000-$400F = $00 (not sure about $4010-$4013)


				_inhibitInterrupt = true;
				_frameInterruptFlag = false;
				triggerCount = 0;
				_clocks = 0;
				_clockCount  = 0;
				// no need to set internal_step and sequencer because setting 0 to 4017 will do this for us.
				for(int i=0;i<0xF;i++) {
					setRegister(i, (byte)0);
				}
				setRegister(0x15, (byte)0);
				setRegister(0x17, (byte)0);
				notifyAPUChanged();
				dropClock(9);
			}

			public void reset(){
				// APU mode in $4017 was unchanged
				// APU was silenced ($4015 = 0)
				_inhibitInterrupt = true;
				_frameInterruptFlag = false;
				triggerCount = 0;
				
				setRegister(0x17, apu_register[0x17]);
				setRegister(0x15, (byte)0);
				//  At reset, $4017 should should be rewritten with last value written
				_clocks = 0;
				
				notifyAPUChanged();
				dropClock(9);

			}
			private void dropClock(int dropCount) {
				if(_fourStepSequencerMode){
					sequencer -= dropCount;
					_clocks += dropCount;
					
				}
			}

			private void decrementLength(int index) {
				if(volumeLengthCounter[index] > 0 && (!volumeLengthCounterHalt[index])){
					volumeLengthCounter[index]--;
				}
			}

			private void clockEnvelope(){
				clock_square1_timer();
				clock_square2_timer();
			}

			private void clockLengthCounter(){
				if(hasSquare1Sound()){
					decrementLength(SQUARE1);
				}
				if(hasSquare2Sound()){
					decrementLength(SQUARE2);
				}
				if(hasTriangleSound()){
					decrementLength(TRIANGLE);
				}
				if(hasNoiseSound()){
					decrementLength(NOISE);
				}

			}
			private void clockSweepUnits(){

			}


			public void clockAPU() {
				//http://wiki.nesdev.com/w/index.php/APU_Frame_Counter
				// We are expecting to create a sample 735 times per frame
				// There are 446710  master cycles, ~29780 CPU cycles, and 89342 PPU cycles per frame
				// So maybe we are best to clock each CPU cycle, but only add about 740 samples per frame (leaves me some extras)
				// That means sampling each 40 apu cycles (735 * 40 ~= 29780 )
				if(_nes != null && triggerCount > 0){
						if(APU_DEV_MODE) {
							System.out.println("Triggering APU IRQ at clock: " + _clocks);
						}
						_frameInterruptFlag = true;						
						_nes.doIRQ(_frameInterruptFlag);
						triggerCount--;
				}
				_justSet = false;
				
				// a simple clock counter.  Unused, but gets cleared each IRQ period
				_clocks++;


				// Keep an internal counter that only updates every 40
				if(_clockCount > 0) {
					_clockCount--;
				} else {
					// reset back to 0
					_clockCount = 40;
					// calling soundInputPut.write is SLOW!!!!
					// this is why we need a separate thread to play audio as we fill in the buffer
				
					float soundRatio = apu_mix(square1_amp(), square2_amp(), triangle_amp(), noise_amp(), dmc_amp());
					
					//byte  soundValue = (byte)(((int)(soundRatio * 256)) & 0xFF);
					byte  soundValue = (byte)(((int)(soundRatio * 256)) & 0xFF);
					soundBuffer[soundBufferIndex] = soundValue;
					soundBufferIndex++;
					lastCount++;
					if(soundBufferIndex >= actualBufferSize){
						soundBufferIndex=0;
					} else {
						for(int i=soundBufferIndex;i<actualBufferSize;i++){
							soundBuffer[i] = soundValue;
						}
					}
					
					if(!bufferReady &&  soundBufferIndex > soundBufferChunkSize) {
						bufferReady = true;
					}
				}

				// keep an internal sequencer that updates the internal state each X clocks
				sequencer--;
				if(sequencer > 0){
					return;
				}
				
				// next part is only done 4 or 5 times per frame
				if(_fourStepSequencerMode) {
					clockAPUFourStepMode();
				} else {
					clockAPUFiveStepMode();
				}
			}
			// in 4 step mode, to run 240 Hz, I need to clock 4 times per frame
			private void clockAPUFourStepMode() {
				
				// we clock envelope on step 1,2,3,4 in either mode
				clockEnvelope();
				clockTriangleLinearCounter();

				if(internal_step == 2 || internal_step == 4) {
					clockLengthCounter();
					clockSweepUnits();
					if(internal_step == 4) {
						if(!_inhibitInterrupt) {
							triggerCount = 2; // apparently the irq flag is set 3 times in a row
							_frameInterruptFlag = true;
							_justSet = true;
							if(_nes != null){
								if(APU_DEV_MODE) {
									System.out.println("Triggering APU IRQ at clock: " + _clocks);
								}
								_nes.doIRQ(_frameInterruptFlag);
						}

						}												
					}
				}

				if(internal_step == 4){
					internal_step = 1;
					_clocks = 0;
				} else {
					internal_step++;
				}
				
				sequencer = SEQUENCER_4STEP[_mode][internal_step];
				notifyAPUChanged();

			}
			
			// in 5 step mode, to run 192 Hz I need to clock 3.2 times per frame
			private void clockAPUFiveStepMode() {
				if(internal_step != 5) {
					// we clock envelope on step 1,2,3,4 in either mode
					clockEnvelope();
					clockTriangleLinearCounter();
				}

				if(internal_step == 1 || internal_step == 3) {
					clockLengthCounter();
					clockSweepUnits();
				}
				

				if(internal_step == 5) {
					internal_step = 1;
					_clocks = 0;
				} else {
					internal_step++;					
				}

				sequencer = SEQUENCER_5STEP[_mode][internal_step];
				notifyAPUChanged();
			}
			

			// Need to add square 2, etc...
			private void clock_square1_timer() {
				square1TVal = (periodHigh[SQUARE1]<< 8) | (periodLow[SQUARE1]) << 1;
				square1Freq = fCPU/(16*(square1TVal + 1));
			}
			private void clock_square2_timer() {
				square2TVal = (periodHigh[SQUARE2]<< 8) | (periodLow[SQUARE2]) << 1;
				square2Freq = fCPU/(16*(square2TVal + 1));
			}

			int square1_amp() {
				return (hasSquare1Sound() ? volume[SQUARE1] : 0);
			}

			int square2_amp() {
				return (hasSquare2Sound() ? volume[SQUARE2] : 0);
			}



			


			// clock_tri_timer and triangle_amp provided by BLARGG http://nesdev.parodius.com/bbs/viewtopic.php?t=7007
			// Clocks triangle's timer. Should be called once per CPU cycle,
			// 1789773 times per second on NTSC.
			int getTriSeq(int phase){
				return ( phase < 16 ) ? phase : (31 - phase);
			}
			void clockTriangleLinearCounter() {
				tri_timer--;
				if ( tri_timer <= 0 ) {
					tri_timer = (periodHigh[TRIANGLE])*0x100 + periodLow[TRIANGLE] + 1;
					tri_phase = (tri_phase + 1) % 32;
				}
				tri_sequencer_val = getTriSeq(tri_phase);
			}

			// Current triangle amplitude. Call at any time.
			int triangle_amp() {
				return tri_sequencer_val;
			/*	if(!hasTriangleSound()){
					return 0;
				}
				if ( tri_phase < 16 )
					return tri_phase;
				else
					return 31 - tri_phase;
			 */					
			}



			int noise_amp() {
				return 0;
			}

			int dmc_amp() {
				return 0;
			}


			/*
Note: $4015 is the only R/W register here. All others do not respond to read 
cycles. Reads from $4016 and $4017 are decoded inside the 2A03, and those 
signals are available externally. Writes to bits D0-D2 of $4016 updates an 
internal 3-bit latch, with the status of those bits available externally.
			 */
			public boolean isReadMapped(int address){
				return (address == 0x4015);
			}
			public boolean isWriteMapped(int address){
				return (address >= 0x4000 && address <= 0x4017);
			}

			// therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
			public byte getMappedMemory(int address){
				return getRegister((address - 0x4000));
			}

			// therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
			public byte getMappedMemoryDirect(int address){
				return getRegister_direct((address - 0x4000));
			}

			// therefore 0x2000 to $3FFF are mapped and mirrored every 8 bytes
			public void setMappedMemory(int address, byte val){
				setRegister((address - 0x4000), val);
			}
			// deal with the 18 different registers
			private void setRegister(int register, byte val){
				apu_register[register] = val;
				switch (register) {
				case 0x00:  // 0x4000
					setRegister4000(val);
					break;
				case 0x01:  // 0x4001
					setRegister4001(val);
					break;
				case 0x02:  // 0x4002
					setRegister4002(val);
					break;
				case 0x03:  // 0x4003
					setRegister4003(val);
					break;
				case 0x04:  // 0x4004
					setRegister4004(val);
					break;
				case 0x05:  // 0x4005
					setRegister4005(val);
					break;
				case 0x06:  // 0x4006
					setRegister4006(val);
					break;
				case 0x07:  // 0x4007
					setRegister4007(val);
					break;
				case 0x08:  // 0x4008
					setRegister4008(val);
					break;
				case 0x09:  // 0x4009
					setRegister4009(val);
					break;
				case 0x0A:  // 0x400A
					setRegister400A(val);
					break;
				case 0x0B:  // 0x400B
					setRegister400B(val);
					break;
				case 0x0C:  // 0x400C
					setRegister400C(val);
					break;
				case 0x0D:  // 0x400D
					setRegister400D(val);
					break;
				case 0x0E:  // 0x400E
					setRegister400E(val);
					break;
				case 0x0F:  // 0x400F
					setRegister400F(val);
					break;
				case 0x10:  // 0x4010
					setRegister4010(val);
					break;
				case 0x11:  // 0x4011
					setRegister4011(val);
					break;
				case 0x12:  // 0x4012
					setRegister4012(val);
					break;
				case 0x13:  // 0x4013
					setRegister4013(val);
					break;
				case 0x14:  // 0x4014
					setRegister4014(val);
					break;
				case 0x15:  // 0x4015
					setRegister4015(val);
					break;
					//case 0x16:  // 0x4016
					//    break;
				case 0x17:  // 0x4017
					setRegister4017(val);
					break;
				default:
					if(APU_DEV_MODE) System.err.println("Invalid APU Set register:" + register);
					break;
				}
			}

			// only 4015 can be read
			private byte getRegister(int register){
				if(register == 0x15){
					return getRegister4015();
				} else {
					return  apu_register[register];
				}
			}

			// deal with the 18 different registers
			private byte getRegister_direct(int register){
				return apu_register[register];
			}


			/*
     $4015	IF-D.NT21	NES APU Status (read)
bit 7	I--- ----	DMC interrupt flag
bit 6	-F-- ----	Frame interrupt flag
bit 4	---D ----	DMC bytes remaining is non-zero
bit 3	---- N---	Noise channel's length counter is non-zero
bit 2	---- -T--	Triangle channel's length counter is non-zero
bit 1	---- --2-	Pulse channel 2's length counter is non-zero
bit 0	---- ---1	Pulse channel 1's length counter is non-zero
Side effects	 Clears the frame interrupt flag after being read (but not the DMC interrupt flag).
If an interrupt flag was set at the same moment of the read, it will read back as 1 but it will not be cleared.
			 */
			private byte getRegister4015(){
				boolean oldFrameInterruptFlag = _frameInterruptFlag;
				if(!_justSet){
					if(_frameInterruptFlag) {
				//		System.out.println("Clearing Interrupt 4015");
						_frameInterruptFlag = false; // need to handle a special case here
						//triggerCount = 0;
					}
				}
				byte b =
					(byte)((_dmcInterruptFlag    ? 0x80 : 0)
							| (oldFrameInterruptFlag      ? 0x40 : 0)
							| ((_dmcBytesRemaining > 0) ? 0x10 : 0)
							| ((volumeLengthCounter[NOISE] > 0)               ? 0x8  : 0)
							| ((volumeLengthCounter[TRIANGLE] > 0)            ? 0x4  : 0)
							| ((volumeLengthCounter[SQUARE2] > 0)             ? 0x2  : 0)
							| ((volumeLengthCounter[SQUARE1] > 0)             ? 0x1  : 0)
					);
				return b;
			}


			// Info on SQUARE (PULSE) channels
			// http://wiki.nesdev.com/w/index.php/APU_Pulse

			private void setSquareVolume(int index, int duty, boolean lengthCounterHalt, int envelope ){
				volumeDuty[index] = duty; // value between 0 and 3
				volumeLengthCounterHalt[index] = lengthCounterHalt;
				volumeEnvelope[index] = envelope;
				if(duty == 0)
					volume[index] = volumeEnvelope[index] /8;
				else if(duty == 1)
					volume[index] = volumeEnvelope[index] /4;
				else if(duty == 2)
					volume[index] = volumeEnvelope[index] /2;
				else if(duty == 3)
					volume[index] = volumeEnvelope[index]  /4; // ??


			}
			private void setSquareSweep(int index, int sweepVal ){
				sweep[index] = sweepVal;
			}

			private void setPeriodLow(int index, int val ){
				periodLow[index] = val;
			}

			private void setPeriodHigh(int index, int lengthCounterLoad, int val ) {
				volumeLengthCounter[index] = (flags[index]) ? LENGTH_TABLE[lengthCounterLoad] : 0;
				periodHigh[index] = val;
			}

			// SQUARE 1
			private void setRegister4000(byte val){
				// From the wiki:
				/*
   top 2 bits are Pulse Duty
         Duty Cycle Sequences
         Duty	 Waveform sequence
         0	0 1 0 0 0 0 0 0 (12.5%)
         1	0 1 1 0 0 0 0 0 (25%)
         2	0 1 1 1 1 0 0 0 (50%)
         3	1 0 0 1 1 1 1 1 (25% negated)

The sequencer is clocked by a timer whose period is the 12-bit value (%HHHL.LLLLLLL0, incorporating a left shift) formed by timer high and timer low, plus two. It further divides the timer's output by 8 to produce the audio frequency. So given the following:
fCPU = the clock rate of the CPU
tval = the 11-bit value that the program writes to the timer high and low registers
f = the frequency of the wave generated by this channel
The following relationships hold:
f = fCPU/(16*(tval + 1))
tval = fCPU/(16*f) - 1
The mixer receives the current envelope volume except when
The sequencer output is zero, or
The sweep unit is silencing the channel, or
The length counter is zero
The behavior of the two pulse channels differs only in the effect of the negate mode of their sweep units.
				 */

				setSquareVolume(SQUARE1, (val & 0xC0) >> 6, (((val & 0x20) >> 5) != 0), (val & 0x1F));
			}
			private void setRegister4001(byte val){
				setSquareSweep(SQUARE1, val&0xFF);
			}
			private void setRegister4002(byte val){
				setPeriodLow(SQUARE1, val&0xFF);
			}
			private void setRegister4003(byte val){
				setPeriodHigh(SQUARE1, (val & 0xF8) >> 3, (val & 0x7));
			}
			// SQUARE 2
			private void setRegister4004(byte val){
				setSquareVolume(SQUARE2, (val & 0xC0) >> 6, (((val & 0x20) >> 5) != 0), (val & 0x1F));
			}
			private void setRegister4005(byte val){
				setSquareSweep(SQUARE2, val&0xFF);
			}
			private void setRegister4006(byte val){
				setPeriodLow(SQUARE2, val&0xFF);
			}
			private void setRegister4007(byte val){
				setPeriodHigh(SQUARE2, (val&0xF8) >> 3, (val & 0x7));
			}

			private void setRegister4008(byte val){
				setTriangleSetup((val & 0x80) == 0x80, (val & 0x7F));
			}
			private void setRegister4009(byte val){
				// unused
				if(APU_DEV_MODE) System.err.println("Register 4009 is never used");
			}
			private void setRegister400A(byte val){
				setPeriodLow(TRIANGLE,  val&0xFF);
			}
			private void setRegister400B(byte val){
				setPeriodHigh(TRIANGLE, (val&0xF8) >> 3, (val & 0x7));
			}

			private void setRegister400C(byte val){
				setNoiseVolume((val & 0x20) == 0x20, val & 0x1F);
			}
			private void setRegister400D(byte val){
				// unused
				if(APU_DEV_MODE) System.err.println("Register 400D is never used");
			}
			private void setRegister400E(byte val){
				setNoiseLow((val & 0x80) == 0x80, val & 0xF);
			}
			private void setRegister400F(byte val){
				setNoiseHigh((val & 0xF8) >> 3);
			}
			private void setRegister4010(byte val){
				setDMCFreqency((val & 0x80) == 0x80,(val & 0x40) == 0x40, val & 0xF);
			}
			private void setRegister4011(byte val){
				setDMCRaw(val & 0xCF);
			}
			private void setRegister4012(byte val){
				setDMCSampleAddress(((val&0xFF) << 6) | 0xC000);
			}
			private void setRegister4013(byte val){
				setDMCSampleLength(val & 0xFF);
			}
			private void setRegister4014(byte val){
				// OAM DMA is performed in the PPU
				if(APU_DEV_MODE) System.err.println("OAM DMA not implemented by APU");
			}
			private void setRegister4015(byte val){
				setSoundChannels((val & 0x10)==0x10,(val & 0x8)==0x8, (val & 0x4)==0x4,(val & 0x2)==0x2,(val & 0x1)==0x1);
			}

			//    private void setRegister4016(byte val){
			//        if(APU_DEV_MODE) System.err.println("Not implemented");
			//    }
			private void setRegister4017(byte val){
				setAPUFrameCounter((val & 0x80)==0x80, (val&0x40)==0x40);
			}



			// TRIANGLE
			private void setTriangleSetup(boolean controlFlag, int counterReloadVal ) {
				// controlFlag is also the length counter halt flag
				volumeLengthCounterHalt[TRIANGLE] = controlFlag;
				triangleCounterReloadVal = counterReloadVal;
			}


			// NOISE
			private void setNoiseVolume(boolean lengthHalt,  int envelope) {
				volumeLengthCounterHalt[NOISE] = lengthHalt;
				volumeEnvelope[NOISE] = envelope;
			}
			private void setNoiseLow(boolean loopFlag,  int period) {
				if(APU_DEV_MODE) System.err.println("setNoiseLow Not implemented");
			}
			private void setNoiseHigh( int lengthCounter) {
				volumeLengthCounter[NOISE] = (flags[NOISE]) ? LENGTH_TABLE[lengthCounter] : 0;
			}

			// DMC
			private void setDMCFreqency(boolean irqEnabled, boolean loopEnabled, int dmcRateIndex){
				if(APU_DEV_MODE) System.err.println("setDMCFreqency Not implemented");
			}
			private void setDMCRaw(int dmcRaw){
				if(APU_DEV_MODE) System.err.println("setDMCRaw Not implemented");
			}
			private void setDMCSampleAddress(int dmcSampleAddress){
				if(APU_DEV_MODE) System.err.println("setDMCSampleAddress Not implemented");
			}
			private void setDMCSampleLength(int dmcSampleLength){
				if(APU_DEV_MODE) System.err.println("setDMCSampleLength Not implemented");
			}



			//   4017   http://wiki.nesdev.com/w/index.php/APU_Frame_Counter
			private void setAPUFrameCounter(boolean isFiveStepSequence, boolean inhibitInterrupt){
				_fourStepSequencerMode = !isFiveStepSequence;
				_inhibitInterrupt = inhibitInterrupt;
				
				//  blargg apu test 03.irg_flag
				// Writing $00 or $80 to $4017 doesn't affect flag
				// Writing $40 or $c0 to $4017 clears flag
				if(_inhibitInterrupt ) {
					// If set, the frame interrupt flag is cleared, otherwise it is unaffected.
					if(_frameInterruptFlag) {
				//		System.out.println("Clearing Interrupt 4017");
						_frameInterruptFlag = false;
					}
					
				} 
				/*
        // TO DO
        The sequencer is restarted at step 1 of the selected mode.
        If mode is 1 the sequencer is then clocked, causing the first step to be carried out immediately.
        Finally, the divider is reloaded, resulting in a 1/240 second delay before the sequencer is next clocked
				 */
				_clocks = 0;
				internal_step = 1;
				sequencer = (_fourStepSequencerMode ? SEQUENCER_4STEP[_mode][0] : SEQUENCER_5STEP[_mode][0]);

			}
			private void setSoundChannels(boolean dmcFlag, boolean noiseFlag,  boolean triangleFlag,  boolean square2Flag,  boolean square1Flag) {
				flags[DMC] = dmcFlag;
				flags[NOISE] = noiseFlag;
				if(!noiseFlag){
					volumeLengthCounter[NOISE] = 0;
				}
				flags[TRIANGLE] = triangleFlag;
				if(!triangleFlag){
					volumeLengthCounter[TRIANGLE] = 0;
				}
				flags[SQUARE2] = square2Flag;
				if(!square2Flag){
					volumeLengthCounter[SQUARE2] = 0;
				}
				flags[SQUARE1] = square1Flag;
				if(!square1Flag){
					volumeLengthCounter[SQUARE1] = 0;
				}
				if(! dmcFlag ){
					// TO DO
					//If clear, the DMC's bytes remaining is set to 0, otherwise the DMC sample is restarted only if the DMC's bytes remaining is 0.
				}
				// Side effects	 After the write, the DMC's interrupt flag is cleared
				_dmcInterruptFlag = false;
			}

			
			
			// for emulator debug viewing
			// APU clocks (should be about the same as CPU clocks
			public int getClocks(){
				return _clocks;
			}
			// Internal 40 counter that is used when populating data sample buffers
			public int getClockCount(){
				return _clockCount;
			}
			// Internal sequencer value.  Transitions to a new internal step when this counter hits zero
			public int getSequencerValue(){
				return sequencer;
			}
			public int getInternalStep() {
				return internal_step;
			}
			public boolean isNTSCMode() {
				return (_mode == NTSC_MODE);
			}
			public boolean isFourStepMode() {
				return _fourStepSequencerMode;			
			}
			public boolean isIRQInhibitMode() {
				return _inhibitInterrupt;			
			}
			
			public boolean getDMCInterruptFlagDirect() {
				return _dmcInterruptFlag;
			}
			
			public boolean getFrameIRQFlagDirect() {
				return _frameInterruptFlag;
			}
			
			public boolean hasSquare1Sound() {
				return SQUARE1_ENABLED && flags[SQUARE1];
			}
			public boolean hasSquare2Sound() {
				return SQUARE2_ENABLED && flags[SQUARE2];
			}
			public boolean hasTriangleSound() {
				return TRIANGLE_ENABLED && flags[TRIANGLE];
			}
			public boolean hasNoiseSound() {
				return NOISE_ENABLED && flags[NOISE];
			}

}