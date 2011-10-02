/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.concurrent.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.tm.te.core.concurrent.factories.SingleThreadThreadFactory;
import org.eclipse.tm.te.core.concurrent.interfaces.INestableExecutor;
import org.eclipse.tm.te.core.concurrent.interfaces.ISingleThreadedExecutor;

/**
 * A single threaded executor service implementation.
 */
public class SingleThreadedExecutorService extends AbstractDelegatingExecutorService implements ISingleThreadedExecutor, INestableExecutor {

	/**
	 * A single threaded executor implementation.
	 */
	protected class SingleThreadedExecutor extends ThreadPoolExecutor implements INestableExecutor {
		// The current nesting depth
		private final AtomicInteger currentNestingDepth = new AtomicInteger(0);

		/**
		 * Constructor.
		 *
		 * @param threadFactory
		 *            The thread factory instance. Must not be <code>null</code>.
		 *
		 * @throws NullPointerException
		 *             if threadFactory is <code>null</code>.
		 */
		public SingleThreadedExecutor(ThreadFactory threadFactory) {
			this(threadFactory, new LinkedBlockingQueue<Runnable>());
		}

		/**
		 * Constructor.
		 * <p>
		 * Private constructor to catch the work queue instance passed into the
		 * {@link ThreadPoolExecutor} constructor.
		 *
		 * @param threadFactory
		 *            The thread factory instance. Must not be <code>null</code>.
		 * @param workQueue
		 *            The work queue instance. Must not be <code>null</code>.
		 */
		private SingleThreadedExecutor(ThreadFactory threadFactory, BlockingQueue<Runnable> workQueue) {
			super(1, 1, 0L, TimeUnit.NANOSECONDS, workQueue, threadFactory);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.core.concurrent.interfaces.INestableExecutor#getMaxDepth()
		 */
		public int getMaxDepth() {
			return 1;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.core.concurrent.interfaces.INestableExecutor#readAndExecute()
		 */
		public boolean readAndExecute() {
			// Method is callable from the executor thread only
			if (!isExecutorThread()) {
				throw new IllegalStateException("Must be called from within the executor thread!"); //$NON-NLS-1$
			}

			BlockingQueue<Runnable> queue = getQueue();

			// If the work queue is empty, there is nothing to do
			if (!queue.isEmpty()) {
				// Work queue not empty, check if we reached the maximum nesting
				// depth
				if (currentNestingDepth.get() >= getMaxDepth()) {
					throw new IllegalStateException("Maximum nesting depth exceeded!"); //$NON-NLS-1$
				}

				// Get the next work item to do
				Runnable runnable = null;
				try {
					// Double check that the queue is not empty, we desire to
					// avoid
					// blocking here!
					if (!queue.isEmpty()) {
						runnable = queue.take();
					}
				} catch (InterruptedException e) { /* ignored on purpose */ }

				if (runnable != null) {
					// Increase the nesting depth
					currentNestingDepth.incrementAndGet();
					try {
						// Execute the runnable
						runnable.run();
					} finally {
						// Decrease nesting depth
						currentNestingDepth.decrementAndGet();
					}
				}
			}

			return !queue.isEmpty();
		}

		/* (non-Javadoc)
		 * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
		 */
		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			if (t != null)
				logException(t);
		}
	}

	// Internal reference to the one shot thread factory instance
	private SingleThreadThreadFactory threadFactory;

	/**
	 * Constructor.
	 */
	public SingleThreadedExecutorService() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.concurrent.executors.AbstractDelegatingExecutorService#createExecutorServiceDelegate()
	 */
	@Override
	protected ExecutorService createExecutorServiceDelegate() {
		threadFactory = new SingleThreadThreadFactory(getThreadPoolNamePrefix());
		return new SingleThreadedExecutor(threadFactory);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.concurrent.interfaces.ISingleThreadedExecutor#isExecutorThread()
	 */
	public final boolean isExecutorThread() {
		return isExecutorThread(Thread.currentThread());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.concurrent.interfaces.ISingleThreadedExecutor#isExecutorThread(java.lang.Thread)
	 */
	public final boolean isExecutorThread(Thread thread) {
		if (thread != null && threadFactory != null) {
			return thread.equals(threadFactory.getThread());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.concurrent.interfaces.INestableExecutor#getMaxDepth()
	 */
	public int getMaxDepth() {
		if (!(getExecutorServiceDelegate() instanceof INestableExecutor)) {
			throw new UnsupportedOperationException("Executor service delegate must implement INestableExecutor"); //$NON-NLS-1$
		}
		return ((INestableExecutor) getExecutorServiceDelegate()).getMaxDepth();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.concurrent.interfaces.INestableExecutor#readAndExecute()
	 */
	public boolean readAndExecute() {
		if (!(getExecutorServiceDelegate() instanceof INestableExecutor)) {
			throw new UnsupportedOperationException("Executor service delegate must implement INestableExecutor"); //$NON-NLS-1$
		}
		return ((INestableExecutor) getExecutorServiceDelegate()).readAndExecute();
	}
}
