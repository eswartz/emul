/**
 * 
 */
package v9t9.common.dsr;

/**
 * @author ejs
 *
 */
public interface IPrinterHtmlHandler {

	void addListener(IPrinterHtmlListener listener);

	void removeListener(IPrinterHtmlListener listener);

}