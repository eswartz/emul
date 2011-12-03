/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import org.apache.bcel.generic.InstructionList;

import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.decomp.IDecompileInfo;

public interface InstructionRangeCompiler {
	void compileInstructionRange(CompilerBase compiler, RawInstruction[] insts,
			IDecompileInfo highLevel,  
			InstructionList ilist, CompileInfo info);
}