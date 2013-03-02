/*
  AddrOperand.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.ll.LLAddrOperand;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;

/**
 * @author ejs
 *
 */
public class AddrOperand extends BaseOperand {

	private final AssemblerOperand addr;

	public AddrOperand(AssemblerOperand addr) {
		this.addr = addr;
	}

	@Override
	public String toString() {
		return "@" + getAddr();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAddr() == null) ? 0 : getAddr().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AddrOperand other = (AddrOperand) obj;
		if (getAddr() == null) {
			if (other.getAddr() != null) {
				return false;
			}
		} else if (!getAddr().equals(other.getAddr())) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isMemory() {
		return true;
	}
	@Override
	public boolean isRegister() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}
	

	public LLOperand resolve(IAssembler assembler, IInstruction inst)
			throws ResolveException {
		LLOperand lop = getAddr().resolve(assembler, inst);
		if (lop instanceof LLForwardOperand)
			return new LLForwardOperand(this, 2);
		
		if (lop instanceof LLPCRelativeOperand) {
			// XXX
			//lop = new LLAddrOperand(this, inst.getPc() + ((LLPCRelativeOperand)lop).getOffset());
			return lop;
		} else if (lop instanceof LLImmedOperand) {
			lop = new LLAddrOperand(this, lop.getImmediate());
		} else
			throw new ResolveException(lop, "Expected an immediate");
		return lop;
	}
	
	public AssemblerOperand getAddr() {
		return addr;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newAddr = addr.replaceOperand(src, dst);
		if (newAddr != addr) {
			return new AddrOperand(newAddr);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[] { addr };
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		if (addr instanceof BinaryOperand && ((BinaryOperand) addr).getRight() instanceof NumberOperand
				&& ((BinaryOperand) addr).getKind() == '+') {
			AssemblerOperand offs = (NumberOperand) ((BinaryOperand) addr).getRight();
			offs = offs.addOffset(i); 
			return new AddrOperand(new BinaryOperand('+',  ((BinaryOperand) addr).getLeft(), offs)); 
		}
		return new AddrOperand(new BinaryOperand('+', addr, new NumberOperand(i)));
	}
}
