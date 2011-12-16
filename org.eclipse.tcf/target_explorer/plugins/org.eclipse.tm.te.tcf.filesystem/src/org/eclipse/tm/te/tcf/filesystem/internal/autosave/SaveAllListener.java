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
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.tm.te.tcf.filesystem.internal.handlers.PersistenceManager;
import org.eclipse.tm.te.tcf.filesystem.internal.handlers.StateManager;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.model.CacheState;
import org.eclipse.tm.te.tcf.filesystem.model.FSModel;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The execution listener of command "SAVE ALL", which synchronizes the local
 * file with the one on the target server after it is saved.
 */
public class SaveAllListener implements IExecutionListener {
	// Dirty nodes that should be saved and synchronized.
	private List<FSTreeNode> fDirtyNodes;
	// The file system fModel storing the existing FSTreeNodes.
	private FSModel fModel;
	/**
	 * Create the listener listening to command "SAVE ALL".
	 */
	public SaveAllListener() {
		this.fModel = FSModel.getInstance();
		this.fDirtyNodes = new ArrayList<FSTreeNode>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IExecutionListener#postExecuteSuccess(java.lang.String, java.lang.Object)
	 */
	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
		if (!fDirtyNodes.isEmpty()) {
			try {
				List<FSTreeNode> modified = new ArrayList<FSTreeNode>();
				List<FSTreeNode> conflicts = new ArrayList<FSTreeNode>();
				for (FSTreeNode node : fDirtyNodes) {
					// Refresh the dirty nodes and get their latest states.
					StateManager.getInstance().refreshState(node);
					CacheState state = StateManager.getInstance().getCacheState(node);
					switch (state) {
					case consistent:
						break;
					case outdated:
						break;
					case modified:
						// Reclassifying
						modified.add(node);
						break;
					case conflict:
						// Reclassifying
						conflicts.add(node);
						break;
					}
				}

				if (PersistenceManager.getInstance().isAutoSaving()) {
					// If auto saving is on.
					if (!modified.isEmpty()) {
						// Upload the modified nodes.
						CacheManager.getInstance().upload(modified.toArray(new FSTreeNode[modified.size()]));
					}
					if (!conflicts.isEmpty()) {
						// Merge the conflicting ones.
						mergeConflicts(conflicts);
					}
				}
			} catch (TCFException tcfe) {
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(parent, Messages.StateManager_RefreshFailureTitle, tcfe.getLocalizedMessage());
			}
		}
	}

	/**
	 * Merge those conflicting nodes.
	 *
	 * @param conflicts The conflicting nodes.
	 */
	private void mergeConflicts(List<FSTreeNode> conflicts) {
		for (FSTreeNode node : conflicts) {
			String title = Messages.SaveAllListener_StateChangedDialogTitle;
			String message = NLS.bind(Messages.SaveAllListener_SingularMessage, node.name);
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			Shell parent = window.getShell();
			MessageDialog msgDialog = new MessageDialog(parent, title, null, message,
					MessageDialog.QUESTION, new String[] { Messages.SaveAllListener_Merge,
					Messages.SaveAllListener_SaveAnyway, Messages.SaveAllListener_Cancel }, 0);
			int index = msgDialog.open();
			if (index == 0) { // Merge
				LocalTypedElement local = new LocalTypedElement(node);
				RemoteTypedElement remote = new RemoteTypedElement(node);
				MergeEditorInput mergeInput = new MergeEditorInput(local, remote, page);
				CompareUI.openCompareDialog(mergeInput);
			} else if (index == 1) { // Save anyway
				CacheManager.getInstance().upload(conflicts.toArray(new FSTreeNode[conflicts.size()]));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IExecutionListener#preExecute(java.lang.String, org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
		fDirtyNodes.clear();
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		IEditorPart[] editors = page.getDirtyEditors();
		for (IEditorPart editor : editors) {
			IEditorInput input = editor.getEditorInput();
			FSTreeNode node = getEditedNode(input);
			if (node != null) {
				// If it is a modified node, add it to the dirty node list.
				fDirtyNodes.add(node);
			}
		}
	}

	/**
	 * Get the corresponding FSTreeNode from the input.
	 * If the input has no corresponding FSTreeNode, return null;
	 * @param input The editor input.
	 * @return The corresponding FSTreeNode or null if it has not.
	 */
	private FSTreeNode getEditedNode(IEditorInput input){
		if (input instanceof IURIEditorInput) {
			//Get the file that is being edited.
			IURIEditorInput fileInput = (IURIEditorInput) input;
			URI uri = fileInput.getURI();
			try {
				IFileStore store = EFS.getStore(uri);
				File localFile = store.toLocalFile(0, new NullProgressMonitor());
				if (localFile != null) {
					// Get the file's mapped FSTreeNode.
					FSTreeNode node = fModel.getTreeNode(localFile.toString());
					return node;
				}
			}catch(CoreException e){}
		}
		return null;
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
