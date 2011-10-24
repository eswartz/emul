/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.stepper.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Stepper Runtime plugin externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tm.te.runtime.stepper.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String AbstractContextStep_error_missingRequiredAttribute;
	public static String AbstractContextStep_warning_stepFinishedWithWarnings;

	public static String AbstractContextStepper_error_typeAndSubtype;
	public static String AbstractContextStepper_error_stepGroup;
	public static String AbstractContextStepper_error_step;
	public static String AbstractContextStepper_error_referencedBaseGroup;
	public static String AbstractContextStepper_error_referencedStepOrGroup;
	public static String AbstractContextStepper_error_requiredStepOrGroup;
	public static String AbstractContextStepper_error_requiredStep;
	public static String AbstractContextStepper_error_initializeNotCalled;
	public static String AbstractContextStepper_error_missingStepGroup;
	public static String AbstractContextStepper_multiStatus_finishedWithWarnings;
	public static String AbstractContextStepper_multiStatus_finishedWithErrors;
	public static String AbstractContextStepper_error_missingRequiredStep;
	public static String AbstractContextStepper_error_requiredStepNotExecuted;
}
