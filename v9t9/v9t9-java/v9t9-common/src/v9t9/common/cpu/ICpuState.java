/**
 * 
 */
package v9t9.common.cpu;

import v9t9.common.memory.IMemoryDomain;

/**
 * @author Ed
 *
 */
public interface ICpuState {

	short getPC();

	void setPC(short pc);

	short getST();

	void setST(short st);

	int getRegister(int reg);

	void setRegister(int reg, int val);
	
	int getRegisterCount();
	String getRegisterName(int reg);
	String getRegisterTooltip(int reg);

	IMemoryDomain getConsole();

	IStatus createStatus();

	IStatus getStatus();
	void setStatus(IStatus status);

}