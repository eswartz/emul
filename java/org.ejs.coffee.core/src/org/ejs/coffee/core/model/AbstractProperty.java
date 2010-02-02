/**
 * 
 */
package org.ejs.coffee.core.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.ejs.coffee.core.jface.IPropertyEditor;
import org.ejs.coffee.core.utils.XMLUtils;
import org.w3c.dom.Element;



/**
 * @author ejs
 *
 */
public abstract class AbstractProperty implements IProperty, ICellEditorProvider {

	
	protected final String name;
	protected final IClassPropertyFactory factory;
	protected Class<?> type;
	protected IPropertyEditorProvider editorProvider;
	private String descr;
	protected IPropertyListener[] listenerArray;
	private List<IPropertyListener> listeners;
	private boolean isHidden;
	/**
	 * 
	 */
	public AbstractProperty(IClassPropertyFactory factory, Class<?> type, String name) {
		this(factory, type, name, null);
	}
	public AbstractProperty(IClassPropertyFactory factory, Class<?> type, String name, IPropertyEditorProvider editorProvider) {
		this.listenerArray = null;
		this.listeners = new LinkedList<IPropertyListener>();
		this.editorProvider = editorProvider;
		this.factory = factory;
		this.type = type;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.ICellEditorProvider#createCellEditor(org.eclipse.swt.widgets.Composite)
	 */
	public CellEditor createCellEditor(Composite composite) {
		return new TextCellEditor(composite);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((factory == null) ? 0 : factory.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractProperty other = (AbstractProperty) obj;
		if (factory == null) {
			if (other.factory != null) {
				return false;
			}
		} else if (!factory.equals(other.factory)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IProperty#getType()
	 */
	public Class<?> getType() {
		return type;
	}
	/**
	 * @param type
	 */
	protected void setType(Class<?> type) {
		this.type = type;
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#getName()
	 */
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.AbstractProperty#getPersistedName()
	 */
	public String getPersistedName() {
		return name;
	}

	/**
	 * @return
	 */
	public IClassPropertyFactory getClassFactory() {
		return factory;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + "=" + getValue();
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#loadState(org.w3c.dom.Element)
	 */
	public void loadState(Element element) {
		if (factory != null) {
			Element[] elements = XMLUtils.getChildElementsNamed(element, getPersistedName());
			if (elements.length == 1) {
				String id = elements[0].getAttribute("id");
				Object obj = factory.create(id);
				if (obj != null)
					loadChildState(elements[0], obj, getPersistedName());
				else
					System.err.println("Cannot recreate id="+id+" for " +getPersistedName());
				setValue(obj);
			}
			else if (elements.length > 1)
				throw new IllegalStateException();
			
			return;
		}
		
		Object value = getValue();
		loadChildState(element, value, getPersistedName());
		
		/*
		IPropertySource ps = null;
		if (value instanceof IPropertySource) {
			ps = (IPropertySource) value;
		} 
		else if (value instanceof IPropertyProvider) {
			ps = ((IPropertyProvider) value).getPropertySource();
		}
		if (ps != null) {
			Element[] elements = XMLUtils.getChildElementsNamed(element, getPersistedName());
			if (elements.length == 1)
				doLoadChildState(value, ps, elements[0]);
			else if (elements.length > 1)
				throw new IllegalStateException();
			return;
		}
		String attr = element.getAttribute(getPersistedName());
		if (attr != null)
			setValueFromString(attr);
			*/
	}

	/**
	 * @return
	 */
	protected Object createProperty(String id) {
		return factory.create(id);
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IPersistable#loadChildState(org.w3c.dom.Element, java.lang.Object, java.lang.String)
	 */
	public void loadChildState(Element element, Object obj, String propertyName) {
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
			ps.loadState(element);
			//setValue(obj);
			return;
		} 		
		String attr = element.getAttribute(propertyName);
		if (attr != null)
			setValueFromString(attr);		
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#saveState(org.w3c.dom.Element)
	 */
	public void saveState(Element element) {
		Object value = getValue();
		saveChildState(element, value, getPersistedName());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IPersistable#saveChildState(org.w3c.dom.Element, java.lang.Object, java.lang.String)
	 */
	public void saveChildState(Element element, Object value, String propertyName) {
		IPropertySource ps = null;
		if (value instanceof IPersistable) {
			((IPersistable) value).saveState(element);
			return;
		}
		if (value instanceof IPropertyProvider) {
			ps = ((IPropertyProvider) value).getPropertySource();
		}
		if (ps != null) {
			Element child = element.getOwnerDocument().createElement(propertyName);
			element.appendChild(child);
			doSaveChildState(value, ps, child);
			return;
		}
		
		if (value != null)
			element.setAttribute(propertyName, value.toString());		
	}
	/**
	 * @param child
	 * @param ps
	 */
	protected void doSaveChildState(Object value, IPropertySource ps, Element child) {
		ps.saveState(child);
		String id = factory.getId(value);
		if (id != null) {
			child.setAttribute("id", id);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#createEditor()
	 */
	public IPropertyEditor createEditor() {
		if (editorProvider == null)
			return null;
		return editorProvider.createEditor(this);
	}

	public String getDescription() { 
		return descr;
	}
	public void setDescription(String descr) {
		this.descr = descr;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IProperty#addListener(org.ejs.chiprocksynth.model.IPropertyListener)
	 */
	public synchronized void addListener(IPropertyListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
			listenerArray = (IPropertyListener[]) listeners.toArray(new IPropertyListener[listeners.size()]);
		}
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IProperty#removeListener(org.ejs.chiprocksynth.model.IPropertyListener)
	 */
	public synchronized void removeListener(IPropertyListener listener) {
		if (listeners.remove(listener))
			listenerArray = (IPropertyListener[]) listeners.toArray(new IPropertyListener[listeners.size()]);
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IProperty#updateFromPropertyChange()
	 */
	public final void firePropertyChange() {
		if (listenerArray == null)
			return;
		for (IPropertyListener listener : listenerArray)
			listener.propertyChanged(this);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.model.IProperty#isHidden()
	 */
	public boolean isHidden() {
		return isHidden;
	}
	
	/**
	 * @param isHidden the isHidden to set
	 */
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
}
