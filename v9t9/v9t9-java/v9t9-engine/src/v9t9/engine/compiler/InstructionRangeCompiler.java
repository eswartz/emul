/*
  InstructionRangeCompiler.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.compiler;

import org.apache.bcel.generic.InstructionList;

import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.RawInstruction;
import v9t9.common.compiler.ICompiler;

public interface InstructionRangeCompiler {
	void compileInstructionRange(ICompiler compiler, RawInstruction[] insts,
			IDecompileInfo highLevel,  
			InstructionList ilist, CompileInfo info);
}