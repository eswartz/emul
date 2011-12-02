/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.sound.ISoundVoice;

public abstract class SoundVoice implements ISoundVoice, IPersistable
{
	/** volume, 0 == off, 0xf == loudest */
	private byte	volume;			

	private final String name;
	
	/** how the left/right channels are balanced; -128 for all left to 127 for all right */
	protected byte balance;

	static public final int volumeToMagntiude24[] = {
		0x00000000,
		0x0000a396,
		0x00017b71,
		0x00029844,
		0x00041018,
		0x0005ffff,
		0x00088e5a,
		0x000bedc6,
		0x00106111,
		0x00164060,
		0x001dffff,
		0x00283968,
		0x0035b719,
		0x00478446,
		0x005f0180,
		0x007fffff,

		};
	
	public static void main(String[] args) {
		for (int x = 0; x < 16; x++) {
			double y = (Math.exp((x/15.)*Math.log(64))-1.0)/64;
			System.out.printf("\t0x%08x,\n", (int)(y * 0x800000));
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

	protected abstract void setupVoice();
	//public abstract int generate(int soundClock, int sample);
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
	public void setVolume(byte volume) {
		this.volume = volume;
	}
	public float getCurrentMagnitude() {
		return (float) volumeToMagntiude24[volume & 0xf] / 0x007FFFFF;
	}
	public byte getVolume() {
		return volume;
	}
	public boolean isActive() {
		return volume > 0;
	}
}