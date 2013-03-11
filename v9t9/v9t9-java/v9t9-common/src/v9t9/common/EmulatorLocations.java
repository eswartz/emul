/*
  EmulatorLocations.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author ejs
 *
 */
public class EmulatorLocations {

	public static URL getV9t9BaseURL(Class<?> klass) {
		URL base = klass.getProtectionDomain().getCodeSource().getLocation();
		
		URL baseURL = null;
		
		try {
			if (base.getPath().endsWith("/")) {
				// development mode
				// get out of sources to build dir
				baseURL = new URL(base, "../../");
			} else {
				// jar file
				baseURL = new URL(base, ".");
			}
			
			return baseURL;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static URL getV9t9DataURL(Class<?> klass) {
		
		URL base = klass.getProtectionDomain().getCodeSource().getLocation();
		
		URL baseDataURL;
		try {
			if (base.getPath().endsWith("/")) {
				// development mode
				// get out of sources to build dir
				base = new URL(base, "../../");
				baseDataURL = new URL(base, "v9t9-data/data/");
			} else {
				// jar file
				base = new URL(base, ".");
				baseDataURL = new URL("jar:" + base + "v9t9-data.jar!/");
			}
			
			return baseDataURL;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
