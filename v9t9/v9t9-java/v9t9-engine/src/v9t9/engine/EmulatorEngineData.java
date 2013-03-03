/*
  EmulatorEngineData.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine;

import java.net.MalformedURLException;
import java.net.URL;

import v9t9.common.EmulatorLocations;

/**
 * @author ejs
 *
 */
public class EmulatorEngineData {
	
	private static final URL sBaseDataURL;

	static {
		sBaseDataURL = EmulatorLocations.getV9t9DataURL(EmulatorEngineData.class);
		if (sBaseDataURL == null) {
			System.err.println("Could not find data/ directory");
			System.exit(123);
		}
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
