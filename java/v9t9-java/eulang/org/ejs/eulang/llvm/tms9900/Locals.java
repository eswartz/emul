/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ejs.eulang.ITarget;
import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.*;
import org.ejs.eulang.llvm.ops.*;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLBoolType;
import org.ejs.eulang.types.LLType;

/**
 * Manage locals for a routine.
 * @author ejs
 *
 */
public class Locals {

	private final ITarget target;
	private Map<ISymbol, ILocal> locals;
	protected LLBlock currentBlock;
	private int frameSize;
	
	public Locals(ITarget target) {
		this.target = target;
		locals = new LinkedHashMap<ISymbol, ILocal>();
	}
	
	public Map<ISymbol, ILocal> getLocals() {
		return locals;
	}
	public void buildLocalTable(LLDefineDirective def) {
		ILLCodeVisitor visitor = new LLCodeVisitor() {
			@Override
			public boolean enterBlock(LLBlock block) {
				currentBlock = block;
				System.out.println("Block " + currentBlock.getLabel());
				return true;
			}
			public boolean enterInstr(LLInstr instr) {
				if (instr instanceof LLAllocaInstr) {
					allocate((LLAllocaInstr) instr);
				}
				return false;
			}
		};
		def.accept(visitor);
	}

	protected void allocate(LLAllocaInstr instr) {
		assert instr.getResult() instanceof LLSymbolOp;
		
		ISymbol name = ((LLSymbolOp) instr.getResult()).getSymbol();
		LLType type = instr.getType();
		if (type.getBits() % 8 != 0)
			unhandled(instr.getResult());
		int size = type.getBits() / 8;
		
		ILocal local = new StackLocal(name, type, size, currentBlock.getLabel(), -frameSize);
		frameSize += size;
		
		assert !locals.containsKey(name);
		
		locals.put(name, local);
		System.out.println("Allocated " + local);
	}

	private void unhandled(LLOperand op) {
		throw new IllegalStateException(op.toString());
	}

	/**
	 * @return
	 */
	public int getFrameSize() {
		return frameSize;
	}


}
