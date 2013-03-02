/*
  ICompilerStrategy.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.compiler;

import v9t9.common.cpu.IExecutor;

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
	
	void setup(IExecutor exec, ICompiler compiler);

	/**
	 * 
	 */
	void reset();
}
