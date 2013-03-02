/*
  BrowserUtils.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
