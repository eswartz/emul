/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.HashMap;
import java.util.Map;

import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.types.LLBoolType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLFloatType;
import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLVoidType;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class TypeEngine {
	public LLType UNSPECIFIED = null;
	public int ptrBits;
	public LLIntType INT;
	public LLIntType BYTE;
	public LLFloatType FLOAT;
	public LLIntType INT_ANY;
	public LLBoolType BOOL;
	public LLVoidType VOID;
	
	private Map<String, LLCodeType> codeTypes = new HashMap<String, LLCodeType>();

	/**
	 * 
	 */
	public TypeEngine() {
		ptrBits = 16;
		VOID = new LLVoidType();
		BOOL = new LLBoolType(1);
		BYTE = new LLIntType(8);
		INT = new LLIntType(16);
		INT_ANY = new LLIntType(0);
		FLOAT = new LLFloatType(32, 23);
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
		
		if (a.getBasicType() == BasicType.INTEGRAL && b.getBasicType() == BasicType.INTEGRAL)
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
			type = new LLCodeType(retType, argTypes, ptrBits);
			codeTypes .put(key, type);
		}
		return type;
	}

	/**
	 * @param retAndArgTypes
	 * @return
	 */
	private String getCodeTypeKey(LLType retType, LLType[] retAndArgTypes) {
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
	public LLType getCodeType(IAstType retType, IAstArgDef[] argumentTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param globalScope
	 */
	public void populateTypes(GlobalScope globalScope) {
		globalScope.add(new AstName("Int"), new AstType(INT));
		globalScope.add(new AstName("Float"), new AstType(FLOAT));		
		globalScope.add(new AstName("Void"), new AstType(VOID));		
		globalScope.add(new AstName("Bool"), new AstType(BOOL));		
		globalScope.add(new AstName("Byte"), new AstType(BYTE));		
	}
}
