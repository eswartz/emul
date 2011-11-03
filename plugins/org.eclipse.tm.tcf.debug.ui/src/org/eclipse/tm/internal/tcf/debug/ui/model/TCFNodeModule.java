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

import java.math.BigInteger;

import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.swt.SWT;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IMemoryMap;

/**
 * A node representing a memory region (module).
 */
public class TCFNodeModule extends TCFNode implements IDetailsProvider {

    private final IMemoryMap.MemoryRegion region;

    protected TCFNodeModule(TCFNode parent, String id, IMemoryMap.MemoryRegion region) {
        super(parent, id);
        this.region = region;
    }

    @Override
    protected boolean getData(ILabelUpdate update, Runnable done) {
        String[] col_ids = update.getColumnIds();
        if (col_ids == null) {
            update.setLabel(region.getFileName(), 0);
        }
        else {
            for (int i=0; i < col_ids.length; ++i) {
                String col_id = col_ids[i];
                if (TCFColumnPresentationModules.COL_NAME.equals(col_id)) {
                    update.setLabel(region.getFileName(), i);
                }
                else if (TCFColumnPresentationModules.COL_ADDRESS.equals(col_id)) {
                    update.setLabel(toHexString(region.getAddress()), i);
                }
                else if (TCFColumnPresentationModules.COL_SIZE.equals(col_id)) {
                    update.setLabel(toHexString(region.getSize()), i);
                }
                else if (TCFColumnPresentationModules.COL_FLAGS.equals(col_id)) {
                    update.setLabel(getFlagsLabel(region.getFlags()), i);
                }
                else if (TCFColumnPresentationModules.COL_OFFSET.equals(col_id)) {
                    update.setLabel(toHexString(region.getOffset()), i);
                }
                else if (TCFColumnPresentationModules.COL_SECTION.equals(col_id)) {
                    String sectionName = region.getSectionName();
                    update.setLabel(sectionName != null ? sectionName : "", i);
                }
            }
        }
        update.setImageDescriptor(ImageCache.getImageDescriptor(ImageCache.IMG_MEMORY_MAP), 0);
        return true;
    }

    public boolean getDetailText(StyledStringBuffer bf, Runnable done) {
        bf.append("File: ", SWT.BOLD).append(region.getFileName()).append('\n');
        bf.append("Address: ", SWT.BOLD).append(toHexString(region.getAddress())).append('\n');
        bf.append("Size: ", SWT.BOLD).append(toHexString(region.getSize())).append('\n');
        bf.append("Flags: ", SWT.BOLD).append(getFlagsLabel(region.getFlags())).append('\n');
        bf.append("Offset: ", SWT.BOLD).append(toHexString(region.getOffset())).append('\n');
        String sectionName = region.getSectionName();
        bf.append("Section: ", SWT.BOLD).append(sectionName != null ? sectionName : "<unknown>").append('\n');
        return true;
    }

    private String toHexString(Number address) {
        if (address == null) return "";
        BigInteger addr = JSON.toBigInteger(address);
        String s = addr.toString(16);
        int sz = s.length() <= 8 ? 8 : 16;
        int l = sz - s.length();
        if (l < 0) l = 0;
        if (l > 16) l = 16;
        return "0x0000000000000000".substring(0, 2 + l) + s;
    }

    private String getFlagsLabel(int flags) {
        StringBuilder flagsLabel = new StringBuilder(3);
        if ((flags & IMemoryMap.FLAG_READ) != 0) flagsLabel.append('r');
        else flagsLabel.append('-');
        if ((flags & IMemoryMap.FLAG_WRITE) != 0) flagsLabel.append('w');
        else flagsLabel.append('-');
        if ((flags & IMemoryMap.FLAG_EXECUTE) != 0) flagsLabel.append('x');
        else flagsLabel.append('-');
        return flagsLabel.toString();
    }
}
