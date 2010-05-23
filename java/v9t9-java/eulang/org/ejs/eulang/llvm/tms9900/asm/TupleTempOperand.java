/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import java.util.Arrays;

import org.ejs.eulang.types.LLType;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * This operand represents the components making up a tuple
 * temporary.
 * 
 * When inserting values, we don't actually duplicate the entirety of
 * the possibly huge tuple.  Instead, we track individual temps
 * for every component of the tuple and keep track of the state of
 * temps for each temp.  E.g:
 * <pre>
 * %1 = insertvalue {%Int,%Int,%Int} undef, Int %0, 0
 * --> %1 = {%0, undef, undef}
 * %2 = insertvalue {%Int,%Int,%Int} %1, Int %0, 2
 * --> %2 = {%0, undef, %1}
 * </pre>
 * Since the temps are all SSA values, we won't lose track of 
 * the value of a tuple due to changes in its component values.
 * @author ejs
 *
 */
public class TupleTempOperand implements AsmOperand {

	private AssemblerOperand[] components;
	private final LLType type;

	public TupleTempOperand(LLType type, AssemblerOperand[] components) {
		this.type = type;
		this.components = components;
	}
	public TupleTempOperand(LLType type) {
		this(type, new AssemblerOperand[type.getCount()]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		boolean first = true;
		for (AssemblerOperand op : components) {
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append(op);
		}
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(components);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TupleTempOperand other = (TupleTempOperand) obj;
		if (!Arrays.equals(components, other.components))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}

	@Override
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		throw new ResolveException(inst, null, "Should not have this operand in assembler code!");
	}

	public AssemblerOperand get(int index) {
		return components[index];
	}
	public TupleTempOperand put(int index, AssemblerOperand op) {
		AssemblerOperand[] copy = Arrays.copyOf(components, components.length);
		copy[index] = op;
		return new TupleTempOperand(type, copy);
	}
	/**
	 * @return
	 */
	public AssemblerOperand[] getComponents() {
		return components;
	}
	


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand[] newComponents = null;
		for (int idx = 0; idx < components.length; idx++) {
			AssemblerOperand newComp = null;
			if (components[idx] != null)
				newComp = components[idx].replaceOperand(src, dst);
			if (newComp != components[idx]) {
				if (newComponents == null)
					newComponents = Arrays.copyOf(components, components.length);
				newComponents[idx] = newComp;
			}
		}
		if (newComponents != null)
			return new TupleTempOperand(type, newComponents);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return components;
	}
}
