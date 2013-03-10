/*
  IFileHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.IOException;

import ejs.base.properties.IProperty;


/**
 * @author Ed
 *
 */
public interface IEmulatedFileHandler {
	/**
	 * Get a catalog for the given disk
	 * @param diskProperty
	 * @param isDiskImage
	 * @return
	 * @throws IOException
	 */
	Catalog createCatalog(IProperty diskProperty, boolean isDiskImage) throws IOException;
	/**
	 * Get the mapper for FIAD (files-in-a-directory) management
	 * @return
	 */
	IFilesInDirectoryMapper getFilesInDirectoryMapper();
	/**
	 * @return
	 */
	IDiskImageMapper getDiskImageMapper();
	/**
	 * @return
	 */
	IFileExecutionHandler getFileExecutionHandler();
}
