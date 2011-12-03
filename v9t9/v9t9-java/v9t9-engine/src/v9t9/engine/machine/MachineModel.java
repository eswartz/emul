/**
 * 
 */
package v9t9.engine.machine;

import java.util.List;

import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.memory.MemoryModel;
import v9t9.engine.cpu.Executor;
import v9t9.engine.dsr.IDeviceIndicatorProvider;
import v9t9.engine.dsr.IDsrSettings;
import v9t9.engine.hardware.SoundChip;
import v9t9.engine.hardware.VdpChip;

/**
 * The model for a machine, which controls how its hardware is fit together.
 * @author ejs
 *
 */
public interface MachineModel {
	String getIdentifier();
	
	IMachine createMachine();

	MemoryModel getMemoryModel();
	
	VdpChip createVdp(IMachine machine);
	
	void defineDevices(IMachine machine);

	SoundChip createSoundProvider(IMachine machine);
	
	Executor createExecutor(ICpu cpu, ICpuMetrics metrics);

	IRawInstructionFactory getInstructionFactory();

	ICpu createCPU(Machine machine);

	List<IDsrSettings> getDsrSettings(IMachine machine);

	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine);
}
