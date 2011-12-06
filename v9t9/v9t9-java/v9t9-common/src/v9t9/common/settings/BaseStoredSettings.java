/**
 * 
 */
package v9t9.common.settings;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.base.settings.ISettingSection;
import v9t9.base.settings.ISettingStorage;
import v9t9.base.settings.SettingProperty;
import v9t9.base.settings.SettingsSection;
import v9t9.base.settings.XMLSettingStorage;

/**
 * @author ejs
 *
 */
public abstract class BaseStoredSettings implements IStoredSettings {

	private static class SyntheticSettingProperty extends SettingProperty {

		public SyntheticSettingProperty(String name, Object value) {
			super(name, value);
		}
		
	}
	private Map<String, SyntheticSettingProperty> syntheticSettings;
	protected Map<String, SettingProperty> registeredSettings;
	
	private static final String ROOT = "root";
	protected ISettingSection section;
	
	
	//protected List<SettingProperty> trackedSettings;
	protected boolean isLoading;
	protected IPropertyListener trackedSettingListener;
	protected boolean isLoaded;
	protected boolean needsSave;
	private final String context;

	public BaseStoredSettings(String context) {
		this.context = context;
		section = new SettingsSection();
		syntheticSettings = new HashMap<String, SyntheticSettingProperty>();
		registeredSettings = new HashMap<String, SettingProperty>();
		//trackedSettings = new ArrayList<SettingProperty>();
		trackedSettingListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				if (!isLoading)
					needsSave = true;
			}
		};
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IStoredSettings#load(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public void load(ISettingSection settings) {
		//for (SettingProperty setting : registeredSettings.values()) {
		//	setting.loadState(settings);
		//}
		for (String name : settings.getSettingNames()) {
			SettingProperty property = registeredSettings.get(name);
			if (property == null) {
				System.out.println("Synthesizing: " + context + "::" + name + " = " + settings.get(name));
				Object value;
				try {
					value = settings.getInt(name);
				} catch (NumberFormatException e) {
					value = settings.getObject(name);
				}
				SyntheticSettingProperty synProperty = findOrCreate(new SyntheticSettingProperty(name, value));
				syntheticSettings.put(name, synProperty);
			} else {
				System.out.println("Loading: "+ context + "::"  + name + " = " + settings.get(name));
				property.loadState(settings);
			}
		}
	}
	
	public synchronized void load() throws IOException {
		File settingsConfigurationFile = new File(getConfigFilePath());
		InputStream fis = null;
		try {
			ISettingStorage storage = new XMLSettingStorage(ROOT);
			fis = new BufferedInputStream(new FileInputStream(settingsConfigurationFile));
			section = storage.load(fis);
		} catch (IOException e) {
			needsSave = true;
			section = new SettingsSection();
			throw e;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
			
		isLoaded = true;
		needsSave = false;
		isLoading = true;
		try {
			load(section);
		} finally {
			isLoading = false;
		}
	}

	public synchronized void save() throws IOException {
		String path = getConfigFilePath();
		File file = new File(path);
		file.getParentFile().mkdirs();
		//if (file.exists() && !needsSave)
		//	return;
		
		ISettingSection saveSection = section;
		
		// toss unrecognized keys
		//if (true)
		//	saveSection = new SettingsSection();
		
		save(saveSection);
		
		ISettingStorage storage = new XMLSettingStorage(ROOT);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		storage.save(bos, saveSection);
		
		// see if contents changed
		boolean shouldWrite = true;
		if (file.exists()) {
			InputStream fis = null;
			try {
				int size = (int) file.length();
				if (size == bos.size()) {
					ByteArrayOutputStream bosold = new ByteArrayOutputStream(size);
					byte[] arr = new byte[size];
					fis = new BufferedInputStream(new FileInputStream(file));
					fis.read(arr);
					bosold.write(arr);
					if (Arrays.equals(bos.toByteArray(), bosold.toByteArray())) {
						shouldWrite = false;
					}
				}
			} catch (IOException e) {
				// ignore
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		if (shouldWrite) {
			File backup = new File(path + "~");
			file.renameTo(backup);
			
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bos.toByteArray());
			fos.close();
		}
		
		needsSave = false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IStoredSettings#save(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public void save(ISettingSection settings) {
		for (SettingProperty setting : registeredSettings.values()) {
			setting.saveState(settings);
		}
	}

	/**
	 * @param schema
	 * @return
	 */
	private SettingProperty findOrRealize(SettingSchema schema) {
		SettingProperty prop = registeredSettings.get(schema.getName());
		if (prop instanceof SyntheticSettingProperty) {
			System.out.println("*** Replacing synthetic " + context + "::" + schema.getName() + " with actual");
			SyntheticSettingProperty synProp = (SyntheticSettingProperty) prop;
			prop = schema.createSetting();
			prop.setValue(synProp.getValue());
			registeredSettings.put(schema.getName(), prop);
			syntheticSettings.remove(schema.getName());
		}
		return prop;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.settings.IStoredSettings#register(v9t9.common.settings.SettingDefinition)
	 */
	@Override
	public SettingProperty findOrCreate(SettingSchema schema) {
		SettingProperty prop = findOrRealize(schema);
		if (prop == null) {
			System.out.println("Creating: "+ context + "::" + schema.getName());
			prop = schema.createSetting();
			registeredSettings.put(schema.getName(), prop);
		}
		return prop;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.settings.IStoredSettings#register(v9t9.common.settings.SettingDefinition)
	 */
	@Override
	public SettingProperty findOrCreate(SettingSchema schema, Object defaultOverride) {
		SettingProperty prop = findOrRealize(schema);
		if (prop == null) {
			prop = schema.createSetting();
			prop.setValue(defaultOverride);
			registeredSettings.put(schema.getName(), prop);
		}
		return prop;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.settings.IStoredSettings#register(v9t9.common.settings.SettingDefinition)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SettingProperty> T findOrCreate(T defaultProperty) {
		SettingProperty prop = registeredSettings.get(defaultProperty.getName());
		if (prop == null) {
			prop = defaultProperty;
			registeredSettings.put(defaultProperty.getName(), prop);
		}
		return (T) prop;
	}

	/*
	public void register(SettingProperty setting) {
		registeredSettings.put(setting.getName(), setting);
		if (!trackedSettings.contains(setting)) {
			trackedSettings.add(setting);
			setting.addListener(trackedSettingListener);
			if (isLoaded) {
				setting.loadState(section);
			}
		}
	}

	public void register(SettingProperty setting, String custom) {
		register(setting);
		if (custom != null && setting.isDefault() && setting.getValue() instanceof String)
			setting.setString(custom);
	}
	*/
	
	public void clearConfigVar(String configVar) {
		section.put(configVar, (String) null);
	}

	public ISettingSection getSettings() {
		if (section == null) {
			try {
				load();
			} catch (IOException e) {
				assert section != null;
			}
		}
		return section;
	}

	public String getConfigDirectory() {
		return System.getProperty("user.home") + File.separatorChar + ".v9t9j" + File.separatorChar;
	}
	
	public String getConfigFilePath() {
		return getConfigDirectory() + getConfigFileName();
	}

	public ISettingSection getHistorySettings() {
		ISettingSection historySection = section.findOrAddSection("History");
		return historySection;
	}
	public void setDirty(boolean b) {
		needsSave = b;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.settings.IStoredSettings#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return needsSave;
	}

}