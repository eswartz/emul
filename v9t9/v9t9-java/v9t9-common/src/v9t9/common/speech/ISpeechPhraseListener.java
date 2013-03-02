/*
  ISpeechPhraseListener.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.speech;

/**
 * Listener for the behavioral details of speech synthesis,
 * from the perspective of the CPU<-->chip interaction.
 * @author ejs
 *
 */
public interface ISpeechPhraseListener {

	/** A new phrase started */
	void phraseStarted();
	/** A byte (LPC encoded, in FIFO memory order) was added to the phrase */
	void phraseByteAdded(byte byt);
	/** The phrase ended */
	void phraseStopped();
	/** The phrase was terminated abruptly */
	void phraseTerminated();
}
