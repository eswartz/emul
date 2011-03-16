/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.filesystem;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * @author DSchaefe
 *
 */
public class FileSystemDropAdapter extends PluginDropAdapter {

	protected FileSystemDropAdapter(TreeViewer viewer) {
		super(viewer);
		setFeedbackEnabled(false);
	}

	@Override
	public boolean performDrop(Object data) {
		String[] source = null;
		if (data instanceof IResource[]) {
			IResource[] resources = (IResource[])data;
			source = new String[resources.length];
			
		} else if (data instanceof String[]) {
			source = (String[])data;
		} else
			return false;
		
		ITarget target = (ITarget)getViewer().getInput();
		FileSystemNode destNode = (FileSystemNode)getCurrentTarget();
		if (!destNode.getDirEntry().attrs.isDirectory()) {
			Object parent = destNode.getParent();
			if (parent instanceof FileSystemNode)
				destNode = (FileSystemNode)parent;
			else
				return false;
		}
		CopyToTargetOperation op = new CopyToTargetOperation(target, source, destNode.getPath());
		op.run();
		
		return true;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferData) {
		if (FileTransfer.getInstance().isSupportedType(transferData)) {
			return true;
		} else if (ResourceTransfer.getInstance().isSupportedType(transferData)) {
			return true;
		}
		
		return false;
	}

}
