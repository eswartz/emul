/*
  EffectsController.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import static v9t9.common.sound.TMS9919BConsts.*;

import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * @author ejs
 *
 */
public class EffectsController {
	private static final Logger logger = Logger.getLogger(EffectsController.class);
	
	private static final int VOL_SHIFT = 16;
	private static final int VOL_SCALE = (1 << VOL_SHIFT);
	private static final int VOL_MASK = (VOL_SCALE - 1);
	private static final int VOL_TO_SAMPLE = 4;
	//private static final int VOL_TO_SAMPLE_BIAS = ~(~0 << (VOL_TO_SAMPLE - 1));
	
	/** Milliseconds for ADHR values to take effect */
	final static int tickTimes[] = {
		0, 5, 10, 15,
		20, 30, 40, 50,
		60, 75, 100, 200,
		400, 800, 1600, 3200
	};
	
	final static short sines[] = new short[256];
	
	static {
		for (int i = 0; i < sines.length; i++) {
			sines[i] = (short) (Math.sin(i * 2 * Math.PI / sines.length) * 32767);
		}
	}
	
	final static int WAVELENGTH = 256;
	
	// http://www.music.mcgill.ca/~gary/307/week5/bandlimited.html
	// and Wikipedia
	// bandlimited sawtooth wave
	final static short sawtooth[] = new short[WAVELENGTH];

	// bandlimited triangle wave
	final static short triangle[] = new short[WAVELENGTH];
	
	// bandlimited square wave
	final static short square[] = new short[WAVELENGTH];

	static {
		for (int i = 0; i < WAVELENGTH; i++) {
			double phase = i * 2 * Math.PI / WAVELENGTH;
			double ssample = 0.0;
			for (int k = 1; k <= WAVELENGTH * 2; k++) {
				ssample += Math.sin(phase * k) / k;
			}
			sawtooth[i] = (short)((-2 * ssample / Math.PI) * 32767);
			
			double tsample = 0.0;
			for (int k = 1; k <= WAVELENGTH / 2; k+=2) {
				tsample += Math.sin(phase * k) * Math.pow(-1, (k-1)/2.0) / (k*k);
			}
			triangle[i] = (short)((8 * tsample / (Math.PI * Math.PI)) * 32767);
			
			double qsample = 0.0;
			for (int k = 1; k <= WAVELENGTH * 2; k++) {
				qsample += Math.sin(phase * (2 * k - 1)) / (2 * k - 1);
			}
			square[i] = (short)((4 * qsample / Math.PI) * 32767);
		}
	}
	
	private int[] adhr = new int[4];
	private int sustain;
	private int index;		// operation currently on, -1 means inactive

	private int clock;
	private int timeout;
	private final ClockedSoundVoice voice;
	private int voldelta;
	/** 24 bits */
	private int volume;

	private int sustainVolume;

	private int fullVolume;
	private int vibratoAmount;
	private int vibratoIncr;
	private int vibratoClock;
	private int tremoloAmount;
	private int tremoloIncr;
	private int tremoloClock;

	private int waveform;

	private boolean sweepStarting;
	private int sweepClocks;

	private int sweepRate;

	private int sweepCounter;

	private int sweepDelta;

	private int periodFixed16_16;

	private int sclock;

