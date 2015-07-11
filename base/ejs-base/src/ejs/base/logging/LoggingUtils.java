/*
  LoggingUtils.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.logging;

import java.net.URL;

/**
 * @author ejs
 *
 */
public class LoggingUtils {

	public static void setupLogging(Class<?> owner, String configFile) {
		String className = owner.getName();
		int didx = className.lastIndexOf('.');
		String logName = "/" + className.substring(0, didx+1).replace('.', '/') + configFile;
		URL logURL = owner.getResource(logName);
		if (logURL != null)
			System.setProperty("log4j.configuration", logURL.toString());
	
	}

	public static void setupNullLogging() {
		setupLogging(LoggingUtils.class, "null.properties");
	}
	
}
