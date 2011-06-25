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
	
	public static LLVisibility getForToken(String t) {
		for (LLVisibility v : values()) {
			if (v.getVisibility().equals(t))
				return v;
		}
		throw new IllegalArgumentException(t);
	}
}
