/*
  SoundVoice.java

  (c) 2009-2012 Edward Swartz

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

import javax.sound.sampled.AudioFormat;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.ISoundVoice;

/**
 * The base class for sounds.  This assumes 255 distinct volume settings
 * versus the TMS9919's 15, for possible future expansion.
 * @author ejs
 *
 */
public abstract class SoundVoice implements ISoundVoice
{
	protected static final int MAX_VOLUME = 255;
	
	/** volume, 0 == off, 0xff == loudest */
	private int	volume;			

	private final String name;
	
	/** how the left/right channels are balanced; -128 for all left to 127 for all right */
	protected byte balance;

	static final float[] volumeToMagnitude;
	
	static {
		volumeToMagnitude = new float[MAX_VOLUME + 1];
		double val = 1.0;
		
		// dB power falls by 10^(dB/20).
		// With 15 steps of 2 dB, the ratio is 10^-0.1
		// With 255 steps of 1/8 dB, the ratio is 10^-0.1
		double mult = Math.pow(10, -(30. / MAX_VOLUME / 20.));
		for (int x = MAX_VOLUME; x > 0; x--) {
			volumeToMagnitude[x] = (float) val;
			//System.out.println(val);
			val *= mult; 
		}
		volumeToMagnitude[0] = 0.0f;
	}
	public SoundVoice(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		if (volume == 0)
			return name + " [SILENT]";
		else
			return name + " volume="+volume;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundVoice#shouldDispose()
	 */
	@Override
	public boolean shouldDispose() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.sound.ISoundVoice#setFormat(javax.sound.sampled.AudioFormat)
	 */
	@Override
	public void setFormat(AudioFormat format) {
		
	}
	
	public void setBalance(byte balance) {
		this.balance = balance;
	}

	public abstract void setupVoice();
	public String getName() {
		return name;
	}
	public void saveState(ISettingSection section) {
		// derived
		//section.put("Volume", volume);
	}
	public void loadState(ISettingSection section) {
		// derived
		//volume = (byte) Utils.readSavedInt(section, "Volume");
	}
	/** Set volume in range 0 (silence) to {@value #MAX_VOLUME} (maximum) */
	public void setVolume(int volume) {
		this.volume = volume & MAX_VOLUME;
	}
	/** Set volume in range 0 (silence) to {@value #MAX_VOLUME} */
	public int getVolume() {
		return volume;
	}
	public float getCurrentMagnitude() {
		return volumeToMagnitude[volume];
	}
	public boolean isActive() {
		return volume != 0;
	}
}