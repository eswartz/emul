/**
 * 
 */
package v9t9.common.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collection;

import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public interface IPathFileLocator {

	void addReadOnlyPathProperty(IProperty property);

	void setReadWritePathProperty(IProperty property);

	/**
	 * @param path
	 * @return
	 * @throws URISyntaxException 
	 */
	URI createURI(String path) throws URISyntaxException;

	/**
	 * Get all the active paths along the read-write and read-only
	 * path properties, with the read-write path (if any) first.
	 * @return
	 */
	URI[] getSearchURIs();

	void setConnectionTimeout(int timeoutMs);

	/**
	 * @return the timeoutMs
	 */
	int getTimeoutMs();

	/**
	 * Find an existing file along the search paths
	 * @param file
	 * @return <code>null</code> if no match is found
	 * @throws IllegalArgumentException if file contributes invalid URI segment
	 */
	URI findFile(String file);

	/**
	 * Get the listing of entries in this URI 
	 * @param uri
	 * @return list of filenames
	 */
	Collection<String> getDirectoryListing(URI uri) throws IOException;

	/**
	 * Open a (new) input stream to the URI
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	InputStream createInputStream(URI uri) throws IOException;

	/**
	 * Get the size of the content at the URI
	 * @param uri
	 * @return size in bytes
	 * @throws IOException
	 */
	int getContentLength(URI uri) throws IOException;

	/**
	 * Attempt to open a file existing at the given URI 
	 * @param uri
	 * @return connection or <code>null</code>
	 */
	URLConnection openConnection(URI uri);

	/**
	 * Get a URI which supports writing the file.  This relies only on 
	 * the read/write path property.
	 * 
	 * @param file
	 * @return URI for file or <code>null</code> if none can be found
	 */
	URI getWriteURI(String file);

	/**
	 * Create an output stream for the given URI.
	 * Unlike a direct use of {@link URLConnection#getOutputStream()}, this
	 * will handle file: schemas.
	 * @param uri
	 * @return
	 */
	OutputStream createOutputStream(URI uri) throws IOException;

	/**
	 * @param uri
	 * @return
	 */
	boolean exists(URI uri);

	/**
	 * @return
	 */
	IProperty[] getSearchPathProperties();
	
	URI resolveInsideURI(URI uri, String string); 


}