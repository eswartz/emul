/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.ISemantics;
import v9t9.forthcomp.IWord;

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
