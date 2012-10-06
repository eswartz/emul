/**
 * 
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
		return new Catalog(dir.getName().toUpperCase(), (int)(total / 256) & 0xffff, 
				(int)((total - used) / 256) & 0xffff,
				info.readCatalog());
	}

	
}
