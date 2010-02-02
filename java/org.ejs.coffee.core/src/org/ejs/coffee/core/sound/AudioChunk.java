/**
 * 
 */
package org.ejs.coffee.core.sound;

import javax.sound.sampled.AudioFormat;

/**
 * Digital sound in a specific encoding
 * @author ejs
 *
 */
public class AudioChunk {
	private boolean isEmpty;

	public AudioChunk(SoundChunk chunk) {
		AudioFormat format = chunk.getFormat();
		this.isEmpty = true;
		this.soundData = new byte[format.getSampleSizeInBits() * chunk.soundData.length / 8];
		
		if (format.getSampleSizeInBits() == 16) {
			for (int i = 0; i < chunk.soundData.length; i++) {
				float s = chunk.soundData[i];
				if (s < -1.0f) s = -1.0f; else if (s > 1.0f) s = 1.0f;
				short samp = (short) (s * 32767);
				if (samp != 0)
					isEmpty = false;
				//samp &= 0xf000;
				soundData[i*2] = (byte) (samp & 0xff);
				soundData[i*2+1] = (byte) (samp >> 8);
			}
		} else if (format.getSampleSizeInBits() == 8) {
			for (int i = 0; i < chunk.soundData.length; i++) {
				float s = chunk.soundData[i];
				if (s < -1.0f) s = -1.0f; else if (s > 1.0f) s = 1.0f;
				byte samp = (byte) (s * 127);
				if (samp != 0)
					isEmpty = false;
				soundData[i] = samp;
			}
		} else {
			System.err.println("Not handled: " + format);
		}
	}

	public byte[] soundData;
	
	/**
	 * @return the isEmpty
	 */
	public boolean isEmpty() {
		return isEmpty;
	}
}