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

import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;

/**
 * Help for dumping logs.
 * @author ejs
 *
 */
public class Logging {
	private static Map<Setting, File> settingToFilenameMap = new HashMap<Setting, File>();
	private static Map<File, PrintWriter> fileToStreamMap = new HashMap<File, PrintWriter>();
	private static Map<Setting, PrintWriter> settingToPrintwriterMap = new HashMap<Setting, PrintWriter>();
	
	final static String TMPDIR = File.separatorChar == '/' ? "/tmp/" : "c:/temp/";
    
	/**
	 * Register a log file for the given setting.  Must register before setting value.
	 * Multiple settings may share a file.
	 * @param setting the boolean setting 
	 * @param logFileName filename, which will live in a temporary directory 
	 * unless absolute
	 */
	public static void registerLog(Setting setting, String logFileName) {
		File file = new File(logFileName);
		if (!file.isAbsolute()) {
			logFileName = TMPDIR + logFileName;
			file = new File(logFileName);
		}
		
		settingToFilenameMap.put(setting, file);
		
		// delete upon registration and append when opening to allow
		// multiple settings to share the same file
		file.delete();
		
		setting.addListener(new ISettingListener() {

            public void changed(Setting setting, Object oldValue) {
            	PrintWriter dump = settingToPrintwriterMap.get(setting);
            	
                if (setting.getBoolean() && dump == null) {
                    File file = settingToFilenameMap.get(setting);
                    try {
                        dump = fileToStreamMap.get(file);
                        if (dump == null) {
                        	dump = new PrintWriter(new FileOutputStream(file, true));
                        	fileToStreamMap.put(file, dump);
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
	 * Get the log for a given setting, if it's true.
	 * @param setting
	 * @return PrintWriter or <code>null</code>
	 */
	public static PrintWriter getLog(Setting setting) {
		if (setting.getBoolean())
			return settingToPrintwriterMap.get(setting);
		else
			return null;
	}
	
	/**
	 * Lazy method of writing to a log which may or may not be open.
	 * Message thrown away if log is not open.
	 * @param setting setting controlling the log
	 * @param msg text to write
	 */
	public static void writeLogLine(Setting setting, String msg) {
		PrintWriter pw = getLog(setting);
		if (pw != null) {
			pw.println(msg);
		}
	}
}
