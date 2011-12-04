/**
 * 
 */
package v9t9.tools.forthcomp;

import v9t9.tools.forthcomp.words.TargetContext;
import v9t9.tools.forthcomp.words.TargetWord;

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
