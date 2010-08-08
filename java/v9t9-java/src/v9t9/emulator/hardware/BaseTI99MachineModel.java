/**
 * 
 */
package v9t9.emulator.hardware;

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

}