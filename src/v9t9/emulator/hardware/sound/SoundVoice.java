/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.eclipse.jface.dialogs.IDialogSettings;

public abstract class SoundVoice
{
	/** volume, 0 == off, 0xf == loudest */
	private byte	volume;			

	private final String name;
	
	/** how the left/right channels are balanced; -128 for all left to 127 for all right */
	protected byte balance;

	static public final int volumeToMagntiude24[] = {
		0x00000000,
		0x0009A9C5, 0x000BAC10, 0x000E1945, 0x001107A1, 0x001491FC,
		0x0018D8C4, 0x001E0327, 0x00244075, 0x002BC9D6, 0x0034E454,
		0x003FE353, 0x004D2B8C, 0x005D36AB, 0x007097A5, 0x007FFFFF };
	
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

	protected abstract void setupVoice();
	//public abstract int generate(int soundClock, int sample);
	public String getName() {
		return name;
	}
	public void saveState(IDialogSettings section) {
		// derived
		//section.put("Volume", volume);
	}
	public void loadState(IDialogSettings section) {
		// derived
		//volume = (byte) Utils.readSavedInt(section, "Volume");
	}
	public void setVolume(byte volume) {
		this.volume = volume;
	}
	public int getSampleSize(int volume) {
		return volumeToMagntiude24[volume & 0xf];
	}
	public int getCurrentMagnitude() {
		return getSampleSize(volume);
	}
	public byte getVolume() {
		return volume;
	}
	public abstract void generate(int[] soundGeneratorWorkBuffer, int from,
			int to);
	public boolean isActive() {
		return volume > 0;
	}
}