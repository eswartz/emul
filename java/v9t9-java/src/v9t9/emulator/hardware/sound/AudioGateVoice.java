/**
 * 
 */
package v9t9.emulator.hardware.sound;

import org.ejs.coffee.core.properties.IPropertyStorage;

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

	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#setSoundClock(int)
	 */
	public void setSoundClock(int soundClock) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.emul.core.sound.ISoundVoice#reset()
	 */
	public void reset() {
		
	}
	
	/*
	public int generate(int soundClock, int sample) {
		sample += volumeToMagntiude24[0xf];
		return sample;
	}*/
	
	public void generate(float[] soundGeneratorWorkBuffer, int from,
			int to) {
		float sampleDelta = getCurrentMagnitude();
		while (from < to) {
			soundGeneratorWorkBuffer[from++] += sampleDelta;
		}
	}
	
	@Override
	public void loadState(IPropertyStorage settings) {
		if (settings == null) return;
		super.loadState(settings);
		setVolume((byte) (settings.getBoolean("State") ? 15 : 0));
	}
	
	@Override
	public void saveState(IPropertyStorage settings) {
		super.saveState(settings);
		settings.put("State", Boolean.toString(getVolume() != 0));
	}

	public void setState(boolean b) {
		state = b;
	}
	
}