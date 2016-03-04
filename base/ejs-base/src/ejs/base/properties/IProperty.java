/*
  IProperty.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.util.Comparator;
import java.util.List;



/**
 * @author ejs
 *
 */
public interface IProperty extends IXMLPersistable, IPersistable,
	Comparable<IProperty>, Comparator<IProperty> {
	/** get the property name (internal) */
	String getName();
	/** get the human-readable label */
	String getLabel();
	String getDescription();
	
	//void setEditorProvider(IPropertyEditorProvider editorProvider);
	
	//IPropertyEditor createEditor();
	
	Object getValue();
	void setValue(Object value);
	void setValueFromString(String value);
	
	// utilities
	
	int getInt();
	double getDouble();
    boolean getBoolean();
    String getString();
	<T> List<T> getList();
    void setInt(int val);
    void setDouble(double val);
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
	void addListenerAndFire(IPropertyListener listener);
	void removeListener(IPropertyListener listener);
	void firePropertyChange();
	
	/**
	 * Tell if the property is hidden in generalized UI.
	 * @return
	 */
	boolean isHidden();
	
	void addEnablementDependency(IProperty other);
}
