/**
 * 
 */
package v9t9.base.properties;

import java.util.Comparator;
import java.util.List;



/**
 * @author ejs
 *
 */
public interface IProperty extends IXMLPersistable, IPersistable,
	Comparable<IProperty>, Comparator<IProperty> {
	String getName();
	String getLabel();
	String getDescription();
	
	//void setEditorProvider(IPropertyEditorProvider editorProvider);
	
	//IPropertyEditor createEditor();
	
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
	
	void addEnablementDependency(IProperty other);
}
