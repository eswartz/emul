/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

import org.apache.batik.css.engine.value.svg.AlignmentBaselineManager;
import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public class LLTupleType extends BaseLLAggregateType {

	private LLType[] types;

	/**
	 * @param name
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 */
	public LLTupleType(TypeEngine engine, LLType[] types) {
		super(null, sumTypeBits(engine, types), toLLVMString(types), BasicType.TUPLE, null, types == null);
		this.types = types != null ? types : NO_TYPES;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(types);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLTupleType other = (LLTupleType) obj;
		if (!Arrays.equals(types, other.types))
			return false;
		return true;
	}


	/**
	 * @param types
	 * @return
	 */
	private static String toLLVMString(LLType[] types) {
		if (types == null)
			return "<tuple>";
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("{");
		for (LLType type : types) {
			if (first) first = false; else sb.append(',');
			sb.append(type != null ? (type.getLLVMType() != null ? type.getLLVMType() : type.getName()) : "<unknown>");
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
		if (types == null)
			return 0;
		
		TypeEngine.Alignment align = engine.new Alignment(TypeEngine.Target.STRUCT);
		for (LLType type : types)  {
			align.alignAndAdd(type);
		}
		return align.sizeof();
	}

	
	/**
	 * @return
	 */
	public LLType[] getTypes() {
		return types;
	};

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getCount()
	 */
	@Override
	public int getCount() {
		return types.length;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getType(int)
	 */
	@Override
	public LLType getType(int idx) {
		return types[idx];
	}
	
	public LLTupleType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return new LLTupleType(typeEngine, type);
	}
}
