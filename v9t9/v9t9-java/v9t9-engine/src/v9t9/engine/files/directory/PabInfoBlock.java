/*
  PabInfoBlock.java

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
import java.util.HashMap;
import java.util.Map;

import v9t9.common.files.IFileMapper;
import v9t9.common.files.NativeFile;
import v9t9.engine.dsr.DsrException;
import v9t9.engine.dsr.PabConstants;

public class PabInfoBlock {
	private Map<Short, OpenFile> openFiles = new HashMap<Short, OpenFile>();
	private Map<Short, FileLikeDirectoryInfo> openDirectories = new HashMap<Short, FileLikeDirectoryInfo>();
	private int maxOpenFileCount;
	private int openFileCount;
	
	public PabInfoBlock() {
		reset();
	}
	
	/**
	 * 
	 */
	public void reset() {
		maxOpenFileCount = 3;
		openFileCount = 0;
		for (OpenFile file : openFiles.values())
			try {
				file.close();
			} catch (DsrException e) {
				e.printStackTrace();
			}
		openFiles.clear();
		openDirectories.clear();
	}

	protected OpenFile allocOpenFile(short pabaddr, NativeFile nativefile, String devName, String fileName) throws DsrException {
		OpenFile pabfile = openFiles.get(pabaddr);
		if (pabfile != null) {
			pabfile.close();
		} else {
			if (openFileCount >= maxOpenFileCount)
				throw new DsrException(PabConstants.e_outofspace, null, "Too many open files");
			openFileCount++;
		}

		pabfile = new OpenFile(nativefile, devName, fileName);
		openFiles.put(pabaddr, pabfile);
		return pabfile;
	}

	public OpenFile findOpenFile(short pabaddr) {
		return openFiles.get(pabaddr);
	}

	/**
	 * @param pabaddr
	 */
	public void removeOpenFile(short pabaddr) {
		openFiles.remove(pabaddr);
		openFileCount--;
	}

	/**
	 * @param pabaddr
	 * @param file
	 * @param dskName 
	 * @throws DsrException 
	 */
	public void openDirectory(short pabaddr, File file, IFileMapper mapper) throws DsrException {
		if (openFileCount >= maxOpenFileCount) {
			throw new DsrException(PabConstants.e_outofspace, null, "Too many open files");
		}
		FileLikeDirectoryInfo info = new FileLikeDirectoryInfo(file, mapper);
		openDirectories.put(pabaddr, info);
		openFileCount++;
	}

	/**
	 * @param pabaddr
	 */
	public void closeDirectory(short pabaddr) {
		openDirectories.remove(pabaddr);
		openFileCount--;
	}

	/**
	 * @param pabaddr
	 * @return
	 */
	public FileLikeDirectoryInfo getDirectory(short pabaddr) {
		return openDirectories.get(pabaddr);
	}

	/**
	 * @param openFiles the openFiles to set
	 */
	public void setOpenFiles(Map<Short, OpenFile> openFiles) {
		this.openFiles = openFiles;
	}

	/**
	 * @return the openFiles
	 */
	public Map<Short, OpenFile> getOpenFiles() {
		return openFiles;
	}

	/**
	 * @param maxOpenFileCount the maxOpenFileCount to set
	 */
	public void setMaxOpenFileCount(int maxOpenFileCount) {
		this.maxOpenFileCount = maxOpenFileCount;
	}

	/**
	 * @return the maxOpenFileCount
	 */
	public int getMaxOpenFileCount() {
		return maxOpenFileCount;
	}


}