/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.eclipse.jface.dialogs.IDialogSettings;

import v9t9.utils.Utils;

public class AudioGateVoice extends SoundVoice {

	private boolean state;

	public AudioGateVoice(String name) {
		super((name != null ? name + " " : "") + "Audio Gate");
	}
	
	@Override
	protected
	void setupVoice() {
		setVolume((byte) (state ? 15 : 0));
	}

	/*
	public int generate(int soundClock, int sample) {
		sample += volumeToMagntiude24[0xf];
		return sample;
	}*/
	
	public void generate(int soundClock, int[] soundGeneratorWorkBuffer,
			int from, int to, int active) {
		int sampleDelta = volumeToMagntiude24[getVolume()] / active;
		while (from < to) {
			soundGeneratorWorkBuffer[from++] += sampleDelta;
		}
	}
	
	@Override
	public void loadState(IDialogSettings settings) {
		super.loadState(settings);
		setVolume((byte) (Utils.readSavedBoolean(settings, "State") ? 15 : 0));
	}
	
	@Override
	public void saveState(IDialogSettings settings) {
		super.saveState(settings);
		settings.put("State", getVolume() != 0);
	}

	public void setState(boolean b) {
		state = b;
	}
	
}