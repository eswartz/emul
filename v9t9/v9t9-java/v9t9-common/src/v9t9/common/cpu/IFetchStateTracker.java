/**
 * 
 */
package v9t9.common.cpu;

/**
 * Track CPU state while instructions are being fetched
 * @author ejs
 *
 */
public interface IFetchStateTracker {
	IFetchStateTracker getParent();
	void setParent(IFetchStateTracker parent);
	
	int fetchRegister(int reg);
	short fetchWord(int addr);
	byte fetchByte(int addr);
}
