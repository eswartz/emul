/**
 * 
 */
package v9t9.emulator.clients.builtin.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFileFormat.Type;

import v9t9.emulator.Machine;
import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.hardware.sound.ClockedSoundVoice;
import v9t9.emulator.hardware.sound.SoundVoice;
import v9t9.engine.SoundHandler;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;

/**
 * Mixing and output for sound and speech
 * 
 * @author ejs
 * 
 */
public class JavaSoundHandler implements SoundHandler {

	private SourceDataLine soundGeneratorLine;
	private volatile int[] soundGeneratorWorkBuffer;
	private int[] soundGeneratorWorkBuffer2;
	private byte[] soundGeneratorWaveForm;
	private AudioFormat soundFormat;
	private int soundFramesPerTick;
	private int soundClock;

	private SourceDataLine speechLine;
	private AudioFormat speechFormat;
	private int speechFramesPerTick;
	private byte[] speechWaveForm;

	private SoundProvider sound;

	// private boolean audioSilence;

	protected volatile int lastUpdatedPos;
	protected volatile int lastSpeechUpdatedPos;

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
	private DFTAnalyzer dftAnalyzer;

	public static Setting settingPlaySound = new Setting("PlaySound", new Boolean(true));
	public static Setting settingRecordSoundOutputFile = new Setting("RecordSoundOutputFile", (String)null);
	public static Setting settingRecordSpeechOutputFile = new Setting("RecordSpeechOutputFile", (String)null);
	private final Machine machine;

