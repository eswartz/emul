/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

import org.apache.log4j.Logger;

/**
 * Digital sound in a specific encoding
 * @author ejs
 *
 */
public class AudioChunk {
	private static final Logger logger = Logger.getLogger(AudioChunk.class);
	
	private boolean isEmpty;

	public AudioChunk(SoundChunk chunk) {
		this(chunk, 1.0);
	}

	/**
	 * @param chunk
	 * @param volume
	 */
	public AudioChunk(SoundChunk chunk, double volume) {
		//logger.debug(volume);
		double scale = volume * volume * volume;
		AudioFormat format = chunk.getFormat();
		this.isEmpty = true;
		int length = chunk.getLength();
		this.soundData = new byte[format.getSampleSizeInBits() * length / 8];
		if (!chunk.isSilent()) {
			if (format.getSampleSizeInBits() == 16) {
				for (int i = 0; i < length; i++) {
					float s = chunk.at(i);
					if (s < -1.0f) s = -1.0f; else if (s > 1.0f) s = 1.0f;
					short samp = (short) (s * scale * 32767);
					if (samp != 0)
						isEmpty = false;
					//samp &= 0xf000;
					soundData[i*2] = (byte) (samp & 0xff);
					soundData[i*2+1] = (byte) (samp >> 8);
				}
			} else if (format.getSampleSizeInBits() == 8) {
				for (int i = 0; i < length; i++) {
					float s = chunk.at(i);
					if (s < -1.0f) s = -1.0f; else if (s > 1.0f) s = 1.0f;
					byte samp = (byte) (s * scale * 127);
					if (samp != 0)
						isEmpty = false;
					soundData[i] = samp;
				}
			} else {
				logger.error("Not handled: " + format);
			}
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