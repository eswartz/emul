/**
 * 
 */
package v9t9.common.dsr;


/**
 * @author ejs
 *
 */
public interface IPrinterImageListener {

	/** either BufferedImage or Image */
	void newPage(Object image);
	/** either BufferedImage or Image */
	void updated(Object image);
}
