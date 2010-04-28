/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;


/**
 * @author ejs
 *
 */
public abstract class BaseLLAggregateType extends BaseLLType implements LLAggregateType {

	protected final LLType[] NO_TYPES = new LLType[0];
	
	private final boolean isAbstract;

	/**
	 * @param name
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 * @param isAbstract 
	 */
	public BaseLLAggregateType(String name, int bits, String llvmType,
			BasicType basicType, LLType subType, boolean isAbstract) {
		super(name, bits, llvmType, basicType, subType);
		this.isAbstract = isAbstract;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#isAbstract()
	 */
	@Override
	public boolean isAbstract() {
		return isAbstract;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#getSymbolicName()
	 */
	@Override
	public String getSymbolicName() {
		if (getName() != null)
			return getName();
		StringBuilder sb = new StringBuilder();
		sb.append(getBasicType().toString()).append('$');
		boolean first = true;
		for (LLType type : getTypes()) {
			if (first) first = false; else sb.append('.');
			sb.append(type.getSymbolicName());
		}
		sb.append('$');
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		for (int idx = 0; idx < getCount(); idx++) {
			LLType type = getType(idx);
			if (type == null || (!type.isGeneric() && !type.isComplete()))
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
		
		LLAggregateType otherAggregate = null;
		
		if (!(otherType instanceof LLGenericType)) {
			if (!otherType.getClass().equals(getClass()))
				return false;
			
			otherAggregate = (LLAggregateType) otherType;
			if (isAbstract() && otherAggregate.isAbstract())
				return false;
			if (isAbstract())
				return false;
			if (otherAggregate.isAbstract())
				return true;
			if (otherAggregate.getCount() != getCount()) 
				return false;
		}
		
		if (isComplete() && !otherType.isComplete())
			return true;
		
		if (otherAggregate == null)
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
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#matchesExactly(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean matchesExactly(LLType target) {
		if (target == null)
			return true;
		
		if (getBasicType() != target.getBasicType())
			return false;

		if (target instanceof LLUpType) {
			IAstType realType = ((LLUpType) target).getRealType();
			if (realType != null && realType.getType().equals(this))
				return true;
		}
		if (!(target instanceof LLAggregateType)) 
			return false;
		LLAggregateType aggTarget = (LLAggregateType) target;
		if (getCount() != aggTarget.getCount())
			return false;
		
		LLType[] types = getTypes();
		LLType[] otherTypes = aggTarget.getTypes();
		
		for (int idx = 0; idx < getCount(); idx++) {
			if (types[idx] == null || otherTypes[idx] == null)
				continue;
			if (!types[idx].matchesExactly(otherTypes[idx]))
				return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isCompatibleWith(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isCompatibleWith(LLType target) {
		if (!super.isCompatibleWith(target))
			return false;
		
		if (!(target instanceof LLAggregateType))
			return false;
		LLAggregateType aggTarget = (LLAggregateType) target;
		if (isAbstract() || aggTarget.isAbstract())
			return true;
		if (getCount() != aggTarget.getCount())
			return false;
				
		LLType[] types = getTypes();
		LLType[] otherTypes = aggTarget.getTypes();
		
		for (int idx = 0; idx < getCount(); idx++) {
			if (types[idx] == null || otherTypes[idx] == null)
				continue;
			if (!types[idx].isCompatibleWith(otherTypes[idx]))
				return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, java.lang.String, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, LLType fromType, LLType toType) {
		if (fromType == null)
			return toType;
		
		LLType[] types = getTypes();
		LLType[] newTypes = null;
		for (int idx = 0; idx < types.length; idx++) {
			if (types[idx] != null) {
				LLType updated = types[idx].substitute(typeEngine, fromType, toType);
				if (updated != types[idx]) {
					if (newTypes == null) {
						newTypes = Arrays.copyOf(types, types.length);
					}
					newTypes[idx] = updated;
				}
			}
		}
		if (newTypes != null)
			return updateTypes(typeEngine, newTypes);
		else
			return this;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, org.ejs.eulang.symbols.IScope, java.util.Map)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, IScope origScope,
			Map<Integer, ISymbol> symbolMap) {
		LLType[] types = getTypes();
		LLType[] newTypes = null;
		for (int idx = 0; idx < types.length; idx++) {
			if (types[idx] != null) {
				LLType updated = types[idx].substitute(typeEngine, origScope, symbolMap);
				if (updated != types[idx]) {
					if (newTypes == null) {
						newTypes = Arrays.copyOf(types, types.length);
					}
					newTypes[idx] = updated;
				}
			}
		}
		if (newTypes != null)
			return updateTypes(typeEngine, newTypes);
		else
			return this;
	}
}
