/**
 * 
 */
package v9t9.common.speech;

public interface ISpeechDataSender {
	void sendSample(short val, int pos, int length);
	void speechDone();
}