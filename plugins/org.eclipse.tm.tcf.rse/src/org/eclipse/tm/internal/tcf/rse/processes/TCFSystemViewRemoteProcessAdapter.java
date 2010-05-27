/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.processes;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemResourceSet;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.processes.ui.actions.SystemKillProcessAction;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteTypes;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystem;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.rse.Activator;
import org.eclipse.tm.internal.tcf.rse.Messages;
import org.eclipse.tm.tcf.services.ISysMonitor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;


@SuppressWarnings("restriction")
public class TCFSystemViewRemoteProcessAdapter extends AbstractSystemViewAdapter
        implements ISystemViewElementAdapter, ISystemRemoteElementAdapter{

    private SystemCopyToClipboardAction copyClipboardAction;
    private static final Object[] EMPTY_LIST = new Object[0];
    private static IPropertyDescriptor[] properties = null;
    private SystemKillProcessAction killProcessAction;

    private static final NumberFormat percent_format;

    static {
        percent_format = NumberFormat.getPercentInstance();
        percent_format.setMaximumFractionDigits(3);
    }

    @Override
    public boolean canDrag(Object element) {
        return true;
    }

    @Override
    public boolean canDrag(SystemRemoteResourceSet elements) {
        return true;
    }

    @Override
    public Object doDrag(Object element, boolean sameSystemType, IProgressMonitor monitor) {
        return getText(element);
    }

    @Override
    public ISystemResourceSet doDrag(SystemRemoteResourceSet set, IProgressMonitor monitor) {
        return set;
    }

    @Override
    public void addActions(SystemMenuManager menu,
                    IStructuredSelection selection, Shell parent, String menuGroup) {
        if (killProcessAction == null) {
            killProcessAction = new SystemKillProcessAction(getShell());
        }
        menu.add(ISystemContextMenuConstants.GROUP_CHANGE, killProcessAction);
        if (copyClipboardAction == null) {
            Clipboard clipboard = RSEUIPlugin.getTheSystemRegistryUI().getSystemClipboard();
            copyClipboardAction = new SystemCopyToClipboardAction(getShell(), clipboard);
        }
        menu.add(menuGroup, copyClipboardAction);
    }

    @Override
    public ISubSystem getSubSystem(Object element) {
        if (element instanceof IRemoteProcess) {
            IRemoteProcess process = (IRemoteProcess)element;
            return process.getParentRemoteProcessSubSystem();
        }
        return super.getSubSystem(element);
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object element) {
        IRemoteProcess process = (IRemoteProcess)element;
        TCFProcessResource r = (TCFProcessResource)process.getObject();
        String state = r.getState();
        if (r.getParentID() != null) {
            if (state == null || state.indexOf('R') >= 0) {
                return Activator.getDefault().getImageDescriptorFromPath("icons/thread-r.gif"); //$NON-NLS-1$
            }
            return Activator.getDefault().getImageDescriptorFromPath("icons/thread-s.gif"); //$NON-NLS-1$
        }
        else {
            if (state == null || state.indexOf('R') >= 0) {
                return Activator.getDefault().getImageDescriptorFromPath("icons/process-r.gif"); //$NON-NLS-1$
            }
            return Activator.getDefault().getImageDescriptorFromPath("icons/process-s.gif"); //$NON-NLS-1$
        }
    }

    public String getText(Object element) {
        String text = ((IRemoteProcess)element).getLabel();
        return (text == null) ? "" : text; //$NON-NLS-1$
    }

    @Override
    public String getAlternateText(Object element) {
        IRemoteProcess process = (IRemoteProcess)element;
        String allProperties = process.getAllProperties();
        return allProperties.replace('|', '\t');
    }

    public String getAbsoluteName(Object object) {
        IRemoteProcess process = (IRemoteProcess) object;
        return "" + process.getPid(); //$NON-NLS-1$
    }

    @Override
    public String getType(Object element) {
        return "Process"; //$NON-NLS-1$
    }

    @Override
    public Object getParent(Object element) {
        IRemoteProcess process = (IRemoteProcess) element;
        IRemoteProcess parent = process.getParentRemoteProcess();
        if ((parent != null) && parent.getAbsolutePath().equals(process.getAbsolutePath()))
            // should never happen but sometimes it does, leading to infinite loop.
            parent = null;
        return parent;
    }

    @Override
    public boolean hasChildren(IAdaptable element) {
        return getChildren(element, new NullProgressMonitor()).length > 0;
    }

    @Override
    public Object[] getChildren(IAdaptable element, IProgressMonitor monitor) {
        IRemoteProcess process = (IRemoteProcess)element;
        IRemoteProcessSubSystem ss = process.getParentRemoteProcessSubSystem();
        IHostProcessFilter orgRpfs = process.getFilterString();
        IHostProcessFilter newRpfs = new HostProcessFilterImpl(orgRpfs.toString());

        Object[] children1 = null;
        Object[] children2 = null;
        newRpfs.setPpid(Long.toString(process.getPid()));

        try {
            TCFProcessResource r = (TCFProcessResource)process.getObject();
            IHostProcess[] nodes = r.getService().listAllProcesses(orgRpfs, r, monitor);
            TCFProcessAdapter adapter = new TCFProcessAdapter();
            children1 = adapter.convertToRemoteProcesses(process.getContext(), process, nodes);
            if (children1 == null) children1 = EMPTY_LIST;

            children2 = ss.listAllProcesses(newRpfs, process.getContext(), monitor);
            if (children2 == null) children2 = EMPTY_LIST;
        }
        catch (Exception exc) {
            children1 = new SystemMessageObject[1];
            children1[0] = new SystemMessageObject(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXPAND_FAILED), ISystemMessageObject.MSGTYPE_ERROR, element);
            children2 = null;
            SystemBasePlugin.logError("Exception resolving file filter strings", exc); //$NON-NLS-1$
        }
        if (children1 == null || children1.length == 0) return children2;
        if (children2 == null || children2.length == 0) return children1;
        Object[] children = new Object[children1.length + children2.length];
        System.arraycopy(children1, 0, children, 0, children1.length);
        System.arraycopy(children2, 0, children, children1.length, children2.length);
        return children;
    }

    @Override
    protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
        if (properties != null) return properties;
        List<IPropertyDescriptor> l = new ArrayList<IPropertyDescriptor>();

        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_ID, Messages.PROCESS_ID_LABEL, Messages.PROCESS_ID_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_PID, Messages.PROCESS_PID_LABEL, Messages.PROCESS_PID_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_FILE, Messages.PROCESS_NAME_LABEL, Messages.PROCESS_NAME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CWD, Messages.PROCESS_CWD_LABEL, Messages.PROCESS_CWD_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_ROOT, Messages.PROCESS_ROOT_LABEL, Messages.PROCESS_ROOT_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_STATE, Messages.PROCESS_STATE_LABEL, Messages.PROCESS_STATE_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_UID, Messages.PROCESS_UID_LABEL, Messages.PROCESS_UID_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_USERNAME, Messages.PROCESS_USERNAME_LABEL, Messages.PROCESS_USERNAME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_UGID, Messages.PROCESS_GID_LABEL, Messages.PROCESS_GID_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_GROUPNAME, Messages.PROCESS_GROUPNAME_LABEL, Messages.PROCESS_GROUPNAME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_PPID, Messages.PROCESS_PPID_LABEL, Messages.PROCESS_PPID_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_PGRP, Messages.PROCESS_PGRP_LABEL, Messages.PROCESS_PGRP_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_TGID, Messages.PROCESS_TGID_LABEL, Messages.PROCESS_TGID_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_TRACERPID, Messages.PROCESS_TRACERPID_LABEL, Messages.PROCESS_TRACERPID_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_VSIZE, Messages.PROCESS_VMSIZE_LABEL, Messages.PROCESS_VMSIZE_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_RSS, Messages.PROCESS_VMRSS_LABEL, Messages.PROCESS_VMRSS_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_SESSION, Messages.PROCESS_SESSION_LABEL, Messages.PROCESS_SESSION_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_TTY, Messages.PROCESS_TTY_LABEL, Messages.PROCESS_TTY_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_FLAGS, Messages.PROCESS_FLAGS_LABEL, Messages.PROCESS_FLAGS_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_MINFLT, Messages.PROCESS_MINFLT_LABEL, Messages.PROCESS_MINFLT_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CMINFLT, Messages.PROCESS_CMINFLT_LABEL, Messages.PROCESS_CMINFLT_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_MAJFLT, Messages.PROCESS_MAJFLT_LABEL, Messages.PROCESS_MAJFLT_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CMAJFLT, Messages.PROCESS_CMAJFLT_LABEL, Messages.PROCESS_CMAJFLT_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_UTIME, Messages.PROCESS_UTIME_LABEL, Messages.PROCESS_UTIME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_STIME, Messages.PROCESS_STIME_LABEL, Messages.PROCESS_STIME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CUTIME, Messages.PROCESS_CUTIME_LABEL, Messages.PROCESS_CUTIME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CSTIME, Messages.PROCESS_CSTIME_LABEL, Messages.PROCESS_CSTIME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(TCFProcessResource.PROP_PC_UTIME, Messages.PROCESS_PC_UTIME_LABEL, Messages.PROCESS_PC_UTIME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(TCFProcessResource.PROP_PC_STIME, Messages.PROCESS_PC_STIME_LABEL, Messages.PROCESS_PC_STIME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_PRIORITY, Messages.PROCESS_PRIORITY_LABEL, Messages.PROCESS_PRIORITY_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_NICE, Messages.PROCESS_NICE_LABEL, Messages.PROCESS_NICE_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_ITREALVALUE, Messages.PROCESS_ITREALVALUE_LABEL, Messages.PROCESS_ITREALVALUE_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_STARTTIME, Messages.PROCESS_STARTTIME_LABEL, Messages.PROCESS_STARTTIME_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_RLIMIT, Messages.PROCESS_RLIMIT_LABEL, Messages.PROCESS_RLIMIT_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CODESTART, Messages.PROCESS_CODESTART_LABEL, Messages.PROCESS_CODESTART_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CODEEND, Messages.PROCESS_CODEEND_LABEL, Messages.PROCESS_CODEEND_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_STACKSTART, Messages.PROCESS_STACKSTART_LABEL, Messages.PROCESS_STACKSTART_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_SIGNALS, Messages.PROCESS_SIGNALS_LABEL, Messages.PROCESS_SIGNALS_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_SIGBLOCK, Messages.PROCESS_SIGBLOCK_LABEL, Messages.PROCESS_SIGBLOCK_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_SIGIGNORE, Messages.PROCESS_SIGIGNORE_LABEL, Messages.PROCESS_SIGIGNORE_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_SIGCATCH, Messages.PROCESS_SIGCATCH_LABEL, Messages.PROCESS_SIGCATCH_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_WCHAN, Messages.PROCESS_WCHAN_LABEL, Messages.PROCESS_WCHAN_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_NSWAP, Messages.PROCESS_NSWAP_LABEL, Messages.PROCESS_NSWAP_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_CNSWAP, Messages.PROCESS_CNSWAP_LABEL, Messages.PROCESS_CNSWAP_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_EXITSIGNAL, Messages.PROCESS_EXITSIGNAL_LABEL, Messages.PROCESS_EXITSIGNAL_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_PROCESSOR, Messages.PROCESS_PROCESSOR_LABEL, Messages.PROCESS_PROCESSOR_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_RTPRIORITY, Messages.PROCESS_RTPRIORITY_LABEL, Messages.PROCESS_RTPRIORITY_TOOLTIP));
        l.add(createSimplePropertyDescriptor(ISysMonitor.PROP_POLICY, Messages.PROCESS_POLICY_LABEL, Messages.PROCESS_POLICY_TOOLTIP));

        properties = l.toArray(new IPropertyDescriptor[l.size()]);
        return properties;
    }

    /**
     * Returns the current value for the named property.
     * @return the current value of the given property
     */
    @Override
    protected Object internalGetPropertyValue(Object property) {
        Object v = internalGetPropertyValueOrNull(property);
        if (v == null) v = "";
        return v;
    }

    private Object internalGetPropertyValueOrNull(Object property) {
        TCFRemoteProcess process = (TCFRemoteProcess)propertySourceInput;
        Object p = process.getProperties().get(property);
        if (property.equals(ISysMonitor.PROP_VSIZE)) {
            return NLS.bind(Messages.PROCESS_VMSIZE_VALUE, Long.toString(process.getVmSizeInKB()));
        }
        if (property.equals(ISysMonitor.PROP_RSS)) {
            return NLS.bind(Messages.PROCESS_VMRSS_VALUE, Long.toString(process.getVmRSSInKB()));
        }
        if (property.equals(ISysMonitor.PROP_SIGNALS)) return formatBitSet(p);
        if (property.equals(ISysMonitor.PROP_SIGBLOCK)) return formatBitSet(p);
        if (property.equals(ISysMonitor.PROP_SIGCATCH)) return formatBitSet(p);
        if (property.equals(ISysMonitor.PROP_SIGIGNORE)) return formatBitSet(p);
        if (property.equals(ISysMonitor.PROP_CODESTART)) return formatHex(p);
        if (property.equals(ISysMonitor.PROP_CODEEND)) return formatHex(p);
        if (property.equals(ISysMonitor.PROP_STACKSTART)) return formatHex(p);
        if (property.equals(ISysMonitor.PROP_WCHAN)) return formatHex(p);
        if (property.equals(ISysMonitor.PROP_FLAGS)) return formatBitSet(p);
        if (property.equals(ISysMonitor.PROP_UTIME)) return formatTime(p);
        if (property.equals(ISysMonitor.PROP_STIME)) return formatTime(p);
        if (property.equals(ISysMonitor.PROP_CUTIME)) return formatTime(p);
        if (property.equals(ISysMonitor.PROP_CSTIME)) return formatTime(p);
        if (property.equals(ISysMonitor.PROP_STARTTIME)) return formatTime(p);
        if (property.equals(ISysMonitor.PROP_ITREALVALUE)) return formatTime(p);
        if (property.equals(TCFProcessResource.PROP_PC_UTIME)) return formatPercent(p);
        if (property.equals(TCFProcessResource.PROP_PC_STIME)) return formatPercent(p);
        if (p != null) return p.toString();
        return null;
    }

    /**
     * Returns the current value for the named property.
     *
     * @param property the name or key of the property as named by its property descriptor
     * @param formatted indication of whether to return the value in formatted or raw form
     * @return the current value of the given property
     */
    @Override
    public Object getPropertyValue(Object property, boolean formatted) {
        if (formatted) return getPropertyValue(property);

        TCFRemoteProcess process = (TCFRemoteProcess)propertySourceInput;
        Object p = process.getProperties().get(property);
        if (p == null) {
            if (property.equals(ISysMonitor.PROP_PID)) return Long.valueOf(0);
            if (property.equals(ISysMonitor.PROP_PPID)) return Long.valueOf(0);
            if (property.equals(ISysMonitor.PROP_CODESTART)) return BigInteger.ZERO;
            if (property.equals(ISysMonitor.PROP_CODEEND)) return BigInteger.ZERO;
            if (property.equals(ISysMonitor.PROP_STACKSTART)) return BigInteger.ZERO;
            if (property.equals(ISysMonitor.PROP_WCHAN)) return BigInteger.ZERO;
            if (property.equals(ISysMonitor.PROP_UTIME)) return Long.valueOf(0);
            if (property.equals(ISysMonitor.PROP_STIME)) return Long.valueOf(0);
            if (property.equals(ISysMonitor.PROP_CUTIME)) return Long.valueOf(0);
            if (property.equals(ISysMonitor.PROP_CSTIME)) return Long.valueOf(0);
            if (property.equals(ISysMonitor.PROP_STARTTIME)) return Long.valueOf(0);
            if (property.equals(ISysMonitor.PROP_ITREALVALUE)) return Long.valueOf(0);
            if (property.equals(TCFProcessResource.PROP_PC_UTIME)) return Double.valueOf(0);
            if (property.equals(TCFProcessResource.PROP_PC_STIME)) return Double.valueOf(0);
            if (property.equals(ISysMonitor.PROP_CWD)) return "";
            if (property.equals(ISysMonitor.PROP_FILE)) return "";
        }
        return p;
    }

    private String formatPercent(Object o) {
        if (o instanceof Number) {
            Number n = (Number)o;
            return percent_format.format(n.doubleValue());
        }
        return null;
    }

    private String formatTime(Object o) {
        if (o instanceof Number) {
            BigInteger n = new BigInteger(o.toString());
            BigInteger s[] = n.divideAndRemainder(BigInteger.valueOf(1000));
            BigInteger m[] = s[0].divideAndRemainder(BigInteger.valueOf(60));
            BigInteger h[] = m[0].divideAndRemainder(BigInteger.valueOf(60));
            StringBuffer buf = new StringBuffer();
            if (!h[0].equals(BigInteger.ZERO)) {
                buf.append(h[0]);
                buf.append("h "); //$NON-NLS-1$
            }
            if (buf.length() > 0 || !h[1].equals(BigInteger.ZERO)) {
                buf.append(h[1]);
                buf.append("m "); //$NON-NLS-1$
            }
            buf.append(m[1]);
            buf.append('.');
            String ms = s[1].toString();
            buf.append("000".substring(ms.length())); //$NON-NLS-1$
            buf.append(ms);
            buf.append('s');
            return buf.toString();
        }
        if (o == null) return null;
        return o.toString();
    }

    private void formatHex(StringBuffer buf, BigInteger n, int cnt) {
        BigInteger m[] = n.divideAndRemainder(BigInteger.valueOf(16));
        if (cnt < 7 || !m[0].equals(BigInteger.ZERO)) {
            formatHex(buf, m[0], cnt + 1);
        }
        int d = m[1].intValue();
        buf.append((char)(d <= 9 ? '0' + d : 'a' + d - 10));
    }

    protected String formatHex(Object o) {
        if (o instanceof Number) {
            BigInteger n = o instanceof BigInteger ?
                    (BigInteger)o : new BigInteger(o.toString());
            StringBuffer buf = new StringBuffer();
            buf.append("0x"); //$NON-NLS-1$
            formatHex(buf, n, 0);
            return buf.toString();
        }
        if (o == null) return null;
        return o.toString();
    }

    protected String formatBitSet(Object o) {
        if (o instanceof Number) {
            StringBuffer buf = new StringBuffer();
            long n = ((Number)o).longValue();
            for (int i = 0; i < 64; i++) {
                if ((n & (1l << i)) != 0) {
                    if (buf.length() > 0) buf.append(',');
                    int i0 = i;
                    while (i < 63 && (n & (1l << (i + 1))) != 0) i++;
                    buf.append(i0);
                    if (i0 != i) {
                        buf.append(".."); //$NON-NLS-1$
                        buf.append(i);
                    }
                }
            }
            return buf.toString();
        }
        if (o == null) return null;
        return o.toString();
    }

    protected String formatState(String state) {
        return state;
    }

    /**
     * Return fully qualified name that uniquely identifies this remote object's remote parent within its subsystem
     */
    public String getAbsoluteParentName(Object element) {
        IRemoteProcess process = (IRemoteProcess) element;
        IRemoteProcess parent = process.getParentRemoteProcess();
        if (parent != null) return parent.getAbsolutePath();
        else return "/proc/0"; //$NON-NLS-1$
    }

    /**
     * Given a remote object, returns it remote parent object. Eg, given a process, return the process that
     * spawned it.
     * <p>
     * The shell is required in order to set the cursor to a busy state if a remote trip is required.
     *
     * @return an IRemoteProcess object for the parent
     */
    public Object getRemoteParent(Object element, IProgressMonitor monitor) throws Exception {
        return ((IRemoteProcess) element).getParentRemoteProcess();
    }

    /**
     * Given a remote object, return the unqualified names of the objects contained in that parent. This is
     *  used for testing for uniqueness on a rename operation, for example. Sometimes, it is not
     *  enough to just enumerate all the objects in the parent for this purpose, because duplicate
     *  names are allowed if the types are different, such as on iSeries. In this case return only
     *  the names which should be used to do name-uniqueness validation on a rename operation.
     *
     * @return an array of all file and folder names in the parent of the given IRemoteFile object
     */
    public String[] getRemoteParentNamesInUse(Object element, IProgressMonitor monitor) throws Exception {
        String[] pids = EMPTY_STRING_LIST;

        IRemoteProcess process = (IRemoteProcess) element;
        String parentName = "" + process.getPPid(); //$NON-NLS-1$
        if (parentName.equals("-1")) // given a root? //$NON-NLS-1$
            return pids; // not much we can do. Should never happen: you can't rename a root!

        Object[] children = getChildren(process.getParentRemoteProcess(), monitor);
        if ((children == null) || (children.length == 0))
            return pids;

        pids = new String[children.length];
        for (int idx = 0; idx < pids.length; idx++)
            pids[idx] = "" + ((IRemoteProcess) children[idx]).getPid(); //$NON-NLS-1$

        return pids;
    }

    public String getRemoteSubType(Object element) {
        return null;
    }

    public String getRemoteType(Object element) {
        IRemoteProcess process = (IRemoteProcess) element;
        if (process.isRoot())
            return ISystemProcessRemoteTypes.TYPE_ROOT;
        else
            return ISystemProcessRemoteTypes.TYPE_PROCESS;
    }

    public String getRemoteTypeCategory(Object element) {
        return ISystemProcessRemoteTypes.TYPECATEGORY;
    }

    /**
     * Return the subsystem factory id that owns this remote object
     * The value must not be translated, so that property pages registered via xml can subset by it.
     */
    public String getSubSystemConfigurationId(Object element) {
        IRemoteProcess process = (IRemoteProcess) element;
        return process.getParentRemoteProcessSubSystem().getSubSystemConfiguration().getId();
    }

    public boolean refreshRemoteObject(Object oldElement, Object newElement) {
        return false;
    }

    public boolean supportsUserDefinedActions(Object object) {
        return false;
    }
}
