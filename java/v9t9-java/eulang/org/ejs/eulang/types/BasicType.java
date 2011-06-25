/**
 * 
 */
package org.ejs.eulang.types;

public enum BasicType {
	VOID(0),
	INTEGRAL(LLType.TYPECLASS_PRIMITIVE),
	CHARACTER(LLType.TYPECLASS_PRIMITIVE),
	FLOATING(LLType.TYPECLASS_PRIMITIVE),
	BOOL(LLType.TYPECLASS_PRIMITIVE), 
	POINTER(LLType.TYPECLASS_MEMORY),
	DATA(LLType.TYPECLASS_DATA ),
	CODE(LLType.TYPECLASS_CODE), 
	LABEL(LLType.TYPECLASS_CODE), 
	REF(LLType.TYPECLASS_MEMORY), 
	TUPLE(LLType.TYPECLASS_DATA),
	ARRAY(LLType.TYPECLASS_MEMORY),
	GENERIC(~0);

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