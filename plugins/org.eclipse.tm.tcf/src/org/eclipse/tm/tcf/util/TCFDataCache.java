/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.util;

import java.util.HashSet;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * Objects of this class are used to cache TCF remote data.
 * The cache is asynchronous state machine. The states are:
 *  1. Valid - cache is in sync with remote data, use getError() and getData() to get cached data;
 *  2. Invalid - cache is out of sync, start data retrieval by calling validate();
 *  3. Pending - cache is waiting result of a command that was sent to remote peer.
 * @param <V> - type of data to be stored in the cache.
 */
public abstract class TCFDataCache<V> implements Runnable {

    private Throwable error;
    private boolean valid;
    private boolean posted;
    private V data;
    
    protected final IChannel channel;
    protected IToken command;

    private final HashSet<Runnable> waiting_list = new HashSet<Runnable>();
    
    public TCFDataCache(IChannel channel) {
        assert channel != null;
        this.channel = channel;
    }
    
    private void post() {
        if (posted) return;
        if (waiting_list.isEmpty()) return;
        Protocol.invokeLater(this);
        posted = true;
    }
    
    /**
     * @return true if cache contains up-to-date data (or data retrieval error).
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * @return true if data retrieval command is in progress.
     */
    public boolean isPending() {
        return command != null;
    }
    
    /**
     * @return error object if data retrieval ended with an error, or null if retrieval was successful.
     * Note: It is prohibited to call this method when cache is not valid. 
     */
    public Throwable getError() {
        assert valid;
        return error;
    }
    
    /**
     * @return cached data object.
     * Note: It is prohibited to call this method when cache is not valid. 
     */
    public V getData() {
        assert Protocol.isDispatchThread();
        assert valid;
        return data;
    }
    
    /**
     * Notify waiting clients about cache state change and remove them from wait list.
     * It is responsibility of clients to check if the state change was one they are waiting for.
     */
    public void run() {
        assert Protocol.isDispatchThread();
        posted = false;
        Runnable[] arr = waiting_list.toArray(new Runnable[waiting_list.size()]);
        waiting_list.clear();
        for (Runnable r : arr) {
            if (r instanceof TCFDataCache<?> && ((TCFDataCache<?>)r).posted) continue;
            r.run();
        }
    }
    
    /**
     * Add a client call-back to cache wait list.
     * Client call-backs are activated when cache state changes.
     * Call-backs are removed from waiting list after that.
     * It is responsibility of clients to check if the state change was one they are waiting for.
     * @param cb - a call-back object
     */
    public void wait(Runnable cb) {
        assert Protocol.isDispatchThread();
        assert !valid;
        if (cb != null) waiting_list.add(cb);
    }
    
    /**
     * Initiate data retrieval if the cache is not valid.
     * @return true if the cache is already valid
     */
    public boolean validate() {
        assert Protocol.isDispatchThread();
        if (channel.getState() != IChannel.STATE_OPEN) {
            error = null;
            command = null;
            valid = true;
            data = null;
        }
        else {
            if (command != null) return false;
            if (!valid && !startDataRetrieval()) return false;
        }
        assert valid;
        assert command == null;
        post();
        return true;
    }
    
    /**
     * End cache pending state.
     * @param token - pending command handle.
     * @param error - data retrieval error or null
     * @param data - up-to-date data object
     */
    public void set(IToken token, Throwable error, V data) {
        assert Protocol.isDispatchThread();
        if (command != token) return;
        command = null;
        if (channel.getState() != IChannel.STATE_OPEN) data = null;
        this.error = error;
        this.data = data;
        valid = true;
        post();
    }

    /**
     * Force cache to become valid, cancel pending data retrieval if any.
     * @param data - up-to-date data object
     */
    public void reset(V data) {
        assert Protocol.isDispatchThread();
        if (command != null) {
            command.cancel();
            command = null;
        }
        this.data = data;
        error = null;
        valid = true;
        post();
    }
    
    /**
     * Invalidate the cache. If retrieval is in progress - let it continue.
     */
    public void reset() {
        assert Protocol.isDispatchThread();
        error = null;
        valid = false;
        data = null;
        post();
    }
    
    /**
     * Force cache to invalid state, cancel pending data retrieval if any.
     */
    public void cancel() {
        assert Protocol.isDispatchThread();
        if (command != null) {
            command.cancel();
            command = null;
        }
        error = null;
        valid = false;
        data = null;
        post();
    }
    
    @Override
    public String toString() {
        StringBuffer bf = new StringBuffer();
        bf.append('[');
        if (valid) bf.append("valid,");
        if (posted) bf.append("posted,");
        if (error != null) bf.append("error,");
        bf.append("data=");
        bf.append(data == null ? "null" : data.toString());
        bf.append(']');
        return bf.toString();
    }
    
    /**
     * Sub-classes should override this method to implement actual data retrieval logic.
     * @return true is all done, false if retrieval is in progress.
     */
    protected abstract boolean startDataRetrieval();
}
