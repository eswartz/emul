/**
 * 
 */
package v9t9.common.cpu;

/**
 * Element of a {@link ChangeBlock}.  This may be a fetch, store,
 * or other CPU-specific operation, which can be executed (interpreted)
 * to emulate an instruction.  
 * @author ejs
 *
 */
public interface IChangeElement {
	void apply(ICpuState cpuState);
	void revert(ICpuState cpuState);
}
