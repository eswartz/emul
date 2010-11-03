/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;


/**
 * @author ejs
 * 
 */
public abstract class BaseHostBranch extends BaseWord implements Cloneable {

	protected int target;

	
	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public boolean isImmediate() {
		return false;
	}

	public abstract Object clone(); 
}
