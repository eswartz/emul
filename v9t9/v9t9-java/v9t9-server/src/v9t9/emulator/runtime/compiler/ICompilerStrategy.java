/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import v9t9.emulator.runtime.cpu.Executor;

/**
 * Strategy for compiling code
 * @author ejs
 *
 */
public interface ICompilerStrategy {
	/** 
	 * Fetch or compile code that can handle the code at the CPU's state
	 * @return CodeBlock which can be executed, or <code>null</code>
	 */
	ICompiledCode getCompiledCode();
	
	void setup(Executor exec, CompilerBase compiler);
}
