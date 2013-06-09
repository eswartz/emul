/**
 * 
 */
package v9t9.common.dsr;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.machine.IMachine;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.ISettingSection;

/**
 * @author ejs
 *
 */
public class SelectableDsrHandler implements ISelectableDsrHandler {

	protected Map<Object,IDsrHandler> selToDsrMap;
	protected IProperty selectorProperty;
	protected IPropertyListener selectorListener;
	protected IDsrHandler currentDsr;
	protected IMachine machine;

	public SelectableDsrHandler(IMachine machine, IProperty selectorProperty,
			Object... valueAndDsrPairs) {
		this.machine = machine;
		selToDsrMap = new HashMap<Object, IDsrHandler>();
		for (int i = 0; i < valueAndDsrPairs.length; i += 2) {
			selToDsrMap.put(valueAndDsrPairs[i], (IDsrHandler) valueAndDsrPairs[i+1]);
		}
		
		this.selectorProperty = selectorProperty;
		selectorListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty property) {
				IDsrHandler newDsr = selToDsrMap.get(property.getValue());
				if (newDsr == null) {
					System.err.println("unknown DSR selection: " + property);
					return;
				}
				if (currentDsr != null) {
					currentDsr.dispose();
				}
				currentDsr = newDsr;
				currentDsr.init();
			}
		};
		selectorProperty.addListenerAndFire(selectorListener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDsrHandler#init()
	 */
	@Override
	public void init() {
		if (currentDsr == null)
			throw new IllegalStateException("no DSR enabled for " + selectorProperty);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDsrHandler#dispose()
	 */
	@Override
	public void dispose() {
		for (IDsrHandler dsr : selToDsrMap.values()) {
			dsr.dispose();
		}
		selToDsrMap.clear();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDsrHandler#getName()
	 */
	@Override
	public String getName() {
		return getCurrentDsr().getName();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDsrHandler#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders() {
		return getCurrentDsr().getDeviceIndicatorProviders();
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		for (IDsrHandler dsr : selToDsrMap.values()) {
			dsr.saveState(section);
		}
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		for (IDsrHandler dsr : selToDsrMap.values()) {
			dsr.loadState(section);
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.IDeviceSettings#getEditableSettingGroups()
	 */
	@Override
	public Map<String, Collection<IProperty>> getEditableSettingGroups() {
		return getCurrentDsr().getEditableSettingGroups();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.ISelectableDsr#getSelectionProperty()
	 */
	@Override
	public IProperty getSelectionProperty() {
		return selectorProperty;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.dsr.ISelectableDsr#getCurrentDsr()
	 */
	@Override
	public IDsrHandler getCurrentDsr() {
		return currentDsr;
	}

}
