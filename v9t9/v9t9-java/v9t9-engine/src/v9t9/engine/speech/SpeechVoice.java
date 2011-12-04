/**
 * 
 */
package v9t9.engine.speech;

import v9t9.base.sound.ISoundVoice;

public class SpeechVoice implements ISoundVoice {
	
	private short sample;

	public void setSoundClock(int soundClock) {
	}
	
	public void reset() {
		sample = 0;
	}
	
	public boolean isActive() {
		return true;
	}
	
	public boolean generate(float[] soundGeneratorWorkBuffer, int from, int to) {
		if (sample == 0)
			return false;
		float delta = sample / 32768.f;
		while (from < to)
			soundGeneratorWorkBuffer[from++] += delta;
		return true;
	}
	
	public void setSample(short sample) {
		this.sample = sample;
	}
}