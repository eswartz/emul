/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;

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
	private final ISymbol symbol;

	public LLDataType(TypeEngine engine, ISymbol symbol, LLInstanceField[] ifields, LLStaticField[] statics) {
		super(symbol.getScope().getUniqueName() + symbol.getUniqueName(), 
				sumTypeBits(engine, ifields), toLLVMString(ifields), BasicType.DATA, null, ifields == null);
		this.symbol = symbol;
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) {
			if (obj instanceof LLType) {
				return (symbol.getScope().getUniqueName() + symbol.getUniqueName()).equals(((LLType) obj).getName());
			}
			return false;
		}
		if (!super.equals(obj))
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
	private static String toLLVMString(LLInstanceField[] fields) {
		if (fields == null)
			return null;
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("{");
		for (BaseLLField field : fields) {
			if (first) first = false; else sb.append(',');
			LLType type = field.getType();
			String typeName = null;
			if (type == null || !type.isComplete())
				return null;
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

	public LLType updateTypes(TypeEngine typeEngine, LLType[] types) {
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
		
		return typeEngine.getDataType(symbol, Arrays.asList(newIFields), Arrays.asList(newSFields));
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
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isCompatibleWith(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isCompatibleWith(LLType target) {
		if (target instanceof LLInstanceType)
			return target.isCompatibleWith(this);
		if (target instanceof LLSymbolType)
			return target.isCompatibleWith(this);
		return super.isCompatibleWith(target);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#substitute(org.ejs.eulang.TypeEngine, org.ejs.eulang.symbols.IScope, java.util.Map)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, IScope origScope,
			Map<Integer, ISymbol> symbolMap) {
		ISymbol newSym = symbol;
		if (origScope == symbol.getScope()) {
			newSym = symbolMap.get(symbol.getNumber());
			if (newSym == null)
				newSym = symbol;
		}
		if (newSym != symbol) {
			LLType newData = typeEngine.getDataType(newSym, Arrays.asList(ifields), Arrays.asList(statics));
			// continue substituting on fields
			return newData.substitute(typeEngine, origScope, symbolMap);
		}
		return super.substitute(typeEngine, origScope, symbolMap);
	}

	/**
	 * @return
	 */
	public ISymbol getSymbol() {
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType otherType) {
		if (otherType instanceof LLInstanceType) {
			LLInstanceType instance = (LLInstanceType) otherType;
			if (instance.getSymbol().getName().equals(symbol.getName()) && instance.getCount() == 0)
				return true;
		}
					
		return super.isMoreComplete(otherType);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isComplete()
	 */
	/*
	@Override
	public boolean isComplete() {
		for (int idx = 0; idx < getCount(); idx++) {
			LLType type = getType(idx);
			if (type == null || type.isGeneric())
				return false;
		}
		return true;
	}
	*/
}
