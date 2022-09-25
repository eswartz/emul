/*
  GplMmio.java

  (c) 2005-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import v9t9.common.hardware.IGplChip;

/** GPL chip entry
 * @author ejs
 */
public class GplMmio implements IConsoleMmioReader, IConsoleMmioWriter {
	private IGplChip chip;

    /**
     */
    public GplMmio(IGplChip chip) {
    	this.chip = chip;
     }

    public int getAddr() {
        return chip.getAddr();
    }
    
    public void setAddr(short addr) {
    	chip.setAddr(addr);
    }

    public boolean addrIsComplete() {
    	return chip.addrIsComplete();
    }
    
    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioReader#read
     */
    public byte read(int addr) {
    	if ((addr & 2) != 0) {
    	    /* >9802, address read */
    		return chip.readAddressByte();
    	} else {
    	    /* >9800, memory read */
    		return chip.readDataByte();
    	}
    }

	/**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
    	if ((addr & 2) != 0) {				
    	    /* >9C02, address write */
    		chip.writeAddressByte(val);
    	} else {					
    	    /* >9C00, data write */
    		chip.writeDataByte(val);
    	}   
    	
    }

	public void reset() {
		chip.reset();
	}
}
