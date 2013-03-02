/*
  IInterpreter.java

  (c) 2005-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
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