/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.Activator;
import org.eclipse.tm.tcf.internal.nls.TcfPluginMessages;


/**
 * Abstract extension point manager base implementation.
 */
public abstract class TcfAbstractExtensionPointManager<V> {
    // Flag to mark the extension point manager initialized (extensions loaded).
    private boolean fInitialized = false;
    // The map of loaded extension listed by their unique ids
    private Map<String, TcfExtensionProxy<V>> fExtensions = new LinkedHashMap<String, TcfExtensionProxy<V>>();
    // The extension point comparator
    private TcfExtensionPointComparator fComparator = null;

    /**
     * Constructor.
     */
    public TcfAbstractExtensionPointManager() {
    }

    /**
     * Returns if or if not the service provider extension point manager
     * got initialized. Initialized means that the manager read the
     * contributitions for the managed extension point.
     *
     * @return <code>True</code> if already initialized, <code>false</code> otherwise.
     */
    protected boolean isInitialized() {
        return fInitialized;
    }

    /**
     * Sets if or if not the service provider extension point manager
     * is initialized. Initialized means that the manager has read
     * the contributitions for the managed extension point.
     *
     * @return <code>True</code> to set the extension point manager is initialized, <code>false</code> otherwise.
     */
    protected void setInitialized(boolean initialized) {
        fInitialized = initialized;
    }

    /**
     * Returns the map of managed extensions. If not loaded before,
     * this methods trigger the loading of the extensions to the managed
     * extension point.
     *
     * @return The map of contributables.
     */
    protected Map<String, TcfExtensionProxy<V>> getExtensions() {
            if (!isInitialized()) { loadExtensions(); setInitialized(true); }
            return fExtensions;
    }

    /**
     * Returns the extensions of the specified extension point sorted.
     * For the order of the extensions, see {@link WRLaunchExtensionPointComparator}.
     *
     * @param point The extension point. Must be not <code>null</code>.
     * @return The extensions in sorted order or an empty array if the extension point has no extensions.
     */
    protected IExtension[] getExtensionsSorted(IExtensionPoint point) {
        assert point != null;

        List<IExtension> extensions = new ArrayList<IExtension>(Arrays.asList(point.getExtensions()));
        if (extensions.size() > 0) {
            Collections.sort(extensions, getExtensionPointComparator());
        }

        return extensions.toArray(new IExtension[extensions.size()]);
    }

    /**
     * Returns the extension point comparator instance. If not available,
     * {@link #doCreateExtensionPointComparator()} is called to create a new instance.
     *
     * @return The extension point comparator or <code>null</code> if the instance creation fails.
     */
    protected final TcfExtensionPointComparator getExtensionPointComparator() {
        if (fComparator == null) {
            fComparator = doCreateExtensionPointComparator();
        }
        return fComparator;
    }

    /**
     * Creates a new extension point comparator instance.
     *
     * @return The extension point comparator instance. Must never be <code>null</code>.
     */
    protected TcfExtensionPointComparator doCreateExtensionPointComparator() {
        return new TcfExtensionPointComparator();
    }

    /**
     * Returns the extension point id to read. The method
     * must return never <code>null</code>.
     *
     * @return The extension point id.
     */
    protected abstract String getExtensionPointId();

    /**
     * Returns the configuration element name. The method
     * must return never <code>null</code>.
     *
     * @return The configuration element name.
     */
    protected abstract String getConfigurationElementName();

    /**
     * Creates the extension proxy instance.
     *
     * @param element The configuration element of the extension. Must be not <code>null</code>.
     * @return The extension proxy instance.
     *
     * @throws CoreException If the extension proxy instanciation failed.
     */
    protected TcfExtensionProxy<V> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
        assert element != null;
        return new TcfExtensionProxy<V>(element);
    }

    /**
     * Loads the extensions for the managed extenions point.
     */
    protected void loadExtensions() {
        // If already initialized, this method will do nothing.
        if (isInitialized()) return;

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(getExtensionPointId());
        if (point != null) {
            IExtension[] extensions = getExtensionsSorted(point);
            for (IExtension extension : extensions) {
                IConfigurationElement[] elements = extension.getConfigurationElements();
                for (IConfigurationElement element : elements) {
                    if (getConfigurationElementName().equals(element.getName())) {
                        try {
                            TcfExtensionProxy<V> candidate = doCreateExtensionProxy(element);
                            if (candidate.getId() != null) {
                                // If no contributable with this id had been registered before, register now.
                                if (!fExtensions.containsKey(candidate.getId())) {
                                    fExtensions.put(candidate.getId(), candidate);
                                }
                                else {
                                    throw new CoreException(new Status(IStatus.ERROR,
                                            Activator.PLUGIN_ID,
                                            0,
                                            NLS.bind(TcfPluginMessages.Extension_error_duplicateExtension, candidate.getId(), element.getContributor().getName()),
                                            null));
                                }
                            } else {
                                throw new CoreException(new Status(IStatus.ERROR,
                                        Activator.PLUGIN_ID,
                                        0,
                                        NLS.bind(TcfPluginMessages.Extension_error_missingRequiredAttribute, "id", element.getAttribute("label")), //$NON-NLS-1$ //$NON-NLS-2$
                                        null));
                            }
                        } catch (CoreException e) {
                            IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                                        NLS.bind(TcfPluginMessages.Extension_error_invalidExtensionPoint, element.getDeclaringExtension().getUniqueIdentifier()),
                                                        e);
                            Activator.getDefault().getLog().log(status);
                        }
                    }
                }
            }
        }
    }
}
