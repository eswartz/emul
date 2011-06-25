/**
 * 
 */
package org.ejs.coffee.core.properties;

/**
 * @author ejs
 *
 */
public interface IPropertyListener {
	/** The property changed: either value, hidden state, ... */
	void propertyChanged(IProperty property);
}
