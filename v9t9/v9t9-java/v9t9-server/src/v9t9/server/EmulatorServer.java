package v9t9.server;

import v9t9.client.EmulatorClientBase;
import v9t9.common.machine.IMachineModel;
import v9t9.machine.f99b.machine.F99bMachineModel;
import v9t9.machine.ti99.machine.Enhanced48KForthTI994AMachineModel;
import v9t9.machine.ti99.machine.EnhancedTI994AMachineModel;
import v9t9.machine.ti99.machine.StandardMachineModel;

public class EmulatorServer extends EmulatorClientBase {

	static {
		MachineModelFactory.INSTANCE.register(
				StandardMachineModel.ID, StandardMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				EnhancedTI994AMachineModel.ID, EnhancedTI994AMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				Enhanced48KForthTI994AMachineModel.ID, Enhanced48KForthTI994AMachineModel.class);
		MachineModelFactory.INSTANCE.register(
				F99bMachineModel.ID, F99bMachineModel.class);
	}
	
    public EmulatorServer() {
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

	/**
	 * @return
	 */
	public MachineModelFactory getMachineModelFactory() {
		return MachineModelFactory.INSTANCE;
	}

}

