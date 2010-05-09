/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public interface LLAggregateType extends LLType {
	/** tell if the type is a stub (e.g. code block or data block with no known content) */
	boolean isAbstract();
}
