/**
 * 
 */
package v9t9.server.tcf.services.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.JSON.Binary;
import org.eclipse.tm.tcf.protocol.Protocol;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.server.tcf.MemoryWriteTracker;
import v9t9.server.tcf.services.IMemoryV2;

/**
 * @author ejs
 *
 */
public class MemoryServiceV2 extends MemoryService {
 
	class ListenerInfo {
		private final IMemoryDomain domain;

		/**
		 * @param domain
		 * @param delay
		 */
		public ListenerInfo(final IMemoryDomain domain, int delay, int granularity) {
			this.domain = domain;
			tracker = new MemoryWriteTracker(domain);
			period = delay;
			this.granularity = granularity;
		}
		MemoryWriteTracker tracker;
		TimerTask task;
		int granularity;
		long period;
		
		public synchronized void start() {
			if (task == null) {
				startTask();
				tracker.addMemoryListener();
			}
		}

		/**
		 * 
		 */
		protected void startTask() {
			task = new TimerTask() {
				@Override
				public void run() {
					sendEvent();
				}
			};
			timer.schedule(task, 0, period);
		}
		
		/**
		 * 
		 */
		protected synchronized void sendEvent() {
			synchronized (tracker) {
				System.out.println(System.currentTimeMillis());
				BitSet bs = tracker.getChangedMemory();
				if (!bs.isEmpty()) {
					sendMemoryChangedEvent(domain, bs, granularity);
				}
				bs.clear();
			}
		}
		
		public synchronized void stop() {
			if (task != null) {
				// flush
				sendEvent();
				
				tracker.removeMemoryListener();
				stopTask();
				task = null;
			}
		}

		/**
		 * tcf Memory startNotify "VIDEO" 16 1
		 * tcf Memory startNotify "CPU" 10000 1
		 */
		protected void stopTask() {
			task.cancel();
			timer.purge();
		}

		public synchronized void updateInfo(int delay, int granularity) {
			stopTask();
			period = delay;
			this.granularity = granularity;
			startTask();
		}
	}
	private Map<String, ListenerInfo> listeners;
	private Timer timer;

	/**
	 * @param machine
	 * @param channel
	 * @param serviceName
	 */
	public MemoryServiceV2(IMachine machine, IChannel channel) {
		super(machine, channel, IMemoryV2.NAME);
		
		listeners = new HashMap<String, ListenerInfo>();
		timer = new Timer(true);
		
		registerCommand(IMemoryV2.COMMAND_START_CHANGE_NOTIFY, 3, 1);
		registerCommand(IMemoryV2.COMMAND_STOP_CHANGE_NOTIFY, 1, 1);
		
		channel.addChannelListener(new IChannelListener() {
			
			@Override
			public void onChannelOpened() {
			}
			
			@Override
			public void onChannelClosed(Throwable error) {
				for (ListenerInfo info : listeners.values()) {
					info.stop();
				}
				timer.cancel();
			}
			
			@Override
			public void congestionLevel(int level) {
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.local.BaseServiceImpl#handleCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object[] handleCommand(String name, Object[] args)
			throws ErrorReport, Exception {
		if (IMemoryV2.COMMAND_START_CHANGE_NOTIFY.equals(name)) {
			return doStartNotify(args);
		} else if (IMemoryV2.COMMAND_STOP_CHANGE_NOTIFY.equals(name)) {
			return doStopNotify(args);
		}
		return null;
	}

	/**
	 * @param args
	 * @return
	 * @throws ErrorReport
	 */
	protected Object[] doStopNotify(Object[] args) throws ErrorReport {
		// args: 0:contextID  
		IMemoryDomain domain = getDomainOrError(args[0]);
		
		ListenerInfo listener = listeners.get(domain.getIdentifier());
		if (listener != null) {
			listener.stop();
			listeners.remove(domain.getIdentifier());
		}
		
		return new Object[] { null };
	}

	/**
	 * @param args
	 * @return
	 * @throws ErrorReport
	 */
	protected Object[] doStartNotify(Object[] args) throws ErrorReport {
		// args: 0:contextID  1:ms 2:gap
		IMemoryDomain domain = getDomainOrError(args[0]);
		int delay = ((Number) args[1]).intValue();
		int granularity = ((Number) args[2]).intValue();
		
		if (delay <= 0)
			throw new ErrorReport("Bad rate", 
					IErrorReport.TCF_ERROR_INV_NUMBER);
		if (granularity < 0 || (granularity & (granularity - 1)) != 0)
			throw new ErrorReport("Bad granularity", 
					IErrorReport.TCF_ERROR_INV_NUMBER);
			
		ListenerInfo listener = listeners.get(domain.getIdentifier());
		if (listener == null) {
			listener = new ListenerInfo(domain, delay, granularity);
			listeners.put(domain.getIdentifier(), listener);
			listener.start();
		} else {
			listener.updateInfo(delay, granularity);
		}
		
		return new Object[] { null };
	}

	
	/**
	 * @param identifier
	 * @param bs
	 * @param data 
	 * @param granularity 
	 */
	protected void sendMemoryChangedEvent(final IMemoryDomain domain, BitSet bs, int granularity) {
		final List<Map<String, Object>> ranges = new ArrayList<Map<String,Object>>();
		
		if (granularity < 1)
			granularity = 1;
		
		int curAddr = 0;
		int curEnd = 0;
		
		int idx = bs.nextSetBit(0);
		while (idx >= 0) {
			int end = bs.nextClearBit(idx);
			if (end == -1) 
				end = bs.length();
			
			int addr = idx & -granularity;
			int size = end - addr;
			size = (size + granularity - 1) & -granularity;
			
			if (curEnd == curAddr) {
				curAddr = addr;
			}
			else if (addr > curEnd) {
				addRange(ranges, curAddr, curEnd - curAddr, domain);
				curAddr = addr;
			}
			curEnd = addr + size;
			
			idx = bs.nextSetBit(end);
		}
		
		if (curEnd > curAddr) {
			addRange(ranges, curAddr, curEnd - curAddr, domain);
		}

		Protocol.invokeLater(new Runnable() {
			public void run() {
				try {
					Protocol.sendEvent(IMemoryV2.NAME, IMemoryV2.EVENT_CONTENT_CHANGED, 
							JSON.toJSONSequence(new Object[] { domain.getIdentifier(), ranges }, true));
				} catch (IOException e) {
					Protocol.log("Failed to send event", e);
				}
			}
		});
		
	}

	/**
	 * @param ranges
	 * @param addr
	 * @param size
	 * @param domain
	 */
	private void addRange(List<Map<String, Object>> ranges, int addr, int size,
			IMemoryDomain domain) {
		// custom
		byte[] data = new byte[size];
		for (int i = 0; i < size; i++)
			data[i] = domain.flatReadByte(addr + i);

		Map<String, Object> range = new HashMap<String, Object>(3);
		range.put(IMemoryV2.PROP_ADDR, addr);
		range.put(IMemoryV2.PROP_SIZE, size);
		range.put(IMemoryV2.PROP_DATA, new Binary(data, 0, data.length));
		ranges.add(range);
		
	}

	
}
