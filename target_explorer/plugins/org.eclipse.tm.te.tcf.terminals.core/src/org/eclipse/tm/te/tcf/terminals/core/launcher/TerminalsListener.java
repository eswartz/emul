/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.core.launcher;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.tcf.services.ITerminals;
import org.eclipse.tm.tcf.services.ITerminals.TerminalContext;
import org.eclipse.tm.te.runtime.events.EventManager;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tm.te.tcf.terminals.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsContextAwareListener;
import org.eclipse.tm.te.tcf.terminals.core.internal.tracing.ITraceIds;

/**
 * Remote process processes listener implementation.
 */
public class TerminalsListener implements ITerminals.TerminalsListener, ITerminalsContextAwareListener {
	// The parent terminals launcher instance
	private final TerminalsLauncher parent;
	// The remote terminals context
	private ITerminals.TerminalContext context;
	// A flag to remember if exited(...) got called
	private boolean exitedCalled = false;

	/**
	 * Constructor.
	 *
	 * @param parent The parent terminals launcher instance. Must not be <code>null</code>
	 */
	public TerminalsListener(TerminalsLauncher parent) {
		Assert.isNotNull(parent);
		this.parent = parent;
	}

	/**
	 * Returns the parent terminals launcher instance.
	 *
	 * @return The parent terminals launcher instance.
	 */
	protected final TerminalsLauncher getParent() {
		return parent;
	}

	/**
	 * Dispose the terminals listener instance.
	 * <p>
	 * <b>Note:</b> The terminals listener is removed from the terminals service by the parent remote terminals launcher.
	 *
	 * @param callback The callback to invoke if the dispose finished or <code>null</code>.
	 */
	public void dispose(ICallback callback) {
		// If exited(...) hasn't been called yet, but dispose is invoked,
		// send a ... event to signal listeners that a terminated event won't come.
		if (!exitedCalled && context != null) {
			TerminalsStateChangeEvent event = new TerminalsStateChangeEvent(context, TerminalsStateChangeEvent.EVENT_LOST_COMMUNICATION, Boolean.FALSE, Boolean.TRUE, -1);
			EventManager.getInstance().fireEvent(event);
		}
		// Invoke the callback
		if (callback != null) callback.done(this, Status.OK_STATUS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsContextAwareListener#setTerminalsContext(org.eclipse.tm.tcf.services.ITerminals.TerminalContext)
	 */
	@Override
	public void setTerminalsContext(TerminalContext context) {
		Assert.isNotNull(context);
		this.context = context;

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_TERMINALS_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("Terminals context set to: id='" + context.getID() + "', PTY type='" + context.getPtyType() + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                                            0, ITraceIds.TRACE_TERMINALS_LISTENER,
			                                            IStatus.INFO, getClass());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsContextAwareListener#getTerminalsContext()
	 */
	@Override
	public TerminalContext getTerminalsContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.ITerminals.TerminalsListener#exited(java.lang.String, int)
	 */
	@Override
    public void exited(String terminalId, int exitCode) {
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_TERMINALS_LISTENER)) {
			CoreBundleActivator.getTraceHandler().trace("Terminals context terminated: id='" + terminalId + "', exitCode='" + exitCode + "'", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                                            0, ITraceIds.TRACE_TERMINALS_LISTENER,
			                                            IStatus.INFO, getClass());
		}

		// If the exited terminal is the one we are monitoring,
		// --> initiate the disposal of the parent remote terminal launcher
		ITerminals.TerminalContext context = getTerminalsContext();
		if (context != null && terminalId != null && terminalId.equals(context.getID())) {
			// Exited got called for the associated terminal context
			exitedCalled = true;
			// Send a notification
			TerminalsStateChangeEvent event = createRemoteTerminalStateChangeEvent(context, exitCode);
			EventManager.getInstance().fireEvent(event);
			// Dispose the parent remote process launcher
			getParent().dispose();
		}
	}

	/**
	 * Creates a new remote terminal state change event instance.
	 *
	 * @param context The terminal context. Must not be <code>null</code>.
	 * @return The event instance or <code>null</code>.
	 */
	protected TerminalsStateChangeEvent createRemoteTerminalStateChangeEvent(ITerminals.TerminalContext context, int exitCode) {
		Assert.isNotNull(context);
		return new TerminalsStateChangeEvent(context, TerminalsStateChangeEvent.EVENT_TERMINAL_TERMINATED, Boolean.FALSE, Boolean.TRUE, exitCode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.services.ITerminals.TerminalsListener#winSizeChanged(java.lang.String, int, int)
	 */
	@Override
	public void winSizeChanged(String terminal_id, int newWidth, int newHeight) {
		// Pass on to the terminal widget
	}
}
