/**
 * 
 */
package v9t9.common.files;

import java.io.IOException;

import v9t9.base.properties.IProperty;

/**
 * @author Ed
 *
 */
public interface IFileHandler {
	Catalog createCatalog(IProperty diskProperty, boolean isDiskImage) throws IOException;
}
