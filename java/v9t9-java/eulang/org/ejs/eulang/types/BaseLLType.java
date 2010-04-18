/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public abstract class BaseLLType implements LLType {

	protected final int bits;

	protected final String name;
	protected final LLType subType;
	protected final BasicType basicType;

	protected final String llvmType;

	/**
	 * 
	 */
	public BaseLLType(String name, int bits, String llvmType, BasicType basicType, LLType subType) {
		this.name = name;
		this.bits = bits;
		this.llvmType = llvmType;
		this.basicType = basicType;
		this.subType = subType;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((basicType == null) ? 0 : basicType.hashCode());
		result = prime * result + bits;
		result = prime * result
				+ ((llvmType == null) ? 0 : llvmType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
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
		BaseLLType other = (BaseLLType) obj;
		if (basicType == null) {
			if (other.basicType != null)
				return false;
		} else if (!basicType.equals(other.basicType))
			return false;
		if (bits != other.bits)
			return false;
		if (llvmType == null) {
			if (other.llvmType != null)
				return false;
		} else if (!llvmType.equals(other.llvmType))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (subType == null) {
			if (other.subType != null)
				return false;
		} else if (!subType.equals(other.subType))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getBasicType()
	 */
	@Override
	public BasicType getBasicType() {
		return basicType;
	}
	
	public LLType getSubType() {
		return subType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return llvmType;
	}
	
	public int getBits() {
		return bits;
	}

	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getllvmType()
	 */
	@Override
	public String getLLVMType() {
		return llvmType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getLLVMName()
	 */
	@Override
	public String getLLVMName() {
		return name != null ? "%" + name : llvmType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#getSymbolicName()
	 */
	@Override
	public String getSymbolicName() {
		return name != null ? name : llvmType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType type) {
		return isComplete() && (type == null || !type.isComplete() || type.isGeneric());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isGeneric()
	 */
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#matchesExactly(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean matchesExactly(LLType target) {
		return this.equals(target);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isCompatibleWith(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isCompatibleWith(LLType target) {
		if (target == null)
			return false;
		if (!getBasicType().isCompatibleWith(target.getBasicType()))
			return false;

		if (getSubType() == null && target.getSubType() == null)
			return true;
		if (getSubType() == null || target.getSubType() == null)
			return true;
		return subTypesCompatible(target.getSubType());
	}



	protected boolean subTypesCompatible(LLType subType) {
		return subType.isCompatibleWith(subType);
	}
}