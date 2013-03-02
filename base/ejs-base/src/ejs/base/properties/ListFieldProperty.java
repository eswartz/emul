/*
  ListFieldProperty.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import ejs.base.utils.XMLUtils;


/**
 * @author ejs
 *
 */
public class ListFieldProperty extends FieldProperty {

	private final String elementName;
	private IClassPropertyFactory elementFactory;

	public ListFieldProperty(IClassPropertyFactory factory, Object obj, String fieldName,
			String elementName, IClassPropertyFactory elementFactory, 
			String name) {
		super(factory, obj, fieldName, name != null ? name : fieldName);
		this.elementName = elementName;
		this.elementFactory = elementFactory;
	}


	/* (non-Javadoc)
	 * 
	 */
	@Override
	public void saveChildState(Element element, Object value,
			String propertyName) {
		List<?> list = (List<?>)value;
		if (list == null || list.isEmpty())
			return;
		Element listEl = element.getOwnerDocument().createElement(propertyName);
		element.appendChild(listEl);
		for (Object el : list) {
			if (el instanceof IXMLPersistable) {
				Element child = listEl.getOwnerDocument().createElement(elementName);
				listEl.appendChild(child);
				((IXMLPersistable)el).saveState(child);
				String id = elementFactory.getId(el);
				if (id != null) {
					child.setAttribute("id", id);
				}
				
			} else if (el instanceof IPropertyProvider) {
				Element child = listEl.getOwnerDocument().createElement(elementName);
				listEl.appendChild(child);
				((IPropertyProvider)el).getPropertySource().saveState(child);
				String id = elementFactory.getId(el);
				if (id != null) {
					child.setAttribute("id", id);
				}
				//((IPropertyProvider)el).getPropertySource().saveState(child);
			}
		}
	}

	
	/* (non-Javadoc)
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void loadChildState(Element parent, Object obj, String propertyName) {
		Element[] elements = XMLUtils.getChildElementsNamed(parent, elementName);
		for (Element element : elements) {
			String id = element.getAttribute("id");
			Object value = elementFactory.create(id);
			if (value instanceof IXMLPersistable)
				((IXMLPersistable) value).loadState(element);
			else if (value instanceof IPropertyProvider)
				((IPropertyProvider) value).getPropertySource().loadState(element);
			if (value != null)
				((List<Object>)obj).add(value);
		}
		//setValue(obj);
	}
	
	/* (non-Javadoc)
	 * 
	 */
	/*
	@Override
	public IPropertyEditor createEditor() {
		return new ListPropertyEditor(this);
	}
	*/

	/**
	 * @return
	 */
	public IClassPropertyFactory getElementClassFactory() {
		return elementFactory;
	}


	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getList() {
		List<Object>  list = (List<Object>)getValue();
		if (list == null) {
			list = new LinkedList<Object>();
			setValue(list);
		}
		return list;
	}
}
