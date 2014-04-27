/*
  Logging.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * Help for dumping logs.
 * @author ejs
 *
 */
public class Logging {
	private static Map<IProperty, File> settingToFilenameMap = new HashMap<IProperty, File>();
	private static Map<File, PrintWriter> fileToStreamMap = new HashMap<File, PrintWriter>();
	private static Map<IProperty, PrintWriter> settingToPrintwriterMap = new HashMap<IProperty, PrintWriter>();
	
	final static String TMPDIR = System.getProperty("java.io.tmpdir");
    
	/**
	 * Register a log file for the given setting.  Must register before setting value.
	 * Multiple settings may share a file.
	 * @param setting the boolean setting 
	 * @param logFileName filename, which will live in a temporary directory 
	 * unless absolute
	 */
	public static void registerLog(IProperty setting, String logFileName) {
		if (setting == null)
			return;
		
		if (settingToFilenameMap.containsKey(setting))
			return;
			
		File file = new File(logFileName);
		if (!file.isAbsolute()) {
			logFileName = TMPDIR + File.separatorChar + logFileName;
			file = new File(logFileName);
		}
		
		if (!settingToFilenameMap.values().contains(file)) {
			// delete upon registration and append when opening to allow
			// multiple settings to share the same file
			file.delete();
		}
		
		settingToFilenameMap.put(setting, file);
		
		setting.addListenerAndFire(new IPropertyListener() {

            public void propertyChanged(IProperty setting) {
            	PrintWriter dump = settingToPrintwriterMap.get(setting);
            	
            	boolean enabled = isSettingEnabled(1, setting);
                if (enabled && dump == null) {
                    createDump(setting);
                } else if (!setting.getBoolean() && dump != null) {
                	dump.close();
                	settingToPrintwriterMap.remove(setting);
                }
            }
		});
	}
	

	/**
	 * @param setting
	 */
	protected static void createDump(IProperty setting) {
		PrintWriter dump;
		File file = settingToFilenameMap.get(setting);
		try {
		    dump = fileToStreamMap.get(file);
		    if (dump != null) {
		    	dump.println("Enabling " + setting.getName());
		    	// might be an error
		    }
		    if (dump == null || dump.checkError()) {
		    	dump = new PrintWriter(new FileOutputStream(file, true));
		    	fileToStreamMap.put(file, dump);
		    	dump.println("Enabling " + setting.getName());
		    }
		    settingToPrintwriterMap.put(setting, dump);
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
	}

	/**
	 * Get the log for a given setting, if it's true.
	 * @param setting
	 * @return PrintWriter or <code>null</code>
	 */
	public static PrintWriter getLog(IProperty setting) {
		return setting != null ? getLog(1, setting) : null;
	}
	
	/**
	 * Get the log for a given setting, if its level is level is matched
	 * @param level 1+ for integer-based level
	 * @param setting
	 * @return PrintWriter or <code>null</code>
	 */
	public static PrintWriter getLog(int level, IProperty setting) {
		boolean enabled = setting != null && isSettingEnabled(level, setting);
		if (enabled)
			return settingToPrintwriterMap.get(setting);
		else
			return null;
	}
	
	public static boolean isSettingEnabled(int level, IProperty setting) {
		boolean enabled;
		if (setting.getValue() instanceof Integer) 
			enabled = setting.getInt() >= level;
		else
			enabled = setting.getBoolean();
		return enabled;
	}


	/**
	 * Lazy method of writing to the log.
	 * @param setting setting controlling the log
	 * @param msg text to write
	 */
	public static void writeLogLine(IProperty setting, String msg) {
		writeLogLine(1, setting, msg);
	}

	/**
	 * Lazy method of writing to the log.
	 * @param level minimum level to log
	 * @param setting setting controlling the log
	 * @param msg text to write
	 */
	public static void writeLogLine(int level, IProperty setting, String msg) {
		if (isSettingEnabled(level, setting)) {
			Logger.getLogger(setting.getName()).info(msg);
		}
	}

}
