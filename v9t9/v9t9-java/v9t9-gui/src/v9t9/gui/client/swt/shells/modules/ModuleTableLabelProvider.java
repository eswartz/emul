/**
 * 
 */
package v9t9.gui.client.swt.shells.modules;

import java.net.URI;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import v9t9.common.modules.IModule;

class ModuleTableLabelProvider extends LabelProvider implements ITableColorProvider {

	private ModuleSelector selector;

	public ModuleTableLabelProvider(ModuleSelector selector) {
		this.selector = selector;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	@Override
	public Color getForeground(Object element, int columnIndex) {
		if (element instanceof URI) {
			if (selector.getMachine().getRomPathFileLocator().exists((URI) element))
				return null;
			return selector.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
		}
		
		if (!(element instanceof IModule)) {
			return null;
		}
		
		IModule module = (IModule) element;
		if (!selector.isModuleLoadable(module))
			return selector.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	@Override
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}
}