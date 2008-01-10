/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.swt.widgets.Display;

import com.windriver.debug.tcf.core.model.TCFLaunch;
import com.windriver.tcf.api.protocol.Protocol;

public class TCFModelManager {

    private final Display display;
    private final Map<TCFLaunch,TCFModel> models = new HashMap<TCFLaunch,TCFModel>();

    private final TCFLaunch.Listener tcf_launch_listener = new TCFLaunch.Listener() {

        public void onConnected(TCFLaunch launch) {
            TCFModel model = models.get(launch);
            if (model != null) model.onConnected();
        }

        public void onDisconnected(TCFLaunch launch) {
            TCFModel model = models.get(launch);
            if (model != null) model.onDisconnected();
        }
    };

    private final ILaunchesListener debug_launch_listener = new ILaunchesListener() {

        public void launchesAdded(final ILaunch[] launches) {
        }

        public void launchesChanged(final ILaunch[] launches) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    for (int i = 0; i < launches.length; i++) {
                        if (launches[i] instanceof TCFLaunch) {
                            TCFModel model = models.get(launches[i]);
                            if (model != null) model.launchChanged();
                        }
                    }
                }
            });
        }

        public void launchesRemoved(final ILaunch[] launches) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    for (int i = 0; i < launches.length; i++) {
                        if (launches[i] instanceof TCFLaunch) {
                            TCFModel model = models.remove(launches[i]);
                            if (model != null) model.dispose();
                        }
                    }
                }
            });
        }
    };

    public TCFModelManager() {
        display = Display.getDefault();
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(debug_launch_listener);
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                TCFLaunch.addListener(tcf_launch_listener);
            }
        });
    }

    public void dispose() {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(debug_launch_listener);
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                TCFLaunch.removeListener(tcf_launch_listener);
                for (Iterator<TCFModel> i = models.values().iterator(); i.hasNext();) {
                    TCFModel model = i.next();
                    model.dispose();
                    i.remove();
                }
                assert models.isEmpty();
            }
        });
    }

    public TCFModel getModel(TCFLaunch launch) {
        TCFModel model = models.get(launch);
        if (model == null) {
            model = new TCFModel(display, launch);
            models.put(launch, model);
            if (launch.getChannel() != null) tcf_launch_listener.onConnected(launch);
        }
        return model;
    }

    public TCFNode getRootNode(TCFLaunch launch) {
        return getModel(launch).getRootNode();
    }
}
