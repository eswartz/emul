/*
  ArrayElementFieldProperty.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

/**
 * @author ejs
 *
 */
public class ArrayElementFieldProperty extends FieldProperty implements
		IProperty {

	private final int index;

	public ArrayElementFieldProperty(
			Object obj, String arrayFieldName,
			int index, String indexName) {
		super(arrayFieldName + "_" + indexName, obj, arrayFieldName);
		this.index = index; 
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.FieldProperty#doGetValue()
	 */
	@Override
	protected Object doGetValue() throws Exception {
		return FieldUtils.getArrayValue(field, index, obj);
	}
	/* (non-Javadoc)
	 * @see ejs.base.properties.FieldProperty#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object value) throws Exception {
		FieldUtils.setArrayValue(field, index, obj, value);
	}
	
	public int getIndex() {
		return index;
	}
}
