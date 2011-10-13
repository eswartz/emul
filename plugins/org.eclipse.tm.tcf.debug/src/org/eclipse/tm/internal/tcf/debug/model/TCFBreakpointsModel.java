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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
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

    public static final String
        CDATA_CLIENT_ID = "ClientID",
        CDATA_TYPE = "Type",
        CDATA_FILE = "File",
        CDATA_MARKER = "Marker";

    public static final String
        ATTR_INSTALL_COUNT = "org.eclipse.cdt.debug.core.installCount",
        ATTR_ADDRESS = "org.eclipse.cdt.debug.core.address",
        ATTR_FUNCTION = "org.eclipse.cdt.debug.core.function",
        ATTR_EXPRESSION = "org.eclipse.cdt.debug.core.expression",
        ATTR_READ = "org.eclipse.cdt.debug.core.read",
        ATTR_WRITE = "org.eclipse.cdt.debug.core.write",
        ATTR_SIZE = "org.eclipse.cdt.debug.core.range",
        ATTR_FILE = "org.eclipse.cdt.debug.core.sourceHandle",
        ATTR_CONDITION = "org.eclipse.cdt.debug.core.condition",
        ATTR_IGNORE_COUNT = "org.eclipse.cdt.debug.core.ignoreCount";

    private final IBreakpointManager bp_manager = DebugPlugin.getDefault().getBreakpointManager();
    private final HashMap<IChannel,Map<String,Object>> channels = new HashMap<IChannel,Map<String,Object>>();
    private final HashMap<String,IBreakpoint> id2bp = new HashMap<String,IBreakpoint>();
    private final String client_id = UUID.randomUUID().toString();

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
    public static String getBreakpointID(IBreakpoint bp) throws CoreException {
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

    public String getClientID() {
        return client_id;
    }

    @SuppressWarnings("unchecked")
    public String getClientID(Map<String,Object> properties) {
        Map<String,Object> client_data = (Map<String,Object>)properties.get(IBreakpoints.PROP_CLIENT_DATA);
        if (client_data == null) return null;
        return (String)client_data.get(TCFBreakpointsModel.CDATA_CLIENT_ID);
    }

    @SuppressWarnings("unchecked")
    public void downloadBreakpoints(final IChannel channel, final Runnable done) throws IOException, CoreException {
        assert !disposed;
        assert Protocol.isDispatchThread();
        final IBreakpoints service = channel.getRemoteService(IBreakpoints.class);
        if (service != null) {
            service.getCapabilities(null, new IBreakpoints.DoneGetCapabilities() {
                public void doneGetCapabilities(IToken token, Exception error, Map<String,Object> capabilities) {
                    if (channel.getState() != IChannel.STATE_OPEN) {
                        Protocol.invokeLater(done);
                        return;
                    }
                    if (channels.isEmpty()) {
                        bp_manager.addBreakpointListener(TCFBreakpointsModel.this);
                        bp_manager.addBreakpointManagerListener(TCFBreakpointsModel.this);
                    }
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
                    channels.put(channel, capabilities);
                    IBreakpoint[] arr = bp_manager.getBreakpoints();
                    if (arr != null && arr.length > 0) {
                        List<Map<String,Object>> bps = new ArrayList<Map<String,Object>>(arr.length);
                        for (int i = 0; i < arr.length; i++) {
                            try {
                                if (!isSupported(channel, arr[i])) continue;
                                String id = getBreakpointID(arr[i]);
                                if (id == null) continue;
                                if (!arr[i].isPersisted()) continue;
                                IMarker marker = arr[i].getMarker();
                                String file = getFilePath(marker.getResource());
                                bps.add(toBreakpointAttributes(channel, id, file, marker.getType(), marker.getAttributes()));
                                id2bp.put(id, arr[i]);
                            }
                            catch (Exception x) {
                                Activator.log("Cannot get breakpoint attributes", x);
                            }
                        }
                        if (!bps.isEmpty()) {
                            Map<String, Object>[] bp_arr = (Map<String,Object>[])bps.toArray(new Map[bps.size()]);
                            service.set(bp_arr, new IBreakpoints.DoneCommand() {
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
            });
        }
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
                    for (final IChannel channel : channels.keySet()) {
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
            for (final IChannel channel : channels.keySet()) {
                tcf_attrs = toBreakpointAttributes(channel, marker_id, marker_file, marker_type, marker_attrs);
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
        keys.remove(ATTR_INSTALL_COUNT);
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

    @SuppressWarnings("unchecked")
    public Map<String,Object> toMarkerAttributes(Map<String,Object> p) {
        assert !disposed;
        assert Protocol.isDispatchThread();
        Map<String,Object> client_data = (Map<String,Object>)p.get(IBreakpoints.PROP_CLIENT_DATA);
        if (client_data != null) {
            Map<String,Object> m = (Map<String,Object>)client_data.get(CDATA_MARKER);
            if (m != null) return m;
        }
        Map<String,Object> m = new HashMap<String,Object>();
        for (Map.Entry<String,Object> e : p.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            if (key.equals(IBreakpoints.PROP_ENABLED)) continue;
            if (key.equals(IBreakpoints.PROP_FILE)) continue;
            if (key.equals(IBreakpoints.PROP_LINE)) continue;
            if (key.equals(IBreakpoints.PROP_COLUMN)) continue;
            if (key.equals(IBreakpoints.PROP_LOCATION)) continue;
            if (key.equals(IBreakpoints.PROP_ACCESSMODE)) continue;
            if (key.equals(IBreakpoints.PROP_SIZE)) continue;
            if (key.equals(IBreakpoints.PROP_CONDITION)) continue;
            if (val instanceof String[]) {
                StringBuffer bf = new StringBuffer();
                for (String s : (String[])val) {
                    if (bf.length() > 0) bf.append(',');
                    bf.append(s);
                }
                if (bf.length() == 0) continue;
                val = bf.toString();
            }
            else if (val instanceof Collection) {
                StringBuffer bf = new StringBuffer();
                for (String s : (Collection<String>)val) {
                    if (bf.length() > 0) bf.append(',');
                    bf.append(s);
                }
                if (bf.length() == 0) continue;
                val = bf.toString();
            }
            m.put(ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + key, val);
        }
        Boolean enabled = (Boolean)p.get(IBreakpoints.PROP_ENABLED);
        if (enabled == null) m.put(IBreakpoint.ENABLED, Boolean.FALSE);
        else m.put(IBreakpoint.ENABLED, enabled);
        String location = (String)p.get(IBreakpoints.PROP_LOCATION);
        if (location != null && location.length() > 0) {
            int access_mode = IBreakpoints.ACCESSMODE_EXECUTE;
            Number access_mode_num = (Number)p.get(IBreakpoints.PROP_ACCESSMODE);
            if (access_mode_num != null) access_mode = access_mode_num.intValue();
            if ((access_mode & IBreakpoints.ACCESSMODE_EXECUTE) != 0) {
                if (Character.isDigit(location.charAt(0))) {
                    m.put(ATTR_ADDRESS, location);
                }
                else {
                    m.put(ATTR_FUNCTION, location);
                }
            }
            else {
                m.put(ATTR_EXPRESSION, location.replaceFirst("^&\\((.+)\\)$", "$1"));
                m.put(ATTR_READ, (access_mode & IBreakpoints.ACCESSMODE_READ) != 0);
                m.put(ATTR_WRITE, (access_mode & IBreakpoints.ACCESSMODE_WRITE) != 0);
            }
            Number size_num = (Number)p.get(IBreakpoints.PROP_SIZE);
            if (size_num != null) m.put(ATTR_SIZE, size_num.toString());
        }
        m.put(IBreakpoint.REGISTERED, Boolean.TRUE);
        m.put(IBreakpoint.PERSISTED, Boolean.TRUE);
        m.put(IBreakpoint.ID, ITCFConstants.ID_TCF_DEBUG_MODEL);
        String msg = "";
        if (location != null) msg += location;
        m.put(IMarker.MESSAGE, "Breakpoint: " + msg);
        String file = (String)p.get(IBreakpoints.PROP_FILE);
        if (file != null && file.length() > 0) {
            m.put(ATTR_FILE, file);
        }
        Number line = (Number)p.get(IBreakpoints.PROP_LINE);
        if (line != null) {
            m.put(IMarker.LINE_NUMBER, new Integer(line.intValue()));
            Number column = (Number)p.get(IBreakpoints.PROP_COLUMN);
            if (column != null) {
                m.put(IMarker.CHAR_START, new Integer(column.intValue()));
                m.put(IMarker.CHAR_END, new Integer(column.intValue() + 1));
            }
        }
        String condition = (String)p.get(IBreakpoints.PROP_CONDITION);
        if (condition != null && condition.length() > 0) m.put(ATTR_CONDITION, condition);
        Number ignore_count = (Number)p.get(IBreakpoints.PROP_IGNORECOUNT);
        if (ignore_count != null) m.put(ATTR_IGNORE_COUNT, ignore_count);
        return m;
    }

    public Map<String,Object> toBreakpointAttributes(IChannel channel, String id, String file, String type, Map<String,Object> p) {
        assert !disposed;
        assert Protocol.isDispatchThread();
        Map<String,Object> m = new HashMap<String,Object>();
        Map<String,Object> capabilities = channels.get(channel);
        Map<String,Object> client_data = null;
        if (capabilities != null) {
            Object obj = capabilities.get(IBreakpoints.CAPABILITY_CLIENT_DATA);
            if (obj instanceof Boolean && ((Boolean)obj).booleanValue()) client_data = new HashMap<String,Object>();
        }
        m.put(IBreakpoints.PROP_ID, id);
        if (client_data != null) {
            m.put(IBreakpoints.PROP_CLIENT_DATA, client_data);
            client_data.put(CDATA_CLIENT_ID, client_id);
            if (type != null) client_data.put(CDATA_TYPE, type);
            if (file != null) client_data.put(CDATA_FILE, file);
            client_data.put(CDATA_MARKER, p);
        }
        for (Map.Entry<String,Object> e : p.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            if (key.startsWith(ITCFConstants.ID_TCF_DEBUG_MODEL)) {
                String tcf_key = key.substring(ITCFConstants.ID_TCF_DEBUG_MODEL.length() + 1);
                if (IBreakpoints.PROP_CONTEXTIDS.equals(tcf_key)) {
                    val = filterContextIds(channel, ((String)val).split(",\\s*"));
                }
                else if (IBreakpoints.PROP_CONTEXTNAMES.equals(tcf_key) ||
                        IBreakpoints.PROP_EXECUTABLEPATHS.equals(tcf_key)) {
                    val = ((String)val).split(",\\s*");
                }
                m.put(tcf_key, val);
            }
        }
        Boolean enabled = (Boolean)p.get(IBreakpoint.ENABLED);
        if (enabled != null && enabled.booleanValue() && bp_manager.isEnabled()) {
            m.put(IBreakpoints.PROP_ENABLED, enabled);
        }
        if (file == null) file = (String)p.get(ATTR_FILE);
        if (file != null && file.length() > 0) {
            String name = file;
            boolean file_mapping = false;
            if (capabilities != null) {
                Object obj = capabilities.get(IBreakpoints.CAPABILITY_FILE_MAPPING);
                if (obj instanceof Boolean) file_mapping = ((Boolean)obj).booleanValue();
            }
            if (!file_mapping) {
                int i = file.lastIndexOf('/');
                int j = file.lastIndexOf('\\');
                if (i > j) name = file.substring(i + 1);
                else if (i < j) name = file.substring(j + 1);
            }
            m.put(IBreakpoints.PROP_FILE, name);
            Integer line = (Integer)p.get(IMarker.LINE_NUMBER);
            if (line != null) {
                m.put(IBreakpoints.PROP_LINE, new Integer(line.intValue()));
                Integer column = (Integer)p.get(IMarker.CHAR_START);
                if (column != null) m.put(IBreakpoints.PROP_COLUMN, column);
            }
        }
        if (p.get(ATTR_EXPRESSION) != null) {
            String expr = (String)p.get(ATTR_EXPRESSION);
            if (expr != null && expr.length() != 0) {
                boolean writeAccess = Boolean.TRUE.equals(p.get(ATTR_WRITE));
                boolean readAccess = Boolean.TRUE.equals(p.get(ATTR_READ));
                int accessMode = 0;
                if (readAccess) accessMode |= IBreakpoints.ACCESSMODE_READ;
                if (writeAccess) accessMode |= IBreakpoints.ACCESSMODE_WRITE;
                m.put(IBreakpoints.PROP_ACCESSMODE, Integer.valueOf(accessMode));
                Object range = p.get(ATTR_SIZE);
                if (range != null) {
                    int size = Integer.parseInt(range.toString());
                    if (size > 0) m.put(IBreakpoints.PROP_SIZE, size);
                }
                if (!Character.isDigit(expr.charAt(0))) {
                    expr = "&(" + expr + ')';
                }
                m.put(IBreakpoints.PROP_LOCATION, expr);
            }
        }
        else if (p.get(ATTR_FUNCTION) != null) {
            String expr = (String)p.get(ATTR_FUNCTION);
            if (expr != null && expr.length() != 0) m.put(IBreakpoints.PROP_LOCATION, expr);
        }
        else if (file == null) {
            String address = (String)p.get(ATTR_ADDRESS);
            if (address != null && address.length() > 0) m.put(IBreakpoints.PROP_LOCATION, address);
        }
        String condition = (String)p.get(ATTR_CONDITION);
        if (condition != null && condition.length() > 0) m.put(IBreakpoints.PROP_CONDITION, condition);
        Number ignore_count = (Number)p.get(ATTR_IGNORE_COUNT);
        if (ignore_count != null && ignore_count.intValue() > 0) m.put(IBreakpoints.PROP_IGNORECOUNT, ignore_count);
        return m;
    }

    /**
     * Filter given array of scope IDs of the form sessionId/contextId
     * to those applicable to the given channel.
     */
    private String[] filterContextIds(IChannel channel, String[] scopeIds) {
        String sessionId = getSessionId(channel);
        List<String> contextIds = new ArrayList<String>();
        for (String scopeId : scopeIds) {
            if (scopeId.length() == 0) continue;
            int slash = scopeId.indexOf('/');
            if (slash < 0) {
                contextIds.add(scopeId);
            }
            else if (sessionId != null && sessionId.equals(scopeId.substring(0, slash))) {
                contextIds.add(scopeId.substring(slash+1));
            }
        }
        return (String[]) contextIds.toArray(new String[contextIds.size()]);
    }

    /**
     * @return launch config name for given channel or <code>null</code>
     */
    private String getSessionId(IChannel channel) {
        ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch launch : launches) {
            if (launch instanceof TCFLaunch) {
                if (channel == ((TCFLaunch) launch).getChannel()) {
                    ILaunchConfiguration lc = launch.getLaunchConfiguration();
                    return lc != null ? lc.getName() : null;
                }
            }
        }
        return null;
    }
}
