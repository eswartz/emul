/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
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
import java.util.Collection;

import org.eclipse.swt.graphics.RGB;

class StyledStringBuffer {

    private final StringBuffer bf = new StringBuffer();
    private final ArrayList<Style> styles = new ArrayList<Style>();

    static class Style {
        int pos;
        int len;
        int font;
        RGB bg;
        RGB fg;
    }

    StyledStringBuffer append(int pos, int font, RGB bg, RGB fg) {
        Style x = new Style();
        x.pos = pos;
        x.len = bf.length() - pos;
        x.font = font;
        x.bg = bg;
        x.fg = fg;
        styles.add(x);
        return this;
    }

    StyledStringBuffer append(String s) {
        bf.append(s);
        return this;
    }

    StyledStringBuffer append(char ch) {
        bf.append(ch);
        return this;
    }

    StyledStringBuffer append(int i) {
        bf.append(i);
        return this;
    }

    StyledStringBuffer append(String s, int font) {
        Style x = new Style();
        x.pos = bf.length();
        x.len = s.length();
        x.font = font;
        styles.add(x);
        bf.append(s);
        return this;
    }

    StyledStringBuffer append(String s, int font, RGB bg, RGB fg) {
        Style x = new Style();
        x.pos = bf.length();
        x.len = s.length();
        x.font = font;
        x.bg = bg;
        x.fg = fg;
        styles.add(x);
        bf.append(s);
        return this;
    }

    StyledStringBuffer append(StyledStringBuffer s) {
        int offs = bf.length();
        for (Style y : s.styles) {
            Style x = new Style();
            x.pos = y.pos + offs;
            x.len = y.len;
            x.font = y.font;
            x.bg = y.bg;
            x.fg = y.fg;
            styles.add(x);
        }
        bf.append(s.bf);
        return this;
    }

    StringBuffer getStringBuffer() {
        return bf;
    }

    Collection<Style> getStyle() {
        return styles;
    }

    int length() {
        return bf.length();
    }

    @Override
    public String toString() {
        return bf.toString();
    }
}
