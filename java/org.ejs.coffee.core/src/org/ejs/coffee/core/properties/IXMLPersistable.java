/**
 * 
 */
package org.ejs.coffee.core.properties;

import org.w3c.dom.Element;

/**
 * @author ejs
 *
 */
public interface IXMLPersistable {
	/** Save the property into the given element.  Children may be created. 
	 * The type of the property should be recreatable from the data in the element
	 * (e.g., a number should be emitted in ASCII so that loadState can re-read it;
	 * or an object should be emitted with a known child element name that
	 * loadState can find.)
	 * */
	void saveState(Element element);
	/** Save the property into the given element.  The property already exists */
	void loadState(Element element);
}
