/**
 * 
 */
package v9t9.common.demos;

import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class DemoContentSource implements IEmulatorContentSource {

	private IMachine machine;
	private IDemo demo;

	/**
	 * 
	 */
	public DemoContentSource(IMachine machine, IDemo demo) {
		this.machine = machine;
		this.demo = demo;
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
	public IDemo getContent() {
		return demo;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSource#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Recorded demonstration '" + demo.getName() + "'";
	}
}
