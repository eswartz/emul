/*
  JarUtils.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.launch;

import java.io.File;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

/**
 * From <a href="http://www.objectdefinitions.com/odblog/2008/workaround-for-bug-id-6753651-find-path-to-jar-in-cache-under-webstart/"/>
 * @author ejs
 *
 */
public class JarUtils {
    public static JarFile getJarFile(URL jarUrl) {
    	if (jarUrl.getProtocol().equals("file")) {
    		try {
				File file = new File(jarUrl.toURI());
				if (file.isDirectory())
					return null;
					
				return new JarFile(file);
			} catch (Exception e) {
				throw new RuntimeException("Failed to get JarFile from file: " + jarUrl, e);
			}
    	}
        try {
            URLConnection conn = jarUrl.openConnection();
			JarURLConnection jarUrlConnection = (JarURLConnection) conn;

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
}
