package v9t9.engine.interpreter;

import v9t9.common.cpu.IExecutor;
import v9t9.engine.cpu.Executor;

public interface IInterpreter {
	/**
	 * Execute a chunk of instructions as quickly as possible, watching for
	 * {@link Executor#interruptExecution} and updating {@link Executor#nInstructions}
	 */
	void executeChunk(int numinsts, IExecutor executor);

	void dispose();
}