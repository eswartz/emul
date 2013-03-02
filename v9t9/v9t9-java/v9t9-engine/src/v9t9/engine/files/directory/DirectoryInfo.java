/*
  DirectoryInfo.java

  (c) 2010-2012 Edward Swartz

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