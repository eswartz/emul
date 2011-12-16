/**
 * 
 */
package ejs.base.properties;

/**
 * @author ejs
 *
 */
public interface IPropertyListener {
	/** The property changed: either value, hidden state, ... */
	void propertyChanged(IProperty property);
}
