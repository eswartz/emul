/*
  ToneGeneratorVoice.java

  (c) 2009-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import ejs.base.settings.ISettingSection;

public class ToneGeneratorVoice extends ClockedSoundVoice
{
	protected boolean out;
	private float last;
	
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
				last = 0f;
			}
			
			float sampleMagnitude = getCurrentMagnitude();
			if (sampleMagnitude != 0.0f) {
				any = true;
				float sampleL = ((256 - ratio) * sampleMagnitude) / 128.f;
				float sampleR = (ratio * sampleMagnitude) / 128.f;
				
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
		if (!out)
			mag = -mag;
		mag = (mag + last) / 4;
		last = mag;
		return mag;
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