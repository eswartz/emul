/**
 * 
 */
package v9t9.tools.forthcomp;

import v9t9.tools.forthcomp.words.TargetContext;

/**
 * 
 * @author ejs
 *
 */
public interface ISemantics {

	void execute(HostContext hostContext, TargetContext targetContext) throws AbortException;

}
