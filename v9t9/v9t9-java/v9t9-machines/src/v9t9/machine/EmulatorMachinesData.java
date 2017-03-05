/*
  EmulatorMachinesData.java

  (c) 2011-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine;
/**
 * 
 */


import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import v9t9.common.EmulatorLocations;

/**
 * @author ejs
 *
 */
public class EmulatorMachinesData {
	private static final Logger logger = Logger.getLogger(EmulatorMachinesData.class);
	
	private static final URL sBaseDataURL;

	static {
		sBaseDataURL = EmulatorLocations.getV9t9DataURL(EmulatorMachinesData.class);
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
