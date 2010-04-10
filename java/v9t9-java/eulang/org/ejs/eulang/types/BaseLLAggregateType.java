/**
 * 
 */
package org.ejs.eulang.types;


/**
 * @author ejs
 *
 */
public abstract class BaseLLAggregateType extends BaseLLType implements LLAggregateType {

	/**
	 * @param name
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 */
	public BaseLLAggregateType(String name, int bits, String llvmType,
			BasicType basicType, LLType subType) {
		super(name, bits, llvmType, basicType, subType);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		for (int idx = 0; idx < getCount(); idx++) {
			LLType type = getType(idx);
			if (type == null)
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType otherType) {
		if (this == otherType)
			return false;
		
		if (otherType == null)
			return true;
		
		if (isComplete() && !otherType.isComplete())
			return true;
		
		if (!otherType.getClass().equals(getClass()))
			return false;
		
		LLAggregateType otherAggregate = (LLAggregateType) otherType;
		if (otherAggregate.getCount() != getCount()) 
			return false;
		
		int thisDefinedTypes = 0;
		for (int idx = 0; idx < getCount(); idx++) {
			LLType type = getType(idx);
			LLType other = otherAggregate.getType(idx);
			if (type == other)
				continue;
			if (type != null && other == null) {
				thisDefinedTypes++;
			} else if (type == null && other != null) {
				thisDefinedTypes--;
			} else if (!type.isGeneric() && !other.isGeneric()) {
				if (type.getBasicType() != other.getBasicType())
					return false;
			} else
				/*if (!type.isGeneric() && type.isComplete() && other.isGeneric()) {
				thisDefinedTypes++;
			} else if (type.isGeneric() && (!other.isGeneric() && other.isComplete())) {
				thisDefinedTypes--;
			} else*/ {
				if (type.isMoreComplete(other)) {
					thisDefinedTypes++;
				} else if (other.isMoreComplete(type)) {
					thisDefinedTypes--;
				}
			}
			/*
			if (type != null && type.isComplete() && !type.isGeneric()) {
				if (other != null) {
					if (!other.isGeneric() && !other.equals(type))
						return false;
					if (!other.isComplete())
						thisDefinedTypes++;
				} else {
					thisDefinedTypes++;
				}
			}
			else if (other != null) {
				thisDefinedTypes--;
			}
			*/
		}
		
		return thisDefinedTypes > 0;
	}


	protected int typeCount() {
		int cnt = 0;
		for (int idx = 0; idx < getCount(); idx++) {
			LLType type = getType(idx);
			if (type != null && type.isComplete())
				cnt++;
		}
		return cnt;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isGeneric()
	 */
	@Override
	public boolean isGeneric() {
		for (int idx = 0; idx < getCount(); idx++) {
			LLType type = getType(idx);
			if (type != null && type.isGeneric())
				return true;
		}
		return false;
	}
}
