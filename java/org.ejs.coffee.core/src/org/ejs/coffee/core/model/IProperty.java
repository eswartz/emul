/**
 * 
 */
package org.ejs.coffee.core.model;

import org.ejs.coffee.core.jface.IPropertyEditor;


/**
 * @author ejs
 *
 */
public interface IProperty extends IPersistable {
	String getName();
	IPropertyEditor createEditor();
	Object getValue();
	void setValue(Object value);
	void setValueFromString(String value);
	
	
	/**
	 * If the property is not a simple type, return a factory for creating and persisting it
	 * @return
	 */
	IClassPropertyFactory getClassFactory();
	
	/**
	 * @return
	 */
	Class<?> getType();
	
	void addListener(IPropertyListener listener);
	void removeListener(IPropertyListener listener);
	void firePropertyChange();
	
	boolean isHidden();
}
