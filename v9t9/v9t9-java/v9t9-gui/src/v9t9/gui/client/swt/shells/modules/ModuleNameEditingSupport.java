/*
  ModuleNameEditingSupport.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
final class ModuleNameEditingSupport extends EditingSupport {
	private final ModuleSelector moduleSelector;

	/**
	 */
	ModuleNameEditingSupport(ModuleSelector moduleSelector, ColumnViewer viewer) {
		super(viewer);
		this.moduleSelector = moduleSelector;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof IModule) {
			IModule module = (IModule) element;
			if (!value.toString().equals(module.getName())) {
				module.setName(value.toString());
				this.moduleSelector.dirtyModuleLists.add(module.getDatabaseURI());
				getViewer().refresh(module);
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
			return new TextCellEditor((Composite) getViewer().getControl());

		return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		return this.moduleSelector.isEditing;
	}
}