/**
 * 
 */
package v9t9.engine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author ejs
 *
 */
public class EmulatorData {

	static final boolean sIsDevBuild;
	private static final URL sBaseV9t9URL;
	private static final URL sBaseDataURL;

	static {
		URL url = EmulatorData.class.getClassLoader().getResource(".");
		URL burl = EmulatorData.class.getClassLoader().getResource(
				EmulatorData.class.getName().replace(".", "/") + ".class");
		System.out.println("\n\n\n\n");
		System.out.println("/ URL = " + url);
		System.out.println("Emulator.class URL = " + burl);
		System.out.flush();
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
					burl = new URL(burlString.substring(0, burlString.indexOf(EmulatorData.class.getName().replace(".", "/"))));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		sBaseV9t9URL = url;
		sBaseDataURL = burl;
		System.out.println("sBaseV9t9URL = " + sBaseV9t9URL);
		System.out.println("sBaseBuildURL = " + sBaseDataURL);
		
		sIsDevBuild = sBaseV9t9URL != null && sBaseV9t9URL.getProtocol().equals("file");
	}

	// FIXME
	/*
	static {
		DataFiles.settingBootRomsPath.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				if (setting.getList().isEmpty())
					addDefaultPaths();
			}
	
		});
		
		addDefaultPaths();
	}

	private static void addDefaultPaths() {
		if (sIsDevBuild) {
			DataFiles.addSearchPath(settings, "../../build/roms");
		}
	}
*/
	
	public static URL getDataURL(String string) {
		try {
			return new URL(sBaseDataURL, string);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
