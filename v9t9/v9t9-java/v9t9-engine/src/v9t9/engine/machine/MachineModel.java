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
import v9t9.engine.hardware.ISoundChip;
import v9t9.engine.hardware.ISpeechChip;
import v9t9.engine.hardware.IVdpChip;

/**
 * The model for a machine, which controls how its hardware is fit together.
 * @author ejs
 *
 */
public interface MachineModel {
	String getIdentifier();
	
	IMachine createMachine();

	MemoryModel getMemoryModel();
	
	IVdpChip createVdp(IMachine machine);
	
	void defineDevices(IMachine machine);

	ISoundChip createSoundChip(IMachine machine);
	
	ISpeechChip createSpeechChip(IMachine machine);
	
	Executor createExecutor(ICpu cpu, ICpuMetrics metrics);

	IRawInstructionFactory getInstructionFactory();

	ICpu createCPU(Machine machine);

	List<IDsrSettings> getDsrSettings(IMachine machine);

	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine);

}
