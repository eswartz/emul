/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;

import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuListener;
import v9t9.common.demo.IDemoHandler.IDemoListener;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;
import v9t9.common.sound.TMS9919Consts;

import v9t9.server.demo.events.*;
/**
 * @author ejs
 *
 */
public class DemoRecorder {
	private final IDemoOutputStream os;
	private final ListenerList<IDemoListener> listeners;


	private ICpuListener cpuListener;

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
	
	public DemoRecorder(IMachine machine, IDemoOutputStream os, ListenerList<IDemoListener> listeners) throws NotifyException {
		this.machine = machine;
		this.os = os;
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
		// send VDP regs and data
		IRegisterAccess vra = machine.getVdp();
		int lastReg = vra.getFirstRegister() + vra.getRegisterCount();
		for (int i = vra.getFirstRegister(); i < lastReg; i++) {
			os.writeEvent(new VideoWriteRegisterEvent(i, vra.getRegister(i)));
		}
		
		int memSize = machine.getVdp().getMemorySize();
		for (int addr = 0; addr < memSize; ) {
			int toUse = Math.min(255, memSize - addr);
			ByteMemoryAccess access = machine.getVdp().getByteReadMemoryAccess(addr);
			os.writeEvent(new VideoWriteDataEvent(addr, access.memory, access.offset, toUse));
			addr += toUse;
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
		cpuListener = new ICpuListener() {
			public void ticked(ICpu cpu) {
				try {
					if (os != null) {
						flushSoundData();
						flushVideoData();
						os.writeEvent(new TimerTick());
					}
				} catch (final Throwable t) {
					fail(t);
				}
			}
		};
		machine.getCpu().addListener(cpuListener);
		
		vdpRegisterListener = new IRegisterWriteListener() {

			@Override
			public void registerChanged(int reg, int value) {
				if (reg >= 0) {
					try {
						flushVideoData();
						os.writeEvent(new VideoWriteRegisterEvent((reg << 8) | 0x8000 | (value & 0xff)));
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
		machine.getCpu().removeListener(cpuListener);
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
