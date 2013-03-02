/*
  SpeechTMS5220Generator.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.speech;

import v9t9.common.hardware.ISpeechChip;
import v9t9.common.speech.ISpeechGenerator;
import v9t9.common.speech.ISpeechSoundVoice;

/**
 * @author ejs
 *
 */
public class SpeechTMS5220Generator implements ISpeechGenerator {
	private SpeechVoice[] speechVoices;

	/**
	 * 
	 */
	public SpeechTMS5220Generator(ISpeechChip speech) {
		speechVoices = new SpeechVoice[1];
		speechVoices[0] = new SpeechVoice();
		
		speech.addSpeechListener(this);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.speech.ISpeechGenerator#getSpeechVoices()
	 */
	@Override
	public ISpeechSoundVoice[] getSpeechVoices() {
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
}

