/**
 * 
 */
package v9t9.engine.speech;

/**
 * @author ejs
 *
 */
public interface IFifoLpcDataFetcher extends ILPCDataFetcher {

	public interface IFifoStatusListener {
		void fetchedEmpty();
		void lengthChanged(int length);
	}

	void setListener(IFifoStatusListener listener);
	void write(byte val);
	void purge();
}
