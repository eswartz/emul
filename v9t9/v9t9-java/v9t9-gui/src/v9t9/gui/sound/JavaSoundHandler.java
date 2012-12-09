/**
 * 
 */
package v9t9.gui.sound;

import javax.sound.sampled.AudioFormat;

import org.ejs.gui.sound.SoundRecordingHelper;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.sound.AlsaSoundListener;
import ejs.base.sound.ISoundEmitter;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;
import ejs.base.sound.SoundFactory;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.speech.ISpeechGenerator;
import v9t9.common.speech.ISpeechSoundVoice;

/**
 * Handle sound generation for output with Java APIs
 * @author ejs
 *
 */
public class JavaSoundHandler implements ISoundHandler {
	public static SettingSchema settingRecordSoundOutputFile = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"RecordSoundOutputFile", String.class, null);
	public static SettingSchema settingRecordSpeechOutputFile = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"RecordSpeechOutputFile", String.class, null);

	private SoundRecordingHelper soundRecordingHelper;
	private SoundRecordingHelper speechRecordingHelper;
	private AudioFormat soundFormat;
	private ISoundOutput output;
	private int lastUpdatedPos;
	private int soundFramesPerTick;
	private AudioFormat speechFormat;
	private ISoundOutput speechOutput;
	private ISoundEmitter audio;
	private ISoundEmitter speechAudio;
	private final IMachine machine;
	private IProperty soundVolume;
	private IProperty playSound;
	
	private ISoundGenerator soundGenerator;
	private final ISpeechGenerator speechGenerator;
	/*private*/ int lastSpeechUpdatedPos;
	
	public JavaSoundHandler(final IMachine machine, final ISoundGenerator soundGenerator,
			final ISpeechGenerator speechGenerator) {

		this.machine = machine;
		this.soundGenerator = soundGenerator;
		this.speechGenerator = speechGenerator;
		
		soundVolume = Settings.get(machine, ISoundHandler.settingSoundVolume);
		playSound = Settings.get(machine, ISoundHandler.settingPlaySound);
		
		soundFormat = new AudioFormat(55930, 16, 2, true, false);
		
		speechFormat = new AudioFormat(8000 * 6, 16, 1, true, false);
		
		output = SoundFactory.createSoundOutput(soundFormat, machine.getCpuTicksPerSec());
		speechOutput = SoundFactory.createSoundOutput(speechFormat, machine.getCpuTicksPerSec());

		audio = SoundFactory.createAudioListener();
		if (audio instanceof AlsaSoundListener)
			((AlsaSoundListener) audio).setBlockMode(false);
		
		speechAudio = SoundFactory.createAudioListener();
		
		output.addEmitter(audio);
		speechOutput.addEmitter(speechAudio);

		output.addMutator(new TI99SoundSmoother());
		
		soundVolume.addListenerAndFire(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				output.setVolume(setting.getInt() / 10.0);
				speechOutput.setVolume(setting.getInt() / 10.0);
			}
		});

		soundRecordingHelper = new SoundRecordingHelper(output, 
				Settings.get(machine, settingRecordSoundOutputFile), 
				"sound");
		speechRecordingHelper = new SoundRecordingHelper(speechOutput, 
				Settings.get(machine, settingRecordSpeechOutputFile), 
				"speech");
		
		// frames in ALSA means samples per channel, but raw freq in javax
		//soundFramesPerTick = (int) ((soundFormat.getFrameRate()
		//		+ machine.getCpuTicksPerSec() - 1) / machine.getCpuTicksPerSec());
		soundFramesPerTick = output.getSamples((1000 + machine.getCpuTicksPerSec() - 1) / machine.getCpuTicksPerSec());
		
		playSound.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				toggleSound(setting.getBoolean());
			}
			
		});
		
		if (soundGenerator != null) {
			for (ISoundVoice voice : soundGenerator.getSoundVoices()) {
				voice.setFormat(soundFormat);
			}
		}
		if (speechGenerator != null) {
			for (ISpeechSoundVoice voice : speechGenerator.getSpeechVoices()) {
				voice.setFormat(speechFormat);
			}
		}
		

	}
	

	public synchronized void dispose() {
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


	public synchronized void toggleSound(boolean enabled) {
		if (enabled) {
			output.start();
			speechOutput.start();
		} else {
			if (output != null)
				output.stop();
			if (speechOutput != null)
				speechOutput.stop();
		}
	}

	public synchronized void generateSound(int pos, int total) {
		if (soundGenerator != null) {
			if (total == 0)
				return;
			
			int totalCount = pos;
			
			long ticksPos = (long) (pos * soundFramesPerTick * soundFormat.getChannels() );
			int currentPos = (int) ((ticksPos + total - 1 ) / total);
			if (currentPos < 0)
				currentPos = 0;
			updateSoundGenerator(lastUpdatedPos, currentPos, totalCount);
			lastUpdatedPos = currentPos;
		}
	}

	protected void updateSoundGenerator(int from, int to, int totalCount) {
		if (to > soundFramesPerTick)
			to = soundFramesPerTick;
		if (from >= to)
			return;

		ISoundVoice[] vs = soundGenerator.getSoundVoices();

		if (output != null)
			output.generate(vs, to - from);
	}

	public void speech() {
		synchronized (speechGenerator) {
			ISpeechChip speech = machine.getSpeech();
			if (speech == null || speechOutput == null)
				return;
	
			ISpeechSoundVoice[] vs = speechGenerator.getSpeechVoices();
			
			//int samples = speechFramesPerTick * speechFormat.getChannels();

			int count = vs[0].getSampleCount();
			if (count == 0)
				return;
	
			int total = (int) (count * speechFormat.getFrameRate() / 8000.f);
			speechOutput.generate(vs, total);
			lastSpeechUpdatedPos += total;
		}
	}

	public synchronized void flushAudio(int pos, int total) {
		if (output != null && machine.getSound() != null && total > 0) {
			int totalCount = (int) (((long) pos * (soundFramesPerTick - lastUpdatedPos + total - 1)) / total);
			updateSoundGenerator(lastUpdatedPos, soundFramesPerTick, totalCount);
			
			lastUpdatedPos = 0;
	
			ISoundVoice[] vs = soundGenerator.getSoundVoices();
			output.flushAudio(vs, total);
		}
		
		if (speechOutput != null && machine.getSpeech() != null) {
			ISpeechSoundVoice[] speechVoices = speechGenerator.getSpeechVoices();
			int count = speechVoices[0].getSampleCount();
			speechOutput.generate(speechVoices, (int) (count * speechFormat.getSampleRate() / 8000.f)); 
			lastSpeechUpdatedPos = 0;
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
