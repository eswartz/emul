/**
 * 
 */
package org.ejs.eulang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.impl.AstBoolLitExpr;
import org.ejs.eulang.ast.impl.AstFloatLitExpr;
import org.ejs.eulang.ast.impl.AstIntLitExpr;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLBoolType;
import org.ejs.eulang.types.LLCharType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLFloatType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLInstanceType;
import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLLabelType;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLRefType;
import org.ejs.eulang.types.LLStaticField;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLTupleType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLVoidType;

/**
 * @author ejs
 *
 */
public class TypeEngine {
	
	public LLType UNSPECIFIED = null;
	private int ptrBits;
	public LLIntType INT;
	public LLIntType BYTE;
	public LLFloatType FLOAT;
	public LLFloatType DOUBLE;
	public LLCharType CHAR;
	public LLDataType STR;

	//public LLIntType INT_ANY;
	public LLBoolType BOOL;
	public LLVoidType VOID;
	public LLLabelType LABEL;
	public LLType NIL;

	public LLType INTPTR;
	public LLType REFPTR;
	public LLIntType PTRDIFF;
	
	/** llvm bool type */
	public LLBoolType LLBOOL;

	private Map<ISymbol, LLType> llvmNameToTypeMap = new HashMap<ISymbol, LLType>();
	
	private Map<String, LLCodeType> codeTypes = new HashMap<String, LLCodeType>();
	private Set<LLType> types = new HashSet<LLType>();
	private Map<String, LLPointerType> ptrTypeMap = new HashMap<String, LLPointerType>();
	private Map<String, LLRefType> refTypeMap = new HashMap<String, LLRefType>();
	private Map<Tuple, LLArrayType> arrayTypeMap = new HashMap<Tuple, LLArrayType>();
	private Map<Integer, LLIntType> intMap = new HashMap<Integer, LLIntType>();
	private Map<String, LLTupleType> tupleTypeMap = new HashMap<String, LLTupleType>();
	private Map<String, LLDataType> dataTypeMap = new HashMap<String, LLDataType>();
	private Map<Integer, LLDataType> stringLitTypeMap = new TreeMap<Integer, LLDataType>();
	private Map<LLInstanceType, LLType> instanceToRealTypeMap = new HashMap<LLInstanceType, LLType>();
	
	private boolean isLittleEndian;
	private int ptrAlign;
	private int stackMinAlign;
	
	public enum Target {
		STACK,
		STRUCT
	};
	public class Alignment {
		private int offset;
		private final Target target;
		public Alignment(Target target) {
			this.target = target;
			offset = 0;
		}
		
		/**
		 * Get the size of a gap from the current offset to the position of the given type's field
		 * @param type
		 * @return bit offset
		 */
		public int alignmentGap(LLType type) {
			if (type instanceof LLSymbolType) {
				type = ((LLSymbolType) type).getRealType(TypeEngine.this);
			}
			while (type != null && type instanceof LLArrayType) {
				type = type.getSubType();
			}
			
			while (type != null && type instanceof LLAggregateType) {
				if (((LLAggregateType) type).getCount() > 0)
					type = ((LLAggregateType) type).getType(0);
				else
					break;
			}
			
			int bits = type != null ? type.getBits() : 0;
			
			int minAlign = target == Target.STRUCT ? getStructMinAlign() : getStackMinAlign();
			int align = target == Target.STRUCT ? getStructAlign() : getStackAlign();
			
			if (bits != 0 && bits < minAlign)
				bits = minAlign;
			if (bits < align)
				align = minAlign;
			
			int fieldOffset = offset;
			if (fieldOffset % align != 0) {
				fieldOffset += (align - fieldOffset % align);
			}

			return fieldOffset - offset;
		}
		
