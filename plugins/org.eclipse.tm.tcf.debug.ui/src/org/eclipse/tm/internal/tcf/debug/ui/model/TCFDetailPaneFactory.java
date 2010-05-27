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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The TCF detail pane factory is contributed to the <code>org.eclipse.debug.ui.detailPaneFactories</code>
 * extension. For any selection that contains TCFNode the factory can produce a <code>IDetailPane</code> object.
 */
public class TCFDetailPaneFactory implements IDetailPaneFactory {

    public IDetailPane createDetailPane(String paneID) {
        assert paneID.equals(TCFDetailPane.ID);
        return new TCFDetailPane();
    }

    public String getDefaultDetailPane(IStructuredSelection selection) {
        return TCFDetailPane.ID;
    }

    public String getDetailPaneDescription(String paneID) {
        return TCFDetailPane.NAME;
    }

    public String getDetailPaneName(String paneID) {
        return TCFDetailPane.DESC;
    }

    @SuppressWarnings("unchecked")
    public Set getDetailPaneTypes(IStructuredSelection selection) {
        HashSet<String> set = new HashSet<String>();
        set.add(TCFDetailPane.ID);
        return set;
    }
}
