/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public interface IWord {

	void execute(HostContext hostContext, TargetContext targetContext) throws AbortException;

	boolean isImmediate();

}
