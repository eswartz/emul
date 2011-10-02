/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.concurrent.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.te.core.concurrent.Executors;
import org.eclipse.tm.te.core.concurrent.interfaces.IExecutorUtilDelegate;
import org.eclipse.tm.te.core.concurrent.interfaces.INestableExecutor;
import org.eclipse.tm.te.core.concurrent.interfaces.ISingleThreadedExecutor;
import org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager;
import org.eclipse.tm.te.core.extensions.ExecutableExtensionProxy;
import org.eclipse.tm.te.core.interfaces.IConditionTester;


/**
 * Utility class to provide helper methods to execute tasks at
 * a executor service asynchronous and synchronous.
 */
public final class ExecutorsUtil {

	/**
	 * Execution utility wait and dispatch utility extension point manager.
	 */
	protected static class ExecutorUtilDelegateExtensionPointManager extends AbstractExtensionPointManager<IExecutorUtilDelegate> {

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager#getExtensionPointId()
		 */
		@Override
		protected String getExtensionPointId() {
			return "org.eclipse.tm.te.core.concurrent.executorUtilDelegates"; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.core.extensions.AbstractExtensionPointManager#getConfigurationElementName()
		 */
		@Override
		protected String getConfigurationElementName() {
			return "executorUtilDelegate"; //$NON-NLS-1$
		}

		/**
		 * Returns the list of all contributed executor utility delegates.
		 *
		 * @return The list of contributed executor utility delegates, or an
		 *         empty array.
		 */
		public IExecutorUtilDelegate[] getExecutorUtilDelegates() {
			List<IExecutorUtilDelegate> contributions = new ArrayList<IExecutorUtilDelegate>();
			Collection<ExecutableExtensionProxy<IExecutorUtilDelegate>> proxies = getExtensions().values();
			for (ExecutableExtensionProxy<IExecutorUtilDelegate> proxy : proxies) {
				if (proxy.getInstance() != null&& !contributions.contains(proxy.getInstance())) {
					contributions.add(proxy.getInstance());
				}
			}

			return contributions.toArray(new IExecutorUtilDelegate[contributions.size()]);
		}

		/**
		 * Returns the executor utility delegate identified by its unique id. If
		 * no executor utility delegate with the specified id is registered,
		 * <code>null</code> is returned.
		 *
		 * @param id
		 *            The unique id of the executor utility delegate. Must not
		 *            be <code>null</code>
		 * @param newInstance
		 *            Specify <code>true</code> to get a new executor utility
		 *            delegate instance, <code>false</code> otherwise.
		 *
		 * @return The executor instance or <code>null</code>.
		 */
		public IExecutorUtilDelegate getExecutorUtilDelegate(String id, boolean newInstance) {
			Assert.isNotNull(id);

			IExecutorUtilDelegate executorUtilDelegate = null;
			if (getExtensions().containsKey(id)) {
				ExecutableExtensionProxy<IExecutorUtilDelegate> proxy = getExtensions().get(id);
				// Get the extension instance
				executorUtilDelegate = newInstance ? proxy.newInstance() : proxy.getInstance();
			}

			return executorUtilDelegate;
		}
	}

	// Reference to the executor service extension point manager
	private final static ExecutorUtilDelegateExtensionPointManager EXTENSION_POINT_MANAGER = new ExecutorUtilDelegateExtensionPointManager();

	// Reference to the used executor service.
	private final static ISingleThreadedExecutor EXECUTOR;
	// Reference to the used UI executor service (might be null if not available)
	private final static ISingleThreadedExecutor UI_EXECUTOR;

	/**
	 * Static constructor.
	 */
	static {
		EXECUTOR = (ISingleThreadedExecutor) Executors.getSharedExecutor("org.eclipse.tm.te.core.concurrent.executors.singleThreaded"); //$NON-NLS-1$
		Assert.isNotNull(EXECUTOR);
		UI_EXECUTOR = (ISingleThreadedExecutor) Executors.getSharedExecutor("org.eclipse.tm.ui.swt.executors.display"); //$NON-NLS-1$
	}

	/**
	 * Shutdown the executor service used.
	 */
	public static void shutdown() {
		if (EXECUTOR instanceof ExecutorService) {
			((ExecutorService) EXECUTOR).shutdown();
		}
		if (UI_EXECUTOR instanceof ExecutorService) {
			((ExecutorService) UI_EXECUTOR).shutdown();
		}
	}

	/**
	 * Checks if the current thread is identical with the executor thread.
	 *
	 * @return <code>True</code> if the current thread is the executor thread,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isExecutorThread() {
		return EXECUTOR != null ? EXECUTOR.isExecutorThread() : false;
	}

	/**
	 * Checks if the current thread is identical with the UI executor thread.
	 *
	 * @return <code>True</code> if the current thread is the UI executor
	 *         thread, <code>false</code> otherwise.
	 */
	public static boolean isUIExecutorThread() {
		return UI_EXECUTOR != null ? UI_EXECUTOR.isExecutorThread() : false;
	}

