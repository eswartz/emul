/**
 * 
 */
package v9t9.common.cpu;

/**
 * @author ejs
 *
 */
public interface IBreakpointListener {

	void breakpointChanged(IBreakpoint bp, boolean added);
}
