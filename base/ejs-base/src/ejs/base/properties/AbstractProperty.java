/*
  AbstractProperty.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.Check;
import ejs.base.utils.XMLUtils;




/**
 * @author ejs
 *
 */
public abstract class AbstractProperty implements IProperty {

	
	private static final Logger logger = Logger.getLogger(AbstractProperty.class);
	
	protected final String name;
	protected final IClassPropertyFactory factory;
	protected Class<?> type;
	//protected IPropertyEditorProvider editorProvider;
	private String descr;
	protected IPropertyListener[] listenerArray;
	private List<IPropertyListener> listeners;
	private boolean isHidden;
	/**
	 * 
	 */
	public AbstractProperty(IClassPropertyFactory factory, Class<?> type, String name) {
		Check.checkArg(type);
		this.listenerArray = null;
		this.listeners = new LinkedList<IPropertyListener>();
		//this.editorProvider = editorProvider;
		this.factory = factory;
		this.type = type;
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(IProperty o1, IProperty o2) {
		return o1.getName().compareTo(o2.getName());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IProperty o) {
		return getName().compareTo(o.getName());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
	 * 
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
	 * 
	 */
	public String getLabel() {
		return name;
	}
	/* (non-Javadoc)
	 * 
	 */
	public String getName() {
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
	

    public int getInt() {
    	Object value = getValue();
    	if (value instanceof Number)
    		return ((Number)value).intValue();
    	return 0;
    }
    public double getDouble() {
    	Object value = getValue();
    	if (value instanceof Number)
    		return ((Number)value).doubleValue();
    	return 0;
    }
    public boolean getBoolean() {
    	Object value = getValue();
    	if (value instanceof Boolean)
    		return ((Boolean)value).booleanValue();
    	if (value instanceof String)
    		return "true".equals(value);
    	return false;
    }
    public String getString() {
        Object value = getValue();
		return value != null ? value.toString() : null;
    }
    @SuppressWarnings("unchecked")
	public <T> List<T> getList() {
    	return (List<T>)getValue();
    }
    public void setInt(int val) {
        setValue(Integer.valueOf(val));
    }
    public void setDouble(double val) {
    	setValue(Double.valueOf(val));
    }
    public void setBoolean(boolean val) {
    	setValue(Boolean.valueOf(val));
    }
    public void setString(String val) {
        setValue(val);
    }
    public void setList(List<?> val) {
    	Object old = getList();
    	setValue(val);
    	if (old == val) {
			firePropertyChange();
		}
    }

	/* (non-Javadoc)
	 * 
	 */
	public void loadState(Element element) {
		if (factory != null) {
			Element[] elements = XMLUtils.getChildElementsNamed(element, getName());
			if (elements.length == 1) {
				String id = elements[0].getAttribute("id");
				Object obj = factory.create(id);
				if (obj != null)
					loadChildState(elements[0], obj, getName());
				else
					logger.error("Cannot recreate id="+id+" for " +getName());
				setValue(obj);
			}
			else if (elements.length > 1)
				throw new IllegalStateException();
			
			return;
		}
		
		Object value = getValue();
		loadChildState(element, value, getName());
	}
	

	/* (non-Javadoc)
	 * 
	 */
	public void loadChildState(Element element, Object obj, String propertyName) {
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
			ps.loadState(element);
			//setValue(obj);
			return;
		} 		
		String attr = element.getAttribute(propertyName);
		if (attr != null)
			setValueFromString(attr);		
	}

	/* (non-Javadoc)
	 * 
	 */
	public void loadState(ISettingSection section) {
		if (factory != null) {
			ISettingSection childSection = section.getSection(getName());
			String id = childSection.get("id");
			Object obj = factory.create(id);
			if (obj != null)
				loadChildState(childSection, obj, getName());
			else
				logger.error("Cannot recreate id="+id+" for " +getName());
			setValue(obj);
			
			return;
		}
		
		Object value = getValue();
		loadChildState(section, value, getName());
	}


	/* (non-Javadoc)
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void loadChildState(ISettingSection section, Object obj, String propertyName) {
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
			ps.loadState(section);
			//setValue(obj);
			return;
		} 		
		if (obj instanceof Collection) {
			String[] items = section.getArray(propertyName);
			if (items != null) {
				((Collection<?>) obj).clear();
				for (String item : items) {
					Object value =  PropertyUtils.convertStringToValue(item, type);
					if (value != null)
						((Collection<Object>) obj).add(value);
				}
			}
		} else {
			String attr = section.get(propertyName);
			if (attr != null)
				setValueFromString(attr);
		}
	}
	/**
	 * @return
	 */
	protected Object createProperty(String id) {
		return factory.create(id);
	}

	/* (non-Javadoc)
	 * 
	 */
	public void saveState(Element element) {
		Object value = getValue();
		saveChildState(element, value, getName());
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void saveChildState(Element element, Object value, String propertyName) {
		IPropertySource ps = null;
		if (value instanceof IXMLPersistable) {
			((IXMLPersistable) value).saveState(element);
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
		
		if (value != null) {
			element.setAttribute(propertyName, value.toString());
		}
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
	 * 
	 */
	public void saveState(ISettingSection section) {
		Object value = getValue();
		saveChildState(section, value, getName());
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public void saveChildState(ISettingSection section, Object value, String propertyName) {
		IPropertySource ps = null;
		if (value instanceof IPersistable) {
			((IPersistable) value).saveState(section);
			return;
		}
		if (value instanceof IPropertyProvider) {
			ps = ((IPropertyProvider) value).getPropertySource();
		}
		if (ps != null) {
			ISettingSection child = section.addSection(propertyName);
			doSaveChildState(value, ps, child);
			return;
		}
		
		if (value != null) {
			if (value instanceof Collection) {
				String[] items = new String[((Collection<?>) value).size()];
				int idx = 0;
				for (Object obj : ((Collection<?>) value)) {
					items[idx++] = obj.toString();
				}
				section.put(propertyName, items);
			} else if (value instanceof String[]) {
				section.put(propertyName, (String[]) value);
			} else {
				section.put(propertyName, value.toString());
			}
		}
	}
	/**
	 * @param child
	 * @param ps
	 */
	protected void doSaveChildState(Object value, IPropertySource ps, ISettingSection child) {
		ps.saveState(child);
		String id = factory.getId(value);
		if (id != null) {
			child.put("id", id);
		}
	}
	
	/* (non-Javadoc)
	 * 
	 */
	/*
	public IPropertyEditor createEditor() {
		if (editorProvider == null)
			return null;
		return editorProvider.createEditor(this);
	}
	*/

	public String getDescription() { 
		return descr;
	}
	public void setDescription(String descr) {
		this.descr = descr;
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public synchronized void addListener(IPropertyListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
			listenerArray = (IPropertyListener[]) listeners.toArray(new IPropertyListener[listeners.size()]);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.base.properties.IProperty#addListenerAndFire(v9t9.base.properties.IPropertyListener)
	 */
	@Override
	public void addListenerAndFire(IPropertyListener listener) {
		addListener(listener);
		listener.propertyChanged(this);
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public synchronized void removeListener(IPropertyListener listener) {
		if (listeners.remove(listener))
			listenerArray = (IPropertyListener[]) listeners.toArray(new IPropertyListener[listeners.size()]);
	}
	/* (non-Javadoc)
	 * 
	 */
	public final void firePropertyChange() {
		if (listenerArray == null)
			return;
		for (IPropertyListener listener : listenerArray)
			listener.propertyChanged(this);
	}
	
	/* (non-Javadoc)
	 * 
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
	

    private class DependentSettingListener implements IPropertyListener {
    	private final IProperty other;
		public DependentSettingListener(IProperty other) {
			this.other = other;
    	}
		public void propertyChanged(IProperty property) {
			firePropertyChange();
		}
	}; 
    private List<DependentSettingListener> dependentListeners;
    
    public synchronized void addEnablementDependency(final IProperty other) {
    	DependentSettingListener listener = new DependentSettingListener(other);
    	if (dependentListeners == null) {
    		dependentListeners = new ArrayList<DependentSettingListener>();
    	}
    	dependentListeners.add(listener);
		other.addListener(listener);
    };

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
    	super.finalize();
    	if (dependentListeners != null) {
    		for (DependentSettingListener listener : dependentListeners) {
    			listener.other.removeListener(listener);
    		}
    		dependentListeners.clear();
    	}
    		
    }
}
