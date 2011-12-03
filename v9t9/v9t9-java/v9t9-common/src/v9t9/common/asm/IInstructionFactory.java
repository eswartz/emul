/**
 * 
 */
package v9t9.common.asm;

import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.MemoryDomain;

/**
 * @author Ed
 *
 */
public interface IInstructionFactory {

	boolean isByteInst(int inst);

	boolean isJumpInst(int inst);

	String getInstName(int inst);

	
	RawInstruction decodeInstruction(int pc, MemoryDomain domain);
	byte[] encodeInstruction(RawInstruction instruction);

	int getInstructionFlags(RawInstruction inst);

	IDecompileInfo createDecompileInfo(ICpuState cpuState);
	
}
