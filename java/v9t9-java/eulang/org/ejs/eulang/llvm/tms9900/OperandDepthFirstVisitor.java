/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.TypeEngine.Alignment;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * Subclass to handle each non-aggregate operand in an operand tree.
 * @author ejs
 *
 */
public abstract class OperandDepthFirstVisitor  {

	protected final TypeEngine typeEngine;
	private final Target target;

	public OperandDepthFirstVisitor(TypeEngine typeEngine, Target target) {
		this.typeEngine = typeEngine;
		this.target = target;
	}
	
	protected abstract void handleOperand(AssemblerOperand operand, LLType type, int byteOffset);
	
	public int accept(AssemblerOperand op, LLType type, int offset) {
		return accept(op, type, typeEngine.new Alignment(target), offset);
	}
	protected int accept(AssemblerOperand op, LLType type, Alignment align, int baseOffset) {
		if (op instanceof TupleTempOperand) {
			AssemblerOperand[] components = ((TupleTempOperand) op).getComponents();
			
			Alignment subAlign = typeEngine.new Alignment(target);
			for (int i = 0; i < components.length; i++) {
				LLType subType = type instanceof LLAggregateType ? ((LLAggregateType) type).getType(i) : type.getSubType();
				AssemblerOperand subOp = components[i];
				
				int offset = baseOffset + (subAlign.sizeof() + subAlign.alignmentGap(subType)) / 8;
				accept(subOp, subType, subAlign, offset);
			}
		} else {
			handleOperand(op, type, baseOffset);
		}
		
		align.alignAndAdd(type);
		
		return baseOffset + align.sizeof() / 8;
	}
}