		/** 
		 * Add a type to align.  
		 * 
		 * @param type
		 * @return the bit offset of the type
		 */
		public int addAtOffset(LLType type) {
			if (type instanceof LLSymbolType) {
				type = ((LLSymbolType) type).getRealType(TypeEngine.this);
			}
			
			LLType alignType = type;
			while (alignType != null && alignType instanceof LLArrayType) {
				alignType = alignType.getSubType();
			}
			
			
			int bits = type != null ? type.getBits() : 0;
			int alignBits = alignType != null ? alignType.getBits() : 0;
			
			int minAlign = target == Target.STRUCT ? getStructMinAlign() : getStackMinAlign();
			int align = target == Target.STRUCT ? getStructAlign() : getStackAlign();
			
			if (bits != 0 && bits < minAlign)
				bits = minAlign;
			if (alignBits < align)
				align = minAlign;
			
			int fieldOffset = offset;
			int nextOffset = fieldOffset;

			nextOffset += bits; 
			if (nextOffset % align != 0)
				nextOffset += (align - offset % align);
			offset = nextOffset;
			
			return fieldOffset;
		}
		
		/** 
		 * Add a type to align.  
		 * 
		 * @param type
		 * @return the bit offset of the type
		 */
		public int alignAndAdd(LLType type) {
			add(alignmentGap(type));
			return addAtOffset(type);
		}
		/**
		 * @return the bit size after final alignment 
		 */
		public int sizeof() {
			return offset;
		}

		/**
		 * @param gap
		 */
		public void add(int gap) {
			offset += gap;
		}

		/**
		 * @param type
		 * @return
		 */
		public int alignedSize(LLType type) {
			if (type instanceof LLSymbolType) {
				type = ((LLSymbolType) type).getRealType(TypeEngine.this);
			}
			LLType alignType = type;
			while (alignType != null && alignType instanceof LLArrayType) {
				alignType = alignType.getSubType();
			}
			
			int bits = type != null ? type.getBits() : 0;
			int alignBits = alignType != null ? alignType.getBits() : 0;
			
			int minAlign = target == Target.STRUCT ? getStructMinAlign() : getStackMinAlign();
			int align = target == Target.STRUCT ? getStructAlign() : getStackAlign();
			
			if (bits != 0 && bits < minAlign)
				bits = minAlign;
			if (alignBits < align)
				align = minAlign;
			
			int fieldOffset = offset;
			int nextOffset = fieldOffset;

			nextOffset += bits; 
			if (nextOffset % align != 0)
				nextOffset += (align - offset % align);
			
			return nextOffset - fieldOffset;
		}
	}
	
	public int getStackMinAlign() {
		return stackMinAlign;
	}

	public void setStackMinAlign(int stackMinAlign) {
		this.stackMinAlign = stackMinAlign;
	}

	private int stackAlign;
	private int structAlign;
	private int structMinAlign;
	
	/**
	 * 
	 */
	public TypeEngine() {
	}
	
	/**
	 * Add names for globally accessible types
	 * @param globalScope
	 */
	public void populateTypes(GlobalScope globalScope) {
		VOID = register(new LLVoidType(null));
		NIL = register(new LLVoidType(null));
		LABEL = register(new LLLabelType());

		REFPTR = register(new LLPointerType("RefPtr", getPtrBits(), 
				getRefType(BYTE)));
		
		STR = register(getStringLiteralType("", globalScope.add("Str", false)));
		
		//INT_ANY = new LLIntType("Int*", 0);
		
		populateType(globalScope, "Int", INT);
		populateType(globalScope, "Float", FLOAT);		
		populateType(globalScope, "Double", DOUBLE);		
		populateType(globalScope, "Void", VOID);		
		populateType(globalScope, "Bool", BOOL);		
		populateType(globalScope, "Byte", BYTE);		
		populateType(globalScope, "Char", CHAR);		
		
	}

	/**
	 * @param globalScope 
	 * @param name
	 * @param iNT2
	 */
	private void populateType(GlobalScope globalScope, String name, LLType type) {
		ISymbol symbol = globalScope.add(name, false);
		symbol.setDefinition(new AstType(type));
		
	}

