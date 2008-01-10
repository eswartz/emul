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
package com.windriver.tcf.rse.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.IndicatorException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.processes.IProcessService;

import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.ISysMonitor;

public class TCFProcessService implements IProcessService {
    
    private final TCFConnectorService connector;
    private final TCFProcessResource root;
    private final Map<String,TCFProcessResource> id2res = new HashMap<String,TCFProcessResource>();
    private final Map<Long,TCFProcessResource> pid2res = new HashMap<Long,TCFProcessResource>();
    
    public TCFProcessService(IHost host) {
        connector = (TCFConnectorService)TCFConnectorServiceManager
            .getInstance().getConnectorService(host, ITCFSubSystem.class);
        root = new TCFProcessResource(this, null, null, null);
    }

    public String getDescription() {
        return "The TCF Process Service uses the Target Communication Framework to provide service for the Processes subsystem." +
            " It requires a TCF agent to be running on the remote machine.";
    }

    public SystemMessage getMessage(String messageID) {
        try {
            return new SystemMessage("TCF", "C", "0001",
                    SystemMessage.ERROR, messageID, null);
        }
        catch (IndicatorException e) {
            throw new Error(e);
        }
    }

    public String getName() {
        return "TCF Process Service";
    }

    public void initService(IProgressMonitor monitor) {
    }

    public void uninitService(IProgressMonitor monitor) {
    }

    public IHostProcess getParentProcess(long PID, IProgressMonitor monitor)
            throws SystemMessageException {
        return getProcess(getProcess(PID, monitor).getPPid(), monitor);
    }

    public IHostProcess getProcess(final long PID, IProgressMonitor monitor)
            throws SystemMessageException {
        return new TCFRSETask<IHostProcess>() {
            public void run() {
                if (!loadProcesses(this, root)) return;
                if (root.getChildrenError() != null) {
                    error(root.getChildrenError());
                    return;
                }
                done(pid2res.get(PID));
            }
        }.getS(monitor, "Get process properties");
    }

    public String[] getSignalTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean kill(long PID, String signal, IProgressMonitor monitor)
            throws SystemMessageException {
        // TODO Auto-generated method stub
        return false;
    }
    
    private void sort(IHostProcess[] arr) {
        Comparator<IHostProcess> c = new Comparator<IHostProcess>() {
            public int compare(IHostProcess o1, IHostProcess o2) {
                long p1 = o1.getPid();
                long p2 = o2.getPid();
                if (p1 < p2) return -1;
                if (p1 > p2) return 1;
                return 0;
            }
        };
        Arrays.sort(arr, c);
    }

    public IHostProcess[] listAllProcesses(IProgressMonitor monitor) throws SystemMessageException {
        HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
        return listAllProcesses(rpfs, monitor);
    }

    public IHostProcess[] listAllProcesses(final IHostProcessFilter filter, IProgressMonitor monitor) throws SystemMessageException {
        return listAllProcesses(filter, root, monitor);
    }
    
    private boolean eqaulIDs(String x, String y) {
        if (x == null) return y == null;
        return x.equals(y);
    }
    
    public IHostProcess[] listAllProcesses(final IHostProcessFilter filter, final IHostProcess up, IProgressMonitor monitor) throws SystemMessageException {
        // TODO: figure out better way to flush the cache
        final TCFProcessResource parent = new TCFRSETask<TCFProcessResource>() {
            public void run() {
                TCFProcessResource parent = (TCFProcessResource)up;
                if (parent == null) {
                    error(new IOException("Invalid parent"));
                    return;
                }
                if (filter.getPpid() != null && filter.getPpid().equals("*") || parent.getChildrenError() != null) {
                    for (Iterator<TCFProcessResource> i = pid2res.values().iterator(); i.hasNext();) {
                        TCFProcessResource r = i.next();
                        if (eqaulIDs(parent.getID(), r.getParentID())) {
                            i.remove();
                            if (r.getChildrenLoaded()) r.cancelChildrenLoading();
                        }
                    }
                    parent.setChildrenLoaded(false);
                    parent.setChildrenError(null);
                }
                done(parent);
            }
        }.getS(monitor, "Flush processes cache");
        return new TCFRSETask<IHostProcess[]>() {
            public void run() {
                if (!loadProcesses(this, parent)) return;
                if (parent.getChildrenError() != null) {
                    error(parent.getChildrenError());
                    return;
                }
                List<IHostProcess> l = new ArrayList<IHostProcess>();
                for (TCFProcessResource p : pid2res.values()) {
                    if (eqaulIDs(parent.getID(), p.getParentID())) {
                        Throwable error = p.getError();
                        if (error == null && filter.allows(p.getStatusLine())) l.add(p);
                    }
                }
                IHostProcess[] arr = new IHostProcess[l.size()];
                l.toArray(arr);
                sort(arr);
                done(arr);
            }
        }.getS(monitor, "List processes");
    }

