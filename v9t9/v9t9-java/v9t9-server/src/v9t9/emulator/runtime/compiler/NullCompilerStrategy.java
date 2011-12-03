package v9t9.emulator.runtime.compiler;

import v9t9.emulator.runtime.cpu.Executor;

public class NullCompilerStrategy implements ICompilerStrategy {

	@Override
	public ICompiledCode getCompiledCode() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#setup(v9t9.emulator.runtime.cpu.Executor, v9t9.emulator.runtime.compiler.Compiler)
	 */
	@Override
	public void setup(Executor exec, CompilerBase compiler) {
		
	}
}