	/**
	 * Schedule the given {@link Runnable} for invocation within the used
	 * executor thread.
	 *
	 * @param runnable
	 *            The <code>java.lang.Runnable</code> to execute within the
	 *            executor thread.
	 */
	public static void execute(Runnable runnable) {
		if (runnable != null) {
			if (EXECUTOR instanceof ExecutorService) {
				if (!((ExecutorService) EXECUTOR).isShutdown()
						&& !((ExecutorService) EXECUTOR).isTerminated()) {
					EXECUTOR.execute(runnable);
				}
			} else {
				if (EXECUTOR != null) {
					EXECUTOR.execute(runnable);
				}
			}
		}
	}

	/**
	 * Schedule the given {@link Runnable} to run the current workbench display
	 * thread.
	 *
	 * @param runnable
	 *            The runnable to execute.
	 */
	public static void executeInUI(Runnable runnable) {
		if (runnable != null) {
			if (UI_EXECUTOR instanceof ExecutorService) {
				if (!((ExecutorService) UI_EXECUTOR).isShutdown()
						&& !((ExecutorService) UI_EXECUTOR).isTerminated()) {
					UI_EXECUTOR.execute(runnable);
				}
			} else {
				if (UI_EXECUTOR != null) {
					UI_EXECUTOR.execute(runnable);
				}
			}
		}
	}

	/**
	 * Waits either for the given condition tester to signal that the condition,
	 * the caller want's to wait for, has been completely fulfilled or till the
	 * timeout runs out. If the specified condition tester is <code>null</code>,
	 * the method will always wait till the timeout occurs. In case
	 * <code>timeout == 0</code> and <code>conditionTester == null</code>, the
	 * method returns immediately with the return value <code>true</code>!
	 *
	 * @param timeout
	 *            The timeout to wait in milliseconds. <code>0</code> means
	 *            infinite wait time!
	 * @param conditionTester
	 *            The condition tester to use for checking the interrupt
	 *            condition.
	 *
	 * @return <code>false</code> if the exit reason if that the waiting
	 *         condition has been fulfilled, <code>true</code> if the exit
	 *         reason is the timeout!
	 */
	public static boolean waitAndExecute(final long timeout,
			final IConditionTester conditionTester) {
		// both parameter are null, return immediately!
		if (conditionTester == null && timeout == 0)
			return true;

		// we assume that the exit reason will be the timeout
		boolean exitReason = true;

		// Remember the executors utility delegate down the road. As long
		// we don't leave the waitAndExecute method, the thread cannot change.
		IExecutorUtilDelegate lastDelegate = null;

		// Remember the start time to calculate the timeout
		final long startTime = System.currentTimeMillis();
		// keep going till either the condition tester or the timeout will
		// break the loop!
		while (true) {
			if (conditionTester != null && conditionTester.isConditionFulfilled()) {
				// the exit reason is the condition tester!
				exitReason = false;
				break;
			}
			if (timeout != 0 && ((System.currentTimeMillis() - startTime) >= timeout)) {
				// timeout occurred, just break the loop
				break;
			}
			// none of the break conditions are fulfilled, so wait a little bit
			// before testing again.
			if (isExecutorThread()) {
				// We are in the executor thread. Keep the command dispatching running.
				if (EXECUTOR instanceof INestableExecutor) {
					((INestableExecutor) EXECUTOR).readAndExecute();
					Thread.yield();
				} else {
					throw new IllegalStateException("waitAndExecute called from within a non-nestable executor service!"); //$NON-NLS-1$
				}
			}
			// Check if we are in the UI executor thread
			else if (isUIExecutorThread()) {
				// We are in the executor thread. Keep the command dispatching
				// running.
				if (UI_EXECUTOR instanceof INestableExecutor) {
					((INestableExecutor) UI_EXECUTOR).readAndExecute();
					Thread.yield();
				} else {
					throw new IllegalStateException("waitAndExecute called from within a non-nestable UI executor service!"); //$NON-NLS-1$
				}
			}
			// Check if we have a delegate contribution which is handling
			// the current thread.
			else {
				boolean foundHandlingDelegate = false;

				if (lastDelegate == null) {
					// Get all registered delegates
					IExecutorUtilDelegate[] delegates = EXTENSION_POINT_MANAGER.getExecutorUtilDelegates();
					for (IExecutorUtilDelegate delegate : delegates) {
						// Does the delegate handles the current thread?
						if (delegate.isHandledExecutorThread()) {
							foundHandlingDelegate = true;
							lastDelegate = delegate;
							// Read and dispatch one event
							delegate.readAndDispatch();
							break;
						}
					}
				} else {
					foundHandlingDelegate = true;
					// Read and dispatch one event
					lastDelegate.readAndDispatch();
				}

				if (!foundHandlingDelegate) {
					// Not in any executor thread, put the current thread to sleep
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) { /* ignored on purpose */ }
				}
			}
		}

		// give the condition tester the chance to cleanup
		if (conditionTester != null) {
			conditionTester.cleanup();
		}

		return exitReason;
	}
}