	public <T extends LLType> T register(T type) {
		types.add(type);
		return type;
	}

	/**
	 * @return the isLittleEndian
	 */
	public boolean isLittleEndian() {
		return isLittleEndian;
	}
	
	/**
	 * Get the basic type, ignoring ptrs and refs
	 */
	public LLType getBaseType(LLType a) {
		while (a != null && (a.getBasicType() == BasicType.REF || a.getBasicType() == BasicType.POINTER))
			a = a.getSubType();
		return a;
	}
	/**
	 * Get the type to which a and b should be promoted 
	 * @param a
	 * @param b
	 * @return one of the types, or <code>null</code>
	 */
	public LLType getPromotionType(LLType a, LLType b) {
		//a = getBaseType(a);
		//b = getBaseType(b);
		
		if (a == null || b == null)
			return null;

		if (a.equals(b))
			return a;

		if (a.getBasicType() == BasicType.INTEGRAL && b.getBasicType() == BasicType.INTEGRAL)
			return a.getBits() > b.getBits() ? a : b;
		if (a.getBasicType() == BasicType.FLOATING && b.getBasicType() == BasicType.FLOATING)
			return a.getBits() > b.getBits() ? a : b;
			
		if (a.getBasicType() == BasicType.BOOL && b.getBasicType() == BasicType.INTEGRAL)
			return b;
		if (b.getBasicType() == BasicType.BOOL && a.getBasicType() == BasicType.INTEGRAL)
			return a;
			
		if ((a.getBasicType() == BasicType.INTEGRAL || a.getBasicType() == BasicType.BOOL) 
				&& b.getBasicType() == BasicType.FLOATING) {
			return b;
		}
		if ((b.getBasicType() == BasicType.INTEGRAL || b.getBasicType() == BasicType.BOOL)
				&& a.getBasicType() == BasicType.FLOATING) {
			return a;
		}
		
		if (a.getBasicType() != BasicType.VOID && b.getBasicType() == BasicType.VOID)
			return a;
		if (b.getBasicType() != BasicType.VOID && a.getBasicType() == BasicType.VOID)
			return b;
		if (a.getBasicType() != BasicType.VOID && b.getBasicType() == BasicType.VOID)
			return null;
		
		
		if (a.getBasicType() == BasicType.TUPLE && b.getBasicType() == BasicType.TUPLE) {
			if (!a.isCompatibleWith(b))
				return null;
			int subCount = ((LLAggregateType) a).getCount();
			LLType[] common = new LLType[subCount];
			for (int i = 0; i < subCount; i++) {
				common[i] = getPromotionType(((LLAggregateType) a).getType(i), ((LLAggregateType) b).getType(i));
			}
			return getTupleType(common);
		}

		if (a.getBasicType() == BasicType.ARRAY && b.getBasicType() == BasicType.ARRAY) {
			if (a.getSubType() == null && b.getSubType() == null)
				return a;
			if (a.getSubType() == null)
				return b;
			if (b.getSubType() == null)
				return a;
			if (a.getSubType().equals(b.getSubType()))
				return a;
			return null;
		}

		if (a.getBasicType() == BasicType.POINTER && b.getBasicType() == BasicType.INTEGRAL)
			return a;

		if (a.getBasicType() == BasicType.GENERIC || b.getBasicType() == BasicType.GENERIC)
			return null;
		/*
		if (a.getBasicType() == BasicType.GENERIC && b.getBasicType() != BasicType.GENERIC) {
			return b;
		}
		if (b.getBasicType() == BasicType.GENERIC && a.getBasicType() != BasicType.GENERIC) {
			return a;
		}
		if (a.getBasicType() == BasicType.GENERIC && b.getBasicType() == BasicType.GENERIC) {
			return a.getName().compareTo(b.getName()) <= 0 ? a : b;
		}
		*/
		
		// ptrs, refs, voids cannot be interconverted
		
		return null;
	}

