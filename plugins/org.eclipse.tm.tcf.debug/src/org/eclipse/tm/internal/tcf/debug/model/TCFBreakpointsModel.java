/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.tm.internal.tcf.debug.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;


/**
 * TCFBreakpointsModel class handles breakpoints for all active TCF launches.
 * It downloads initial set of breakpoint data when launch is activated,
 * listens for Eclipse breakpoint manager events and propagates breakpoint changes to TCF targets.
 */
public class TCFBreakpointsModel implements IBreakpointListener, IBreakpointManagerListener {

    private final IBreakpointManager bp_manager = DebugPlugin.getDefault().getBreakpointManager();
    private final HashSet<IChannel> channels = new HashSet<IChannel>();
    private final HashMap<String,IBreakpoint> id2bp = new HashMap<String,IBreakpoint>();

    private boolean disposed;

    public static TCFBreakpointsModel getBreakpointsModel() {
        return Activator.getBreakpointsModel();
    }

    public void dispose() {
        bp_manager.removeBreakpointListener(this);
        bp_manager.removeBreakpointManagerListener(this);
        channels.clear();
        disposed = true;
    }

    public boolean isSupported(IChannel channel, IBreakpoint bp) {
        // TODO: implement per-channel breakpoint filtering
        return true;
    }

    /**
     * Get TCF ID of a breakpoint.
     * @param bp - IBreakpoint object.
     * @return TCF ID of the breakpoint.
     * @throws CoreException
     */
    public String getBreakpointID(IBreakpoint bp) throws CoreException {
        IMarker marker = bp.getMarker();
        String id = (String)marker.getAttributes().get(ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_ID);
        if (id != null) return id;
        id = marker.getResource().getLocationURI().toString();
        if (id == null) return null;
        return id + ':' + marker.getId();
    }

    /**
     * Get IBreakpoint for given TCF breakpoint ID.
     * The mapping works only for breakpoints that were sent to a debug target.
     * It can be used to map target responses to IBreakpoint objects.
     * @param id - TCF breakpoint ID.
     * @return IBreakpoint object associated with the ID, or null.
     */
    public IBreakpoint getBreakpoint(String id) {
        assert Protocol.isDispatchThread();
        return id2bp.get(id);
    }

