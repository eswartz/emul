/*
  Include.java

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

import java.io.File;
import java.io.FileNotFoundException;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;

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
