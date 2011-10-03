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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Target Explorer: Table part implementation.
 */
public class TablePart extends AbstractStructuredViewerPart implements ISelectionChangedListener, IDoubleClickListener {

	/**
	 * Constructor.
	 *
	 * @param labels The list of label to apply to the created buttons in the given order. Must not be <code>null</code>.
	 */
	public TablePart(String[] labels) {
		super(labels);
	}

	/**
	 * Creates the table viewer instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param style The viewer style.
	 *
	 * @return The table viewer instance.
	 */
	protected TableViewer createTableViewer(Composite parent, int style) {
		return new TableViewer(parent, style);
	}

	/**
	 * Configures the table viewer instance.
	 *
	 * @param viewer The table viewer instance. Must not be <code>null</code<.
	 */
	protected void configureTableViewer(TableViewer viewer) {
		Assert.isNotNull(viewer);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				TablePart.this.selectionChanged(e);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent e) {
				TablePart.this.doubleClick(e);
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

		TableViewer viewer = createTableViewer(parent, style);
		Assert.isNotNull(viewer);
		configureTableViewer(viewer);

		return viewer;
	}

	/**
	 * Returns the table viewer instance.
	 *
	 * @return The table viewer instance or <code>null</code>.
	 */
	protected TableViewer getTableViewer() {
		return (TableViewer)getViewer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
	}
}