    @SuppressWarnings("unchecked")
    public void downloadBreakpoints(final IChannel channel, final Runnable done) throws IOException, CoreException {
        assert Protocol.isDispatchThread();
        assert !disposed;
        IBreakpoints service = channel.getRemoteService(IBreakpoints.class);
        if (service != null) {
            if (channels.isEmpty()) {
                bp_manager.addBreakpointListener(this);
                bp_manager.addBreakpointManagerListener(this);
            }
            channels.add(channel);
            channel.addChannelListener(new IChannel.IChannelListener() {
                public void congestionLevel(int level) {
                }
                public void onChannelClosed(Throwable error) {
                    if (disposed) return;
                    channels.remove(channel);
                    if (channels.isEmpty()) {
                        bp_manager.removeBreakpointListener(TCFBreakpointsModel.this);
                        bp_manager.removeBreakpointManagerListener(TCFBreakpointsModel.this);
                        id2bp.clear();
                    }
                }
                public void onChannelOpened() {
                }
            });
            IBreakpoint[] arr = bp_manager.getBreakpoints();
            if (arr != null && arr.length > 0) {
                List<Map<String,Object>> bps = new ArrayList<Map<String,Object>>(arr.length);
                for (int i = 0; i < arr.length; i++) {
                    if (!isSupported(channel, arr[i])) continue;
                    String id = getBreakpointID(arr[i]);
                    if (id == null) continue;
                    if (!arr[i].isPersisted()) continue;
                    IMarker marker = arr[i].getMarker();
                    String file = getFilePath(marker.getResource());
                    bps.add(toBreakpointAttributes(id, file, marker.getType(), marker.getAttributes()));
                    id2bp.put(id, arr[i]);
                }
                if (!bps.isEmpty()) {
                    Map<String, Object>[] bpArr = (Map<String,Object>[]) bps.toArray(new Map[bps.size()]);
                    service.set(bpArr, new IBreakpoints.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            if (error == null) done.run();
                            else channel.terminate(error);
                        }
                    });
                    return;
                }
            }
        }
        Protocol.invokeLater(done);
    }

    public void breakpointManagerEnablementChanged(final boolean enabled) {
        try {
            IBreakpoint[] arr = bp_manager.getBreakpoints();
            if (arr == null || arr.length == 0) return;
            final Map<String,IBreakpoint> map = new HashMap<String,IBreakpoint>();
            for (int i = 0; i < arr.length; i++) {
                IMarker marker = arr[i].getMarker();
                Boolean b = marker.getAttribute(IBreakpoint.ENABLED, Boolean.FALSE);
                if (!b.booleanValue()) continue;
                String id = getBreakpointID(arr[i]);
                if (id == null) continue;
                map.put(id, arr[i]);
            }
            if (map.isEmpty()) return;
            Runnable r = new Runnable() {
                public void run() {
                    if (disposed) return;
                    for (final IChannel channel : channels) {
                        IBreakpoints service = channel.getRemoteService(IBreakpoints.class);
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
            Activator.log("Unhandled exception in breakpoint listener", x);
        }
    }

    private abstract class BreakpointUpdate implements Runnable {

        private final IBreakpoint breakpoint;
        protected final Map<String,Object> marker_attrs;
        private final String marker_file;
        private final String marker_type;
        private final String marker_id;

        IBreakpoints service;
        IBreakpoints.DoneCommand done;
        Map<String,Object> tcf_attrs;

        @SuppressWarnings("unchecked")
        BreakpointUpdate(IBreakpoint breakpoint) throws CoreException, IOException {
            this.breakpoint = breakpoint;
            marker_attrs = new HashMap<String,Object>(breakpoint.getMarker().getAttributes());
            marker_file = getFilePath(breakpoint.getMarker().getResource());
            marker_type = breakpoint.getMarker().getType();
            marker_id = getBreakpointID(breakpoint);
        }

        synchronized void exec() throws InterruptedException {
            assert !Protocol.isDispatchThread();
            if (marker_id != null) {
                Protocol.invokeLater(this);
                wait();
            }
        }

        public void run() {
            if (disposed) return;
            tcf_attrs = toBreakpointAttributes(marker_id, marker_file, marker_type, marker_attrs);
            for (final IChannel channel : channels) {
                service = channel.getRemoteService(IBreakpoints.class);
                if (!isSupported(channel, breakpoint)) continue;
                done = new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) channel.terminate(error);
                    }
                };
                update();
            }
            Protocol.sync(new Runnable() {
                public void run() {
                    synchronized (BreakpointUpdate.this) {
                        BreakpointUpdate.this.notify();
                    }
                }
            });
        };

        abstract void update();
    }

    private String getFilePath(IResource resource) throws IOException {
        if (resource == ResourcesPlugin.getWorkspace().getRoot()) return null;
        IPath p = resource.getRawLocation();
        if (p == null) return null;
        return p.toFile().getCanonicalPath();
    }

    public void breakpointAdded(final IBreakpoint breakpoint) {
        try {
            new BreakpointUpdate(breakpoint) {
                @Override
                void update() {
                    if (!Boolean.FALSE.equals(marker_attrs.get(IBreakpoint.PERSISTED))) {
                        service.add(tcf_attrs, done);
                    }
                    id2bp.put((String)tcf_attrs.get(IBreakpoints.PROP_ID), breakpoint);
                }
            }.exec();
        }
        catch (Throwable x) {
            Activator.log("Unhandled exception in breakpoint listener", x);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> calcMarkerDeltaKeys(IMarker marker, IMarkerDelta delta) throws CoreException {
        Set<String> keys = new HashSet<String>();
        if (delta == null) return keys;
        Map<String,Object> m0 = delta.getAttributes();
        Map<String,Object> m1 = marker.getAttributes();
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
        keys.remove("org.eclipse.cdt.debug.core.installCount");
        return keys;
    }

    public void breakpointChanged(final IBreakpoint breakpoint, IMarkerDelta delta) {
        try {
            final Set<String> s = calcMarkerDeltaKeys(breakpoint.getMarker(), delta);
            if (s.isEmpty()) return;
            new BreakpointUpdate(breakpoint) {
                @Override
                void update() {
                    String id = (String)tcf_attrs.get(IBreakpoints.PROP_ID);
                    if (s.size() == 1 && s.contains(IBreakpoint.ENABLED)) {
                        Boolean enabled = (Boolean)tcf_attrs.get(IBreakpoints.PROP_ENABLED);
                        if (enabled == null || !enabled.booleanValue()) {
                            service.disable(new String[]{ id }, done);
                        }
                        else {
                            service.enable(new String[]{ id }, done);
                        }
                    }
                    else {
                        service.change(tcf_attrs, done);
                    }
                    id2bp.put(id, breakpoint);
                }
            }.exec();
        }
        catch (Throwable x) {
            Activator.log("Unhandled exception in breakpoint listener", x);
        }
    }

    public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
        try {
            new BreakpointUpdate(breakpoint) {
                @Override
                void update() {
                    String id = (String)tcf_attrs.get(IBreakpoints.PROP_ID);
                    if (!Boolean.FALSE.equals(marker_attrs.get(IBreakpoint.PERSISTED))) {
                        service.remove(new String[]{ id }, done);
                    }
                    id2bp.remove(id);
                }
            }.exec();
        }
        catch (Throwable x) {
            Activator.log("Unhandled exception in breakpoint listener", x);
        }
    }

    public Map<String,Object> toMarkerAttributes(Map<String,Object> p) {
        assert !disposed;
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
        if (p.get(IBreakpoints.PROP_LOCATION) != null) msg += p.get(IBreakpoints.PROP_LOCATION);
        m.put(IMarker.MESSAGE, "Breakpoint: " + msg);
        Number line = (Number)p.get(IBreakpoints.PROP_LINE);
        if (line != null) {
            m.put(IMarker.LINE_NUMBER, new Integer(line.intValue()));
            Number column = (Number)p.get(IBreakpoints.PROP_COLUMN);
            if (column != null) {
                m.put(IMarker.CHAR_START, new Integer(column.intValue()));
                m.put(IMarker.CHAR_END, new Integer(column.intValue() + 1));
            }
        }
        return m;
    }

    public Map<String,Object> toBreakpointAttributes(String id, String file, String type, Map<String,Object> p) {
        assert !disposed;
        assert Protocol.isDispatchThread();
        Map<String,Object> m = new HashMap<String,Object>();
        m.put(IBreakpoints.PROP_ID, id);
        for (Iterator<Map.Entry<String,Object>> i = p.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String,Object> e = i.next();
            String key = e.getKey();
            Object val = e.getValue();
            if (!key.startsWith(ITCFConstants.ID_TCF_DEBUG_MODEL)) continue;
            String tcfKey = key.substring(ITCFConstants.ID_TCF_DEBUG_MODEL.length() + 1);
            if (IBreakpoints.PROP_CONTEXTIDS.equals(tcfKey)) {
                val = ((String) val).split(",\\s*");
            }
            m.put(tcfKey, val);
        }
        Boolean enabled = (Boolean)p.get(IBreakpoint.ENABLED);
        if (enabled != null && enabled.booleanValue() && bp_manager.isEnabled()) {
            m.put(IBreakpoints.PROP_ENABLED, enabled);
        }
        if (file == null) file = (String)p.get("org.eclipse.cdt.debug.core.sourceHandle");
        if (file != null) {
            // Map file path to remote file system
            int i = file.lastIndexOf('/');
            int j = file.lastIndexOf('\\');
            String name = file;
            if (i > j) name = file.substring(i + 1);
            else if (i < j) name = file.substring(j + 1);
            m.put(IBreakpoints.PROP_FILE, name);
            Integer line = (Integer)p.get(IMarker.LINE_NUMBER);
            if (line != null) {
                m.put(IBreakpoints.PROP_LINE, new Integer(line.intValue()));
                Integer column = (Integer)p.get(IMarker.CHAR_START);
                if (column != null) m.put(IBreakpoints.PROP_COLUMN, column);
            }
        }
        if ("org.eclipse.cdt.debug.core.cWatchpointMarker".equals(type)) {
            String expr = (String)p.get("org.eclipse.cdt.debug.core.expression");
            if (expr != null && expr.length() != 0) {
                boolean writeAccess = Boolean.TRUE.equals(p.get("org.eclipse.cdt.debug.core.write"));
                boolean readAccess = Boolean.TRUE.equals(p.get("org.eclipse.cdt.debug.core.read"));
                int accessMode = 0;
                if (readAccess) accessMode |= IBreakpoints.ACCESSMODE_READ;
                if (writeAccess) accessMode |= IBreakpoints.ACCESSMODE_WRITE;
                m.put(IBreakpoints.PROP_ACCESSMODE, Integer.valueOf(accessMode));
                m.put(IBreakpoints.PROP_LOCATION, "&(" + expr + ')');
            }
        }
        else if ("org.eclipse.cdt.debug.core.cFunctionBreakpointMarker".equals(type)) {
            String expr = (String)p.get("org.eclipse.cdt.debug.core.function");
            if (expr != null && expr.length() != 0) {
                m.put(IBreakpoints.PROP_LOCATION, expr);
            }
        }
        else if (file == null) {
            String address = (String)p.get("org.eclipse.cdt.debug.core.address");
            if (address != null && address.length() > 0) m.put(IBreakpoints.PROP_LOCATION, address);
        }
        String condition = (String)p.get("org.eclipse.cdt.debug.core.condition");
        if (condition != null && condition.length() > 0) m.put(IBreakpoints.PROP_CONDITION, condition);
        Integer skip_count = (Integer)p.get("org.eclipse.cdt.debug.core.ignoreCount");
        if (skip_count != null && skip_count.intValue() > 0) m.put(IBreakpoints.PROP_IGNORECOUNT, skip_count);
        return m;
    }
}
