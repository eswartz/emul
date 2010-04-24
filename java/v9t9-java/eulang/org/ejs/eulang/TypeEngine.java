/**
 * 
 */
package org.ejs.eulang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.impl.AstBoolLitExpr;
import org.ejs.eulang.ast.impl.AstFloatLitExpr;
import org.ejs.eulang.ast.impl.AstIntLitExpr;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLBoolType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLFloatType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLLabelType;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLRefType;
import org.ejs.eulang.types.LLStaticField;
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

	public LLIntType INT_ANY;
	public LLBoolType BOOL;
	public LLVoidType VOID;
	public LLLabelType LABEL;
	public LLType NIL;
	
	private Map<String, LLType> llvmNameToTypeMap = new HashMap<String, LLType>();
	
	private Map<String, LLCodeType> codeTypes = new HashMap<String, LLCodeType>();
	private Set<LLType> types = new HashSet<LLType>();
	private Map<LLType, LLPointerType> ptrTypeMap = new HashMap<LLType, LLPointerType>();
	private Map<LLType, LLRefType> refTypeMap = new HashMap<LLType, LLRefType>();
	private Map<Tuple, LLArrayType> arrayTypeMap = new HashMap<Tuple, LLArrayType>();
	private Map<Integer, LLIntType> intMap = new HashMap<Integer, LLIntType>();
	private Map<String, LLDataType> dataTypeMap = new HashMap<String, LLDataType>();
	
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
			/*if (alignToField) {
				if (fieldOffset % align != 0) {
					fieldOffset += (align - fieldOffset % align);
				}
			}
			*/
			
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
	public LLType INTPTR;
	public LLType REFPTR;
	public LLBoolType LLBOOL;
	private int gUniqueId;
	public LLIntType PTRDIFF;
	
	/**
	 * 
	 */
	public TypeEngine() {
		isLittleEndian = false;
		setPtrBits(16);
		setPtrAlign(16);
		setStackMinAlign(16);
		setStackAlign(16);
		setStructAlign(16);
		setStructMinAlign(8);
		
		VOID = register(new LLVoidType(null));
		NIL = register(new LLVoidType(null));
		LABEL = register(new LLLabelType());
		BOOL = register(new LLBoolType("Bool", 1));
		LLBOOL = register(new LLBoolType(null, 1));
		BYTE = register(new LLIntType("Byte", 8));
		INT = register(new LLIntType("Int", 16));
		FLOAT = register(new LLFloatType("Float", 32, 23));
		DOUBLE = register(new LLFloatType("Double", 64, 53));
		//REFPTR = register(new LLRefType(new LLPointerType(ptrBits, VOID), ptrBits));
		REFPTR = register(new LLPointerType("RefPtr", ptrBits, 
				getRefType(BYTE)));
		
		INT_ANY = new LLIntType("Int*", 0);
	}
	
	/**
	 * Add names for globally accessible types
	 * @param globalScope
	 */
	public void populateTypes(GlobalScope globalScope) {
		globalScope.add(new AstName("Int"), new AstType(INT));
		globalScope.add(new AstName("Float"), new AstType(FLOAT));		
		globalScope.add(new AstName("Double"), new AstType(DOUBLE));		
		globalScope.add(new AstName("Void"), new AstType(VOID));		
		globalScope.add(new AstName("Bool"), new AstType(BOOL));		
		globalScope.add(new AstName("Byte"), new AstType(BYTE));		
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
		if (a.equals(b))
			return a;
		
		//a = getBaseType(a);
		//b = getBaseType(b);
		
		if (a == null || b == null)
			return null;
		
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
		INTPTR = getPointerType(new LLIntType("Int", ptrBits));
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
		LLPointerType ptrType = ptrTypeMap.get(type);
		if (ptrType == null) {
			ptrType = new LLPointerType(ptrBits, type);
			ptrTypeMap.put(type, ptrType);
		}
		return ptrType;
	}

	public LLRefType getRefType(LLType type) {
		LLRefType refType = refTypeMap.get(type);
		if (refType == null) {
			refType = new LLRefType(type, ptrBits, INT.getBits());
			refTypeMap.put(type, refType);
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
		LLIntType type = intMap.get(bits);
		if (type == null) {
			type = new LLIntType(null, bits);
			intMap.put(bits, type);
		}
		return type;
	}
	
	public LLDataType getDataType(String name, List<LLInstanceField> ifields, List<LLStaticField> statics) {
		String key = getDataTypeKey(name, ifields, statics);
		LLDataType data = dataTypeMap.get(key);
		if (data == null) {
			name = uniquify(name);
			data = new LLDataType(this, name,
					(LLInstanceField[]) ifields.toArray(new LLInstanceField[ifields.size()]),
					(LLStaticField[]) statics.toArray(new LLStaticField[statics.size()]));
			dataTypeMap.put(key, data);
			llvmNameToTypeMap.put(name, data);
		}
		return data;
	}

	/**
	 * @param name
	 * @return
	 */
	private String uniquify(String origName) {
		String name = origName; 
		while (true) {
			if (llvmNameToTypeMap.containsKey(name)) {
				name = origName + "." + gUniqueId++;
			} else {
				break;
			}
		}
		return name;
	}

	private String getDataTypeKey(String name, List<LLInstanceField> ifields,
			List<LLStaticField> statics) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('$');
		for (LLInstanceField field : ifields)
			sb.append(field.getType() != null ? field.getType().toString() : "<u>").append('$');
		for (LLStaticField field : statics)
			sb.append(field.getType() != null ? field.getType().toString() : "<u>").append('$');
		return sb.toString();
	}
	
	public LLDataType getDataType(String name, List<LLType> fieldTypes) {
		String key = getDataTypeKey(name, fieldTypes);
		LLDataType data = dataTypeMap.get(key);
		if (data == null) {
			List<LLInstanceField> ifields = new ArrayList<LLInstanceField>(fieldTypes.size());
			for (LLType type : fieldTypes)
				ifields.add(new LLInstanceField("", type, null, null));
			name = uniquify(name);
			data = new LLDataType(this, name,
					(LLInstanceField[]) ifields.toArray(new LLInstanceField[ifields.size()]),
					null);
			llvmNameToTypeMap.put(name, data);
			dataTypeMap.put(key, data);
		}
		return data;
	}
	
	private String getDataTypeKey(String name, List<LLType> ifields) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('$');
		for (LLType type : ifields)
			sb.append(type != null ? type.toString() : "<u>").append('$');
		return sb.toString();
	}
}
