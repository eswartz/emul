/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)- [345387]Open the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.url;

/**
 * A helper class used to synchronize producer and consumer threads. It is used
 * to join a thread with its asynchronous call backs or listeners. Usually it is
 * used in a case in which the thread have to wait for the end of an
 * asynchronously called method.
 * <p>
 * The following is an example:
 * <p>
 * 
 * <pre>
 * final Rendezvous rendezvous = new Rendezvous(); 
 * service.open(path, IFileSystem.TCF_O_READ, null, new DoneOpen() { 
 *     public void doneOpen(IToken token, FileSystemException error, IFileHandle hdl) {
 *         ...
 *         rendezvous.arrive(); 
 *     } 
 * });
 * try{
 *     renderzvous.waiting(1000); //Waiting for 1 second.
 * }catch(InterruptedException e){
 *     // Waiting has timed out.
 *     ...
 * }
 * </pre>
 * 
 * The call renderzvous.waiting(1000) won't return until renderzvous.arrive() is
 * called in the doneOpen(), or the waiting has timed out.
 * <p>
 * A rendezvous can be reused once it is reset:
 * <p>
 * 
 * <pre>
 * renderzvous.reset(); 
 * service.open(path, IFileSystem.TCF_O_READ, null, new DoneOpen() { 
 *     public void doneOpen(IToken token, FileSystemException error, IFileHandle hdl) {
 *         ...
 *         rendezvous.arrive(); 
 *     } 
 * });
 * try{
 *     renderzvous.waiting(2000); //Waiting for 2 seconds.
 * }catch(InterruptedException e){
 *     // Waiting has timed out.
 *     ...
 * }
 * </pre>
 * 
 */
public class Rendezvous {
	// Flag indicating if the other thread has arrived.
	private boolean arrived;

	/**
	 * Called to unblock the thread that is waiting on this rendezvous.
	 */
	public synchronized void arrive() {
		arrived = true;
		notifyAll();
	}

	/**
	 * Called to block the current thread until it is woken up by
	 * another thread or until it is timed out.
	 * 
	 * @param timeout The timeout time.
	 * @throws InterruptedException The waiting has timed out.
	 */
	public synchronized void waiting(long timeout) throws InterruptedException {
		long now = System.currentTimeMillis();
		while (!arrived && (timeout <= 0 || System.currentTimeMillis() - now < timeout)) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
			}
		}
		if (!arrived)
			throw new InterruptedException();
	}
	
	/**
	 * Called to block the current thread until it is woken up by another 
	 * thread. 
	 * 
	 * @throws InterruptedException The waiting has timed out.
	 */
	public synchronized void waiting() throws InterruptedException {
		waiting(0);
	}

	/**
	 * Reset the rendezvous so that it is reusable.
	 */
	public synchronized void reset() {
		arrived = false;
	}
}
