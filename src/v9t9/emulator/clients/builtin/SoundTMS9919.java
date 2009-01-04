/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.emulator.Machine;
import v9t9.engine.SoundHandler;
import v9t9.utils.Utils;

/**
 * Controller for the TMS9919 sound chip
 * <p>
 * 3579545 Hz divided by 32 = 111860.78125 / 2 = 55930 Hz maximum frequency 
 * @author ejs
 *
 */
public class SoundTMS9919 {

	/* These are used as an index into the operation[] field */
	final static int OPERATION_FREQUENCY_LO = 0,		/* low 4 bits [1vv0yyyy] */
		OPERATION_CONTROL = 0,			/* for noise  [11100xyy] */
		OPERATION_FREQUENCY_HI = 1,		/* hi 6 bits  [00yyyyyy] */
		OPERATION_ATTENUATION = 2		/* low 4 bits [1vv1yyyy] */
	;

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
	public abstract class SoundVoice
	{
		int		voice;
		
		byte	operation[] = { 0, 0, 0 };	// operation bytes
		
		/** // volume, 0 == off, 0xf == loudest */
		byte	volume;			
		int		period, hertz;	// calculated from OPERATION_FREQUENCY_xxx
		
		int		div;			// divisor to add to the delta per clock
		int		delta;			// current accumulator, tracking the clock
		boolean	out;			// current output of generator (on or off)

		@Override
		public String toString() {
			if (volume == 0)
				return "[SILENT]";
			else
				return "hertz="+hertz+"; volume="+volume;
		}
		int OPERATION_TO_NOISE_TYPE() {
			return ( operation[OPERATION_CONTROL] & 0x4 );
		}

		int OPERATION_TO_NOISE_PERIOD()  {
			return ( operation[OPERATION_CONTROL] & 0x3 );
		}
		
		byte OPERATION_TO_ATTENUATION() {
			return (byte) ( operation[OPERATION_ATTENUATION] & 0xf );
		}

		byte OPERATION_TO_VOLUME() {
			return (byte) ( 0xf - OPERATION_TO_ATTENUATION() );
		}
		
		int OPERATION_TO_PERIOD() {
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
					   volume,
					   voice));
			}
		}
		abstract void cacheVoices();
		abstract int generate(int soundClock, int sample, int sampleDelta);
	};

	public class ToneGeneratorVoice extends SoundVoice
	{
		void cacheVoices()
		{
				volume = OPERATION_TO_VOLUME();
				period = OPERATION_TO_PERIOD();
				hertz = PERIOD_TO_HERTZ(period);

				if (hertz * 2 < 55938) {
					delta = hertz * 2;
				} else {
					delta = 0;
				}
				
			dump();
		}

		int generate(int soundClock, int sample, int sampleDelta) {
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
	};
	
	public class NoiseGeneratorVoice extends SoundVoice
	{
		boolean isWhite;
		int ns1;
		void cacheVoices()
		{
			int periodtype = OPERATION_TO_NOISE_PERIOD();
			boolean prevType = isWhite;
			boolean wasSilent = volume == 0;
			isWhite = OPERATION_TO_NOISE_TYPE() == NOISE_WHITE;
			
			
			if (periodtype != NOISE_PERIOD_VARIABLE) {
				volume = OPERATION_TO_VOLUME();
				period = noise_period[periodtype];
				hertz = PERIOD_TO_HERTZ(period);
			} else {
				volume = OPERATION_TO_VOLUME();
				period = sound_voices[VOICE_TONE_2].period;
				hertz = sound_voices[VOICE_TONE_2].hertz;
			}
		
			if (isWhite) {
				delta = hertz;
			} else {
				delta = hertz;
			}
			if (prevType != isWhite || (wasSilent && volume != 0) || (isWhite && ns1 == 0)) {
				ns1 = (short) 0x8000;		// TODO: this should reset when the type of noise or sound changes only
				div = 0;
			}
			out = true;
			
			dump();
		}

		int generate(int soundClock, int sample, int sampleDelta) {
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
					//sample += sampleDelta;
					while (div >= soundClock) 
						div -= soundClock;
				} else {
					//sample -= sampleDelta;
				}
			}
			return sample;
		}
	};
	

	

	
	final public static int 
		VOICE_TONE_0 = 0, 
		VOICE_TONE_1 = 1, 
		VOICE_TONE_2 = 2, 
		VOICE_NOISE = 3;

	private SoundVoice sound_voices[] = new SoundVoice[4];

	int OPERATION_TO_VOICE(int op) {
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


	int PERIOD_TO_HERTZ(int p) {
		return ((p) > 1 ? (111860 / (p)) : (55930));
	}

	int	cvoice;

	private SoundHandler soundHandler;

	private final Machine machine;
	
	public SoundTMS9919(Machine machine) {
		this.machine = machine;
		for (int i = 0; i < 3; i++) {
			sound_voices[i] = new ToneGeneratorVoice();
			sound_voices[i].voice = i;
		}
		sound_voices[VOICE_NOISE] = new NoiseGeneratorVoice();
		sound_voices[VOICE_NOISE].voice = VOICE_NOISE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#writeSound(byte)
	 */
	public void writeSound(byte val) {
		SoundVoice v;
		/*  handle command byte */
		//System.out.println("sound byte: " + Utils.toHex2(val));
		int vn;
		if ((val & 0x80) != 0) {
			vn = OPERATION_TO_VOICE(val);
			cvoice = vn;
			v = sound_voices[vn];
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
			v = sound_voices[vn];
			v.operation[OPERATION_FREQUENCY_HI] = val;
		}
		
		v.cacheVoices();
		updateNoise();
		if (soundHandler != null)
			soundHandler.updateVoice(machine.getCpu().getCurrentCycleCount(), machine.getCpu().getCurrentTargetCycleCount());
	}

	void
	updateNoise()
	{
		SoundVoice v = sound_voices[VOICE_NOISE];
		
		if ((cvoice == VOICE_TONE_2 && v.OPERATION_TO_NOISE_PERIOD() == NOISE_PERIOD_VARIABLE)
			 || cvoice == VOICE_NOISE)
		{
			v.cacheVoices();
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
		for (int vn = 0; vn < 4; vn++) {
			SoundVoice v = sound_voices[vn];
			for (int idx = 0; idx < 3; idx++) {
				settings.put("" + vn + "." + idx, v.operation[idx]);
			}
		}
	}
	public void loadState(IDialogSettings settings) {
		if (settings == null) return;
		for (int vn = 0; vn < 4; vn++) {
			SoundVoice v = sound_voices[vn];
			for (int idx = 0; idx < 3; idx++) {
				v.operation[idx] = (byte) Utils.readSavedInt(settings, "" + vn + "." + idx);

			}
			v.cacheVoices();
		}
	}
}