	public EffectsController(ClockedSoundVoice voice) {
		this.voice = voice;
		reset();
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

	/** Set voice sustain (volume) in range of 0 ... {@value SoundVoice#MAX_VOLUME} */
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
		volume = voice.getVolume() << VOL_SHIFT;
		vibratoClock = 0;
		tremoloClock = 0;
		sclock = (int) ((long) getSoundClock() * 65536 / 55930);
		if (isEnvelopeSet()) {
			index = OP_ATTACK;
			sustainVolume = (fullVolume * sustain + SoundVoice.MAX_VOLUME - 1) / SoundVoice.MAX_VOLUME;
			volume = 0;
			//voice.setVolume((byte) 0);
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
		//do {
			ticks1000 = getEnvelopePortionTime();
		//	if (ticks1000 == 0) {
		//		index++;
		//	} else {
		//		break;
		//	}
		//} while (index < OP_RELEASE);
		
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
		
		timeout = (int) ((long)ticks1000 * getSoundClock() / 1000); 
		clock = 0;
		volume = fromVolume;
		if (timeout > 0)
			voldelta = (targetVolume - fromVolume) / timeout;
		else
			voldelta = 0;
		
		if (logger.isDebugEnabled())
			logger.debug(voice.getName() + ": ADHR#"+ index+
				"; volume = " + (fromVolume>>VOL_SHIFT)+"; voldelta = " +((targetVolume - fromVolume)>>VOL_SHIFT)
				+" over " + ticks1000 + " ms");
	}

	/**
	 * @return
	 */
	private int getSoundClock() {
		return voice.soundClock;
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

	public boolean updateDivisor() {
		
		int vib = 0;
		if (vibratoAmount != 0) {
			vib = (int) ((long) sines[vibratoClock * sines.length / sclock] * vibratoAmount * 55930 / 16 / sclock);
			vibratoClock -= vibratoIncr;
			while (vibratoClock < 0)
				vibratoClock += sclock;
		}
		
		return voice.updateAccumulator(voice.incr + vib);
	}

	public int getCurrentSample() {
		int basic = volume >> VOL_TO_SAMPLE;
		if (basic == 0)
			return basic;
		
		if (tremoloAmount != 0) {
			if (volume > 0) {
				int sin = sines[tremoloClock * sines.length / sclock];
				int delta = tremoloAmount * sin;
				
				// reduce magnitude by maximum tremolo
				basic += delta - tremoloAmount * 32768;
				//basic += sin * tremoloAmount ;
			}
		}
		
		if (periodFixed16_16 > 0 && voice instanceof ToneGeneratorVoice) {
			int half = (int) (periodFixed16_16 / 2);
			//int quarter = (periodFixed16_16 / 4);
			
			int ang = (int) ((long)voice.accum * sines.length / periodFixed16_16);
			ang = clamp(ang, sines.length);
			int wang = (int) ((long)voice.accum * WAVELENGTH / periodFixed16_16);
			wang = clamp(wang, WAVELENGTH);
			
			switch (waveform) {
			case 0:
			default: {
				//if (toneGen.out)
				//	basic = -basic;
				basic = (int) (((long) basic * square[wang] / 32768));
				break;
			}
			case 1: {
				// sawtooth
				//basic = (int) (((long)basic * voice.clock ) / periodFixed16_16 - basic / 2 ) * 2;
				basic = (int) (((long) basic * sawtooth[wang] / 32768));
				break;
			}
			case 2: {
				// triangle
				/*
				if (voice.clock <= half)
					basic = (int) ((long)basic * voice.clock / quarter - basic);
				else
					basic = (int) ((long)basic * (periodFixed16_16 - voice.clock) / quarter - basic);
					*/
				basic = (int) (((long) basic * triangle[wang] / 32768));
				break;
			}
			case 3: {
				// sine
				if (ang < 0) ang += sines.length; else if (ang >= sines.length) ang -= sines.length;
				basic = (int) ((long) basic * sines[ang] / 32768);
				break;
			}
			case 4:
				// half saw
				if (voice.accum >= half)
					//basic = (int) (((long)basic * voice.clock ) / half - basic / 2 ) * 2;
					basic = (int) (((long) basic * sawtooth[wang] / 32768));
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
				//if (voice.clock >= quarter && voice.clock < quarter + half) {
				if (voice.accum >= half) {
					//if (voice.clock <= half)
					//	basic = (int) ((long)basic * voice.clock / quarter- basic);
					//else
					//	basic = (int) ((long)basic * (periodFixed16_16 - voice.clock) / quarter - basic);
					basic = (int) (((long) basic * triangle[wang] / 32768));
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

	/**
	 * @param ang
	 * @param length
	 * @return
	 */
	private  final int clamp(int v, int length) {
		if (v >= length)
			return v % length;
		if (v < 0)
			v = ((v % length) + length) % length;
		return v;
	}

	public synchronized void updateEffect() {
		if (isEnvelopeSet()) {
			if (index >= 0) {
				clock++;
				int oldVol = volume;
				volume += voldelta;
				if (logger.isDebugEnabled() && ((oldVol ^ volume) & ~VOL_MASK) != 0) { 
					logger.debug(voice.getName() + ": volume = " + (volume>>VOL_SHIFT));
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
				tremoloClock -= tremoloIncr;
				while (tremoloClock < 0)
					tremoloClock += sclock;
			}
		}
		if (sweepClocks-- > 0) {
			if (sweepStarting) {
				sweepCounter = voice.getPeriod() << 16;
				sweepStarting = false;
			}
			sweepCounter += sweepRate;
			voice.setPeriod(sweepCounter >> 16);
		}
	}
	
	public boolean isActive() {
		return isEnvelopeSet() && index >= 0;
		//return volume != 0;
	}

	/** Amount: 0-15, rate: 0-15 */
	public void setVibrato(int amount, int rate) {
		vibratoAmount = amount;
		vibratoIncr = rate;
		vibratoClock = 0;
	}

	/** Amount: 0-15, rate: 0-15 */
	public void setTremolo(int amount, int rate) {
		tremoloAmount = amount;
		tremoloIncr = rate;
		tremoloClock = 0;
		
	}

	public void setWaveform(int i) {
		this.waveform = i;
	}

	public void setSweepTarget(int targetPeriod) {
		sweepDelta = targetPeriod - voice.getPeriod();
	}

	/** Set time to complete in ms */
	public void setSweepTime(int ms) {
		sweepClocks = ms * sclock / 1000;
		if (sweepClocks != 0 && sweepDelta != 0) {
			sweepRate = (int) ((long) sweepDelta * 65536 / sweepClocks);
			sweepCounter = 0;
			sweepStarting = true;
		}
	}

	public void updateFrequency() {
		periodFixed16_16 = voice.period * 65536;
	}
}
