/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import org.ejs.v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public abstract class BaseWord implements IWord {

	private String name;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName()+" ("+name+")";
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#getName()
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
	

}
