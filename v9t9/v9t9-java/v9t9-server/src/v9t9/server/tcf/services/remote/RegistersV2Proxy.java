/**
 * 
 */
package v9t9.server.tcf.services.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.internal.tcf.services.remote.RegistersProxy;
import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;

import ejs.base.utils.Pair;

import v9t9.server.tcf.services.IRegistersV2;

/**
 * @author ejs
 *
 */
public class RegistersV2Proxy extends RegistersProxy implements IRegistersV2 {
	private final Map<IRegistersV2.RegisterContentChangeListener, IChannel.IEventListener> changeListeners =
		new HashMap<IRegistersV2.RegisterContentChangeListener, IChannel.IEventListener>();

	/**
	 * @param channel
	 */
	public RegistersV2Proxy(IChannel channel) {
		super(channel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.tcf.services.remote.MemoryProxy#getName()
	 */
	@Override
	public String getName() {
		return IRegistersV2.NAME;
	}
	
	@Override
	public IToken startChangeNotify(String notifyId, String contextId, 
			Collection<Integer> regNums, 
			int msDelay, int granularity, final DoneCommand done) {
		return new Command(channel, this, COMMAND_START_CHANGE_NOTIFY, 
				new Object[] { notifyId, contextId, regNums, msDelay, granularity }) {
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
	
	@Override
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
	 * @see v9t9.server.tcf.services.IMemoryV2#addListener(v9t9.server.tcf.services.IRegistersV2.RegisterContentChangeListener)
	 */
	@Override
	public void addListener(final RegisterContentChangeListener listener) {
		IChannel.IEventListener l = new IChannel.IEventListener() {

			public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("contentChanged")) {
                        assert args.length == 2;
                        
                        String notifyId = args[0].toString();
                        byte[] regData = JSON.toByteArray(args[1]);
                        
                        List<Pair<Integer, RegisterChange[]>> regChangeList = new ArrayList<Pair<Integer,RegisterChange[]>>();
                        
                        int regIdx = 0;
                    	int baseReg = regData[regIdx++];   // signed
                    	int regSize = regData[regIdx++] & 0xff;

                        while (regIdx < regData.length) {
                        	int timestampOffs = 0;
                        	for (int s = 0; s < 4; s++)
                        		timestampOffs |= (regData[regIdx++] & 0xff) << (8 * (4 - s - 1));
                        	
                        	int count = regData[regIdx++] & 0xff;
                        	
	                        RegisterChange[] regChanges = new RegisterChange[count];
	                        for (int idx = 0; idx < count; idx++) {
	                        	// number
	                        	int regNum = regData[regIdx++] & 0xff;
	                        	
	                        	// value, big-endian
	                        	int value = 0;
	                        	for (int s = 0; s < regSize; s++) {
	                        		value |= (regData[regIdx++] & 0xff) << (8 * (regSize - s - 1));
	                        	}
	                        	
	                        	regChanges[idx] = new RegisterChange(regNum + baseReg, value);
	                        }
	                        
	                        regChangeList.add(new Pair<Integer, IRegistersV2.RegisterChange[]>(timestampOffs, regChanges));
                        }
                        
                        listener.contentChanged(notifyId, regChangeList);
                    }
                    else {
                        throw new IOException("RegisterV2 service: unknown event: " + name);
                    }
                }
                catch (Throwable x) {
                	Protocol.log("Internal error in RegistersV2Proxy#handleEvent", x);
                    x.printStackTrace();
                }
            }
        };
        channel.addEventListener(this, l);
        changeListeners.put(listener, l);		
	}
	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.IMemoryV2#removeListener(v9t9.server.tcf.services.IRegistersV2.RegisterContentChangeListener)
	 */
	@Override
	public void removeListener(RegisterContentChangeListener listener) {
		 IChannel.IEventListener l = changeListeners.remove(listener);
	        if (l != null) channel.removeEventListener(this, l);		
	}
}
