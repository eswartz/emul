/*
  FileDirectory.java

  (c) 2011-2012 Edward Swartz

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

import v9t9.common.files.Catalog;
import v9t9.common.files.IFileMapper;

/**
 * @author ejs
 *
 */
public class FileDirectory {

	private IFileMapper mapper;
	private File dir;

	/**
	 * @param dir
	 * @param mapper
	 */
	public FileDirectory(File file, IFileMapper mapper) {
		this.dir = file.isDirectory() ? file : file.getParentFile();
		this.mapper = mapper;
	}

	/**
	 * @return
	 */
	public Catalog readCatalog() {
		FileLikeDirectoryInfo info = new FileLikeDirectoryInfo(dir, mapper);
		long total = dir.getTotalSpace();
		long used = total - dir.getFreeSpace();
		return new Catalog(
				mapper.getDsrDeviceName(dir),
				dir.getName().toUpperCase(), (int)(total / 256) & 0xffff, 
				(int)((total - used) / 256) & 0xffff,
				info.readCatalog());
	}

	
}
