/**
 * 
 */
package org.ejs.coffee.core.properties;

/**
 * This is a factory for properties based on classes, which provides
 * the ability to serialize and deserialize them. 
 * @author ejs
 *
 */
public interface IClassPropertyFactory {

	/** Get the identifier used to serialize the object */
	String getId(Object value);
	/** Create an instance of the object identified by the id when deserializing the object */
	Object create(String id);
	/**
	 * 
	 */
	String[] getIds();

}
