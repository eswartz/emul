/**
 * 
 */
package v9t9.server.tcf.services.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;

import ejs.base.utils.Pair;

import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.RegisterWriteTracker;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.server.tcf.services.IRegistersV2;
import v9t9.server.tcf.services.IRegistersV2.RegisterChange;

/**
 * @author ejs
 *
 */
public class RegistersV2Service extends RegisterService {

	interface RegListenerInfo {
		void start();
		void stop();
	}
	
	/** map of notifyId to info */
	private Map<String, RegListenerInfo> listeners;
	private Timer timer;


	/**
	 * @param machine
	 * @param channel
	 */
	public RegistersV2Service(IMachine machine, IChannel channel) {
		super(machine, channel, IRegistersV2.NAME);
		
		listeners = new HashMap<String, RegListenerInfo>();
		timer = new Timer(true);
		
		registerCommand(IRegistersV2.COMMAND_START_CHANGE_NOTIFY, 5, 1);
		registerCommand(IRegistersV2.COMMAND_STOP_CHANGE_NOTIFY, 1, 1);
		
		channel.addChannelListener(new IChannelListener() {
			
			@Override
			public void onChannelOpened() {
			}
			
			@Override
			public void onChannelClosed(Throwable error) {
				for (RegListenerInfo info : listeners.values()) {
					info.stop();
				}
				timer.cancel();
			}
			
			@Override
			public void congestionLevel(int level) {
			}
		});
	}

	abstract class BaseListenerInfo implements RegListenerInfo {
		protected final String id;
		private final long period;
		
		protected final Map<Integer, String> regNumToIds = new HashMap<Integer, String>();
		protected final int granularity;
		
		protected int regSize;
		private TimerTask task;
		
		protected long timestamp;
		protected int baseReg;
		private BitSet regbits;
		private final IRegisterAccess access;
		private final Collection<Integer> trackedRegs;

		

		public BaseListenerInfo(String id, final IRegisterAccess access, 
				Collection<Integer> trackedRegs, int delay, int granularity) {
			this.id = id;
			this.access = access;
			this.trackedRegs = trackedRegs;
			this.period = delay;
			this.granularity = granularity;
			
			if (access.getRegisterCount() >= 32768)
				throw new UnsupportedOperationException("too many registers for -based encoding");
			
			baseReg = access.getFirstRegister();
			
			regbits = new BitSet();
			String prefix = access.getGroupName() + ".";
			regSize = 1;
			for (int reg : trackedRegs) {
				IRegisterAccess.RegisterInfo info = access.getRegisterInfo(reg);
				assert info != null;
				regbits.set(reg - baseReg);
				regNumToIds.put(reg, prefix + info.id);
				if (info.size > regSize) {
					regSize = info.size;
				}
			}
			
			init(access, regbits);
		}

		public synchronized void start() {
			if (task == null) {
				startTask();
				timestamp = System.currentTimeMillis();
				
				synchronized (access) {
					addRegisterListener();
					
					// populate every register the first time
					Map<Integer, Integer> changes = new HashMap<Integer, Integer>(trackedRegs.size());
					for (Integer regNum : trackedRegs) {
						changes.put(regNum, access.getRegister(regNum));
					}
					
					sendRegisterChangedEvent(id, baseReg, regSize, 
							(int) (System.currentTimeMillis() - timestamp), changes);
				}
			}
		}

		/**
		 * 
		 */
		protected void startTask() {
			task = new TimerTask() {
				@Override
				public void run() {
					synchronized (BaseListenerInfo.this) {
						sendEvent();
						timestamp = System.currentTimeMillis();
					}
				}
			};
			timer.schedule(task, 0, period);
		}
		
		public synchronized void stop() {
			if (task != null) {
				// flush
				sendEvent();
				
				removeRegisterListener();
				stopTask();
				task = null;
			}
		}

		protected void stopTask() {
			task.cancel();
			timer.purge();
		}

		/** Initialize tracker for the given registers */
		protected abstract void init(IRegisterAccess access, BitSet regbits);
		/** send register data via e.g. {@link RegistersV2Service#sendRegisterChangedEvent(String, int, Map)} */
		protected abstract void sendEvent();
		/** start listening */
		protected abstract void addRegisterListener();
		/** stop listening */
		protected abstract void removeRegisterListener();
	}



