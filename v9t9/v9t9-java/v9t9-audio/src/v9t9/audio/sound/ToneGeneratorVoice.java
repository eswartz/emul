/**
 * 
 */
package v9t9.audio.sound;

import ejs.base.settings.ISettingSection;

public class ToneGeneratorVoice extends ClockedSoundVoice
{
	protected boolean out;
	public ToneGeneratorVoice(String name, int number) {
		super((name != null ? name + " " : "") + "Voice " + number);
	}
	
	public boolean generate(float[] soundGeneratorWorkBuffer, int from,
			int to) {
		int ratio = 128 + balance;
		boolean any = false;
		while (from < to) {
			updateEffect();
			
			if (updateAccumulator()) {
				out = !out;
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