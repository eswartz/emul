/**
 * 
 */
package v9t9.common.dsr;


/**
 * @author ejs
 *
 */
public interface IPrinterImageEngine {

	void addListener(IPrinterImageListener listener);

	void removeListener(IPrinterImageListener listener);

	void newPage();

	void print(char ch);
	void print(String text);

	/**
	 * 
	 */
	void flushPage();

	/**
	 * @param i
	 * @param j
	 */
	void setDpi(int i, int j);

}