/*
  PathFileLocator.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.MemoryEntryInfo;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.utils.FileUtils;
import ejs.base.utils.ListenerList;
import ejs.base.utils.Pair;

/**
 * This class assists in locating files along paths (which are {@link IProperty} instances
 * that contain lists of Strings).  This allows e.g. user configuration files
 * to reference bare filenames -- assuming these are unique no matter where they
 * live -- without performing path lookups themselves. 
 * @author Ed
 *
 */
public class PathFileLocator implements IPathFileLocator {

	private static final Logger logger = Logger.getLogger(PathFileLocator.class);
	
	/**
	 * Files beyond this size are not queried for MD5
	 */
	private static final int MAX_FILE_SIZE = 16 * 1024 * 1024;

	private List<IProperty> roPathProperties = new ArrayList<IProperty>();
	private IProperty rwPathProperty = null;
	private IPropertyListener pathListChangedListener;

	private Set<String> potentiallyWriteableFiles = new HashSet<String>(2);
	
	private int lastCachedHash;
	private Set<URI> cachedSlowURIs = new HashSet<URI>();
	private URI[] cachedURIs = new URI[0];
	private URI cachedWriteURI = null;
	
	private Map<URI, Map<String, FileInfo>> cachedListings = new HashMap<URI, Map<String, FileInfo>>();
	private Map<URI, Long> cachedListingTime = new HashMap<URI, Long>();
	private Map<URI, Long> cachedListingModifiedTime = new HashMap<URI, Long>();
	
//	private Map<URL, Map<String, Collection<String>>> cachedJarListings = new HashMap<URL, Map<String,Collection<String>>>();
	
	private Map<URI, Map<String, String>> cachedUriToMD5Hashes = new HashMap<URI, Map<String,String>>();
	private Map<String, URI> cachedMD5HashesToURIs = new LinkedHashMap<String, URI>();
	
	private int timeoutMs = 30 * 1000;

	private ListenerList<IPathChangeListener> listeners = new ListenerList<IPathFileLocator.IPathChangeListener>();

	private static Set<String> sReportedMissing = new HashSet<String>();
	
