/**
 * 
 */
package v9t9.base.properties;

/**
 * @author ejs
 *
 */
public class ArrayElementFieldProperty extends FieldProperty implements
		IProperty {

	private final int index;

	public ArrayElementFieldProperty(
			Object obj, String arrayFieldName,
			int index, String name) {
		super(null, obj, arrayFieldName, name);
		this.index = index; 
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.FieldProperty#setValueFromString(java.lang.String)
	 */
	@Override
	public void setValueFromString(String txt) {
		FieldUtils.setArrayValueFromString(field, index, obj, txt);
		firePropertyChange();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.FieldProperty#getValue()
	 */
	@Override
	public Object getValue() {
		return FieldUtils.getArrayValue(field, index, obj);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.FieldProperty#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		FieldUtils.setArrayValue(field, index, obj, value);
		firePropertyChange();
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.FieldProperty#getPersistedName()
	 */
	@Override
	public String getName() {
		return fieldName + "_" + name;
	}
	
	/**
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.FieldProperty#createEditor()
	 */
	/*
	@Override
	public IPropertyEditor createEditor() {
		return new ArrayFieldPropertyEditor(this);
	}
	*/
}
