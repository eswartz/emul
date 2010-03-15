/**
 * 
 */
package org.ejs.coffee.core.properties;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * Storage built around the IDialogSettings class
 * @author ejs
 *
 */
public class DialogSettingsPropertyStorage implements IPropertyStorage {

	private IDialogSettings storage;

	/**
	 * 
	 */
	public DialogSettingsPropertyStorage(IDialogSettings storage) {
		this.storage = storage;
	}
	
	/**
	 * @return the storage
	 */
	public IDialogSettings getDialogSettings() {
		return storage;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#load(java.io.File)
	 */
	public void load(File file) throws IOException {
		storage.load(new FileReader(file));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#load(java.lang.String)
	 */
	public void load(String filename) throws IOException {
		storage.load(filename);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#save(java.io.File)
	 */
	public void save(File file) throws IOException {
		storage.save(new FileWriter(file));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#save(java.lang.String)
	 */
	public void save(String filename) throws IOException {
		storage.save(filename);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#addNewSection(java.lang.String)
	 */
	public IPropertyStorage addNewSection(String name) {
		IDialogSettings settings = storage.addNewSection(name);
		return new DialogSettingsPropertyStorage(settings);
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#getSection(java.lang.String)
	 */
	public IPropertyStorage getSection(String sectionName) {
		IDialogSettings settings = storage.getSection(sectionName);
		if (settings == null)
			return null;
		return new DialogSettingsPropertyStorage(settings);
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#getSections()
	 */
	public IPropertyStorage[] getSections() {
		IDialogSettings[] sections = storage.getSections();
		IPropertyStorage[] storages = new IPropertyStorage[sections.length];
		for (int i = 0; i < sections.length; i++)
			storages[i] = new DialogSettingsPropertyStorage(sections[i]);
		return storages;
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#read(org.ejs.coffee.core.properties.IProperty)
	 */
	public void read(IProperty property) {
		String value = storage.get(property.getName());
		if (value == null) {
			property.setValue(null);
			return;
		}
		property.setValueFromString(value);
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#get(java.lang.String)
	 */
	public String get(String name) {
		return storage.get(name);
	}
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#getBool(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		String value = get(name);
		if (value == null)
			return false;
		return Boolean.TRUE.toString().equals(value);
	}
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		try {
			String val = get(name);
			if (val == null)
				return 0;
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		try {
			String val = get(name);
			if (val == null)
				return 0;
			return Double.parseDouble(val);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#getArray(java.lang.String)
	 */
	public String[] getArray(String string) {
		return storage.getArray(string);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#put(java.lang.String, java.lang.String)
	 */
	public void put(String name, String value) {
		if (value != null)
			storage.put(name, value);
		else
			storage.put(name, (String) null);
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#put(java.lang.String, boolean)
	 */
	public void put(String name, boolean value) {
		put(name, Boolean.toString(value));		
	}
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#put(java.lang.String, double)
	 */
	public void put(String name, double value) {
		put(name, Double.toString(value));
	}
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#put(java.lang.String, int)
	 */
	public void put(String name, int value) {
		put(name, Integer.toString(value));
	}
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#put(java.lang.String, java.lang.String[])
	 */
	public void put(String name, String[] array) {
		storage.put(name, array);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#write(org.ejs.coffee.core.properties.IProperty)
	 */
	public void write(IProperty property) {
		storage.put(property.getName(), property.getString());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPropertyStorage#remove(org.ejs.coffee.core.properties.IProperty)
	 */
	public void remove(IProperty property) {
		storage.put(property.getName(), (String) null);
	}
	
}
