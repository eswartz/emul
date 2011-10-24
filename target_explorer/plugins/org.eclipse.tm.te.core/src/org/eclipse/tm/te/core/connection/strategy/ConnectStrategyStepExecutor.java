/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.connection.strategy;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.te.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.core.nls.Messages;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.stepper.AbstractContextStepExecutor;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContext;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId;

/**
 * Connect strategy step executor implementation.
 */
public class ConnectStrategyStepExecutor extends AbstractContextStepExecutor<IPropertiesContainer> {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepExecutor#formatMessage(java.lang.String, int, org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.tm.te.runtime.stepper.interfaces.IContext, java.lang.Object)
	 */
	@Override
	protected String formatMessage(String message, int severity, IContextStep<IPropertiesContainer> step, IFullQualifiedId id, IContext context, IPropertiesContainer data) {
		String template = null;

		switch (severity) {
			case IStatus.INFO:
				template = Messages.ConnectStrategyStepExecutor_info_stepFailed;
				break;
			case IStatus.WARNING:
				template = Messages.ConnectStrategyStepExecutor_warning_stepFailed;
				break;
			case IStatus.ERROR:
				template = Messages.ConnectStrategyStepExecutor_error_stepFailed;
				break;
		}

		// If we cannot determine the formatted message template, just return the message as is
		if (template == null) {
			return message;
		}

		// Split the message. The first sentence is shown more prominent on the top,
		// the rest as additional information below the step information.
		String[] splittedMsg = message != null ? message.split("[\t\n\r\f]+", 2) : new String[] { null, null }; //$NON-NLS-1$

		// Format the core message
		String formattedMessage = NLS.bind(template, new String[] { splittedMsg[0],
										   context.getContextName(),
										   ConnectStrategyStepper.getConnectStrategy(data).getLabel(),
										   step.getLabel()
									});

		// Get the context information
		String contextInfo = formatContextInfo(context);
		if (contextInfo != null) {
			formattedMessage += "\n\n" + contextInfo; //$NON-NLS-1$
		}

		// If we have more information available, append them
		if (splittedMsg.length > 1 && splittedMsg[1] != null && !"".equals(splittedMsg[1])) { //$NON-NLS-1$
			formattedMessage += "\n\n" + splittedMsg[1]; //$NON-NLS-1$
		}

		// In debug mode, there is even more information to add
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(1, null)) {
			formattedMessage += "\n\n" + NLS.bind(Messages.ConnectStrategyStepExecutor_stepFailed_debugInfo, id.toString()); //$NON-NLS-1$
		}

		return formattedMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepExecutor#isExceptionMessageFormatted(java.lang.String)
	 */
	@Override
	protected boolean isExceptionMessageFormatted(String message) {
		Assert.isNotNull(message);
		return message.startsWith(Messages.ConnectStrategyStepExecutor_checkPoint_normalizationNeeded);
	}

	/**
	 * Determines additional context information to show in the failure message.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @return The additional context information string or <code>null</code>.
	 */
	protected String formatContextInfo(IContext context) {
		Assert.isNotNull(context);
		return context.getContextInfo();
	}
}
