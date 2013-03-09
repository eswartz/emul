/**
 * 
 */
package v9t9.gui.client.swt.shells.modules;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import v9t9.common.modules.IModule;

/**
 * This filter only allows through module entries for
 * which all the ROM segments exist.
 * @author ejs
 *
 */
class ExistingModulesFilter extends ViewerFilter {

	/**
	 * 
	 */
	private final ModuleSelector moduleSelector;
	/**
	 * @param moduleSelector
	 */
	ExistingModulesFilter(ModuleSelector moduleSelector) {
		this.moduleSelector = moduleSelector;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#isFilterProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isFilterProperty(Object element, String property) {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement,
			Object element) {
		if (this.moduleSelector.isShowMissingModules() || !(element instanceof IModule))
			return true;
		
		IModule module = (IModule) element;
		return this.moduleSelector.isModuleLoadable(module);
	}
	
}