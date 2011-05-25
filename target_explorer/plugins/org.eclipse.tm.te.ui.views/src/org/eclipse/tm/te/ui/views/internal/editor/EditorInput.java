/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tm.te.ui.views.activator.UIPlugin;
import org.eclipse.tm.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tm.te.ui.views.interfaces.ImageConsts;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;


/**
 * Target Explorer: Details editor input implementation.
 */
public class EditorInput implements IEditorInput, IPersistableElement {
	// The parent editor id
	private final String id;
	// The editor input name, once determined
	private String name;
	// The node (selection) the editor is showing
	private final Object node;

	/**
	 * Constructor.
	 *
	 * @param node The node (selection) the editor is showing. Must not be <code>null</code>.
	 */
	public EditorInput(Object node) {
		this(node, IUIConstants.ID_EDITOR);
	}

	/**
	 * Constructor.
	 *
	 * @param node The node (selection) the editor is showing. Must not be <code>null</code>.
	 * @param id The parent editor id or <code>null</code>
	 */
	public EditorInput(Object node, String id) {
		super();
		this.id = id;
		Assert.isNotNull(node);
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (node != null && obj instanceof EditorInput) {
			return node.equals(((EditorInput)obj).node)
						&& (id != null ? id.equals(((EditorInput)obj).id) : ((EditorInput)obj).id == null);
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node != null ? node.hashCode() << 16 + (id != null ? id.hashCode() : 0) : super.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return node != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return UIPlugin.getImageDescriptor(ImageConsts.IMAGE_EDITOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		if (name == null && node != null) {
			CommonViewer viewer = getViewer();
			name = viewer != null && viewer.getLabelProvider() instanceof ILabelProvider ? ((ILabelProvider)viewer.getLabelProvider()).getText(node) : node.toString();
		}

		return name != null ? name : ""; //$NON-NLS-1$
	}

	/**
	 * Get the common viewer used by the Target Explorer view instance.
	 *
	 * @return The common viewer or <code>null</code>
	 */
	protected CommonViewer getViewer() {
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart part = page.findView(IUIConstants.ID_EXPLORER);
			if (part instanceof CommonNavigator) {
				return ((CommonNavigator)part).getCommonViewer();
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		// We cannot persist this kind of editor input.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	public String getFactoryId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (IPersistableElement.class.isAssignableFrom(adapter)) {
			return getPersistable();
		}

		// If the adapter can be applied to the node instance, return the node
		Object adapted = Platform.getAdapterManager().getAdapter(node, adapter);
		if (adapted != null) return adapted;

		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
