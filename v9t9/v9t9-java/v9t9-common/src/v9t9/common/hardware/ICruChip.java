/*
  ICruChip.java

  (c) 2005-2012 Edward Swartz

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
package v9t9.common.hardware;


import ejs.base.properties.IPersistable;
import v9t9.common.cpu.ICpu;

/**
 * This is the interface for CRU access from the CPU.
 * @author ejs
 *
 */
public interface ICruChip extends IPersistable {
	/**
	 * Poll the CRU for interrupts, pins, etc. which influence the CPU,
	 * and set any pins on the CPU.
	 * @param cpu
	 */
	void pollForPins(ICpu cpu);
	
	/**
	 * Get the active interrupt level
	 * @return
	 */
	byte getInterruptLevel();
	
	/**
	 * Trigger an interrupt from hardware
	 */
	void triggerInterrupt(int level);

	boolean isInterruptWaiting();
	
	void handledInterrupt();

	/**
	 * 
	 */
	void reset();
}
