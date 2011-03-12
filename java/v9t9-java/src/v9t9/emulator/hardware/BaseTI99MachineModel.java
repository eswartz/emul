/**
 * 
 */
package v9t9.emulator.hardware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import v9t9.emulator.clients.builtin.swt.IDeviceIndicatorProvider;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.dsrs.DsrHandler;
import v9t9.emulator.hardware.dsrs.DsrSettings;
import v9t9.emulator.runtime.compiler.CodeBlockCompilerStrategy;
import v9t9.emulator.runtime.cpu.*;
import v9t9.emulator.runtime.interpreter.Interpreter9900;
import v9t9.tools.asm.assembler.*;

/**
 * @author ejs
 *
 */
public abstract class BaseTI99MachineModel implements MachineModel {

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getCPU()
	 */
	@Override
	public Cpu createCPU(Machine machine) {
		return new Cpu9900(machine, 1000 / machine.getCpuTicksPerSec(), machine.getVdp());
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getInstructionFactory()
	 */
	@Override
	public IInstructionFactory getInstructionFactory() {
		return InstructionFactory9900.INSTANCE;
	}

	@Override
	public Executor createExecutor(Cpu cpu, CpuMetrics metrics) {
		return new Executor(cpu, metrics, 
				new Interpreter9900((TI99Machine) cpu.getMachine()),
				new CodeBlockCompilerStrategy(),
				new DumpFullReporter9900((Cpu9900) cpu),
				new DumpReporter9900((Cpu9900) cpu));
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDsrSettings()
	 */
	@Override
	public List<DsrSettings> getDsrSettings(Machine machine) {
		List<DsrSettings> settings = new ArrayList<DsrSettings>();
		if (machine.getDsrManager() != null)  {
			for (DsrHandler handler : machine.getDsrManager().getDsrs()) {
				settings.add(handler);
			}
		}
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(Machine machine) {
		List<IDeviceIndicatorProvider> list = new ArrayList<IDeviceIndicatorProvider>();
		if (machine.getDsrManager() != null)  {
			for (DsrHandler handler : machine.getDsrManager().getDsrs()) {
				list.addAll(handler.getDeviceIndicatorProviders());
			}
		}
		return list;
	}

}