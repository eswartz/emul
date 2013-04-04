/**
 * 
 */
package v9t9.audio.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

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
		final int POLL_CLOCK = 1300;
		int cyclesPerPoll = BASE_CLOCK / POLL_CLOCK;
		
		CassetteReader reader = new CassetteReader(is);
		while (!reader.isDone()) {
			System.out.print(reader.getPosition() + ": ");
			int val = reader.readBit(cyclesPerPoll);
			System.out.println(val);
		}
	}

	
	private long position;
	private AudioInputStream is;

	/**
	 * @param is 
	 * 
	 */
	public CassetteReader(AudioInputStream is) {
		this.is = is;
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
		try {
			int nch = is.getFormat().getChannels();
			float total = 0.f;
			int sampSize = is.getFormat().getSampleSizeInBits();
			boolean bigEndian = is.getFormat().isBigEndian();
			boolean signed = is.getFormat().getEncoding() == Encoding.PCM_SIGNED;
			byte[] buf = new byte[sampSize / 8];
			for (int ch = 0; ch < nch; ch++) {
				int samp = 0;
				is.read(buf);
				if (sampSize == 8) {
					samp = signed ? buf[0] : (buf[0] - 0x80) & 0xff;
					total += samp / 128f;
				}
				else if (sampSize == 16) {
					if (bigEndian)
						samp = (buf[0] << 8) | (buf[1] & 0xff);
					else
						samp = (buf[1] << 8) | (buf[0] & 0xff);
					total += samp / 32768f;
				}
				else {
					if (bigEndian)
						samp = ((buf[3] & 0xff) << 24) | ((buf[2] & 0xff) << 16) |
							((buf[1] & 0xff) << 8) | (buf[1] & 0xff);
					else
						samp = ((buf[0] & 0xff) << 24) | ((buf[1] & 0xff) << 16) |
							((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
					total += samp / (float)0x80000000L;
				}
			}
			return total / nch;
		} catch (IOException e) {
			return 0;
		}
	}
	
	/**
	 * @param msec
	 * @return
	 */
	private int readBit(int msec) {
		int samples = (int) (is.getFormat().getFrameSize() * is.getFormat().getFrameRate() * msec / 1000);
		//StringBuilder sb = new StringBuilder();
		while (samples-- > 0) {
			float samp = readSample();
			
			assert samp >= -1.0f && samp <= 1.0f;
			
			/*
			sb.setLength(0);
			int col = (int) ((samp + 1f) * 80 / 2);
			while (--col > 0)
				sb.append(' ');
			sb.append('*');
			System.out.println(sb);
			*/
			
			
		}
		return 0;
	}


}
