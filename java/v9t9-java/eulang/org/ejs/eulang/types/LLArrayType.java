/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * @author ejs
 *
 */
public class LLArrayType extends BaseLLType {

	private final int arrayCount;
	private final IAstTypedExpr dynamicSizeExpr;


	public LLArrayType(LLType baseType, int arrayCount, IAstTypedExpr dynamicSizeExpr) {
		super((baseType != null ? baseType.getName() : "<unknown>") + (dynamicSizeExpr != null ? "$dyn" : "x" + arrayCount), 
				baseType != null ? baseType.getBits() * arrayCount : 0, 
				 "[ " + arrayCount + " x " + (baseType != null ? baseType.getLLVMType() : "") + " ]",
				 BasicType.ARRAY, baseType);
		this.arrayCount = arrayCount;
		this.dynamicSizeExpr = dynamicSizeExpr;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + arrayCount;
		result = prime * result
				+ ((dynamicSizeExpr == null) ? 0 : dynamicSizeExpr.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLArrayType other = (LLArrayType) obj;
		if (arrayCount != other.arrayCount)
			return false;
		if (dynamicSizeExpr == null) {
			if (other.dynamicSizeExpr != null)
				return false;
		} else if (!dynamicSizeExpr.equals(other.dynamicSizeExpr))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#toString()
	 */
	@Override
	public String toString() {
		String str = super.toString();
		if (dynamicSizeExpr != null) {
			str += " { dynamic: " + DumpAST.dumpString(dynamicSizeExpr) + " }";
		}
		return str;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return subType != null;
	}

	/**
	 * @return the arraySize
	 */
	public int getArrayCount() {
		return arrayCount;
	}

	public boolean isValidArrayIndex(int index) {
		if (isInitSized())
			return true;
		else
			return index < arrayCount;
	}


	/**
	 * @return
	 */
	public IAstTypedExpr getDynamicSizeExpr() {
		return dynamicSizeExpr;
	}



	/**
	 * @return
	 */
	public boolean isInitSized() {
		return arrayCount == 0 && dynamicSizeExpr == null;
	}
}
