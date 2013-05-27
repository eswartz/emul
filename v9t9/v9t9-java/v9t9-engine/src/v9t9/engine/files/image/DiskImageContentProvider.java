/*
  DiskImageContentProvider.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.client.IEmulatorContentSourceProvider;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.files.Catalog;
import v9t9.common.files.EmulatedDiskContentSource;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IDiskImageMapper;
import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public class DiskImageContentProvider implements IEmulatorContentSourceProvider {

	private IMachine machine;

	public DiskImageContentProvider(IMachine machine) {
		this.machine = machine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentSourceProvider#analyze(java.net.URI)
	 */
	@Override
	public IEmulatorContentSource[] analyze(URI uri) {
		File file;
		try {
			file = new File(uri);
		} catch (IllegalArgumentException e) {
			return IEmulatorContentSource.EMPTY;
		}
		
		IDiskImageMapper imageMapper = machine.getEmulatedFileHandler().getDiskImageMapper();
		Catalog catalog = null;
		IDiskImage image;
		try {
			image = imageMapper.createDiskImage(file);
		} catch (IOException e1) {
			e1.printStackTrace();
			// not disk image 
			return IEmulatorContentSource.EMPTY;
		}
		
		if (!image.isFormatted()) {
			return IEmulatorContentSource.EMPTY;
		}
		
		try {
			catalog = image.readCatalog();
		} catch (IOException e1) {
			machine.getEventNotifier().notifyEvent(null, Level.ERROR, "Could not read disk image catalog\n" + e1.getMessage());
			return IEmulatorContentSource.EMPTY;
		}
		
		Map<String, IProperty> diskSettingsMap = 
				machine.getEmulatedFileHandler().getDiskImageMapper().getDiskSettingsMap();
		IEmulatorContentSource[] sources = new IEmulatorContentSource[diskSettingsMap.size()];
		int index = 0;
		for (Map.Entry<String, IProperty> ent : diskSettingsMap.entrySet()) {
			sources[index++] = new EmulatedDiskContentSource(machine, image, catalog, ent.getValue()); 
		}
		return sources;
	}

}
