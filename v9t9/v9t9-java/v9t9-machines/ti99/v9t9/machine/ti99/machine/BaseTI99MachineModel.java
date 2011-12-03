/**
 * 
 */
package v9t9.machine.ti99.machine;

import java.util.ArrayList;
import java.util.List;

import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.engine.compiler.CodeBlockCompilerStrategy;
import v9t9.engine.cpu.Executor;
import v9t9.engine.dsr.IDeviceIndicatorProvider;
import v9t9.engine.dsr.IDsrHandler;
import v9t9.engine.dsr.IDsrSettings;
import v9t9.engine.machine.IMachine;
import v9t9.engine.machine.Machine;
import v9t9.engine.machine.MachineModel;
import v9t9.machine.ti99.asm.RawInstructionFactory9900;
import v9t9.machine.ti99.compiler.Compiler9900;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.DumpFullReporter9900;
import v9t9.machine.ti99.cpu.DumpReporter9900;
import v9t9.machine.ti99.interpreter.Interpreter9900;

/**
 * @author ejs
 *
 */
public abstract class BaseTI99MachineModel implements MachineModel {

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getCPU()
	 */
	@Override
	public ICpu createCPU(Machine machine) {
		return new Cpu9900(machine, 1000 / machine.getCpuTicksPerSec(), machine.getVdp());
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getInstructionFactory()
	 */
	@Override
	public IRawInstructionFactory getInstructionFactory() {
		return RawInstructionFactory9900.INSTANCE;
	}

	@Override
	public Executor createExecutor(ICpu cpu, ICpuMetrics metrics) {
		return new Executor(cpu, metrics, 
				new Interpreter9900((TI99Machine) cpu.getMachine()),
				new Compiler9900((Cpu9900) cpu),
				new CodeBlockCompilerStrategy(),
				new DumpFullReporter9900((Cpu9900) cpu), new DumpReporter9900((Cpu9900) cpu));
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDsrSettings()
	 */
	@Override
	public List<IDsrSettings> getDsrSettings(IMachine machine) {
		List<IDsrSettings> settings = new ArrayList<IDsrSettings>();
		if (machine.getDsrManager() != null)  {
			for (IDsrHandler handler : machine.getDsrManager().getDsrs()) {
				settings.add(handler);
			}
		}
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine) {
		List<IDeviceIndicatorProvider> list = new ArrayList<IDeviceIndicatorProvider>();
		if (machine.getDsrManager() != null)  {
			for (IDsrHandler handler : machine.getDsrManager().getDsrs()) {
				list.addAll(handler.getDeviceIndicatorProviders());
			}
		}
		return list;
	}
}