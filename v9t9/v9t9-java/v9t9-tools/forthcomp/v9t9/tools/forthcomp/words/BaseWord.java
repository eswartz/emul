/*
  BaseWord.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetContext;
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

	protected String parseString(HostContext hostContext)
			throws AbortException {
		
		StringBuilder sb = new StringBuilder();
		while (true) {
			char ch = hostContext.getStream().readChar();
			if (ch == 0)
				throw hostContext.abort("unterminated string at " + sb);
			if (ch == '"')
				return sb.toString();
			sb.append(ch);
		}
	}

	protected String popString(HostContext hostContext, ITargetContext targetContext) {
		int leng = hostContext.popData();
		int addr = hostContext.popData();
		StringBuilder sb = new StringBuilder();
		while (leng-- > 0)
			sb.append((char) targetContext.readChar(addr++));
		return sb.toString();
	}
}
