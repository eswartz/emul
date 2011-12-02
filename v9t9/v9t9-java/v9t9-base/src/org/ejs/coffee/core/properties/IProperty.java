/**
 * 
 */
package org.ejs.coffee.core.properties;

import java.util.List;



/**
 * @author ejs
 *
 */
public interface IProperty extends IXMLPersistable, IPersistable {
	String getName();
	String getLabel();
	String getDescription();
	
	IPropertyEditor createEditor();
	
	Object getValue();
	void setValue(Object value);
	void setValueFromString(String value);
	
	// utilities
	
	int getInt();
    boolean getBoolean();
    String getString();
	<T> List<T> getList();
    void setInt(int val);
    void setBoolean(boolean val);
    void setString(String val);
    void setList(List<?> val);
    
	
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
	
	/**
	 * Tell if the property is hidden in generalized UI.
	 * @return
	 */
	boolean isHidden();
}
