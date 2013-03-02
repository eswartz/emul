/*
  SpeechVoice.java

  (c) 2011-2012 Edward Swartz

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