package v9t9.common;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static Bundle bundle;

	/**
	 * @return the bundle
	 */
	public static Bundle getBundle() {
		return bundle;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		this.bundle = context.getBundle();
	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
