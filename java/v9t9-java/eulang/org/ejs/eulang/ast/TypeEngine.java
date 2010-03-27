/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.HashMap;
import java.util.Map;

import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLFloatType;
import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class TypeEngine {
	public LLType UNSPECIFIED = null;
	public int ptrBits;
	public LLIntType INT;
	public LLFloatType FLOAT;
	public LLType INT_ANY;
	public LLType BOOL;
	
	private Map<String, LLCodeType> codeTypes = new HashMap<String, LLCodeType>();

	/**
	 * 
	 */
	public TypeEngine() {
		ptrBits = 16;
		BOOL = new LLIntType(1);
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
		
		if (a instanceof LLIntType && b instanceof LLIntType)
			return a.getBits() > b.getBits() ? a : b;
		
		if (a instanceof LLIntType && b instanceof LLFloatType) {
			return b;
		}
		if (b instanceof LLIntType && a instanceof LLFloatType) {
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
}
