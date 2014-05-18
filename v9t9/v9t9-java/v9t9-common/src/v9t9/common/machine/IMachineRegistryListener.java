/**
 * 
 */
package v9t9.common.machine;

/**
 * @author ejs
 *
 */
public interface IMachineRegistryListener {

	void machineAdded(IMachine machine);
	void machineRemoved(IMachine machine);
}
