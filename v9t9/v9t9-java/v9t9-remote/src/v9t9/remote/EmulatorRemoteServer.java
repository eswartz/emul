/**
 * 
 */
package v9t9.remote;

import v9t9.common.machine.IMachineModel;
import v9t9.server.client.EmulatorServerBase;

/**
 * @author ejs
 *
 */
public class EmulatorRemoteServer extends EmulatorServerBase {

	final String addr;

	public EmulatorRemoteServer(String addr) {
		this.addr = addr;
	}

	/* (non-Javadoc)
	 * @see v9t9.client.EmulatorClientBase#createModel(java.lang.String)
	 */
	@Override
	protected IMachineModel createModel(String modelId) {
		return null;
	}

}
