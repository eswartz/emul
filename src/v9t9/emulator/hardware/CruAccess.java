/**
 * 
 */
package v9t9.emulator.hardware;

import v9t9.emulator.runtime.Cpu;

/**
 * This is the interface for CRU access from the CPU.
 * @author ejs
 *
 */
public interface CruAccess {
	/**
	 * Poll the CRU for interrupts, pins, etc. and set them on the CPU.
	 * @param cpu
	 */
	void pollForPins(Cpu cpu);
	
	/**
	 * Trigger an interrupt from hardware
	 */
	void triggerInterrupt(int level);
}
