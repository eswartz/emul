/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
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
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
    private final ArrayList<StyleRange> style_ranges = new ArrayList<StyleRange>();
    private final HashMap<RGB,Color> colors = new HashMap<RGB,Color>();

    private final ITextPresentationListener presentation_listener = new ITextPresentationListener() {
        public void applyTextPresentation(TextPresentation presentation) {
            for (StyleRange r : style_ranges) presentation.addStyleRange(r);
        }
    };

    public Control createControl(Composite parent) {
        assert source_viewer == null;
        source_viewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
        source_viewer.configure(new SourceViewerConfiguration());
        source_viewer.setDocument(document);
        source_viewer.setEditable(false);
        source_viewer.addTextPresentationListener(presentation_listener);
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
                final StyledStringBuffer s = getDetailText(nodes, this);
                if (s == null) return;
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (g != generation) return;
                        document.set(getStyleRanges(s));
                    }
                });
            }
        });
    }

    private StyledStringBuffer getDetailText(ArrayList<TCFNode> nodes, Runnable done) {
        StyledStringBuffer bf = new StyledStringBuffer();
        for (TCFNode n : nodes) {
            if (n instanceof IDetailsProvider) {
                if (!((IDetailsProvider)n).getDetailText(bf, done)) return null;
            }
        }
        return bf;
    }

    private String getStyleRanges(StyledStringBuffer s) {
        style_ranges.clear();
        for (StyledStringBuffer.Style x : s.getStyle()) {
            style_ranges.add(new StyleRange(x.pos, x.len, getColor(x.fg), getColor(x.bg), x.font));
        }
        return s.toString();
    }

    private Color getColor(RGB rgb) {
        if (rgb == null) return null;
        Color c = colors.get(rgb);
        if (c == null) colors.put(rgb, c = new Color(display, rgb));
        return c;
    }

    public void dispose() {
        for (Color c : colors.values()) c.dispose();
        colors.clear();
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
