/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package org.ejs.coffee.core.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;

/** A single configurable setting.
 * @author ejs
 */
public class Setting implements Comparable<Setting>, Comparator<Setting> {
    private Object storage;
    private String name;
    private ListenerList listeners, enabledListeners;
	private final String label;
	private final String description;
    
	public Setting(String name, Object storage) {
        this.name = name;
        this.label = name;
        this.description = null;
        this.storage = storage;
        this.listeners = null;
        this.enabledListeners = null;
        this.dependentListeners = null;
    }
    
	public Setting(String name, String label, String description, Object storage) {
        this.name = name;
		this.label = label;
		this.description = description;
        this.storage = storage;
        this.listeners = null;
        this.enabledListeners = null;
        this.dependentListeners = null;
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName() + " = " + getValue();
	}
    public String getName() {
        return name;
    }
    
    /**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
    /**
     * Tell whether the setting is available.  A subclass can override this if
     * a setting has conditional availability.
     * @return
     */
    public boolean isEnabled() {
    	return true;
    }
    public synchronized void addListener(ISettingListener listener) {
    	if (listeners == null) 
    		listeners = new ListenerList();
        listeners.add(listener);
    }
    public synchronized void removeListener(ISettingListener listener) {
    	if (listeners == null) return;
        listeners.remove(listener);
    }
    private synchronized void notifyListeners(Object oldValue) {
    	if (listeners == null) return;
        for (Object obj : listeners.getListeners()) {
            ISettingListener listener = (ISettingListener) obj;
            listener.changed(this, oldValue);
        }
    }
    
    public synchronized void addEnabledListener(ISettingEnabledListener listener) {
    	if (enabledListeners == null)
    		enabledListeners = new ListenerList();
        enabledListeners.add(listener);
    }
    public synchronized void removeEnabledListener(ISettingEnabledListener listener) {
    	if (enabledListeners == null) return;
        enabledListeners.remove(listener);
    }
    protected synchronized void notifyEnabledListeners() {
    	if (enabledListeners == null) return;
        for (Object obj : enabledListeners.getListeners()) {
            ISettingEnabledListener listener = (ISettingEnabledListener) obj;
            listener.changed(this);
        }
    }

        
    public Object getValue() {
        return storage;
    }
    public int getInt() {
        return ((Integer)storage).intValue();
    }
    public boolean getBoolean() {
        return ((Boolean)storage).booleanValue();
    }
    public String getString() {
        return (String)storage;
    }
    public void setValue(Object val) {
        Object old = storage;
        storage = val;
        notifyListeners(old);
    }
    public void setInt(int val) {
        Object old = storage;
        storage = new Integer(val);
        notifyListeners(old);
    }
    public void setBoolean(boolean val) {
        Object old = storage;
        storage = new Boolean(val);
        notifyListeners(old);
    }
    public void setString(String val) {
        Object old = storage;
        storage = val;
        notifyListeners(old);
    }
	public void saveState(IDialogSettings section) {
		if (storage instanceof Integer)
			section.put(name, Integer.toString(getInt()));
		else
			section.put(name, getValue().toString());
	}
	public void loadState(IDialogSettings section) {
		String value = section.get(name);
		if (value != null) {
			if (storage instanceof Boolean)
				setValue(Boolean.parseBoolean(value));
			else if (storage instanceof Integer)
				setValue(Integer.parseInt(value));
			else
				setValue(value);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Setting o1, Setting o2) {
		return o1.getName().compareTo(o2.getName());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Setting o) {
		return getName().compareTo(o.getName());
	}
	

    private class DependentSettingListener implements ISettingListener, ISettingEnabledListener {
    	private Setting other;
    	public DependentSettingListener(Setting other) {
    		this.other = other;
    	}
		public void changed(Setting setting, Object oldValue) {
			notifyEnabledListeners();
		}
		public void changed(Setting setting) {
			notifyEnabledListeners();
		}
	}; 
    private List<DependentSettingListener> dependentListeners;
    
    public synchronized void addEnablementDependency(final Setting other) {
    	DependentSettingListener listener = new DependentSettingListener(other);
    	if (dependentListeners == null) {
    		dependentListeners = new ArrayList<DependentSettingListener>();
    	}
    	dependentListeners.add(listener);
		other.addListener(listener);
		other.addEnabledListener(listener);
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
    			listener.other.removeEnabledListener(listener);
    		}
    		dependentListeners.clear();
    	}
    		
    }
}
