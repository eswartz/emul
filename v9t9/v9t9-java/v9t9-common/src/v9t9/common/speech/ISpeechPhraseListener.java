/*
  ISpeechPhraseListener.java

  (c) 2012 Edward Swartz

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
