/*
  ICruChip.java

  (c) 2005-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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

	/**
	 * @param level
	 */
	void acknowledgeInterrupt(int level);

	/**
	 * 
	 */
	void handlingInterrupt();
}
