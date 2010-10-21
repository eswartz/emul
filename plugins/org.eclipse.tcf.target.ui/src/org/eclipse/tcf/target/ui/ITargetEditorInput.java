/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.target.ui;

import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.ui.IEditorInput;

public interface ITargetEditorInput extends IEditorInput {

	ITarget getTarget();
	
}
