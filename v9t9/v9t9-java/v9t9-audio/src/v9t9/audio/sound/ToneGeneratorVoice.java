/**
 * 
 */
package v9t9.audio.sound;

import ejs.base.settings.ISettingSection;
import static v9t9.common.sound.TMS9919Consts.*;

public class ToneGeneratorVoice extends ClockedSoundVoice
{
	protected boolean out;
	public ToneGeneratorVoice(String name, int number) {
		super((name != null ? name + " " : "") + "Voice " + number);
	}
	
	public void setupVoice()
	{
		byte lastVolume = getVolume();
		setVolume((byte) (0xf - getOperationAttenuation()));
		//int lastPeriod = period;
		period16 = getOperationPeriod() * soundClock;
		hertz = period16ToHertz(period16);
		
		if (hertz * 2 < 55930) {
			incr = hertz * 2;
		} else {
			// will alias, just silence
			incr = 0;
		}
		
		// reset clock on volume == 0 to avoid clicks
		if (/*lastPeriod != period ||*/ (lastVolume == 0) != (getVolume() == 0)) {
			if (period16 > 0) {
				clock %= period16;
				accum %= soundClock;
			}
			else {
				accum = 0;
				clock = 0;
			}
		}
			
		dump();
	}

	/*
	public int generate(int soundClock, int sample) {
		if (!out) {
			sample += sampleDelta;
		} else {
			sample -= sampleDelta;
		}
		updateDivisor();
		
		// this loop usually executes only once
		while (div >= soundClock) {
			out = !out;
			div -= soundClock;
		}	
		return sample;
	}*/
	
	public boolean generate(float[] soundGeneratorWorkBuffer, int from,
			int to) {
		int ratio = 128 + balance;
		boolean any = false;
		while (from < to) {
			updateEffect();
			updateAccumulator();
			
			// this loop usually executes only once
			while (accum >= soundClock) {
				out = !out;
				accum -= soundClock;
			}
			
			float sampleMagnitude = getCurrentMagnitude();
			if (sampleMagnitude != 0.0f) {
				any = true;
				float sampleL = ((256 - ratio) * sampleMagnitude) / 256.f;
				float sampleR = (ratio * sampleMagnitude) / 256.f;
				
				soundGeneratorWorkBuffer[from++] += sampleL;
				soundGeneratorWorkBuffer[from++] += sampleR;
			} else {
				from += 2;
			}
			
		}
		return any;
	}
	
	@Override
	public float getCurrentMagnitude() {
		float mag = super.getCurrentMagnitude();
		return out ? mag : -mag;
	}
	
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		super.loadState(settings);
		out = settings.getBoolean("Out");
	}
	
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		settings.put("Out", out);
	}
}