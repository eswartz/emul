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
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.JSON.Binary;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.server.tcf.MemoryWriteTracker;

/**
 * @author ejs
 *
 */
public class MemoryService extends BaseServiceImpl {

	/**
	 * Standard commands 
	 */
	private static final String GET = "get";
	private static final String SET = "set";
	private static final String GET_CONTEXT = "getContext";
	private static final String GET_CHILDREN = "getChildren";
	private static final String MEMORY_CHANGED = "memoryChanged";
	
	private static final String START_NOTIFY = "startNotify";
	private static final String STOP_NOTIFY = "stopNotify";
	
	class ListenerInfo {
		private final IMemoryDomain domain;

		/**
		 * @param domain
		 * @param delay
		 */
		public ListenerInfo(final IMemoryDomain domain, int delay, int minGap) {
			this.domain = domain;
			tracker = new MemoryWriteTracker(domain);
			period = delay;
			this.minGap = minGap;
		}
		MemoryWriteTracker tracker;
		TimerTask task;
		int minGap;
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
					synchronized (ListenerInfo.this) {
						System.out.println(System.currentTimeMillis());
						synchronized (tracker) {
							BitSet bs = tracker.getChangedMemory();
							if (!bs.isEmpty()) {
								sendMemoryChangedEvent(domain, bs, minGap);
							}
							bs.clear();
						}
					}
				}
			};
			timer.schedule(task, 0, period);
		}
		
		public synchronized void stop() {
			if (task != null) {
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

		public synchronized void updateInfo(int delay, int minGap) {
			stopTask();
			period = delay;
			this.minGap = minGap;
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
	public MemoryService(IMachine machine, IChannel channel) {
		super(machine, channel, IMemory.NAME);
		
		listeners = new HashMap<String, ListenerInfo>();
		timer = new Timer(true);
		
		registerCommand(GET_CHILDREN, 1, 1);
		registerCommand(GET_CONTEXT, 1, 1);
		registerCommand(SET, 6, 1);
		registerCommand(GET, 5, 1);
		
		registerCommand(START_NOTIFY, 3, 0);
		registerCommand(STOP_NOTIFY, 1, 0);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.local.BaseServiceImpl#handleCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object[] handleCommand(String name, Object[] args)
			throws ErrorReport, Exception {
		if (GET_CHILDREN.equals(name)) {
			// args: contextId
			// ret: list of contextIds
			String id = args[0] != null ? args[0].toString() : null;
			if (id == null || id.length() == 0 || "root".equals(id)) {
				// get domain IDs
				id = "";
				List<String> contextIds = new ArrayList<String>();
				for (IMemoryDomain domain : machine.getMemory().getDomains()) {
					contextIds.add(domain.getIdentifier());
				}
				return new Object[] { contextIds };
			}
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);
		} else if (GET_CONTEXT.equals(name)) {
			// args: contextId
			// args: object of attributes
			
			IMemoryDomain domain = getDomainOrError(args[0]);
			Map<String, Object> context = createContext(null, domain);
			if (context != null)
				return new Object[] { context };
			
			throw new ErrorReport("Unknown context " + args[0], IErrorReport.TCF_ERROR_INV_CONTEXT);
		} else if (GET.equals(name)) {
			// args: 0:contextId 1:addr 2:word_size 3:size 4:mode
			// ret: content error ranges
			
			try {
				IMemoryDomain domain = getDomainOrError(args[0]);
				int word_size = ((Number) args[2]).intValue();
				if (word_size <= 0 || (word_size & (word_size - 1)) != 0)
					throw new ErrorReport("Bad word size " + word_size, 
							IErrorReport.TCF_ERROR_INV_DATA_SIZE);
	
				
				/*int mode = ((Number) args[4]).intValue();*/
				int addr = ((Number) args[1]).intValue();
				int size = ((Number) args[3]).intValue();
				if (size < 0 || size != ((size + word_size - 1) & ~(word_size - 1)))
					throw new ErrorReport("Bad size of " + size + " @ " + word_size, 
							IErrorReport.TCF_ERROR_INV_DATA_SIZE);
	
				if (addr < 0 || addr != ((addr) & ~(word_size - 1)))
					throw new ErrorReport("Bad alignment of " + addr + " @ " + word_size, 
							IErrorReport.TCF_ERROR_INV_ADDRESS);
	
				byte[] buf = new byte[size];
	
				for (int i = 0; i < size; i++) {
					buf[i] = domain.flatReadByte(addr + i);
				}
				
				return new Object[] { new Binary(buf, 0, buf.length), null, null };
			} catch (ErrorReport e) {
				return new Object[] { null, e, null };
			}
			
		} else if (SET.equals(name)) {
			// args: 0:contextId 1:addr 2:word_size 3:size 4:mode 5:content
			// ret: content error ranges
			
			try {
				IMemoryDomain domain = getDomainOrError(args[0]);
				int word_size = ((Number) args[2]).intValue();
				if (word_size < 0 || (word_size & (word_size - 1)) != 0)
					throw new ErrorReport("Bad word size " + word_size, 
							IErrorReport.TCF_ERROR_INV_DATA_SIZE);
	
				/*int mode = ((Number) args[4]).intValue();*/
				int addr = ((Number) args[1]).intValue();
				int size = ((Number) args[3]).intValue();
				if (size < 0 || size != ((size + word_size - 1) & ~(word_size - 1)))
					throw new ErrorReport("Bad size of " + size + " @ " + word_size, 
							IErrorReport.TCF_ERROR_INV_DATA_SIZE);
	
				if (addr < 0 || addr != ((addr) & ~(word_size - 1)))
					throw new ErrorReport("Bad alignment of " + addr + " @ " + word_size, 
							IErrorReport.TCF_ERROR_INV_ADDRESS);
	
				byte[] buf;
				if (args[5] instanceof List) {
					@SuppressWarnings("unchecked")
					List<Number> list = (List<Number>) args[5];
					buf = new byte[list.size()];
					for (int i = 0; i < buf.length; i++)
						buf[i] = ((Number) list.get(i)).byteValue();
				} else {
					buf = JSON.toByteArray(args[5]);
				}
	
				for (int i = 0; i < size; i++) {
					domain.flatWriteByte(addr + i, buf[i]);
				}
				
				return new Object[] { null, null };
			} catch (ErrorReport e) {
				return new Object[] { e, null };
			}
		} else if (START_NOTIFY.equals(name)) {
			// args: 0:contextID  1:ms 2:gap
			IMemoryDomain domain = getDomainOrError(args[0]);
			int delay = ((Number) args[1]).intValue();
			int minGap = ((Number) args[2]).intValue();
			
			if (delay <= 0 || minGap < 0)
				throw new ErrorReport("Bad rate or minimum gap", 
						IErrorReport.TCF_ERROR_INV_DATA_SIZE);
				
			ListenerInfo listener = listeners.get(domain.getIdentifier());
			if (listener == null) {
				listener = new ListenerInfo(domain, delay, minGap);
				listeners.put(domain.getIdentifier(), listener);
				listener.start();
			} else {
				listener.updateInfo(delay, minGap);
			}
			
			return new Object[] { };
		} else if (STOP_NOTIFY.equals(name)) {
			// args: 0:contextID  
			IMemoryDomain domain = getDomainOrError(args[0]);
			
			ListenerInfo listener = listeners.get(domain.getIdentifier());
			if (listener != null) {
				listener.stop();
				listeners.remove(domain.getIdentifier());
			}
			
			return new Object[] { };
		}
		return null;
	}

	/**
	 * @param object
	 * @return
	 * @throws ErrorReport 
	 */
	private IMemoryDomain getDomainOrError(Object object) throws ErrorReport {
		String id = object != null ? object.toString() : null;
		
		IMemoryDomain domain = machine.getMemory().getDomain(id);
		if (domain != null) 
			return domain;
		
		throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);
	}

	/**
	 * @param domain
	 * @return
	 */
	private Map<String, Object> createContext(String parent, IMemoryDomain domain) {
		Map<String, Object> ctx = new HashMap<String, Object>();
		String id = domain.getIdentifier();
		
		int minAddr = 0;
		int maxAddr = 0;
		for (IMemoryEntry entry : domain.getFlattenedMemoryEntries()) {
			if (entry.getName().equals(IMemoryDomain.UNMAPPED_MEMORY_ID))
				continue;
			if (entry.getAddr() < minAddr)
				minAddr = entry.getAddr();
			if (entry.getAddr() + entry.getSize() > maxAddr)
				maxAddr = entry.getAddr() + entry.getSize();
		}
		
		if (minAddr == 0 && maxAddr == 0)
			return null;
		
		ctx.put(IMemory.PROP_ID, id);
		ctx.put(IMemory.PROP_NAME, domain.getName());
		ctx.put(IMemory.PROP_START_BOUND, minAddr);
		ctx.put(IMemory.PROP_END_BOUND, maxAddr);
		ctx.put(IMemory.PROP_BIG_ENDIAN, true);
		ctx.put(IMemory.PROP_PARENT_ID, parent);
		ctx.put(IMemory.PROP_ADDRESS_SIZE, 2);
		ctx.put(IMemory.PROP_ACCESS_TYPES, new String[] { 
				IMemory.ACCESS_DATA, IMemory.ACCESS_PHYSICAL,
				IMemory.ACCESS_USER
		});
		return ctx;
	}

	/**
	 * @param identifier
	 * @param bs
	 * @param data 
	 * @param minGap 
	 */
	protected void sendMemoryChangedEvent(final IMemoryDomain domain, BitSet bs, int minGap) {
		final List<Map<String, Object>> ranges = new ArrayList<Map<String,Object>>();
		
		int idx = bs.nextSetBit(0);
		while (idx >= 0) {
			int end = bs.nextClearBit(idx);
			if (end == -1) 
				end = bs.length();
			
			Map<String, Object> range = new HashMap<String, Object>(3);
			int addr = idx;
			int size = end - idx;
			
			range.put(IMemory.ErrorOffset.RANGE_KEY_ADDR, addr);
			range.put(IMemory.ErrorOffset.RANGE_KEY_SIZE, size);
			
			// custom
			byte[] data = new byte[size];
			for (int i = 0; i < size; i++)
				data[i] = domain.flatReadByte(addr + i);
			
			range.put("data", new Binary(data, 0, data.length));
			
			ranges.add(range);
			
			idx = bs.nextSetBit(end);
		}

		Protocol.invokeLater(new Runnable() {
			public void run() {
				try {
					Protocol.sendEvent(IMemory.NAME, MEMORY_CHANGED, 
							JSON.toJSONSequence(new Object[] { domain.getIdentifier(), ranges }, true));
				} catch (IOException e) {
					Protocol.log("Failed to send event", e);
				}
			}
		});
		
	}

}
