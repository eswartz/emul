/**
 * 
 */
package v9t9.emulator.hardware.dsrs.emudisk;

import java.io.File;
import java.util.Arrays;

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