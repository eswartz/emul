/**
 * 
 */
package v9t9.common.dsr;

import java.io.IOException;

import v9t9.base.properties.IProperty;
import v9t9.common.files.Catalog;

/**
 * @author ejs
 *
 */
public interface IDiskDsr {
	boolean isImageBased();
	Catalog getCatalog(IProperty diskSetting) throws IOException;
}
