/**
 * 
 */
package org.ejs.coffee.core.properties;

import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.utils.XMLUtils;
import org.w3c.dom.Element;

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
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#createEditor()
	 */
	public IPropertyEditor createEditor() {
		return null;
	}


	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#getName()
	 */
	public String getLabel() {
		return name;
	}


	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#getValue()
	 */
	public Object getValue() {
		return obj;
	}


	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		obj = value;
		firePropertyChange();
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IProperty#setValueFromString(java.lang.String)
	 */
	public void setValueFromString(String value) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#saveState(org.w3c.dom.Element)
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
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#saveState(org.w3c.dom.Element)
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
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#loadState(org.w3c.dom.Element)
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
