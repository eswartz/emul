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
	 * @param horizDpi
	 * @param vertDpi
	 */
	void setDpi(int horizDpi, int vertDpi);

	/**
	 * Get the location of the print head vertically on the page, from 0.0 to 1.0
	 * @return
	 */
	double getPageRowPercentage();
	/**
	 * Get the location of the print head horizontally on the page, from 0.0 to 1.0
	 * @return
	 */
	double getPageColumnPercentage();

}