/**
 * 
 */
package org.ejs.v9t9.forthcomp.words;

import java.io.File;
import java.io.FileNotFoundException;

import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class Include extends BaseWord {
	public Include() {
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
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
		});
		setCompilationSemantics(getExecutionSemantics());
	}
}
