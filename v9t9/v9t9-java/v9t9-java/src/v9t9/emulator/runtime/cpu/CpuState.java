/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import v9t9.engine.cpu.Status;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public interface CpuState {

	short getPC();

	void setPC(short pc);

	short getST();

	void setST(short st);

	int getRegister(int reg);

	void setRegister(int reg, int val);
	
	int getRegisterCount();
	String getRegisterName(int reg);
	String getRegisterTooltip(int reg);

	MemoryDomain getConsole();

	Status createStatus();

	Status getStatus();
	void setStatus(Status status);

}