/**
 * 
 */
package v9t9.common.cpu;

/**
 * @author ejs
 *
 */
public interface IBreakpoint {
	/**
	 * Get address of breakpoint
	 * @return
	 */
	int getPc();

	/**
	 * Execute the breakpoint action
	 * @param cpu
	 * @return true to keep running, false to stop 
	 */
	boolean execute(ICpuState cpu);


	/**
	 * @return
	 */
	boolean isCompleted();

}
