/**
 * 
 */
package org.ejs.eulang.types;

/**
 * @author ejs
 *
 */
public class LLLabelType extends BaseLLType {

	public LLLabelType() {
		super("__label", 0, "label", BasicType.CODE, null);
	}
	@Override
	public boolean isComplete() {
		return true;
	}

}
