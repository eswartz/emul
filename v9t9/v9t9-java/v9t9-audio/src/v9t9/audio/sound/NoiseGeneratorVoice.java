/*
  NoiseGeneratorVoice.java

  (c) 2009-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import v9t9.common.sound.TMS9919Consts;
import ejs.base.settings.ISettingSection;
import ejs.base.sound.SoundFormat;

/**
 * Help from http://www.smspower.org/Development/SN76489
 * and thanks to John Kortink (http://web.inter.nl.net/users/J.Kortink/home/articles/sn76489/)
 * for the exact white-noise algorithm!
 *
 * @author ejs
 *
 */
public class NoiseGeneratorVoice extends ClockedSoundVoice
{
	private boolean isWhite;
	private volatile int ns1;
	private int control;
	
	public NoiseGeneratorVoice(String name) {
		super((name != null ? name + " " : "") + "Noise");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.audio.sound.ClockedSoundVoice#setFormat(javax.sound.sampled.AudioFormat)
	 */
	@Override
	public void setFormat(SoundFormat format) {
		// Noise works on effectively double the clock of tones,
		// because tones split the clock for the up and down parts of
		// one cycle, whereas noise works on every cycle.
		SoundFormat dblFormat = new SoundFormat(
				format.getSampleRate() * 2,
				format.getChannels(),
				format.getType());
		
		super.setFormat(dblFormat);
	}
	
	/**
	 * @param control the noiseControl to set
	 */
	public void setNoiseControl(int control) {
		
		int oldControl = control;
		
		this.control = control;
		int ps = control & TMS9919Consts.NOISE_PERIOD_MASK;
		switch (ps) {
		case 0:
		case 1:
		case 2:
			setPeriod(TMS9919Consts.NOISE_DIVISORS[ps]);
			break;
		default:
			// will be updated next
			setPeriod(0);
			break;
		}
		
		this.isWhite = (control & TMS9919Consts.NOISE_FEEDBACK_MASK) != 0;
		
		if (oldControl != control || ns1 == 0) {
			ns1 = (short) 0x8000;
			accum = 0;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.audio.sound.ClockedSoundVoice#setPeriod(int)
	 */
	@Override
	public void setPeriod(int period) {
		// noise operates at half the rate
		super.setPeriod(period << 1);
	}
	public boolean generate(float[] soundGeneratorWorkBuffer, int from,
			int to) {
		int ratio = 128 + balance;
		boolean any = false;
		while (from < to) {
			boolean toggled = updateAccumulator();
			updateEffect();
			
			float sampleMagnitude;
			float sampleL = 0;
			float sampleR = 0;
			
			sampleMagnitude = getCurrentMagnitude();
			if (sampleMagnitude != 0.0f) {
				any = true;
				sampleL = ((256 - ratio) * sampleMagnitude) / 128.f;
				sampleR = (ratio * sampleMagnitude) / 128.f;
				
				if (isWhite) {
					if ((ns1 & 1) != 0 ) {
						soundGeneratorWorkBuffer[from] += sampleL;
						soundGeneratorWorkBuffer[from+1] += sampleR;
					}
					
					if (toggled) {
						short rx = (short) ((ns1 ^ ((ns1 >>> 1) & 0x7fff) ));
						rx = (short) (0x4000 & (rx << 14));
						ns1 = (short) (rx | ((ns1 >>> 1) & 0x7fff) );
					}
				} else {
					// For periodic noise, the generator is "on" 1/15 of the time.
					// The clock is the hertz / 15.
					
					if (ns1 <= 1) {
						soundGeneratorWorkBuffer[from] -= sampleL * 2;
						soundGeneratorWorkBuffer[from+1] -= sampleR * 2;
						ns1 = (short) 0x8000;
					}
					if (toggled) {
						ns1 = (short) ((ns1 >>> 1) & 0x7fff);
					}
				}
			}
			
			from += 2;
		}
		return any;
	}
	
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("Shifter", ns1);
	}
	
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		super.loadState(settings);
		ns1 = settings.getInt("Shifter");
	}

	/**
	 * @return
	 */
	public int getNoiseControl() {
		return control;
	}
}