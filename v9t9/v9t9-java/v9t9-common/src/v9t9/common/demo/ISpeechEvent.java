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

	/** an LPC encoded byte (Byte) (legacy format) */
	int SPEECH_ADDING_BYTE = 1;
	
	/** terminating speech phrase */
	int SPEECH_TERMINATING = 2;

	/** finished naturally */
	int SPEECH_STOPPING = 3;

	/** LPC-encoded bytes representing a full equation
	 * (followed by length byte, then bytes) */
	int SPEECH_ADDING_EQUATION = 4;
	

	int getCode();

	/** data associated with event: SPEECH_ADDING_BYTE, SPEECH_ADDING_EQUATION */
	Object getData();
}
