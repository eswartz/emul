/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public interface LLAggregateType extends LLType {
	int getCount();
	LLType[] getTypes();

	LLType getType(int idx);
	LLAggregateType updateTypes(LLType[] type);
}
