/**
 * 
 */
package v9t9.common.files;

import java.net.URI;

/**
 * @author Ed
 *
 */
public interface IResourceLocator {

	URI resolve(String filename);
	
}
