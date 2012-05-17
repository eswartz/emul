/**
 * 
 */
package v9t9.common.speech;

/**
 * Listener for the mathematical details of speech, from the
 * LPC decoding point of view.  Each callback indicates that
 * the given parameters were converted to speech.
 * @author ejs
 *
 */
public interface ILPCParametersListener {

	void parametersAdded(ILPCParameters params);
}
