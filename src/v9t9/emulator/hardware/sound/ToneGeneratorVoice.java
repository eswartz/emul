/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.emul.core.utils.PrefUtils;

public class ToneGeneratorVoice extends ClockedSoundVoice
{
	protected boolean out;
	public ToneGeneratorVoice(String name, int number) {
		super((name != null ? name + " " : "") + "Voice " + number);
	}
	
	protected void setupVoice()
	{
		byte lastVolume = getVolume();
		setVolume((byte) (0xf - getOperationAttenuation()));
		//int lastPeriod = period;
		period16 = getOperationPeriod() * soundClock;
		hertz = SoundTMS9919.period16ToHertz(period16);
		
		if (hertz * 2 < 55930) {
			incr = hertz * 2;
		} else {
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
	
	@Override
	public void generate(int[] soundGeneratorWorkBuffer, int from,
			int to) {
		int ratio = 128 + balance;
		while (from < to) {
			updateEffect();
			updateAccumulator();
			
			// this loop usually executes only once
			while (accum >= soundClock) {
				out = !out;
				accum -= soundClock;
			}
			
			int sampleMagnitude = getCurrentMagnitude();
			
			int sampleL = ((256 - ratio) * sampleMagnitude) >> 8;
			int sampleR = (ratio * sampleMagnitude) >> 8;
			
			soundGeneratorWorkBuffer[from++] += sampleL;
			soundGeneratorWorkBuffer[from++] += sampleR;
			
			/*
			if (!out) {
				soundGeneratorWorkBuffer[from++] += sampleL;
				soundGeneratorWorkBuffer[from++] += sampleR;
			} else {
				soundGeneratorWorkBuffer[from++] -= sampleL;
				soundGeneratorWorkBuffer[from++] -= sampleR;
			}
			*/
			
			
		}
	}
	
	@Override
	public int getCurrentMagnitude() {
		int mag = super.getCurrentMagnitude();
		return out ? mag : -mag;
	}
	
	@Override
	public void loadState(IDialogSettings settings) {
		super.loadState(settings);
		out = PrefUtils.readSavedBoolean(settings, "Out");
	}
	
	@Override
	public void saveState(IDialogSettings settings) {
		super.saveState(settings);
		settings.put("Out", out);
	}
}