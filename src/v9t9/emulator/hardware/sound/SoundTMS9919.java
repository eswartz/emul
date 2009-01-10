/**
 * 
 */
package v9t9.emulator.hardware.sound;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.engine.SoundHandler;
import v9t9.utils.Utils;

/**
 * Controller for the TMS9919 sound chip
 * <p>
 * 3579545 Hz divided by 32 = 111860.78125 / 2 = 55930 Hz maximum frequency 
 * @author ejs
 *
 */
public class SoundTMS9919 implements SoundProvider {

	/* These are used as an index into the operation[] field */
	final static int OPERATION_FREQUENCY_LO = 0,		/* low 4 bits [1vv0yyyy] */
		OPERATION_CONTROL = 0,			/* for noise  [11100xyy] */
		OPERATION_FREQUENCY_HI = 1,		/* hi 6 bits  [00yyyyyy] */
		OPERATION_ATTENUATION = 2		/* low 4 bits [1vv1yyyy] */
	;

	public abstract class SoundVoice
	{
		/** volume, 0 == off, 0xf == loudest */
		private byte	volume;			

		private final String name;

		public SoundVoice(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			if (volume == 0)
				return name + " [SILENT]";
			else
				return name + " volume="+volume;
		}
		protected abstract void setupVoice();
		public abstract int generate(int soundClock, int sample, int sampleDelta);
		public String getName() {
			return name;
		}
		public void saveState(IDialogSettings section) {
			section.put("Volume", volume);
		}
		public void loadState(IDialogSettings section) {
			volume = (byte) Utils.readSavedInt(section, "Volume");
		}
		public void setVolume(byte volume) {
			this.volume = volume;
		}
		public byte getVolume() {
			return volume;
		}
	};

	/*	This struct is defined through writes
	to the sound port.  
	
	'period' and 'hertz' are related; 'period' is a number 0 through 0x3ff
	which was decoded from writes to the port and 'hertz' is the frequency
	of the tone that represents.
	
	'volume' is really attenuation -- 0 is loudest, 0xf is silent or off. 
	
	'stype' describes the characteristics of the noise channel (voices[3]);
	this is directly from the sound port (0-7).  stype >= 4 is white noise,
	else periodic.  The sound module defines 'period' and 'hertz' based on
	whether the stype represents a fixed-frequency sound or one that is
	derived from the frequency of channel 2.
	 */
	public abstract class ClockedSoundVoice extends SoundVoice
	{
		byte	operation[] = { 0, 0, 0 };	// operation bytes
		
		int		period, hertz;	// calculated from OPERATION_FREQUENCY_xxx
		
		int		div;			// divisor to add to the delta per clock
		int		delta;			// current accumulator, tracking the clock

		public ClockedSoundVoice(String name) {
			super(name);
		}
		
		@Override
		public String toString() {
			return super.toString() + "; hertz="+hertz;
		}
		protected int getOperationNoiseType() {
			return ( operation[OPERATION_CONTROL] & 0x4 );
		}

		protected int getOperationNoisePeriod()  {
			return ( operation[OPERATION_CONTROL] & 0x3 );
		}
		
		protected byte getOperationAttenuation() {
			return (byte) ( operation[OPERATION_ATTENUATION] & 0xf );
		}
		
		protected int getOperationPeriod() {
			return ( (operation[OPERATION_FREQUENCY_LO] & 0xf) |
			( (operation[OPERATION_FREQUENCY_HI] & 0x3f) << 4 ) );
		}
		
		protected void dump() {
			if (false) {
				System.out.println(MessageFormat.format(
						"voice_cache_values[{5}]: lo=>{0}, hi=>{1}, period=>{2}, hertz={3}, volume={4}",
					   Utils.toHex4(operation[OPERATION_FREQUENCY_LO]), 
					   Utils.toHex4(operation[OPERATION_FREQUENCY_HI]),
					   Utils.toHex4(period),
					   hertz,
					   getVolume(),
					   getName()));
			}
		}
		
		@Override
		public void loadState(IDialogSettings settings) {
			super.loadState(settings);
			operation[0] = (byte) Utils.readSavedInt(settings, "Op1");
			operation[1] = (byte) Utils.readSavedInt(settings, "Op2");
			operation[2] = (byte) Utils.readSavedInt(settings, "Op3");
			div = (byte) Utils.readSavedInt(settings, "Accumulator");
		}
		
		@Override
		public void saveState(IDialogSettings settings) {
			super.saveState(settings);
			settings.put("Op1", operation[0]);
			settings.put("Op2", operation[1]);
			settings.put("Op3", operation[2]);
			settings.put("Accumulator", div);
		}
	};

