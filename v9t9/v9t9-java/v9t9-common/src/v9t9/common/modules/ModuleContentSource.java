/**
 * 
 */
package v9t9.common.modules;

import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class ModuleContentSource implements IEmulatorContentSource {

	private IMachine machine;
	private IModule module;

	/**
	 * @param machine
	 * @param module
	 */
	public ModuleContentSource(IMachine machine, IModule module) {
		this.machine = machine;
		this.module = module;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSource#getMachine()
	 */
	@Override
	public IMachine getMachine() {
		return machine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSource#getContent()
	 */
	@Override
	public IModule getContent() {
		return module;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSource#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Module '" + module.getName() + "'";
	}
}
