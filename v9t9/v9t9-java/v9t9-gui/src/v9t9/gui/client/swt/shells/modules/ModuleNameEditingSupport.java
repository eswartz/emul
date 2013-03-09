/**
 * 
 */
package v9t9.gui.client.swt.shells.modules;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
final class ModuleNameEditingSupport extends EditingSupport {
	/**
	 * 
	 */
	private final ModuleSelector moduleSelector;
	private final TableViewer viewer;

	/**
	 * @param viewer
	 * @param moduleSelector TODO
	 * @param viewer2
	 */
	ModuleNameEditingSupport(ModuleSelector moduleSelector, TableViewer viewer) {
		super(viewer);
		this.moduleSelector = moduleSelector;
		this.viewer = viewer;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof IModule) {
			IModule module = (IModule) element;
			if (!value.toString().equals(module.getName())) {
				module.setName(value.toString());
				this.moduleSelector.dirtyModuleLists.add(module.getDatabaseURI());
				viewer.refresh(module);
				//viewer.setSelection(new StructuredSelection(module), true);
			}
		}
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof IModule)
			return ((IModule) element).getName();
		return null;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof IModule)
			return new TextCellEditor(viewer.getTable());

		return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		return this.moduleSelector.isEditing;
	}
}