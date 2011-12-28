/**
 * 
 */
package v9t9.common.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * This class assists in locating files along paths (which are {@link IProperty} instances
 * that contain lists of Strings).  This allows e.g. user configuration files
 * to reference bare filenames -- assuming these are unique no matter where they
 * live -- without performing path lookups themselves. 
 * @author Ed
 *
 */
public class PathFileLocator {
	
	private List<IProperty> roPathProperties = new ArrayList<IProperty>();
	private IProperty rwPathProperty = null;
	private IPropertyListener pathListChangedListener;

	private static final URI MISSING = URI.create("missing:/");

	private Set<String> potentiallyWriteableFiles = new HashSet<String>();
	
	private int lastCachedHash;
	private Map<String, URI> cachedLookups = new HashMap<String, URI>();
	private Set<URI> cachedSlowURIs = new HashSet<URI>();
	private URI[] cachedURIs = new URI[0];
	private URI cachedWriteURI = null;
	
	private Map<URI, List<String>> cachedListings = new HashMap<URI, List<String>>();
	private Map<URI, Long> cachedListingTime = new HashMap<URI, Long>();
	
	private int timeoutMs = 30 * 1000;
	
	/**
	 * 
	 */
	public PathFileLocator() {
		pathListChangedListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				synchronized (PathFileLocator.this) {
					cachedLookups.clear();
					cachePaths();
				}
			}
		};
	}
	
	public synchronized void addReadOnlyPathProperty(IProperty property) {
		roPathProperties.add(property);
		
		property.addListenerAndFire(pathListChangedListener);
	}
	
	public synchronized void setReadWritePathProperty(IProperty property) {
		if (rwPathProperty == property)
			return;
		
		if (property.getValue() instanceof Collection)
			throw new IllegalArgumentException("read/write path must be singular");
		
		if (rwPathProperty != null)
			rwPathProperty.removeListener(pathListChangedListener);
			
		rwPathProperty = property;
		property.addListenerAndFire(pathListChangedListener);
	}
	
	interface IPathIterator {
		void handle(String path);
	}
	
	protected void cachePaths() {
		final Set<URI> uris = new LinkedHashSet<URI>();
		
		lastCachedHash = 0;
		cachedSlowURIs.clear();
		cachedLookups.clear();
		cachedListings.clear();
		cachedListingTime.clear();
		
		iteratePaths(new IPathIterator() {

			@Override
			public void handle(String path) {
				// ensure all act like directories
				if (!path.endsWith("/"))
					path += "/";
				
				try {
					URI uri = new URI(path);
					if (!uri.isAbsolute()) {
						uri = new URI("file", path, null);
					}
					uris.add(uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			
		});
		
		cachedURIs = uris.toArray(new URI[uris.size()]);
		
		lastCachedHash = calculateCachedHash();
		
		cachedWriteURI = null;
		if (rwPathProperty != null) {
			String path = rwPathProperty.getValue().toString();
			if (!path.endsWith("/"))
				path += "/";
			try {
				cachedWriteURI = new URI("file", path, null);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param iPathIterator
	 */
	private void iteratePaths(IPathIterator iterator) {

		if (rwPathProperty != null) {
			Object val = rwPathProperty.getValue();
			if (val != null)
				iterator.handle(val.toString());
		}
		
		for (IProperty prop : roPathProperties) {
			List<Object> propPaths = prop.getList();
			
			for (Object propPath : propPaths) {
				if (propPath != null)
					iterator.handle(propPath.toString());
			}
		}
				
	}
	
	/**
	 * Cache the contents of properties (and/or their lists)
	 * to quickly detect if they have changed.  This augments
	 * the {@link IPropertyListener} already attached to the
	 * properties and serves to detect changes happening inside
	 * list properties.
	 * @return new hash code for current paths
	 */
	protected int calculateCachedHash() {
		final int[] code = { 0 };

		iteratePaths(new IPathIterator() {

			@Override
			public void handle(String path) {
				code[0] ^= path.hashCode();
				code[0] += 1000;
			}
			
		});
		
		return code[0];
	}

	/**
	 * Get all the active paths along the read-write and read-only
	 * path properties, with the read-write path (if any) first.
	 * @return
	 */
	public synchronized URI[] getSearchURIs() {
		// lists do not maintain inner listeners
		if (calculateCachedHash() != lastCachedHash) {
			cachePaths();
		}
		return cachedURIs;
	}
	
	public void setConnectionTimeout(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
	
	/**
	 * @return the timeoutMs
	 */
	public int getTimeoutMs() {
		return timeoutMs;
	}
	
	/**
	 * Find an existing file along the search paths
	 * @param file
	 * @return <code>null</code> if no match is found
	 * @throws IllegalArgumentException if file contributes invalid URI segment
	 */
	public synchronized URI findFile(String file) {
		URI[] searchURIs = getSearchURIs();
		
		URI uri = null;
		
		if (!potentiallyWriteableFiles.contains(file)) { 
			uri = cachedLookups.get(file);
			if (uri == MISSING)
				return null;
		}
		
		if (uri != null)
			return uri;
		
		for (URI baseUri : searchURIs) {
			if (cachedSlowURIs.contains(baseUri))
				continue;
			
			uri = baseUri.resolve(file);
		
			List<String> listing;
			try {
				listing = getDirectoryListing(uri);
				if (listing.contains(file)) {
					return uri;
				}
			} catch (IOException e) {
			}
		}
		
		cachedLookups.put(file, MISSING);
		return null;
	}
	
	
	/**
	 * Get the listing of entries in this URI 
	 * @param uri
	 * @return list of filenames
	 */
	public List<String> getDirectoryListing(URI uri) throws IOException {
		
		URI directory = uri.resolve(".");
		List<String> cachedListing = cachedListings.get(directory);
		
		// read from directory, not file
		URLConnection connection;
		try {
			connection = connect(directory, false);
		} catch (SocketTimeoutException e) {
			cachedSlowURIs.add(directory);
			throw e;
		}
		
		if (!connection.getContentType().equals("text/plain")) {
			throw new IOException("unexpected content at " + directory);
		}
			
		long time = connection.getLastModified();

		if (cachedListing == null || directory.equals(cachedWriteURI) || time != cachedListingTime.get(directory)) {
			InputStream is = connection.getInputStream();
			try {
				String content = DataFiles.readInputStreamText(is);
				String[] entries = content.split("\r\n|\n");
				cachedListing = Arrays.asList(entries);
			} finally {
				if (is != null) { 
					try { is.close(); } catch (IOException e) {} 
				}
			}
			
			time = connection.getLastModified();
			
			cachedListings.put(directory, cachedListing);
			cachedListingTime.put(directory, time);
		} 

		
		return cachedListing;
	}
	

	protected URLConnection connect(URI uri) throws IOException,
			MalformedURLException {
		return connect(uri, true);
	}

	protected URLConnection connect(URI uri, boolean useCache) throws IOException,
		MalformedURLException {
		URLConnection connection = uri.toURL().openConnection();
		configureConnection(connection, useCache);
		connection.connect();
		return connection;
	}

	/**
	 * Open a (new) input stream to the URI
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public InputStream createInputStream(URI uri) throws IOException {

		InputStream is = null; 
		URLConnection connection = connect(uri);
		is = connection.getInputStream();
		
		return is;
	}

	

	/**
	 * Get the size of the content at the URI
	 * @param uri
	 * @return size in bytes
	 * @throws IOException
	 */
	public int getContentLength(URI uri) throws IOException {
		URLConnection connection = connect(uri);
		return connection.getContentLength();
	}
	
	/**
	 * @param connection
	 * @param useCache 
	 */
	private void configureConnection(URLConnection connection, boolean useCache) {
		connection.setConnectTimeout(timeoutMs);
		connection.setUseCaches(useCache);
	}

	/**
	 * Attempt to open a file existing at the given URI 
	 * @param uri
	 * @return connection or <code>null</code>
	 */
	public URLConnection openConnection(URI uri) {
		try {
			return uri.toURL().openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return null;
	}


	/**
	 * Get a URI which supports writing the file.  This relies only on 
	 * the read/write path property.
	 * 
	 * @param file
	 * @return URI for file or <code>null</code> if none can be found
	 */
	public URI getWriteURI(String file) {
		if (cachedWriteURI == null)
			return null;

		// assume the user will create it
		cachedLookups.remove(file);
		potentiallyWriteableFiles.add(file);
		
		return cachedWriteURI.resolve(file);
	}
	
	public static void main(String[] args) throws URISyntaxException, IOException {
		URL url = new URL("file:///home/ejs/path/to/files/");
		URI root = url.toURI();
		System.out.println(root);
		URI same = root.resolve(".");
		System.out.println(same);
		URI parent = root.resolve("..");
		System.out.println(parent);
		URI parentParent = root.resolve("../../");
		if (parentParent.getScheme().equals("file"))
			System.out.println(new File(parentParent));
		
		System.out.println(url);
		System.out.println(new URL(url, ".."));
		System.out.println(new URL(url, "."));
		
		URI another = new URI("jar:///tmp/foo.jar!/in/and/out/");
		System.out.println(another);
		URI anotherParent = another.resolve("..");
		System.out.println(anotherParent);
		anotherParent = anotherParent.resolve("..");
		if (anotherParent.getScheme().equals("file"))
			System.out.println(new File(anotherParent));
		System.out.println(anotherParent);
		anotherParent = anotherParent.resolve("..");
		System.out.println(anotherParent);
		
		URI query = new URI("http://foo.bar.com/path/to/something/?query=bar");
		System.out.println(query);
		URI queryPlus = query.resolve("foo/la/dee");
		System.out.println(queryPlus);
		URI queryPlus2 = query.resolve("foo/la/dee" + "?"+ query.getQuery());
		System.out.println(queryPlus2);
		
		try {
			URL dir = new URL("file:/tmp");
			URLConnection connection = dir.openConnection();
			connection.connect();
			System.out.println(connection.getContentType());
			System.out.println(connection.getContentLength());
			InputStream is = connection.getInputStream();
			System.out.println(DataFiles.readInputStreamText(is));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create an output stream for the given URI.
	 * Unlike a direct use of {@link URLConnection#getOutputStream()}, this
	 * will handle file: schemas.
	 * @param uri
	 * @return
	 */
	public OutputStream createOutputStream(URI uri) throws IOException {
		URLConnection conn = uri.toURL().openConnection();
		OutputStream os;
		try {
			os = conn.getOutputStream();
		} catch (UnknownServiceException e) {
			// blah
			if (!uri.getScheme().equals("file")) 
				throw e;
			
			File file = new File(uri);
			os = new FileOutputStream(file);
		}
		return os;
	}

}