	class StandardListenerInfo extends BaseListenerInfo {
		private RegisterWriteTracker tracker;
		public StandardListenerInfo(String id, final IRegisterAccess access, 
				Collection<Integer> trackedRegs, int delay, int granularity) {
			super(id, access, trackedRegs, delay, granularity);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.server.tcf.services.local.RegistersV2Service.BaseListenerInfo#init(java.util.BitSet)
		 */
		@Override
		protected void init(IRegisterAccess access, BitSet regbits) {
			tracker = new RegisterWriteTracker(access, baseReg, regbits);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.server.tcf.services.local.RegistersV2Service.BaseListenerInfo#addRegisterListener()
		 */
		@Override
		protected void addRegisterListener() {
			tracker.addRegisterListener();
		}

		protected synchronized void sendEvent() {
			//System.out.println(System.currentTimeMillis());
			Map<Integer, Integer> changes = tracker.getChangeMapAndReset();
			if (!changes.isEmpty()) {
				sendRegisterChangedEvent(id, baseReg, regSize, 
						(int) (System.currentTimeMillis() - timestamp), changes);
			}
		}
		
		/* (non-Javadoc)
		 * @see v9t9.server.tcf.services.local.RegistersV2Service.BaseListenerInfo#removeRegisterListener()
		 */
		@Override
		protected void removeRegisterListener() {
			tracker.removeRegisterListener();
		}
	}

	
	class ContinuousListenerInfo extends BaseListenerInfo {
		private IRegisterWriteListener registerWriteListener;
		private List<Pair<Integer, Collection<RegisterChange>>> changes;
		private int timestampOffs;
		private Map<Integer, RegisterChange> curChanges;
		private IRegisterAccess access;

		public ContinuousListenerInfo(String id, final IRegisterAccess access, 
				Collection<Integer> trackedRegs, int delay, int granularity) {
			super(id, access, trackedRegs, delay, granularity);

		}
		
		/* (non-Javadoc)
		 * @see v9t9.server.tcf.services.local.RegistersV2Service.BaseListenerInfo#init(v9t9.common.machine.IRegisterAccess, java.util.BitSet)
		 */
		@Override
		protected void init(final IRegisterAccess access, final BitSet regbits) {
			this.access = access;
			changes = new ArrayList<Pair<Integer,Collection<RegisterChange>>>();
			
			registerWriteListener = new IRegisterWriteListener() {
	
				/* (non-Javadoc)
				 * @see v9t9.common.machine.IRegisterAccess.IRegisterWriteListener#registerChanged(int, int)
				 */
				@Override
				public void registerChanged(int reg, int value) {
					if (!regbits.get(reg - baseReg))
						return;
					
					synchronized (ContinuousListenerInfo.this) {
						synchronized (changes) {
							int offs = (int) (System.currentTimeMillis() - timestamp);
							if (curChanges == null || (offs - timestampOffs >= granularity)) {
								if (curChanges != null) {
									changes.add(new Pair<Integer, Collection<RegisterChange>>(timestampOffs, curChanges.values()));
								}
								curChanges = new TreeMap<Integer, IRegistersV2.RegisterChange>();
								timestampOffs = offs;
							}
							curChanges.put(reg, new RegisterChange(reg, value));
						}
					}
				}
				
			};
			
		}

		/* (non-Javadoc)
		 * @see v9t9.server.tcf.services.local.RegistersV2Service.BaseListenerInfo#addRegisterListener()
		 */
		@Override
		protected void addRegisterListener() {
			access.addWriteListener(registerWriteListener);
		}

		/* (non-Javadoc)
		 * @see v9t9.server.tcf.services.local.RegistersV2Service.BaseListenerInfo#removeRegisterListener()
		 */
		@Override
		protected void removeRegisterListener() {
			access.removeWriteListener(registerWriteListener);
		}
		
		/**
		 * 
		 */
		protected void sendEvent() {
			synchronized (access) {
				if (curChanges != null) {
					changes.add(new Pair<Integer, Collection<RegisterChange>>(timestampOffs, curChanges.values()));
				}
				if (!changes.isEmpty()) {
					sendRegisterChangedEvent(id, baseReg, regSize, timestamp, changes);
				}
				changes.clear();
				curChanges = null;
				timestamp = System.currentTimeMillis();
			}
		}
	}

	protected void writeReg(ByteArrayOutputStream bos, int reg) {
		bos.write(reg >> 8);
		bos.write(reg & 0xff);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.local.BaseServiceImpl#handleCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object[] handleCommand(String name, Object[] args)
			throws ErrorReport, Exception {
		if (IRegistersV2.COMMAND_START_CHANGE_NOTIFY.equals(name)) {
			return doStartNotify(args);
		} else if (IRegistersV2.COMMAND_STOP_CHANGE_NOTIFY.equals(name)) {
			return doStopNotify(args);
		}
		return super.handleCommand(name, args);
	}


	/**
	 * @param args
	 * @return
	 * @throws ErrorReport
	 */
	protected Object[] doStartNotify(Object[] args) throws ErrorReport {
		// args: 0:notifyID 1:contextID 2:trackedRegs 3:ms 4:granularity
		String id = args[0].toString();
		IRegisterAccess access = getAccessOrError(args[1].toString());
		@SuppressWarnings("unchecked")
		Collection<Integer> trackedRegs = (Collection<Integer>) args[2];
		int delay = ((Number) args[3]).intValue();
		int granularity = ((Number) args[4]).intValue();

		if (delay <= 0)
			throw new ErrorReport("Bad rate", 
					IErrorReport.TCF_ERROR_INV_NUMBER);
			
		RegListenerInfo listener = listeners.get(id);
		if (listener != null) {
			throw new ErrorReport("Listener " + id + " already registered", 
					IErrorReport.TCF_ERROR_ALREADY_ATTACHED);
		}
		
		if (granularity < 0)
			listener = new ContinuousListenerInfo(id, access, trackedRegs, delay, granularity);
		else
			listener = new StandardListenerInfo(id, access, trackedRegs, delay, granularity);
		
		listeners.put(id, listener);
		listener.start();
		
		return new Object[] { null };
	}

	
	/**
	 * @param args
	 * @return
	 * @throws ErrorReport
	 */
	protected Object[] doStopNotify(Object[] args) throws ErrorReport {
		// args: 0:notifyID  
		String id = args[0].toString();
		
		RegListenerInfo listener = listeners.get(id);
		if (listener != null) {
			listener.stop();
			listeners.remove(id);
		} else {
			throw new ErrorReport("Unknown listener " + id, 
					IErrorReport.TCF_ERROR_SYM_NOT_FOUND);
		}
		
		return new Object[] { null };
	}

	
	/**
	 * Send standard register change info
	 */
	protected void sendRegisterChangedEvent(final String id, int baseReg, int regSize,
			int timestampOffs, Map<Integer, Integer> changes) {

		if (changes.isEmpty())
			return;
		
		assert changes.size() < 65536;
		assert regSize < 256;
		
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// base & size
		writeReg(bos, baseReg);
		bos.write(regSize);
		
		//[[ one group
		
		// timestamp
		int offs = timestampOffs;
		for (int s = 0; s < 4; s++) {
			bos.write(offs >> (8 * (4 - s - 1)));
		}

		// # regs
		writeReg(bos, changes.size());
		
		// reg info
		for (Map.Entry<Integer, Integer> change : changes.entrySet()) {
			int regnum = change.getKey();
			
			// reg #
			writeReg(bos, regnum - baseReg);
			
			// value, big-endian
			int value = change.getValue();
			for (int s = 0; s < regSize; s++) {
				bos.write((byte) (value >> (8 * (regSize - s - 1))));
			}
		}

		//]]
		
		Protocol.invokeLater(new Runnable() {
			public void run() {
				try {
					Protocol.sendEvent(IRegistersV2.NAME, IRegistersV2.EVENT_CONTENT_CHANGED, 
							JSON.toJSONSequence(new Object[] { id,  
									new JSON.Binary(bos.toByteArray(), 0, bos.size()) }, 
									true));
				} catch (IOException e) {
					Protocol.log("Failed to send event", e);
				}
			}
		});
		
	}


	/**
	 * Send continuous register change info
	 */
	protected void sendRegisterChangedEvent(final String id, int baseReg, int regSize,
			long timestamp, List<Pair<Integer, Collection<RegisterChange>>> changes) {

		if (changes.isEmpty())
			return;
		
		assert regSize < 256;
		
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// base & reg size
		writeReg(bos, baseReg);
		bos.write(regSize);
		
		for (Pair<Integer, Collection<RegisterChange>> changeGroup : changes) {
			// timestamp offset
			int offs = changeGroup.first;
			for (int s = 0; s < 4; s++) {
				bos.write(offs >> (8 * (4 - s - 1)));
			}
			
			// # regs
			assert changeGroup.second.size() < 65536;
			writeReg(bos, changeGroup.second.size());
			
			for (RegisterChange change : changeGroup.second) {
				// reg #
				writeReg(bos, change.regNum - baseReg);
				
				// value, big-endian
				for (int s = 0; s < regSize; s++) {
					bos.write((byte) (change.value >> (8 * (regSize - s - 1))));
				}
			}
		}

		Protocol.invokeLater(new Runnable() {
			public void run() {
				try {
					Protocol.sendEvent(IRegistersV2.NAME, IRegistersV2.EVENT_CONTENT_CHANGED, 
							JSON.toJSONSequence(new Object[] { id,  
									new JSON.Binary(bos.toByteArray(), 0, bos.size()) }, 
									true));
				} catch (IOException e) {
					Protocol.log("Failed to send event", e);
				}
			}
		});
		
	}
}
