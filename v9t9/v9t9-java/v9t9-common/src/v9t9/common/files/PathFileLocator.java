/**
 * 
 */
package v9t9.common.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
public class PathFileLocator implements IPathFileLocator {

	private List<IProperty> roPathProperties = new ArrayList<IProperty>();
	private IProperty rwPathProperty = null;
	private IPropertyListener pathListChangedListener;

	private Set<String> potentiallyWriteableFiles = new HashSet<String>(2);
	
	private int lastCachedHash;
	private Set<URI> cachedSlowURIs = new HashSet<URI>();
	private URI[] cachedURIs = new URI[0];
	private URI cachedWriteURI = null;
	
	private Map<URI, Collection<String>> cachedListings = new HashMap<URI, Collection<String>>();
	private Map<URI, Long> cachedListingTime = new HashMap<URI, Long>();
	private Map<URI, Long> cachedListingModifiedTime = new HashMap<URI, Long>();
	
	private Map<URI, Map<String, Collection<String>>> cachedJarListings = new HashMap<URI, Map<String,Collection<String>>>();
	
	private int timeoutMs = 30 * 1000;
	
	/**
	 * 
	 */
	public PathFileLocator() {
		pathListChangedListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				synchronized (PathFileLocator.this) {
					cachePaths();
				}
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#addReadOnlyPathProperty(ejs.base.properties.IProperty)
	 */
	@Override
	public synchronized void addReadOnlyPathProperty(IProperty property) {
		roPathProperties.add(property);
		
		property.addListenerAndFire(pathListChangedListener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#setReadWritePathProperty(ejs.base.properties.IProperty)
	 */
	@Override
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
		void handle(IProperty property, String path);
	}
	
	protected void cachePaths() {
		final Set<URI> uris = new LinkedHashSet<URI>();
		
		lastCachedHash = 0;
		cachedSlowURIs.clear();
		cachedListings.clear();
		cachedListingTime.clear();
		cachedListingModifiedTime.clear();
		
		iteratePaths(new IPathIterator() {

			@Override
			public void handle(IProperty property, String path) {
				try {
					if (path == null || path.length() == 0)
						return;
					URI uri = createURI(path);
					if (!uri.isAbsolute())
						return;
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
			try {
				String path = rwPathProperty.getValue().toString();
				cachedWriteURI = createURI(path);
				if (!cachedWriteURI.isAbsolute()) {
					cachedWriteURI = null;
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#createURI(java.lang.String)
	 */
	@Override
	public URI createURI(final String path_) throws URISyntaxException {
		// convert slashes
		String path = path_.replace('\\', '/');
		
		// ensure all act like directories
		if (!path.contains(":/") || path.indexOf(":/") == 1) {
			path = new File(path).getAbsolutePath();
		}
		if (!path.endsWith("/"))
			path += "/";
		
		URI uri = new URI(path);
		if (uri.getScheme() == null) {
			uri = new URI("file", path, null);
		}

		return uri;
	}

	/**
	 * @param iPathIterator
	 */
	private void iteratePaths(IPathIterator iterator) {

		if (rwPathProperty != null) {
			Object val = rwPathProperty.getValue();
			if (val != null)
				iterator.handle(rwPathProperty, val.toString());
		}
		
		for (IProperty prop : roPathProperties) {
			List<Object> propPaths = prop.getList();
			
			for (Object propPath : propPaths) {
				if (propPath != null)
					iterator.handle(prop, propPath.toString());
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
			public void handle(IProperty property, String path) {
				code[0] ^= path.hashCode();
				code[0] += 1000;
			}
			
		});
		
		return code[0];
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getSearchURIs()
	 */
	@Override
	public synchronized URI[] getSearchURIs() {
		// lists do not maintain inner listeners
		if (calculateCachedHash() != lastCachedHash) {
			cachePaths();
		}
		return cachedURIs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#setConnectionTimeout(int)
	 */
	@Override
	public void setConnectionTimeout(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getTimeoutMs()
	 */
	@Override
	public int getTimeoutMs() {
		return timeoutMs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#findFile(java.lang.String)
	 */
	@Override
	public synchronized URI findFile(String file) {
		if (file == null)
			return null;
		
		File localFile = new File(file);
		if (localFile.isAbsolute())
			return localFile.toURI();
		
		URI[] searchURIs = getSearchURIs();
		
		URI uri = null;
		
		for (URI baseUri : searchURIs) {
			if (cachedSlowURIs.contains(baseUri))
				continue;
			
			uri = resolveInsideURI(baseUri, file);
		
			Collection<String> listing;
			try {
				listing = getDirectoryListing(uri);
				if (listing.contains(file)) {
					return uri;
				}
			} catch (IOException e) {
			}
		}
		
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getDirectoryListing(java.net.URI)
	 */
	@Override
	public synchronized Collection<String> getDirectoryListing(URI uri) throws IOException {
		
		// read from directory, not file
		URI directory = resolveInsideURI(uri, ".");
		if (directory == null)
			throw new IOException("cannot read directory in " + uri);
		
		Collection<String> cachedListing = cachedListings.get(directory);
		
		// don't update anything more than once a second
		if (cachedListing != null && !directory.equals(rwPathProperty) 
				&& (!"jar".equals(directory.getScheme())
						&& cachedListingTime.get(directory) + 1 * 1000 > System.currentTimeMillis())) {
			return cachedListing;
		}
		
		// do the hard work
		long time;
		URLConnection connection = null;
		try {
			connection = connect(directory, false);
		} catch (SocketTimeoutException e) {
			cachedSlowURIs.add(directory);
			throw e;
		}

		// JAR?
		if (connection instanceof JarURLConnection) {
			try {
				cachedListing = getJarDirectoryListing(((JarURLConnection) connection).getJarFileURL().toURI(),
						directory.getSchemeSpecificPart().substring(directory.getSchemeSpecificPart().lastIndexOf('!') + 1));

				cachedListings.put(directory, cachedListing);
				cachedListingModifiedTime.put(directory, connection.getLastModified());
			} catch (URISyntaxException e) {
				throw new IOException("failed to read inside JAR", e);
			}
		}
		else  {
			if (!connection.getContentType().equals("text/plain")) {
				throw new IOException("unexpected content at " + directory);
			}
		
			// only re-read if the directory changed
			time = connection.getLastModified();
	
			if (cachedListing == null || directory.equals(cachedWriteURI) || time != cachedListingModifiedTime.get(directory)) {
				String[] entries;
				InputStream is = connection.getInputStream();
				try {
					String content = DataFiles.readInputStreamTextAndClose(is);
					entries = content.split("\r\n|\n");
				} catch (IOException e) {
					throw e;
				} finally {
					if (is != null) { 
						try { is.close(); } catch (IOException e) {} 
					}
				}
				
				time = connection.getLastModified();
				
				cachedListing = new HashSet<String>(Arrays.asList(entries));
				
				cachedListings.put(directory, cachedListing);
				cachedListingModifiedTime.put(directory, time);
			} 
		}
		

		cachedListingTime.put(directory, System.currentTimeMillis());
		
		return cachedListing;
	}
	

	/**
	 * Fetch a directory listing from a JAR (ZIP) file and cache all the
	 * directories underneath, and give them infinite timeouts (since JARs are read-only) 
	 * @param jar
	 * @param substring
	 * @return
	 */
	private Collection<String> getJarDirectoryListing(URI jar, String dir) throws IOException {
		File file;
		try {
			file = new File(jar);
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
		
		if (dir.startsWith("/"))
			dir = dir.substring(1);
		if (dir.endsWith("/"))
			dir = dir.substring(0, dir.length() - 1);
		
		Map<String, Collection<String>> subdirs = cachedJarListings.get(jar);
		
		if (subdirs == null) {
			subdirs = new TreeMap<String, Collection<String>>();
			
			ZipFile zf = new ZipFile(file);
			for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();) {
				ZipEntry ent = e.nextElement();
				
				File entFile = new File(ent.getName());
				String entDir = entFile.getParent();
				if (entDir == null)
					entDir = "";
				Collection<String> directory = subdirs.get(entDir);
				if (directory == null) {
					directory = new ArrayList<String>();
					subdirs.put(entDir, directory);
				}
				
				// ignore class files
				if (!entFile.getName().endsWith(".class"))
					directory.add(entFile.getName());
			}
			zf.close();
			
			cachedJarListings.put(jar, subdirs);
		}
		
		return subdirs.get(dir);
	}

	/**
	 * @param uri
	 * @param string
	 * @return URI or <code>null</code>
	 */
	public URI resolveInsideURI(URI uri, String string) {
		URI resolved = null;
		if (!uri.isOpaque()) {
			resolved = uri.resolve(string);
		} else {
			try {
				// urgh, resolving inside these kinds of URLs does not strip the non-directory suffix
				// automagically; do it manually
				String ssp = uri.getSchemeSpecificPart();
				if (!ssp.endsWith("/")) {
					int idx = ssp.lastIndexOf("/");
					if (idx >= 0)
						ssp = ssp.substring(0, idx + 1);
				}
				resolved = createURI(ssp).resolve(string);
				resolved = new URI(uri.getScheme(), resolved.toString(), uri.getFragment());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		

		return resolved;
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

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#createInputStream(java.net.URI)
	 */
	@Override
	public InputStream createInputStream(URI uri) throws IOException {

		InputStream is = null; 
		URLConnection connection = connect(uri);
		is = connection.getInputStream();
		
		return is;
	}

	

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getContentLength(java.net.URI)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#openConnection(java.net.URI)
	 */
	@Override
	public URLConnection openConnection(URI uri) {
		try {
			return uri.toURL().openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getWriteURI(java.lang.String)
	 */
	@Override
	public URI getWriteURI(String file) {
		if (cachedWriteURI == null)
			return null;

		// assume the user will create it
		potentiallyWriteableFiles.add(file);
		
		return resolveInsideURI(cachedWriteURI, file);
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
			System.out.println(DataFiles.readInputStreamTextAndClose(is));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#createOutputStream(java.net.URI)
	 */
	@Override
	public synchronized OutputStream createOutputStream(URI uri) throws IOException {
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
		
		cachedListings.remove(uri.resolve("."));
		return os;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#exists(java.net.URI)
	 */
	@Override
	public boolean exists(URI uri) {
		InputStream is = null;
		try {
			is = createInputStream(uri);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getSearchPathProperties()
	 */
	@Override
	public IProperty[] getSearchPathProperties() {
		List<IProperty> props = new ArrayList<IProperty>();
		if (rwPathProperty != null)
			props.add(rwPathProperty);
		props.addAll(roPathProperties);
		return (IProperty[]) props.toArray(new IProperty[props.size()]);
	}

}
