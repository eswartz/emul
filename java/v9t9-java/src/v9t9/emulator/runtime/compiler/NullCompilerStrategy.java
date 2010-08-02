package v9t9.emulator.runtime.compiler;

import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.Executor;

public class NullCompilerStrategy implements ICompilerStrategy {

	@Override
	public ICompiledCode getCompiledCode(Cpu cpu) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#setExecutor(v9t9.emulator.runtime.cpu.Executor)
	 */
	@Override
	public void setExecutor(Executor exec) {
		
	}

}
