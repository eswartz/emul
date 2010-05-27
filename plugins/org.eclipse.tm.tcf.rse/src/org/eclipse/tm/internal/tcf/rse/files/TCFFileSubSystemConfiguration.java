/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.files;

import java.util.Vector;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchService;
import org.eclipse.rse.subsystems.files.core.ILanguageUtilityFactory;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IHostFileToRemoteFileAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.tm.internal.tcf.rse.ITCFSubSystem;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorService;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorServiceManager;

public class TCFFileSubSystemConfiguration extends FileServiceSubSystemConfiguration {

    private final TCFFileAdapter file_adapter = new TCFFileAdapter();

    @Override
    public ISubSystem createSubSystemInternal(IHost host) {
        TCFConnectorService connectorService = (TCFConnectorService)getConnectorService(host);
        return new FileServiceSubSystem(host, connectorService,
                getFileService(host), getHostFileAdapter(), createSearchService(host));
    }

    public IFileService createFileService(IHost host) {
        return new TCFFileService(host);
    }

    public IHostSearchResultConfiguration createSearchConfiguration(IHost host,
            IHostSearchResultSet resultSet, Object searchTarget,
            SystemSearchString searchString) {
        // TODO Auto-generated method stub
        return null;
    }

    public ISearchService createSearchService(IHost host) {
        // TODO Auto-generated method stub
        return null;
    }

    public IHostFileToRemoteFileAdapter getHostFileAdapter() {
        return file_adapter;
    }

    public ILanguageUtilityFactory getLanguageUtilityFactory(IRemoteFileSubSystem ss) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean supportsArchiveManagement() {
        return false;
    }

    @Override
    public IConnectorService getConnectorService(IHost host) {
        return TCFConnectorServiceManager.getInstance()
            .getConnectorService(host, ITCFSubSystem.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class getServiceImplType() {
        return TCFFileService.class;
    }

    @Override
    public void setConnectorService(IHost host, IConnectorService connectorService) {
        TCFConnectorServiceManager.getInstance().setConnectorService(host, getServiceImplType(), connectorService);
    }

    @Override
    protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr) {
        ISystemFilterPool pool = null;
        try {
            pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true);

            Vector<String> filterStrings = new Vector<String>();
            RemoteFileFilterString myHomeFilterString = new RemoteFileFilterString(this);
            myHomeFilterString.setPath("."); //$NON-NLS-1$
            myHomeFilterString.setFile("*"); //$NON-NLS-1$
            filterStrings.add(myHomeFilterString.toString());
            ISystemFilter filter = mgr.createSystemFilter(pool, "Home", filterStrings); //$NON-NLS-1$
            filter.setNonChangable(true);
            filter.setSingleFilterStringOnly(true);

            filterStrings = new Vector<String>();
            RemoteFileFilterString rootFilesFilterString = new RemoteFileFilterString(this);
            rootFilesFilterString.setPath(""); //$NON-NLS-1$
            rootFilesFilterString.setFile("*"); //$NON-NLS-1$
            filterStrings.add(rootFilesFilterString.toString());
            filter = mgr.createSystemFilter(pool, "Root", filterStrings);                  //$NON-NLS-1$
            filter.setNonChangable(true);
            filter.setSingleFilterStringOnly(true);
        }
        catch (Exception exc) {
            SystemBasePlugin.logError("Error creating default filter pool", exc); //$NON-NLS-1$
        }
        return pool;
    }
}
