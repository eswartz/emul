/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public interface LLAggregateType extends LLType {
	/** tell if the type is a stub (e.g. code block or data block with no known content) */
	boolean isAbstract();
}
