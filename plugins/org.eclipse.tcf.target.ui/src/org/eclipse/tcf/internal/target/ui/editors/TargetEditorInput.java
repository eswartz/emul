/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.ui.ITargetEditorInput;
import org.eclipse.ui.IPersistableElement;

public class TargetEditorInput implements ITargetEditorInput {

	private final ITarget target;
	
	public TargetEditorInput(ITarget target) {
		this.target = target;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITargetEditorInput)
			return target.equals(((ITargetEditorInput)obj).getTarget());
		return super.equals(obj);
	}
	
	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return target.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return target.getName();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (ITarget.class.equals(adapter))
			return target;
		return null;
	}

	@Override
	public ITarget getTarget() {
		return target;
	}

}
