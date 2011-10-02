/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.concurrent.factories;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.Assert;

/**
 * A thread factory implementation creating a single thread only.
 */
public class SingleThreadThreadFactory implements ThreadFactory {
	private final ThreadGroup threadGroup;
	private final String threadName;
	private Thread thread;

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	/**
	 * Constructor.
	 *
	 * @param namePrefix
	 *            The name prefix to name the created threads. Must not be
	 *            <code>null</code>.
	 */
	public SingleThreadThreadFactory(String namePrefix) {
		Assert.isNotNull(namePrefix);

		// Determine the thread group. Use the security manager if available.
		this.threadGroup = (System.getSecurityManager() != null) ? System.getSecurityManager().getThreadGroup() : Thread.currentThread().getThreadGroup();
		// Set the thread name prefix
		this.threadName = ("".equals(namePrefix.trim()) ? "Executor" : namePrefix) + " - " + threadNumber.getAndIncrement(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	public Thread newThread(Runnable r) {
		// The thread can be created on once. If called a second time,
		// this factory cannot create any additional threads.
		if (thread != null) return null;

		// Create the thread with the desired name and the current thread number
		thread = new Thread(threadGroup, r, threadName);
		thread.setDaemon(false);
		thread.setPriority(Thread.NORM_PRIORITY);

		// Return the thread
		return thread;
	}

	/**
	 * Returns the single created thread instance or <code>null</code> if
	 * {@link #newThread(Runnable)} have not been called yet.
	 *
	 * @return The single created thread instance or <code>null</code>.
	 */
	public Thread getThread() {
		return thread;
	}
}
