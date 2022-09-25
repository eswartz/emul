/*
  PabInfoBlock.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.directory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.files.DsrException;
import v9t9.common.files.DiskDirectory;
import v9t9.common.files.FileLikeDirectoryInfo;
import v9t9.common.files.IFilesInDirectoryMapper;
import v9t9.common.files.NativeFile;
import v9t9.common.files.PabConstants;

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

		pabfile = new OpenFile(nativefile, fileName);
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
	public void openDirectory(short pabaddr, File file, IFilesInDirectoryMapper mapper) throws DsrException {
		if (openFileCount >= maxOpenFileCount) {
			throw new DsrException(PabConstants.e_outofspace, null, "Too many open files");
		}
		DiskDirectory dir = new DiskDirectory(file, mapper);
		//FileLikeDirectoryInfo info = new FileLikeDirectoryInfo( file, mapper);
		openDirectories.put(pabaddr, dir.getInfo());
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