/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public interface LLType {
	final int TYPECLASS_PRIMITIVE = 1,
		TYPECLASS_MEMORY = 2,
		TYPECLASS_CODE = 4,
		TYPECLASS_DATA = 8;
	
	String toString();
	boolean equals(Object obj);
	int hashCode();
	
	/** Get the subtype, if any */
	LLType getSubType();
	
	int getBits();
	
	BasicType getBasicType();
	/**
	 * @return
	 */
	boolean isComplete();
	/**
	 * @return
	 */
	String getName();
	
	/** Get the type, e.g. "i8" or "{ i16* , i16 }" */
	String getLLVMType();
	/**
	 * Get the name of the type -- either #getLLVMType() or "%" + getName()
	 * @return
	 */
	String getLLVMName();
	
	/** Get the mangled name */
	String getSymbolicName();
	
	/**
	 * Tell if this type is more complete than the other, which may have
	 * generics or unknown types.  Also, if the types are not compatible,
	 * this always returns false.
	 * @param type
	 * @return
	 */
	boolean isMoreComplete(LLType type);
	
	/**
	 * Tell if this type, or any contained/aggregate type, is generic.
	 * @return
	 */
	boolean isGeneric();
	/**
	 * Tell whether this type matches another on exact type comparisons -- 
	 * but ignoring any unknown types.
	 * @param target
	 * @return
	 */
	boolean matchesExactly(LLType target);

	/**
	 * Tell whether the two types are compatible.  They must be the same basic
	 * type, have the same aggregate structure (if aggregates), and have either
	 * unknowns, generics, or matching subtypes in each position. 
	 * @param target
	 * @return
	 */
	boolean isCompatibleWith(LLType target);
}
