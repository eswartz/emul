/**
 * 
 */
package v9t9.emulator.hardware;

import java.util.List;

import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.common.IMachine;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.DsrSettings;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.CpuMetrics;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.IRawInstructionFactory;
import v9t9.engine.memory.MemoryModel;

/**
 * The model for a machine, which controls how its hardware is fit together.
 * @author ejs
 *
 */
public interface MachineModel {
	String getIdentifier();
	
	IMachine createMachine();

	MemoryModel getMemoryModel();
	
	VdpHandler createVdp(IMachine machine);
	
	void defineDevices(IMachine machine);

	SoundProvider createSoundProvider(IMachine machine);
	
	Executor createExecutor(Cpu cpu, CpuMetrics metrics);

	IRawInstructionFactory getInstructionFactory();

	Cpu createCPU(Machine machine);

	List<DsrSettings> getDsrSettings(IMachine machine);

	List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine);
}
