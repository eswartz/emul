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
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IWorkbenchWindow;

public class CastToArrayCommand extends AbstractActionDelegate {

    private class CastToTypeInputValidator implements IInputValidator {

        public CastToTypeInputValidator() {
        }

        public String isValid(String new_text) {
            try {
                if (new_text.length() == 0) return "";
                int i = Integer.parseInt(new_text);
                if (i < 1) return "Array length must be >= 1";
            }
            catch (Exception x) {
                return "Invalid number";
            }
            return null;
        }
    }

    private class CastToTypeDialog extends InputDialog {

        public CastToTypeDialog(Shell shell, String initial_value) {
            super(shell, "Cast To Array", "Enter array length",
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
        final String base_type_name = getBaseTypeName();
        if (base_type_name == null) return;
        CastToTypeDialog dialog = new CastToTypeDialog(window.getShell(), node.getModel().getCastToType(node.getID()));
        if (dialog.open() != Window.OK) return;
        final String new_type = dialog.getValue().trim();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                node.getModel().setCastToType(node.getID(), base_type_name + "[" + new_type + "]");
            }
        });
    }

    private String getBaseTypeName() {
        final TCFNode node = getCastToTypeNode();
        if (node == null) return null;
        return new TCFTask<String>(node.getChannel()) {
            public void run() {
                TCFDataCache<ISymbols.Symbol> type_cache = ((ICastToType)node).getType();
                if (!type_cache.validate(this)) return;
                ISymbols.Symbol type_data = type_cache.getData();
                if (type_data == null || type_data.getTypeClass() != ISymbols.TypeClass.pointer) {
                    done(null);
                }
                else {
                    String ptrs = "****";
                    for (int i = 0; i <= ptrs.length(); i++) {
                        TCFDataCache<ISymbols.Symbol> base_type_cache = node.getModel().getSymbolInfoCache(type_data.getBaseTypeID());
                        if (!base_type_cache.validate(this)) return;
                        ISymbols.Symbol base_type_data = base_type_cache.getData();
                        if (base_type_data == null) {
                            done(null);
                        }
                        else {
                            if (base_type_data.getName() != null) {
                                done(base_type_data.getName() + ptrs.substring(0, i));
                                return;
                            }
                            else if (base_type_data.getTypeClass() == ISymbols.TypeClass.pointer) {
                                type_data = base_type_data;
                            }
                            else {
                                done("\"" + base_type_data.getID() + "\"" + ptrs.substring(0, i));
                                return;
                            }
                        }
                    }
                }
            }
        }.getE();
    }

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(getBaseTypeName() != null);
    }

    private TCFNode getCastToTypeNode() {
        TCFNode node = getSelectedNode();
        if (node instanceof ICastToType) return node;
        return null;
    }
}
