/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;
import v9t9.tools.asm.assembler.operand.ll.LLScaledRegOffsOperand;

/**
 * @author ejs
 *
 */
public class ScaledRegOffsOperand extends BaseOperand {

	private final AssemblerOperand offs;
	private final AssemblerOperand addedReg;
	private final AssemblerOperand scaledReg;
	private final AssemblerOperand scale;

	/**
	 * @param offs
	 * @param reg
	 * @param scaledReg
	 * @param scale multiplier
	 */
	public ScaledRegOffsOperand(AssemblerOperand offs, AssemblerOperand reg, AssemblerOperand scaledReg, AssemblerOperand scale) {
		this.offs = offs;
		this.addedReg = reg;
		this.scaledReg = scaledReg;
		this.scale = scale;
	}

	@Override
	public String toString() {
		return "@" + offs + "(" + (addedReg != null ? addedReg.toString() + "+" : "") + 
			scaledReg + (isScaled() ? "*" + scale.toString() : "") + ")"; 
	}
	

	/**
	 * @return
	 */
	public boolean isScaled() {
		return scale != null && (!(scale instanceof NumberOperand) ||
				((NumberOperand) scale).getValue() != 1);
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((addedReg == null) ? 0 : addedReg.hashCode());
		result = prime * result + ((offs == null) ? 0 : offs.hashCode());
		result = prime * result + ((scale == null) ? 0 : scale.hashCode());
		result = prime * result
				+ ((scaledReg == null) ? 0 : scaledReg.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScaledRegOffsOperand other = (ScaledRegOffsOperand) obj;
		if (addedReg == null) {
			if (other.addedReg != null)
				return false;
		} else if (!addedReg.equals(other.addedReg))
			return false;
		if (offs == null) {
			if (other.offs != null)
				return false;
		} else if (!offs.equals(other.offs))
			return false;
		if (scale == null) {
			if (other.scale != null)
				return false;
		} else if (!scale.equals(other.scale))
			return false;
		if (scaledReg == null) {
			if (other.scaledReg != null)
				return false;
		} else if (!scaledReg.equals(other.scaledReg))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
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
	
	@Override
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		LLOperand scaleRes = scale.resolve(assembler, inst);
		LLOperand offsRes = offs.resolve(assembler, inst);
		LLOperand addedRegRes = addedReg != null ? addedReg.resolve(assembler, inst) : null;
		LLOperand scaledRegRes = scaledReg.resolve(assembler, inst);
		if (offsRes instanceof LLForwardOperand || scaledRegRes instanceof LLForwardOperand
				|| addedRegRes instanceof LLForwardOperand
				|| scaleRes instanceof LLForwardOperand)
			return new LLForwardOperand(this, 2);
		if (addedRegRes != null && !(addedRegRes instanceof LLRegisterOperand))
			throw new ResolveException(addedRegRes, "Expected a register");
		if (!(scaledRegRes instanceof LLRegisterOperand))
			throw new ResolveException(scaledRegRes, "Expected a register");
		if (!(offsRes instanceof LLImmedOperand))
			throw new ResolveException(offsRes, "Expected an immediate");
		if (!(scaleRes instanceof LLImmedOperand))
			throw new ResolveException(scaleRes, "Expected an immediate");
		int scale = ((LLImmedOperand) scaleRes).getImmediate();
		if ((scale & (scale - 1)) != 0 || scale <= 0 || scale >= 256)
			throw new ResolveException(scaleRes, "Expected a scale a power of two in the range 1-128");
		return new LLScaledRegOffsOperand(this, 
				offsRes.getImmediate(), 
				addedRegRes != null ? ((LLRegisterOperand) addedRegRes).getRegister() : MachineOperandMFP201.SR,
				((LLRegisterOperand)scaledRegRes).getRegister(), 
				scale);
	}

	/**
	 * @return
	 */
	public AssemblerOperand getOffs() {
		return offs;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newScale = scale.replaceOperand(src, dst);
		AssemblerOperand newOffs = offs.replaceOperand(src, dst);
		AssemblerOperand newAddedReg = addedReg != null ? addedReg.replaceOperand(src, dst) : addedReg;
		AssemblerOperand newScaledReg = scaledReg.replaceOperand(src, dst);
		if (newScale != scale || newOffs != offs
				||newAddedReg != addedReg
				|| newScaledReg != scaledReg) {
			return new ScaledRegOffsOperand(newOffs, newAddedReg,
					newScaledReg, newScale);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[] { offs, scale };
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.RegisterOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return new ScaledRegOffsOperand(offs.addOffset(i), addedReg, scaledReg, scale);
	}
}
