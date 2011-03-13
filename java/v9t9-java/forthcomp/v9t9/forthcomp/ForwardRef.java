/**
 * 
 */
package v9t9.forthcomp;

import java.util.ArrayList;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;

import v9t9.forthcomp.words.TargetContext;
import v9t9.forthcomp.words.TargetWord;

/**
 * @author ejs
 *
 */
public class ForwardRef extends TargetWord {

	private int id;
	private final String location;

	/**
	 * @param id 
	 * @param i
	 */
	public ForwardRef(String name, String location, int id) {
		super(new DictEntry(0, id, name));
		this.location = location;
		this.id = id;
		
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				throw hostContext.abort("cannot invoke forward referenced word: " + ForwardRef.this.toString());
			}
		});
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
	
}
