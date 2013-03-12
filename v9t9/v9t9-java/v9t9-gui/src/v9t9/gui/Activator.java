package v9t9.gui;

import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "v9t9-gui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		if (System.getProperty("jna.library.path") == null) {
			String path;
			try {
				path = new URL(EmulatorGuiData.sBaseV9t9URL, "libv9t9render").getPath();
				System.out.println("Native libs at " + path);
				if (path != null)
					System.setProperty("jna.library.path", path);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		
//		String origPath = System.getProperty("java.library.path");
//		URL url = FileLocator.find(context.getBundle(), 
//				new Path("libs").append("lwjgl").append("native").append(Platform.getOS()), null);
//		if (url != null) {
//			url = FileLocator.toFileURL(url);
//			System.out.println("Native libs at " + url.getPath());
//			String newPath = url.getPath() + File.pathSeparatorChar + origPath;
//			System.setProperty("java.library.path", newPath);
//		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
