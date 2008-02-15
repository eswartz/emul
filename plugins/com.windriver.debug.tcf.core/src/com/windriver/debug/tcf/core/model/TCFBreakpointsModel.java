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
package com.windriver.debug.tcf.core.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;

import com.windriver.debug.tcf.core.TCFCore;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.IBreakpoints;

/**
 * TCFBreakpointsModel class handles breakpoints for all active TCF launches.
 * It downloads initial set of breakpoint data when launch is activated, 
 * listens for Eclipse breakpoint manager events and propagates breakpoint changes to TCF targets.
 */
public class TCFBreakpointsModel implements IBreakpointListener, IBreakpointManagerListener {

    private static final String PROP_ID = "ID";
    private final IBreakpointManager bp_manager = DebugPlugin.getDefault().getBreakpointManager();
    
    public TCFBreakpointsModel() {
        bp_manager.addBreakpointListener(this);
        bp_manager.addBreakpointManagerListener(this);
    }

    public void dispose() {
        bp_manager.removeBreakpointListener(this);
        bp_manager.removeBreakpointManagerListener(this);
    }
    
    public boolean isSupported(IChannel channel, IBreakpoint bp) {
        // TODO implement per-channel breakpoint filtering
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public void downloadBreakpoints(final IChannel channel, final Runnable done)
            throws IOException, CoreException {
        assert Protocol.isDispatchThread();
        IBreakpoints service = channel.getRemoteService(IBreakpoints.class);
        if (service != null) {
            IBreakpoint[] arr = bp_manager.getBreakpoints(ITCFConstants.ID_TCF_DEBUG_MODEL);
            if (arr != null && arr.length > 0) {
                Map<String,Object>[] bps = new Map[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    if (!isSupported(channel, arr[i])) continue;
                    IMarker marker = arr[i].getMarker();
                    String file = getFilePath(marker.getResource());
                    bps[i] = toBreakpointAttributes(file, marker.getAttributes());
                }
                service.set(bps, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error == null) done.run();
                        else channel.terminate(error);
                    }
                });
                return;
            }
        }
        Protocol.invokeLater(done);
    }

    public void breakpointManagerEnablementChanged(final boolean enabled) {
        try {
            IBreakpoint[] arr = bp_manager.getBreakpoints(ITCFConstants.ID_TCF_DEBUG_MODEL);
            if (arr == null || arr.length == 0) return;
            final Map<String,IBreakpoint> map = new HashMap<String,IBreakpoint>();
            for (int i = 0; i < arr.length; i++) {
                IMarker marker = arr[i].getMarker();
                Boolean b = marker.getAttribute(IBreakpoint.ENABLED, Boolean.FALSE);
                if (!b.booleanValue()) continue;
                String id = marker.getAttribute(ITCFConstants.ID_TCF_DEBUG_MODEL +
                        '.' + IBreakpoints.PROP_ID, (String)null);
                map.put(id, arr[i]);
            }
            if (map.isEmpty()) return;
            final ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
            Runnable r = new Runnable() {
                public void run() {
                    for (int i = 0; i < launches.length; i++) {
                        if (launches[i] instanceof TCFLaunch) {
                            TCFLaunch launch = (TCFLaunch)launches[i];
                            final IChannel channel = launch.getChannel();
                            if (channel == null) continue;
                            if (channel.getState() != IChannel.STATE_OPEN) continue;
                            IBreakpoints service = channel.getRemoteService(IBreakpoints.class);
                            if (service == null) continue;
                            Set<String> ids = new HashSet<String>();
                            for (String id : map.keySet()) {
                                IBreakpoint bp = map.get(id);
                                if (isSupported(channel, bp)) ids.add(id);
                            }
                            IBreakpoints.DoneCommand done = new IBreakpoints.DoneCommand() {
                                public void doneCommand(IToken token, Exception error) {
                                    if (error != null) channel.terminate(error);
                                }
                            };
                            if (enabled) {
                                service.enable(ids.toArray(new String[ids.size()]), done);
                            }
                            else {
                                service.disable(ids.toArray(new String[ids.size()]), done);
                            }
                        }
                    }
                    Protocol.sync(new Runnable() {
                        public void run() {
                            synchronized (map) {
                                map.notify();
                            }
                        }
                    });
                }
            };
            synchronized (map) {
                assert !Protocol.isDispatchThread();
                Protocol.invokeLater(r);
                map.wait();
            }
        }
        catch (Throwable x) {
            TCFCore.log("Unhandled exception in breakpoint listener", x);
        }
    }
    
    private abstract class BreakpointUpdate implements Runnable {
        
        private final IBreakpoint breakpoint;
        private final ILaunch[] launches;
        private final Map<String,Object> marker_attrs;
        private final String marker_file;
        
        IBreakpoints service;
        IBreakpoints.DoneCommand done;
        Map<String,Object> tcf_attrs;

        @SuppressWarnings("unchecked")
        BreakpointUpdate(IBreakpoint breakpoint) throws CoreException, IOException {
            this.breakpoint = breakpoint;
            marker_attrs = new HashMap<String,Object>(breakpoint.getMarker().getAttributes());
            marker_file = getFilePath(breakpoint.getMarker().getResource());
            launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        }
        
        synchronized void exec() throws InterruptedException {
            assert !Protocol.isDispatchThread();
            Protocol.invokeLater(this);
            wait();
        }
        
        public void run() {
            tcf_attrs = toBreakpointAttributes(marker_file, marker_attrs);
            for (int i = 0; i < launches.length; i++) {
                if (launches[i] instanceof TCFLaunch) {
                    final TCFLaunch launch = (TCFLaunch)launches[i];
                    final IChannel channel = launch.getChannel();
                    if (channel == null) continue;
                    if (channel.getState() != IChannel.STATE_OPEN) continue;
                    service = channel.getRemoteService(IBreakpoints.class);
                    if (service == null) continue;
                    if (!isSupported(channel, breakpoint)) continue;
                    done = new IBreakpoints.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            if (error != null) channel.terminate(error);
                            else done(launch);
                        }
                    };
                    update();
                }
            }
            Protocol.sync(new Runnable() {
                public void run() {
                    synchronized (BreakpointUpdate.this) {
                        BreakpointUpdate.this.notify();
                    }
                }
            });
        };
        
        void done(TCFLaunch launch) {
        }

        abstract void update();
    }

    private String getFilePath(IResource resource) throws IOException {
        if (resource == ResourcesPlugin.getWorkspace().getRoot()) return null;
        IPath p = resource.getRawLocation();
        if (p == null) return null;
        return p.toFile().getCanonicalPath();
    }

    public void breakpointAdded(IBreakpoint breakpoint) {
        try {
            if (!breakpoint.getModelIdentifier().equals(ITCFConstants.ID_TCF_DEBUG_MODEL)) return;
            new BreakpointUpdate(breakpoint) {
                @Override
                void update() {
                    service.add(tcf_attrs, done);
                }
            }.exec();
        }
        catch (Throwable x) {
            TCFCore.log("Unhandled exception in breakpoint listener", x);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Set<String> calcMarkerDeltaKeys(IMarker marker, IMarkerDelta delta) throws CoreException {
        assert delta.getKind() == IResourceDelta.CHANGED;
        Map<String,Object> m0 = delta.getAttributes();
        Map<String,Object> m1 = marker.getAttributes();
        Set<String> keys = new HashSet<String>();
        if (m0 != null) keys.addAll(m0.keySet());
        if (m1 != null) keys.addAll(m1.keySet());
        for (Iterator<String> i = keys.iterator(); i.hasNext();) {
            String key = i.next();
            Object v0 = m0 != null ? m0.get(key) : null;
            Object v1 = m1 != null ? m1.get(key) : null;
            if (v0 instanceof String && ((String)v0).length() == 0) v0 = null;
            if (v1 instanceof String && ((String)v1).length() == 0) v1 = null;
            if (v0 instanceof Boolean && !((Boolean)v0).booleanValue()) v0 = null;
            if (v1 instanceof Boolean && !((Boolean)v1).booleanValue()) v1 = null;
            if ((v0 == null) != (v1 == null)) continue;
            if (v0 != null && !v0.equals(v1)) continue;
            i.remove();
        }
        return keys;
    }

    public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
        try {
            if (!breakpoint.getModelIdentifier().equals(ITCFConstants.ID_TCF_DEBUG_MODEL)) return;
            final Set<String> s = calcMarkerDeltaKeys(breakpoint.getMarker(), delta);
            if (s.isEmpty()) return;
            new BreakpointUpdate(breakpoint) {
                @Override
                void update() {
                    if (s.size() == 1 && s.contains(IBreakpoint.ENABLED)) {
                        Boolean enabled = (Boolean)tcf_attrs.get(IBreakpoints.PROP_ENABLED);
                        if (enabled == null || !enabled.booleanValue()) {
                            service.disable(new String[]{ (String)tcf_attrs.get(PROP_ID) }, done);
                        }
                        else {
                            service.enable(new String[]{ (String)tcf_attrs.get(PROP_ID) }, done);
                        }
                    }
                    else {
                        service.change(tcf_attrs, done);
                    }
                }
            }.exec();
        }
        catch (Throwable x) {
            TCFCore.log("Unhandled exception in breakpoint listener", x);
        }
    }

    public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
        try {
            if (!breakpoint.getModelIdentifier().equals(ITCFConstants.ID_TCF_DEBUG_MODEL)) return;
            new BreakpointUpdate(breakpoint) {
                @Override
                void update() {
                    service.remove(new String[]{ (String)tcf_attrs.get(PROP_ID) }, done);
                }
                @Override
                void done(TCFLaunch launch) {
                    launch.getBreakpointsStatus().removeStatus((String)tcf_attrs.get(PROP_ID));
                }
            }.exec();
        }
        catch (Throwable x) {
            TCFCore.log("Unhandled exception in breakpoint listener", x);
        }
    }
    
    public Map<String,Object> toMarkerAttributes(Map<String,Object> p) {
        assert Protocol.isDispatchThread();
        Map<String,Object> m = new HashMap<String,Object>();
        for (Iterator<Map.Entry<String,Object>> i = p.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String,Object> e = i.next();
            String key = e.getKey();
            Object val = e.getValue();
            if (key.equals(IBreakpoints.PROP_ENABLED)) continue;
            if (key.equals(IBreakpoints.PROP_FILE)) continue;
            if (key.equals(IBreakpoints.PROP_LINE)) continue;
            if (key.equals(IBreakpoints.PROP_COLUMN)) continue;
            m.put(ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + key, val);
        }
        Boolean enabled = (Boolean)p.get(IBreakpoints.PROP_ENABLED);
        if (enabled == null) m.put(IBreakpoint.ENABLED, Boolean.FALSE);
        else m.put(IBreakpoint.ENABLED, enabled);
        m.put(IBreakpoint.REGISTERED, Boolean.TRUE);
        m.put(IBreakpoint.PERSISTED, Boolean.TRUE);
        m.put(IBreakpoint.ID, ITCFConstants.ID_TCF_DEBUG_MODEL);
        String msg = "";
        if (p.get(IBreakpoints.PROP_ADDRESS) != null) msg += p.get(IBreakpoints.PROP_ADDRESS); 
        m.put(IMarker.MESSAGE, "Breakpoint: " + msg);
        Number line = (Number)p.get(IBreakpoints.PROP_LINE);
        if (line != null) {
            m.put(IMarker.LINE_NUMBER, Integer.toString(line.intValue() + 1));
            Number column = (Number)p.get(IBreakpoints.PROP_COLUMN);
            if (column != null) {
                m.put(IMarker.CHAR_START, column.toString());
                m.put(IMarker.CHAR_END, Integer.toString(column.intValue() + 1));
            }
        }
        return m;
    }
    
    public Map<String,Object> toBreakpointAttributes(String file, Map<String,Object> p) {
        assert Protocol.isDispatchThread();
        Map<String,Object> m = new HashMap<String,Object>();
        for (Iterator<Map.Entry<String,Object>> i = p.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String,Object> e = i.next();
            String key = e.getKey();
            Object val = e.getValue();
            if (!key.startsWith(ITCFConstants.ID_TCF_DEBUG_MODEL)) continue;
            m.put(key.substring(ITCFConstants.ID_TCF_DEBUG_MODEL.length() + 1), val);
        }
        Boolean enabled = (Boolean)p.get(IBreakpoint.ENABLED);
        if (enabled != null && enabled.booleanValue() && bp_manager.isEnabled()) {
            m.put(IBreakpoints.PROP_ENABLED, enabled);
        }
        if (file != null) {
            m.put(IBreakpoints.PROP_FILE, file);
            String line = (String)p.get(IMarker.LINE_NUMBER);
            if (line != null) {
                m.put(IBreakpoints.PROP_LINE, new Integer(Integer.parseInt(line) - 1));
                String column = (String)p.get(IMarker.CHAR_START);
                if (column != null) {
                    m.put(IBreakpoints.PROP_COLUMN, new Integer(column));
                }
            }
        }
        return m;
    }
}
