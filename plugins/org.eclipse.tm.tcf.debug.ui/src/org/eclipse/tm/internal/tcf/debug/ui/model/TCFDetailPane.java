/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * This detail pane uses a source viewer to display detailed information about the current
 * selection.
 */
public class TCFDetailPane implements IDetailPane {

    public static final String ID = "org.eclipse.tm.tcf.debug.DetailPaneFactory";
    public static final String NAME = "TCF Detail Pane";
    public static final String DESC = "TCF Detail Pane";

    private SourceViewer source_viewer;
    private Display display;
    private int generation;
    @SuppressWarnings("unused")
    private IWorkbenchPartSite part_site;
    private final Document document = new Document();

    public Control createControl(Composite parent) {
        assert source_viewer == null;
        source_viewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
        source_viewer.setDocument(document);
        source_viewer.setEditable(false);
        Control control = source_viewer.getControl();
        GridData gd = new GridData(GridData.FILL_BOTH);
        control.setLayoutData(gd);
        display = control.getDisplay();
        return control;
    }

    public void display(IStructuredSelection selection) {
        if (source_viewer == null) return;
        generation++;
        final int g = generation;
        final ArrayList<TCFNode> nodes = new ArrayList<TCFNode>();
        if (selection != null) {
            Iterator<?> iterator = selection.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next instanceof TCFNode) nodes.add((TCFNode)next);
            }
        }
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (g != generation) return;
                final String s = getDetailText(nodes, this);
                if (s == null) return;
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (g != generation) return;
                        document.set(s);
                    }
                });
            }
        });
    }

    private String getDetailText(ArrayList<TCFNode> nodes, Runnable done) {
        StringBuffer bf = new StringBuffer();
        for (TCFNode n : nodes) {
            if (n instanceof TCFNodeExpression) {
                String s = ((TCFNodeExpression)n).getDetailText(done);
                if (s == null) return null;
                bf.append(s);
            }
            else if (n instanceof TCFNodeRegister) {
                String s = ((TCFNodeRegister)n).getDetailText(done);
                if (s == null) return null;
                bf.append(s);
            }
        }
        return bf.toString();
    }

    public void dispose() {
        if (source_viewer == null) return;
        generation++;
        if (source_viewer.getControl() != null) {
            source_viewer.getControl().dispose();
        }
        source_viewer = null;
    }

    public String getDescription() {
        return DESC;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return NAME;
    }

    public void init(IWorkbenchPartSite part_site) {
        this.part_site = part_site;
    }

    public boolean setFocus() {
        if (source_viewer == null) return false;
        source_viewer.getTextWidget().setFocus();
        return true;
    }
}
