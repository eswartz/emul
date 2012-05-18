/**
 * 
 */
package v9t9.common.demos;

import java.net.URI;

/**
 * @author ejs
 *
 */
public interface IDemo {

	/** get the full location of the *.dem */
	URI getURI();

	/** get the path holding the *.dem */
	URI getParentURI();

	/** get the filename */
	String getName();
	
	/** get a description */
	String getDescription();

}
