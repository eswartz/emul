/**
 * 
 */
package v9t9.emulator.hardware.sound;

import java.util.Arrays;

/**
 * @author ejs
 *
 */
public class EffectsController {
	public boolean DUMP = false;

	private static final int SOUND_CLOCK = 55930;
	
	private static final int VOL_SHIFT = 24;
	private static final int VOL_SCALE = (1 << VOL_SHIFT);
	private static final int VOL_MASK = (VOL_SCALE - 1);
	private static final int VOL_TO_SAMPLE = (VOL_SHIFT - 23) + 4;
	//private static final int VOL_TO_SAMPLE_BIAS = ~(~0 << (VOL_TO_SAMPLE - 1));
	
	final static int OP_ATTACK = 0,
		OP_DECAY = 1,
		OP_HOLD = 2,
		OP_RELEASE = 3;
	final static int tickTimes[] = {
		0, 5, 10, 15,
		20, 25, 30, 35,
		50, 100, 200, 400,
		800, 1600, 3200, 6400
	};
	
	final static short sines[] = new short[256];
	
	static {
		for (int i = 0; i < sines.length; i++) {
			sines[i] = (short) (Math.sin(i * 2 * Math.PI / sines.length) * 32767);
		}
	}
	
	private int[] adhr = new int[4];
	private int sustain;
	private int index;		// operation currently on, -1 means inactive

	private int clock;
	private int timeout;
	private final ClockedSoundVoice voice;
	private int voldelta;
	private int volume;

	private int sustainVolume;

	private byte fullVolume;
	private int vibratoAmount;
	private int vibratoIncr;
	private int vibratoClock;
	private int tremoloAmount;
	private int tremoloIncr;
	private int tremoloClock;

	private int waveform;
	
	public EffectsController(ClockedSoundVoice voice) {
		this.voice = voice;
		index = -1;
	}

	/**
	 * Reset all effects
	 */
	public void reset() {
		Arrays.fill(adhr, 0);
		sustain = 0;
		vibratoAmount = 0;
		tremoloAmount = 0;
		waveform = 0;
		volume = voice.getVolume() << VOL_SHIFT;
		index = -1;
	}
	
	public int getADSR(int op) {
		return adhr[op];
	}

	public void setADSR(int op, int val) {
		adhr[op] = val;
		index = -1;
	}

	public void setSustain(int sustain) {
		this.sustain = sustain; 
	}

	public int getSustain() {
		return sustain;
	}
	
	/**
	 * Call when a voice is silenced.
	 */
	public void stopEnvelope() {
		index = -1;
		timeout = 0; 
		clock = 0;
		volume = voice.getVolume() << VOL_SHIFT;
		voldelta = 0;
	}
	
	/**
	 * Call when a voice's volume is set to non-zero.
	 */
	public void updateVoice() {
		fullVolume = voice.getVolume();
		vibratoClock = 0;
		tremoloClock = 0;
		
		if (isEnvelopeSet()) {
			index = OP_ATTACK;
			sustainVolume = (fullVolume * sustain + 15) / 16;
			volume = 0;
			voice.setVolume((byte) 0);
			nextADHR();
		} else {
			volume = fullVolume << VOL_SHIFT;
			index = -1;
		}
	}
	
	/**
	 * Call when a note's attenuation is set to 14. 
	 */
	public void startRelease() {
		if (isEnvelopeSet()) {
			index = OP_RELEASE;
			nextADHR();
		}
	}

	private boolean isEnvelopeSet() {
		return sustain != 0;
	}

	private synchronized void nextADHR() {
		int ticks1000;
		do {
			ticks1000 = getEnvelopePortionTime();
			if (ticks1000 == 0) {
				index++;
			} else {
				break;
			}
		} while (index < OP_RELEASE);
		
		int fromVolume = 0, targetVolume = 0;
		
		if (index > OP_RELEASE) {
			index = -1;
		}
		
		switch (index) {
		case -1:
			// done
			stopEnvelope();
			return;
		case OP_ATTACK:
			// attack: 0 to full volume
			fromVolume = 0 * VOL_SCALE;
			targetVolume = fullVolume * VOL_SCALE;
			break;
		case OP_DECAY:
			// decay: current volume to sustain
			fromVolume = fullVolume * VOL_SCALE;
			targetVolume = sustainVolume * VOL_SCALE;
			break;
		case OP_HOLD:
			// hold: remain at sustain
			fromVolume = sustainVolume * VOL_SCALE;
			targetVolume = sustainVolume * VOL_SCALE;
			break;
		case OP_RELEASE:
			// release:  from current volume (since this can happen
			// at any time during a note) to silence
			fromVolume = volume;
			targetVolume = 0;
			break;
		}
		
		timeout = (int) ((long)ticks1000 * SOUND_CLOCK / 1000); 
		clock = 0;
		volume = fromVolume;
		voldelta = (targetVolume - fromVolume) / timeout;
		
		if (DUMP)
			System.out.println(voice.getName() + ": ADHR#"+ index+
				"; volume = " + (fromVolume>>VOL_SHIFT)+"; voldelta = " +((targetVolume - fromVolume)>>VOL_SHIFT)
				+" over " + ticks1000 + " ms");
	}

