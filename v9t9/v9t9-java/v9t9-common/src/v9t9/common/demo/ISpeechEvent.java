/**
 * 
 */
package v9t9.common.demo;


/**
 * @author ejs
 *
 */
public interface ISpeechEvent extends IDemoEvent {

	/** new phrase */
	int SPEECH_STARTING = 0;

	/** an LPC encoded byte (following) */
	int SPEECH_ADDING_BYTE = 1;
	
	/** terminating speech phrase */
	int SPEECH_TERMINATING = 2;

	/** finished naturally */
	int SPEECH_STOPPING = 3;
	
	int getCode();

	/** for SPEECH_ADDING_BYTE only */
	byte getAddedByte();
}
