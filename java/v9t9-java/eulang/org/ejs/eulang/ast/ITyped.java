/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public interface ITyped {

	LLType getType();

	void setType(LLType type);

}