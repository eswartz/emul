/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.model.ICastToType;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.ui.IWorkbenchWindow;

public class CastToTypeCommand extends AbstractActionDelegate {

    private class CastToTypeInputValidator implements IInputValidator {

        public CastToTypeInputValidator() {
        }

        public String isValid(String new_text) {
            return null;
        }
    }

    private class CastToTypeDialog extends InputDialog {

        public CastToTypeDialog(Shell shell, String initial_value) {
            super(shell, "Cast To Type", "Enter type name",
                    initial_value, new CastToTypeInputValidator() );
        }

        @Override
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setImage(ImageCache.getImage(ImageCache.IMG_TCF));
        }
    }

    @Override
    protected void run() {
        final TCFNode node = getCastToTypeNode();
        if (node == null) return;
        IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (window == null) return;
        CastToTypeDialog dialog = new CastToTypeDialog(window.getShell(), node.getModel().getCastToType(node.getID()));
        if (dialog.open() != Window.OK) return;
        final String new_type = dialog.getValue().trim();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                node.getModel().setCastToType(node.getID(), new_type);
            }
        });
    }

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(getCastToTypeNode() != null);
    }

    private TCFNode getCastToTypeNode() {
        TCFNode node = getSelectedNode();
        if (node instanceof ICastToType) return node;
        return null;
    }
}
