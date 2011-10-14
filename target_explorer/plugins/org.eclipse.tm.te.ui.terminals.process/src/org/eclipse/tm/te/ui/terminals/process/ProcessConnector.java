/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.process;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ILineSeparatorConstants;
import org.eclipse.tm.te.ui.terminals.process.activator.UIPlugin;
import org.eclipse.tm.te.ui.terminals.process.nls.Messages;
import org.eclipse.tm.te.ui.terminals.streams.AbstractStreamsConnector;

/**
 * Process connector implementation.
 */
@SuppressWarnings("restriction")
public class ProcessConnector extends AbstractStreamsConnector {
	// Reference to the process settings
	private final ProcessSettings settings;

	// Reference to the PTY instance.
	private PTY pty;
	// Reference to the launched process instance.
	private Process process;
	// Reference to the process monitor
	private ProcessMonitor monitor;

	// The terminal width and height. Initially unknown.
	private int width = -1;
	private int height = -1;

	/**
	 * Constructor.
	 */
	public ProcessConnector() {
		this(new ProcessSettings());
	}

	/**
	 * Constructor.
	 *
	 * @param settings The process settings. Must not be <code>null</code>
	 */
	public ProcessConnector(ProcessSettings settings) {
		super();

		Assert.isNotNull(settings);
		this.settings = settings;
	}

