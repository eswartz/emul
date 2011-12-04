/**
 * 
 */
package v9t9.tools.forthcomp.words;



/**
 * @author ejs
 * 
 */
public abstract class BaseHostBranch extends BaseStdWord implements Cloneable {

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
