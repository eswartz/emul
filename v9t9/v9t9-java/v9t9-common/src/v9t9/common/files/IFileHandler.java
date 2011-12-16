/**
 * 
 */
package v9t9.common.files;

import java.io.IOException;

import ejs.base.properties.IProperty;


/**
 * @author Ed
 *
 */
public interface IFileHandler {
	Catalog createCatalog(IProperty diskProperty, boolean isDiskImage) throws IOException;
}
