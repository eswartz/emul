/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import java.io.File;
import java.io.FileNotFoundException;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.IWord;

/**
 * @author ejs
 *
 */
public class Include implements IWord {
	public Include() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String filename = hostContext.readToken();
		
		try {
			File dir = new File(hostContext.getStream().getFile()).getParentFile();
			File file = new File(dir, filename);
			if (file.exists())
				hostContext.getStream().push(file);
			else
				hostContext.getStream().push(new File(filename));
		} catch (FileNotFoundException e) {
			throw hostContext.abort(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
