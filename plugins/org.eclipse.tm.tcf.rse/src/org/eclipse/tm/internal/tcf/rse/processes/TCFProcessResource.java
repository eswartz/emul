/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.processes;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ISysMonitor;
import org.eclipse.tm.tcf.services.ISysMonitor.SysMonitorContext;


public class TCFProcessResource extends AbstractResource implements IHostProcess {
    
    private final TCFProcessService rse_service;
    private final ISysMonitor tcf_service;
    private final TCFProcessResource prev;
    private final String id;
    private Throwable error;
    private ISysMonitor.SysMonitorContext context;
    
    private final List<Runnable> children_wait_list = new ArrayList<Runnable>();
    private boolean children_loading;
    private boolean children_loaded;
    private Throwable children_error;
    private boolean running_wait_list;
    
    private long gid = -1;
    private String name;
    private long ppid = -1;
    private long pid = -1;
    private String state;
    private long tgid = -1;
    private long tracepid = -1;
    private long uid;
    private String username;
    private long vm_rss_kb;
    private long vm_size_kb;
    private String utime_pc;
    private String stime_pc;
    private long timestamp;
    
    private Map<String, Object> properties = new HashMap<String, Object>();
    
    private static final NumberFormat percent_format;
    
    static {
        percent_format = NumberFormat.getPercentInstance();
        percent_format.setMaximumFractionDigits(3);
    }
    
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
                if (error != null) {
                    properties = new HashMap<String, Object>();
                    gid = -1;
                    name = null;
                    ppid = -1;
                    pid = -1;
                    state = null;
                    tgid = -1;
                    tracepid = -1;
                    uid = 0;
                    username = null;
                    vm_rss_kb = 0;
                    vm_size_kb = 0;
                }
                else {
                    properties = new HashMap<String, Object>(context.getProperties());
                    gid = context.getUGID();
                    name = context.getFile();
                    if (properties.containsKey(ISysMonitor.PROP_PPID)) {
                        ppid = context.getPPID();
                    }
                    pid = context.getPID();
                    state = context.getState();
                    tgid = context.getTGID();
                    if (properties.containsKey(ISysMonitor.PROP_TRACERPID)) {
                        tracepid = context.getTracerPID();
                    }
                    uid = context.getUID();
                    username = context.getUserName();
                    vm_rss_kb = context.getRSS();
                    long page_bytes = context.getPSize();
                    if (page_bytes <= 0 || vm_rss_kb < 0) {
                        vm_rss_kb = -1;
                    }
                    else {
                        vm_rss_kb = (vm_rss_kb * page_bytes + 1023) / 1024;
                    }
                    vm_size_kb = (context.getVSize() + 1023) / 1024;
                }
                timestamp = System.currentTimeMillis();
                Protocol.invokeLater(done);
            }
            
        });
        return false;
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
        // TODO Auto-generated method stub
        return null;
    }

    public long getGid() {
        return gid;
    }

    public String getLabel() {
        return Long.toString(getPid()) + " " + name;
    }

    public String getName() {
        return name;
    }

    public long getPPid() {
        return ppid;
    }

    public long getPid() {
        return pid;
    }

    public String getState() {
        return state;
    }

    public long getTgid() {
        return tgid;
    }

    public long getTracerPid() {
        return tracepid;
    }

    public long getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public long getVmRSSInKB() {
        return vm_rss_kb;
    }

    public long getVmSizeInKB() {
        return vm_size_kb;
    }

    public boolean isRoot() {
        return true;
    }
    
    String getStatusLine() {
        final String STATUS_DELIMITER = "|"; //$NON-NLS-1$
        StringBuffer s = new StringBuffer();
        s.append(getPid()).append(STATUS_DELIMITER);
        s.append(name).append(STATUS_DELIMITER);
        s.append(getState()).append(STATUS_DELIMITER);
        s.append(getTgid()).append(STATUS_DELIMITER);
        s.append(getPPid()).append(STATUS_DELIMITER);
        s.append('0').append(STATUS_DELIMITER);
        s.append(getUid()).append(STATUS_DELIMITER);
        s.append(getUsername()).append(STATUS_DELIMITER);
        s.append(getGid()).append(STATUS_DELIMITER);
        s.append(getVmSizeInKB()).append(STATUS_DELIMITER);
        s.append(getVmRSSInKB()).append(STATUS_DELIMITER);
        return s.toString();
    }

    public Map<String,Object> getProperties() {
        return properties;
    }
    
    private String getTimePC(String name) {
        if (prev == null) return null;
        Object x = prev.properties.get(name);
        Object y = properties.get(name);
        if (x instanceof Number && y instanceof Number) {
            BigInteger nx = x instanceof BigInteger ? (BigInteger)x : new BigInteger(x.toString());
            BigInteger ny = y instanceof BigInteger ? (BigInteger)y : new BigInteger(y.toString());
            double d = ny.subtract(nx).doubleValue() / (timestamp - prev.timestamp);
            return percent_format.format(d);
        }
        return null;
    }
    
    public String getUserTimePC() {
        if (utime_pc != null) return utime_pc;
        return utime_pc = getTimePC(ISysMonitor.PROP_UTIME);
    }

    public String getSysTimePC() {
        if (stime_pc != null) return stime_pc;
        return stime_pc = getTimePC(ISysMonitor.PROP_STIME);
    }
    
    public boolean getChildrenLoading() {
        return children_loading;
    }
    
    public void setChildrenLoading(boolean b) {
        children_loading = b;
    }
    
    public boolean getChildrenLoaded() {
        return children_loaded;
    }
    
    public void setChildrenLoaded(boolean b) {
        children_loaded = b;
    }
    
    public Throwable getChildrenError() {
        return children_error;
    }
    
    public void setChildrenError(Throwable error) {
        children_error = error;
    }
    
    public void addChildrenWaitList(Runnable run) {
        assert !running_wait_list;
        assert children_loading;
        assert !children_loaded;
        children_wait_list.add(run);
    }
    
    public void runChildrenWaitList() {
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
    
    public void cancelChildrenLoading() {
        // TODO: cancelChildrenLoading
    }
    
    public String toString() {
        return "[" + getStatusLine() + "]";
    }
}
