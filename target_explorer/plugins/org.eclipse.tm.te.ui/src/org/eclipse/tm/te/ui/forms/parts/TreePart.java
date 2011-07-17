/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.forms.parts;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Target Explorer: Tree part implementation.
 */
public class TreePart extends AbstractStructuredViewerPart implements ISelectionChangedListener, IDoubleClickListener {

	/**
	 * Constructor.
	 *
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public TreePart(String[] labels) {
		super(labels);

	}

	/**
	 * Creates the tree viewer instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The viewer style.
	 *
	 * @return The tree viewer instance.
	 */
	protected TreeViewer createTreeViewer(Composite parent, int style) {
		return new TreeViewer(parent, style);
	}

	/**
	 * Configures the tree viewer instance.
	 *
	 * @param viewer The tree viewer instance. Must not be <code>null</code<.
	 */
	protected void configureTreeViewer(TreeViewer viewer) {
		Assert.isNotNull(viewer);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				TreePart.this.selectionChanged(e);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				TreePart.this.doubleClick(e);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.forms.parts.AbstractStructuredViewerPart#createStructuredViewer(org.eclipse.swt.widgets.Composite, int, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected StructuredViewer createStructuredViewer(Composite parent, int style, FormToolkit toolkit) {
		Assert.isNotNull(parent);

		// Adjust the style bits
		style |= SWT.H_SCROLL | SWT.V_SCROLL | (toolkit != null ? toolkit.getBorderStyle() : SWT.BORDER);

		TreeViewer viewer = createTreeViewer(parent, style);
		Assert.isNotNull(viewer);
		configureTreeViewer(viewer);

		return viewer;
	}

	/**
	 * Returns the tree viewer instance.
	 *
	 * @return The tree viewer instance or <code>null</code>.
	 */
	protected TreeViewer getTreeViewer() {
		return (TreeViewer)getViewer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
	}

}
