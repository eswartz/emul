package v9t9.engine.compiler;

import v9t9.engine.cpu.IExecutor;

public class NullCompilerStrategy implements ICompilerStrategy {

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#canCompile()
	 */
	@Override
	public boolean canCompile() {
		return false;
	}
	
	@Override
	public ICompiledCode getCompiledCode() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#setup(v9t9.emulator.runtime.cpu.Executor, v9t9.emulator.runtime.compiler.Compiler)
	 */
	@Override
	public void setup(IExecutor exec, CompilerBase compiler) {
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.compiler.ICompilerStrategy#reset()
	 */
	@Override
	public void reset() {
		
	}
}
