/**
 * 
 */
package v9t9.emulator.hardware;

import java.util.List;

import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.DsrSettings;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.CpuMetrics;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.MemoryModel;
import v9t9.tools.asm.assembler.IInstructionFactory;

/**
 * The model for a machine, which controls how its hardware is fit together.
 * @author ejs
 *
 */
public interface MachineModel {
	String getIdentifier();
	
	Machine createMachine();

	MemoryModel getMemoryModel();
	
	VdpHandler createVdp(Machine machine);
	
	void defineDevices(Machine machine);

	SoundProvider createSoundProvider(Machine machine);
	
	Executor createExecutor(Cpu cpu, CpuMetrics metrics);

	IInstructionFactory getInstructionFactory();

	Cpu createCPU(Machine machine);

	List<DsrSettings> getDsrSettings(Machine machine);
}
