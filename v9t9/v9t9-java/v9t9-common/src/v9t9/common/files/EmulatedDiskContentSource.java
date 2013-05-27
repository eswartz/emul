/*
  EmulatedDiskContentSource.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import ejs.base.properties.IProperty;
import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class EmulatedDiskContentSource implements IEmulatorContentSource {

	private IMachine machine;
	private IEmulatedDisk disk;
	private Catalog catalog;
	private IProperty diskImageProperty;
	private int drive;
	private String device;

	public EmulatedDiskContentSource(IMachine machine, IEmulatedDisk disk,
			Catalog catalog, IProperty diskImageProperty) {
		this.machine = machine;
		this.disk = disk;
		this.catalog = catalog;
		
		drive = 1;
		if (diskImageProperty != null) 
			drive = ((IDiskDriveSetting) diskImageProperty).getDrive();
		device = "DSK" + drive;
		
		this.diskImageProperty = diskImageProperty;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSource#getMachine()
	 */
	@Override
	public IMachine getMachine() {
		return machine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSource#getContent()
	 */
	@Override
	public IEmulatedDisk getContent() {
		return disk;
	}
	
	/**
	 * @return the catalog
	 */
	public Catalog getCatalog() {
		return catalog;
	}
	
	/**
	 * @return the diskImageProperty
	 */
	public IProperty getDiskImageProperty() {
		return diskImageProperty;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSource#getLabel()
	 */
	@Override
	public String getLabel() {
		if (disk instanceof IDiskImage)
			return "Disk image '" + catalog.volumeName.trim() + "' for " + device;
		else
			return "Disk directory '" + catalog.volumeName.trim() + "' for " + device;
	}

	/**
	 * @return
	 */
	public int getDrive() {
		return drive;
	}

	/**
	 * @return
	 */
	public String getDevice() {
		return device;
	}

}
