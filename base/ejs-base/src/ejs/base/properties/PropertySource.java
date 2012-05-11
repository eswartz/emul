/**
 * 
 */
package ejs.base.properties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.w3c.dom.Element;

import ejs.base.settings.ISettingSection;


/**
 * @author ejs
 *
 */
public class PropertySource implements IPropertySource {

	private LinkedHashMap<String, IProperty> propertyMap;
	private List<IProperty> propertyOrder;

	public PropertySource() {
		propertyMap = new LinkedHashMap<String, IProperty>();
		propertyOrder = new ArrayList<IProperty>();
	}
	/* (non-Javadoc)
	 * 
	 */
	public IProperty[] getProperties() {
		return (IProperty[]) propertyOrder.toArray(new IProperty[propertyOrder.size()]);
	}

	/* (non-Javadoc)
	 * 
	 */
	public String[] getPropertyNames() {
		return (String[]) propertyMap.keySet().toArray(new String[propertyMap.size()]);
	}

	/* (non-Javadoc)
	 * 
	 */
	public void loadState(Element el) {
		for (IProperty property : propertyMap.values()) {
			property.loadState(el);
		}
	}

	/* (non-Javadoc)
	 * 
	 */
	public void saveState(Element el) {
		for (IProperty property : propertyMap.values()) {
			property.saveState(el);
		}
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void loadState(ISettingSection section) {
		for (IProperty property : propertyMap.values()) {
			property.loadState(section);
		}
	}

	/* (non-Javadoc)
	 * 
	 */
	public void saveState(ISettingSection section) {
		for (IProperty property : propertyMap.values()) {
			property.saveState(section);
		}
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public IProperty getProperty(String propertyName) {
		return propertyMap.get(propertyName);
	}
	/**
	 * @param fieldProperty
	 */
	public IProperty addProperty(IProperty property) {
		propertyMap.put(property.getName(), property);
		propertyOrder.add(property);
		return property;
	}
	
	
	/**
	 * @param fieldProperty
	 */
	public IProperty addProperty(int index, IProperty property) {
		propertyMap.put(property.getName(), property);
		propertyOrder.add(index, property);
		return property;
	}
}
