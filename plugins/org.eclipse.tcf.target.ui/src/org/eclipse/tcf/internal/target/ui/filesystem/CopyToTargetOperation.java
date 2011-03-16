/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.filesystem;

import org.eclipse.tcf.target.core.ITarget;

/**
 * @author DSchaefe
 *
 */
public class CopyToTargetOperation {

	private final ITarget target;
	private final String[] source;
	private final String destination;
	
	public CopyToTargetOperation(ITarget target, String[] source, String destination) {
		this.target = target;
		this.source = source;
		this.destination = destination;
	}
	
	public void run() {
		for (String src : source)
			System.out.println("Copy " + src + " to " + target.getName() + ":" + destination);
	}
	
}
