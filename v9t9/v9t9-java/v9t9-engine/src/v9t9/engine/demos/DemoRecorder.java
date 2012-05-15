/**
 * 
 */
package v9t9.engine.demos;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoHandler.IDemoListener;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.FullRegisterWriteTracker;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.SimpleRegisterWriteTracker;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.FullMemoryWriteTracker;
import v9t9.common.memory.SimpleMemoryWriteTracker;
import v9t9.common.settings.Settings;
import v9t9.common.sound.TMS9919Consts;
import v9t9.engine.demos.events.SoundWriteDataEvent;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.events.VideoWriteDataEvent;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;
import ejs.base.properties.IProperty;
import ejs.base.timer.FastTimer;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;
/**
 * @author ejs
 *
 */
public class DemoRecorder {
	private final IDemoOutputStream os;
	private final ListenerList<IDemoListener> listeners;


	private FastTimer timer;
	private Runnable timerTask;

	private SimpleRegisterWriteTracker vdpRegisterListener;

	private SimpleMemoryWriteTracker vdpMemoryListener;

	private FullRegisterWriteTracker soundRegisterListener;
	private final IMachine machine;
	
	private int firstVidAddr;
	private int nextVidAddr;
	private byte[] videoBytes = new byte[256];
	private int videoIdx;

	private boolean useSoundRegisters;
	private FullMemoryWriteTracker soundDataListener;
	
	private int soundMmioAddr;
	private byte[] soundBytes = new byte[256];
	private int soundIdx;
	private int timerRate;
	
	public DemoRecorder(IMachine machine, IDemoOutputStream os, ListenerList<IDemoListener> listeners) throws IOException {
		this.machine = machine;
		this.os = os;
		this.timerRate = os.getTimerRate();
		this.listeners = listeners;
		
		useSoundRegisters = ! machine.getSound().getGroupName().equals(TMS9919Consts.GROUP_NAME);
		if (!useSoundRegisters) {
			soundMmioAddr = 0x8400;
		}
		
		connect();
		
		sendSetupInfo();
	}
	
	/**
	 * @throws NotifyException 
	 * 
	 */
	private void sendSetupInfo() throws IOException {
		// send VDP data
		int memSize = machine.getVdp().getMemorySize();
		for (int addr = 0; addr < memSize; ) {
			int toUse = Math.min(255, memSize - addr);
			ByteMemoryAccess access = machine.getVdp().getByteReadMemoryAccess(addr);
			os.writeEvent(new VideoWriteDataEvent(addr, access.memory, access.offset, toUse));
			addr += toUse;
		}

		// send video regs
		IRegisterAccess vra = machine.getVdp();
		int lastReg = vra.getFirstRegister() + vra.getRegisterCount();
		for (int i = vra.getFirstRegister(); i < lastReg; i++) {
			os.writeEvent(new VideoWriteRegisterEvent(i, vra.getRegister(i)));
		}

		if (useSoundRegisters) {
			// send sound regs 
			IRegisterAccess sra = machine.getSound();
			int slastReg = sra.getFirstRegister() + sra.getRegisterCount();
			for (int i = sra.getFirstRegister(); i < slastReg; i++) {
				os.writeEvent(new SoundWriteRegisterEvent(i, sra.getRegister(i)));
			}
		} else {
			// send silence
			os.writeEvent(new SoundWriteDataEvent(soundMmioAddr, 
					new byte[] { (byte) 0x9f, (byte) 0xbf, (byte) 0xdf, (byte) 0xff }));
		}
	}

	public void stop() throws IOException {
		disconnect();
		
		os.close();
	}

	private void fail(final Throwable t) {
		t.printStackTrace();
		
		listeners.fire(new IFire<IDemoListener>() {

			@Override
			public void fire(IDemoListener listener) {
				t.printStackTrace();
				listener.stopped(new NotifyEvent(System.currentTimeMillis(), null, Level.ERROR, 
						"Unexpected error writing demo: " + t.getMessage()));
			}
		});

		disconnect();
	}
	
