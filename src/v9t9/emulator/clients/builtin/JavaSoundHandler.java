/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
 * @author ejs
 *
 */
public class JavaSoundHandler implements SoundHandler {

	//private static final long SOUND_UPDATE_RATE = 100;	// times per second
	
	private SourceDataLine soundGeneratorLine;
	private SourceDataLine audioGateLine;
	private AudioFormat stdFormat;
	//private Timer mixTimer;
	//private TimerTask mixerTask;
	
	private byte[] soundGeneratorWaveForm;
	private byte[] soundGeneratorWaveForm2;
	private byte[] audioGateWaveForm;
	private byte[] audioGateWaveForm2;

	private int soundFramesPerTick;

	private int soundClock;

	private SoundTMS9919 sound;

	private AudioFormat audioGateFormat;

	private int audioGateFramesPerTick;

	//private boolean audioSilence;

	protected boolean wasSilent;

	protected int lastUpdatedPos;

	//private Mixer mixer;
	
	private FileOutputStream fos;
	private Thread soundWritingThread;
	private Object soundWritingToken = new Object();
	protected boolean soundWritingRequest;
	private final Machine machine;
	private byte[] soundToWrite;
	private byte[] audioToWrite;
	
	{
		if (false) {
			try {
				if (File.separatorChar == '/')
					fos = new FileOutputStream("/tmp/v9t9_audio.raw");
				else
					fos = new FileOutputStream("c:/temp/v9t9_audio.raw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public JavaSoundHandler(Machine machine) {
		
		this.machine = machine;
		sound = machine.getSound();
		stdFormat = new AudioFormat(55930, 8, 1, true, false);
		Line.Info slInfo = new DataLine.Info(SourceDataLine.class,
				stdFormat);
		if (!AudioSystem.isLineSupported(slInfo)) {
			System.err.println("Line not supported: " + stdFormat);
			
			stdFormat = new AudioFormat(44100, 8, 1, true, false);
			slInfo = new DataLine.Info(SourceDataLine.class,
					stdFormat);
			
			if (!AudioSystem.isLineSupported(slInfo)) {
				System.err.println("Line not supported: " + stdFormat);
				return;
			}
		}
		audioGateFormat = new AudioFormat(44100, 8, 1, true, false);
		Line.Info agInfo = new DataLine.Info(SourceDataLine.class,
				audioGateFormat);
		if (!AudioSystem.isLineSupported(agInfo)) {
			System.err.println("Line not supported: " + agInfo);
			return;
		}
		
		try {
			soundFramesPerTick = (int) (stdFormat.getFrameRate() / (1000 / machine.getCpuTickLength()));
			soundGeneratorLine = (SourceDataLine) AudioSystem.getLine(slInfo);
			soundGeneratorLine.open(stdFormat, soundFramesPerTick * 10);
			audioGateFramesPerTick = (int) (audioGateFormat.getFrameRate() / (1000 / machine.getCpuTickLength()));
			audioGateLine = (SourceDataLine) AudioSystem.getLine(agInfo);
			audioGateLine.open(audioGateFormat, audioGateFramesPerTick * 10);
		} catch (LineUnavailableException e) {
			System.err.println("Line not available");
			e.printStackTrace();
			soundGeneratorWaveForm = new byte[0];
			soundGeneratorWaveForm2 = new byte[0];
			audioGateWaveForm = new byte[0];
			audioGateWaveForm2 = new byte[0];
			return;
		}
		
		
		soundWritingThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					// wait for the main thread(s) to send us data to write,
					// which we do here in a separate thread to allow for
					// the potential of blocking.
					synchronized (soundWritingToken) {
						while (!soundWritingRequest) {
							try {
								soundWritingToken.wait();
							} catch (InterruptedException e) {
								if (!JavaSoundHandler.this.machine.isRunning())
									return;
							}
						}
						
						if (fos != null) {
							try {
								fos.write(soundToWrite, 0, soundToWrite.length);
							} catch (IOException e) {
								try {
									fos.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}

						/*byte prev = soundGeneratorWaveForm[0];
						for (int i = 1; i < length; i++) {
							byte nprev = soundGeneratorWaveForm[i];
							soundGeneratorWaveForm[i] = (byte) ((prev + soundGeneratorWaveForm[i]) / 2);
							prev = nprev;
						}*/
						soundGeneratorLine.write(soundToWrite, 0, soundToWrite.length);
						
						
						if (/*!audioSilence &&*/ audioToWrite != null) {
							audioGateLine.write(
									audioToWrite, 0, audioToWrite.length);
						}
						
						soundWritingRequest = false;
					}
				}
			}
			
		}, "Sound Writing");
		soundWritingThread.start();
		
		toggleSound(true);
	}

	public void toggleSound(boolean enabled) {
		if (enabled) {
			soundGeneratorLine.start();
			audioGateLine.start();
		} else {
			soundGeneratorLine.stop();
			audioGateLine.stop();
		}
		
		soundGeneratorWaveForm = new byte[stdFormat.getFrameSize() * soundFramesPerTick];
		soundGeneratorWaveForm2 = new byte[stdFormat.getFrameSize() * soundFramesPerTick];
		audioGateWaveForm = new byte[audioGateFormat.getFrameSize() * audioGateFramesPerTick];
		audioGateWaveForm2 = new byte[audioGateFormat.getFrameSize() * audioGateFramesPerTick];
		soundClock = (int) stdFormat.getFrameRate();
		
		/*
		mixTimer = new Timer("Mixer");
		mixerTask = new TimerTask() {
			@Override
			public void run() {
				synchronized (JavaSoundHandler.this) {
					int length = soundGeneratorWaveForm.length;
					updateSoundGenerator(lastUpdatedPos, length);
					
					byte prev = soundGeneratorWaveForm[0];
					for (int i = 1; i < length; i++) {
						byte nprev = soundGeneratorWaveForm[i];
						soundGeneratorWaveForm[i] = (byte) ((prev + soundGeneratorWaveForm[i]) / 2);
						prev = nprev;
					}
					soundGeneratorLine.write(
							soundGeneratorWaveForm, 0, length);
					lastUpdatedPos = 0;
				}
			}
		};
		
		mixTimer.scheduleAtFixedRate(mixerTask, 0, 1000 / SOUND_UPDATE_RATE);
		*/
	}
	
	static final int  atten[] = {
		0x00000000,
	//  0x00080000,
		0x0009A9C5,
		0x000BAC10,
		0x000E1945,
		0x001107A1,
		0x001491FC,
		0x0018D8C4,
		0x001E0327,
		0x00244075,
		0x002BC9D6,
		0x0034E454,
		0x003FE353,
		0x004D2B8C,
		0x005D36AB,
		0x007097A5,
		0x007FFFFF
	};
	
	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#updateVoice(int, v9t9.emulator.clients.builtin.Sound99xx.voiceinfo)
	 */
	public void updateVoice(int vn, int updateFlags, int pos, int total) {
		if (vn >= 4) 
			throw new IllegalArgumentException("Did not expect more than 4 voices");

		// need to update all the voices together
		SoundVoice v = sound.getSoundVoices()[vn];
		
		//for (int vi = SoundTMS9919.VOICE_TONE_0; vi <= SoundTMS9919.VOICE_TONE_2; vi++) {
		if (vn < SoundTMS9919.VOICE_NOISE) {
			if (v.hertz * 2 < soundClock) {
				v.delta = v.hertz * 2;
			} else {
				v.delta = 0;
			}
			v.sampleDelta = (v.voice & 1) != 0 ? atten[v.volume] : -atten[v.volume];
	//	}
		} else {
			if (v.OPERATION_TO_NOISE_TYPE() == SoundTMS9919.NOISE_WHITE) {
				v.delta = v.hertz;
			} else {
				//clock = m->soundhz * PERIODMULT;
				v.delta = v.hertz / 16;
			}
			v.div = v.volume != 0 ? v.div : 0;
			//v.sampleDelta = v.volume * 127 / 15;
			v.sampleDelta = atten[v.volume];
		}
		
		int currentPos = pos * soundGeneratorWaveForm.length / total;
		updateSoundGenerator(lastUpdatedPos, currentPos);
		lastUpdatedPos = currentPos;
	}

	protected synchronized void updateSoundGenerator(int from, int to) {
		if (from >= to)
			return;
		
		SoundVoice[] vs = sound.getSoundVoices();
		boolean iswhite = vs[SoundTMS9919.VOICE_NOISE].OPERATION_TO_NOISE_TYPE() == SoundTMS9919.NOISE_WHITE;
		
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
					if (v.voice < SoundTMS9919.VOICE_NOISE) {
						sample = combineSquareWaveform(v, sample);
					} else if (iswhite) {
						sample = combineWhiteNoiseWaveform(v, sample);
					} else {
						sample = combinePeriodicNoiseWaveform(v, sample, i % 16 == 0);
					}
				}
				soundGeneratorWaveForm[i] = (byte) (sample >> 18);
			}
		} else {
			if (from < to)
				Arrays.fill(soundGeneratorWaveForm, from, to, (byte) 0);
		}
	}

	private int combineWhiteNoiseWaveform(SoundVoice v, int sample) {
		v.div += v.delta;
		while (v.div >= soundClock) {
			v.ns1 = (v.ns1<<1) | ((v.ns1>>31) & 1);
			v.ns1 ^= v.ns2;	
			if ((v.ns2 += v.ns1)==0)	v.ns2++;
			v.div -= soundClock;
		}
		if ((v.ns1 & 1) != 0 ) {
			sample += v.sampleDelta;
		}
		return sample;
	}
	private int combinePeriodicNoiseWaveform(SoundVoice v, int sample, boolean go) {
		v.div += v.delta;
		if (v.div >= soundClock) {
			sample += v.sampleDelta;
			while (v.div >= soundClock)
				v.div -= soundClock;
		} else {
			sample -= v.sampleDelta;
		}
		return sample;
	}

	private int combineSquareWaveform(SoundVoice v, int sample) {
		if (!v.alt) {
			sample += v.sampleDelta;
		} else {
			sample -= v.sampleDelta;
		}
		v.div += v.delta;
		
		// this loop usually executes only once
		while (v.div >= soundClock) {
			v.alt = !v.alt;
			v.div -= soundClock;
		}	
		return sample;
	}

	public void dispose() {
		if (soundWritingThread != null)
			soundWritingThread.interrupt();
		//if (mixTimer != null)
		//	mixTimer.cancel();
		if (audioGateLine != null)
			audioGateLine.close();
		if (soundGeneratorLine != null)
			soundGeneratorLine.close();
	}
	
	//int maxAudioPos;
	public void audioGate(int bit, int pos, int total) {
		if (audioGateWaveForm != null && bit != 0) {
			//long clock = machine.getCpu().getCurrentCycleCount() 
			//	* this.audioGateFramesPerTick / machine.getCpu().getCurrentTargetCycleCount();
			//if (clock < 0) clock = 0;
			//System.out.println(clock);
			int idx = (int) ((long)pos * (audioGateFramesPerTick - 1) / total);
			if (idx >= audioGateWaveForm.length)
				idx = audioGateWaveForm.length - 1;
			//if (pos > maxAudioPos)
			//	maxAudioPos = pos;
			audioGateWaveForm[idx] = (byte) 0x80;
			//audioSilence = false;
		}
	}

	public synchronized void flushAudio() {
		int length = soundGeneratorWaveForm.length;
		updateSoundGenerator(lastUpdatedPos, length);
		lastUpdatedPos = 0;
		
		synchronized (soundWritingToken) {
			
			// swap buffers so we're not writing the same one being
			// pushed to the device
			byte[] t = soundGeneratorWaveForm;
			soundGeneratorWaveForm = soundGeneratorWaveForm2;
			soundGeneratorWaveForm2 = t;
			t = audioGateWaveForm;
			audioGateWaveForm = audioGateWaveForm2;
			audioGateWaveForm2 = t;
			
			// tell the thread which ones to read from
			soundToWrite = soundGeneratorWaveForm2;
			audioToWrite = audioGateWaveForm2;
			soundWritingRequest = true;
			soundWritingToken.notifyAll();
			
			//Arrays.fill(audioGateWaveForm, (byte) 0);
		}
		Arrays.fill(audioGateWaveForm, (byte) 0);
	}

}
