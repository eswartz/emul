package org.eclipse.tm.internal.tcf;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements  BundleActivator {
    
    private static final String TCF_INTEGRATION_BUNDLE_ID = "org.eclipse.tm.tcf";

    public void start(BundleContext context) throws Exception {
        /*
         * Activate TCF Eclipse integration bundle "org.eclipse.tm.tcf".
         * It must be activated explicitly, because default activation through
         * class loading may never happen - most client don't need classes from that bundle.
         */
        ServiceTracker tracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
        tracker.open();
        Bundle[] bundles = ((PackageAdmin)tracker.getService()).getBundles(TCF_INTEGRATION_BUNDLE_ID, null);
        int cnt = 0;
        if (bundles != null) {
            for (Bundle bundle : bundles) {
                if ((bundle.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
                    bundle.start(Bundle.START_TRANSIENT);
                    cnt++;
                }
            }
        }
        if (cnt != 1) throw new Exception("Invalid or missing bundle: " + TCF_INTEGRATION_BUNDLE_ID);
    }

    public void stop(BundleContext context) throws Exception {
    }
}
