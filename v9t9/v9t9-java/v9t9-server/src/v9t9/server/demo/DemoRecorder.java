/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;

import v9t9.common.demo.IDemoHandler.IDemoListener;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;
import v9t9.common.settings.Settings;
import v9t9.common.sound.TMS9919Consts;
import v9t9.engine.video.v9938.VdpV9938;
import v9t9.server.demo.events.SoundWriteDataEvent;
import v9t9.server.demo.events.SoundWriteRegisterEvent;
import v9t9.server.demo.events.TimerTick;
import v9t9.server.demo.events.VideoWriteDataEvent;
import v9t9.server.demo.events.VideoWriteRegisterEvent;
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

	private IRegisterWriteListener vdpRegisterListener;

	private IMemoryWriteListener vdpMemoryListener;

	private IMemoryDomain videoMem;

	private IRegisterWriteListener soundRegisterListener;
	private final IMachine machine;
	
	private int firstVidAddr;
	private int nextVidAddr;
	private byte[] videoBytes = new byte[256];
	private int videoIdx;

	private boolean useSoundRegisters;
	private IMemoryWriteListener soundDataListener;
	
	private int soundMmioAddr;
	private byte[] soundBytes = new byte[256];
	private int soundIdx;
	private long timerRate;
	
	public DemoRecorder(IMachine machine, IDemoOutputStream os, int timerRate,
			ListenerList<IDemoListener> listeners) throws NotifyException {
		this.machine = machine;
		this.os = os;
		this.timerRate = timerRate;
		this.listeners = listeners;
		
		useSoundRegisters = ! machine.getSound().getGroupName().equals(TMS9919Consts.GROUP_NAME);
		if (!useSoundRegisters) {
			soundMmioAddr = 0x8400;
		}
		
		videoMem = machine.getMemory().getDomain(IMemoryDomain.NAME_VIDEO);

		connect();
		
		sendSetupInfo();
	}
	
	/**
	 * @throws NotifyException 
	 * 
	 */
	private void sendSetupInfo() throws NotifyException {
		// send VDP data
		int memSize = machine.getVdp().getMemorySize();
		for (int addr = 0; addr < memSize; ) {
			if (machine.getVdp() instanceof VdpV9938) {
				if ((addr & 0x3fff) == 0) {
					// set the memory page
					os.writeEvent(new VideoWriteRegisterEvent(
							14, addr / 0x4000));
				}
			}
			int toUse = Math.min(255, memSize - addr);
			if ((addr & ~0x3fff) != ((addr + toUse) & ~0x3fff)) {
				toUse = 0x4000 - (addr & 0x3fff);
			}
			ByteMemoryAccess access = machine.getVdp().getByteReadMemoryAccess(addr);
			os.writeEvent(new VideoWriteDataEvent(addr & 0x3fff, access.memory, access.offset, toUse));
			addr += toUse;
		}

		// send video regs
		IRegisterAccess vra = machine.getVdp();
		int lastReg = vra.getFirstRegister() + vra.getRegisterCount();
		for (int i = vra.getFirstRegister(); i < lastReg; i++) {
			os.writeEvent(new VideoWriteRegisterEvent(i, vra.getRegister(i)));
		}

		// send sound regs 
		IRegisterAccess sra = machine.getSound();
		int slastReg = sra.getFirstRegister() + sra.getRegisterCount();
		for (int i = sra.getFirstRegister(); i < slastReg; i++) {
			os.writeEvent(new SoundWriteRegisterEvent(i, sra.getRegister(i)));
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
						flushSoundData();
						flushVideoData();
						if (!pauseDemoSetting.getBoolean()) {
							os.writeEvent(new TimerTick());
						}
					}
				} catch (final Throwable t) {
					fail(t);
				}				
			}
		};
		timer.scheduleTask(timerTask, timerRate);
		
		vdpRegisterListener = new IRegisterWriteListener() {

			@Override
			public void registerChanged(int reg, int value) {
				if (reg >= 0) {
					try {
						flushVideoData();
						os.writeEvent(new VideoWriteRegisterEvent(reg, value));
					} catch (Exception e) {
						fail(e);
					}
				}
				
			}
			
		};
		machine.getVdp().addWriteListener(vdpRegisterListener);
		
		vdpMemoryListener = new IMemoryWriteListener() {

			@Override
			public void changed(IMemoryEntry entry, int addr, Number value) {
				try {
					pushSetVideoMemory((short) addr, entry.flatReadByte(addr));
				} catch (Exception e) {
					fail(e);
				}
			}
			
		};
		videoMem.addWriteListener(vdpMemoryListener);
		
		
		if (useSoundRegisters) {
			soundRegisterListener = new IRegisterWriteListener() {
				
				@Override
				public void registerChanged(int reg, int value) {
					try {
						os.writeEvent(new SoundWriteRegisterEvent(reg, value));
					} catch (Exception e) {
						fail(e);
					}
				}
				
			};
			machine.getSound().addWriteListener(soundRegisterListener);
		} else {
			soundDataListener = new IMemoryWriteListener() {

				@Override
				public void changed(IMemoryEntry entry, int addr, Number value) {
					if (addr == soundMmioAddr) {
						try {
							if (soundIdx >= soundBytes.length) {
								flushSoundData();
							}
							soundBytes[soundIdx++] = value.byteValue();
						} catch (Throwable t) {
							fail(t);
						}
					}
				}
				
			};
			machine.getConsole().addWriteListener(soundDataListener);
		}
		
	}

	/**
	 * 
	 */
	private void disconnect() {
		timer.cancel();
		machine.getVdp().removeWriteListener(vdpRegisterListener);
		videoMem.removeWriteListener(vdpMemoryListener);
		machine.getSound().removeWriteListener(soundRegisterListener);
		machine.getConsole().removeWriteListener(soundDataListener);
	}
	

	protected void flushSoundData() throws NotifyException {
		SoundWriteDataEvent event = new SoundWriteDataEvent(soundMmioAddr, soundBytes, soundIdx);
		os.writeEvent(event);
		
		soundIdx = 0;
	}
	protected void flushVideoData() throws NotifyException {
		VideoWriteDataEvent event = new VideoWriteDataEvent(firstVidAddr, videoBytes, videoIdx);
		os.writeEvent(event);
		
		firstVidAddr = nextVidAddr;
		videoIdx = 0;
	}
	
	public void pushSetVideoMemory(short addr_, byte val) throws NotifyException {
		int addr = addr_ & 0x3fff;
		if (videoIdx >= 255 || addr != nextVidAddr) {
			flushVideoData();
			firstVidAddr = addr;
			nextVidAddr = firstVidAddr;
			videoIdx = 0;
		}
		videoBytes[videoIdx++] = val;
		nextVidAddr++;
	}

}
