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
	public class SoundVoice
	{
		int		voice;
		
		byte	operation[] = { 0, 0, 0 };	// operation bytes
		
		/** // volume, 0 == off, 0xf == loudest */
		byte	volume;			
		int		period, hertz;	// calculated from OPERATION_FREQUENCY_xxx
		
		// These following fields are used by the sound handler.
		int		div;

		public boolean alt;

		public int delta;

		public int sampleDelta;

		public int ns1;

		public int ns2;

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
		
		void cacheVoices()
		{
			if (voice != VOICE_NOISE) {
				volume = OPERATION_TO_VOLUME();
				period = OPERATION_TO_PERIOD();
				hertz = PERIOD_TO_HERTZ(period);
			} else {
				int period = OPERATION_TO_NOISE_PERIOD();
				//int type = OPERATION_TO_NOISE_TYPE();
		
				if (period != NOISE_PERIOD_VARIABLE) {
					volume = OPERATION_TO_VOLUME();
					period = noise_period[period];
					hertz = PERIOD_TO_HERTZ(period);
				} else {
					volume = OPERATION_TO_VOLUME();
					period = sound_voices[VOICE_TONE_2].period;
					hertz = sound_voices[VOICE_TONE_2].hertz;
				}
			}
		
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
		for (int i = 0; i < sound_voices.length; i++) {
			sound_voices[i] = new SoundVoice();
			sound_voices[i].voice = i;
		}
		sound_voices[VOICE_NOISE].ns1 = 0x55555555;
		sound_voices[VOICE_NOISE].ns2 = 0x55555555;
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#writeSound(byte)
	 */
	public void writeSound(byte val) {
		SoundVoice v;
		/*  handle command byte */
		//System.out.println("sound byte: " + Utils.toHex2(val));
		if ((val & 0x80) != 0) {
			int vn = OPERATION_TO_VOICE(val);
			cvoice = vn;
			v = sound_voices[vn];
			switch ((val & 0x70) >> 4) 
			{
			case 0:				/* T1 FRQ */
			case 2:				/* T2 FRQ */
			case 4:				/* T3 FRQ */
				v.operation[OPERATION_FREQUENCY_LO] = val;
				/*  not until second byte comes in
			   	v.cacheVoices();
				updateNoise();
				if (soundHandler != null)
					soundHandler.updateVoice(vn, SoundHandler.UPDATE_PITCH);
				*/
				break;
			case 1:				/* T1 ATT */
			case 3:				/* T2 ATT */
			case 5:				/* T3 ATT */
				v.operation[OPERATION_ATTENUATION] = val;
				v.cacheVoices();
				if (soundHandler != null)
					soundHandler.updateVoice(vn, SoundHandler.UPDATE_VOLUME,
							machine.getCpu().getCurrentCycleCount(),
							machine.getCpu().getCurrentTargetCycleCount());
				break;
			case 6:				/* noise ctl */
				v.operation[OPERATION_CONTROL] = val;
				updateNoise();
				break;
			case 7:				/* noise vol */
				v.operation[OPERATION_ATTENUATION] = val;
				v.cacheVoices();
				if (soundHandler != null)
					soundHandler.updateVoice(vn, SoundHandler.UPDATE_VOLUME,
							machine.getCpu().getCurrentCycleCount(),
							machine.getCpu().getCurrentTargetCycleCount());
				break;
			}
		}
		/*  second frequency byte */
		else {
			v = sound_voices[cvoice];
			v.operation[OPERATION_FREQUENCY_HI] = val;
			v.cacheVoices();
			updateNoise();
			if (soundHandler != null)
				soundHandler.updateVoice(cvoice, SoundHandler.UPDATE_NOISE,
						machine.getCpu().getCurrentCycleCount(),
						machine.getCpu().getCurrentTargetCycleCount());
		}
	}

	void
	updateNoise()
	{
		SoundVoice v = sound_voices[VOICE_NOISE];
		int period = v.OPERATION_TO_NOISE_PERIOD();

		switch (period) {
		case 0:
			v.hertz = 6991;
			break;
		case 1:
			v.hertz = 3496;
			break;
		case 2:
			v.hertz = 1748;
			break;
		case 3:
			v.hertz = sound_voices[VOICE_TONE_2].hertz;
			break;
		}
		if ((cvoice == VOICE_TONE_2 && period == NOISE_PERIOD_VARIABLE)
			 || cvoice == VOICE_NOISE)
		{
			if (soundHandler != null)
				soundHandler.updateVoice(VOICE_NOISE, SoundHandler.UPDATE_NOISE,
						machine.getCpu().getCurrentCycleCount(),
						machine.getCpu().getCurrentTargetCycleCount());
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
		}
		
	}
}
