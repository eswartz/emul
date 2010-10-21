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
package org.eclipse.tcf.internal.target.ui.actions;

import org.eclipse.tcf.internal.target.ui.Activator;
import org.eclipse.tcf.internal.target.ui.editors.TargetEditor;
import org.eclipse.tcf.internal.target.ui.editors.TargetEditorInput;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.ide.IDE;

public class InspectTargetAction extends BaseSelectionListenerAction {

	private final IWorkbenchPage page;
	
	public InspectTargetAction(IWorkbenchPage page) {
		super("Inspect");
		this.page = page;
	}
	
	@Override
	public void run() {
		Object element = getStructuredSelection().getFirstElement();
		if (element instanceof ITarget) {
			try {
				IDE.openEditor(page, new TargetEditorInput((ITarget)element), TargetEditor.EDITOR_ID);
			} catch (PartInitException e) {
				Activator.log(e.getStatus());
			}
		}
	}	
}
