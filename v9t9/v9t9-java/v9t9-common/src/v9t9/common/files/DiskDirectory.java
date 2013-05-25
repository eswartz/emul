/*
  FileDirectory.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;

/**
 * @author ejs
 *
 */
public class DiskDirectory implements IDiskDirectory {

	private IFilesInDirectoryMapper mapper;
	private File dir;

	/**
	 * @param dir
	 * @param mapper
	 */
	public DiskDirectory(File file, IFilesInDirectoryMapper mapper) {
		this.dir = file.isDirectory() ? file : file.getParentFile();
		this.mapper = mapper;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#getPath()
	 */
	@Override
	public String getPath() {
		return dir.getPath();
	}

	/**
	 * @return
	 */
	public Catalog readCatalog() {
		FileLikeDirectoryInfo info = new FileLikeDirectoryInfo(this, dir, mapper);
		long total = dir.getTotalSpace();
		long used = total - dir.getFreeSpace();
		return new Catalog(
				this,
				mapper.getDsrDeviceName(dir),
				dir.getName().toUpperCase(), (int)(total / 256) & 0xffff, 
				(int)((total - used) / 256) & 0xffff,
				info.readCatalog());
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#getFile(java.lang.String)
	 */
	@Override
	public IEmulatedFile getFile(String name) throws IOException {
		return NativeFileFactory.INSTANCE.createNativeFile(this,  
				new File(dir, name)); 
						//mapper.getLocalFileName(name)));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#createFile(java.lang.String, int, int)
	 */
	@Override
	public IEmulatedFile createFile(String fileName, FDR srcFdr) throws IOException {
		
		NativeFile file = null;
		
		File localFile = mapper.getLocalFile(dir, fileName);
		
		// write FDR info (or create file)
		file = new NativeFDRFile(localFile, srcFdr instanceof V9t9FDR ? new V9t9FDR() : new TIFILESFDR());
			
		if (srcFdr != null && file instanceof EmulatedBaseFDRFile) {
			EmulatedBaseFDRFile fdrFile = (EmulatedBaseFDRFile) file;
			FDR fdr = fdrFile.getFDR();
			fdr.copyFrom(srcFdr);
			fdr.writeFDR(file.getFile());
			file.flush();
		}
		
			
		return file;
	}

	/**
	 * @return
	 */
	public FileLikeDirectoryInfo getInfo() {
		return new FileLikeDirectoryInfo(this, dir, mapper);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#isFormatted()
	 */
	@Override
	public boolean isFormatted() {
		return dir.exists();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IEmulatedDisk#isValid()
	 */
	@Override
	public boolean isValid() {
		return true;
	}
}
