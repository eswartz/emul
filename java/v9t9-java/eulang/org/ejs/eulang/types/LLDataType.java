/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstType;

/**
 * @author ejs
 *
 */
public class LLDataType extends BaseLLAggregateType {

	private static final LLInstanceField[] NO_FIELDS = new LLInstanceField[0];
	private static final LLStaticField[] NO_STATIC_FIELDS = new LLStaticField[0];
	
	private LLInstanceField[] ifields;
	private LLStaticField[] statics;
	private int sizeof;

	public LLDataType(TypeEngine engine, String name, LLInstanceField[] ifields, LLStaticField[] statics) {
		super(name, sumTypeBits(engine, ifields), toLLVMString(name, ifields), BasicType.DATA, null, ifields == null);
		this.ifields = ifields != null ? ifields : NO_FIELDS;
		this.statics = statics != null ? statics : NO_STATIC_FIELDS;
		
		updateOffsets(engine);
	}

	/**
	 * 
	 */
	private void updateOffsets(TypeEngine typeEngine) {
		TypeEngine.Alignment align = typeEngine.new Alignment(Target.STRUCT);
		for (int idx = 0; idx < ifields.length; idx++) {
			LLInstanceField field = ifields[idx];
			field.setOffset(align.alignAndAdd(field.getType()));
		}
		this.sizeof = align.sizeof();		
	}
	
	/**
	 * @return the sizeof
	 */
	public int getSizeof() {
		return sizeof;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(ifields) + Arrays.hashCode(statics);
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
		LLDataType other = (LLDataType) obj;
		if (!Arrays.equals(ifields, other.ifields))
			return false;
		if (!Arrays.equals(statics, other.statics))
			return false;
		return true;
	}


	/**
	 * @param fields
	 * @return
	 */
	private static String toLLVMString(String name, LLInstanceField[] fields) {
		if (fields == null)
			return "<data>";
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("{");
		for (BaseLLField field : fields) {
			if (first) first = false; else sb.append(',');
			LLType type = field.getType();
			String typeName = null;
			/*
			if (type instanceof LLUpType) {
				IAstType realType = ((LLUpType) type).getRealType();
				if (realType instanceof IAstDataType) {
					if (((IAstDataType) realType).getTypeName().getName().equals(name))
						typeName = 
				}
			}*/
			typeName = type != null ? (type.getLLVMType() != null ? type.getLLVMType() : type.getName()) : "<unknown>";
			sb.append(typeName);
		}
		sb.append('}');
		return sb.toString();
	}

	/**
	 * @param engine 
	 * @param types
	 * @return
	 */
	private static int sumTypeBits(TypeEngine engine, LLInstanceField[] fields) {
		if (fields == null)
			return 0;
		
		TypeEngine.Alignment align = engine.new Alignment(TypeEngine.Target.STRUCT);
		for (LLInstanceField field : fields)  {
			align.alignAndAdd(field.getType());
		}
		return align.sizeof();
	}

	
	/**
	 * @return
	 */
	public LLType[] getTypes() {
		LLType[] types = new LLType[ifields.length + statics.length];
		for (int idx = 0; idx < ifields.length; idx++)
			types[idx] = ifields[idx].getType(); 
		for (int idx = 0; idx < statics.length; idx++)
			types[idx + ifields.length] = statics[idx].getType(); 
		return types;
	};

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getCount()
	 */
	@Override
	public int getCount() {
		return ifields.length + statics.length;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getType(int)
	 */
	@Override
	public LLType getType(int idx) {
		return idx < ifields.length ? ifields[idx].getType() : statics[idx - ifields.length].getType();
	}

	public LLDataType updateTypes(TypeEngine typeEngine, LLType[] types) {
		LLInstanceField[] newIFields = new LLInstanceField[ifields.length];
		for (int idx = 0; idx < newIFields.length; idx++) {
			LLInstanceField field = ifields[idx];
			newIFields[idx] = new LLInstanceField(field.getName(), types[idx],
					field.getDefinition(), field.getDefault());
		}
		
		LLStaticField[] newSFields = new LLStaticField[statics.length];
		for (int idx = 0; idx < newSFields.length; idx++) {
			LLStaticField field = statics[idx];
			newSFields[idx] = new LLStaticField(field.getName(), types[idx + ifields.length],
					field.getSymbol(),
					field.getDefinition(), field.getDefault());
		}
		
		return new LLDataType(typeEngine, name, newIFields, newSFields);
	}

	/**
	 * @return
	 */
	public LLInstanceField[] getInstanceFields() {
		return ifields;
	}
	
	 /**
	 * @return the statics
	 */
	public LLStaticField[] getStaticFields() {
		return statics;
	}

	/**
	 * @param name
	 * @return
	 */
	public BaseLLField getField(String name) {
		for (BaseLLField field : ifields)
			if (field.getName().equals(name))
				return field;
		for (BaseLLField field : statics)
			if (field.getName().equals(name))
				return field;
		return null;
	}

	/**
	 * @param field
	 * @return
	 */
	public int getFieldIndex(BaseLLField afield) {
		int idx = 0;
		for (BaseLLField field : ifields)
			if (field == afield)
				return idx;
			else
				idx++;
		for (BaseLLField field : statics)
			if (field == afield)
				return idx;
			else
				idx++;
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	/*
	@Override
	public boolean isComplete() {
		for (int idx = 0; idx < getCount(); idx++) {
			LLType type = getType(idx);
			if (type == null || !type.isComplete())
				return false;
		}
		return true;
	}*/
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isCompatibleWith(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isCompatibleWith(LLType target) {
		if (target instanceof LLUpType)
			return target.isCompatibleWith(this);
		return super.isCompatibleWith(target);
	}
}
