/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Anna Dushistova (MontaVista) - [246996] [tcf] NullPointerException when trying to copy the process
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.processes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ISysMonitor;
import org.eclipse.tm.tcf.services.ISysMonitor.SysMonitorContext;

public class TCFProcessResource extends AbstractResource implements IHostProcess {

    public static final String PROP_PC_UTIME = "PCUTime"; //$NON-NLS-1$
    public static final String PROP_PC_STIME = "PCSTime"; //$NON-NLS-1$

    private final TCFProcessService rse_service;
    private final ISysMonitor tcf_service;
    private final TCFProcessResource prev;
    private final String id;

    private Throwable error;
    private ISysMonitor.SysMonitorContext context;

    private final List<Runnable> children_wait_list = new ArrayList<Runnable>();
    private final HashMap<String,TCFProcessResource> children = new HashMap<String,TCFProcessResource>();
    private boolean children_loading;
    private boolean children_loaded;
    private Throwable children_error;
    private boolean running_wait_list;

    private long timestamp;

    private final String[] propertyKeys = new String[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_COUNT];
    {
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_EXENAME] = ISysMonitor.PROP_FILE;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_GID] = ISysMonitor.PROP_GROUPNAME;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_PID] = ISysMonitor.PROP_PID;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_PPID] = ISysMonitor.PROP_PPID;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_STATUS] = ISysMonitor.PROP_STATE;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_TGID] = ISysMonitor.PROP_TGID;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_TRACERPID] = ISysMonitor.PROP_TRACERPID;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_UID] = ISysMonitor.PROP_UID;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_USERNAME] = ISysMonitor.PROP_USERNAME;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_VMSIZE] = ISysMonitor.PROP_VSIZE;
        propertyKeys[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_VMRSS] = ISysMonitor.PROP_RSS;
    }

    private Map<String, Object> properties = new HashMap<String, Object>();

    TCFProcessResource(TCFProcessService rse_service, ISysMonitor service,
            TCFProcessResource prev, String id) {
        this.rse_service = rse_service;
        this.tcf_service = service;
        this.prev = prev;
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public String getParentID() {
        return (String)properties.get(ISysMonitor.PROP_PARENTID);
    }

    public TCFProcessService getService() {
        return rse_service;
    }

    public void invalidate() {
        assert Protocol.isDispatchThread();
        error = null;
        context = null;
    }

    public boolean validate(final Runnable done) {
        assert Protocol.isDispatchThread();
        if (error != null) return true;
        if (context != null) return true;
        tcf_service.getContext(id, new ISysMonitor.DoneGetContext() {

            public void doneGetContext(IToken token, Exception error, SysMonitorContext context) {
                TCFProcessResource.this.error = error;
                TCFProcessResource.this.context = context;
                timestamp = System.currentTimeMillis();
                if (error != null) {
                    properties = new HashMap<String,Object>();
                }
                else {
                    properties = new HashMap<String,Object>(context.getProperties());
                    if (prev != null &&  timestamp > prev.timestamp) {
                        setPCProperty(PROP_PC_UTIME, ISysMonitor.PROP_UTIME);
                        setPCProperty(PROP_PC_STIME, ISysMonitor.PROP_STIME);
                    }
                    // Conversions are necessary for sorting to work
                    toLong(ISysMonitor.PROP_PID);
                    toLong(ISysMonitor.PROP_PPID);
                    toLong(ISysMonitor.PROP_UTIME);
                    toLong(ISysMonitor.PROP_STIME);
                    toLong(ISysMonitor.PROP_CUTIME);
                    toLong(ISysMonitor.PROP_CSTIME);
                    toLong(ISysMonitor.PROP_STARTTIME);
                    toLong(ISysMonitor.PROP_ITREALVALUE);
                    toBigInteger(ISysMonitor.PROP_CODESTART);
                    toBigInteger(ISysMonitor.PROP_CODEEND);
                    toBigInteger(ISysMonitor.PROP_STACKSTART);
                    toBigInteger(ISysMonitor.PROP_WCHAN);
                }
                Protocol.invokeLater(done);
            }

        });
        return false;
    }

    private void toLong(String name) {
        Number n = (Number)properties.get(name);
        if (n == null || n instanceof Long) return;
        properties.put(name, Long.valueOf(n.longValue()));
    }

    private void toBigInteger(String name) {
        Number n = (Number)properties.get(name);
        if (n == null || n instanceof BigInteger) return;
        properties.put(name, new BigInteger(n.toString()));
    }

    private void setPCProperty(String property, String name) {
        Object x = prev.properties.get(name);
        Object y = properties.get(name);
        if (x instanceof Number && y instanceof Number) {
            BigInteger nx = x instanceof BigInteger ? (BigInteger) x
                    : new BigInteger(x.toString());
            BigInteger ny = y instanceof BigInteger ? (BigInteger) y
                    : new BigInteger(y.toString());
            double d = ny.subtract(nx).doubleValue()
                    / (timestamp - prev.timestamp);
            properties.put(property, d);
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Throwable getError() {
        assert Protocol.isDispatchThread();
        return error;
    }

    public ISysMonitor.SysMonitorContext getContext() {
        assert Protocol.isDispatchThread();
        return context;
    }

    // IHostProcess methods

    public String getAllProperties() {
        String result = ""; //$NON-NLS-1$
        for (int i = 0; i < ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_COUNT; i++) {
            result = result + properties.get(propertyKeys[i]);
            if (i != ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_COUNT - 1)
                result = result + IServiceConstants.TOKEN_SEPARATOR;
        }
        return result;
    }

    public long getGid() {
        Number n = (Number)properties.get(ISysMonitor.PROP_UGID);
        if (n == null) return -1;
        return n.longValue();
    }

    public String getLabel() {
        return Long.toString(getPid()) + " " + notNull(getName()); //$NON-NLS-1$
    }

    public String getName() {
        String s = (String)properties.get(ISysMonitor.PROP_FILE);
        if (s != null) {
            int i = s.lastIndexOf('/');
            int j = s.lastIndexOf('\\');
            if (j > i) i = j;
            if (i > 0) s = s.substring(i + 1);
        }
        return s;
    }

    public long getPPid() {
        Number n = (Number)properties.get(ISysMonitor.PROP_PPID);
        if (n == null) return -1;
        return n.longValue();
    }

    public long getPid() {
        Number n = (Number)properties.get(ISysMonitor.PROP_PID);
        if (n == null) return -1;
        return n.longValue();
    }

    public String getState() {
        return (String)properties.get(ISysMonitor.PROP_STATE);
    }

    public long getTgid() {
        Number n = (Number)properties.get(ISysMonitor.PROP_TGID);
        if (n == null) return -1;
        return n.longValue();
    }

    public long getTracerPid() {
        Number n = (Number)properties.get(ISysMonitor.PROP_TRACERPID);
        if (n == null) return -1;
        return n.longValue();
    }

    public long getUid() {
        Number n = (Number)properties.get(ISysMonitor.PROP_UID);
        if (n == null) return -1;
        return n.longValue();
    }

    public String getUsername() {
        return (String)properties.get(ISysMonitor.PROP_USERNAME);
    }

    public long getVmRSSInKB() {
        Number rss = (Number)properties.get(ISysMonitor.PROP_RSS);
        Number psz = (Number)properties.get(ISysMonitor.PROP_PSIZE);
        if (rss == null || psz == null) return 0;
        return (rss.longValue() * psz.longValue() + 1023) / 1024;
    }

    public long getVmSizeInKB() {
        Number vsz = (Number)properties.get(ISysMonitor.PROP_VSIZE);
        if (vsz == null) return 0;
        return (vsz.longValue() + 1023) / 1024;
    }

    public boolean isRoot() {
        return true;
    }

    private String notNull(String s) {
        return s == null ? "" : s;
    }

    String getStatusLine() {
        final String STATUS_DELIMITER = "|"; //$NON-NLS-1$
        StringBuffer s = new StringBuffer();
        s.append(getPid()).append(STATUS_DELIMITER);
        s.append(notNull(getName())).append(STATUS_DELIMITER);
        s.append(notNull(getState())).append(STATUS_DELIMITER);
        s.append(getTgid()).append(STATUS_DELIMITER);
        s.append(getPPid()).append(STATUS_DELIMITER);
        s.append('0').append(STATUS_DELIMITER);
        s.append(getUid()).append(STATUS_DELIMITER);
        s.append(notNull(getUsername())).append(STATUS_DELIMITER);
        s.append(getGid()).append(STATUS_DELIMITER);
        s.append(getVmSizeInKB()).append(STATUS_DELIMITER);
        s.append(getVmRSSInKB()).append(STATUS_DELIMITER);
        return s.toString();
    }

    public Map<String,Object> getProperties() {
        return properties;
    }

    private void runChildrenWaitList() {
        assert !children_loading;
        assert children_loaded;
        try {
            running_wait_list = true;
            for (Runnable r : children_wait_list) r.run();
            children_wait_list.clear();
        }
        finally {
            running_wait_list = false;
        }
    }

    public Throwable getChildrenError() {
        return children_error;
    }

    public void flushChildrenCache() {
        Map<Long,TCFProcessResource> pid2res = rse_service.getProcessCache();
        for (TCFProcessResource r : children.values()) {
            long pid = r.getPid();
            if (pid > 0 && getPid() != pid) pid2res.remove(r.getPid());
        }
        children_loaded = false;
        children_error = null;
    }

    public boolean loadChildren(Runnable run) {
        if (children_loaded) return true;
        assert !running_wait_list;
        children_wait_list.add(run);
        if (children_loading) return false;
        children_loading = true;
        try {
            final ISysMonitor m = rse_service.getTCFConnectorService().getSysMonitorService();
            m.getChildren(getID(), new ISysMonitor.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] ids) {
                    try {
                        if (error != null) {
                            loadProcessesDone(error, null);
                        }
                        else if (ids == null) {
                            loadProcessesDone(null, new TCFProcessResource[0]);
                        }
                        else {
                            final TCFProcessResource[] arr = new TCFProcessResource[ids.length];
                            final Set<IHostProcess> pending = new HashSet<IHostProcess>();
                            for (int i = 0; i < ids.length; i++) {
                                final TCFProcessResource r = new TCFProcessResource(
                                        rse_service, m, children.get(ids[i]), ids[i]);
                                if (!r.validate(new Runnable() {
                                    public void run() {
                                        pending.remove(r);
                                        if (pending.isEmpty()) loadProcessesDone(null, arr);
                                    }
                                })) pending.add(r);
                                arr[i] = r;
                            }
                            if (pending.isEmpty()) loadProcessesDone(null, arr);
                        }
                    }
                    catch (Throwable x) {
                        loadProcessesDone(x, null);
                    }
                }
            });
            return false;
        }
        catch (Throwable x) {
            loadProcessesDone(x, null);
            return true;
        }
    }

    private void loadProcessesDone(Throwable error, TCFProcessResource[] arr) {
        assert children_loading;
        children_loading = false;
        children_loaded = true;
        children.clear();
        if (arr != null && error == null) {
            Map<Long,TCFProcessResource> pid2res = rse_service.getProcessCache();
            for (TCFProcessResource r : arr) {
                long pid = r.getPid();
                if (pid > 0 && getPid() != pid) pid2res.put(pid, r);
                if (r.getError() == null) children.put(r.getID(), r);
            }
        }
        children_error = error;
        runChildrenWaitList();
    }

    @Override
    public String toString() {
        return "[" + getStatusLine() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
