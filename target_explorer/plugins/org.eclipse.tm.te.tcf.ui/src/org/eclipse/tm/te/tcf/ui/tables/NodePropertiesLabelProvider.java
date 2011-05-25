/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.tables;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.ui.internal.nls.Messages;
import org.eclipse.tm.te.ui.tables.TableNode;


/**
 * Target Explorer: TCF node properties table label provider implementation.
 */
public class NodePropertiesLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
	// Reference to the parent table viewer
	private final TableViewer parentViewer;

	/**
	 * Constructor.
	 *
	 * @param viewer The table viewer or <code>null</code>.
	 */
	public NodePropertiesLabelProvider(TableViewer viewer) {
		super();
		parentViewer = viewer;
	}

	/**
	 * Returns the parent table viewer instance.
	 *
	 * @return The parent table viewer or <code>null</code>.
	 */
	protected final TableViewer getParentViewer() {
		return parentViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.datasource.controls.tables.TableLabelProvider#getColumnText(org.eclipse.tm.te.tcf.core.runtime.model.interfaces.IModelNode, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		Assert.isNotNull(element);

		String label = null;

		if (element instanceof TableNode) {
			switch (columnIndex) {
				case 0:
					label = ((TableNode)element).name;
					break;
				case 1:
					label = ((TableNode)element).value;
					break;
			}

			if (label != null) {
				if (IPeerModelProperties.PROP_LAST_SCANNER_ERROR.equals(label)) {
					label = Messages.NodePropertiesLabelProvider_lastScannerError;
				} else if (IPeerModelProperties.PROP_STATE.equals(label)) {
					label = Messages.NodePropertiesLabelProvider_state;
				} else if (IPeerModelProperties.PROP_LOCAL_SERVICES.equals(label)) {
					label = Messages.NodePropertiesLabelProvider_services_local;
				} else if (IPeerModelProperties.PROP_REMOTE_SERVICES.equals(label)) {
					label = Messages.NodePropertiesLabelProvider_services_remote;
				} else if (columnIndex == 1 && IPeerModelProperties.PROP_STATE.equals(((TableNode)element).name)) {
					label = Messages.getString("NodePropertiesLabelProvider_state_" + label.replace('-', '_')); //$NON-NLS-1$
				}
			}
		}

		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		if (element instanceof TableNode && IPeerModelProperties.PROP_LAST_SCANNER_ERROR.equals(((TableNode)element).name)) {
			return getParentViewer().getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}


}
