/*
  SpeechTMS5220Generator.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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

