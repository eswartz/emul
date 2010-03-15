/**
 * 
 */
package org.ejs.coffee.core.properties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.ejs.coffee.core.jface.PropertySourceEditor;
import org.w3c.dom.Element;

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
	 * @see org.ejs.chiprocksynth.editor.model.IPropertySource#getProperties()
	 */
	public IProperty[] getProperties() {
		return (IProperty[]) propertyOrder.toArray(new IProperty[propertyOrder.size()]);
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertySource#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return (String[]) propertyMap.keySet().toArray(new String[propertyMap.size()]);
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertySource#loadState(org.w3c.dom.Element)
	 */
	public void loadState(Element el) {
		for (IProperty property : propertyMap.values()) {
			property.loadState(el);
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertySource#saveState()
	 */
	public void saveState(Element el) {
		for (IProperty property : propertyMap.values()) {
			property.saveState(el);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertySource#loadState(org.w3c.dom.Element)
	 */
	public void loadState(IPropertyStorage storage) {
		for (IProperty property : propertyMap.values()) {
			property.loadState(storage);
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertySource#saveState()
	 */
	public void saveState(IPropertyStorage storage) {
		for (IProperty property : propertyMap.values()) {
			property.saveState(storage);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IPropertySource#getProperty(java.lang.String)
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
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IPropertySource#createEditor()
	 */
	public IPropertyEditor createEditor(String label) {
		return new PropertySourceEditor(this, label);
	}

}
