/**
 * 
 */
package org.ejs.eulang.llvm;

/**
 * @author ejs
 *
 */
public enum LLLinkage {
	INTERNAL("internal"),
	PRIVATE("private"), 
	APPENDING("appending"), 
	LINKONCE_ODR("linkonce_odr");
	
	private final String dname;

	/**
	 * 
	 */
	private LLLinkage(String name) {
		this.dname = name;
	}
	
	/**
	 * @return the dname
	 */
	public String getLinkageName() {
		return dname;
	}
	
	public static LLLinkage getForToken(String t) {
		for (LLLinkage l : values()) {
			if (l.getLinkageName().equals(t))
				return l;
		}
		throw new IllegalArgumentException(t);
	}
}
