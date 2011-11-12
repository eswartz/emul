/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.concurrent.executors;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.runtime.concurrent.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.concurrent.interfaces.IExecutor;
import org.eclipse.tm.te.runtime.concurrent.nls.Messages;
import org.eclipse.tm.te.runtime.extensions.ExecutableExtension;

/**
 * Abstract delegating execution service implementation.
 */
public abstract class AbstractDelegatingExecutorService extends ExecutableExtension implements IExecutor, ExecutorService {
	// The executor service to delegate the API calls to
	private ExecutorService delegate;

	// The thread pool name prefix
	private String threadPoolNamePrefix;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.ExecutableExtension#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.doSetInitializationData(config, propertyName, data);

		if (config != null && data instanceof Map<?, ?>) {
			Map<?, ?> params = (Map<?, ?>) data;
			// Initialize the thread pool name prefix field by reading the
			// "threadPoolNamePrefix" extension attribute if present.
			threadPoolNamePrefix = (String) params.get("threadPoolNamePrefix"); //$NON-NLS-1$
			if (threadPoolNamePrefix == null || threadPoolNamePrefix.trim().length() == 0) {
				threadPoolNamePrefix = ""; //$NON-NLS-1$
			}
		}

		// Create the executor service delegate
		this.delegate = createExecutorServiceDelegate();
		Assert.isNotNull(delegate);
	}

	/**
	 * Returns the thread pool name prefix if specified by the extension.
	 *
	 * @return The thread pool name prefix or an empty string.
	 */
	public String getThreadPoolNamePrefix() {
		return threadPoolNamePrefix != null ? threadPoolNamePrefix : ""; //$NON-NLS-1$
	}

	/**
	 * Invoked by the constructor exactly once to create the executor service
	 * delegate instance.
	 *
	 * @return The executor service instance and never <code>null</code>.
	 */
	protected abstract ExecutorService createExecutorServiceDelegate();

	/**
	 * Returns the executor service delegate instance.
	 *
	 * @return The executor service delegate instance.
	 */
	protected final ExecutorService getExecutorServiceDelegate() {
		return delegate;
	}

	/**
	 * Log the given exception as error to the error log.
	 *
	 * @param e
	 *            The exception or <code>null</code>.
	 */
	protected void logException(Throwable e) {
		if (e != null) {
			IStatus status = new Status(
					IStatus.ERROR,
					CoreBundleActivator.getUniqueIdentifier(),
					NLS.bind(Messages.AbstractDelegatingExecutorService_unhandledException,
							 e.getLocalizedMessage()), e);
			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command) {
		delegate.execute(command);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 */
	@Override
	public void shutdown() {
		delegate.shutdown();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 */
	@Override
	public List<Runnable> shutdownNow() {
		return delegate.shutdownNow();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#isShutdown()
	 */
	@Override
	public boolean isShutdown() {
		return delegate.isShutdown();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		return delegate.isTerminated();
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return delegate.awaitTermination(timeout, unit);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
	 */
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return delegate.submit(task);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
	 */
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return delegate.submit(task, result);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
	 */
	@Override
	public Future<?> submit(Runnable task) {
		return delegate.submit(task);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection)
	 */
	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return delegate.invokeAll(tasks);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return delegate.invokeAll(tasks, timeout, unit);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection)
	 */
	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return delegate.invokeAny(tasks);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return delegate.invokeAny(tasks, timeout, unit);
	}
}
