/**
 * 
 */
package v9t9.gui.sound;

import javax.sound.sampled.AudioFormat;

import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.base.properties.SettingProperty;
import v9t9.base.sound.AlsaSoundListener;
import v9t9.base.sound.ISoundListener;
import v9t9.base.sound.ISoundOutput;
import v9t9.base.sound.ISoundVoice;
import v9t9.base.sound.SoundFactory;
import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.engine.settings.EmulatorSettings;

/**
 * Handle sound generation for output with Java APIs
 * @author ejs
 *
 */
public class JavaSoundHandler implements ISoundHandler {
	public static SettingProperty settingRecordSoundOutputFile = new SettingProperty("RecordSoundOutputFile", String.class, null);
	public static SettingProperty settingRecordSpeechOutputFile = new SettingProperty("RecordSpeechOutputFile", String.class, null);

	private SoundRecordingHelper soundRecordingHelper;
	private SoundRecordingHelper speechRecordingHelper;
	private AudioFormat soundFormat;
	private ISoundOutput output;
	private int lastUpdatedPos;
	private int soundFramesPerTick;
	private int speechFramesPerTick;
	private AudioFormat speechFormat;
	private ISoundOutput speechOutput;
	private ISoundListener audio;
	private ISoundListener speechAudio;
	private final IMachine machine;
	
	public JavaSoundHandler(final IMachine machine) {

		this.machine = machine;
		soundFormat = new AudioFormat(55930, 16, 2, true, false);
		
		speechFramesPerTick = 6;

		speechFormat = new AudioFormat(speechFramesPerTick * 8000, 16, 1, true, false);
		
		output = SoundFactory.createSoundOutput(soundFormat, machine.getCpuTicksPerSec());
		speechOutput = SoundFactory.createSoundOutput(speechFormat, machine.getCpuTicksPerSec());
		
		audio = SoundFactory.createAudioListener();
		if (audio instanceof AlsaSoundListener)
			((AlsaSoundListener) audio).setBlockMode(false);
		
		speechAudio = SoundFactory.createAudioListener();
		output.addListener(audio);
		speechOutput.addListener(speechAudio);
		
		EmulatorSettings.settingSoundVolume.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				output.setVolume(setting.getInt() / 10.0);
			}
		});

		EmulatorSettings.INSTANCE.register(EmulatorSettings.settingSoundVolume);
		EmulatorSettings.INSTANCE.register(EmulatorSettings.settingPlaySound);
		
		soundRecordingHelper = new SoundRecordingHelper(output, settingRecordSoundOutputFile, "sound");
		speechRecordingHelper = new SoundRecordingHelper(speechOutput, settingRecordSpeechOutputFile, "speech");
		
		// frames in ALSA means samples per channel, but raw freq in javax
		//soundFramesPerTick = (int) ((soundFormat.getFrameRate()
		//		+ machine.getCpuTicksPerSec() - 1) / machine.getCpuTicksPerSec());
		soundFramesPerTick = output.getSamples((1000 + machine.getCpuTicksPerSec() - 1) / machine.getCpuTicksPerSec());
		

		
		
		EmulatorSettings.settingPlaySound.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				toggleSound(setting.getBoolean());
			}
			
		});
		
		toggleSound(EmulatorSettings.settingPlaySound.getBoolean());
	}
	

	public void dispose() {
		toggleSound(false);

		if (soundRecordingHelper != null) {
			soundRecordingHelper.dispose();
			soundRecordingHelper = null;
		}
		if (speechRecordingHelper != null) {
			speechRecordingHelper.dispose();
			speechRecordingHelper = null;
		}
		if (output != null) {
			output.dispose();
			output = null;
		}
		
		if (speechOutput != null) {
			speechOutput.dispose();
			speechOutput = null;
		}
	}


	public void toggleSound(boolean enabled) {
		if (enabled) {
			output.start();
			speechOutput.start();
		} else {
			output.stop();
			speechOutput.stop();
		}
	}

	public synchronized void generateSound() {
		if (machine.getSound() == null)
			return;

		int pos = machine.getCpu().getCurrentCycleCount();
		int total = machine.getCpu().getCurrentTargetCycleCount();
		
		if (total == 0)
			return;
		
		int totalCount = pos;
		
		int currentPos = (int) ((long) (pos * soundFramesPerTick * soundFormat.getChannels() + total - 1 ) / total);
		if (currentPos < 0)
			currentPos = 0;
		updateSoundGenerator(lastUpdatedPos, currentPos, totalCount);
		lastUpdatedPos = currentPos;
	}

	protected synchronized void updateSoundGenerator(int from, int to, int totalCount) {
		if (to > soundFramesPerTick)
			to = soundFramesPerTick;
		if (from >= to)
			return;

		ISoundVoice[] vs = machine.getSound().getSoundVoices();

		output.generate(vs, to - from);
	}

	public synchronized void speech() {
		ISpeechChip speech = machine.getSpeech();
		if (speech == null)
			return;

		ISoundVoice[] vs = speech.getSpeechVoices();
		
		int samples = speechFramesPerTick * speechFormat.getChannels();

		speechOutput.generate(vs, samples);
	}

	public synchronized void flushAudio() {
		int currentCycleCount = machine.getCpu().getCurrentCycleCount();
		if (output != null && machine.getSound() != null && currentCycleCount > 0) {
			updateSoundGenerator(lastUpdatedPos, soundFramesPerTick, 
					currentCycleCount * (soundFramesPerTick - lastUpdatedPos) /
					machine.getCpu().getCurrentTargetCycleCount());
			
			lastUpdatedPos = 0;
	
			ISoundVoice[] vs = machine.getSound().getSoundVoices();
			output.flushAudio(vs, currentCycleCount);
		}
		
		if (speechOutput != null && machine.getSpeech() != null) {
			speechOutput.flushAudio(machine.getSpeech().getSpeechVoices(),
					(int)(speechFormat.getSampleRate() / speechFramesPerTick));
		}
	}

	/**
	 * @return the soundRecordingHelper
	 */
	public SoundRecordingHelper getSoundRecordingHelper() {
		return soundRecordingHelper;
	}
	
	/**
	 * @return the speechRecordingHelper
	 */
	public SoundRecordingHelper getSpeechRecordingHelper() {
		return speechRecordingHelper;
	}
}
