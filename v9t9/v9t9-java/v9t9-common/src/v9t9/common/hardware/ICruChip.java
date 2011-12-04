/**
 * 
 */
package v9t9.common.hardware;


import v9t9.base.properties.IPersistable;
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

	/**
	 * 
	 */
	void reset();
}
