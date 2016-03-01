/*
  SpeechTMS5220Generator.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.speech;

import v9t9.common.client.ISoundHandler;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.settings.SettingSchema;
import v9t9.common.speech.ISpeechGenerator;
import v9t9.common.speech.ISpeechSoundVoice;
import ejs.base.sound.ISoundOutput;
import ejs.base.sound.ISoundVoice;
import ejs.base.sound.SoundFormat;

/**
 * @author ejs
 *
 */
public class SpeechTMS5220Generator implements ISpeechGenerator {
	private static final SoundFormat format = new SoundFormat(8000 * 6, 1, 
			SoundFormat.Type.SIGNED_16_LE);
	
	private SpeechVoice[] speechVoices;

	private ISpeechChip speech;

	/**
	 * 
	 */
	public SpeechTMS5220Generator(ISpeechChip speech) {
		this.speech = speech;
		speechVoices = new SpeechVoice[1];
		speechVoices[0] = new SpeechVoice();
		
		speech.addSpeechListener(this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getName()
	 */
	@Override
	public String getName() {
		return "speech";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getRecordingSettingSchema()
	 */
	@Override
	public SettingSchema getRecordingSettingSchema() {
		return ISoundHandler.settingRecordSpeechOutputFile;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getAudioFormat()
	 */
	@Override
	public SoundFormat getSoundFormat() {
		return format;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.speech.ISpeechGenerator#getSpeechVoices()
	 */
	@Override
	public ISpeechSoundVoice[] getSpeechVoices() {
		return speechVoices;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#getSoundVoices()
	 */
	@Override
	public ISoundVoice[] getSoundVoices() {
		return speechVoices;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.speech.ISpeechDataSender#send(short, int, int)
	 */
	@Override
	public void sendSample(short val, int pos, int length) {
		speechVoices[0].addSample(val);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.speech.ISpeechDataSender#speechDone()
	 */
	@Override
	public void speechDone() {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#configureSoundOutput(ejs.base.sound.ISoundOutput)
	 */
	@Override
	public void configureSoundOutput(ISoundOutput output) {
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#flushAudio(ejs.base.sound.ISoundOutput, int, int)
	 */
	@Override
	public void flushAudio(ISoundOutput output, int pos, int total) {
		output.flushAudio(getSpeechVoices(), total);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#generate(ejs.base.sound.ISoundOutput, int, int)
	 */
	@Override
	public void generate(ISoundOutput output, int pos, int total) {
		if (speech == null || output == null)
			return;

		ISpeechSoundVoice[] vs = getSpeechVoices();
		
		//int samples = speechFramesPerTick * speechFormat.getChannels();

		int count = vs[0].getSampleCount();
		if (count == 0)
			return;

		int realTotal = (int) (count * getSoundFormat().getFrameRate() / 8000.f);
		output.generate(vs, realTotal);
		
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.sound.ISoundGenerator#isSilenceRecorded()
	 */
	@Override
	public boolean isSilenceRecorded() {
		return false;
	}
}

