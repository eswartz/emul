/**
 * 
 */
package org.ejs.eulang;

/**
 * @author ejs
 *
 */
public class TargetV9t9 implements ITarget {

	private TypeEngine typeEngine = new TypeEngine();

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#createTypeEngine()
	 */
	@Override
	public TypeEngine getTypeEngine() {
		return typeEngine;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getTriple()
	 */
	@Override
	public String getTriple() {
		return "9900-unknown-v9t9";
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#getLLCallingConvention()
	 */
	@Override
	public String getLLCallingConvention() {
		return "cc100";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ITarget#moveLocalsToTemps()
	 */
	@Override
	public boolean moveLocalsToTemps() {
		return true;
	}

}
