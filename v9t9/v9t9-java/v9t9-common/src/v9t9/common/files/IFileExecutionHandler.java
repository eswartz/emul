/*
  IImageImportHandler.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import v9t9.common.machine.IMachine;


/**
 * This handles executing files (e.g. on a disk)
 * @author ejs
 *
 */
public interface IFileExecutionHandler {

	/**
	 * @param machine TODO
	 * @param driveNumber 
	 * @param catalog
	 * @return
	 */
	IFileExecutor[] analyze(IMachine machine, int driveNumber, Catalog catalog);
	
}
