package v9t9.emulator.runtime.interpreter;

import v9t9.emulator.runtime.cpu.Executor;

public interface Interpreter {
	/**
	 * Execute a chunk of instructions as quickly as possible, watching for
	 * {@link Executor#interruptExecution} and updating {@link Executor#nInstructions}
	 */
	void executeChunk(int numinsts, Executor executor);

}