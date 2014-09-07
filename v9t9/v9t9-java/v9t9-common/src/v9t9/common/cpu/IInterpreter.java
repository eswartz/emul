/*
  IInterpreter.java

  (c) 2005-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

public interface IInterpreter {
	/** Reset any cached state */ 
	void reset();
	/** Remove any listeners on the CPU or memory */
	void dispose();
	/** Execute instruction(s) and apply changes to the CPU
	 * @param maxCycles maximum number of cycles to attempt
	 */
	void execute(int maxCycles);
	/** 
	 * Determine how one instruction would be executed and return the
	 * changes in the given change block.  Does not affect CPU or memory at all.
	 */
	ChangeBlock simulate();
}