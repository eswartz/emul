/*
  ConsoleMmioReadArea.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.memory.mmio;

import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.IConsoleMmioReader;

public class ConsoleMmioReadArea extends ConsoleMmioArea {
    protected final IConsoleMmioReader reader;

	public ConsoleMmioReadArea(IConsoleMmioReader reader) {
        this.reader = reader;
		if (reader == null) {
			throw new NullPointerException();
		}
    }
	
	@Override
	public byte readByte(IMemoryEntry entry, int addr) {
		if (0 == (addr & 1))
			return reader.read(addr);
		return 0;
	}
	
	@Override
	public short readWord(IMemoryEntry entry, int addr) {
		return reader.read(addr);
	}
	
	@Override
	public byte flatReadByte(IMemoryEntry entry, int addr) {
		return 0;
	}
	
	@Override
	public short flatReadWord(IMemoryEntry entry, int addr) {
		return 0;
	}
	
	@Override
	public void flatWriteByte(IMemoryEntry entry, int addr, byte val) {
	}
	
	@Override
	public void flatWriteWord(IMemoryEntry entry, int addr, short val) {
	}
}