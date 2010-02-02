/**
 * 
 */
package org.ejs.coffee.core.model;

import org.ejs.coffee.core.jface.IPropertyEditor;


/**
 * @author ejs
 *
 */
public interface IPropertySource extends IPersistable {
	IProperty[] getProperties();
	String[] getPropertyNames();
	
	IPropertyEditor createEditor(String label);
	/**
	 * @param propertyName
	 * @return
	 */
	IProperty getProperty(String propertyName);
}