	/**
	 * Returns the process object or <code>null</code> if the
	 * connector is connector.
	 *
	 * @return The process object or <code>null</code>.
	 */
	public Process getProcess() {
		return process;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#connect(org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl)
	 */
	@Override
	public void connect(ITerminalControl control) {
		Assert.isNotNull(control);
		super.connect(control);

		pty = null;
		width = -1;
		height = -1;

		try {
			// Try to determine process and PTY instance from the process settings
			process = settings.getProcess();
			pty = settings.getPTY();

			// No process -> create PTY on supported platforms and execute
			// process image.
			if (process == null) {
				if (PTY.isSupported()) {
					try {
						pty = new PTY(false);
					} catch (IOException e) {
						// PTY not supported on windows
					}
				}

				// Build up the command
				StringBuilder command = new StringBuilder(settings.getImage());
				String arguments = settings.getArguments();
				if (arguments != null && !"".equals(arguments.trim())) { //$NON-NLS-1$
					// Append to the command now
					command.append(" "); //$NON-NLS-1$
					command.append(arguments.trim());
				}

                if (pty != null) {
                	// A PTY is available -> can use the ProcessFactory.

                	// Tokenize the command (ProcessFactory takes an array)
            		StreamTokenizer st = new StreamTokenizer(new StringReader(command.toString()));
            		st.resetSyntax();
            		st.whitespaceChars(0, 32);
            		st.whitespaceChars(0xa0, 0xa0);
            		st.wordChars(33, 255);
            		st.quoteChar('"');
            		st.quoteChar('\'');

            		List<String> argv = new ArrayList<String>();
            		int ttype = st.nextToken();
            		while (ttype != StreamTokenizer.TT_EOF) {
            			argv.add(st.sval);
            			ttype = st.nextToken();
            		}

            		// Execute the process
                    process = ProcessFactory.getFactory().exec(argv.toArray(new String[argv.size()]), getProcessEnvironment(), null, pty);
                } else {
                	// No PTY -> just execute via the standard Java Runtime implementation.
                    process = Runtime.getRuntime().exec(command.toString());
                }
			}

			String lineSeparator = settings.getLineSeparator();
			if (lineSeparator == null) {
				lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
				if ("\r".equals(lineSeparator)) { //$NON-NLS-1$
					lineSeparator = ILineSeparatorConstants.LINE_SEPARATOR_CR;
				}
				else if ("\n".equals(lineSeparator)) { //$NON-NLS-1$
					lineSeparator = ILineSeparatorConstants.LINE_SEPARATOR_LF;
				}
				else {
					lineSeparator = ILineSeparatorConstants.LINE_SEPARATOR_CRLF;
				}
			}

			// connect the streams
			connectStreams(control, process.getOutputStream(), process.getInputStream(), (pty == null ? process.getErrorStream() : null), settings.isLocalEcho(), lineSeparator);

			// Set the terminal control state to CONNECTED
			control.setState(TerminalState.CONNECTED);

			// Create the process monitor
			monitor = new ProcessMonitor(this);
			monitor.startMonitoring();
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
			                            NLS.bind(Messages.ProcessConnector_error_creatingProcess, e.getLocalizedMessage()), e);
			UIPlugin.getDefault().getLog().log(status);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#isLocalEcho()
	 */
	@Override
	public boolean isLocalEcho() {
		return settings.isLocalEcho();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#doDisconnect()
	 */
	@Override
	public void doDisconnect() {
		// Dispose the process
		if (process != null) { process.destroy(); process = null; }

		// Dispose the streams
		super.doDisconnect();

		// Set the terminal control state to CLOSED.
		fControl.setState(TerminalState.CLOSED);
	}

	// ***** Process Connector settings handling *****

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#makeSettingsPage()
	 */
	@Override
	public ISettingsPage makeSettingsPage() {
		return new ProcessSettingsPage(settings);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#getSettingsSummary()
	 */
	@Override
	public String getSettingsSummary() {
		return settings.getImage() != null ? settings.getImage() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#load(org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void load(ISettingsStore store) {
		settings.load(store);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#save(org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void save(ISettingsStore store) {
		settings.save(store);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#setTerminalSize(int, int)
	 */
	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
		if (width != newWidth || height != newHeight) {
			width = newWidth;
			height = newHeight;
			if (pty != null) {
				pty.setTerminalSize(newWidth, newHeight);
			}
		}
	}

	// ***** Process Environment Handling *****

	// Reference to the monitor to lock if determining the native environment
	private final static Object ENV_GET_MONITOR = new Object();

	// Reference to the native environment once retrieved
	private static Map<String, String> nativeEnvironment = null;
	// Reference to the native environment with the case of the variable names preserved
	private static Map<String, String> nativeEnvironmentCasePreserved = null;

	/**
	 * Returns the specific environment to set for the process to be launched.
	 *
	 * @return The process environment.
	 */
	private static String[] getProcessEnvironment() {
        Map<String, String> env = getNativeEnvironment();

        env.put("TERM", "ansi"); //$NON-NLS-1$ //$NON-NLS-2$

        Iterator<Map.Entry<String, String>> iter = env.entrySet().iterator();
        List<String> strings = new ArrayList<String>(env.size());
        StringBuffer buffer = null;
        while (iter.hasNext()) {
        	Map.Entry<String, String>entry = iter.next();
            buffer = new StringBuffer(entry.getKey());
            buffer.append('=').append(entry.getValue());
            strings.add(buffer.toString());
        }

        return strings.toArray(new String[strings.size()]);
    }

	/**
	 * Determine the native environment, but returns all environment variable
	 * names in upper case.
	 *
	 * @return The native environment with upper case variable names, or an empty map.
	 */
	private static Map<String, String> getNativeEnvironment() {
		synchronized (ENV_GET_MONITOR) {
			if (nativeEnvironment == null) {
				Map<String, String> casePreserved = getNativeEnvironmentCasePreserved();
				if (Platform.getOS().equals(org.eclipse.osgi.service.environment.Constants.OS_WIN32)) {
					nativeEnvironment = new HashMap<String, String>();
					Iterator<Map.Entry<String, String>> entries = casePreserved.entrySet().iterator();
					while (entries.hasNext()) {
						Map.Entry<String, String> entry = entries.next();
						nativeEnvironment.put(entry.getKey().toUpperCase(), entry.getValue());
					}
				} else {
					nativeEnvironment = new HashMap<String, String>(casePreserved);
				}
			}
			return new HashMap<String, String>(nativeEnvironment);
		}
	}

	/**
	 * Determine the native environment.
	 *
	 * @return The native environment, or an empty map.
	 */
    private static Map<String, String> getNativeEnvironmentCasePreserved() {
        synchronized (ENV_GET_MONITOR) {
            if (nativeEnvironmentCasePreserved == null) {
            	nativeEnvironmentCasePreserved= new HashMap<String, String>();
                cacheNativeEnvironment(nativeEnvironmentCasePreserved);
            }
            return new HashMap<String, String>(nativeEnvironmentCasePreserved);
        }
    }

    /**
     * Query the native environment and store it to the specified cache.
     *
     * @param cache The environment cache. Must not be <code>null</code>.
     */
	private static void cacheNativeEnvironment(Map<String, String> cache) {
		Assert.isNotNull(cache);

		try {
			String nativeCommand = null;
			boolean isWin9xME = false; // see bug 50567
			String fileName = null;
			if (Platform.getOS().equals(Constants.OS_WIN32)) {
				String osName = System.getProperty("os.name"); //$NON-NLS-1$
				isWin9xME = osName != null && (osName.startsWith("Windows 9") || osName.startsWith("Windows ME")); //$NON-NLS-1$ //$NON-NLS-2$
				if (isWin9xME) {
					// Win 95, 98, and ME
					// SET might not return therefore we pipe into a file
					IPath stateLocation = UIPlugin.getDefault().getStateLocation();
					fileName = stateLocation.toOSString() + File.separator + "env.txt"; //$NON-NLS-1$
					nativeCommand = "command.com /C set > " + fileName; //$NON-NLS-1$
				} else {
					// Win NT, 2K, XP
					nativeCommand = "cmd.exe /C set"; //$NON-NLS-1$
				}
			} else if (!Platform.getOS().equals(Constants.OS_UNKNOWN)) {
				nativeCommand = "env"; //$NON-NLS-1$
			}
			if (nativeCommand == null) { return; }
			Process process = Runtime.getRuntime().exec(nativeCommand);
			if (isWin9xME) {
				// read piped data on Win 95, 98, and ME
				Properties p = new Properties();
				File file = new File(fileName);
				InputStream stream = new BufferedInputStream(new FileInputStream(file));
				p.load(stream);
				stream.close();
				if (!file.delete()) {
					file.deleteOnExit(); // if delete() fails try again on VM close
				}
				for (Enumeration<Object> enumeration = p.keys(); enumeration.hasMoreElements();) {
					// Win32's environment variables are case insensitive. Put everything
					// to upper case so that (for example) the "PATH" variable will match
					// "pAtH" correctly on Windows.
					String key = (String)enumeration.nextElement();
					cache.put(key, (String)p.get(key));
				}
			} else {
				// read process directly on other platforms
				// we need to parse out matching '{' and '}' for function declarations in .bash environments
				// pattern is [function name]=() { and we must find the '}' on its own line with no trailing ';'
				InputStream stream = process.getInputStream();
				InputStreamReader isreader = new InputStreamReader(stream);
				BufferedReader reader = new BufferedReader(isreader);
				String line = reader.readLine();
				String key = null;
				String value = null;
				while (line != null) {
					int func = line.indexOf("=()"); //$NON-NLS-1$
					if (func > 0) {
						key = line.substring(0, func);
						// scan until we find the closing '}' with no following chars
						value = line.substring(func + 1);
						while (line != null && !line.equals("}")) { //$NON-NLS-1$
							line = reader.readLine();
							if (line != null) {
								value += line;
							}
						}
						line = reader.readLine();
					} else {
						int separator = line.indexOf('=');
						if (separator > 0) {
							key = line.substring(0, separator);
							value = line.substring(separator + 1);
							line = reader.readLine();
							if (line != null) {
								// this line has a '=' read ahead to check next line for '=', might be broken on more
								// than one line
								separator = line.indexOf('=');
								while (separator < 0) {
									value += line.trim();
									line = reader.readLine();
									if (line == null) {
										// if next line read is the end of the file quit the loop
										break;
									}
									separator = line.indexOf('=');
								}
							}
						}
					}
					if (key != null) {
						cache.put(key, value);
						key = null;
						value = null;
					} else {
						line = reader.readLine();
					}
				}
				reader.close();
			}
		} catch (IOException e) {
			// Native environment-fetching code failed.
			// This can easily happen and is not useful to log.
		}
	}
}
