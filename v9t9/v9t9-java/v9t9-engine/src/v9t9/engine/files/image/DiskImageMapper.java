/*
  DiskImageMapper.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.Catalog;
import v9t9.common.files.IDiskImage;
import v9t9.common.files.IDiskImageMapper;

/**
 * @author ejs
 *
 */
public class DiskImageMapper implements IDiskImageMapper {
	/** setting name (DiskImage1) to setting */
	protected Map<String, IProperty> diskSettingsMap = new LinkedHashMap<String, IProperty>();

	private Map<String, IDiskImage> disks = new LinkedHashMap<String, IDiskImage>();

	private ISettingsHandler settings;

	private ListenerList<IDiskImageListener> listeners;
	
	/**
	 * 
	 */
	public DiskImageMapper(ISettingsHandler settings) {
		this.settings = settings;
		listeners = new ListenerList<IDiskImageListener>();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImageMapper#addListener(v9t9.common.files.IDiskImageMapper.IDiskImageListener)
	 */
	@Override
	public void addListener(IDiskImageListener listener) {
		listeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImageMapper#removeListener(v9t9.common.files.IDiskImageMapper.IDiskImageListener)
	 */
	@Override
	public void removeListener(IDiskImageListener listener) {
		listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		for (Map.Entry<String, IDiskImage> entry : disks.entrySet())
			entry.getValue().saveState(section.addSection(entry.getKey()));
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		for (Map.Entry<String, IDiskImage> entry : disks.entrySet())
			entry.getValue().loadState(section.getSection(entry.getKey()));
	}

	public IDiskImage getDiskImage(String name) {
		IDiskImage info = disks.get(name);
		if (info == null) {
			IProperty setting = diskSettingsMap.get(name);
			if (setting == null)
				return null;
			info = DiskImageFactory.createDiskImage(settings, name, new File(setting.getString()));
			disks.put(name, info);
		}
		return disks.get(name);
	}

	public IProperty registerDiskImageSetting(String device, String initialPath) {
		DiskImageSetting diskSetting = settings.get(ISettingsHandler.MACHINE,
				new DiskImageSetting(settings, 
						device, initialPath,
						RealDiskDsrSettings.diskImageIconPath));
	
		diskSettingsMap.put(device, diskSetting); 
		diskSetting.addListener(new IPropertyListener() {
			
			public void propertyChanged(final IProperty setting) {
	
				final IDiskImage oldImage = disks.get(setting.getName());
				final IDiskImage newImage = DiskImageFactory.createDiskImage(settings, 
						setting.getName(), new File(setting.getString()));
				
				disks.put(setting.getName(), newImage);
				
				listeners.fire(new ListenerList.IFire<IDiskImageMapper.IDiskImageListener>() {

					@Override
					public void fire(IDiskImageListener listener) {
						listener.diskChanged(setting.getName(), oldImage, newImage);
					}
				});
						
				//setting.saveState(EmulatorSettings.getInstance().getApplicationSettings());
			}
		});
		return diskSetting;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImageMapper#getDiskImageMap()
	 */
	@Override
	public Map<String, IDiskImage> getDiskImageMap() {
		return disks;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImageMapper#getDiskSettingsMap()
	 */
	@Override
	public Map<String, IProperty> getDiskSettingsMap() {
		return diskSettingsMap;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImageMapper#createCatalog(java.io.File)
	 */
	@Override
	public Catalog createCatalog(String name, File spec) throws IOException {
		BaseDiskImage image = DiskImageFactory.createDiskImage(
				settings, name, spec);
		image.openDiskImage(true);
		
		Catalog catalog = image.readCatalog("DSK" + name.charAt(name.length() - 1));
		
		image.closeDiskImage();
		return catalog;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImageMapper#createDiskImage(java.io.File)
	 */
	@Override
	public IDiskImage createDiskImage(String name, File file) throws IOException {
		return DiskImageFactory.createDiskImage(
				settings, name, file);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IDiskImageMapper#isImageSupportEnabled()
	 */
	@Override
	public IProperty getImageSupportProperty() {
		return settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
	}
}
