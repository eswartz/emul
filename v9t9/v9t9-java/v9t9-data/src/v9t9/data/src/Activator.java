package v9t9.data.src;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static Activator singleton;

	/**
	 * @return the singleton
	 */
	public static Activator getDefault() {
		return singleton;
	}
	@Override
	public void start(BundleContext context) throws Exception {
		singleton = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
