/*
  ModuleContentProvider.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.modules;

import java.net.URI;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
public class ModuleContentProvider implements ITreeContentProvider {

	private ModuleInput input;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.input = (ModuleInput) newInput;
		if (newInput != null)
			viewer.refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object el) {
		return getChildren(el);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object el) {
		if (el instanceof Map) {
			return ((Map<?, ?>) el).keySet().toArray();
		} else if (el instanceof URI) {
			return input.modDb.get(el).toArray(); 
		} else if (el instanceof ModuleInput) {
			Object[] ents = ((ModuleInput) el).modDb.keySet().toArray();
			Object[] nents = new Object[ents.length + 1];
			nents[0] = ((ModuleInput) el).noModule;
			System.arraycopy(ents, 0, nents, 1, ents.length);
			return nents;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object el) {
		if (el instanceof URI)
			return input.modDb;
		else if (el instanceof IModule)
			return ((IModule) el).getDatabaseURI();
		else 
			return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object el) {
		return el instanceof Map || el instanceof URI || el instanceof ModuleInput;
	}

}
