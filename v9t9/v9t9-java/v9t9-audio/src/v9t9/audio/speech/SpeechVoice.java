/*
  SpeechVoice.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.speech;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import v9t9.common.speech.ISpeechSoundVoice;
import ejs.base.sound.ISoundOutput;

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
	
	@Override
	public void setOutput(ISoundOutput output) {
		this.soundClock = (int) output.getSoundFormat().getFrameRate();
	}
	
	public void setSoundClock(int soundClock) {
		this.soundClock = soundClock;
	}
	
	public void reset() {
		lastSample = 0;
		//synchronized (samples) { 
			samples.clear();
		//}
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