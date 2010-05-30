/**
 * 
 */
package org.ejs.eulang;

import java.util.HashMap;
import org.ejs.eulang.llvm.FunctionConvention;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.LLArgAttrType;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.llvm.LLFuncAttrs;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLBoolType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLFloatType;
import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLLabelType;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLVoidType;

/**
 * @author ejs
 *
 */
public class TargetV9t9 implements ITarget {

	/**
	 * @author ejs
	 *
	 */
	private static final class IntRegClass implements IRegClass {
		@Override
		public int hashCode() {
			int result = 1;
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
			return true;
		}

		@Override
		public BasicType getBasicType() {
			return BasicType.INTEGRAL;
		}

		@Override
		public int getRegisterCount() {
			return 16;
		}

		@Override
		public int getRegisterSize() {
			return 16;
		}

		@Override
		public boolean supportsType(LLType type) {
			BasicType basic = type.getBasicType();
			if (basic == BasicType.POINTER || basic == BasicType.BOOL)
				basic = BasicType.INTEGRAL;
			
			return basic == getBasicType();
		}

		@Override
		public int getByteSize(LLType type) {
			return type.getBits() <= 8 ? 1 : 2;
		}
	}

	private TypeEngine typeEngine;
	
	private ISymbol intrinsic_IncRef;

	private ISymbol intrinsic_DecRef;

	private IRegClass intRegClass;

	private HashMap<Intrinsic, ISymbol> intrinsicMap;

	//private ISymbol refType;

	/**
	 * 
	 */
	public TargetV9t9() {
		this.typeEngine = new TypeEngine();
		
		initTypes();
		
		intRegClass = new IntRegClass();
		
		intrinsicMap = new HashMap<Intrinsic, ISymbol>();
	}
	
