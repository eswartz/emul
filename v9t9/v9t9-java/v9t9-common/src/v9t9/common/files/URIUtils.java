/**
 * 
 */
package v9t9.common.files;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class URIUtils {
	private static final Logger logger = Logger.getLogger(PathFileLocator.class);

	/**
	 * @param uri
	 * @return
	 */
	public static Pair<URI, String> splitFileName(URI uri) {
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

	private static URL resolveToLocalZipFile(URL zip) {
		return zip;
	}


	/**
	 * @param uri
	 * @param string
	 * @return
	 */
	public static URI resolveInsideURI(URI uri, String string) {
		if (uri == null)
			return URI.create(string);
		
		URI resolved = null;
		if (string.contains("/")) {
			try {
				resolved = URI.create(string);
				if (resolved.isOpaque()) {
					if (!"jar".equals(resolved.getScheme())) 
						return resolved;
					URI ssp = URI.create(resolved.getSchemeSpecificPart());
					if ("file".equals(ssp.getScheme()) || ssp.getScheme() == null) {
						if (("file".equals(ssp.getScheme()) && !ssp.getSchemeSpecificPart().startsWith("/")
								|| (ssp.getScheme() == null && !ssp.getPath().startsWith("/")))) {
							String path = uri.getSchemeSpecificPart() + ssp.getSchemeSpecificPart();
							int idx = path.lastIndexOf('!');
							if (idx >= 0)
								path = path.substring(0, idx);
							if (new File(path).exists()) {
								resolved = URI.create("jar:" + uri + ssp.getSchemeSpecificPart());
								return resolved;
							}
						} else {
							return resolved;
						}
					} 
				}
				
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
	private static String encodeURIcomponent(String s)
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

	private static char toHex(int ch)
	{
	    return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
	}

	private static boolean isUnsafe(char ch)
	{
	    if (ch > 128 || ch < 0)
	        return true;
	    // remove '/' since it is likely a real path separator
	    return " %$&+,:;=?@<>#%".indexOf(ch) >= 0;
	}

	/**
	 * @param path
	 * @return
	 */
	public static URI createURI(String path) throws URISyntaxException {
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

}
