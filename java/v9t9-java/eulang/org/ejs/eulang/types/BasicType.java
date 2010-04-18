/**
 * 
 */
package org.ejs.eulang.types;

public enum BasicType {
	INTEGRAL(LLType.TYPECLASS_PRIMITIVE),
	FLOATING(LLType.TYPECLASS_PRIMITIVE),
	VOID(0),
	POINTER(LLType.TYPECLASS_MEMORY),
	DATA(LLType.TYPECLASS_DATA ),
	CODE(LLType.TYPECLASS_CODE), 
	LABEL(LLType.TYPECLASS_CODE), 
	BOOL(LLType.TYPECLASS_PRIMITIVE), 
	REF(LLType.TYPECLASS_MEMORY), 
	TUPLE(LLType.TYPECLASS_DATA),
	GENERIC(~0), 
	ARRAY(LLType.TYPECLASS_MEMORY);

	private final int classMask;
	private BasicType(int classMask) {
		this.classMask = classMask;
	}
	public boolean isCompatibleWith(BasicType basicType) {
		return (classMask & basicType.classMask) != 0; 
	}
	/**
	 * @return
	 */
	public int getClassMask() {
		return classMask;
	}
}