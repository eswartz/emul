/**
 * 
 */
package v9t9.common.files;

import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

/**
 * From <a href="http://www.objectdefinitions.com/odblog/2008/workaround-for-bug-id-6753651-find-path-to-jar-in-cache-under-webstart/"/>
 * @author ejs
 *
 */
public class JarUtils {
	private static final Logger logger = Logger.getLogger(JarUtils.class);
	
	private static Map<String, String> jarUrlToFileURL = new HashMap<String, String>();
	
	/**
	 * This method will return the URL to the jar file containing the resource
	 * which this URL references
	 * 
	 * It should work with URLs returned by class.getResource() under java
	 * 1.5.0_16 and 1.6.0_07, as well as maintaining backwards compatibility
	 * with previous jres
	 * 
	 * The two jre above contain security patches which make the file path of
	 * the jar inaccessible under webstart. This patch works around that by
	 * using reflection to access private fields in the webstart.jar where
	 * required. This will only work for signed webstart apps running with all
	 * security permissions
	 * 
	 * @param jarUrl
	 *            - url which has jar as the protocol
	 * @return path to Jar file for this jarURL
	 */
    public static URL convertToJarFileURL(URL jarUrl) {
    	String jarUrlPath = jarUrl.toString();
    	int idx = jarUrlPath.lastIndexOf('!');
		String prefix = jarUrlPath.substring(0, idx);
		String suffix = jarUrlPath.substring(idx);
		
		String baseURLStr = jarUrlToFileURL.get(prefix);
		if (baseURLStr == null) {
			try {
		        JarFile jarFile = getJarFile(new URL(jarUrl, prefix + "!/"));
		        String path = findJarPath(jarFile);
		        
			    // windows-ization
		        path = path.replace('\\', '/');
		       // path = path.replace(':', '|');
	        
	        	baseURLStr = "jar:file:" + (path.startsWith("/") ? "" : "/") + path;
	        	logger.debug("convertToJarFileURL: " + prefix + " ==> " + baseURLStr);
				
				jarUrlToFileURL.put(prefix, baseURLStr);
			} catch (Throwable e) {
				logger.debug("bonk", e);
				e.printStackTrace();
				return jarUrl;
			}
		}
		try {
			return new URL(baseURLStr + suffix);
		} catch (MalformedURLException e) {
			logger.debug("bonk", e);
			e.printStackTrace();
			return jarUrl;
		}
    }

    public static JarFile getJarFile(URL jarUrl) {
    	logger.debug("getJarFile for " + jarUrl);
        try {
            JarURLConnection jarUrlConnection = (JarURLConnection)jarUrl.openConnection();

            //try the getJarFile method first.
            //Under webstart in 1.5.0_16 this is overriden to return null
            JarFile jarFile = jarUrlConnection.getJarFile();

            if (jarFile == null) {
                jarFile = getJarFileByReflection(jarUrlConnection);
            }
            return jarFile;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get JarFile from jarUrlConnection", t);
        }
    }

    private static JarFile getJarFileByReflection(JarURLConnection jarUrlConnection) throws Exception {
        //this class only exists in webstart.jar for 1.5.0_16 and later
        Class<?> jnlpConnectionClass = Class.forName("com.sun.jnlp.JNLPCachedJarURLConnection");
        Field jarFileField;
        try {
            jarFileField = jnlpConnectionClass.getDeclaredField("jarFile");
        } catch ( Throwable t) {
            jarFileField = jnlpConnectionClass.getDeclaredField("_jarFile");
        }
        jarUrlConnection.connect(); //this causes the connection to set the jarFile field
        jarFileField.setAccessible(true);
        return (JarFile)jarFileField.get(jarUrlConnection);
    }

    private static String findJarPath(JarFile cachedJarFile) {
        try {
            String name = cachedJarFile.getName();

            //getName is overridden to return "" under 1.6.0_7 so use reflection
            if ( name == null || name.trim().equals("")) {
                Class<?> c = ZipFile.class;
                Field field = c.getDeclaredField("name");
                field.setAccessible(true);
                name = (String)field.get(cachedJarFile);
            }
            return name;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get find name from jarFile", t);
        }
    }
}
