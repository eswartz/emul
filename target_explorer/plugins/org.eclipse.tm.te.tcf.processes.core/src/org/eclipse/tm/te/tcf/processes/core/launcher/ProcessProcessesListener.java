/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.core.launcher;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.ProcessesListener;
import org.eclipse.tm.te.runtime.events.EventManager;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.tcf.processes.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessContextAwareListener;
import org.eclipse.tm.te.tcf.processes.core.internal.tracing.ITraceIds;

/**
 * Remote process processes listener implementation.
 */
public class ProcessProcessesListener implements ProcessesListener, IProcessContextAwareListener {
	// The parent process launcher instance
	private final ProcessLauncher parent;
	// The remote process context
	private IProcesses.ProcessContext context;
	// A flag to remember if exited(...) got called
	private boolean exitedCalled = false;

	/**
	 * Constructor.
	 *
	 * @param parent The parent process launcher instance. Must not be <code>null</code>
	 */
	public ProcessProcessesListener(ProcessLauncher parent) {
		Assert.isNotNull(parent);
		this.parent = parent;
	}

	/**
	 * Returns the parent process launcher instance.
	 *
	 * @return The parent process launcher instance.
	 */
	protected final ProcessLauncher getParent() {
		return parent;
	}

	/**
	 * Dispose the processes listener instance.
	 * <p>
	 * <b>Note:</b> The processes listener is removed from the processes service by the parent remote process launcher.
	 *
	 * @param callback The callback to invoke if the dispose finished or <code>null</code>.
	 */
	public void dispose(ICallback callback) {
		// If exited(...) hasn't been called yet, but dispose is invoked,
		// send a ... event to signal listeners that a terminated event won't come.
		if (!exitedCalled && context != null) {
			ProcessStateChangeEvent event = new ProcessStateChangeEvent(context, ProcessStateChangeEvent.EVENT_LOST_COMMUNICATION, Boolean.FALSE, Boolean.TRUE, -1);
			EventManager.getInstance().fireEvent(event);
		}
		// Invoke the callback
		if (callback != null) callback.done(this, Status.OK_STATUS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessContextAwareListener#setProcessContext(org.eclipse.tm.tcf.services.IProcesses.ProcessContext)
	 */
	@Override
    public void setProcessContext(IProcesses.ProcessContext context) {
		Assert.isNotNull(context);
		this.context = context;

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_PROCESSES_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("Process context set to: id='" + context.getID() + "', name='" + context.getName() + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                                            0, ITraceIds.TRACE_PROCESSES_LISTENER,
			                                            IStatus.INFO, getClass());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.processes.core.interfaces.launcher.IProcessContextAwareListener#getProcessContext()
	 */
	@Override
    public IProcesses.ProcessContext getProcessContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.IProcesses.ProcessesListener#exited(java.lang.String, int)
	 */
	@Override
    public void exited(String processId, int exitCode) {
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_PROCESSES_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("Process context terminated: id='" + processId + "', exitCode='" + exitCode + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                                            0, ITraceIds.TRACE_PROCESSES_LISTENER,
			                                            IStatus.INFO, getClass());
		}

		// If the exited process is the one we are monitoring,
		// --> initiate the disposal of the parent remote process launcher
		IProcesses.ProcessContext context = getProcessContext();
		if (context != null && processId != null && processId.equals(context.getID())) {
			// Exited got called for the associated process context
			exitedCalled = true;
			// Send a notification
			ProcessStateChangeEvent event = createRemoteProcessStateChangeEvent(context, exitCode);
			EventManager.getInstance().fireEvent(event);
			// Dispose the parent remote process launcher
			getParent().dispose();
		}
	}

	/**
	 * Creates a new remote process state change event instance.
	 *
	 * @param context The process context. Must not be <code>null</code>.
	 * @return The event instance or <code>null</code>.
	 */
	protected ProcessStateChangeEvent createRemoteProcessStateChangeEvent(IProcesses.ProcessContext context, int exitCode) {
		Assert.isNotNull(context);
		return new ProcessStateChangeEvent(context, ProcessStateChangeEvent.EVENT_PROCESS_TERMINATED, Boolean.FALSE, Boolean.TRUE, exitCode);
	}

}
