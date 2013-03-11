/*
  FilteredSearchFilter.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.net.URI;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import v9t9.common.modules.IModule;

class FilteredSearchFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#isFilterProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean isFilterProperty(Object element, String property) {
		return ModuleSelector.NAME_PROPERTY.equals(property);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (ModuleSelector.lastFilter != null) {
			// note: instanceof excludes "<No module>" entry too
			if (element instanceof URI)
				return true;
			if (false == element instanceof IModule)
				return false;
			IModule mod = (IModule) element;
			String lowSearch = ModuleSelector.lastFilter.toLowerCase();
			return mod.getName().toLowerCase().contains(lowSearch)
					|| mod.getKeywords().contains(lowSearch);
		}
		return true;
	}
}