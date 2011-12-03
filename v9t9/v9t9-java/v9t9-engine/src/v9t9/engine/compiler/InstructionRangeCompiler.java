/**
 * 
 */
package v9t9.engine.compiler;

import org.apache.bcel.generic.InstructionList;

import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.RawInstruction;

public interface InstructionRangeCompiler {
	void compileInstructionRange(CompilerBase compiler, RawInstruction[] insts,
			IDecompileInfo highLevel,  
			InstructionList ilist, CompileInfo info);
}