/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Martin Oberhuber (Wind River) - [238564] Adopt TM 3.0 APIs
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.processes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.services.processes.AbstractProcessService;
import org.eclipse.rse.services.processes.IProcessService;
import org.eclipse.tm.internal.tcf.rse.ITCFSubSystem;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorService;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorServiceManager;
import org.eclipse.tm.internal.tcf.rse.TCFRSETask;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IProcesses;


public class TCFProcessService extends AbstractProcessService implements IProcessService {

    private final TCFConnectorService connector;
    private final TCFProcessResource root;
    private final Map<Long,TCFProcessResource> pid2res = new HashMap<Long,TCFProcessResource>();
    private final Map<String,Long> signals = new HashMap<String,Long>();
    private final List<Runnable> get_signals_wait_list = new ArrayList<Runnable>();

    public TCFProcessService(IHost host) {
        connector = (TCFConnectorService)TCFConnectorServiceManager
            .getInstance().getConnectorService(host, ITCFSubSystem.class);
        root = new TCFProcessResource(this, null, null, null);
    }

    public TCFConnectorService getTCFConnectorService() {
        return connector;
    }

    @Override
    public String getDescription() {
        return "The TCF Process Service uses the Target Communication Framework to provide service for the Processes subsystem." + //$NON-NLS-1$
            " It requires a TCF agent to be running on the remote machine."; //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return "TCF Process Service"; //$NON-NLS-1$
    }

    @Override
    public IHostProcess getParentProcess(long PID, IProgressMonitor monitor) throws SystemMessageException {
        return getProcess(getProcess(PID, monitor).getPPid(), monitor);
    }

    @Override
    public IHostProcess getProcess(final long PID, IProgressMonitor monitor) throws SystemMessageException {
        return new TCFRSETask<IHostProcess>() {
            public void run() {
                if (!root.loadChildren(this)) return;
                if (root.getChildrenError() != null) {
                    error(root.getChildrenError());
                    return;
                }
                done(pid2res.get(PID));
            }
        }.getS(monitor, "Get process properties"); //$NON-NLS-1$
    }

    public String[] getSignalTypes() {
        return new TCFRSETask<String[]>() {
            public void run() {
                if (signals.isEmpty()) {
                    if (get_signals_wait_list.contains(this)) {
                        done(signals.keySet().toArray(new String[signals.size()]));
                    }
                    else {
                        if (get_signals_wait_list.isEmpty()) {
                            connector.getService(IProcesses.class).getSignalList(null, new IProcesses.DoneGetSignalList() {
                                public void doneGetSignalList(IToken token, Exception error, Collection<Map<String,Object>> list) {
                                    if (list != null) {
                                        for (Map<String,Object> m : list) {
                                            String name = (String)m.get(IProcesses.SIG_NAME);
                                            Number code = (Number)m.get(IProcesses.SIG_CODE);
                                            if (name != null && code != null) signals.put(name, code.longValue());
                                        }
                                    }
                                    for (Runnable r : get_signals_wait_list) r.run();
                                    get_signals_wait_list.clear();
                                }
                            });
                        }
                        get_signals_wait_list.add(this);
                    }
                }
                else {
                    done(signals.keySet().toArray(new String[signals.size()]));
                }
            }
        }.getE();
    }

    public boolean kill(final long PID, final String signal, IProgressMonitor monitor) throws SystemMessageException {
        return new TCFRSETask<Boolean>() {
            public void run() {
                if (!root.loadChildren(this)) return;
                if (root.getChildrenError() != null) {
                    error(root.getChildrenError());
                    return;
                }
                TCFProcessResource prs = pid2res.get(PID);
                if (prs == null) {
                    done(false);
                    return;
                }
                Long signo = signals.get(signal);
                if (signal.equals(ISystemProcessRemoteConstants.PROCESS_SIGNAL_TYPE_DEFAULT)) {
                    if (signo == null) signo = signals.get("SIGTERM");
                    if (signo == null) signo = signals.get("SIGKILL");
                }
                if (signo == null) {
                    error(new Exception("Unknown signal: " + signal));
                    return;
                }
                connector.getService(IProcesses.class).signal(prs.getID(), signo.longValue(), new IProcesses.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) {
                            error(error);
                        }
                        else {
                            done(true);
                        }
                    }
                });
            }
        }.getS(monitor, "Sending signal to a process"); //$NON-NLS-1$
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

    @Override
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
        new TCFRSETask<Boolean>() {
            public void run() {
                TCFProcessResource parent = (TCFProcessResource)up;
                if (parent != null) {
                    if (filter.getPpid() == null || filter.getPpid().equals("*") || parent.getChildrenError() != null) { //$NON-NLS-1$
                        parent.flushChildrenCache();
                    }
                }
                done(true);
            }
        }.getE();
        return new TCFRSETask<IHostProcess[]>() {
            public void run() {
                TCFProcessResource parent = (TCFProcessResource)up;
                if (parent == null) {
                    error(new IOException("Invalid parent")); //$NON-NLS-1$
                    return;
                }
                if (!parent.loadChildren(this)) return;
                if (parent.getChildrenError() != null) {
                    error(parent.getChildrenError());
                    return;
                }
                List<IHostProcess> l = new ArrayList<IHostProcess>();
                for (TCFProcessResource p : pid2res.values()) {
                    if (eqaulIDs(parent.getID(), p.getParentID())) {
                        if (p.getError() == null && filter.allows(p.getStatusLine())) l.add(p);
                    }
                }
                IHostProcess[] arr = new IHostProcess[l.size()];
                l.toArray(arr);
                sort(arr);
                done(arr);
            }
        }.getS(monitor, "List processes"); //$NON-NLS-1$
    }

    @Override
    public IHostProcess[] listAllProcesses(String exeNameFilter, String userNameFilter, String stateFilter, IProgressMonitor monitor)
            throws SystemMessageException {
        HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
        rpfs.setName(exeNameFilter);
        rpfs.setUsername(userNameFilter);
        rpfs.setSpecificState(stateFilter);
        return listAllProcesses(rpfs, monitor);
    }

    @Override
    public IHostProcess[] listChildProcesses(long parentPID, IProgressMonitor monitor) throws SystemMessageException {
        HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
        return listChildProcesses(parentPID, rpfs, monitor);
    }

    @Override
    public IHostProcess[] listChildProcesses(long parentPID, IHostProcessFilter filter, IProgressMonitor monitor)
            throws SystemMessageException {
        filter.setPpid(Long.toString(parentPID));
        return listAllProcesses(filter, monitor);
    }

    @Override
    public IHostProcess[] listRootProcesses(IProgressMonitor monitor) throws SystemMessageException {
        IHostProcess[] roots = new IHostProcess[1];
        roots[0] = getProcess(1, monitor);
        return roots;
    }

    Map<Long,TCFProcessResource> getProcessCache() {
        return pid2res;
    }
}
