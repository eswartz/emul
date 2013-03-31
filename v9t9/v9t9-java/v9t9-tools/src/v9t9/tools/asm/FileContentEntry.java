/*
  FileContentEntry.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * A single file to parse.
 * @author ejs
 *
 */
public class FileContentEntry extends ContentEntry {
	public FileContentEntry(File file) throws IOException {
		super(file.getPath(),
				new LineNumberReader(new InputStreamReader(
						new FileInputStream(file))));
	}

	public File getFile() {
		return new File(name);
	}
}
