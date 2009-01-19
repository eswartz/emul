/**
 * 
 */
package v9t9.emulator.clients.builtin.sound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.hardware.sound.SoundVoice;
import v9t9.engine.SoundHandler;

/**
 * TMS9919 / SN76489 emulation
 * 
 * @author ejs
 * 
 */
public class JavaSoundHandler implements SoundHandler {

	// private static final long SOUND_UPDATE_RATE = 100; // times per second

	private SourceDataLine soundGeneratorLine;
	private int[] soundGeneratorWorkBuffer;
	private byte[] soundGeneratorWaveForm;
	private AudioFormat stdFormat;
	private int soundFramesPerTick;
	private int soundClock;

	private SourceDataLine speechLine;
	private AudioFormat speechFormat;
	private int speechFramesPerTick;
	private byte[] speechWaveForm;

	private SoundProvider sound;

	// private boolean audioSilence;

	protected int lastUpdatedPos;
	protected int lastSpeechUpdatedPos;

	// private Mixer mixer;

	private FileOutputStream soundFos;
	private FileOutputStream speechFos;
	private Thread soundWritingThread;

	static class AudioChunk {
		public AudioChunk(byte[] soundToWrite, byte[] audioToWrite,
				byte[] speechToWrite) {
			this.soundToWrite = soundToWrite;
			this.audioToWrite = audioToWrite;
			this.speechToWrite = speechToWrite;
		}

		byte[] soundToWrite;
		byte[] audioToWrite;
		byte[] speechToWrite;

	}

	BlockingQueue<AudioChunk> soundQueue;
	BlockingQueue<AudioChunk> speechQueue;
	private Thread speechWritingThread;

