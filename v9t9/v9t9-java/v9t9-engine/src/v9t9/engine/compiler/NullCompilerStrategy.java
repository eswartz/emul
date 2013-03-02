/*
  NullCompilerStrategy.java

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.compiler;

import v9t9.common.compiler.ICompiledCode;
import v9t9.common.compiler.ICompiler;
import v9t9.common.compiler.ICompilerStrategy;
import v9t9.common.cpu.IExecutor;

public class NullCompilerStrategy implements ICompilerStrategy {

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#canCompile()
	 */
	@Override
	public boolean canCompile() {
		return false;
	}
	
	@Override
	public ICompiledCode getCompiledCode() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#setup(v9t9.emulator.runtime.cpu.Executor, v9t9.emulator.runtime.compiler.Compiler)
	 */
	@Override
	public void setup(IExecutor exec, ICompiler compiler) {
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.compiler.ICompilerStrategy#reset()
	 */
	@Override
	public void reset() {
		
	}
}
