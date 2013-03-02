/*
  EmulatorMachinesData.java

  (c) 2011-2012 Edward Swartz

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
package v9t9.machine;
/**
 * 
 */


import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author ejs
 *
 */
public class EmulatorMachinesData {
	private static final Logger logger = Logger.getLogger(EmulatorMachinesData.class);
	
	private static final URL sBaseDataURL;

	static {
		URL url = EmulatorMachinesData.class.getClassLoader().getResource(".");
		URL burl = EmulatorMachinesData.class.getClassLoader().getResource(
				EmulatorMachinesData.class.getName().replace(".", "/") + ".class");
		if (url != null) {
			// "." will be under "bin", go to parent of tree
			try {
				url = new URL(url, "..");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		else {
			try {
				// get out of sources to build dir
				File cwdParentParent = new File(System.getProperty("user.dir"), "/../..");
				url = new URL("file", null, cwdParentParent.getAbsolutePath());
			} catch (MalformedURLException e) {
				try {
					url = URI.create(".").toURL();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
					System.exit(123);
				}
			}
		}
		
		if (burl != null) {
			// "." will be under "bin", go to parent of tree
			try {
				String burlString = burl.toString();
				if (!burlString.contains("!/")) {
					burl = new URL(burlString.substring(0, burlString.indexOf("bin/v9t9")));
					burl = new URL(burl, "data/");
				} else {
					burl = new URL(burlString.substring(0, burlString.indexOf(EmulatorMachinesData.class.getName().replace(".", "/"))));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		sBaseDataURL = burl;
		
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