	/**
	 * 
	 */
	private void connect() {
		timer = new FastTimer("demo");
		
		final IProperty pauseDemoSetting = Settings.get(machine, IDemoHandler.settingDemoPaused);
		timerTask = new Runnable() {
			
			@Override
			public void run() {
				try {
					if (os != null) {
						flushData();
						if (!pauseDemoSetting.getBoolean()) {
							os.writeEvent(new TimerTick(os.getElapsedTime()));
						}
					}
				} catch (final Throwable t) {
					fail(t);
				}				
			}
		};
		timer.scheduleTask(timerTask, timerRate);
		
		vdpRegisterListener = new SimpleRegisterWriteTracker(machine.getVdp(),
				machine.getVdp().getFirstRegister(),
				machine.getVdp().getRecordableRegs());
		vdpRegisterListener.addRegisterListener();
		
		vdpMemoryListener = new SimpleMemoryWriteTracker(machine.getVdp().getVideoMemory(), 8);
		vdpMemoryListener.addMemoryRange(0, machine.getVdp().getMemorySize());
		vdpMemoryListener.addMemoryListener();
		
		if (useSoundRegisters) {
			soundRegisterListener = new FullRegisterWriteTracker(machine.getSound());
			soundRegisterListener.addRegisterListener();
		} else {
			soundDataListener = new FullMemoryWriteTracker(machine.getConsole(), 0);
			soundDataListener.addMemoryRange(soundMmioAddr, 1);
			soundDataListener.addMemoryListener();
		}
		
	}

	/**
	 * 
	 */
	private void disconnect() {
		timer.cancel();
		vdpRegisterListener.removeRegisterListener();
		vdpMemoryListener.removeMemoryListener();

		if (soundRegisterListener != null)
			soundRegisterListener.removeRegisterListener();
		if (soundDataListener != null)
			soundDataListener.removeMemoryListener();
	}

	protected void flushData() throws IOException {
		flushVideoRegs();
		
		flushVideoMem();
		
		if (soundRegisterListener != null) {
			flushSoundRegs();
		} else if (soundDataListener != null) {
			flushSoundData();
		}
	}


	protected void flushVideoMem() throws IOException {
		synchronized (vdpMemoryListener) {
			BitSet changes = vdpMemoryListener.getChangedMemory();
			synchronized (changes) {
				firstVidAddr = nextVidAddr = 0;
				videoIdx = 0;
				for (int idx = changes.nextSetBit(0); idx >= 0; idx = changes.nextSetBit(idx + 1)) { 
					if (videoIdx >= videoBytes.length || idx != nextVidAddr) {
						os.writeEvent(new VideoWriteDataEvent(firstVidAddr, videoBytes, videoIdx));
						firstVidAddr = nextVidAddr = idx;
						videoIdx = 0;
					}
					videoBytes[videoIdx++] = machine.getVdp().readAbsoluteVdpMemory(idx);
					nextVidAddr++;
				}
				if (videoIdx > 0) {
					os.writeEvent(new VideoWriteDataEvent(firstVidAddr, videoBytes, videoIdx));
				}
			}
			vdpMemoryListener.clearChanges();
		}
	}
	
	protected void flushSoundData() throws IOException {
		synchronized (soundDataListener) {
			List<Integer> changes = soundDataListener.getChanges();
			synchronized (changes) {
				for (Integer chg : changes) { 
					if (soundIdx >= soundBytes.length) {
						os.writeEvent(new SoundWriteDataEvent(soundMmioAddr, soundBytes, soundIdx));
						soundIdx = 0;
					}
					soundBytes[soundIdx++] = (byte) (chg & 0xff);
				}
				if (soundIdx > 0) {
					os.writeEvent(new SoundWriteDataEvent(soundMmioAddr, soundBytes, soundIdx));
					soundIdx = 0;
				}
			}
			soundDataListener.clearChanges();
		}
	}

	protected void flushSoundRegs() throws IOException {
		synchronized (soundRegisterListener) {
			List<Long> changes = soundRegisterListener.getChanges();
			synchronized (changes) {
				for (Long ent : changes) {
					os.writeEvent(new SoundWriteRegisterEvent(
							(int) (ent >> 32), (int) (ent & 0xffffffff)));
				}
			}
			soundRegisterListener.clearChanges();
		}
	}

	protected void flushVideoRegs() throws IOException {
		synchronized (vdpRegisterListener) {
			Map<Integer, Integer> changes = vdpRegisterListener.getChanges();
			synchronized (changes) {
				for (Map.Entry<Integer, Integer> chg : changes.entrySet()) {
					if (chg.getKey() >= 0) {
						os.writeEvent(new VideoWriteRegisterEvent(chg.getKey(), chg.getValue()));
					}
				}
				vdpRegisterListener.clearChanges();
			}
		}
	}
}
