/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLRefType extends BaseLLType {

	public LLRefType(LLType baseType, int ptrBits, int refCntBits) {
		super(baseType.getName() + "$ref", ptrBits, 
				 "{ " + (baseType != null ? baseType.getLLVMType() : "") + "*, i" + refCntBits + "}*",
				 BasicType.REF, baseType);
		if (baseType instanceof LLRefType)
			throw new IllegalArgumentException();
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return subType != null;
	}

}
