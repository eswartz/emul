/*
  EmulatorMachinesData.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine;
/**
 * 
 */


import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author ejs
 *
 */
public class EmulatorMachinesData {
	private static final Logger logger = Logger.getLogger(EmulatorMachinesData.class);
	
	private static final URL sBaseDataURL;

	static {
		URL url = EmulatorMachinesData.class.getClassLoader().getResource(".");
		URL burl = EmulatorMachinesData.class.getClassLoader().getResource(
				EmulatorMachinesData.class.getName().replace(".", "/") + ".class");
		if (url != null) {
			// "." will be under "bin", go to parent of tree
			try {
				url = new URL(url, "..");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		else {
			try {
				// get out of sources to build dir
				File cwdParentParent = new File(System.getProperty("user.dir"), "/../..");
				url = new URL("file", null, cwdParentParent.getAbsolutePath());
			} catch (MalformedURLException e) {
				try {
					url = URI.create(".").toURL();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
					System.exit(123);
				}
			}
		}
		
		if (burl != null) {
			// "." will be under "bin", go to parent of tree
			try {
				String burlString = burl.toString();
				if (!burlString.contains("!/")) {
					burl = new URL(burlString.substring(0, burlString.indexOf("bin/v9t9")));
					burl = new URL(burl, "data/");
				} else {
					burl = new URL(burlString.substring(0, burlString.indexOf(EmulatorMachinesData.class.getName().replace(".", "/"))));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		sBaseDataURL = burl;
		
		logger.info("Base emulator machine data URL: " + sBaseDataURL);
	}

	public static URL getDataURL(String string) {
		try {
			return new URL(sBaseDataURL, string);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
