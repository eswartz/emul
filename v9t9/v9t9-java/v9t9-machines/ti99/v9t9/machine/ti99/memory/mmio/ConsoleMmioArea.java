/*
  ConsoleMmioArea.java

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

import v9t9.engine.memory.WordMemoryArea;
import v9t9.engine.memory.ZeroWordMemoryArea;

public class ConsoleMmioArea extends WordMemoryArea {
    public ConsoleMmioArea() {
    	// the 16->8 bit multiplexer forces all accesses to be slow
    	this(4);
    	
    }
    public ConsoleMmioArea(int latency) {
    	super(latency);
        bWordAccess = true;
        memory = ZeroWordMemoryArea.zeroes;
    }
    
    @Override
    public boolean hasReadAccess() {
    	return false;
    }
}