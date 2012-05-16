/**
 * 
 */
package v9t9.common.speech;

/**
 * @author ejs
 *
 */
public interface ISpeechPhraseListener {

	/** A new phrase started */
	void phraseStarted();
	/** A byte (LPC encoded) was added to the phrase */
	void phraseByteAdded(byte byt);
	/** The phrase ended */
	void phraseStopped();
	/** The phrase was terminated abruptly */
	void phraseTerminated();
}
