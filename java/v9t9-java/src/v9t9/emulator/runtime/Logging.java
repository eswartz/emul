/**
 * 
 */
package v9t9.emulator.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;

/**
 * Help for dumping logs.
 * @author ejs
 *
 */
public class Logging {
	private static Map<IProperty, File> settingToFilenameMap = new HashMap<IProperty, File>();
	private static Map<File, PrintWriter> fileToStreamMap = new HashMap<File, PrintWriter>();
	private static Map<IProperty, PrintWriter> settingToPrintwriterMap = new HashMap<IProperty, PrintWriter>();
	
	final static String TMPDIR = File.separatorChar == '/' ? "/tmp/" : "c:/temp/";
    
	/**
	 * Register a log file for the given setting.  Must register before setting value.
	 * Multiple settings may share a file.
	 * @param setting the boolean setting 
	 * @param logFileName filename, which will live in a temporary directory 
	 * unless absolute
	 */
	public static void registerLog(IProperty setting, String logFileName) {
		File file = new File(logFileName);
		if (!file.isAbsolute()) {
			logFileName = TMPDIR + logFileName;
			file = new File(logFileName);
		}
		
		settingToFilenameMap.put(setting, file);
		
		// delete upon registration and append when opening to allow
		// multiple settings to share the same file
		file.delete();
		
		setting.addListener(new IPropertyListener() {

            public void propertyChanged(IProperty setting) {
            	PrintWriter dump = settingToPrintwriterMap.get(setting);
            	
            	boolean enabled = isSettingEnabled(1, setting);
                if (enabled && dump == null) {
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
                } else if (!setting.getBoolean() && dump != null) {
                	dump.close();
                	settingToPrintwriterMap.remove(setting);
                }
            }});
		
	}
	
	/**
	 * Register a log file for the given setting.  Must register before setting value.
	 * Multiple settings may share a file.
	 * @param setting the boolean setting 
	 * @param logFileName filename, which will live in a temporary directory 
	 * unless absolute
	 */
	public static void registerLog(IProperty setting, PrintWriter writer) {
		if ((setting.getValue() instanceof Integer && setting.getInt() > 0)
				|| setting.getBoolean())
			settingToPrintwriterMap.put(setting, writer);
		
		setting.addListener(new IPropertyListener() {

            public void propertyChanged(IProperty setting) {
            	PrintWriter dump = settingToPrintwriterMap.get(setting);
            	
            	boolean enabled = isSettingEnabled(1, setting);
                if (enabled && dump == null) {
                	dump = new PrintWriter(System.out, true);
                	dump.println("Enabling " + setting.getName());
                    settingToPrintwriterMap.put(setting, dump);
                } else if (!enabled && dump != null) {
                	settingToPrintwriterMap.remove(setting);
                }
            }});
		
	}
	
	/**
	 * Get the log for a given setting, if its level is level is matched
	 * @param level 1+ for integer-based level
	 * @param setting
	 * @return PrintWriter or <code>null</code>
	 */
	public static PrintWriter getLog(int level, IProperty setting) {
		boolean enabled = isSettingEnabled(level, setting);
		if (enabled)
			return settingToPrintwriterMap.get(setting);
		else
			return null;
	}

	private static boolean isSettingEnabled(int level, IProperty setting) {
		boolean enabled;
		if (setting.getValue() instanceof Integer) 
			enabled = setting.getInt() >= level;
		else
			enabled = setting.getBoolean();
		return enabled;
	}

	/**
	 * Get the log for a given setting, if it's true.
	 * @param setting
	 * @return PrintWriter or <code>null</code>
	 */
	public static PrintWriter getLog(IProperty setting) {
		return getLog(1, setting);
	}
	
	/**
	 * Lazy method of writing to a log which may or may not be open.
	 * Message thrown away if log is not open.
	 * @param setting setting controlling the log
	 * @param msg text to write
	 */
	public static void writeLogLine(IProperty setting, String msg) {
		PrintWriter pw = getLog(setting);
		if (pw != null) {
			pw.println(msg);
		}
	}
	
	/**
	 * Lazy method of writing to a log which may or may not be open.
	 * Message thrown away if log is not open.
	 * @param setting setting controlling the log
	 * @param msg text to write
	 */
	public static void writeLogLine(int level, IProperty setting, String msg) {
		PrintWriter pw = getLog(level, setting);
		if (pw != null) {
			pw.println(msg);
		}
	}
}
