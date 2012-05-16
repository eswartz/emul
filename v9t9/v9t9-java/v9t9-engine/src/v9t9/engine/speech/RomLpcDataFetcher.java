/**
 * 
 */
package v9t9.engine.speech;

/**
 * Read LPC data from speech ROM
 * @author ejs
 *
 */
class RomLpcDataFetcher extends BaseLpcDataFetcher {
	/**
	 * 
	 */
	public RomLpcDataFetcher(ILPCByteFetcher byteFetcher) {
		setByteFetcher(byteFetcher);
	}
	


}