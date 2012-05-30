/**
 * 
 */
package v9t9.gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;


/**
 * @author Ed
 *
 */
public class EmulatorGuiData {
	private static final Logger logger = Logger.getLogger(EmulatorGuiData.class);
	
	private static final URL sBaseDataURL;
	static final URL sBaseV9t9URL;
	static {
		URL url = EmulatorGuiData.class.getClassLoader().getResource("icons");
		URL burl = EmulatorGuiData.class.getClassLoader().getResource(
				EmulatorGuiData.class.getName().replace(".", "/") + ".class");
		if (false) {
			System.out.println("\n\n\n\n");
			System.out.println("/ URL = " + url);
			System.out.println("EmulatorGuiData.class URL = " + burl);
			System.out.flush();
		}
		if (url != null) {
			// go to parent of tree + bin
			try {
				url = new URL(url, "../..");
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
				e.printStackTrace();
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
					burl = new URL(burlString.substring(0, burlString.indexOf(Emulator.class.getName().replace(".", "/"))));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		sBaseV9t9URL = url;
		sBaseDataURL = burl;
		

		logger.info("Base emulator GUI data URL: " + sBaseDataURL);
		logger.info("Base emulator V9t9 data URL: " + sBaseV9t9URL);
		//System.out.println("sBaseV9t9URL = " + sBaseV9t9URL);
		//System.out.println("sBaseBuildURL = " + sBaseDataURL);
	}
	
	public static URL getDataURL(String string) {
		try {
			return new URL(sBaseDataURL, string);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Image loadImage(Device device, String path) {
		URL iconFile = getDataURL(path);
		if (iconFile != null) {
			try {
				Image icon = new Image(device, iconFile.openStream());
				return icon;
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Throwable e1) {
				// try again -- SWT bug
				try {
					Image icon = new Image(device, iconFile.openStream());
					return icon;
				} catch (Throwable e2) {
					e1.printStackTrace();
				}
			}
		}
		return new Image(device, 1, 1);
	}
	
}
