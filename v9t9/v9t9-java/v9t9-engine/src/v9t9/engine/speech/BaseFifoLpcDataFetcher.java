/**
 * 
 */
package v9t9.engine.speech;

/**
 * @author ejs
 *
 */
public abstract class BaseFifoLpcDataFetcher extends BaseLpcDataFetcher implements IFifoLpcDataFetcher, ILPCByteFetcher {

	protected IFifoStatusListener listener;

	/**
	 * 
	 */
	public BaseFifoLpcDataFetcher() {
		super();
	}

	public void setListener(IFifoStatusListener listener) {
		this.listener = listener;
	}

}