/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.HashSet;

import org.ejs.eulang.ICallingConvention;
import org.ejs.eulang.IRegClass;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class RegAlloc {

	private final IRegClass regClass;
	/** in-use physical registers */
	private HashSet<Integer> physRegs;
	/** never-used physical registers */
	private HashSet<Integer> fixedRegs;
	private int vrNum;
	private int regSize;
	private final IScope localScope;
	private final ITarget target;
	private final ICallingConvention cc;

	/**
	 * @param regClass
	 */
	public RegAlloc(ITarget target, ICallingConvention cc, IRegClass regClass, IScope localScope) {
		this.target = target;
		this.cc = cc;
		this.regClass = regClass;
		this.localScope = localScope;
		this.fixedRegs = new HashSet<Integer>();
		
		for (int reg : cc.getFixedRegisters(regClass)) {
			fixedRegs.add(reg);
		}
		this.physRegs = new HashSet<Integer>(fixedRegs);
		
		this.regSize = regClass.getRegisterSize();
		this.vrNum = regClass.getRegisterCount();
	}

	/**
	 * @param symbol symbol needing storage
	 * @param type type to allocate (may differ; e.g. the symbol may be a pointer to stack while we want the content allocated)
	 * @return
	 */
	public RegisterLocal allocate(ISymbol symbol, LLType type) throws UnsupportedOperationException {
		BasicType basic = type.getBasicType();
		if (basic == BasicType.POINTER || basic == BasicType.BOOL)
			basic = BasicType.INTEGRAL;
		if (basic != regClass.getBasicType())
			throw new UnsupportedOperationException();
		
		// allow two regs per var
		int vr = vrNum;
		if (type.getBits() > regSize * 2) {
			throw new UnsupportedOperationException();
		}
		if (type.getBits() > regSize) {
			if (type.getBits() != regSize * 2) { 
				throw new UnsupportedOperationException();
			}
			vrNum += 2;
		} else if (type.getBits() > 0) {
			vrNum++;
		} else {
			throw new UnsupportedOperationException();
		}
		RegisterLocal local = new RegisterLocal(regClass, symbol, 
				type,
				vr);
		return local;
	}

	public void allocateRegister(int number) {
		assert !physRegs.contains(number);
		physRegs.add(number);
	}

}
