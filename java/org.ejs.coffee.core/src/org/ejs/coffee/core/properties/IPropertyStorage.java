/**
 * 
 */
package org.ejs.coffee.core.properties;

import java.io.File;
import java.io.IOException;

/**
 * @author ejs
 *
 */
public interface IPropertyStorage {

	void load(String filename) throws IOException;
	void load(File file) throws IOException;
	void save(String filename) throws IOException;
	void save(File file) throws IOException;
	
	IPropertyStorage addNewSection(String name);

    IPropertyStorage getSection(String sectionName);
    IPropertyStorage[] getSections();

    void read(IProperty property);
    void write(IProperty property);
	void remove(IProperty property);
	
	String get(String name);
	int getInt(String name);
	boolean getBoolean(String name);
	double getDouble(String name);
	String[] getArray(String string);
	
	void put(String name, String value);
	void put(String name, boolean value);
	void put(String name, int value);
	void put(String name, double value);
	void put(String name, String[] array);
}
