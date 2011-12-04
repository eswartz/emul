/**
 * 
 */
package v9t9.engine.compiler;

import v9t9.engine.cpu.IExecutor;

/**
 * Strategy for compiling code
 * @author ejs
 *
 */
public interface ICompilerStrategy {
	boolean canCompile();
	
	/** 
	 * Fetch or compile code that can handle the code at the CPU's state
	 * @return CodeBlock which can be executed, or <code>null</code>
	 */
	ICompiledCode getCompiledCode();
	
	void setup(IExecutor exec, CompilerBase compiler);

	/**
	 * 
	 */
	void reset();
}
