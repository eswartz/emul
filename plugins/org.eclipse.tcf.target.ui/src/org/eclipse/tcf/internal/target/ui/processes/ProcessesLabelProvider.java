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
package org.eclipse.tcf.internal.target.ui.processes;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @author dschaefer
 *
 */
public class ProcessesLabelProvider implements ILabelProvider {
	
	@Override
	public void addListener(ILabelProviderListener listener) {
	}
	
	@Override
	public void dispose() {
	}
	
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	
	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getText(Object element) {
		if (element instanceof ProcessesContentProvider.RootNode)
			return "Processes";
		else if (element instanceof ProcessesNode)
			return ((ProcessesNode)element).getName();
		else if (element == ProcessesContentProvider.pending)
			return element.toString();
		else
			return null;
	}
	
}
