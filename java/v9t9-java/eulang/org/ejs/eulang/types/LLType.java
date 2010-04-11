/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public interface LLType {
	enum BasicType {
		INTEGRAL,
		FLOATING,
		VOID,
		POINTER,
		DATA,
		CODE, 
		BOOL, 
		REF, 
		TUPLE,
		GENERIC
	};
	
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
	
}
