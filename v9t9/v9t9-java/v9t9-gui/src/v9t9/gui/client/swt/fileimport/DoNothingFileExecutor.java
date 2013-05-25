/*
  DoNothingFileExecutor.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.fileimport;

import v9t9.common.events.NotifyException;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IEmulatedDisk;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class DoNothingFileExecutor implements IFileExecutor {

	private IEmulatedDisk disk;
	private String label;

	public DoNothingFileExecutor(IEmulatedDisk disk, String label) {
		this.disk = disk;
		this.label = label;
		
	}
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Change the current disk mapping to point to this " + (disk instanceof IDiskImage ? "image" : "directory");
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(IMachine machine) throws NotifyException {
		
	}
}
