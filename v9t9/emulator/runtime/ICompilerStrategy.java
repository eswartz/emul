/**
 * 
 */
package v9t9.emulator.runtime;

/**
 * Strategy for compiling code
 * @author ejs
 *
 */
public interface ICompilerStrategy {
	/** 
	 * Fetch or compile code that can handle this PC and WP
	 * @param pc the PC to target
	 * @param wp the workspace pointer
	 * @return CodeBlock which can be executed, or <code>null</code>
	 */
	ICompiledCode getCompiledCode(int pc, short wp);
}
