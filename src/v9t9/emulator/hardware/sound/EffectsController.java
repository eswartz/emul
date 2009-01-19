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
			sines[i] = (short) (Math.sin(i * 360 * Math.PI / 180. / sines.length) * 32768);
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
			voice.setVolume((byte) 0);
			index = -1;
			fromVolume = 0;
			targetVolume = 0;
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
		voice.div += voice.delta + vib;
		//while (voice.div < 0)
		//	voice.div += soundClock;
	}

	public int getCurrentSample() {
		return isEnvelopeSet() ? (index >= 0 ? calcSampleMagnitude() : 0)
				: volume;
	}
	
	private int calcSampleMagnitude() {
		int basic = volume >> (4 + (VOL_SHIFT - 23));
	
		
		if (tremoloAmount != 0) {
			if (volume > 0) {
				int sin = sines[tremoloClock * sines.length / SOUND_CLOCK];
				int delta = tremoloAmount * sin;
				
				// reduce magnitude by maximum tremolo
				basic += delta - tremoloAmount * 32768;
				//basic += sin * tremoloAmount ;
			}
		}
		
		if (false) {
			//basic = (int) ((long)basic * voice.div / (SOUND_CLOCK / 2));		// sawtooth
			
			// triangle
			if (voice instanceof ToneGeneratorVoice) {
				if (((ToneGeneratorVoice)voice).out)
					basic = (int) ((long)basic * voice.div / (SOUND_CLOCK / 2));
				else
					basic = (int) ((long)basic * (SOUND_CLOCK - voice.div) / (SOUND_CLOCK / 2));
			}
			
			
		} else {
			if (voice instanceof ToneGeneratorVoice) {
				if ((((ToneGeneratorVoice)voice).out))
					basic = -basic;
			}
		}
		
		
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
	
	
}
