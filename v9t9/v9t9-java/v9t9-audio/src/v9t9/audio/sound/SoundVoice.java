/*
  SoundVoice.java

  (c) 2009-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.sound;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.ISoundOutput;
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
	
	@Override
	public void setOutput(ISoundOutput output) {
		
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