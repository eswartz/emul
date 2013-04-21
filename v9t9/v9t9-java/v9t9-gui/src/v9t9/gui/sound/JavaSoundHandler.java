/*
  JavaSoundHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.sound;

import javax.sound.sampled.AudioFormat;


import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.sound.AlsaSoundListener;
import ejs.base.sound.ISoundEmitter;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;
import ejs.base.sound.SoundFactory;

import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.common.sound.ICassetteVoice;
import v9t9.common.sound.ISoundGenerator;
import v9t9.common.speech.ISpeechGenerator;
import v9t9.common.speech.ISpeechSoundVoice;

/**
 * Handle sound generation for output with Java APIs
 * @author ejs
 *
 */
public class JavaSoundHandler implements ISoundHandler {

	private AudioFormat soundFormat;
	private ISoundOutput soundOutput;
	private int lastSoundUpdatedPos;
	private int soundFramesPerTick;
	private AudioFormat speechFormat;
	private ISoundOutput speechOutput;
	private ISoundEmitter soundAudio;
	private ISoundEmitter speechAudio;
	private AudioFormat cassetteFormat;
	private ISoundOutput cassetteOutput;
	private ISoundEmitter cassetteAudio;
	private final IMachine machine;
	private IProperty soundVolume;
	private IProperty playSound;
	
	private ISoundGenerator soundGenerator;
	private final ISpeechGenerator speechGenerator;
	/*private*/ int lastSpeechUpdatedPos;
	private IProperty recordingSound;
	private IProperty recordingSpeech;
	private ISoundGenerator cassetteGenerator;
	private IProperty recordingCassette1;
	private IProperty recordingCassette2;
	private int cassetteFramesPerTick;
	private int lastCassetteUpdatedPos;
	
