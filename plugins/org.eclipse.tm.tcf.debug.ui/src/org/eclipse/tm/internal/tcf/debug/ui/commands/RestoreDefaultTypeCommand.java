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

import org.eclipse.tm.internal.tcf.debug.ui.model.ICastToType;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.protocol.Protocol;

public class RestoreDefaultTypeCommand extends AbstractActionDelegate {

    @Override
    protected void run() {
        final TCFNode node = getCastToTypeNode();
        if (node == null) return;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                node.getModel().setCastToType(node.getID(), null);
            }
        });
    }

    @Override
    protected void selectionChanged() {
        TCFNode node = getCastToTypeNode();
        getAction().setEnabled(node != null && node.getModel().getCastToType(node.getID()) != null);
    }

    private TCFNode getCastToTypeNode() {
        TCFNode node = getSelectedNode();
        if (node instanceof ICastToType) return node;
        return null;
    }
}
