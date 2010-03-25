/**
 * 
 */
package org.ejs.eulang.llvm.types;

/**
 * @author ejs
 *
 */
public class LLIntType implements LLType {

	private final int bits;
	public LLIntType(int bits) {
		this.bits = bits;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bits;
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
		LLIntType other = (LLIntType) obj;
		if (bits != other.bits)
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "int" + bits;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.types.LLType#getType()
	 */
	@Override
	public LLType getType() {
		return null;
	}

}
