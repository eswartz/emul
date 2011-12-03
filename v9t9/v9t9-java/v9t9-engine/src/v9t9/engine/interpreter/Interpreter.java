package v9t9.engine.interpreter;

import v9t9.engine.cpu.Executor;

public interface Interpreter {
	/**
	 * Execute a chunk of instructions as quickly as possible, watching for
	 * {@link Executor#interruptExecution} and updating {@link Executor#nInstructions}
	 */
	void executeChunk(int numinsts, Executor executor);

	void dispose();
}