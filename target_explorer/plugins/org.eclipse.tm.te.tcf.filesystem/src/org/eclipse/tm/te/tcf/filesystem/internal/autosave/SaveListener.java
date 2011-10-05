/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.autosave;

import java.io.File;
import java.net.URI;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.LocalTypedElement;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.MergeEditorInput;
import org.eclipse.tm.te.tcf.filesystem.internal.compare.RemoteTypedElement;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tm.te.tcf.filesystem.internal.handlers.CacheManager;
import org.eclipse.tm.te.tcf.filesystem.internal.handlers.StateManager;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.model.CacheState;
import org.eclipse.tm.te.tcf.filesystem.model.FSModel;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The execution listener of command "SAVE", which synchronizes the local file
 * with the one on the target server after it is saved.
 */
public class SaveListener implements IExecutionListener {
	// Dirty node that should be committed or merged.
	private FSTreeNode dirtyNode;
	// The file system fModel.
	private FSModel model;

	/**
	 * Create a SaveListener listening to command "SAVE".
	 */
	public SaveListener() {
		this.model = FSModel.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IExecutionListener#postExecuteSuccess(java.lang.String, java.lang.Object)
	 */
	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
		if (dirtyNode != null) {
			try{
			// Refresh the fDirtyNode's state.
			StateManager.getInstance().refreshState(dirtyNode);
			if (CacheManager.getInstance().isAutoSaving()) {
				CacheState state = StateManager.getInstance().getCacheState(dirtyNode);
				switch (state) {
				case conflict:
					String title = Messages.SaveListener_StateChangedDialogTitle;
					String message = NLS.bind(Messages.SaveListener_StateChangedMessage, dirtyNode.name);
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					Shell parent = window.getShell();
					MessageDialog msgDialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION,
							new String[] { Messages.SaveListener_Merge, Messages.SaveListener_SaveAnyway, Messages.SaveListener_Cancel }, 0);
					int index = msgDialog.open();
					if (index == 0) {// Merge
						LocalTypedElement local = new LocalTypedElement(dirtyNode);
						RemoteTypedElement remote = new RemoteTypedElement(dirtyNode);
						MergeEditorInput mergeInput = new MergeEditorInput(local, remote, page);
						CompareUI.openCompareDialog(mergeInput);
					} else if (index == 1) {// Save anyway.
						CacheManager.getInstance().upload(dirtyNode);
					}
					break;
				case modified:
					// Save anyway
					CacheManager.getInstance().upload(dirtyNode);
					break;
				case consistent:
					break;
				case outdated:
					break;
				}
			}
			}catch(TCFException tcfe){
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(parent, Messages.StateManager_RefreshFailureTitle, tcfe.getLocalizedMessage());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IExecutionListener#preExecute(java.lang.String, org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
		dirtyNode = null;
		IEditorInput input = HandlerUtil.getActiveEditorInput(event);
		if (input instanceof IURIEditorInput) {
			IURIEditorInput fileInput = (IURIEditorInput) input;
			URI uri = fileInput.getURI();
			try {
				IFileStore store = EFS.getStore(uri);
				File localFile = store.toLocalFile(0, new NullProgressMonitor());
				if (localFile != null) {
					dirtyNode = model.getTreeNode(localFile.toString());
				}
			}catch(CoreException e){
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IExecutionListener#notHandled(java.lang.String, org.eclipse.core.commands.NotHandledException)
	 */
	@Override
	public void notHandled(String commandId, NotHandledException exception) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IExecutionListener#postExecuteFailure(java.lang.String, org.eclipse.core.commands.ExecutionException)
	 */
	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {
	}
}
