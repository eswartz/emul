/**
 * 
 */
package v9t9.client;

import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryModel;

/**
 * @author ejs
 *
 */
public class MachineModelProxy implements IMachineModel {

	/**
	 * @param modelId
	 */
	public MachineModelProxy(String modelId) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createMachine(v9t9.common.client.ISettingsHandler)
	 */
	@Override
	public IMachine createMachine(ISettingsHandler settings) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createMemoryModel(v9t9.common.machine.IMachine)
	 */
	@Override
	public IMemoryModel createMemoryModel(IMachine machine) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createVdp(v9t9.common.machine.IMachine)
	 */
	@Override
	public IVdpChip createVdp(IMachine machine) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#defineDevices(v9t9.common.machine.IMachine)
	 */
	@Override
	public void defineDevices(IMachine machine) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createSoundChip(v9t9.common.machine.IMachine)
	 */
	@Override
	public ISoundChip createSoundChip(IMachine machine) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createSpeechChip(v9t9.common.machine.IMachine)
	 */
	@Override
	public ISpeechChip createSpeechChip(IMachine machine) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createCPU(v9t9.common.machine.IMachine)
	 */
	@Override
	public ICpu createCPU(IMachine machine) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getDeviceSettings(v9t9.common.machine.IMachine)
	 */
	@Override
	public List<IDeviceSettings> getDeviceSettings(IMachine machine) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getDeviceIndicatorProviders(v9t9.common.machine.IMachine)
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(
			IMachine machine) {
		// TODO Auto-generated method stub
		return null;
	}

}