	/**
	 * 
	 */
	private void initTypes() {
		typeEngine.setLittleEndian(false);
		typeEngine.setPtrBits(16);
		typeEngine.setPtrAlign(16);
		typeEngine.setStackMinAlign(8);
		typeEngine.setStackAlign(16);
		typeEngine.setStructMinAlign(8);
		typeEngine.setStructAlign(16);
		
		typeEngine.VOID = typeEngine.register(new LLVoidType(null));
		typeEngine.NIL = typeEngine.register(new LLVoidType(null));
		typeEngine.LABEL = typeEngine.register(new LLLabelType());
		typeEngine.BOOL = typeEngine.register(new LLBoolType("Bool", 1));
		typeEngine.LLBOOL = typeEngine.register(new LLBoolType(null, 1));
		
		typeEngine.BYTE = typeEngine.register(typeEngine.getIntType("Byte", 8));
		typeEngine.INT = typeEngine.register(typeEngine.getIntType("Int", 16));
		
		typeEngine.FLOAT = typeEngine.register(new LLFloatType("Float", 32, 23));
		typeEngine.DOUBLE = typeEngine.register(new LLFloatType("Double", 64, 53));
		typeEngine.REFPTR = typeEngine.register(new LLPointerType("RefPtr", typeEngine.getPtrBits(), 
				typeEngine.getRefType(typeEngine.BYTE)));
		
		typeEngine.INT_ANY = new LLIntType("Int*", 0);		
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
					new LLArgAttrType[] { new LLArgAttrType("ref", null, codeType.getArgTypes()[0]) },
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
					new LLArgAttrType[] { new LLArgAttrType("ref", null, codeType.getArgTypes()[0]) },
					new LLFuncAttrs(), 
					null /*gc*/);
		}
		
		LLOperand temp = target.newTemp(typeEngine.REFPTR);
		target.emit(new LLCastInstr(temp, ECast.BITCAST, valueType, value, typeEngine.REFPTR));
		target.emit(new LLCallInstr(null, typeEngine.VOID, new LLSymbolOp(intrinsic_DecRef), 
				(LLCodeType) intrinsic_DecRef.getType(), temp));
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getRegisterClasses()
	 */
	@Override
	public IRegClass[] getRegisterClasses() {
		return new IRegClass[] { 
			intRegClass	
		};
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getCallingConvention(org.ejs.eulang.llvm.FunctionConvention)
	 */
	@Override
	public ICallingConvention getCallingConvention(FunctionConvention convention) {
		return new V9t9CallingConvention(this, convention);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getIntrinsic(org.ejs.eulang.ITarget.Intrinsic)
	 */
	@Override
	public ISymbol getIntrinsic(ILLCodeTarget target, Intrinsic intrinsic, LLType type) {
		ISymbol sym = intrinsicMap.get(intrinsic);
		if (sym == null) {
			switch (intrinsic) {
			case DECREF:
			{
				LLCodeType codeType = typeEngine.getCodeType(typeEngine.VOID, new LLType[] { typeEngine.REFPTR });
				sym = target.getModule().addExtern("DecRef",
						codeType,
						null, LLVisibility.DEFAULT, null /*cconv*/,
						new LLAttrType(null, codeType.getRetType()),
						new LLArgAttrType[] { new LLArgAttrType("ref", null, codeType.getArgTypes()[0]) },
						new LLFuncAttrs(), 
						null /*gc*/);
			}
			break;
			case INCREF:
			{
				LLCodeType codeType = typeEngine.getCodeType(typeEngine.VOID, new LLType[] { typeEngine.REFPTR });
				sym = target.getModule().addExtern("DecRef",
						codeType,
						null, LLVisibility.DEFAULT, null /*cconv*/,
						new LLAttrType(null, codeType.getRetType()),
						new LLArgAttrType[] { new LLArgAttrType("ref", null, codeType.getArgTypes()[0]) },
						new LLFuncAttrs(), 
						null /*gc*/);
			}
			break;
			case SHIFT_RIGHT_CIRCULAR:
			case SHIFT_LEFT_CIRCULAR:
			{
				LLCodeType codeType;
				if (type.getBits() == 16)
					codeType = typeEngine.getCodeType(typeEngine.INT, 
							new LLType[] { typeEngine.INT, typeEngine.INT });
				else if (type.getBits() <= 8)
					codeType = typeEngine.getCodeType(typeEngine.BYTE, 
							new LLType[] { typeEngine.BYTE, typeEngine.INT});
				else
					return null;
				sym = target.getModule().addExtern(intrinsic == Intrinsic.SHIFT_RIGHT_CIRCULAR 
						? "intrinsic.src" : "intrinsic.slc",
						codeType,
						null, LLVisibility.DEFAULT, null /*cconv*/,
						new LLAttrType(null, codeType.getRetType()),
						new LLArgAttrType[] { 
							new LLArgAttrType("src", null, codeType.getArgTypes()[0]), 
							new LLArgAttrType("cnt", null, codeType.getArgTypes()[1]) 
						},
						new LLFuncAttrs(), 
						null /*gc*/);
				break;
			}
			case SIGNED_DIVISION:
			case SIGNED_REMAINDER:
			case MODULO:
			{
				LLCodeType codeType;
				if (type.getBits() == 16)
					codeType = typeEngine.getCodeType(typeEngine.INT, 
							new LLType[] { typeEngine.INT, typeEngine.INT });
				else if (type.getBits() <= 8)
					codeType = typeEngine.getCodeType(typeEngine.BYTE, 
							new LLType[] { typeEngine.BYTE, typeEngine.BYTE});
				else
					return null;
				sym = target.getModule().addExtern(intrinsic == Intrinsic.SIGNED_DIVISION 
						? "intrinsic.sdiv" : intrinsic == Intrinsic.SIGNED_REMAINDER ? "intrinsic.srem" : "intrinsic.modulo",
								codeType,
								null, LLVisibility.DEFAULT, null /*cconv*/,
								new LLAttrType(null, codeType.getRetType()),
								new LLArgAttrType[] { 
					new LLArgAttrType("dividend", null, codeType.getArgTypes()[0]), 
					new LLArgAttrType("divisor", null, codeType.getArgTypes()[1]) 
				},
				new LLFuncAttrs(), 
				null /*gc*/);
				break;
			}
			default:
				assert false;
			}
			intrinsicMap.put(intrinsic, sym);
		}

		return sym;

	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getSP()
	 */
	@Override
	public int getSP() {
		return 10;
	}


	/**
	 * Get a symbol representing the status register
	 * @return the statusRegister
	 */
	public ISymbol getStatusRegister(IScope scope) {
		ISymbol statusRegister = scope.get(".status");
		if (statusRegister == null) {
			statusRegister = scope.add(".status", false);
			statusRegister.setType(new LLVoidType(""));
		}
		return statusRegister;
	}

}