	/**
	 * Get or create a type for code using the given return type and arguments
	 * @param retandArgTypes
	 * @return LLType
	 */
	public LLCodeType getCodeType(LLType retType, LLType[] argTypes) {
		String key = getCodeTypeKey(retType, argTypes);
		LLCodeType type = codeTypes.get(key);
		if (type == null) {
			type = new LLCodeType(retType, argTypes, getPtrBits());
			codeTypes.put(key, type);
		}
		return type;
	}

	/**
	 * @param retAndArgTypes
	 * @return
	 */
	private String getCodeTypeKey(LLType retType, LLType[] retAndArgTypes) {
		if (retType == null && retAndArgTypes == null)
			return "<code>";
		
		StringBuilder sb = new StringBuilder();
		if (retType != null)
			sb.append(retType.toString());
		else
			sb.append("<unknown>");
		boolean first = true;
		for (LLType type : retAndArgTypes) {
			if (first) {
				sb.append('='); first = false;
			}
			else
				sb.append(',');
			if (type != null)
				sb.append(type.toString());
			else
				sb.append("<unknown>");
		}
		return sb.toString();
	}

	/**
	 * @param retType
	 * @param argumentTypes
	 * @return
	 */
	public LLCodeType getCodeType(IAstType retType, IAstArgDef[] argumentTypes) {
		LLType[] argTypes = new LLType[argumentTypes != null ? argumentTypes.length : 0];
		for (int i = 0; i < argTypes.length; i++)
			argTypes[i] = argumentTypes[i].getType();
		return getCodeType(retType != null ? retType.getType() : null, argTypes);
	}

	/**
	 * Create a new literal node with the given value
	 * @param type
	 * @param object
	 * @return
	 */
	public IAstLitExpr createLiteralNode(LLType type, Object object) {
		switch (type.getBasicType()) {
		case INTEGRAL: {
			if (object instanceof Number) {
				long newValue = ((Number) object).longValue() << (64 - type.getBits()) >> (64 - type.getBits());
				return new AstIntLitExpr(newValue+"", type, newValue);
			} else if (object instanceof Boolean) {
				long newValue = ((Boolean) object).booleanValue() ? 1 : 0;
				return new AstIntLitExpr(newValue+"", type, newValue);
			} else {
				return null;
			}
		}
		case FLOATING: {
			if (object instanceof Number) {
				double newValue = ((Number) object).doubleValue();
				if (type.getBits() <= 32) {
					newValue = (float) newValue;
				}
				return new AstFloatLitExpr(newValue+"", type, newValue);
			} else if (object instanceof Boolean) {
				double newValue = ((Boolean) object).booleanValue() ? 1 : 0;
				return new AstFloatLitExpr(newValue+"", type, newValue);
			} else {
				return null;
			}
		}
		case BOOL: {
			if (!(object instanceof Number))
				return null;
			boolean newValue = ((Number) object).longValue() != 0;
			return new AstBoolLitExpr(newValue+"", type, newValue);
		}
		}
		return null;
	}

	public void setPtrBits(int ptrBits) {
		this.ptrBits = ptrBits;
		INTPTR = getPointerType(getIntType(ptrBits));
		PTRDIFF = getIntType(ptrBits);
	}

	public int getPtrBits() {
		return ptrBits;
	}

	public void setPtrAlign(int ptrAlign) {
		this.ptrAlign = ptrAlign;
	}

	public int getPtrAlign() {
		return ptrAlign;
	}
	public void setStructAlign(int structAlign) {
		this.structAlign = structAlign;
	}
	
	public int getStructAlign() {
		return structAlign;
	}

	/**
	 * @return
	 */
	public int getStackAlign() {
		return stackAlign;
	}
	/**
	 * @param i
	 */
	public void setStackAlign(int i) {
		stackAlign = i;
	}

	public int getStructMinAlign() {
		return structMinAlign;
	}

	public void setStructMinAlign(int structMinAlign) {
		this.structMinAlign = structMinAlign;
	}