    public IHostProcess[] listAllProcesses(String exeNameFilter, String userNameFilter, String stateFilter, IProgressMonitor monitor)
            throws SystemMessageException {
        HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
        rpfs.setName(exeNameFilter);
        rpfs.setUsername(userNameFilter);
        rpfs.setSpecificState(stateFilter);
        return listAllProcesses(rpfs, monitor);
    }

    public IHostProcess[] listChildProcesses(long parentPID, IProgressMonitor monitor) throws SystemMessageException {
        HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
        return listChildProcesses(parentPID, rpfs, monitor);
    }

    public IHostProcess[] listChildProcesses(long parentPID, IHostProcessFilter filter, IProgressMonitor monitor)
            throws SystemMessageException {
        filter.setPpid(Long.toString(parentPID));
        return listAllProcesses(filter, monitor);
    }

    public IHostProcess[] listRootProcesses(IProgressMonitor monitor)
            throws SystemMessageException {
        IHostProcess[] roots = new IHostProcess[1];
        roots[0] = getProcess(1, monitor);
        return roots;
    }
    
    private boolean loadProcesses(Runnable run, final TCFProcessResource parent) {
        if (parent.getChildrenLoading()) {
            parent.addChildrenWaitList(run);
            return false;
        }
        if (parent.getChildrenLoaded()) {
            return true;
        }
        parent.setChildrenLoading(true);
        try {
            final ISysMonitor m = connector.getSysMonitorService();
            m.getChildren(parent.getID(), new ISysMonitor.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] ids) {
                    try {
                        if (error != null) {
                            loadProcessesDone(parent, error, null);
                        }
                        else if (ids == null) {
                            loadProcessesDone(parent, null, new TCFProcessResource[0]);
                        }
                        else {
                            final TCFProcessResource[] arr = new TCFProcessResource[ids.length];
                            final Set<IHostProcess> pending = new HashSet<IHostProcess>();
                            for (int i = 0; i < ids.length; i++) {
                                final TCFProcessResource r = new TCFProcessResource(
                                        TCFProcessService.this, m, id2res.get(ids[i]), ids[i]);
                                if (!r.validate(new Runnable() {
                                    public void run() {
                                        pending.remove(r);
                                        if (pending.isEmpty()) loadProcessesDone(parent, null, arr);
                                    }
                                })) pending.add(r);
                                arr[i] = r;
                            }
                            if (pending.isEmpty()) loadProcessesDone(parent, null, arr);
                        }
                    }
                    catch (Throwable x) {
                        loadProcessesDone(parent, x, null);
                    }
                }
            });
            parent.addChildrenWaitList(run);
            return false;
        }
        catch (Throwable x) {
            loadProcessesDone(parent, x, null);
            return true;
        }
    }
    
    private void loadProcessesDone(TCFProcessResource parent, Throwable error, TCFProcessResource[] arr) {
        assert parent.getChildrenLoading();
        parent.setChildrenLoading(false);
        parent.setChildrenLoaded(true);
        if (arr != null && error == null) {
            for (TCFProcessResource r : arr) {
                long pid = r.getPid();
                if (pid > 0 && parent.getPid() != pid) {
                    pid2res.put(new Long(pid), r);
                    id2res.put(r.getID(), r);
                }
            }
        }
        for (Iterator<TCFProcessResource> i = id2res.values().iterator(); i.hasNext();) {
            TCFProcessResource r = i.next();
            if (pid2res.get(r.getPid()) == null) i.remove();
        }
        parent.setChildrenError(error);
        parent.runChildrenWaitList();
    }
}
