/**
 * 
 */
package v9t9.engine.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * @author ejs
 *
 */
public class CassetteReader {


	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {

		File audioFile = new File(args[0]);
		AudioFileFormat format = AudioSystem.getAudioFileFormat(audioFile);
		
		AudioInputStream is = null;
		
		is = new AudioInputStream(
				new FileInputStream(args[0]),
				format.getFormat(),
				audioFile.length());

		final int BASE_CLOCK = 3000000;
		final int POLL_CLOCK = 1378 * 3 / 2;
		float secsPerPoll = (float) POLL_CLOCK / BASE_CLOCK;
		
		CassetteReader reader = new CassetteReader(is);
		while (!reader.isDone()) {
			System.out.print(reader.getPosition() + ": ");
			int val = reader.readBit(secsPerPoll);
			System.out.println(val);
		}
	}

	
	private long position;
	private AudioInputStream is;
	private float mag;
	private int nch;
	private int sampSize;
	private boolean bigEndian;
	private boolean signed;
	private boolean endOfTape;
	private int lastPolarity;

	/**
	 * @param is 
	 * 
	 */
	public CassetteReader(AudioInputStream is) {
		this.is = is;
		nch = is.getFormat().getChannels();
		sampSize = is.getFormat().getFrameSize();
		bigEndian = is.getFormat().isBigEndian();
		signed = is.getFormat().getEncoding() == Encoding.PCM_SIGNED;
		mag = 1.0f;
	}

	/**
	 * @return
	 */
	private boolean isDone() {
		try {
			return is.available() == 0;
		} catch (IOException e) {
			return true;
		}
	}

	/**
	 * @return
	 */
	private long getPosition() {
		return position;
	}
	

	/**
	 * @return
	 */
	private float readSample() {
		if (endOfTape)
			return 0f;
		try {
			float total = 0.f;
			byte[] buf = new byte[is.getFormat().getFrameSize()];
			for (int ch = 0; ch < nch; ch++) {
				int samp = 0;
				int len = is.read(buf);
				if (len != buf.length) {
					if (!endOfTape) {
						endOfTape = true;
					}
				}
				if (sampSize == 1) {
					samp = signed ? buf[0] : (buf[0] - 0x80) & 0xff;
					total += samp / 128f;
				}
				else if (sampSize == 2) {
					if (bigEndian)
						samp = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);
					else
						samp = ((buf[1] & 0xff) << 8) | (buf[0] & 0xff);
					if (signed)
						samp = (short) samp;
					total += samp / 32768f;
				}
				else if (sampSize == 4) {
					long lsamp;
					if (bigEndian)
						lsamp = ((buf[3] & 0xff) << 24) | ((buf[2] & 0xff) << 16) |
							((buf[1] & 0xff) << 8) | (buf[1] & 0xff);
					else
						lsamp = ((buf[0] & 0xff) << 24) | ((buf[1] & 0xff) << 16) |
							((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
					if (signed)
						lsamp = (int) lsamp;

					total += lsamp / (float)0x80000000L;
				}
				position++;
			}
			
			float samp = total / nch;
			
			float absSamp = Math.abs(samp);
			if (absSamp >= mag) {
				mag = (mag * 7 + absSamp) / 8f;
			} else {
				mag = (mag * 255f) / 256f;
			}
			
			if (mag >= 0.05f) {
				return samp;
			}
			return 0f;
			
		} catch (IOException e) {
			return 0;
		}
	}
	
	public int readBit(float secs) {
		int polarity = readPolarity(secs);
		if (lastPolarity == 0) {
			lastPolarity = polarity;
			return 0;
		}
		else if (polarity != lastPolarity) {
			lastPolarity = polarity;
			return 1;
		}
		return 0;
	}
	/**
	 * Read the current polarity
	 * @param secs amount of time, in seconds, to poll
	 * @return
	 */
	protected int readPolarity(float secs) {
		//System.out.print("[" + position + "]");
		int samples = (int) (is.getFormat().getFrameRate() * secs);
		int pos = 0;
		int neg = 0;
		float prev = 0;
		boolean count = true;
		while (samples-- > 0) {
			float samp = readSample();
			
			if (!count) {
				// look for next polarity shift
				if (Math.signum(samp) != Math.signum(prev)) {
					count = true;
				} else {
					prev = samp;
					continue;
				}
			}
			
			if (samp < prev)
				neg++;
			else if (samp > prev)
				pos++;
			
		}
		return pos > neg ? 1 : pos < neg ? -1 : 0;
	}

	public boolean isEndOfTape() {
		return endOfTape;
	}
	/**
	 * 
	 */
	public void close() {
		try { is.close(); } catch (IOException e) { }
	}


}
