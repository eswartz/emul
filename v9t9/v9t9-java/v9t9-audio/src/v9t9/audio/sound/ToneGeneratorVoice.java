/*
  ToneGeneratorVoice.java

  (c) 2009-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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