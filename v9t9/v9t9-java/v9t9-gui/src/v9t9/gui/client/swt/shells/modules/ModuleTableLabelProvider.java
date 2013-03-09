/**
 * 
 */
package v9t9.gui.client.swt.shells.modules;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;

class ModuleTableLabelProvider extends LabelProvider implements ITableLabelProvider,
	ITableColorProvider {

	private ModuleSelector selector;

	public ModuleTableLabelProvider(ModuleSelector selector) {
		this.selector = selector;
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof IModule)) {
			if (columnIndex == ModuleSelector.NAME_COLUMN)
				return selector.getModuleListImage();
		}
		IModule module = (IModule) element;
		switch (columnIndex) {
		case ModuleSelector.NAME_COLUMN: 
			{
				return selector.getOrLoadModuleImage(element, module, module.getImagePath());
			}
		default:
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (false == element instanceof IModule && columnIndex == ModuleSelector.NAME_COLUMN)
			return element.toString();
		if (!(element instanceof IModule)) {
			return null;
		}
		IModule module = (IModule) element;
		switch (columnIndex) {
		case ModuleSelector.NAME_COLUMN: return module.getName();
		case ModuleSelector.FILE_COLUMN: {
			for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
				Object v = info.getProperties().get(MemoryEntryInfo.FILENAME);
				if (v != null)
					return v.toString();
			}
		}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	@Override
	public Color getForeground(Object element, int columnIndex) {
		if (!(element instanceof IModule))
			return null;
		
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