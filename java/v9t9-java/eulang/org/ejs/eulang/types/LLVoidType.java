/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLVoidType implements LLType {

	public LLVoidType() {
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return BasicType.VOID;
	}
	
	@Override
	public int hashCode() {
		return 19284;
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



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "void";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.types.LLType#getType()
	 */
	@Override
	public LLType getSubType() {
		return null;
	}

	/**
	 * @return
	 */
	public int getBits() {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return true;
	}
}
