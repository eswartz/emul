/**
 * 
 */
package org.ejs.coffee.core.properties;



/**
 * @author ejs
 *
 */
public interface IPropertySource extends IXMLPersistable, IPersistable {
	IProperty[] getProperties();
	String[] getPropertyNames();
	
	IPropertyEditor createEditor(String label);
	/**
	 * @param propertyName
	 * @return
	 */
	IProperty getProperty(String propertyName);
}
