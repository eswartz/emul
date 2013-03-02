/*
  IBreakpoint.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

/**
 * @author ejs
 *
 */
public interface IBreakpoint {
	/**
	 * Get address of breakpoint
	 * @return
	 */
	int getPc();

	/**
	 * Execute the breakpoint action
	 * @param cpu
	 * @return true to keep running, false to stop 
	 */
	boolean execute(ICpuState cpu);


	/**
	 * @return
	 */
	boolean isCompleted();

}
