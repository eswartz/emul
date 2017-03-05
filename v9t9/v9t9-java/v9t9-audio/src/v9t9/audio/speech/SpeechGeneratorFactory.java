/*
  SpeechGeneratorFactory.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.audio.speech;

import v9t9.common.machine.IMachine;
import v9t9.common.speech.ISpeechGenerator;

/**
 * @author ejs
 *
 */
public class SpeechGeneratorFactory {

	/**
	 * @param machine
	 * @return
	 */
	public static ISpeechGenerator createSpeechGenerator(IMachine machine) {
		if (machine.getSpeech() != null)
			return new SpeechTMS5220Generator(machine.getSpeech());
		else
			return null;
	}

}