	public JavaSoundHandler(final Machine machine) {

		this.machine = machine;
		settingRecordSoundOutputFile.addListener(new ISettingListener() {
			public void changed(Setting setting, Object oldValue) {
				if (soundFos != null) {
					closeSoundDumpFile(soundFos, soundFormat, (String) oldValue);
					soundFos = null;
				}
				String filename = setting.getString();
				if (filename != null) {
					try {
						soundFos = new FileOutputStream(filename);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		
		settingRecordSpeechOutputFile.addListener(new ISettingListener() {
			public void changed(Setting setting, Object oldValue) {
				if (speechFos != null) {
					closeSoundDumpFile(speechFos, speechFormat, (String) oldValue);
					speechFos = null;
				}
				String filename = setting.getString();
				if (filename != null) {
					try {
						speechFos = new FileOutputStream(filename);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		sound = machine.getSound();

		settingPlaySound.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				toggleSound(setting.getBoolean());
			}
			
		});
		
		toggleSound(settingPlaySound.getBoolean());
	}

	protected void dft(byte[] soundToWrite) {
		if (dftAnalyzer == null)
			//dftAnalyzer = new DFTAnalyzer(10);		// analyze 10 bits, e.g. same amount possible via clock
			dftAnalyzer = new DFTAnalyzer(8);
		dftAnalyzer.send(soundToWrite);
	}

	public synchronized void toggleSound(boolean enabled) {
		if (enabled) {
			startSound();
		} else {
			stopSound();
		}
	}

	private synchronized void stopSound() {
		if (soundFos != null) {
			try {
				soundFos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			soundFos = null;
		}
		if (speechFos != null) {
			try {
				speechFos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			speechFos = null;
		}
		if (speechLine != null) {
			speechLine.close();
			speechLine = null;
		}
		if (soundGeneratorLine != null) {
			soundGeneratorLine.close();
			soundGeneratorLine = null;
		}
		
		if (soundWritingThread != null) {
			soundWritingThread.interrupt();
			try {
				soundWritingThread.join();
			} catch (InterruptedException e) {
				
			}
			soundWritingThread = null;
		}

		if (speechWritingThread != null) {
			speechWritingThread.interrupt();
			try {
				speechWritingThread.join();
			} catch (InterruptedException e) {
				
			}
			speechWritingThread = null;
		}
	}

	private synchronized void startSound() {
		stopSound();
		
		soundQueue = new LinkedBlockingQueue<AudioChunk>();
		speechQueue = new LinkedBlockingQueue<AudioChunk>();

		soundFormat = new AudioFormat(55930, 8, 2, true, false);
		Line.Info slInfo = new DataLine.Info(SourceDataLine.class, soundFormat);
		if (true || !AudioSystem.isLineSupported(slInfo)) {
			System.err.println("Line not supported: " + soundFormat);

			soundFormat = new AudioFormat(44100, 8, 2, true, false);
			slInfo = new DataLine.Info(SourceDataLine.class, soundFormat);

			if (!AudioSystem.isLineSupported(slInfo)) {
				System.err.println("Line not supported: " + soundFormat);
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
			soundFramesPerTick = (int) (soundFormat.getFrameRate() / machine
					.getCpuTicksPerSec());
			soundGeneratorLine = (SourceDataLine) AudioSystem.getLine(slInfo);
			soundGeneratorLine.open(soundFormat, soundFramesPerTick * 20);
			
			System.out.println("Sound format: " + soundFormat);
			speechFramesPerTick = (int) (speechFormat.getFrameRate() / machine
					.getCpuTicksPerSec());
			speechLine = (SourceDataLine) AudioSystem.getLine(spInfo);
			speechLine.open(speechFormat, speechFramesPerTick * 20);
			System.out.println("Speech format: " + speechFormat);

			soundClock = (int) soundFormat.getFrameRate();

			for (SoundVoice voice : sound.getSoundVoices()) {
				if (voice instanceof ClockedSoundVoice)
					((ClockedSoundVoice) voice).setSoundClock(soundClock);
			}
		} catch (LineUnavailableException e) {
			System.err.println("Line not available");
			e.printStackTrace();
			soundGeneratorWaveForm = new byte[0];
			soundGeneratorWorkBuffer = new int[0];
			soundGeneratorWorkBuffer2 = new int[0];
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
					
					//if (chunk != null) dft(chunk.soundToWrite);
					
					// toss extra chunks if too many arrive
					while (chunk != null && soundQueue.size() > 2) {
						chunk = soundQueue.poll();
					}

					if (soundGeneratorLine == null)
						return;
					
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

					if (speechLine == null)
						return;
					
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
		
		soundGeneratorLine.start();
		speechLine.start();
		
		soundGeneratorWaveForm = new byte[soundFormat.getFrameSize()
		                  				* soundFramesPerTick];
		soundGeneratorWorkBuffer = new int[soundFormat.getFrameSize()
				* soundFramesPerTick];
		soundGeneratorWorkBuffer2 = new int[soundFormat.getFrameSize()
		         * soundFramesPerTick];
		speechWaveForm = new byte[speechFormat.getFrameSize()
				* speechFramesPerTick];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.engine.SoundHandler#updateVoice(int,
	 * v9t9.emulator.clients.builtin.Sound99xx.voiceinfo)
	 */
	public synchronized void updateVoice(int pos, int total) {
		if (total == 0)
			return;
		
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

		if (true) {
			int active = 0;
			for (SoundVoice v : vs) {
				if (v.isActive()) {
					//Arrays.fill(soundGeneratorWorkBuffer2, 0);
					v.generate(soundGeneratorWorkBuffer, from, to);
					active++;
				}
			}
			if (active > 0) {
				int div = 2;
				for (int i = from; i < to; i++) {
					int s = soundGeneratorWorkBuffer[i];
					if (div > 0) s /= div;
					if (s > 0x7fffff)
						s = 0x7fffff;
					else if (s < -0x7fffff)
						s = -0x7fffff;
					soundGeneratorWorkBuffer[i] = s;
				}
			}
		} else {
			for (SoundVoice v : vs) {
				if (v.isActive()) {
					Arrays.fill(soundGeneratorWorkBuffer2, 0);
					v.generate(soundGeneratorWorkBuffer2, from, to);
					// mix them together
					for (int i = from; i < to; i++) {
						int a = soundGeneratorWorkBuffer[i];
						int b = soundGeneratorWorkBuffer2[i];
						int c;
						if (a == 0)
							c = b;
						else if (b == 0)
							c = a;
						else if ((a ^ b) < 0) {
							c = (a + b) ;
						} else {
							c = (int) (((long)a + (long)b - a * b / 0x1000000) );
						}
						soundGeneratorWorkBuffer[i] = c;
					}
				}
			}
		}
			
	}

	public void dispose() {
		stopSound();

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
		if (soundGeneratorWaveForm == null || (soundGeneratorWaveForm.length == 0 && speechWaveForm.length == 0))
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

		if (false) {
			int prevL = 0, prevR = 0;
			for (int i = 0; i < length; i+=2) {
				int sampleL = soundGeneratorWorkBuffer[i];
				soundGeneratorWorkBuffer[i] = (prevL + sampleL) >> 1;
				prevL = sampleL;
				int sampleR = soundGeneratorWorkBuffer[i+1];
				soundGeneratorWorkBuffer[i+1] = (prevR + sampleR) >> 1;
				prevR = sampleR;
			}
		}
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

	private void closeSoundDumpFile(FileOutputStream fos, AudioFormat format, String filename) {
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (filename != null) {
			// convert to the file type
			AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
			
			int idx = filename.lastIndexOf('.');
			if (idx >= 0) {
				String ext = filename.substring(idx + 1);
				Type[] types = AudioSystem.getAudioFileTypes();
				for (Type type : types) {
					if (type.getExtension().equalsIgnoreCase(ext)) {
						fileType = type;
						break;
					}
				}
			}
			
			File soundFile = new File(filename);
			File soundFileRaw = new File(filename + ".tmp");
			soundFileRaw.delete();
			
			boolean converted = false;
			try {
				if (!soundFile.renameTo(soundFileRaw))
					return;

				AudioInputStream is = null;
				
				try {
					is = new AudioInputStream(
							new FileInputStream(soundFileRaw),
							format,
							soundFileRaw.length());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				
				try {
					AudioSystem.write(is, fileType, soundFile);
					
					soundFileRaw.delete();
					converted = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} finally {
				if (!converted) {
					System.err.println("Could not convert raw sound file (" + format + ") to " + fileType);
				}
			}
		}
	}

	/**
	 * Get the supported file types
	 * @return
	 */
	public static String[] getSoundFileExtensions() {

		Type[] types = AudioSystem.getAudioFileTypes();
		String[] extensions = new String[types.length];
		for (int idx = 0; idx < extensions.length; idx++) {
			extensions[idx] = types[idx].getExtension() + "|" + types[idx].toString();
		}
		return extensions;
	}

}
