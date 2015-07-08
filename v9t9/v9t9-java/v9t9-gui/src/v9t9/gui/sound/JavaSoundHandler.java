/*
  JavaSoundHandler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.sound;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import v9t9.audio.sound.CassetteSoundGenerator;
import v9t9.audio.sound.SoundGeneratorFactory;
import v9t9.audio.speech.SpeechGeneratorFactory;
import v9t9.common.cassette.CassetteConsts;
import v9t9.common.client.ISoundHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.common.sound.ISoundGenerator;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.sound.AlsaSoundListener;
import ejs.base.sound.ISoundEmitter;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;
import ejs.base.sound.SoundFactory;

/**
 * Handle sound generation for output with Java APIs
 * @author ejs
 *
 */
public class JavaSoundHandler implements ISoundHandler {

	private final IMachine machine;
	private IProperty soundVolume;
	private IProperty playSound;
	
	private List<ISoundGenerator> generators = new ArrayList<ISoundGenerator>(1);
	private List<ISoundOutput> outputs = new ArrayList<ISoundOutput>(1);
	private Map<ISoundGenerator, ISoundOutput> genToOutputMap 
		= new LinkedHashMap<ISoundGenerator, ISoundOutput>();
	
	public JavaSoundHandler(final IMachine machine) {

		this.machine = machine;
		
		register(SoundGeneratorFactory.createSoundGenerator(machine));
		register(SpeechGeneratorFactory.createSpeechGenerator(machine));
		if (machine.getCassette() != null) {
			register(new CassetteSoundGenerator(machine, machine.getCassette().getCassette1(), 0));
			register(new CassetteSoundGenerator(machine, machine.getCassette().getCassette2(), CassetteConsts.REG_COUNT_CASSETTE));
		}
				
		soundVolume = Settings.get(machine, ISoundHandler.settingSoundVolume);
		playSound = Settings.get(machine, ISoundHandler.settingPlaySound);
		
		for (ISoundOutput output : outputs) {
			
			ISoundEmitter soundAudio = SoundFactory.createAudioListener();
			if (soundAudio instanceof AlsaSoundListener)
				((AlsaSoundListener) soundAudio).setBlockMode(false);
			output.addEmitter(soundAudio);
		}

		
		soundVolume.addListenerAndFire(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				for (ISoundOutput output : outputs) {
					output.setVolume(setting.getInt() / 10.0);
				}
			}
		});

		playSound.addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				synchronized (JavaSoundHandler.this) {
					if (!isRecording()) {
						toggleSound(setting.getBoolean());
					} else {
						// while recording, just mute
						if (setting.getBoolean()) {
							
							for (ISoundOutput output : outputs) {
								output.setVolume(soundVolume.getInt() / 10.0);
							}
						} else {
							for (ISoundOutput output : outputs) {
								output.setVolume(0);
							}
						}
					}
				}
			}
			
		});
		
		for (ISoundGenerator gen : generators) {
			for (ISoundVoice voice : gen.getSoundVoices()) {
				voice.setFormat(gen.getAudioFormat());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.ISoundHandler#register(ejs.base.sound.ISoundOutput)
	 */
	@Override
	public void register(ISoundGenerator generator) {
		if (generator == null)
			return;
		
		generators.add(generator);
		
		ISoundOutput output = SoundFactory.createSoundOutput(generator.getAudioFormat(), 
				machine.getTicksPerSec());
		genToOutputMap.put(generator, output);
		outputs.add(output);
		
		generator.configureSoundOutput(output);
	}

	/**
	 * @return
	 */
	protected boolean isRecording() {
		for (ISoundGenerator generator : generators) {
			if (generator.getRecordingSettingSchema() != null && 
					machine.getSettings().get(generator.getRecordingSettingSchema()).getString() != null)
				return true;
		}
		return false;
	}


	public synchronized void dispose() {
		toggleSound(false);

		for (ISoundOutput output : outputs) {
			output.dispose();
		}
		genToOutputMap.clear();
		generators.clear();
	}


	public synchronized void toggleSound(boolean enabled) {
		if (enabled) {
			for (ISoundOutput output : outputs) {
				output.start();
			}
		} else {
			for (ISoundOutput output : outputs) {
				output.stop();
			}
		}
	}

	public synchronized void generateSound(int pos, int total) {
		if (total == 0)
			return;
		
		for (ISoundGenerator generator : generators) {
			generator.generate(genToOutputMap.get(generator), pos, total);
		}
	}
	
	public void speech() {
	}

	public synchronized void flushAudio(int pos, int total) {
		for (ISoundGenerator gen : generators) {
			gen.flushAudio(genToOutputMap.get(gen), pos, total);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.ISoundHandler#getGeneratorToOutputMap()
	 */
	@Override
	public Map<ISoundGenerator, ISoundOutput> getGeneratorToOutputMap() {
		return genToOutputMap;
	}
}
