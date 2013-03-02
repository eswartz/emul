/*
  MemoryV2Proxy.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.tcf.services.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.internal.tcf.services.remote.MemoryProxy;
import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;

import v9t9.server.tcf.services.IMemoryV2;

/**
 * @author ejs
 *
 */
public class MemoryV2Proxy extends MemoryProxy implements IMemoryV2 {
	private final Map<IMemoryV2.MemoryContentChangeListener, IChannel.IEventListener> changeListeners =
		new HashMap<IMemoryV2.MemoryContentChangeListener, IChannel.IEventListener>();

	/**
	 * @param channel
	 */
	public MemoryV2Proxy(IChannel channel) {
		super(channel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.tcf.services.remote.MemoryProxy#getName()
	 */
	@Override
	public String getName() {
		return IMemoryV2.NAME;
	}
	
	public IToken startChangeNotify(String notifyId, String contextId, 
			int addr, int size, 
			int msDelay, int granularity, final DoneCommand done) {
		return new Command(channel, this, COMMAND_START_CHANGE_NOTIFY, 
				new Object[] { notifyId, contextId, 
				addr, size,
				msDelay, granularity 
				}) {
			@Override
			public void done(Exception error, Object[] args) {
				if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
				done.done(error);
			}
		}.token;
	}
	
	public IToken stopChangeNotify(String notifyId, final DoneCommand done) {
		return new Command(channel, this, COMMAND_STOP_CHANGE_NOTIFY, 
				new Object[] { notifyId }) {
			@Override
			public void done(Exception error, Object[] args) {
				if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
				done.done(error);
			}
		}.token;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.IMemoryV2#addListener(v9t9.server.tcf.services.IMemoryV2.MemoryContentChangeListener)
	 */
	@Override
	public void addListener(final MemoryContentChangeListener listener) {
		IChannel.IEventListener l = new IChannel.IEventListener() {

            @SuppressWarnings("unchecked")
			public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("contentChanged")) {
                        assert args.length == 2;
                        List<Map<String, Object>> changes = (List<Map<String, Object>>) args[1];
                        MemoryChange[] memChanges = new MemoryChange[changes.size()];
                        int idx = 0;
                        for (Map<String, Object> change : changes) {
                        	memChanges[idx++] = new MemoryChange(change);
                        }
                        listener.contentChanged(args[0].toString(), memChanges);
                    }
                    else {
                        throw new IOException("MemoryV2 service: unknown event: " + name);
                    }
                }
                catch (Throwable x) {
                	Protocol.log("internal error in MemoryV2Proxy#handleEvent", x);
                	x.printStackTrace();
                }
            }
        };
        channel.addEventListener(this, l);
        changeListeners.put(listener, l);		
	}
	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.IMemoryV2#removeListener(v9t9.server.tcf.services.IMemoryV2.MemoryContentChangeListener)
	 */
	@Override
	public void removeListener(MemoryContentChangeListener listener) {
		 IChannel.IEventListener l = changeListeners.remove(listener);
	        if (l != null) channel.removeEventListener(this, l);		
	}
}
