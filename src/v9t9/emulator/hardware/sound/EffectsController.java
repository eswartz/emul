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
	private int[] adhr = new int[4];
	private int sustain;
	private int index;		// operation currently on, -1 means inactive

	private int clock;
	private int timeout;
	private final ClockedSoundVoice voice;
	private int voldelta;
	private int volume;
	private boolean isReleased;

	private int sustainVolume;

	private byte fullVolume;
	public EffectsController(ClockedSoundVoice voice) {
		this.voice = voice;
		index = -1;
	}

	/**
	 * Reset all effects
	 */
	public void reset() {
		Arrays.fill(adhr, 0);
		index = -1;
		isReleased = true;
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
	public void startEnvelope() {
		if (isEnvelopeSet()) {
			isReleased = false;
			index = OP_ATTACK;
			fullVolume = voice.getVolume();
			sustainVolume = fullVolume * sustain / 16;
			nextADHR();
		} else {
			index = -1;
		}
	}
	
	/**
	 * Call when a note's attenuation is set to 14. 
	 */
	public void startRelease() {
		if (isEnvelopeSet()) {
			isReleased = true;
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
				if (index == OP_RELEASE + 1)
					break;
			} else {
				break;
			}
		} while (true);
		
		int fromVolume = 0, targetVolume = 0;
		
		switch (index) {
		case -1:
			return;
		case OP_ATTACK:
			// attack: 0 to full volume
			fromVolume = 0;
			targetVolume = fullVolume;
			break;
		case OP_DECAY:
			// decay: current volume to sustain
			fromVolume = fullVolume;
			targetVolume = sustainVolume;
			break;
		case OP_HOLD:
			// hold: remain at sustain
			fromVolume = sustainVolume;
			targetVolume = sustainVolume;
			break;
		case OP_RELEASE:
			// release:  from current volume (since this can happen
			// at any time during a note) to silence
			fromVolume = volume / VOL_SCALE;
			targetVolume = 0;
			break;
		case OP_RELEASE + 1:
			// done
			voice.setVolume((byte) 0);
			index = -1;
			fromVolume = 0;
			targetVolume = 0;
			return;
		}
		
		timeout = (int) ((long)ticks1000 * 55930 / 1000); 
		clock = 0;
		volume = fromVolume * VOL_SCALE;
		voldelta = (int) ((long) (targetVolume - fromVolume) * VOL_SCALE / timeout);
		
		if (false)
			System.out.println(voice.getName() + ": ADHR#"+ index+
				"; volume = " + fromVolume+"; voldelta = " +(targetVolume - fromVolume)
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
		voice.div += voice.delta;
	}

	public int getCurrentSample() {
		return sustain == 0 ? voice.getSampleSize(voice.getVolume())
				: (index >= 0 ? calcSampleMagnitude() : 0); 
	}
	public synchronized boolean updateSample() {
		boolean changed = false;
		if (isEnvelopeSet()) {
			if (index >= 0) {
				clock++;
				int oldVol = volume;
				volume += voldelta;
				if (((oldVol ^ volume) & ~(VOL_MASK >> 4)) != 0) { 
					changed = true;
					voice.sampleMagnitude = calcSampleMagnitude();
					if (false) {
						if (((oldVol ^ volume) & ~VOL_MASK) != 0) { 
							System.out.println(voice.getName() + ": volume = " + (volume>>VOL_SHIFT));
						}
					}
				}
				if (clock >= timeout) {
					index++;
					nextADHR();
					changed = true;
				}
			} else {
				if (voice.sampleMagnitude != 0) {
					voice.sampleMagnitude = 0;
					changed = true;
				}
			}
		}
		return changed;
	}

	private int calcSampleMagnitude() {
		if (VOL_SHIFT < 24)
			return volume << (23 - VOL_SHIFT - 4);	// 0xFffff -> 0x7Ffff8
		else
			return volume >> (4 + (VOL_SHIFT - 23));
	}
	
	
}
