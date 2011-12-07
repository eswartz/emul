/**
 * 
 */
package v9t9.client;

import v9t9.common.machine.IMachineModel;

/**
 * @author ejs
 *
 */
public class EmulatorClient extends EmulatorClientBase {

	public EmulatorClient() {
	}

	/* (non-Javadoc)
	 * @see v9t9.client.EmulatorClientBase#createModel(java.lang.String)
	 */
	@Override
	protected IMachineModel createModel(String modelId) {
		return new MachineModelProxy(modelId);
	}

}
