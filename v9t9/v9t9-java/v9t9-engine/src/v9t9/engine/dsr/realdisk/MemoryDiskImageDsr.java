/*
  MemoryDiskImageDsr.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.dsr.realdisk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.machine.IMachine;
import v9t9.engine.dsr.IMemoryIOHandler;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * This disk DSR assumes all control and ports are in MMIO.
 * 
 * Consumes 6 bytes from base:
 * 
 * 	0: write: cmd; read: status
 *  1: write/read: track addr
 *  2: write/read: sector addr
 *  3: write/read: data
 *  4: write/read: [0 | 0 | 0 | 0 | motor | hold | head | side ] 
 *  5: write/read: [0 | 0 | 0 | 0 | 0 | D2 | D1 | D0] 
 *  
 * @author ejs
 *
 */
public class MemoryDiskImageDsr extends BaseDiskImageDsr implements IMemoryIOHandler {

	public static final int COMMAND = 0; 
	public static final int STATUS = 0; 
	public static final int TRACK = 1; 
	public static final int SECTOR = 2; 
	public static final int DATA = 3; 
	public static final int FLAGS = 4;
	public static final int DSK = 5;
	/** Motor is spun up */
	public static final int FL_MOTOR = 0x8;
	public static final int FL_HOLD = 0x4;
	public static final int FL_HEAD = 0x2;
	public static final int FL_SIDE = 0x1;
	
	private final int baseAddr;

	private byte flags;

	/**
	 * 
	 */
	public MemoryDiskImageDsr(IMachine machine, int baseAddr) {
		super(machine);
		this.baseAddr = baseAddr;
		flags = 0;
		
		settingDsrEnabled.addListener(new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				settings.get(IDeviceIndicatorProvider.settingDevicesChanged).firePropertyChange();
			}
		});

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.IMemoryIOHandler#writeData(int, byte)
	 */
	@Override
	public void writeData(int offset, byte val) {
		switch (offset - baseAddr) {
		case COMMAND:
			fdc.writeCommand(val);
			break;
		case TRACK:
			fdc.setTrackReg(val);
			//DSK.status &= ~fdc_LOSTDATA;
			break;
		case SECTOR:
			fdc.setSectorReg(val);
			//DSK.status &= ~fdc_LOSTDATA;
			break;
		case DATA:
			fdc.writeData(val);
			break;
		case DSK:
			if (val == 0)
				fdc.selectDisk(0, false);
			else
				fdc.selectDisk(val, true);
			break;
		case FLAGS: {
			byte oldflags = flags;
			flags = val;
			if (((flags ^ oldflags) & FL_SIDE) != 0) {
				setDiskSide((flags & FL_SIDE) != 0 ? 1 : 0);
			}
			if (((flags ^ oldflags) & FL_HEAD) != 0) {
				fdc.setHeads((flags & FL_HEAD) != 0);
			}
			if (((flags ^ oldflags) & FL_HOLD) != 0) {
				fdc.setHold((flags & FL_HOLD) != 0);
			}
			
			// always pass on, since setting will keep it going
			fdc.setDiskMotor((flags & FL_MOTOR) != 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.IMemoryIOHandler#readData(int)
	 */
	@Override
	public byte readData(int offset) {
		switch (offset - baseAddr) {
		case COMMAND:
			byte ret = fdc.readStatus();
			
			return ret;
		case TRACK:
			byte ret1 = fdc.getTrackReg();
			
			return ret1;
		case SECTOR:
			byte ret2 = fdc.getSectorReg();
			
			return ret2;
		case DATA:
			return fdc.readByte();
		case DSK:
			return (byte) fdc.getSelectedDisk();
		case FLAGS:
			flags = (byte) ((getSide() != 0 ? FL_SIDE : 0) 
				| (fdc.isMotorRunning() ? FL_MOTOR : 0)
				| (fdc.isHeads() ? FL_HEAD : 0)
				| (fdc.isHold() ? FL_HOLD : 0));
			return flags;
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.IMemoryIOHandler#handlesAddress(int)
	 */
	@Override
	public boolean handlesAddress(int addr) {
		return settingDsrEnabled.getBoolean() && addr >= baseAddr && addr <= baseAddr + DSK;
	}

	/**
	 * @return
	 */
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {

		if (!settingDsrEnabled.getBoolean())
			return Collections.emptyList();
		
		if (imageMapper.getDiskSettingsMap().isEmpty())
			return Collections.emptyList();
			
		List<IDeviceIndicatorProvider> list = new ArrayList<IDeviceIndicatorProvider>();
		/*
		// one inactive icon for DSR
		DeviceIndicatorProvider deviceIndicatorProvider = new DeviceIndicatorProvider(
				diskImageDsrEnabled, 
				"Disk image activity",
				IDevIcons.DISK_IMAGE, IDevIcons.DISK_IMAGE);
		list(deviceIndicatorProvider);
		 */
			
		for (Map.Entry<String, IProperty> entry : imageMapper.getDiskSettingsMap().entrySet()) {
			IDeviceIndicatorProvider drive = fdc.getDrive(entry.getKey());
			if (drive != null) {
				IProperty activeProperty = drive.getActiveProperty(); 
				DiskMotorIndicatorProvider provider = new DiskMotorIndicatorProvider(entry.getKey(), 
						activeProperty);
				list.add(provider);
			}
		}
		return list;
	}

}
