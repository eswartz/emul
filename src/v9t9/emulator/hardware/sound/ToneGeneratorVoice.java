/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.utils.Utils;

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
		hertz = SoundTMS9919.periodToHertz(period);

		if (hertz * 2 < 55930) {
			delta = hertz * 2;
		} else {
			delta = 0;
		}
		
		// keep waves in sync
		if (lastPeriod != period || (lastVolume == 0) != (getVolume() == 0))
			div = 0;
			
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
	public void generate(int soundClock, int[] soundGeneratorWorkBuffer,
			int from, int to, int active) {
		int sampleL, sampleR;
		sampleMagnitude = getCurrentMagnitude();
		int ratio = 128 + balance;
		sampleL = ((255 - ratio) * sampleMagnitude / active) >> 8;
		sampleR = (ratio * sampleMagnitude / active) >> 8;
		while (from < to) {
			if (updateMagnitude()) {
				ratio = 128 + balance;
				sampleL = ((255 - ratio) * sampleMagnitude / active) >> 8;
				sampleR = (ratio * sampleMagnitude / active) >> 8;
			}
			if (!out) {
				soundGeneratorWorkBuffer[from++] += sampleL;
				soundGeneratorWorkBuffer[from++] += sampleR;
			} else {
				soundGeneratorWorkBuffer[from++] -= sampleL;
				soundGeneratorWorkBuffer[from++] -= sampleR;
			}
			updateDivisor();
			
			// this loop usually executes only once
			while (div >= soundClock) {
				out = !out;
				div -= soundClock;
			}
		}
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
}