	public LLPointerType getPointerType(LLType type) {
		String key = getUniqueTypeName(type);
		LLPointerType ptrType = ptrTypeMap.get(key);
		//if (type instanceof LLUpType) {
		//	ptrType = ptrType;
		//}
		if (ptrType == null) {
			ptrType = new LLPointerType(ptrBits, type);
			ptrTypeMap.put(key, ptrType);
		}
		return ptrType;
	}

	public LLRefType getRefType(LLType type) {
		String key = getUniqueTypeName(type);
		LLRefType refType = refTypeMap.get(key);
		if (refType == null) {
			refType = new LLRefType(type, ptrBits, INT.getBits());
			refTypeMap.put(key, refType);
		}
		return refType;
	}

	/**
	 * @return
	 */
	public Collection<LLType> getTypes() {
		List<LLType> types = new ArrayList<LLType>();
		types.addAll(this.types);
		types.addAll(ptrTypeMap.values());
		types.addAll(refTypeMap.values());
		types.addAll(arrayTypeMap.values());
		types.addAll(dataTypeMap.values());
		types.addAll(tupleTypeMap.values());
		types.addAll(stringLitTypeMap.values());
		//types.addAll(codeTypes.values());
		return types;
	}

	/**
	 * @param baseType
	 * @param count
	 * @param expr
	 * @return
	 */
	public LLArrayType getArrayType(LLType baseType, int count,
			IAstTypedExpr sizeExpr) {
		Tuple key = new Tuple(baseType, count, sizeExpr);
		LLArrayType array = arrayTypeMap.get(key);
		if (array == null) {
			array = new LLArrayType(baseType, count, sizeExpr);
			arrayTypeMap.put(key, array);
		}
		return array;
	}

	public LLIntType getIntType(int bits) {
		return getIntType(null, bits);
	}
	
	public LLIntType getIntType(String name, int bits) {
		LLIntType type = intMap.get(bits);
		if (type == null || (name != null && !name.equals(type.getName()))) {
			type = new LLIntType(name, bits);
			intMap.put(bits, type);
		}
		return type;
	}
	

	/**
	 * @param sym
	 */
	public LLType getNamedType(ISymbol sym) {
		LLType type = llvmNameToTypeMap.get(sym);
		return type;
	}
	
	public LLDataType getDataType(ISymbol symbol, List<LLInstanceField> ifields, List<LLStaticField> statics) {
		String key = getDataTypeKey(symbol.getScope().getUniqueName() + symbol.getUniqueName(), ifields, statics);
		LLDataType data = dataTypeMap.get(key);
		if (data == null) {
			//name = uniquify(name);
			data = new LLDataType(this, symbol,
					(LLInstanceField[]) ifields.toArray(new LLInstanceField[ifields.size()]),
					(LLStaticField[]) statics.toArray(new LLStaticField[statics.size()]));
			
			dataTypeMap.put(key, data);
			llvmNameToTypeMap.put(symbol, data);
		}
		return data;
	}

