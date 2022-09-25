/*
  IGplChip.java

  (c) 2022 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;

import ejs.base.properties.IPersistable;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.SettingSchema;

/** 
 * Handle the work of a GPL chip.  This maintains the memory,
 * register state, and  behavior of the GPL.
 * @author ejs
 */
public interface IGplChip extends IPersistable, IRegisterAccess {
	static public final SettingSchema settingDumpGplAccess = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DumpGplAccess", Boolean.FALSE);
    
	IMemoryDomain getGplMemory();

	IMachine getMachine();
	
	void reset();
	
	short getAddr();
	void setAddr(short addr);
	
    boolean getWaddrFlag();
    void setWaddrFlag(boolean flag);
    boolean getRaddrFlag();
    void setRaddrFlag(boolean flag);
    
    boolean addrIsComplete();
    
    /** Get buffered read before address was autoincremented */
    byte getBuf();
    /** Set buffered read before address was autoincremented */
    void setBuf(byte buf);
    
    /** Read a byte and increment address */
    byte readGrom();

	byte readAddressByte();
	byte readDataByte();
	void writeAddressByte(byte val);
	void writeDataByte(byte val);

}
