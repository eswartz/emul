/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

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

	private static final long SOUND_UPDATE_RATE = 100;	// times per second
	
	private SourceDataLine soundGeneratorLine;
	private SourceDataLine audioGateLine;
	private AudioFormat stdFormat;
	private Timer mixTimer;
	private TimerTask mixerTask;
	
	private byte[] soundGeneratorWaveForm;
	private byte[] audioGateWaveForm;
	private boolean silence;

	private int soundFramesPerTick;

	private int soundClock;

	private SoundTMS9919 sound;

	private final Machine machine;

	private AudioFormat audioGateFormat;

	private int audioGateFramesPerTick;

	private int audioGateClock;

	//private Mixer mixer;
	
	public JavaSoundHandler(Machine machine) {
		this.machine = machine;
		sound = machine.getSound();
		stdFormat = new AudioFormat(44100, 8, 1, true, false);
		Line.Info slInfo = new DataLine.Info(SourceDataLine.class,
				stdFormat);
		if (!AudioSystem.isLineSupported(slInfo)) {
			System.err.println("Line not supported: " + stdFormat);
			return;
		}
		audioGateFormat = new AudioFormat(44100, 8, 1, true, false);
		Line.Info agInfo = new DataLine.Info(SourceDataLine.class,
				audioGateFormat);
		if (!AudioSystem.isLineSupported(agInfo)) {
			System.err.println("Line not supported: " + agInfo);
			return;
		}
		
		try {
			soundFramesPerTick = (int) (stdFormat.getFrameRate() / SOUND_UPDATE_RATE);
			soundGeneratorLine = (SourceDataLine) AudioSystem.getLine(slInfo);
			soundGeneratorLine.open(stdFormat, soundFramesPerTick * 10);
			audioGateFramesPerTick = (int) (audioGateFormat.getFrameRate() / SOUND_UPDATE_RATE);
			audioGateLine = (SourceDataLine) AudioSystem.getLine(agInfo);
			audioGateLine.open(audioGateFormat, audioGateFramesPerTick * 10);
		} catch (LineUnavailableException e) {
			System.err.println("Line not available");
			e.printStackTrace();
			soundGeneratorWaveForm = new byte[0];
			return;
		}
		
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
		silence = true;
		
		soundGeneratorWaveForm = new byte[stdFormat.getFrameSize() * soundFramesPerTick];
		audioGateWaveForm = new byte[audioGateFormat.getFrameSize() * audioGateFramesPerTick];
		soundClock = (int) stdFormat.getFrameRate();
		audioGateClock = (int) audioGateFormat.getFrameRate();
		
		mixTimer = new Timer("Mixer");
		mixerTask = new TimerTask() {
			@Override
			public void run() {
				if (!silence) {
					updateSoundGenerator();
					soundGeneratorLine.write(
							soundGeneratorWaveForm, 0, soundGeneratorWaveForm.length);
					audioGateLine.write(
							audioGateWaveForm, 0, audioGateWaveForm.length);
					Arrays.fill(audioGateWaveForm, (byte) 0);
				}
			}
		};
		
		mixTimer.scheduleAtFixedRate(mixerTask, 0, 1000 / SOUND_UPDATE_RATE);
	}
	
	protected void updateSoundGenerator() {
		SoundVoice[] vs = sound.getSoundVoices();
		boolean iswhite = vs[SoundTMS9919.VOICE_NOISE].OPERATION_TO_NOISE_TYPE() == SoundTMS9919.NOISE_WHITE;
		
		int[] voices = { -1, -1, -1, -1 };
		int vcnt = 0;
		for (int vi = 0; vi < 4; vi++) {
			if (vs[vi].volume != 0)
				voices[vcnt++] = vi;
		}
			
		for (int i = 0; i < soundFramesPerTick; i++) {
			int sample = 0;
			for (int vidx = 0; vidx < vcnt; vidx++) {
				int vi = voices[vidx];
				SoundVoice v = vs[vi];
				if (vi < SoundTMS9919.VOICE_NOISE) {
					sample = combineSquareWaveform(v, sample);
				} else if (iswhite) {
					sample = combineWhiteNoiseWaveform(v, sample);
				} else {
					sample = combinePeriodicNoiseWaveform(v, sample);
				}
			}
			if (sample > 127)
				sample = 127;
			soundGeneratorWaveForm[i] = (byte) sample;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.SoundHandler#updateVoice(int, v9t9.emulator.clients.builtin.Sound99xx.voiceinfo)
	 */
	public void updateVoice(int vn, int updateFlags) {
		if (vn >= 4) 
			throw new IllegalArgumentException("Did not expect more than 4 voices");

		// need to update all the voices together
		silence = true;
		SoundVoice[] soundVoices = sound.getSoundVoices();
		SoundVoice v;
		for (int vi = SoundTMS9919.VOICE_TONE_0; vi <= SoundTMS9919.VOICE_TONE_2; vi++) {
			v = soundVoices[vi];
			
			v.delta = v.hertz * 2;
			v.sampleDelta = (v.volume * 127 / 15);
			if (v.volume != 0) {
				silence = false;
				break;
			}
		}
		
		v = soundVoices[SoundTMS9919.VOICE_NOISE];
		if (v.OPERATION_TO_NOISE_TYPE() == SoundTMS9919.NOISE_WHITE) {
			v.delta = v.hertz;
		} else {
			//clock = m->soundhz * PERIODMULT;
			v.delta = v.hertz / 16;
		}
		if (v.volume != 0)
			silence = false;
		v.div = v.volume != 0 ? v.div : 0;
		v.sampleDelta = v.volume * 127 / 15;

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
	private int combinePeriodicNoiseWaveform(SoundVoice v, int sample) {
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
		mixTimer.cancel();
	}
	
	public void audioGate(int bit) {
		long clock = machine.getCpu().getTotalCurrentCycleCount() 
			* this.audioGateClock / machine.getCpu().getTargetCycleCount();
		int pos = (int) (clock % audioGateFramesPerTick);
		//System.out.println(pos);
		if (bit != 0) {
			audioGateWaveForm[pos] = (byte) 0x80;
			silence = false;
		}
	}

}