	public class ToneGeneratorVoice extends ClockedSoundVoice
	{
		private boolean out;
		public ToneGeneratorVoice(String name, int number) {
			super((name != null ? name + " " : "") + "Voice " + number);
		}
		protected void setupVoice()
		{
			byte lastVolume = getVolume();
			setVolume((byte) (0xf - getOperationAttenuation()));
			int lastPeriod = period;
			period = getOperationPeriod();
			hertz = periodToHertz(period);

			if (hertz * 2 < 55938) {
				delta = hertz * 2;
			} else {
				delta = 0;
			}
			
			// keep waves in sync
			if (lastPeriod != period || (lastVolume == 0) != (getVolume() == 0))
				div = 0;
				
			dump();
		}

		public int generate(int soundClock, int sample, int sampleDelta) {
			if (!out) {
				sample += sampleDelta;
			} else {
				sample -= sampleDelta;
			}
			div += delta;
			
			// this loop usually executes only once
			while (div >= soundClock) {
				out = !out;
				div -= soundClock;
			}	
			return sample;
		}
		
		@Override
		public void loadState(IDialogSettings settings) {
			super.loadState(settings);
			out = Utils.readSavedBoolean(settings, "Out");
		}
		
		@Override
		public void saveState(IDialogSettings settings) {
			super.saveState(settings);
			settings.put("Out", out);
		}
	};
	
	public class NoiseGeneratorVoice extends ClockedSoundVoice
	{
		boolean isWhite;
		int ns1;
		
		public NoiseGeneratorVoice(String name) {
			super((name != null ? name + " " : "") + "Noise");
		}
		protected void setupVoice()
		{
			int periodtype = getOperationNoisePeriod();
			boolean prevType = isWhite;
			boolean wasSilent = getVolume() == 0;
			isWhite = getOperationNoiseType() == NOISE_WHITE;
			
			
			setVolume((byte) (0xf - getOperationAttenuation()));
			if (periodtype != NOISE_PERIOD_VARIABLE) {
				period = noise_period[periodtype];
				hertz = periodToHertz(period);
			} else {
				period = ((ClockedSoundVoice) sound_voices[VOICE_TONE_2]).period;
				hertz = ((ClockedSoundVoice) sound_voices[VOICE_TONE_2]).hertz;
			}
		
			if (isWhite) {
				delta = hertz;
			} else {
				delta = hertz;
			}
			if (prevType != isWhite || (wasSilent && getVolume() != 0) || (isWhite && ns1 == 0)) {
				ns1 = (short) 0x8000;		// TODO: this should reset when the type of noise or sound changes only
				div = 0;
			}
			
			dump();
		}

		public int generate(int soundClock, int sample, int sampleDelta) {
			div += delta;
			if (isWhite) {
				
				// thanks to John Kortink (http://web.inter.nl.net/users/J.Kortink/home/articles/sn76489/)
				// for the exact algorithm here!
				while (div >= soundClock) {
					short rx = (short) ((ns1 ^ ((ns1 >>> 1) & 0x7fff) ));
					rx = (short) (0x4000 & (rx << 14));
					ns1 = (short) (rx | ((ns1 >>> 1) & 0x7fff) );
					div -= soundClock;
				}
				if ((ns1 & 1) != 0 ) {
					sample += sampleDelta;
				}
			} else {
				// For periodic noise, the generator is "on" 1/15 of the time.
				// The clock is the hertz / 15.
				
				// ns1 steps through 16 cycles, where 0x8000 through 0x2 are low, and 0x1 is high 
				if (ns1 <= 1) {
					sample -= sampleDelta * 2;
				}
				if (div >= soundClock) {
					if (ns1 == 1) {
						sample += sampleDelta * 4;
						ns1 = (short) 0x8000;
					}
					ns1 = (short) ((ns1 >>> 1) & 0x7fff);
					while (div >= soundClock) 
						div -= soundClock;
				}
			}
			return sample;
		}
		
		@Override
		public void saveState(IDialogSettings settings) {
			super.saveState(settings);
			settings.put("Shifter", ns1);
		}
		
		@Override
		public void loadState(IDialogSettings settings) {
			super.loadState(settings);
			ns1 = Utils.readSavedInt(settings, "Shifter");
		}
	};
	

	public class AudioGateVoice extends SoundVoice {

		private boolean state;

		public AudioGateVoice(String name) {
			super((name != null ? name + " " : "") + "Audio Gate");
		}
		
		@Override
		protected
		void setupVoice() {
			setVolume((byte) (state ? 15 : 0));
		}

		@Override
		public int generate(int soundClock, int sample, int sampleDelta) {
			sample += sampleDelta;
			return sample;
		}
		
		@Override
		public void loadState(IDialogSettings settings) {
			super.loadState(settings);
			setVolume((byte) (Utils.readSavedBoolean(settings, "State") ? 15 : 0));
		}
		
		@Override
		public void saveState(IDialogSettings settings) {
			super.saveState(settings);
			settings.put("State", getVolume() != 0);
		}