	{
		if (true) {
			try {
				if (File.separatorChar == '/')
					soundFos = new FileOutputStream("/tmp/v9t9_audio.raw");
				else
					soundFos = new FileOutputStream("c:/temp/v9t9_audio.raw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (false) {
			try {
				if (File.separatorChar == '/')
					speechFos = new FileOutputStream("/tmp/v9t9_speech.raw");
				else
					speechFos = new FileOutputStream("c:/temp/v9t9_speech.raw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public JavaSoundHandler(final Machine machine) {

		sound = machine.getSound();

		soundQueue = new LinkedBlockingQueue<AudioChunk>();
		speechQueue = new LinkedBlockingQueue<AudioChunk>();

		stdFormat = new AudioFormat(55930, 8, 2, true, false);
		Line.Info slInfo = new DataLine.Info(SourceDataLine.class, stdFormat);
		if (!AudioSystem.isLineSupported(slInfo)) {
			System.err.println("Line not supported: " + stdFormat);

			stdFormat = new AudioFormat(44100, 8, 2, true, false);
			slInfo = new DataLine.Info(SourceDataLine.class, stdFormat);

			if (!AudioSystem.isLineSupported(slInfo)) {
				System.err.println("Line not supported: " + stdFormat);
				return;
			}
		}

		speechFormat = new AudioFormat(8000, 16, 1, true, false);
		Line.Info spInfo = new DataLine.Info(SourceDataLine.class, speechFormat);
		if (!AudioSystem.isLineSupported(spInfo)) {
			System.err.println("Line not supported: " + spInfo);
			return;
		}

		try {
			soundFramesPerTick = (int) (stdFormat.getFrameRate() / machine
					.getCpuTicksPerSec());
			soundGeneratorLine = (SourceDataLine) AudioSystem.getLine(slInfo);
			soundGeneratorLine.open(stdFormat, soundFramesPerTick * 20);
			speechFramesPerTick = (int) (speechFormat.getFrameRate() / machine
					.getCpuTicksPerSec());
			speechLine = (SourceDataLine) AudioSystem.getLine(spInfo);
			speechLine.open(speechFormat, speechFramesPerTick * 10);
		} catch (LineUnavailableException e) {
			System.err.println("Line not available");
			e.printStackTrace();
			soundGeneratorWaveForm = new byte[0];
			soundGeneratorWorkBuffer = new int[0];
			speechWaveForm = new byte[0];
			return;
		}

		// TODO: add a listener to the settingPauseMachine and send empty data
		// to
		// the sound chip then

		soundWritingThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					AudioChunk chunk = null;

					try {
						chunk = soundQueue.take();
					} catch (InterruptedException e2) {
						return;
					}

					// toss extra chunks if too many arrive
					while (chunk != null && soundQueue.size() > 2) {
						chunk = soundQueue.poll();
					}

					if (chunk.soundToWrite != null) {
						soundGeneratorLine.write(chunk.soundToWrite, 0,
								chunk.soundToWrite.length);

						if (soundFos != null) {
							try {
								soundFos.write(chunk.soundToWrite, 0,
										chunk.soundToWrite.length);
							} catch (IOException e) {
								try {
									soundFos.close();
									soundFos = null;
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}

		}, "Sound Writing");
		soundWritingThread.start();

		speechWritingThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					AudioChunk chunk = null;
					try {
						chunk = speechQueue.take();
					} catch (InterruptedException e) {
						return;
					}

					if (chunk.speechToWrite != null) {
						speechLine.write(chunk.speechToWrite, 0,
								chunk.speechToWrite.length);

						if (speechFos != null) {
							try {
								speechFos.write(chunk.speechToWrite, 0,
										chunk.speechToWrite.length);
							} catch (IOException e) {
								try {
									speechFos.close();
									speechFos = null;
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}

					}
				}
			}

		}, "Speech Writing");
		speechWritingThread.start();

		toggleSound(true);
	}

	public void toggleSound(boolean enabled) {
		if (enabled) {
			soundGeneratorLine.start();
			speechLine.start();
		} else {
			soundGeneratorLine.stop();
			speechLine.stop();
		}

		soundGeneratorWaveForm = new byte[stdFormat.getFrameSize()
				* soundFramesPerTick];
		soundGeneratorWorkBuffer = new int[stdFormat.getFrameSize()
				* soundFramesPerTick];
		speechWaveForm = new byte[speechFormat.getFrameSize()
				* speechFramesPerTick];
		soundClock = (int) stdFormat.getFrameRate();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.engine.SoundHandler#updateVoice(int,
	 * v9t9.emulator.clients.builtin.Sound99xx.voiceinfo)
	 */
	public void updateVoice(int pos, int total) {

		if (soundGeneratorWaveForm != null) {
			int currentPos = (int) ((long) pos * soundGeneratorWaveForm.length / total);
			if (currentPos < 0)
				currentPos = 0;
			// force left channel
			currentPos &= ~1;
			// System.out.print(currentPos+" ");
			updateSoundGenerator(lastUpdatedPos, currentPos);
			lastUpdatedPos = currentPos;
		}
	}

	protected synchronized void updateSoundGenerator(int from, int to) {
		if (to > soundGeneratorWaveForm.length)
			to = soundGeneratorWaveForm.length;
		if (from >= to)
			return;

		SoundVoice[] vs = sound.getSoundVoices();

		int active = 0;
		for (SoundVoice v : vs) {
			if (v.isActive())
				active++;
		}
		if (active > 0) {
			for (SoundVoice v : vs) {
				if (v.isActive())
					v.generate(soundClock, soundGeneratorWorkBuffer, from, to);
			}
			while (from < to) {
				soundGeneratorWorkBuffer[from++] /= active;
			}
		}

	}

	public void dispose() {
		if (soundWritingThread != null)
			soundWritingThread.interrupt();

		if (speechWritingThread != null)
			speechWritingThread.interrupt();

		if (speechLine != null)
			speechLine.close();
		if (soundGeneratorLine != null)
			soundGeneratorLine.close();
	}

	public synchronized void speech(short sample) {
		if (speechWaveForm != null) {

			if (lastSpeechUpdatedPos >= speechWaveForm.length) {
				try {
					speechQueue.put(new AudioChunk(null, null, speechWaveForm));
				} catch (InterruptedException e) {
				}
				lastSpeechUpdatedPos = 0;
				speechWaveForm = new byte[speechWaveForm.length];
			}
			if (lastSpeechUpdatedPos < speechWaveForm.length) {
				speechWaveForm[lastSpeechUpdatedPos++] = (byte) (sample & 0xff);
				speechWaveForm[lastSpeechUpdatedPos++] = (byte) (sample >> 8);
			}
		}
	}

	public synchronized void flushAudio(int pos, int limit) {
		if (soundGeneratorWaveForm == null)
			return;

		// hmm, it would be nice if the audio gate could work perfectly,
		// but the calculations of its data have assumed the "limit" would
		// match reality, when sometimes the tick comes earlier or later.
		// int length = lastUpdatedPos;
		/*
		 * int length = (int) ((long)pos soundGeneratorWaveForm.length / limit);
		 * if (length < lastUpdatedPos) length = lastUpdatedPos; if (length >
		 * soundGeneratorWaveForm.length) length =
		 * soundGeneratorWaveForm.length; byte[] part = new byte[length];
		 * System.arraycopy(soundGeneratorWaveForm, 0, part, 0, length);
		 * Arrays.fill(soundGeneratorWaveForm, (byte) 0); soundQueue.add(new
		 * AudioChunk(part, null, null));
		 */

		int length = soundGeneratorWaveForm.length;
		updateSoundGenerator(lastUpdatedPos, length);
		lastUpdatedPos = 0;

		for (int i = 0; i < length; i++) {
			int sample = soundGeneratorWorkBuffer[i];
			soundGeneratorWaveForm[i] = (byte) (sample >> 16);
		}
		Arrays.fill(soundGeneratorWorkBuffer, 0);

		try {
			soundQueue.put(new AudioChunk(soundGeneratorWaveForm, null, null));
		} catch (InterruptedException e) {
		}
		soundGeneratorWaveForm = new byte[soundGeneratorWaveForm.length];

		boolean anySpeech = (lastSpeechUpdatedPos > 0);
		if (anySpeech) {
			Arrays.fill(speechWaveForm, lastSpeechUpdatedPos,
					speechWaveForm.length, (byte) 0);
			lastSpeechUpdatedPos = 0;
			try {
				speechQueue.put(new AudioChunk(null, null, speechWaveForm));
			} catch (InterruptedException e) {
			}

			speechWaveForm = new byte[speechWaveForm.length];
		}
	}

}
