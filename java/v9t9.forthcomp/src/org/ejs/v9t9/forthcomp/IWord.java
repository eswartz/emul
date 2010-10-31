/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import org.ejs.v9t9.forthcomp.words.TargetContext;

/**
 * @author ejs
 *
 */
public interface IWord {

	void execute(HostContext hostContext, TargetContext targetContext) throws AbortException;

	boolean isImmediate();

}
