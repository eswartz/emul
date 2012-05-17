/**
 * 
 */
package v9t9.engine.speech;

import v9t9.common.speech.ILPCParameters;

/**
 * Provide LPC equation(s) for the LPC engine.
 * @author ejs
 *
 */
public interface ILPCEquationFetcher {

	/**
	 * Fetch an equation  
	 * @param fetcher TODO
	 * @param params parameters to fill in
	 */
	void fetchEquation(ILPCDataFetcher fetcher, ILPCParameters params);
}
