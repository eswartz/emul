/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.async;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.callback.Callback;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;

/**
 * The asynchronous callback collector is an extension to the asynchronous callback handler. The
 * difference is that the collector is not blocking a thread for waiting till all associated
 * callback's have been finished, the collector will by itself call an asynchronous callback if all
 * other callback's have been removed from the collector.
 *
 * In case of an error, all outstanding asynchronous callback's are ignored and the master callback
 * is invoked directly.
 *
 * Note: The creator of the asynchronous callback collector must call <code>AsyncCallbackCollector.initDone()</code>
 * 		 if all initialization work and chaining of the target callback's are done. This will do avoid that
 * 		 the collector is fired before all pending callback could have been added!
 */
public class AsyncCallbackCollector extends AsyncCallbackHandler {
	// The final master callback to send if all other callback have been removed.
	final ICallback callback;
	// Once the master callback has been fired, the collector is marked finish.
	private boolean isFinished;
	private boolean initDone;

	// The reference to the callback invocation delegate
	private ICallbackInvocationDelegate delegate = null;

	/**
	 * Delegation interfaces used by the asynchronous callback collector to
	 * invoke the final callback.
	 */
	public static interface ICallbackInvocationDelegate {

		/**
		 * Invokes the given runnable.
		 *
		 * @param runnable The runnable. Must not be <code>null</code>.
		 */
		public void invoke(Runnable runnable);
	}

	/**
	 * Simple target callback handling an asynchronous callback collector parent itself and remove
	 * themselves from the collector after callback has done.
	 * <p>
	 * Errors are handled using the collector.
	 */
	public static class SimpleCollectorCallback extends Callback {
		private final AsyncCallbackCollector collector;

		/**
		 * Constructor.
		 *
		 * @param collector The parent asynchronous callback collector. Must be not
		 *            <code>null</code>.
		 */
		public SimpleCollectorCallback(AsyncCallbackCollector collector) {
			Assert.isNotNull(collector);
			// Remember the callback collector instance
			this.collector = collector;
			// Add ourself to the callback collector
			this.collector.addCallback(this);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.runtime.callback.Callback#internalDone(java.lang.Object, org.eclipse.core.runtime.IStatus)
		 */
		@Override
		protected void internalDone(Object caller, IStatus status) {
			// If an error occurred, pass on to the collector and
			// let the collector handle the error.
			if (status.getException() != null) {
				collector.handleError(status.getMessage(), status.getException());
			}
			else {
				collector.removeCallback(this);
			}
		}

		/**
		 * Return the collector using this callback.
		 */
		protected AsyncCallbackCollector getCollector() {
			return collector;
		}
	}

	/**
	 * Constructor.
	 */
	public AsyncCallbackCollector() {
		this(null, null);
	}

	/**
	 * Constructor.
	 *
	 * @param callback The final callback to invoke if the collector enters the finished state.
	 * @param delegate The callback invocation delegate. Must not be <code>null</code> if the callback is not <code>null</code>.
	 */
	public AsyncCallbackCollector(ICallback callback, ICallbackInvocationDelegate delegate) {
		super();

		if (callback != null) Assert.isNotNull(delegate);

		// We have to add our master callback to the list of callback to avoid that
		// the collector is running empty to early!
		addCallback(callback);
		this.callback = callback;
		this.delegate = delegate;

		// We are not finished yet.
		isFinished = false;
		initDone = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.async.AsyncCallbackHandler#addCallback(org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public final synchronized void addCallback(ICallback callback) {
		Assert.isTrue(!isFinished() || getError() != null);
		super.addCallback(callback);
	}

	/**
	 * Checks if the collector run empty and we can fire the master callback.
	 */
	protected final synchronized void checkAndFireCallback() {
		if (!isEmpty() || isFinished()) {
			return;
		}
		isFinished = true;
		onCollectorFinished();
	}

	/**
	 * Called from {@link #checkAndFireCallback()} once the collector has been marked finished.
	 * Subclasses may override this method for any necessary finished handling necessary.<br>
	 * The default implementation is just firing the collectors final callback.
	 * <p>
	 * Note: The method does not need to be explicitly synchronized. It's called from inside a
	 * 		 <code>synchronized(this)</code> block!
	 */
	protected void onCollectorFinished() {
		if (callback != null) {
			Assert.isNotNull(delegate);
			delegate.invoke(new Runnable() {
				@Override
				public void run() {
					Throwable error = getError();
					IStatus status = new Status((error != null ? (error instanceof OperationCanceledException ? IStatus.CANCEL : IStatus.ERROR) : IStatus.OK), CoreBundleActivator
					                .getUniqueIdentifier(), 0, (error != null ? error.getMessage() : null), error);
					callback.done(this, status);
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.async.AsyncCallbackHandler#removeCallback(org.eclipse.tm.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public final synchronized void removeCallback(ICallback callback) {
		super.removeCallback(callback);
		checkAndFireCallback();
	}

	/**
	 * Returns if or if not all pending callback's have been invoked and the collector entered the
	 * finished state. Once in the finished state, the state cannot be reversed anymore!
	 */
	public final boolean isFinished() {
		return isFinished;
	}

	/**
	 * Method to be called if the initialization of the collector is finished. The creator of the
	 * collector must call this method in order to "activate" the collector finally.
	 */
	public final synchronized void initDone() {
		Assert.isTrue(initDone == false);
		if (callback != null) {
			removeCallback(callback);
		}
		initDone = true;
	}

	/**
	 * Creates and {@link ExecutionException} for the given error message and the given cause. Calls
	 * {@link #handleError(Throwable)} afterwards. If the given cause is an
	 * {@link OperationCanceledException}, the cause will be passed on untouched.
	 *
	 * @param errMsg The error message or <code>null</code>.
	 * @param cause The cause or <code>null</code>.
	 */
	public final synchronized void handleError(final String errMsg, Throwable cause) {
		// In case of an error, isFinished() can be set before all callback's are in.
		// Callback's that come later do not change the result, they are silently ignored.
		Assert.isTrue(!isFinished() || getError() != null);

		Throwable error = cause;

		// In case the incoming error is itself an OperationCanceledException, re-throw it as is.
		// Otherwise re-package the cause to an ExecutionException.
		//
		// In all cases the exceptions will be thrown here in order to create a useful back trace.
		if (error instanceof OperationCanceledException) {
			// leave everything as is
		}
		else {
			cause = new ExecutionException(errMsg, error);
		}
		error.fillInStackTrace();
		handleError(error);
	}

	/**
	 * Handles the given error. If the collector is not yet finished and not error has been set yet,
	 * the collector is finished and the error is set as the error to pass on with the callback. If
	 * the collector is already finished, the method will return immediately.
	 *
	 * @param error The error to handle. Must be not <code>null</code>.
	 */
	public final synchronized void handleError(final Throwable error) {
		Assert.isNotNull(error);

		// In case of an error, isFinished() can be set before all callback's are in.
		// Callback's that come later do not change the result, they are silently ignored.
		Assert.isTrue(!isFinished() || getError() != null);

		if (!isFinished()) {
			setError(error);
			clear();
			checkAndFireCallback();
		}

		// Re-throw any Error except assertion errors ! NEVER REMOVE THIS !
		if ((error instanceof Error) && !(error instanceof AssertionError)) {
			throw (Error) error;
		}
	}
}