	public JavaSoundHandler(final IMachine machine,
			final ISoundGenerator soundGenerator,
			final ISpeechGenerator speechGenerator,
			final ISoundGenerator cassetteGenerator
			) {

		this.machine = machine;
		this.soundGenerator = soundGenerator;
		this.speechGenerator = speechGenerator;
		this.cassetteGenerator = cassetteGenerator;
		
		soundVolume = Settings.get(machine, ISoundHandler.settingSoundVolume);
		playSound = Settings.get(machine, ISoundHandler.settingPlaySound);
		
		recordingSound = Settings.get(machine, ISoundHandler.settingRecordSoundOutputFile);
		recordingSpeech = Settings.get(machine, ISoundHandler.settingRecordSpeechOutputFile);
		recordingCassette1 = Settings.get(machine, ICassetteVoice.settingCassette1OutputFile);
		recordingCassette2 = Settings.get(machine, ICassetteVoice.settingCassette2OutputFile);
		
		soundFormat = new AudioFormat(55930, 16, 2, true, false);
		speechFormat = new AudioFormat(8000 * 6, 16, 1, true, false);
		cassetteFormat = new AudioFormat(22000, 8, 1, false, false);	// 22050 freaks out Pulse :(
		
		soundOutput = SoundFactory.createSoundOutput(soundFormat, machine.getTicksPerSec());
		speechOutput = SoundFactory.createSoundOutput(speechFormat, machine.getTicksPerSec());
		cassetteOutput = SoundFactory.createSoundOutput(cassetteFormat, machine.getTicksPerSec());

		soundAudio = SoundFactory.createAudioListener();
		if (soundAudio instanceof AlsaSoundListener)
			((AlsaSoundListener) soundAudio).setBlockMode(false);
		
		speechAudio = SoundFactory.createAudioListener();
		
		cassetteAudio = SoundFactory.createAudioListener();
		
		soundOutput.addEmitter(soundAudio);
		speechOutput.addEmitter(speechAudio);
		cassetteOutput.addEmitter(cassetteAudio);

		soundOutput.addMutator(new TI99SoundSmoother());
		
		soundVolume.addListenerAndFire(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				soundOutput.setVolume(setting.getInt() / 10.0);
				speechOutput.setVolume(setting.getInt() / 10.0);
				cassetteOutput.setVolume(setting.getInt() / 10.0);
			}
		});

		// frames in ALSA means samples per channel, but raw freq in javax
		//soundFramesPerTick = (int) ((soundFormat.getFrameRate()
		//		+ machine.getCpuTicksPerSec() - 1) / machine.getCpuTicksPerSec());
		soundFramesPerTick = soundOutput.getSamples((1000 + machine.getTicksPerSec() - 1) / machine.getTicksPerSec());
		cassetteFramesPerTick = cassetteOutput.getSamples((1000 + machine.getTicksPerSec() - 1) / machine.getTicksPerSec());
		
		toggleSound(true);
		playSound.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				if (!isRecording()) {
					toggleSound(setting.getBoolean());
				} else {
					// while recording, just mute
					if (setting.getBoolean()) {
						soundOutput.setVolume(soundVolume.getInt() / 10.0);
						speechOutput.setVolume(soundVolume.getInt() / 10.0);
						cassetteOutput.setVolume(soundVolume.getInt() / 10.0);
					} else {
						soundOutput.setVolume(0);
						speechOutput.setVolume(0);
						cassetteOutput.setVolume(0);
					}
				}
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
		if (cassetteGenerator != null) {
			for (ISoundVoice voice : cassetteGenerator.getSoundVoices()) {
				voice.setFormat(cassetteFormat);
			}
		}
	}
	

	/**
	 * @return
	 */
	protected boolean isRecording() {
		return recordingSound.getString() != null || recordingSpeech.getString() != null
				|| recordingCassette1.getString() != null || recordingCassette2.getString() != null;
	}


	public synchronized void dispose() {
		toggleSound(false);

		if (soundOutput != null) {
			soundOutput.dispose();
			soundOutput = null;
		}
		
		if (speechOutput != null) {
			speechOutput.dispose();
			speechOutput = null;
		}
	}


	public synchronized void toggleSound(boolean enabled) {
		if (enabled) {
			soundOutput.start();
			speechOutput.start();
		} else {
			if (soundOutput != null)
				soundOutput.stop();
			if (speechOutput != null)
				speechOutput.stop();
		}
	}

	public synchronized void generateSound(int pos, int total) {
		if (total == 0)
			return;
		
		if (soundGenerator != null) {
			
			int totalCount = pos;
			
			long ticksPos = (long) (pos * soundFramesPerTick * soundFormat.getChannels() );
			int currentPos = (int) ((ticksPos + total - 1 ) / total);
			if (currentPos < 0)
				currentPos = 0;
			updateSoundGenerator(lastSoundUpdatedPos, currentPos, totalCount);
			lastSoundUpdatedPos = currentPos;
		}
		if (cassetteGenerator != null) {
			int totalCount = pos;
			
			long ticksPos = (long) (pos * cassetteFramesPerTick * cassetteFormat.getChannels() );
			int currentPos = (int) ((ticksPos + total - 1 ) / total);
			if (currentPos < 0)
				currentPos = 0;
			updateCassetteGenerator(lastCassetteUpdatedPos, currentPos, totalCount);
			lastCassetteUpdatedPos = currentPos;
		}
	}

	protected void updateSoundGenerator(int from, int to, int totalCount) {
		if (to > soundFramesPerTick)
			to = soundFramesPerTick;
		if (from >= to)
			return;

		if (soundOutput != null) {
			ISoundVoice[] vs = soundGenerator.getSoundVoices();
			soundOutput.generate(vs, to - from);
		}
	}

	protected void updateCassetteGenerator(int from, int to, int totalCount) {
		if (to > cassetteFramesPerTick)
			to = cassetteFramesPerTick;
		if (from >= to)
			return;

		if (cassetteOutput != null) {
			ISoundVoice[] vs = cassetteGenerator.getSoundVoices();
			cassetteOutput.generate(vs, to - from);
		}
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
		if (soundOutput != null && machine.getSound() != null && total > 0) {
			int totalSoundCount = (int) (((long) pos * (soundFramesPerTick - lastSoundUpdatedPos + total - 1)) / total);
			updateSoundGenerator(lastSoundUpdatedPos, soundFramesPerTick, totalSoundCount);
			lastSoundUpdatedPos = 0;
	
			ISoundVoice[] vs = soundGenerator.getSoundVoices();
			soundOutput.flushAudio(vs, total);
		}
		if (cassetteOutput != null && machine.getSound() != null && total > 0) {
			int totalCassetteCount = (int) (((long) pos * (cassetteFramesPerTick - lastCassetteUpdatedPos + total - 1)) / total);
			updateCassetteGenerator(lastCassetteUpdatedPos, cassetteFramesPerTick, totalCassetteCount);
			lastCassetteUpdatedPos = 0;
			
			ISoundVoice[] vs = cassetteGenerator.getSoundVoices();
			cassetteOutput.flushAudio(vs, total);
		}
		
		if (speechOutput != null && machine.getSpeech() != null) {
			ISpeechSoundVoice[] speechVoices = speechGenerator.getSpeechVoices();
			int count = speechVoices[0].getSampleCount();
			speechOutput.generate(speechVoices, (int) (count * speechFormat.getSampleRate() / 8000.f)); 
			lastSpeechUpdatedPos = 0;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.ISoundHandler#getSoundOutput()
	 */
	@Override
	public ISoundOutput getSoundOutput() {
		return soundOutput;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.ISoundHandler#getSpeechOutput()
	 */
	@Override
	public ISoundOutput getSpeechOutput() {
		return speechOutput;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.client.ISoundHandler#getCassetteOutput()
	 */
	@Override
	public ISoundOutput getCassetteOutput() {
		return cassetteOutput;
	}
}
