/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.util.ArrayList;

import org.ejs.v9t9.forthcomp.words.TargetContext;
import org.ejs.v9t9.forthcomp.words.TargetWord;

/**
 * @author ejs
 *
 */
public class ForwardRef extends TargetWord {

	private ArrayList<Integer> fwds;
	private int id;
	private final String location;

	/**
	 * @param id 
	 * @param i
	 */
	public ForwardRef(String name, String location, int id) {
		super(new DictEntry(0, id, name));
		this.location = location;
		fwds = new ArrayList<Integer>();
		this.id = id;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		throw hostContext.abort("cannot invoke forward referenced word");
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}

	/**
	 * @param num
	 */
	public void add(int num) {
		fwds.add(num);		
	}

	/**
	 * @return the fwds
	 */
	public ArrayList<Integer> getFwds() {
		return fwds;
	}
}
