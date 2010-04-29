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
}
