/**
 * 
 */
package v9t9.base.settings;


/**
 * @author ejs
 *
 */
public interface ISettingSection extends Iterable<ISettingSection.SettingEntry>{
	enum Type {
		/** ISettingStorage */
		Section("section"),
		Int("int"),
		Double("double"),
		Boolean("bool"),
		String("str"),
		/** String[] */
		StringArray("strs"),
		Object("object");
		
		private final java.lang.String xmlName;

		private Type(String xmlName) {
			this.xmlName = xmlName;
		}
		
		public java.lang.String getXmlName() {
			return xmlName;
		}
	}
	
	static class SettingEntry {
		public Type type;
		public String name;
		public Object value;
	}

	/** Get the existing section with this name, or <code>null</code> */ 
	ISettingSection getSection(String sectionName);
	/** Add a section with the given name, <b>erasing</b> any existing section */
	ISettingSection addSection(String sectionName);
    /** Find an existing section with the given name, or create and add a new section */
    ISettingSection findOrAddSection(String sectionName);
    String[] getSectionNames();
    ISettingSection[] getSections();
    
    String[] getSettingNames();

	String get(String name);
	int getInt(String name);
	boolean getBoolean(String name);
	double getDouble(String name);
	String[] getArray(String name);
	Object getObject(String name);
	
	void put(String name, String value);
	void put(String name, boolean value);
	void put(String name, int value);
	void put(String name, double value);
	void put(String name, String[] array);
	void put(String name, Object value);
}
