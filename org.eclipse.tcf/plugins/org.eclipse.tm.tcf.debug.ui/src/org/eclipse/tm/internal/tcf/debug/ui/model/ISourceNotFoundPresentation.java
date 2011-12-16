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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.IEditorInput;

/**
 * Adapter to customize source presentation for the source-not-found case.
 */
public interface ISourceNotFoundPresentation {

    /**
     * Returns an editor input that should be used to display a source-not-found editor
     * for the given object or <code>null</code> if unable to provide an editor input
     * for the given object.
     *
     * @param element a debug model element
     * @param cfg the launch configuration of the debug element
     * @param file unresolved source file path
     * @return an editor input, or <code>null</code> if none
     */
    public IEditorInput getEditorInput(Object element, ILaunchConfiguration cfg, String file);

    /**
     * Returns the id of the source-not-found editor to use to display the
     * given editor input and object, or <code>null</code> if
     * unable to provide an editor id.
     *
     * @param input an editor input that was previously retrieved from this
     *    presentation's <code>getEditorInput</code> method
     * @param element the object that was used in the call to
     *  <code>getEditorInput</code>, that corresponds to the given editor
     *  input
     * @return an editor id, or <code>null</code> if none
     */
    public String getEditorId(IEditorInput input, Object element);

}
