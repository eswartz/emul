/*
  ClassProperty.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import org.w3c.dom.Element;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.XMLUtils;


/**
 * @author ejs
 *
 */
public class ClassProperty extends AbstractProperty {

	public ClassProperty(IClassPropertyFactory factory, Class<?> type, String name) {
		super(factory, type, name);
	}
	private Object obj;

	/* (non-Javadoc)
	 * 
	 */
	public String getLabel() {
		return name;
	}


	/* (non-Javadoc)
	 * 
	 */
	public Object getValue() {
		return obj;
	}


	/* (non-Javadoc)
	 * 
	 */
	public void setValue(Object value) {
		obj = value;
		firePropertyChange();
		
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void setValueFromString(String value) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * 
	 */
	public void saveState(Element element) {
		IPropertySource ps = null;
		if (obj instanceof IXMLPersistable) {
			((IXMLPersistable) obj).saveState(element);
			return;
		}
		if (obj instanceof IPropertySource) {
			ps = (IPropertySource) obj;
		} 
		else if (obj instanceof IPropertyProvider) {
			ps = ((IPropertyProvider) obj).getPropertySource();
		}
		if (ps != null) {
			Element child = element.getOwnerDocument().createElement(name);
			element.appendChild(child);
			ps.saveState(child);
			String id = factory.getId(obj);
			if (id != null)
				child.setAttribute("id", id);
			return;
		}
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void saveState(ISettingSection section) {
		IPropertySource ps = null;
		if (obj instanceof IPersistable) {
			((IPersistable) obj).saveState(section);
			return;
		}
		if (obj instanceof IPropertySource) {
			ps = (IPropertySource) obj;
		} 
		else if (obj instanceof IPropertyProvider) {
			ps = ((IPropertyProvider) obj).getPropertySource();
		}
		if (ps != null) {
			ISettingSection child = section.addSection(name);
			ps.saveState(child);
			String id = factory.getId(obj);
			if (id != null)
				child.put("id", id);
			return;
		}
	}
	/* (non-Javadoc)
	 * 
	 */
	public void loadState(Element element) {
		Element[] elements = XMLUtils.getChildElementsNamed(element, name);
		if (elements.length == 1) {
			String id = elements[0].getAttribute("id");
			if (id == null)
				return;
			
			obj = factory.create(id);
			IPropertySource ps = null;
			if (obj instanceof IXMLPersistable) {
				((IXMLPersistable) obj).loadState(element);
				return;
			}
			if (obj instanceof IPropertySource) {
				ps = (IPropertySource) obj;
			} 
			else if (obj instanceof IPropertyProvider) {
				ps = ((IPropertyProvider) obj).getPropertySource();
			}
			if (ps != null) {
				ps.loadState(elements[0]);
				return;
			} 
		}
		else if (elements.length > 1)
			throw new IllegalStateException();
		
		
		
	}
	public void loadState(ISettingSection section) {
		ISettingSection childSection = section.getSection(name);
		if (childSection != null) {
			String id = childSection.get("id");
			if (id == null)
				return;
			
			obj = factory.create(id);
			IPropertySource ps = null;
			if (obj instanceof IPersistable) {
				((IPersistable) obj).loadState(section);
				return;
			}
			if (obj instanceof IPropertySource) {
				ps = (IPropertySource) obj;
			} 
			else if (obj instanceof IPropertyProvider) {
				ps = ((IPropertyProvider) obj).getPropertySource();
			}
			if (ps != null) {
				ps.loadState(childSection);
				return;
			} 
		}
	}
}
