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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.Activator;
import org.eclipse.tm.tcf.internal.nls.TcfPluginMessages;

/**
 * TCF extension proxy implementation. The use of the proxy asures the
 * lazy plug-in activation policy for the contributing plug-in.
 */
public class TcfExtensionProxy<V> {
    // The extension instance. Create on first access
    private V fInstance;
    // The configuration element
    private final IConfigurationElement fElement;
    // The unique id of the extension.
    private String fId;

    /**
     * Constructor.
     *
     * @param element The configuration element. Must be not <code>null</code>.
     *
     * @throws CoreException In case the configuration element attribute <i>id</i> is <code>null</code> or empty.
     */
    public TcfExtensionProxy(IConfigurationElement element) throws CoreException {
        assert element != null;
        fElement = element;

        // The <id> attribute is mandatory.
        fId = element.getAttribute("id"); //$NON-NLS-1$
        if (fId == null || fId.trim().length() == 0) {
            throw new CoreException(new Status(IStatus.ERROR,
                    Activator.PLUGIN_ID,
                    0,
                    NLS.bind(TcfPluginMessages.Extension_error_missingRequiredAttribute, "id", element.getContributor().getName()), //$NON-NLS-1$
                    null));
        }

        fInstance = null;
    }

    /**
     * Returns the extensions unique id.
     *
     * @return The unique id.
     */
    public String getId() {
        return fId;
    }

    /**
     * Returns the configuration element for this extension.
     *
     * @return The configuration element.
     */
    protected IConfigurationElement getConfigurationElement() {
        return fElement;
    }

    /**
     * Returns the extension class instance. The contributing
     * plug-in will be activated if not yet activated anyway.
     *
     * @return The extension class instance. Might be <code>null</code> if the instanciation fails.
     */
    @SuppressWarnings("unchecked")
    public V getInstance() {
        if (fInstance == null) {
            IConfigurationElement element = getConfigurationElement();
            assert element != null;
            if (element != null && element.getAttribute("class") != null) { //$NON-NLS-1$
                try {
                    fInstance = (V)element.createExecutableExtension("class"); //$NON-NLS-1$
                } catch (Exception e) {
                    // Possible exceptions: CoreException, ClassCastException.
                    IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                            NLS.bind(TcfPluginMessages.Extension_error_invalidExtensionPoint, element.getDeclaringExtension().getUniqueIdentifier()),
                            e);
                    Activator.getDefault().getLog().log(status);
                }
            }
        }
        return fInstance;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // Proxies are equal if they have encapsulate an element
        // with the same unique id
        if (obj instanceof TcfExtensionProxy<?>) {
            return getId().equals(((TcfExtensionProxy<?>)obj).getId());
        }
        return super.equals(obj);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // The hash code of a proxy is the one from the id
        return getId().hashCode();
    }

}
