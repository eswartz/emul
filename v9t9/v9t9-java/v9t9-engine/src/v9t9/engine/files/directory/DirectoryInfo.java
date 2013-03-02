/*
  DirectoryInfo.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.directory;

import java.io.File;
import java.util.Arrays;

import v9t9.common.files.IFileMapper;

class DirectoryInfo {

	protected File[] entries;
	protected final IFileMapper mapper;
	protected File dir;

	public DirectoryInfo(File file, IFileMapper mapper) {
		this.mapper = mapper;
		
		this.dir = file;
		this.entries = file != null ? file.listFiles() : new File[0];
		if (entries == null)
			entries = new File[0];
		else
			Arrays.sort(entries);
	}

}