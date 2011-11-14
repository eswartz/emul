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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.te.runtime.interfaces.IConditionTester;
import org.eclipse.tm.te.runtime.interfaces.callback.ICallback;

/**
 * Common implementation to handle and wait for asynchronous callback's.
 */
public class AsyncCallbackHandler {
	private final List<ICallback> callbacks = new Vector<ICallback>();
	private final IConditionTester conditionTester;
	private Throwable error;

	private final static boolean TRACING_ENABLED = Boolean.parseBoolean(Platform.getDebugOption("org.eclipse.tm.te.runtime/trace/callbacks")); //$NON-NLS-1$

	// This map will track who have added a specific callback to the handler.
	// This is for internal debugging purpose only.
	private final Map<ICallback, Throwable> callbackAdders = new Hashtable<ICallback, Throwable>();

	/**
	 * Constructor.
	 */
	public AsyncCallbackHandler() {
		this(null);
	}

	/**
	 * Constructor.
	 */
	public AsyncCallbackHandler(IConditionTester tester) {
		super();
		error = null;
		conditionTester = new AsyncCallbackConditionTester(tester);
	}

	/**
	 * Adds the given callback to the list of pending sequent's or callback's. In case of adding a
	 * callback to the list, the caller must remove the callback from list as well.
	 *
	 * @param callback The callback to add to the list.
	 */
	public void addCallback(ICallback callback) {
		if (callback != null && !callbacks.contains(callback)) {
			callbacks.add(callback);

			if (TRACING_ENABLED) {
				try {
					throw new Exception("Pending callback added!"); //$NON-NLS-1$
				}
				catch (Exception e) {
					callbackAdders.put(callback, e.fillInStackTrace());
				}
			}
		}
	}

	/**
	 * Removes the given callback from the list of pending sequent's or callback's.
	 *
	 * @param callback The callback to remove from the list.
	 */
	public void removeCallback(ICallback callback) {
		if (callback != null) {
			callbacks.remove(callback);

			if (TRACING_ENABLED) {
				callbackAdders.remove(callback);
			}
		}
	}

	/**
	 * Returns if or if not all previously added callback's have been removed again.
	 *
	 * @return <code>true</code> if the callback handler is empty, <code>false</code> otherwise.
	 */
	public final boolean isEmpty() {
		if (TRACING_ENABLED) {
			System.err.println(this.getClass().getName() + ": size = " + callbacks.size()); //$NON-NLS-1$
			if (callbacks.size() < 4) {
				synchronized (callbacks) {
					Iterator<?> iterator = callbacks.iterator();
					System.err.println("Remaining adders: "); //$NON-NLS-1$
					while (iterator.hasNext()) {
						Exception adder = (Exception) callbackAdders.get(iterator.next());
						adder.printStackTrace();
						System.err.println("*****"); //$NON-NLS-1$
					}
				}
			}
		}
		return callbacks.isEmpty();
	}

	/**
	 * Remove all remaining callback's from the callback handler.
	 */
	public final void clear() {
		callbacks.clear();
	}

	/**
	 * Sets the given error for this handler. In case an error had been set before, the previous
	 * error will be preserved and the current error will be dropped.
	 *
	 * @param error The error to set or <code>null</code>.
	 */
	public final void setError(Throwable error) {
		if (error != null) {
			// The first error is the most precious.
			// Don't override it by subsequent errors.
			if (this.error == null) {
				this.error = error;
			}
		}
		else {
			this.error = error;
		}
	}

	/**
	 * Returns the associated error.
	 *
	 * @return The error or <code>null</code>.
	 */
	public final Throwable getError() {
		return error;
	}

	/**
	 * Returns the condition tester to use for waiting for the callback handler
	 * until all callbacks have been invoked and the external condition tester
	 * is fulfilled too.
	 *
	 * @return The condition tester instance.
	 */
	public IConditionTester getConditionTester() {
		return conditionTester;
	}

	final class AsyncCallbackConditionTester implements IConditionTester {
		private final IConditionTester externalTester;

		/**
		 * Constructor.
		 * <p>
		 * If an external condition tester is passed in, {@link #isConditionFulfilled()} will return
		 * <code>true</code> if either the callback handler is empty or the external tester's
		 * {@link #isConditionFulfilled()} returns <code>true</code>.
		 *
		 * @param tester An external condition tester or <code>null</code>.
		 */
		public AsyncCallbackConditionTester(IConditionTester tester) {
			externalTester = tester;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.runtime.interfaces.IConditionTester#cleanup()
		 */
		@Override
		public void cleanup() {
			if (!isEmpty()) {
				clear();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.runtime.interfaces.IConditionTester#isConditionFulfilled()
		 */
		@Override
		public boolean isConditionFulfilled() {
			// the condition is fulfilled if no remaining callback's are registered!
			return isEmpty() || (externalTester != null && externalTester.isConditionFulfilled());
		}
	}
}
