/**
 * 
 */
package org.ejs.coffee.core.model;

import org.ejs.coffee.core.jface.IPropertyEditor;
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
	public String getName() {
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
		if (obj instanceof IPersistable) {
			((IPersistable) obj).saveState(element);
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
			if (obj instanceof IPersistable) {
				((IPersistable) obj).loadState(element);
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
	
}
