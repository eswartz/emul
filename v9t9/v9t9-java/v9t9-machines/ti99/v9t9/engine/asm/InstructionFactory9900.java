/**
 * 
 */
package v9t9.engine.asm;

import v9t9.emulator.runtime.cpu.CpuState;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.asm.IDecompileInfo;
import v9t9.engine.asm.IInstructionFactory;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.Instruction9900;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public class InstructionFactory9900 implements IInstructionFactory {

	public static final IInstructionFactory INSTANCE = new InstructionFactory9900();

	/**
	 * 
	 */
	public InstructionFactory9900() {
		super();
	}

	@Override
	public byte[] encodeInstruction(RawInstruction instruction) {
		short[] words = InstTable9900.encode(instruction);
		byte[] bytes = new byte[words.length * 2];
		for (int idx = 0; idx < words.length; idx++) {
			bytes[idx*2] = (byte) (words[idx] >> 8);
			bytes[idx*2+1] = (byte) (words[idx] & 0xff);
		}
		return bytes;
	}

	@Override
	public RawInstruction decodeInstruction(int pc, MemoryDomain domain) {
		return InstTable9900.decodeInstruction(domain.flatReadWord(pc), pc, domain);
	}

	public boolean isByteInst(int inst) {
		return InstTable9900.isByteInst(inst);
	}

	@Override
	public boolean isJumpInst(int inst) {
		return InstTable9900.isJumpInst(inst);
	}

	@Override
	public String getInstName(int inst) {
		return InstTable9900.getInstName(inst);
	}

	@Override
	public int getInstructionFlags(RawInstruction inst) {
		return Instruction9900.getInstructionFlags(inst);
	}

	@Override
	public IDecompileInfo createDecompileInfo(CpuState cpuState) {
		return new HighLevelCodeInfo(cpuState, this);
	}

}