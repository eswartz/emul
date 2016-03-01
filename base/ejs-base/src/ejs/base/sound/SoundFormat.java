/**
 * 
 */
package ejs.base.sound;

/**
 * Generic format for audio, independent of javax.sampled.
 * @author ejs
 *
 */
public class SoundFormat {
	public enum Type {
		UNSIGNED_8,
		SIGNED_8,
		SIGNED_16_LE,
		SIGNED_16_BE,
		FLOAT_32_LE
	};
	
	private float sampleRate = 44100;
	private int channels = 1;

	private Type type = Type.FLOAT_32_LE;

	public SoundFormat(float rate, int channels, Type type) {
		setSampleRate(rate);
		setChannels(channels);
		setType(type);
	}
	
	/**
	 * @return the sampleRate
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(float sampleRate) {
		if (sampleRate < 1)
			throw new IllegalArgumentException();

		this.sampleRate = sampleRate;
	}

	/**
	 * @return the channels
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * @param channels the channels to set
	 */
	public void setChannels(int channels) {
		if (channels < 1)
			throw new IllegalArgumentException();
		this.channels = channels;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		if (type == null)
			throw new IllegalArgumentException();
		this.type = type;
	}
	
	public float getFrameRate() {
		return sampleRate;
	}
	
	public int getBytesPerSample() {
		switch (type) {
		case FLOAT_32_LE:
			return 4;
		case SIGNED_16_LE:
		case SIGNED_16_BE:
			return 2;
		case UNSIGNED_8:
		case SIGNED_8:
			return 1;
		}
		throw new IllegalStateException();
	}
	public int getBytesPerFrame() {
		return getBytesPerSample() * channels;
	}
	
	public boolean isSigned() {
		switch (type) {
		case FLOAT_32_LE:
		case SIGNED_16_LE:
		case SIGNED_16_BE:
		case SIGNED_8:
			return true;
		case UNSIGNED_8:
			return false;
		}
		throw new IllegalStateException();
	}

	/**
	 * @return
	 */
	public boolean isIntegral() {
		switch (type) {
		case FLOAT_32_LE:
			return false;
		case SIGNED_16_LE:
		case SIGNED_16_BE:
		case SIGNED_8:
		case UNSIGNED_8:
			return true;
		}
		throw new IllegalStateException();
	}

	/**
	 * @return
	 */
	public boolean isBigEndian() {
		switch (type) {
		case FLOAT_32_LE:
		case SIGNED_16_LE:
		case SIGNED_8:
		case UNSIGNED_8:
			return false;
		case SIGNED_16_BE:
			return true;
		}
		throw new IllegalStateException();
	}
	
}
