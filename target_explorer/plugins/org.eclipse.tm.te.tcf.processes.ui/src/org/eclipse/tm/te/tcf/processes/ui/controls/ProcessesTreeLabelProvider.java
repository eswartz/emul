/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.controls;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tm.te.tcf.processes.ui.interfaces.ImageConsts;

/**
 * Processes tree control label provider implementation.
 */
public class ProcessesTreeLabelProvider extends LabelProvider implements ITableLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof ProcessesTreeNode) {
			return ((ProcessesTreeNode) element).name;
		}
		return super.getText(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof ProcessesTreeNode) {
			return UIPlugin.getImage(ImageConsts.OBJ_Process);
		}

		return super.getImage(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) return getImage(element);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 0) return getText(element);

		if (element instanceof ProcessesTreeNode) {
			ProcessesTreeNode node = (ProcessesTreeNode) element;

			// Pending nodes does not have column texts at all
			if (node.type.endsWith("PendingNode")) return ""; //$NON-NLS-1$ //$NON-NLS-2$

			switch (columnIndex) {
			case 1:
				String id = Long.toString(node.pid);
				if (id == null) id = node.id;
				if (id != null) return id.startsWith("P") ? id.substring(1) : id; //$NON-NLS-1$
				break;
			case 2:
				String ppid = Long.toString(node.ppid);
				if (ppid != null) return ppid;
				break;
			case 3:
				String state = node.state;
				if (state != null) return state;
				break;
			case 4:
				String username = node.username;
				if (username != null) return username;
				break;
			}
		}

		return ""; //$NON-NLS-1$
	}

}
