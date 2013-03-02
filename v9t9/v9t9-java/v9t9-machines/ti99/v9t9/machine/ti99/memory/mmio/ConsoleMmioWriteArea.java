/*
  ConsoleMmioWriteArea.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.IConsoleMmioWriter;

public class ConsoleMmioWriteArea extends ConsoleMmioArea {
    protected final IConsoleMmioWriter writer;

	ConsoleMmioWriteArea(IConsoleMmioWriter writer) {
        this.writer = writer;
		if (writer == null) {
			throw new NullPointerException();
		}
    };
    
    @Override
    public void writeByte(IMemoryEntry entry, int addr, byte val) {
    	if (0 == (addr & 1))
    		writer.write(addr, val);
    }
    
    @Override
    public void writeWord(IMemoryEntry entry, int addr, short val) {
    	writer.write(addr, (byte) (val >> 8));
    }
}