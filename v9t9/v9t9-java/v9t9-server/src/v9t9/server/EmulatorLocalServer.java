package v9t9.server;

import v9t9.common.machine.IMachineModel;
import v9t9.server.client.EmulatorServerBase;

public class EmulatorLocalServer extends EmulatorServerBase {

    public EmulatorLocalServer() {
    }
    
    /* (non-Javadoc)
     * @see v9t9.client.EmulatorClientBase#createModel(java.lang.String)
     */
    @Override
    protected IMachineModel createModel(String modelId) {
		
        IMachineModel model = MachineModelFactory.INSTANCE.createModel(modelId);
        assert (model != null);

    	return model;
    }

}