	private int getEnvelopePortionTime() {
		int ticks1000 = tickTimes[adhr[index & 0x3]];
		return ticks1000;
	}

	/**
	 * Get the CPU 1/100 s tick
	 */
	public void tick() {
	}

	public void updateDivisor() {
		int vib = 0;
		if (vibratoAmount != 0) {
			vib = sines[vibratoClock * sines.length / SOUND_CLOCK / 16] * vibratoAmount / 65536;
			vibratoClock += vibratoIncr;
			while (vibratoClock >= SOUND_CLOCK * 16)
				vibratoClock -= SOUND_CLOCK * 16;
		}
		voice.accum += voice.incr + vib;
		
		if (voice.period > 0)
			voice.clock = (voice.clock + 1) % voice.period;
		else
			voice.clock = 0;
		//while (voice.div < 0)
		//	voice.div += soundClock;
	}

	public int getCurrentSample() {
		int basic = volume >> VOL_TO_SAMPLE;
	
		
		if (tremoloAmount != 0) {
			if (volume > 0) {
				int sin = sines[tremoloClock * sines.length / SOUND_CLOCK];
				int delta = tremoloAmount * sin;
				
				// reduce magnitude by maximum tremolo
				basic += delta - tremoloAmount * 32768;
				//basic += sin * tremoloAmount ;
			}
		}
		
		if (voice.period > 0 && voice instanceof ToneGeneratorVoice) {
			ToneGeneratorVoice toneGen = (ToneGeneratorVoice) voice;
			//int half = SOUND_CLOCK / 2;
			//int quarter = SOUND_CLOCK / 4;
			
			int half = (voice.period / 2);
			int quarter = voice.period / 4;
			
			int ang = voice.clock * sines.length / voice.period;
			if (ang < 0) ang += sines.length; else if (ang >= sines.length) ang -= sines.length;
			
			switch (waveform) {
			case 0:
			default:
				if (toneGen.out)
					basic = -basic;
				break;
			case 1: {
				// sawtooth
				basic = (int) (((long)basic * voice.clock ) / voice.period - basic / 2 ) * 2;		
				break;
			}
			case 2:
				// triangle
				if (voice.clock <= half)
					basic = (int) ((long)basic * voice.clock / quarter - basic);
				else
					basic = (int) ((long)basic * (voice.period - voice.clock) / quarter - basic);
				break;
			case 3: {
				// sine
				if (ang < 0) ang += sines.length; else if (ang >= sines.length) ang -= sines.length;
				basic = (int) ((long) basic * sines[ang] / 32768);
				break;
			}
			case 4:
				// half saw
				if (voice.clock < half)
					basic = (int) (((long)basic * voice.clock ) / half - basic / 2 ) * 2;	
				else
					basic = 0;
				break;
			case 5: {
				// half sine
				int sin = sines[ang];
				if (sin > 0)
					basic = (int) ((long) basic * sin / 32768);
				else
					basic = 0;
				break;
			}
			case 6:
				// half triangle
				if (voice.clock >= quarter && voice.clock < quarter + half) {
					if (voice.clock <= half)
						basic = (int) ((long)basic * voice.clock / quarter- basic);
					else
						basic = (int) ((long)basic * (voice.period - voice.clock) / quarter - basic);
				}
				else
					basic = 0;
				break;
			case 7:
				// tangent
				int cang = (ang + sines.length / 4) % sines.length;
				int sin = sines[ang];
				int cos = sines[cang];
				if (cos != 0) 
					basic = (int) ((long) basic * sin / cos / 2);
				break;
			}
			
		}
		
		//System.out.println(voice.getName() + ": " + basic);
		return basic;
	}

	public synchronized void updateEffect() {
		if (isEnvelopeSet()) {
			if (index >= 0) {
				clock++;
				int oldVol = volume;
				volume += voldelta;
				if (DUMP && ((oldVol ^ volume) & ~VOL_MASK) != 0) { 
					System.out.println(voice.getName() + ": volume = " + (volume>>VOL_SHIFT));
				}
				if (clock >= timeout) {
					index++;
					nextADHR();
				}
			} else {
				volume = 0;
			}
		}
		if (tremoloAmount != 0) {
			if (volume > 0) {
				tremoloClock += tremoloIncr;
				while (tremoloClock >= SOUND_CLOCK)
					tremoloClock -= SOUND_CLOCK;
			}
		}
	}
	
	public boolean isActive() {
		return isEnvelopeSet() && index >= 0;
	}

	public void setVibrato(int amount, int rate) {
		vibratoAmount = amount * 8;
		vibratoIncr = rate * 16;
		vibratoClock = 0;
	}

	public void setTremolo(int amount, int rate) {
		tremoloAmount = amount;
		tremoloIncr = rate;
		tremoloClock = 0;
		
	}

	public void setWaveform(int i) {
		this.waveform = i;
	}
	
	
}
