/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.Protocol;


public class TCFModelManager {

    private final Map<TCFLaunch,TCFModel> models = new HashMap<TCFLaunch,TCFModel>();

    private final TCFLaunch.Listener tcf_launch_listener = new TCFLaunch.Listener() {

        public void onCreated(TCFLaunch launch) {
            assert Protocol.isDispatchThread();
            assert models.get(launch) == null;
            TCFModel model = new TCFModel(launch);
            models.put(launch, model);
        }

        public void onConnected(TCFLaunch launch) {
            assert Protocol.isDispatchThread();
            TCFModel model = models.get(launch);
            if (model != null) model.onConnected();
        }

        public void onDisconnected(TCFLaunch launch) {
            assert Protocol.isDispatchThread();
            TCFModel model = models.get(launch);
            if (model != null) model.onDisconnected();
        }

        public void onContextActionsDone(TCFLaunch launch, String id, String result) {
            assert Protocol.isDispatchThread();
            TCFModel model = models.get(launch);
            if (model != null) model.onContextActionsDone(id, result);
        }

        public void onContextActionsStart(TCFLaunch launch, String id) {
            assert Protocol.isDispatchThread();
            TCFModel model = models.get(launch);
            if (model != null) model.onContextActionsStart(id);
        }

        public void onProcessOutput(TCFLaunch launch, String process_id, int stream_id, byte[] data) {
            assert Protocol.isDispatchThread();
            TCFModel model = models.get(launch);
            if (model != null) model.onProcessOutput(process_id, stream_id, data);
        }

        public void onProcessStreamError(TCFLaunch launch, String process_id,
                int stream_id, Exception error, int lost_size) {
            assert Protocol.isDispatchThread();
            TCFModel model = models.get(launch);
            if (model != null) model.onProcessStreamError(process_id, stream_id, error, lost_size);
        }
    };

    private final ILaunchesListener debug_launch_listener = new ILaunchesListener() {

        public void launchesAdded(final ILaunch[] launches) {
        }

        public void launchesChanged(final ILaunch[] launches) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    for (ILaunch launch : launches) {
                        TCFModel model = models.get(launch);
                        if (model != null) model.launchChanged();
                    }
                }
            });
        }

        public void launchesRemoved(final ILaunch[] launches) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    for (ILaunch launch : launches) {
                        TCFModel model = models.remove(launch);
                        if (model != null) model.dispose();
                    }
                }
            });
        }
    };

    public TCFModelManager() {
        assert Protocol.isDispatchThread();
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(debug_launch_listener);
        TCFLaunch.addListener(tcf_launch_listener);
    }

    public void dispose() {
        assert Protocol.isDispatchThread();
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(debug_launch_listener);
        TCFLaunch.removeListener(tcf_launch_listener);
        for (Iterator<TCFModel> i = models.values().iterator(); i.hasNext();) {
            TCFModel model = i.next();
            model.dispose();
            i.remove();
        }
        assert models.isEmpty();
    }

    public TCFModel getModel(TCFLaunch launch) {
        assert Protocol.isDispatchThread();
        return models.get(launch);
    }

    public TCFNode getRootNode(TCFLaunch launch) {
        TCFModel model = getModel(launch);
        if (model == null) return null;
        return model.getRootNode();
    }
}
