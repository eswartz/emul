/**
 * 
 */
package v9t9.common.compiler;

/**
 * @author ejs
 *
 */
public interface ICompiledCode {
	 /**
     * Using the current CPU state, run any number of instructions, 
     * and save away changed CPU state before return.
     * 
     * @return true if code exited normally (i.e. max # instructions invoked, 
     * or jumped outside its own block), false if exec.cpu.PC refers to
     * instruction that must be emulated. 
     */
	boolean run();
}
