/*
  BaseWord.java

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
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public abstract class BaseWord implements IWord {

	private String name;
	protected ISemantics compileSemantics;
	protected ISemantics interpretSemantics;
	protected ISemantics executionSemantics;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName()+" ("+name+")";
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getEntry()
	 */
	public DictEntry getEntry() {
		return null;
	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getCompileSemantics()
	 */
	public ISemantics getCompilationSemantics() {
		return compileSemantics;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getInterpretSemantics()
	 */
	public ISemantics getInterpretationSemantics() {
		return interpretSemantics;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#getRuntimeSemantics()
	 */
	public ISemantics getExecutionSemantics() {
		return executionSemantics;
	}
	public void setCompilationSemantics(ISemantics compileSemantics) {
		this.compileSemantics = compileSemantics;
	}
	public void setInterpretationSemantics(ISemantics interpretSemantics) {
		this.interpretSemantics = interpretSemantics;
	}
	public void setExecutionSemantics(ISemantics executionSemantics) {
		this.executionSemantics = executionSemantics;
		if (interpretSemantics == null)
			interpretSemantics = executionSemantics;
	}

	@Override
	public boolean isCompilerWord() {
		return false;
	}
}