		public void setState(boolean b) {
			state = b;
		}
		
	}
	

	
	final public static int 
		VOICE_TONE_0 = 0, 
		VOICE_TONE_1 = 1, 
		VOICE_TONE_2 = 2, 
		VOICE_NOISE = 3,
		VOICE_AUDIO = 4;

	private SoundVoice sound_voices[] = new SoundVoice[5];

	private static int getOperationVoice(int op) {
		return ( ((op) & 0x60) >> 5);
	}

	/*
	 *	Masks for the OPERATION_CONTROL byte for VOICE_NOISE
	 */
	final static int NOISE_PERIODIC = 0,
		NOISE_WHITE = 0x4
	;
	
	final static int NOISE_PERIOD_FIXED_0 = 0,
		NOISE_PERIOD_FIXED_1 = 1,
		NOISE_PERIOD_FIXED_2 = 2,
		NOISE_PERIOD_VARIABLE = 3;
	;

	static final int  noise_period[] = 
	{
		16,
		32, 
		64, 
		0 						/* determined by VOICE_TONE_2 */
	};


	private static int periodToHertz(int p) {
		return ((p) > 1 ? (111860 / (p)) : (55930));
	}

	int	cvoice;

	private SoundHandler soundHandler;

	private final Machine machine;

	public SoundTMS9919(Machine machine, String name) {
		this.machine = machine;
		for (int i = 0; i < 3; i++) {
			sound_voices[i] = new ToneGeneratorVoice(name, i);
		}
		sound_voices[VOICE_NOISE] = new NoiseGeneratorVoice(name);
		sound_voices[VOICE_AUDIO] = new AudioGateVoice(name);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#writeSound(byte)
	 */
	public void writeSound(int addr, byte val) {
		ClockedSoundVoice v;
		/*  handle command byte */
		//System.out.println("sound byte: " + Utils.toHex2(val));
		int vn;
		if ((val & 0x80) != 0) {
			vn = getOperationVoice(val);
			cvoice = vn;
			v = (ClockedSoundVoice) sound_voices[vn];
			switch ((val & 0x70) >> 4) 
			{
			case 0:				/* T1 FRQ */
			case 2:				/* T2 FRQ */
			case 4:				/* T3 FRQ */
				v.operation[OPERATION_FREQUENCY_LO] = val;
				return;		// nothing changes til second byte
			case 1:				/* T1 ATT */
			case 3:				/* T2 ATT */
			case 5:				/* T3 ATT */
				v.operation[OPERATION_ATTENUATION] = val;
				break;
			case 6:				/* noise ctl */
				v.operation[OPERATION_CONTROL] = val;
				break;
			case 7:				/* noise vol */
				v.operation[OPERATION_ATTENUATION] = val;
				break;
			default:
				return;
			}
		}
		/*  second frequency byte */
		else {
			vn = cvoice;
			v = (ClockedSoundVoice) sound_voices[vn];
			v.operation[OPERATION_FREQUENCY_HI] = val;
		}
		
		v.setupVoice();
		updateNoise();
		if (soundHandler != null)
			soundHandler.updateVoice(machine.getCpu().getCurrentCycleCount(), machine.getCpu().getCurrentTargetCycleCount());
	}

	void
	updateNoise()
	{
		ClockedSoundVoice v = (ClockedSoundVoice) sound_voices[VOICE_NOISE];
		
		if ((cvoice == VOICE_TONE_2 && v.getOperationNoisePeriod() == NOISE_PERIOD_VARIABLE)
			 || cvoice == VOICE_NOISE)
		{
			v.setupVoice();
		}
	}


	public SoundHandler getSoundHandler() {
		return soundHandler;
	}


	public void setSoundHandler(SoundHandler soundHandler) {
		this.soundHandler = soundHandler;
	}


	public SoundVoice[] getSoundVoices() {
		return sound_voices;
	}
	
	public void saveState(IDialogSettings settings) {
		for (int vn = 0; vn < sound_voices.length; vn++) {
			SoundVoice v = sound_voices[vn];
			v.saveState(settings.addNewSection(v.getName()));
			
		}
	}
	public void loadState(IDialogSettings settings) {
		if (settings == null) return;
		for (int vn = 0; vn < sound_voices.length; vn++) {
			SoundVoice v = sound_voices[vn];
			v.loadState(settings.getSection(v.getName()));
			v.setupVoice();
		}
	}

	public void setAudioGate(int addr, boolean b) {
		AudioGateVoice v = (AudioGateVoice) sound_voices[VOICE_AUDIO];
		v.setState(b);
		v.setupVoice();
		if (soundHandler != null)
			soundHandler.updateVoice(machine.getCpu().getCurrentCycleCount(), machine.getCpu().getCurrentTargetCycleCount());

	}
	
	public boolean isStereo() {
		return false;
	}
}
