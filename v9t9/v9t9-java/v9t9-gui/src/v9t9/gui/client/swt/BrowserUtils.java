/**
 * 
 */
package v9t9.gui.client.swt;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;

/**
 * @author ejs
 *
 */
public class BrowserUtils {

	/**
	 * @param url
	 */
	public static void openURL(String url) {
		Program program = Program.findProgram(".html");
		if (program == null) {
			if (SWT.getPlatform().equals("gtk")) {
				// HIGHLY unlikely this would really be true,
				// but SWT can't seem to cope with Unity...
				// I could hack the Display.getData("Program_DESKTOP") value
				// but after that I can't figure out how to make SWT detect GNOME properly.
				
				try {
					Runtime.getRuntime().exec(new String[] { "/usr/bin/xdg-open", url });
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		}
		if (program != null) {
			if (program.execute(url)) {
				return;
			}
		}
	}

}