	private String getDataTypeKey(String name, List<LLInstanceField> ifields,
			List<LLStaticField> statics) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('$');
		for (LLInstanceField field : ifields)
			sb.append(getUniqueTypeName(field.getType())).append('$');
		for (LLStaticField field : statics)
			sb.append(getUniqueTypeName(field.getType())).append('$');
		return sb.toString();
	}

	private String getUniqueTypeName(LLType type) {
		String name = type != null ? (type.getLLVMType() != null ? type.getLLVMType() : type.toString() + (type.isGeneric() ? "<g>" : "")) : "<u>";
		return name;
	}
	
	public LLDataType getDataType(ISymbol symbol, List<LLType> fieldTypes) {
		String key = getDataTypeKey(symbol.getScope().getUniqueName() + symbol.getUniqueName(), fieldTypes);
		LLDataType data = dataTypeMap.get(key);
		if (data == null) {
			List<LLInstanceField> ifields = new ArrayList<LLInstanceField>(fieldTypes.size());
			for (LLType type : fieldTypes)
				ifields.add(new LLInstanceField("", type, null, null));
			//name = uniquify(name);
			data = new LLDataType(this, symbol,
					(LLInstanceField[]) ifields.toArray(new LLInstanceField[ifields.size()]),
					null);
			dataTypeMap.put(key, data);
			llvmNameToTypeMap.put(symbol, data);
		}
		return data;
	}
	
	private String getDataTypeKey(String name, List<LLType> ifields) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('$');
		for (LLType type : ifields)
			sb.append(getUniqueTypeName(type)).append('$');
		return sb.toString();
	}

	/**
	 * @param llTypes
	 * @return
	 */
	public LLTupleType getTupleType(LLType[] types) {
		String key = getTupleTypeKey(types);
		LLTupleType tuple = tupleTypeMap.get(key);
		if (tuple == null) {
			tuple = new LLTupleType(this, types);
			tupleTypeMap.put(key, tuple);
		}
		return tuple;
	}
	
	private String getTupleTypeKey(LLType[] types) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (LLType type : types) {
			if (first) first = false; else sb.append('$');
			sb.append(getUniqueTypeName(type));
		}
		return sb.toString();
	}

	/**
	 * @param type
	 * @return
	 */
	public LLInstanceType getInstanceType(ISymbol symbol, LLType[] types) {
		LLInstanceType instanceType;
		instanceType = new LLInstanceType(symbol, types);
		return instanceType;
	}

	public void registerInstanceType(LLInstanceType instance, LLType realType) {
		instanceToRealTypeMap.put(instance, realType);
	}
	
	public LLType getInstanceType(LLInstanceType instance) {
		return instanceToRealTypeMap.get(instance);
	}
	
	/**
	 * @return the instanceToRealTypeMap
	 */
	public Map<LLInstanceType, LLType> getInstanceToRealTypeMap() {
		return instanceToRealTypeMap;
	}

	/**
	 * @param name
	 * @return
	 */
	public LLDataType getDataType(LLDataType dataType) {
		for (LLDataType type : dataTypeMap.values()) {
			if (type.getName().equals(dataType.getName()) && type.isMoreComplete(dataType))
				dataType = type;
		}
		return dataType;
	}

	/**
	 * @param b
	 */
	public void setLittleEndian(boolean b) {
		this.isLittleEndian = b;
	}

	/**
	 * @param type
	 * @return
	 */
	public LLType getRealType(LLType type) {
		if (type instanceof LLSymbolType)
			type = ((LLSymbolType) type).getRealType(this);
		if (type instanceof LLDataType) {
			type = getDataType((LLDataType) type);
		}
		return type;
	}


	public LLDataType getStringLiteralType(String str) {
		int len = str.length();
		LLDataType strLitType = stringLitTypeMap.get(len);
		if (strLitType == null) {
			
			String name;
			name = STR.getSymbol().getName() + "$" + len;
			ISymbol sym;
			sym = STR.getSymbol().getScope().get(name);
			if (sym == null) {
				sym = STR.getSymbol().getScope().add(name, false);
			}
			
			return getStringLiteralType(str, sym);
		}
		return strLitType;
	}
	protected LLDataType getStringLiteralType(String str, ISymbol sym) {
		int len = str.length();
		LLDataType strLitType = stringLitTypeMap.get(len);
		if (strLitType == null) {
			List<LLInstanceField> strFields = new ArrayList<LLInstanceField>();
			strFields.add(new LLInstanceField("length", INT, null, null));
			LLArrayType arrayType = getArrayType(CHAR, len, null); 
			strFields.add(new LLInstanceField("s", arrayType, null, null));
			strLitType = getDataType(sym, strFields, Collections.<LLStaticField>emptyList());
			sym.setType(strLitType);
			stringLitTypeMap.put(len, strLitType);
		}
		return strLitType;
	}

	/**
	 * @param type
	 * @return
	 */
	public boolean isStringType(LLType type) {
		return stringLitTypeMap.values().contains(type);
	}

}
