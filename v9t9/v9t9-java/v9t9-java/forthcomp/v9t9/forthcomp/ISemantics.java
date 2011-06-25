/**
 * 
 */
package v9t9.forthcomp;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.HostContext;

import v9t9.forthcomp.words.TargetContext;

/**
 * 
 * @author ejs
 *
 */
public interface ISemantics {

	void execute(HostContext hostContext, TargetContext targetContext) throws AbortException;

}
