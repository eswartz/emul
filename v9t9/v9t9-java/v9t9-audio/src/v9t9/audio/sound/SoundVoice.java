/**
 * 
 */
package v9t9.audio.sound;

import ejs.base.settings.ISettingSection;
import ejs.base.sound.ISoundVoice;

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
		for (int x = 0; x < MAX_VOLUME + 1; x++) {
			double y = (Math.exp((x/(double) MAX_VOLUME)*Math.log(1023))-1.0)/1023;
			//System.out.println(y);
			volumeToMagnitude[x] = (float) y;
		}
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