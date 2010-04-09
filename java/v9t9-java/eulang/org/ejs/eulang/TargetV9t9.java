/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.llvm.LLFuncAttrs;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class TargetV9t9 implements ITarget {

	private TypeEngine typeEngine;
	
	private ISymbol intrinsic_IncRef;

	private ISymbol intrinsic_DecRef;

	//private ISymbol refType;

	/**
	 * 
	 */
	public TargetV9t9(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#createTypeEngine()
	 */
	@Override
	public TypeEngine getTypeEngine() {
		return typeEngine;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getTriple()
	 */
	@Override
	public String getTriple() {
		return "9900-unknown-v9t9";
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getLLCallingConvention()
	 */
	@Override
	public String getLLCallingConvention() {
		return "cc100";
	}

	/**
	 * Increment a reference to a ref-counted object with the given id (may be 0)
	 * @param value
	 */
	public void incRef(ILLCodeTarget target, LLType valueType, LLOperand value) {
		if (intrinsic_IncRef == null) {
			//refType = target.getModule().addExternType(typeEngine.REFPTR);
			LLCodeType codeType = typeEngine.getCodeType(typeEngine.VOID, new LLType[] { typeEngine.REFPTR });
			intrinsic_IncRef = target.getModule().addExtern("IncRef",
					codeType,
					null, LLVisibility.DEFAULT, null /*cconv*/,
					new LLAttrType(null, codeType.getRetType()),
					new LLAttrType[] { new LLAttrType(null, codeType.getArgTypes()[0]) },
					new LLFuncAttrs(), 
					null /*gc*/);
		}
		
		LLOperand temp = target.newTemp(typeEngine.REFPTR);
		target.emit(new LLCastInstr(temp, ECast.BITCAST, valueType, value, typeEngine.REFPTR));
		target.emit(new LLCallInstr(null, typeEngine.VOID, new LLSymbolOp(intrinsic_IncRef), 
				(LLCodeType) intrinsic_IncRef.getType(), temp));
	}

	/**
	 * Decrement a reference to a ref-counted object with the given id (may be  0)
	 * @param value
	 */
	public void decRef(ILLCodeTarget target, LLType valueType, LLOperand value) {
		if (intrinsic_DecRef == null) {
			//refType = target.getModule().addExternType(typeEngine.REFPTR);
			LLCodeType codeType = typeEngine.getCodeType(typeEngine.VOID, new LLType[] { typeEngine.REFPTR });
			intrinsic_DecRef = target.getModule().addExtern("DecRef",
					codeType,
					null, LLVisibility.DEFAULT, null /*cconv*/,
					new LLAttrType(null, codeType.getRetType()),
					new LLAttrType[] { new LLAttrType(null, codeType.getArgTypes()[0]) },
					new LLFuncAttrs(), 
					null /*gc*/);
		}
		
		LLOperand temp = target.newTemp(typeEngine.REFPTR);
		target.emit(new LLCastInstr(temp, ECast.BITCAST, valueType, value, typeEngine.REFPTR));
		target.emit(new LLCallInstr(null, typeEngine.VOID, new LLSymbolOp(intrinsic_DecRef), 
				(LLCodeType) intrinsic_DecRef.getType(), temp));
	}
}
