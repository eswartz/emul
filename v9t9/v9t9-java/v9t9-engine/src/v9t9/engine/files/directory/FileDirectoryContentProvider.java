/*
  FileDirectoryContentProvider.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.directory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.client.IEmulatorContentSourceProvider;
import v9t9.common.files.Catalog;
import v9t9.common.files.CatalogEntry;
import v9t9.common.files.DiskDirectory;
import v9t9.common.files.EmulatedDiskContentSource;
import v9t9.common.files.IDiskDirectory;
import v9t9.common.files.IFilesInDirectoryMapper;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.machine.IMachine;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingProperty;

/**
 * @author ejs
 *
 */
public class FileDirectoryContentProvider implements IEmulatorContentSourceProvider {

	private IMachine machine;

	public FileDirectoryContentProvider(IMachine machine) {
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
		
		IFilesInDirectoryMapper fiadMapper = machine.getEmulatedFileHandler().getFilesInDirectoryMapper();
		if (fiadMapper == null) {
			return IEmulatorContentSource.EMPTY;
		}
		IProperty[] disks = fiadMapper.getSettings();
		IProperty enabledProperty = fiadMapper.getDirectorySupportProperty();
		if (enabledProperty == null || disks.length == 0) {
			//MessageDialog.openError(shell, "Not Supported", "This machine does not support files in directories");
			return IEmulatorContentSource.EMPTY;
		}
		
		Catalog catalog = null;
		IDiskDirectory disk;
		
		if (file.isDirectory()) {
			disk = new DiskDirectory(file, fiadMapper);
			try {
				catalog = disk.readCatalog();
			} catch (IOException e) {
				return IEmulatorContentSource.EMPTY;
			}
		} else {
			CatalogEntry entry;
			try {
				entry = new CatalogEntry(-1,
						fiadMapper.getDsrFileName(file.getName()), 
						NativeFileFactory.INSTANCE.createNativeFile(file));
			} catch (IOException e1) {
				return IEmulatorContentSource.EMPTY;
			}
			
			disk = new DiskDirectory(file.getParentFile(), fiadMapper); 
			catalog = new Catalog(disk,
					"DSK1", fiadMapper.getDsrFileName(file.getParentFile().getName()),
					0, 0, 
					Collections.singletonList(entry));
		}
		
		Map<String, IProperty> diskSettingsMap = fiadMapper.getDiskSettingMap();
		List<IEmulatorContentSource> sources = new ArrayList<IEmulatorContentSource>(diskSettingsMap.size());
		
		for (Map.Entry<String, IProperty> ent : diskSettingsMap.entrySet()) {
			if (ent.getValue() instanceof ISettingProperty) {
				if (!((ISettingProperty) ent.getValue()).isEnabled())
					continue;
			}

			sources.add(new EmulatedDiskContentSource(machine, disk, catalog, ent.getValue())); 
		}
		return (IEmulatorContentSource[]) sources
				.toArray(new IEmulatorContentSource[sources.size()]);
	}

}
