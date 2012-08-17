/**
 * 
 */
package v9t9.audio.speech;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;

import v9t9.common.speech.ISpeechSoundVoice;

public class SpeechVoice implements ISpeechSoundVoice {
	
	/**
	 * 
	 */
	private static final boolean SMOOTH = true;
	
	private Queue<Short> samples = new LinkedBlockingQueue<Short>();

	private short lastSample;

	private int soundClock;

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
		this.soundClock = (int) format.getFrameRate();
	}
	
	public void setSoundClock(int soundClock) {
		this.soundClock = soundClock;
	}
	
	public void reset() {
		lastSample = 0;
		synchronized (samples) { 
			samples.clear();
		}
	}
	
	public boolean isActive() {
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.speech.ISpeechSoundVoice#getSampleCount()
	 */
	@Override
	public int getSampleCount() {
		return samples.size();
	}
	public boolean generate(float[] soundGeneratorWorkBuffer, int from, int to) {

		if (samples.isEmpty())
			return false;
		
		float ptr = from;
		while (ptr < to) {
			Short sample = samples.poll();
			if (sample == null)
				return true;
			
			float next = Math.min(to, ptr + soundClock / 8000.f);
			if (SMOOTH) {
				// smoothly alter voltage
				float delta = (sample - lastSample) / 32768.f / (next - ptr);
				float samp = lastSample / 32768.f;
				
				while (ptr < next) {
					soundGeneratorWorkBuffer[(int) ptr++] += samp;
					samp += delta;
				}
			} else {
				float delta = sample / 32768.f;
				
				while (ptr < next) {
					soundGeneratorWorkBuffer[(int) ptr++] += delta;
				}
			}
			
			this.lastSample = sample;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.audio.speech.ISpeechVoice#setSample(short)
	 */
	@Override
	public void addSample(short sample) {
		samples.add(sample);
	}
}