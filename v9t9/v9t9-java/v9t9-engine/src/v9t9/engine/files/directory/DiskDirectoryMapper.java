/*
  DiskDirectoryMapper.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.directory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.files.DiskDirectoryUtils;
import v9t9.common.files.DiskDirectory;
import v9t9.common.files.IDiskDirectory;
import v9t9.common.files.IFilesInDirectoryMapper;

import ejs.base.properties.IPersistable;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;




public class DiskDirectoryMapper implements IFilesInDirectoryMapper, IPersistable {
	private Map<String, File> diskMap = new HashMap<String, File>();
	private Map<String, String> deviceToDiskMap = new HashMap<String, String>();
	private Map<String, String> diskToDeviceMap = new HashMap<String, String>();
	private Map<String, IProperty> diskSettingsMap = new HashMap<String, IProperty>();
	private Map<String, IPropertyListener> diskSettingsListenerMap = new HashMap<String, IPropertyListener>();
	private IProperty enabledProperty;
	
	public DiskDirectoryMapper(IProperty enabledProperty) {
		this.enabledProperty = enabledProperty;
	}

	public void registerDiskSetting(String device, IProperty diskSetting) {
		diskMap.put(device, new File(diskSetting.getString()));
		
		deviceToDiskMap.put(device, diskSetting.getName());
		diskToDeviceMap.put(diskSetting.getName(), device);
		
		diskSettingsMap.put(device, diskSetting); 
		IPropertyListener listener = new IPropertyListener() {
			
			public void propertyChanged(IProperty setting) {
				diskMap.put(diskToDeviceMap.get(setting.getName()), new File(setting.getString()));
			}
		};
		diskSettingsListenerMap.put(device, listener);
		diskSetting.addListener(listener);
	}

	public void unregisterDiskSetting(String devname) {
		diskMap.remove(devname);
		IProperty diskSetting = diskSettingsMap.remove(devname);
		if (diskSetting != null) {
			IPropertyListener listener = diskSettingsListenerMap.remove(devname);
			diskSetting.removeListener(listener);
		}
	}

	public void setDiskPath(String device, File dir) {
		diskMap.put(device, dir);
		IProperty diskSetting = diskSettingsMap.get(device);
		if (diskSetting != null) {
			try {
				diskSetting.setString(dir.getCanonicalPath());
			} catch (IOException e) {
				diskSetting.setString(dir.getAbsolutePath());
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper#getSetting()
	 */
	public IProperty[] getSettings() {
		ArrayList<? extends IProperty> list = new ArrayList<IProperty>(diskSettingsMap.values());
		Collections.sort(list);
		return (IProperty[]) list.toArray(new IProperty[list.size()]);
	}
	

	public synchronized void saveState(ISettingSection settings) {
		for (Map.Entry<String, IProperty> entry : diskSettingsMap.entrySet()) {
			entry.getValue().saveState(settings);
		}
	}

	public synchronized void loadState(ISettingSection settings) {
		if (settings == null) return;
		for (Map.Entry<String, IProperty> entry : diskSettingsMap.entrySet()) {
			entry.getValue().loadState(settings);
		}
	}


	public File getLocalRoot(File file) {
		while (file != null) {
			for (Map.Entry<String, File> entry : diskMap.entrySet()) {
				if (entry.getValue().equals(file)) {
					return file;
				}
			}

			file = file.getParentFile();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper#getLocalDevice(java.lang.String)
	 */
	public String getDeviceNamed(String dsrName) {
		String localName = getLocalFileName(dsrName);
		for (Map.Entry<String, File> entry : diskMap.entrySet()) {
			if (entry.getValue().getName().equals(localName)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper#getLocalFilePath(java.lang.String)
	 */
	public String getLocalFileName(String dsrPath) {
		return DiskDirectoryUtils.dsrToHost(dsrPath);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper#getLocalDottedFile(java.lang.String)
	 */
	public File getLocalDottedFile(String deviceFilename) {
		int idx = deviceFilename.indexOf('.');
		if (idx < 0)
			return getLocalFile(deviceFilename, null);
		else
			return getLocalFile(deviceFilename.substring(0, idx), deviceFilename.substring(idx + 1));
	}
	
	@Override
	public File getLocalFile(String device, String filename) {
		if (!"DSK".equals(device)) {
			File dir = diskMap.get(device);
			if (dir == null) {
				return null;
			}
			return getLocalFile(dir, filename);
		} else {
			int idx = filename.indexOf('.');
			String diskName = filename.substring(0, idx >= 0 ? idx : filename.length());
			device = getDeviceNamed(diskName);
			if (device == null)
				return null;
			filename = filename.substring(diskName.length() + 1);
			return getLocalFile(device, filename);
		}
	}

	/**
	 * @param dir
	 * @param filename
	 * @return
	 */
	@Override
	public File getLocalFile(File dir, String filename) {
		return DiskDirectoryUtils.getLocalFile(dir, filename);
	}
	
	public String getDsrFileName(String filename) {
		return DiskDirectoryUtils.hostToDSR(filename);
	}
	
	public String getDsrDeviceName(File dir) {
		for (Map.Entry<String, File> entry : diskMap.entrySet()) {
			if (entry.getValue().equals(dir)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IFilesInDirectoryMapper#createCatalog(java.io.File)
	 */
//	@Override
//	public Catalog createCatalog(File dir) throws IOException {
//		FileDirectory fileDir = new FileDirectory(dir, this);
//
//		Catalog catalog = fileDir.readCatalog();
//
//		return catalog;
//	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFilesInDirectoryMapper#createDiskDirectory(java.io.File)
	 */
	@Override
	public IDiskDirectory createDiskDirectory(File dir) {
		return new DiskDirectory(dir, this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFilesInDirectoryMapper#getDirectorySupportProperty()
	 */
	@Override
	public IProperty getDirectorySupportProperty() {
		return enabledProperty;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFilesInDirectoryMapper#getDiskSettingMap()
	 */
	@Override
	public Map<String, IProperty> getDiskSettingMap() {
		return diskSettingsMap;
	}

}