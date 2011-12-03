/**
 * 
 */
package v9t9.engine.asm;

import v9t9.emulator.runtime.cpu.CpuState;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;

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

	IDecompileInfo createDecompileInfo(CpuState cpuState);
	
}
