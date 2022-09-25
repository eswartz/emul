/*
  IVdpChip.java

  (c) 2005-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;


import java.util.BitSet;

import ejs.base.properties.IPersistable;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.settings.SettingSchema;

/** 
 * Handle the work of a VDP chip.  This maintains the memory,
 * register state, and  behavior of the VDP.
 * @author ejs
 */
public interface IVdpChip extends IPersistable, IRegisterAccess {
	SettingSchema settingVdpInterruptRate = new SettingSchema(
			ISettingsHandler.MACHINE, "VdpInterruptRate", Integer.valueOf(60));
	// this should pretty much stay on
	SettingSchema settingCpuSynchedVdpInterrupt = new SettingSchema(
			ISettingsHandler.MACHINE, "CpuSynchedVdpInterrupt", Boolean.valueOf(
					true));
	SettingSchema settingDumpVdpAccess = new SettingSchema(
			ISettingsHandler.TRANSIENT, "DumpVdpAccess", Boolean.FALSE);

	IMemoryDomain getVideoMemory();

	IMachine getMachine();
	
	/** 60Hz timer.  Use this or syncVdpInterrupt / addCpuCycles */
	void tick();
	
	/** coprocessing */
	boolean isThrottled();
	void work();
	
	/** This is called regularly from the CPU and should trigger the VDP
	 * interrupt according to the desired frequency. 
	 * @param machine */
	void syncVdpInterrupt(IMachine machine);

	/** Inform the VDP of the given number of cycles invoked on CPU side. */
	void addCpuCycles(int cycles);

	/**
	 * Tell whether interlacing is active.
	 * 
	 * For use in rendering, we need to know whether raw R9_IL (interlace) bit is set
	 * and also the R9_EO (even/odd) bit is set, which would provide the page flipping
	 * required to *see* two pages.  Finally, the "odd" graphics page must be visible
	 * for the flipping and interlacing to occur.
	 * @return
	 */
	public boolean isInterlacedEvenOdd();

	/**
	 * @return
	 */
	int getGraphicsPageSize();

	/**
	 * @return
	 */
	int getModeNumber();

	/** Read VDP status.
	 */
	byte readVdpStatus();

	/** Touch byte in the absolute VDP memory address
	 * and note the side effects.
	 */
	void touchAbsoluteVdpMemory(int vdpaddr);

	/** Read byte from absolute VDP memory at the given address
	 */
	byte readAbsoluteVdpMemory(int vdpaddr);

	/** Write byte to absolute VDP memory at the given address,
	 * also touching it.
	 */
	void writeAbsoluteVdpMemory(int vdpaddr, byte byt);

	ByteMemoryAccess getByteReadMemoryAccess(int vdpaddr);

	int getMemorySize();
	
	/**
	 * Populate and return a bit set whose 'on' bits
	 * represent areas of video memory that contribute to 
	 * the visible part of the screen.
	 * @param granularityShift optional scaling factor, e.g., a value of 8
	 * will only represent one bit per 256 bytes.
	 * @return bitset
	 */
	BitSet getVisibleMemory(int granularityShift);
	
	/**
	 * Get the registers worth recording in a demo.
	 * For example, the V9938 acceleration control registers
	 * are not important (since they need a CPU driving them,
	 * to fetch status and reset the state) and the commands
	 * invoke VDP memory read/write commands anyway.
	 * @return
	 */
	BitSet getRecordableRegs();
	
	boolean isBlank();
}
