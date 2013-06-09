/**
 * 
 */
package v9t9.common.dsr;

import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public interface ISelectableDsrHandler extends IDsrHandler {
	IProperty getSelectionProperty();
	IDsrHandler getCurrentDsr();
}
