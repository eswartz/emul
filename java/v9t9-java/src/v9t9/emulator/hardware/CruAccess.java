/**
 * 
 */
package v9t9.emulator.hardware;

import org.ejs.coffee.core.properties.IPersistable;

import v9t9.emulator.runtime.Cpu;

/**
 * This is the interface for CRU access from the CPU.
 * @author ejs
 *
 */
public interface CruAccess extends IPersistable {
	/**
	 * Poll the CRU for interrupts, pins, etc. which influence the CPU,
	 * and set any pins on the CPU.
	 * @param cpu
	 */
	void pollForPins(Cpu cpu);
	
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
}