	/**
	 * 
	 */
	public PathFileLocator() {
		pathListChangedListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				synchronized (PathFileLocator.this) {
					cachePaths();
					for (IPathChangeListener listener : listeners) {
						listener.pathsChanged();
					}
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#resetPathProperties()
	 */
	@Override
	public synchronized void resetPathProperties() {
		if (rwPathProperty != null)
			rwPathProperty.removeListener(pathListChangedListener);
		
		rwPathProperty = null;
		
		for (IProperty prop : roPathProperties) {
			prop.removeListener(pathListChangedListener);
		}
		roPathProperties.clear();
		
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
		cachedUriToMD5Hashes.clear();
		cachedMD5HashesToURIs.clear();
		
		iteratePaths(new IPathIterator() {

			@Override
			public void handle(IProperty property, String path) {
				try {
					if (path == null || path.length() == 0)
						return;
					URI uri = createURI(path);
					if (!uri.isAbsolute())
						return;
					logger.info("Adding URI " + uri);
					uris.add(uri);
					
					try {
						getDirectoryListing(uri);
					} catch (IOException e) {
						logger.error("could not cache directory for " + uri, e);
					}
				} catch (URISyntaxException e) {
					logger.error("URI syntax on " + path, e);
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
				logger.error("URI syntax on " + rwPathProperty, e);
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#createURI(java.lang.String)
	 */
	@Override
	public URI createURI(String path) throws URISyntaxException {
		// convert slashes
		path = path.replace('\\', '/');

		// ensure all act like directories
		if (!path.contains(":/") || path.indexOf(":/") == 1) {
			int partIdx = path.indexOf("file:");
			String pathPart = partIdx >= 0 ? path.substring(partIdx+5) : path;
			
			File file = new File(pathPart);
			String uriPath = file.getAbsolutePath();
			// convert slashes
			uriPath = uriPath.replace('\\', '/');

			// windows
			if (new File(uriPath).isAbsolute() && !uriPath.startsWith("/"))
				uriPath = "/" + uriPath;

			uriPath = partIdx >= 0 ? path.substring(0, partIdx + 5) + uriPath : uriPath;
			
			path = uriPath;
		}
		else {
			// windows
			if (new File(path).isAbsolute() && !path.startsWith("/"))
				path = "/" + path;
		}
		
		// convert bad chars
		path = path.replace(" ", "%20");
		
		if (!path.endsWith("/"))
			path += "/";
		
		URI uri = new URI(path);
		if (uri.getScheme() == null) {
			uri = new URI("file", uri.getPath(), null);
		}

		logger.debug("URI created from " + path + " as " + uri);
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

		if (file.contains("/")) {
			try {
				URI resolved = URI.create(file);
				if (resolved.isOpaque() || resolved.isAbsolute())
					return resolved;
			} catch (IllegalArgumentException e) {
				
			}
		}

		URI[] searchURIs = getSearchURIs();
		
		URI uri = null;
		
		logger.info("searching for " + file);
		for (URI baseUri : searchURIs) {
			logger.debug("searching " + file + " on " + baseUri);
			if (cachedSlowURIs.contains(baseUri))
				continue;
			
			uri = resolveInsideURI(baseUri, file);
			if (uri == null) {
				logger.error("failed to get listing from " + baseUri + " for " + file + "; got " + uri);
				return null;
			}
			
			int idx = uri.toString().lastIndexOf('/');
			String baseFile = uri.toString().substring(idx+1); 
			//logger.debug("\t" +uri + " base = " + baseFile);
		
			URI dirURI = URI.create(uri.toString().substring(0, idx+1));
			Collection<String> listing;
			try {
				listing = getDirectoryListing(dirURI).keySet();
				if (listing.contains(baseFile)) {
					logger.info("\tfound in " + dirURI + "; listing: " + listing.size() + " entries");
					return uri;
				}
			} catch (IOException e) {
				if (sReportedMissing.add(dirURI.toString())) {
					logger.error("failed to get listing from " + dirURI, e);
				} else {
					logger.debug("failed to get listing from " + dirURI);
				}
				if (false == e instanceof FileNotFoundException)
					e.printStackTrace();
			}
		}
		
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getDirectoryListing(java.net.URI)
	 */
	@Override
	public Map<String, FileInfo> getDirectoryListing(URI uri) throws IOException {
		
		// read from directory, not file
		URI directory = resolveInsideURI(uri, ".");
		if (directory == null)
			throw new IOException("cannot read directory in " + uri);
		
		Map<String, FileInfo> cachedListing;
		synchronized (this) {
			cachedListing = cachedListings.get(directory);
		}
		
		if (cachedListing != null && !directory.equals(rwPathProperty)) {
			return cachedListing;
//			// never update JAR cache
//			if ("jar".equals(directory.getScheme())) 
//				return cachedListing;
//			
//			// don't update cache more than twice a minute
//			Long listingTime = cachedListingTime.get(directory);
//			if (listingTime != null && listingTime + 1 * 30*1000 > System.currentTimeMillis()) {
//				return cachedListing;
//			}
		}
		
		Pair<Long, Map<String, FileInfo>> info = fetchDirectoryListing(directory);
		long time = info.first;
		cachedListing = info.second;
		synchronized (this) {
			cachedListings.put(directory, cachedListing);
			cachedListingModifiedTime.put(directory, time);
			cachedListingTime.put(directory, System.currentTimeMillis());
			
			for (Map.Entry<String, FileInfo> ent : info.second.entrySet()) {
				if (ent.getKey().toLowerCase().endsWith(".bin")) {
					// cache reverse mapping too
					FileInfo finfo = ent.getValue();
					registerURIForMd5Key(finfo.uri, finfo.md5, 0, (int) finfo.length);
					registerURIForMd5Key(finfo.uri, finfo.md5, 0, -1);
				}
			}
		}		
		
		return cachedListing;
	}

	/**
	 * @param uri
	 * @param mdKey
	 */
	protected void registerURIForMd5Key(URI uri, String md5, int offset, int length) {
		Map<String, String> md5Map = cachedUriToMD5Hashes.get(uri);
		if (md5Map == null) {
			md5Map = new LinkedHashMap<String, String>();
			cachedUriToMD5Hashes.put(uri, md5Map);
		}
		String uriKey = getURIKey(uri, offset, length);
		md5Map.put(uriKey, md5);
		
		String mdKey = getMd5Key(md5, offset, length);
		cachedMD5HashesToURIs.put(mdKey, uri);
	}

	/**
	 * @param uri
	 * @param directory
	 * @param cachedListing
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws SocketTimeoutException
	 */
	protected Pair<Long, Map<String, FileInfo>> fetchDirectoryListing(URI directory) throws IOException,
			MalformedURLException, SocketTimeoutException {
		
		logger.info("\tfetching listing for " + directory);
		Map<String, FileInfo> cachedListing = null;

		// skip JarFile access which just leaks files
		if (directory.getScheme().equals("jar")) {
			String ssp = directory.getSchemeSpecificPart();
			if (ssp == null)
				ssp = directory.getPath();
			int dirIdx = ssp.lastIndexOf('!');
			String zipPath = ssp.substring(0, dirIdx);
			String entPrefix = ssp.substring(dirIdx + 2);	// skip '!' and '/'
			ZipFile zf;
			try {
				logger.info("reading zip file " + directory);
				File file = new File(URI.create(zipPath));
				zf = new ZipFile(file);
				try {
					cachedListing = new LinkedHashMap<String, IPathFileLocator.FileInfo>();
					for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements(); ) {
						ZipEntry entry = en.nextElement();
						
						String entPath = entry.getName();
						// get only entries under the desired directory...
						if (entPath.length() > entPrefix.length() && entPath.startsWith(entPrefix)) {
							String entSuffix = entPath.substring(entPrefix.length());
							int slashIdx = entSuffix.indexOf('/');
							String name;
							if (slashIdx < 0) {
								// and not in a deeper directory
								name = entSuffix;
							} else if (slashIdx == entSuffix.length() - 1) {
								// directory entry
								name = entSuffix.substring(0, slashIdx - 1);
							} else {
								continue;
							}
							URI fileURI;
							fileURI = resolveInsideURI(directory, name);
							String md5 = "";
							
							if (!entry.isDirectory() && entry.getSize() <= MAX_FILE_SIZE) {
								InputStream is = null;
								try {
									is = zf.getInputStream(entry);
									byte[] content = FileUtils.readInputStreamContentsAndClose(is, (int) entry.getSize()); 
									md5 = FileUtils.getMD5Hash(content);
								} catch (EOFException e) {
									md5 = "";
								} finally {
									if (is != null) { try { is.close(); } catch (IOException e) { } }
								}
							}
							
							FileInfo info = new FileInfo(fileURI, 
									entry.getTime(), 
									md5);
							cachedListing.put(name, info);
						}
					}
					
//					synchronized (this) {
//						cachedListings.put(directory, cachedListing);
//						cachedListingModifiedTime.put(directory, file.lastModified());
//						cachedListingTime.put(directory, System.currentTimeMillis());
//					}
					
					logger.info("\tlisting: " + cachedListing.size() + " entries");
					return new Pair<Long, Map<String,FileInfo>>(file.lastModified(), cachedListing);
				} finally {
					zf.close();
				}
				
			} catch (IllegalArgumentException e) {
				// ok, need to try harder below
				logger.error("\tfailed:", e);
				throw new IOException("URI listing failed: " + directory, e);
			}
		}
		
		File dir = null;
		if (directory.getScheme() == null)
			dir = new File(directory.getPath());
		else if (directory.getScheme().equals("file"))
			dir = new File(directory.getSchemeSpecificPart());
		else
			throw new IOException("URI listing not supported: " + directory);
		
		File[] files = dir.listFiles();
		cachedListing = new HashMap<String, IPathFileLocator.FileInfo>();
		if (files != null) {
			for (File file : files) {
				String md5 = "";
				long length = 0;
				if (file.isFile()) {
					length = file.length();
					if (length <= MAX_FILE_SIZE) {
						InputStream is = null;
						try {
							is = new BufferedInputStream(new FileInputStream(file));
							byte[] content = FileUtils.readInputStreamContentsAndClose(is, (int) length); 
							md5 = FileUtils.getMD5Hash(content);
						} catch (IOException e) {
							md5 = "";
						} finally {
							if (is != null) { try { is.close(); } catch (IOException e) { } }
						}
					}
				}
				URI fileURI;
				fileURI = file.toURI();
				cachedListing.put(file.getName(), new FileInfo(
						fileURI,
						length,
						md5));
			}
		}
		
		return new Pair<Long, Map<String,FileInfo>>(dir.lastModified(), cachedListing);
		
//		logger.info("\tok, trying URL instead");
//		// do the hard work
//		long time;
//		URLConnection connection = null;
//		try {
//			connection = connect(directory, false);
//		} catch (SocketTimeoutException e) {
//			synchronized (this) {
//				cachedSlowURIs.add(directory);
//			}
//			throw e;
//		}
//
//		// JAR?
//		if (connection instanceof JarURLConnection) {
//			String ssp = directory.getSchemeSpecificPart();
//			if (ssp == null)
//				ssp = directory.getPath();
//			cachedListing = getJarDirectoryListing(directory.toURL(),
//					ssp.substring(ssp.lastIndexOf('!') + 1));
//
//			synchronized (this) {
//				cachedListings.put(directory, cachedListing);
//				cachedListingModifiedTime.put(directory, connection.getLastModified());
//			}
//		}
//		else  {
//			if (!connection.getContentType().equals("text/plain")) {
//				logger.error("!!! Unexpected directory content at " + directory + ":  " +
//							connection.getContentType());
//
//				throw new IOException("unexpected content at " + directory);
//			}
//		
//			// only re-read if the directory changed
//			time = connection.getLastModified();
//	
//			synchronized (this) {
//				if (cachedListing == null
//						|| directory.equals(cachedWriteURI) 
//						|| ! ((Long) time).equals(cachedListingModifiedTime.get(directory))) {
//					String[] entries;
//					InputStream is = connection.getInputStream();
//					try {
//						String content = FileUtils.readInputStreamTextAndClose(is);
//						entries = content.split("\r\n|\n");
//					} catch (IOException e) {
//						throw e;
//					}
//					
//					time = connection.getLastModified();
//					
//					cachedListing = new HashSet<String>(Arrays.asList(entries));
//					
//					cachedListings.put(directory, cachedListing);
//					cachedListingModifiedTime.put(directory, time);
//				}
//			}
//		}
//		
//		synchronized (this) {
//			cachedListingTime.put(directory, System.currentTimeMillis());
//			logger.info("\tlisting: " + cachedListing.size() + " entries");
//		}
//		return cachedListing;
	}
	

	/**
	 * Fetch a directory listing from a JAR (ZIP) file and cache all the
	 * directories underneath, and give them infinite timeouts (since JARs are read-only) 
	 * @param jar
	 * @param substring
	 * @return
	 */
//	private Map<String, FileInfo> getJarDirectoryListing(URL jar, String dir) throws IOException {
//		
//		if (dir.startsWith("/"))
//			dir = dir.substring(1);
//		if (dir.endsWith("/"))
//			dir = dir.substring(0, dir.length() - 1);
//		
//		Map<String, Collection<String>> subdirs = cachedJarListings.get(jar);
//		
//		if (subdirs == null) {
//			subdirs = new TreeMap<String, Collection<String>>();
//
//			URL localJar = resolveToOwningZipFile(jar);
//			
//			boolean delete = false;
//			File file;
//			try {
//				file = new File(localJar.toURI());
//			} catch (IllegalArgumentException e) {
//				// okay, not a file... urgh... download it?
//				logger.info("Getting local copy of jar at " + jar + " for listing");
//
//				// TODO: sigh... if it's http://, we must have already downloaded it via Java Web Start -- how to find the cache?
//				File tmpFile = File.createTempFile("jar", ".jar");
//
//				InputStream is = jar.openStream();
//				byte[] content = FileUtils.readInputStreamContentsAndClose(is);
//				OutputStream os = new FileOutputStream(tmpFile);
//				FileUtils.writeOutputStreamContentsAndClose(os, content, content.length);
//				
//				file = tmpFile;
//				delete = true;
//			} catch (URISyntaxException e) {
//				throw new IOException(e);
//			}
//			
//			ZipFile zf = new ZipFile(file);
//			for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();) {
//				ZipEntry ent = e.nextElement();
//				
//				File entFile = new File(ent.getName());
//				String entDir = entFile.getParent();
//				if (entDir == null)
//					entDir = "";
//				else
//					entDir = entDir.replace('\\', '/');  /// Windows
//				Collection<String> directory = subdirs.get(entDir);
//				if (directory == null) {
//					directory = new ArrayList<String>();
//					subdirs.put(entDir, directory);
//				}
//				
//				directory.add(entFile.getName());
//			}
//			zf.close();
//			
//			if (delete)
//				file.delete();
//			
//			cachedJarListings.put(jar, subdirs);
//		}
//		
//		Collection<String> list = subdirs.get(dir);
//		if (list == null)
//			list = Collections.emptyList();
//		return list;
//	}

	/**
	 * Get the local zip file that provides the given URL
	 * @param zip
	 * @return
	 */
//	private URL resolveToOwningZipFile(URL zip) {
//		String baseURL = zip.toExternalForm();
//		int idx = baseURL.lastIndexOf('!');
//		if (idx >= 0)
//			baseURL = baseURL.substring(0, idx) + "!/";
//		URL localJar;
//		try {
//			localJar = resolveToLocalJarFile(new URL(baseURL));
//		} catch (MalformedURLException e) {
//			logger.error("malformed URL for " + baseURL, e);
//			e.printStackTrace();
//			return zip;
//		}
//		
//		// remove "jar:" prefix
//		String filePrefix = localJar.toExternalForm().substring(localJar.getProtocol().length() + 1);
//		// and suffix
//		int idx2 = filePrefix.lastIndexOf('!');
//		if (idx2 >= 0)
//			filePrefix = filePrefix.substring(0, idx2);
//		
//		try {
//			return new URL(filePrefix);
//		} catch (MalformedURLException e) {
//			logger.error("malformed URL for " + filePrefix, e);
//			e.printStackTrace();
//			return zip;
//		}
//
//	}

	/**
	 * 
	 * from <a href="http://www.objectdefinitions.com/odblog/2008/workaround-for-bug-id-6753651-find-path-to-jar-in-cache-under-webstart/">this post</a>
	 * @param jar
	 * @return
	 */
//	private URL resolveToLocalJarFile(URL jar) {
//		URL url = JarUtils.convertToJarFileURL(jar);
//		logger.debug("Resolved " + jar + " to " + url);
//		return url;
//	}

	private URL resolveToLocalZipFile(URL zip) {
		return zip;
	}

	/**
	 * @param uri
	 * @param string
	 * @return URI or <code>null</code>
	 */
	public URI resolveInsideURI(URI uri, String string) {
		URI resolved = null;
		if (string.contains("/")) {
			try {
				resolved = URI.create(string);
				if (resolved.isOpaque())
					return resolved;
			} catch (IllegalArgumentException e) {
				
			}
		}
		if (!uri.isOpaque()) {
			resolved = uri.resolve(encodeURIcomponent(string));
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
				resolved = createURI(ssp).resolve(encodeURIcomponent(string));
				resolved = new URI(uri.getScheme() + ":" + resolved.getScheme(),
						resolved.getUserInfo(),
						resolved.getHost(),
						resolved.getPort(),
						resolved.getPath(),
						resolved.getQuery(),
						uri.getFragment());
				boolean got = false;
				try {
					if ("jar".equals(resolved.getScheme())) {
//						resolved = resolveToLocalJarFile(resolved.toURL()).toURI();
						resolved = resolveToLocalZipFile(resolved.toURL()).toURI();
						got = true;
					}
				} catch (MalformedURLException e) {
					logger.error("malformed URL from " + resolved, e);
					e.printStackTrace();
				} catch (URISyntaxException e) {
					logger.error("URI syntax from " + resolved, e);
					e.printStackTrace();
				}
				if (got) {
					logger.debug("Resolved " + uri + " + " + string + " ==> " + resolved);
				}
			} catch (URISyntaxException e) {
				logger.error("URI syntax error " + string, e);
				e.printStackTrace();
			}
		}
		

		return resolved;
	}
	
	/** Converts a string into something you can safely insert into a URL. */
	private String encodeURIcomponent(String s)
	{
	    StringBuilder o = new StringBuilder();
	    for (char ch : s.toCharArray()) {
	        if (isUnsafe(ch)) {
	        	if (ch < 0x100) {
	        		o.append('%');
		            o.append(toHex((ch >> 4) & 0xf));
		            o.append(toHex(ch & 0xf));
	        	} else {
	        		try {
						for (byte c : ("" + ch).getBytes("UTF-8")) {
							o.append('%');
							o.append(toHex((c >> 4) & 0xf));
							o.append(toHex(c & 0xf));
						}
					} catch (UnsupportedEncodingException e) {
						throw new IllegalArgumentException(e);
					}
	        	}
	        }
	        else o.append(ch);
	    }
	    return o.toString();
	}

	private char toHex(int ch)
	{
	    return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
	}

	private boolean isUnsafe(char ch)
	{
	    if (ch > 128 || ch < 0)
	        return true;
	    // remove '/' since it is likely a real path separator
	    return " %$&+,:;=?@<>#%".indexOf(ch) >= 0;
	}

	protected URLConnection connect(URI uri) throws IOException,
			MalformedURLException {
		return connect(uri, true);
	}

	protected URLConnection connect(URI uri, boolean useCache) throws IOException,
		MalformedURLException {
		URLConnection connection = uri.toURL().openConnection();
		configureConnection(connection, useCache);
		try {
			connection.connect();
		} catch (NullPointerException e) {
			// too-many files open bug...
			throw new IOException(e);
		}
		return connection;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#createInputStream(java.net.URI)
	 */
	@Override
	public InputStream createInputStream(URI uri) throws IOException {

		try {
			return new BufferedInputStream(new FileInputStream(new File(uri)));
		} catch (IllegalArgumentException e) 
		{
			InputStream is = null; 
			URLConnection connection = connect(uri, false);
			is = connection.getInputStream();
			
			if (is == null)
				throw new FileNotFoundException("failed to connect to " + uri);
			return is;
		}
	}

	

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getContentLength(java.net.URI)
	 */
	@Override
	public int getContentLength(URI uri) throws IOException {
		try {
			File file = new File(uri);
			return (int) Math.min(Integer.MAX_VALUE, file.length());
		} catch (IllegalArgumentException e) {
			URLConnection connection = connect(uri, false);
			return connection.getContentLength();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getLastModified(java.net.URI)
	 */
	@Override
	public long getLastModified(URI uri) throws IOException {
		try {
			File file = new File(uri);
			return file.lastModified();
		} catch (IllegalArgumentException e) {
			URLConnection connection = connect(uri, false);
			return connection.getLastModified();
		}
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
			logger.error("malformed URL from " + uri, e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("ignored IOException from " + uri, e);
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
			logger.debug("exists? " + uri);
			try {
				// cheaper path for files
				File file = new File(uri);
				return file.exists();
			} catch (IllegalArgumentException e) {
				is = createInputStream(uri);
				return true;
			}
		} catch (IllegalArgumentException e) {
			logger.error("illegal URI: " + uri, e);
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
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

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getContentMD5(java.net.URI)
	 */
	@Override
	public String getContentMD5(URI uri, int offset, int length) throws IOException {
		URI directory = resolveInsideURI(uri, ".");
		Map<String, String> md5Dir = cachedUriToMD5Hashes.get(directory);
		if (md5Dir == null) {
			md5Dir = new LinkedHashMap<String, String>();
			cachedUriToMD5Hashes.put(directory, md5Dir);
		}
		String uriKey = getURIKey(uri, offset, length);
		String md5 = md5Dir.get(uriKey);
		if (md5 == null) {
			md5 = fetchMD5(uri, offset, length);
			md5Dir.put(uriKey, md5);

			// store reverse mapping too
			String mdKey = getMd5Key(md5, offset, length);
			cachedMD5HashesToURIs.put(mdKey, uri);
		}
		return md5;
	}

	/**
	 * @param uri
	 * @param offset
	 * @param length
	 * @return
	 */
	protected String getURIKey(URI uri, int offset, int length) {
		String key = uri + ":" + offset + ":" + (length <= 0 ? -1 : length);
		return key;
	}

	/**
	 * @param uri
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	protected String fetchMD5(URI uri, int offset, int length)
			throws IOException {
		String md5;
		try {
			int size = getContentLength(uri);
			if (size < 0 || size > MAX_FILE_SIZE) {
				// ignore huge files -- probably bogus
				md5 = "";
			}
			else {
//				// HACK: ignore trailing garbage
//				if (uri.toString().endsWith(".bin") && size > 0x1000 && (size & 0x3ff) >= 0x200) {
//					size &= ~0x3ff;
//				}
				
				InputStream is = createInputStream(uri);
				try {
					FileUtils.skipFully(is, offset);
					byte[] content = FileUtils.readInputStreamContentsAndClose(is, 
							length > 0 ? length : size - offset);
					md5 = FileUtils.getMD5Hash(content);
				} catch (EOFException e) {
					md5 = "";
				}
			}
		} catch (NullPointerException e) {
			// too many files open...
			md5 = "";
			logger.error("can't fetch MD5 for " + uri, e);
		} catch (FileNotFoundException e) {
			// this happens when invalid filenames are located and the
			// URI was not properly de/en-coded -- TODO
			md5 = "";
			logger.error("can't fetch MD5 for " + uri, e);
		}
		return md5;
	}

	/**
	 * @param md5
	 * @param offset
	 * @param length
	 * @return
	 */
	protected String getMd5Key(String md5, int offset, int length) {
		String mdKey = md5 + ":" + offset + ":" + (length <= 0 ? -1 : length);
		return mdKey;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#getContentMD5(java.net.URI)
	 */
	@Override
	public String getContentMD5(URI uri) throws IOException {
		return getContentMD5(uri, 0, -1);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#findFileByMD5(java.lang.String)
	 */
	@Override
	public URI findFileByMD5(String md5, int offset, int limit) {
		String md5Key;
		md5Key = getMd5Key(md5, offset, limit);
		if (cachedMD5HashesToURIs.containsKey(md5Key)) {
			return cachedMD5HashesToURIs.get(md5Key);	// if null, we already tried
		}
		if (offset == 0 && limit > 0) {
			md5Key = getMd5Key(md5, 0, -1);
			if (cachedMD5HashesToURIs.containsKey(md5Key)) {
				URI uri = cachedMD5HashesToURIs.get(md5Key);
				if (uri == null)
					return null;	// already tried
				try {
					int length = getContentLength(uri);
					if (length == limit) {
						return uri;
					}
				} catch (IOException e) {
					// nope
					cachedMD5HashesToURIs.put(md5Key, null);
				}
			}
		}

		// if we are summing a portion, need to explicitly find a match
		if (offset != 0 || limit > 0) {
			for (URI directory : getSearchURIs()) {
				URI uri = findFileByMD5(directory, md5, offset, limit);
				if (uri != null)
					return uri;
			}
		}
		
		// nope... 
		return null;
	}

	/**
	 * @param directory
	 * @param md5
	 * @param limit
	 */
	protected URI findFileByMD5(URI directory, String md5, int offset, int limit) {
		try {
//			System.out.println("searching " + directory + " for " + md5);
			Map<String, FileInfo> direct = getDirectoryListing(directory);
			for (Map.Entry<String, FileInfo> ent : direct.entrySet()) {
				try {
					FileInfo info = ent.getValue();
					URI uri = info.uri;
					String entMd5;
					if (offset == 0 && (limit <= 0 || limit == info.length)) {
						entMd5 = info.md5;
					} else {
						entMd5 = getContentMD5(uri, offset, limit);
					}
					if (entMd5.equalsIgnoreCase(md5)) {
						//System.out.println("\t" + entMd5 + " = " + uri);
						return uri; 
					}
					
					// try inside archive itself
					if (uri.getScheme().equals("file") && ent.getKey().toLowerCase().matches(".*\\.(rpk|zip)")) {
						URI zipURI = URI.create("jar:" + uri + "!/");
						uri = findFileByMD5(
								zipURI,
								md5, 
								offset, limit);
						if (uri != null) {
							//System.out.println("\t" + entMd5 + " = " + uri);
							return uri;
						}
					}
					
				} catch (Throwable e) {
					logger.error(ent + ": " + e.toString(), e);
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			if (sReportedMissing.add(e.toString())) {
				logger.error("file not found", e);
			}
		} catch (IOException e) {
			logger.error("MD5 search error", e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#splitFileName(java.net.URI)
	 */
	@Override
	public Pair<URI, String> splitFileName(URI uri) {
		URI parent = resolveInsideURI(uri, ".");
		if (parent.getPath() != null) {
			int l1 = parent.getPath().length();
			String path = uri.getPath();
			String name = path.substring(l1);
			return new Pair<URI, String>(parent, name);
		} else {
			int l1 = parent.getSchemeSpecificPart().length();
			String path = uri.getSchemeSpecificPart();
			String name = path.substring(l1);
			return new Pair<URI, String>(parent, name);
		}
//		String path = uri.toString();
//		int idx = path.lastIndexOf('/');
//		if (idx == path.length() - 1)
//			return new Pair<String, String>(path.substring(0, idx + 1), "");
//		else if (idx >= 0)
//			return new Pair<String, String>(path.substring(0, idx + 1), path.substring(idx + 1));
//		else
//			return new Pair<String, String>("", path);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#findFile(v9t9.common.memory.MemoryEntryInfo, v9t9.common.memory.StoredMemoryEntryInfo)
	 */
	@Override
	public URI findFile(ISettingsHandler settings, MemoryEntryInfo info) {
		// note: if stored, this finds the user's copy first or the original template
		URI uri = null;

		// default is to match by content unless filename is overridden...
		boolean searchByContent = info.getFileMD5() != null && info.isDefaultFilename(settings);
			
		String theFilename = info.getResolvedFilename(settings);
		
		if (searchByContent) {
			uri = findFileByMD5(info.getFileMD5(), info.getFileMd5Offset(), 
					info.getFileMd5Limit() != 0 ? info.getFileMd5Limit() : info.getSize());
			if (uri != null) {
				logger.info("*** Found matching entry by MD5: " + uri);
				theFilename = splitFileName(uri).second;
			}
		}
		
		if (uri == null && theFilename != null) {
			uri = findFile(theFilename);
		}
			
		if (uri == null && theFilename != null) {
			if (info.isStored()) {
				uri = getWriteURI(theFilename);
			}
		}
		return uri;
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
		
		query = new URI("https://foo.bar.com/path/to/something/?query=bar");
		System.out.println(query);
		queryPlus = query.resolve("foo/la/dee");
		System.out.println(queryPlus);
		queryPlus2 = query.resolve("foo/la/dee" + "?"+ query.getQuery());
		System.out.println(queryPlus2);
		
		try {
			URL dir = new URL("file:/tmp");
			URLConnection connection = dir.openConnection();
			connection.connect();
			System.out.println(connection.getContentType());
			System.out.println(connection.getContentLength());
			InputStream is = connection.getInputStream();
			System.out.println(FileUtils.readInputStreamTextAndClose(is));
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#addListener(v9t9.common.files.IPathFileLocator.IPathChangeListener)
	 */
	@Override
	public void addListener(IPathChangeListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.files.IPathFileLocator#removeListener(v9t9.common.files.IPathFileLocator.IPathChangeListener)
	 */
	@Override
	public void removeListener(IPathChangeListener listener) {
		
		listeners.remove(listener);
	}
}
