/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.SoundTMS9919.SoundVoice;
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
	private byte[] soundGeneratorWaveForm;
	private AudioFormat stdFormat;
	private int soundFramesPerTick;
	private int soundClock;

	private SourceDataLine audioGateLine;
	private AudioFormat audioGateFormat;
	private int audioGateFramesPerTick;
	private byte[] audioGateWaveForm;

	private SourceDataLine speechLine;
	private AudioFormat speechFormat;
	private int speechFramesPerTick;
	private byte[] speechWaveForm;

	private SoundTMS9919 sound;

	// private boolean audioSilence;

	protected boolean wasSilent;

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

	ConcurrentLinkedQueue<AudioChunk> soundQueue;
	ConcurrentLinkedQueue<AudioChunk> speechQueue;
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

	public JavaSoundHandler(Machine machine) {

		sound = machine.getSound();

		soundQueue = new ConcurrentLinkedQueue<AudioChunk>();
		speechQueue = new ConcurrentLinkedQueue<AudioChunk>();

		stdFormat = new AudioFormat(55900, 8, 1, true, false);
		Line.Info slInfo = new DataLine.Info(SourceDataLine.class, stdFormat);
		if (!AudioSystem.isLineSupported(slInfo)) {
			System.err.println("Line not supported: " + stdFormat);

			stdFormat = new AudioFormat(44100, 8, 1, true, false);
			slInfo = new DataLine.Info(SourceDataLine.class, stdFormat);

			if (!AudioSystem.isLineSupported(slInfo)) {
				System.err.println("Line not supported: " + stdFormat);
				return;
			}
		}
		audioGateFormat = new AudioFormat(55900, 8, 1, true, false);
		Line.Info agInfo = new DataLine.Info(SourceDataLine.class,
				audioGateFormat);
		if (!AudioSystem.isLineSupported(agInfo)) {
			System.err.println("Line not supported: " + agInfo);

			audioGateFormat = new AudioFormat(44100, 8, 1, true, false);
			slInfo = new DataLine.Info(SourceDataLine.class, audioGateFormat);

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
			soundGeneratorLine.open(stdFormat, soundFramesPerTick * 10);
			audioGateFramesPerTick = (int) (audioGateFormat.getFrameRate() / machine
					.getCpuTicksPerSec());
			audioGateLine = (SourceDataLine) AudioSystem.getLine(agInfo);
			audioGateLine.open(audioGateFormat, audioGateFramesPerTick * 10);
			speechFramesPerTick = (int) (speechFormat.getFrameRate() / machine
					.getCpuTicksPerSec());
			speechLine = (SourceDataLine) AudioSystem.getLine(spInfo);
			speechLine.open(speechFormat, speechFramesPerTick * 10);
		} catch (LineUnavailableException e) {
			System.err.println("Line not available");
			e.printStackTrace();
			soundGeneratorWaveForm = new byte[0];
			audioGateWaveForm = new byte[0];
			speechWaveForm = new byte[0];
			return;
		}

		// TODO: add a listener to the settingPauseMachine and send empty data
		// to
		// the sound chip then

		soundWritingThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					// wait for the main thread(s) to send us data to write,
					// which we do here in a separate thread to allow for
					// the potential of blocking.
					AudioChunk chunk = null;
					synchronized (soundQueue) {

						chunk = soundQueue.poll();
						
						if (chunk == null) {
							try {
								soundQueue.wait(500);
							} catch (InterruptedException e) {
								break;
							}
							if (Thread.interrupted())
								return;

							continue;
						}

						// toss extra chunks if too many arrive
						while (chunk != null && soundQueue.size() > 2) {
							chunk = soundQueue.poll();
						}

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

					if (chunk.audioToWrite != null) {
						audioGateLine.write(chunk.audioToWrite, 0,
								chunk.audioToWrite.length);
					}

					/*
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

					}*/

					// soundWritingRequest = false;
				}
			}

		}, "Sound Writing");
		soundWritingThread.start();
		
		
		speechWritingThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					// wait for the main thread(s) to send us data to write,
					// which we do here in a separate thread to allow for
					// the potential of blocking.
					AudioChunk chunk = null;
					synchronized (speechQueue) {

						chunk = speechQueue.poll();

						if (chunk == null) {
							try {
								speechQueue.wait(500);
							} catch (InterruptedException e) {
								break;
							}
							if (Thread.interrupted())
								return;
							continue;
						}
						
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

					// soundWritingRequest = false;
				}
			}

		}, "Speech Writing");
		speechWritingThread.start();

		toggleSound(true);
	}

	public void toggleSound(boolean enabled) {
		if (enabled) {
			soundGeneratorLine.start();
			audioGateLine.start();
			speechLine.start();
		} else {
			soundGeneratorLine.stop();
			audioGateLine.stop();
			speechLine.stop();
		}

		soundGeneratorWaveForm = new byte[stdFormat.getFrameSize()
				* soundFramesPerTick];
		audioGateWaveForm = new byte[audioGateFormat.getFrameSize()
				* audioGateFramesPerTick];
		speechWaveForm = new byte[speechFormat.getFrameSize()
				* speechFramesPerTick];
		soundClock = (int) stdFormat.getFrameRate();

	}

	static final int atten[] = {
			0x00000000,
			// 0x00080000,
			0x0009A9C5, 0x000BAC10, 0x000E1945, 0x001107A1, 0x001491FC,
			0x0018D8C4, 0x001E0327, 0x00244075, 0x002BC9D6, 0x0034E454,
			0x003FE353, 0x004D2B8C, 0x005D36AB, 0x007097A5, 0x007FFFFF };

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
			updateSoundGenerator(lastUpdatedPos, currentPos);
			lastUpdatedPos = currentPos;
		}
	}

	protected synchronized void updateSoundGenerator(int from, int to) {
		if (from >= to)
			return;

		// System.out.println("Updating " + from + " to " + to);
		SoundVoice[] vs = sound.getSoundVoices();

		int[] voices = { -1, -1, -1, -1 };
		int vcnt = 0;
		for (int vi = 0; vi < 4; vi++) {
			if (vs[vi].volume != 0)
				voices[vcnt++] = vi;
		}

		if (vcnt > 0) {
			for (int i = from; i < to; i++) {
				int sample = 0;
				for (int vidx = 0; vidx < vcnt; vidx++) {
					int vi = voices[vidx];
					SoundVoice v = vs[vi];
					int sampleDelta = (v.voice & 1) != 0 ? atten[v.volume]
							: -atten[v.volume];
					sample = v.generate(soundClock, sample, sampleDelta);
				}
				soundGeneratorWaveForm[i] = (byte) (sample >> 18);
			}
		} else {
			if (from < to)
				Arrays.fill(soundGeneratorWaveForm, from, to, (byte) 0);
		}
	}

	public void dispose() {
		if (soundWritingThread != null)
			soundWritingThread.interrupt();
		synchronized (soundQueue) {
			soundQueue.notify();
		}
		
		if (speechWritingThread != null)
			speechWritingThread.interrupt();
		synchronized (speechQueue) {
			speechQueue.notify();
		}
		// if (mixTimer != null)
		// mixTimer.cancel();
		if (speechLine != null)
			speechLine.close();
		if (audioGateLine != null)
			audioGateLine.close();
		if (soundGeneratorLine != null)
			soundGeneratorLine.close();
	}

	// int maxAudioPos;
	public void audioGate(int bit, int pos, int total) {
		if (audioGateWaveForm != null && bit != 0) {
			int idx = (int) ((long) pos * (audioGateFramesPerTick - 1) / total);
			if (idx >= audioGateWaveForm.length)
				idx = audioGateWaveForm.length - 1;
			audioGateWaveForm[idx] = (byte) 0x80;
		}
	}

	public synchronized void speech(short sample) {
		if (speechWaveForm != null) {

			if (lastSpeechUpdatedPos >= speechWaveForm.length) {
				speechQueue.add(new AudioChunk(null, null, speechWaveForm));
				synchronized (speechQueue) {
					speechQueue.notify();
				}
				lastSpeechUpdatedPos = 0;
				speechWaveForm = new byte[speechWaveForm.length];
			}
			speechWaveForm[lastSpeechUpdatedPos++] = (byte) (sample & 0xff);
			speechWaveForm[lastSpeechUpdatedPos++] = (byte) (sample >> 8);
		}
	}

	public synchronized void flushAudio() {
		if (soundGeneratorWaveForm == null)
			return;

		int length = soundGeneratorWaveForm.length;
		updateSoundGenerator(lastUpdatedPos, length);
		lastUpdatedPos = 0;


		soundQueue.add(new AudioChunk(soundGeneratorWaveForm,
				audioGateWaveForm, null));

		synchronized (soundQueue) {
			soundQueue.notify();

		}
		soundGeneratorWaveForm = new byte[soundGeneratorWaveForm.length];
		audioGateWaveForm = new byte[audioGateWaveForm.length];
		

		boolean anySpeech = (lastSpeechUpdatedPos > 0);
		if (anySpeech) {
			Arrays.fill(speechWaveForm, lastSpeechUpdatedPos,
					speechWaveForm.length, (byte) 0);
			lastSpeechUpdatedPos = 0;
			speechQueue.add(new AudioChunk(null, null, speechWaveForm));
			
			speechWaveForm = new byte[speechWaveForm.length];
		}
		synchronized (speechQueue) {
			speechQueue.notify();

		}
	}

}
