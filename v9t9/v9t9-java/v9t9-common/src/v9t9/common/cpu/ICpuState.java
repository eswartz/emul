/**
 * 
 */
package v9t9.common.cpu;

import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author Ed
 *
 */
public interface ICpuState extends IRegisterAccess {

	short getPC();

	void setPC(short pc);

	short getST();

	void setST(short st);

	IMemoryDomain getConsole();

	IStatus createStatus();

	IStatus getStatus();
	void setStatus(IStatus status);

}