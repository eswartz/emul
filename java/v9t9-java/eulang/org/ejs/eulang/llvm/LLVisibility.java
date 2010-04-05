/**
 * 
 */
package org.ejs.eulang.llvm;

/**
 * @author ejs
 *
 */
public enum LLVisibility {
	DEFAULT("default"),
	HIDDEN("hidden"),
	PROTECTED("protected");
	
	private String visName;

	/**
	 * 
	 */
	private LLVisibility(String name) {
		this.visName = name;
	}
	
	/**
	 * @return the visName
	 */
	public String getVisibility() {
		return visName;
	}
}
