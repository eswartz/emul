/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class LLTupleType extends BaseLLType {

	private LLType[] types;

	/**
	 * @param name
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 */
	public LLTupleType(TypeEngine engine, LLType[] types) {
		super(null, sumTypeBits(engine, types), toLLVMString(types), BasicType.TUPLE, null);
		this.types = types;
	}

	/**
	 * @param types
	 * @return
	 */
	private static String toLLVMString(LLType[] types) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("{");
		for (LLType type : types) {
			if (first) first = false; else sb.append(',');
			sb.append(type != null ? type.getLLVMType() : "opaque");
		}
		sb.append('}');
		return sb.toString();
	}

	/**
	 * @param engine 
	 * @param types
	 * @return
	 */
	private static int sumTypeBits(TypeEngine engine, LLType[] types) {
		int sum = 0;
		int align = engine.getStructAlign(); 
		for (LLType type : types)  {
			if (sum % align != 0)
				sum += (align - sum % align);
			sum += type != null ? type.getBits() : 0; 
		}
		return sum;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		for (LLType type : types)
			if (type == null)
				return false;
		return true;
	}

	/**
	 * @return
	 */
	public LLType[] getElementTypes() {
		return types;
	};

}
