/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.IOException;

import v9t9.emulator.runtime.Cpu;

/**
 * Java code that handles the work of a DSR through the Idsr instruction.
 * @author ejs
 *
 */
public interface DsrHandler {

	/** Get the CRU base */
	short getCruBase();

	/** Handle the DSR call
	 * @param cpu 
	 * 
	 * @param code the operand of the Idsr instruction
	 * @return true if handled the operand, false if the device doesn't match
	 */
	boolean handleDSR(Cpu cpu, short code);

	/** Activate the DSR (should be called when the ROM memory entry is mapped) 
	 * @throws IOException */
	void activate() throws IOException;
	/** Dectivate the DSR (should be called when the ROM memory entry is unmapped) */
	void deactivate();

	String getName();
}
