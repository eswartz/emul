/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Determine the current host environment.
 */
public final class Host {
	private static Boolean isWindowsHost = null;
	private static Boolean isInteractive = null;
	private static Boolean isHeadless = null;
	private static Boolean isLinuxHost = null;

	/**
	 * Method looking up the current host (once) and returning a boolean indicating whether the host
	 * is Unix(false) or Windows(true).
	 *
	 * @return boolean true if running on Windows host
	 */
	public static boolean isWindowsHost() {
		if (isWindowsHost == null) {
			if (System.getProperty("os.name", "").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				isWindowsHost = Boolean.TRUE;
			}
			else {
				isWindowsHost = Boolean.FALSE;
			}
		}
		return isWindowsHost.booleanValue();
	}

	/**
	 * Method looking up the current host (once) and returning a boolean indicating whether the host
	 * is Linux(true) or something else (false).
	 *
	 * @return boolean true if running on Linux host
	 */
	public static boolean isLinuxHost() {
		if (isLinuxHost == null) {
			if (System.getProperty("os.name", "").toLowerCase().startsWith("linux")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				isLinuxHost = Boolean.TRUE;
			}
			else {
				isLinuxHost = Boolean.FALSE;
			}
		}
		return isLinuxHost.booleanValue();
	}

	/**
	 * Returns the content of the release file which exists in every linux distribution in /etc.
	 * e.g. /etc/redhat-release . The file holds information about the distribution itself.
	 *
	 * @return String containing the first line of the linux distri's release file. e.g. Red Hat
	 *         Enterprise Linux WS release 4 (Nahant Update 3)
	 */
	public static String getLinuxRelease() {
		String firstLine = "Unknown"; //$NON-NLS-1$
		File etcdir = new File("/etc");//$NON-NLS-1$
		String[] list = etcdir.list(new FilenameFilter() {
			@Override
            public boolean accept(File dir, String name) {
				String[] distStrings = { "fedora-release", //$NON-NLS-1$
				"redhat-release", //$NON-NLS-1$
				"SuSE-release", //$NON-NLS-1$
				"lsb-release" }; //$NON-NLS-1$
				// Strip path information:
				String f = new File(name).getName();
				String filter;
				for (String distString : distStrings) {
					filter = distString;
					if (f.equalsIgnoreCase(filter)) {
						return true;
					}
				}
				return false;
			}
		});

		if (list == null || list.length == 0) {
			return "Unknown"; //$NON-NLS-1$
		}
		String entry = list[0];

		if (Boolean.getBoolean("shell.debug")) { //$NON-NLS-1$
			System.out.println("UtilityEnvironment#getLinuxRelease: reading file: " + etcdir + "/" + entry); //$NON-NLS-1$ //$NON-NLS-2$
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(etcdir + "/" + entry))); //$NON-NLS-1$
			// In case of "lsb-release" we have to look for the line starting with
			// "DISTRIB_DESCRIPTION"
			if ("lsb-release".equalsIgnoreCase(entry)) { //$NON-NLS-1$
				while (firstLine != null && !firstLine.toUpperCase()
				                .startsWith("DISTRIB_DESCRIPTION")) { //$NON-NLS-1$
					if (Boolean.getBoolean("shell.debug")) { //$NON-NLS-1$
						System.out.println("UtilityEnvironment#getLinuxRelease: firstLine='" + firstLine + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					firstLine = reader.readLine();
				}
			}
			else {
				// Just read the first line
				firstLine = reader.readLine();
			}
		}
		catch (IOException ioe) {
			firstLine = "Unknown"; //$NON-NLS-1$
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException ex) {
					// silently ignored
				}
			}
		}

		firstLine = firstLine != null ? firstLine : "Unknown"; //$NON-NLS-1$

		if (Boolean.getBoolean("shell.debug")) { //$NON-NLS-1$
			System.out.println("UtilityEnvironment#getLinuxRelease: return value='" + firstLine + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return firstLine;
	}

	/**
	 * Check if running interactive (default) or in batch mode (e.g. during unit tests).
	 *
	 * @return boolean <code>true</code> if running in interactive mode (default) or
	 *         <code>false</code> if not (-DNOINTERACTIVE=true).
	 */
	public static boolean isInteractive() {
		if (isInteractive == null) {
			boolean batchMode = Boolean.valueOf(System.getProperty("NOINTERACTIVE")).booleanValue(); //$NON-NLS-1$
			isInteractive = Boolean.valueOf(!batchMode);
		}
		return isInteractive.booleanValue();
	}

	/**
	 * Check if running in headless mode or with full UI.
	 *
	 * @return <code>true</code> if running in headless mode, <code>false</code> otherwise.
	 */
	public static boolean isHeadless() {
		if (isHeadless == null) {
			String headless = System.getProperty("HEADLESS"); //$NON-NLS-1$
			isHeadless = Boolean.valueOf(headless);
		}
		return isHeadless.booleanValue();
	}
}
