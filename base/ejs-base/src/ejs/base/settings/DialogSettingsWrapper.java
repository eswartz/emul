/**
 * 
 */
package ejs.base.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * @author ejs
 *
 */
public class DialogSettingsWrapper implements IDialogSettings {

	private final ISettingSection settings;

	public DialogSettingsWrapper(ISettingSection settings) {
		this.settings = settings;
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#addNewSection(java.lang.String)
	 */
	@Override
	public IDialogSettings addNewSection(String name) {
		return new DialogSettingsWrapper(settings.addSection(name));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#addSection(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
	public void addSection(IDialogSettings section) {
		if (!(section instanceof DialogSettingsWrapper))
			throw new IllegalArgumentException();
		
		settings.addSection(section.getName()).mergeFrom(((DialogSettingsWrapper) section).settings);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		return settings.get(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getArray(java.lang.String)
	 */
	@Override
	public String[] getArray(String key) {
		return settings.getArray(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String key) {
		return settings.getBoolean(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String key) throws NumberFormatException {
		if (settings.getObject(key) == null)
			throw new NumberFormatException();
		return settings.getDouble(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String key) throws NumberFormatException {
		if (settings.getObject(key) == null)
			throw new NumberFormatException();
		return (float) settings.getDouble(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String key) throws NumberFormatException {
		if (settings.getObject(key) == null)
			throw new NumberFormatException();
		return settings.getInt(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String key) throws NumberFormatException {
		if (settings.getObject(key) == null)
			throw new NumberFormatException();
		return settings.getInt(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getName()
	 */
	@Override
	public String getName() {
		return settings.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getSection(java.lang.String)
	 */
	@Override
	public IDialogSettings getSection(String sectionName) {
		return new DialogSettingsWrapper(settings.getSection(sectionName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#getSections()
	 */
	@Override
	public IDialogSettings[] getSections() {
		ISettingSection[] sections = settings.getSections();
		IDialogSettings[] dsections = new IDialogSettings[sections.length];
		for (int i = 0; i < sections.length; i++)
			dsections[i] = new DialogSettingsWrapper(sections[i]);
		return dsections;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#load(java.io.Reader)
	 */
	@Override
	public void load(Reader reader) throws IOException {
		throw new IOException("not implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#load(java.lang.String)
	 */
	@Override
	public void load(String fileName) throws IOException {
		throw new IOException("not implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#put(java.lang.String, java.lang.String[])
	 */
	@Override
	public void put(String key, String[] value) {
		settings.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#put(java.lang.String, double)
	 */
	@Override
	public void put(String key, double value) {
		settings.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#put(java.lang.String, float)
	 */
	@Override
	public void put(String key, float value) {
		settings.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#put(java.lang.String, int)
	 */
	@Override
	public void put(String key, int value) {
		settings.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#put(java.lang.String, long)
	 */
	@Override
	public void put(String key, long value) {
		settings.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#put(java.lang.String, java.lang.String)
	 */
	@Override
	public void put(String key, String value) {
		settings.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#put(java.lang.String, boolean)
	 */
	@Override
	public void put(String key, boolean value) {
		settings.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#save(java.io.Writer)
	 */
	@Override
	public void save(Writer writer) throws IOException {
		throw new IOException("not implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogSettings#save(java.lang.String)
	 */
	@Override
	public void save(String fileName) throws IOException {
		throw new IOException("not implemented");
	}

}
