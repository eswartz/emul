/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.IScanner;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;


/**
 * Locator model scanner implementation.
 */
public class Scanner extends Job implements IScanner {
	// Reference to the parent model instance.
	private final ILocatorModel parentModel;

	// Reference to the scanner configuration
	private final Map<String, Object> configuration = new HashMap<String, Object>();

	// Flag to mark if the scanner is terminated
	private AtomicBoolean terminated = new AtomicBoolean(false);

	/**
	 * Constructor.
	 *
	 * @param parentModel The parent model instance. Must not be <code>null</code>.
	 */
	public Scanner(ILocatorModel parentModel) {
		super(Scanner.class.getName());
		Assert.isNotNull(parentModel);
		this.parentModel = parentModel;
	}

	/**
	 * Returns the parent model instance.
	 *
	 * @return The parent model instance.
	 */
	protected ILocatorModel getParentModel() {
		return parentModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.IScanner#setConfiguration(java.util.Map)
	 */
	public void setConfiguration(Map<String, Object> configuration) {
		Assert.isNotNull(configuration);
		this.configuration.clear();
		this.configuration.putAll(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.IScanner#getConfiguration()
	 */
	public Map<String, Object> getConfiguration() {
		return configuration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor == null) monitor = new NullProgressMonitor();

		// Get the current list of peers known to the parent model
		IPeerModel[] peers = getParentModel().getPeers();
		// Do we have something to scan at all
		if (peers.length > 0) {
			// The first runnable is setting the thread which will finish
			// the job at the end
			Protocol.invokeLater(new Runnable() {
				public void run() {
					Scanner.this.setThread(Thread.currentThread());
				}
			});
			// Loop the nodes and try to get an channel
			for (IPeerModel peer : peers) {
				// Check for the progress monitor getting canceled
				if (monitor.isCanceled() || isTerminated()) break;
				// Create the scanner runnable
				Runnable runnable = new ScannerRunnable(this, peer);
				// Submit for execution
				Protocol.invokeLater(runnable);
			}
			// The last runnable will terminate the job as soon all
			// scanner runnable's are processed and will reschedule the job
			final IStatus result = monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
			Protocol.invokeLater(new Runnable() {
				public void run() {
					Scanner.this.done(result);

					Long delay = (Long)getConfiguration().get(IScanner.PROP_SCHEDULE);
					if (delay != null) {
						Scanner.this.schedule(delay.longValue());
					}
				}
			});
		}

		return peers.length > 0 ? ASYNC_FINISH : Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.IScanner#terminate()
	 */
	public void terminate() {
		terminated.set(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.IScanner#isTerminated()
	 */
	public final boolean isTerminated() {
		return terminated.get();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
	 */
	@Override
	public boolean shouldRun() {
		return Platform.isRunning() && !getParentModel().isDisposed() && !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#shouldSchedule()
	 */
	@Override
	public boolean shouldSchedule() {
		return Platform.isRunning() && !getParentModel().isDisposed() && !isTerminated();
	}
}
