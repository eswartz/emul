/**
 * IWRNotificationFireDelegateListener.java
 * Created on Sep 14, 2006
 *
 * Copyright (c) 2006 - 2009 Wind River Systems, Inc.
 *
 * The right to copy, distribute, modify, or otherwise make use
 * of this software may be licensed only pursuant to the terms
 * of an applicable Wind River license agreement.
 */
package org.eclipse.tm.te.runtime.interfaces.events;

/**
 * Common interface for notification fire delegate listeners.<br>
 * If a notification listener additionally implements this interface, the notification
 * manager will call the {@link #fire(Runnable)} method to delegate the thread
 * handling.
 *
 * @author tobias.schwarz@windriver.com
 */
public interface IEventFireDelegate {

	/**
	 * Fire the given runnable. If the given runnable is <code>null</code>,
	 * the method should return immediatelly. The implementator of the
	 * interface is responsible for the thread-handling.
	 *
	 * @param runnable The runnable that should be started for notification or <code>null</code>.
	 */
	public void fire(final Runnable runnable);
}
