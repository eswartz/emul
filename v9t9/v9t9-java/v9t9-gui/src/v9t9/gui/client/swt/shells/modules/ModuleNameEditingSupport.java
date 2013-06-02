/*
  ModuleNameEditingSupport.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.net.URI;
import java.util.Collection;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
final class ModuleNameEditingSupport extends EditingSupport {
	private boolean canEdit;
	private Collection<URI> dirtyLists;

	/**
	 */
	public ModuleNameEditingSupport(ColumnViewer viewer, Collection<URI> dirtyModuleLists) {
		super(viewer);
		this.dirtyLists = dirtyModuleLists;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof IModule) {
			IModule module = (IModule) element;
			if (!value.toString().equals(module.getName())) {
				String orig = module.getName();
				for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
					info.getProperties().put(MemoryEntryInfo.NAME,
							info.getName().replace(orig, value.toString()));
				}
				module.setName(value.toString());
				if (dirtyLists != null)
					dirtyLists.add(module.getDatabaseURI());
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
		return canEdit;
	}
	


	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
		
	}
}