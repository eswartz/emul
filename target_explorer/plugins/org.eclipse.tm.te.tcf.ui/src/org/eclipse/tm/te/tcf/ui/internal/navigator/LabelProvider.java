/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.navigator;

import org.eclipse.jface.viewers.DecoratingLabelProvider;

/**
 * Target Explorer: Decorating label provider.
 */
public class LabelProvider extends DecoratingLabelProvider {
	private final static LabelProviderDelegate DELEGATE = new LabelProviderDelegate();

	/**
	 * Constructor.
	 */
	public LabelProvider() {
		super(DELEGATE, DELEGATE);
	}

}
