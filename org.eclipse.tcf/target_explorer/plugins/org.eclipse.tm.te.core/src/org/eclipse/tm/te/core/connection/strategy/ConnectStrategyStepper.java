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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.core.connection.interfaces.IConnectStrategy;
import org.eclipse.tm.te.core.connection.managers.ConnectStrategyExtensionPointManager;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tm.te.runtime.stepper.extensions.AbstractContextStepper;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContext;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepExecutor;
import org.eclipse.tm.te.runtime.stepper.interfaces.IContextStepGroup;
import org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tm.te.runtime.stepper.interfaces.IVariantDelegate;
import org.eclipse.tm.te.runtime.stepper.interfaces.tracing.ITraceIds;

/**
 * A connect strategy stepper.
 */
@SuppressWarnings("restriction")
public class ConnectStrategyStepper extends AbstractContextStepper {

	public static final String PROPERTY_CONNECT_STRATEGY = "connectStrategy"; //$NON-NLS-1$

	private final IContext context;
	private final String stepGroupId;

	/**
	 * Constructor.
	 */
	public ConnectStrategyStepper(IContext context, String stepGroupId, boolean cancelable) {
		super();
		this.context = context;
		this.stepGroupId = stepGroupId;
		setCancelable(cancelable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.extensions.AbstractContextStepper#initialize(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void initialize(IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws IllegalStateException {
		Assert.isNotNull(getConnectStrategy(data));
		super.initialize(data, fullQualifiedId, monitor);
	}

	/**
	 * Get the connect strategy.
	 *
	 * @param data The data object.
	 * @return The connect strategy.
	 */
	public static final IConnectStrategy getConnectStrategy(IPropertiesContainer data) {
		Assert.isNotNull(data);
		Assert.isTrue(data.getProperty(PROPERTY_CONNECT_STRATEGY) instanceof IConnectStrategy);
		return (IConnectStrategy)data.getProperty(PROPERTY_CONNECT_STRATEGY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.extensions.ExecutableExtension#getId()
	 */
	@Override
	public String getId() {
		String id = super.getId();
		return id != null ? id : "org.eclipse.tm.te.core.stepper.ConnectStrategyStepper"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepper#getType()
	 */
	@Override
	protected String getType() {
		return getData() != null ? getConnectStrategy(getData()).getId() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepper#getSubType()
	 */
	@Override
	protected String getSubType() {
		return stepGroupId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepper#getName()
	 */
	@Override
	protected String getName() {
		return context.getContextName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepper#getVariantDelegate()
	 */
	@Override
	protected IVariantDelegate getVariantDelegate() throws CoreException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.extensions.AbstractContextStepper#doCreateStepExecutor(org.eclipse.tm.te.runtime.stepper.interfaces.IContextStep, java.lang.String, org.eclipse.tm.te.runtime.stepper.interfaces.IFullQualifiedId)
	 */
	@Override
	protected IContextStepExecutor doCreateStepExecutor(IContextStep step, String secondaryId, IFullQualifiedId fullQualifiedStepId) {
		return new ConnectStrategyStepExecutor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepper#getStepGroup(java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
	public IContextStepGroup getStepGroup(String connectStrategyId, String stepGroupId, String variant) {
		CoreBundleActivator.getTraceHandler().trace("ConnectStrategyStepper#getStepGroup:" //$NON-NLS-1$
														+ " connectStrategyId = '" + connectStrategyId + "'" //$NON-NLS-1$ //$NON-NLS-2$
														+ ", stepGroupId = '" + stepGroupId + "'" //$NON-NLS-1$ //$NON-NLS-2$
														+ (variant != null && variant.length() > 0 ? ", variant = '" + variant + "'" : ""), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
														0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);

		return ConnectStrategyExtensionPointManager.getInstance().getStepGroup(connectStrategyId, stepGroupId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.stepper.AbstractContextStepper#getContexts()
	 */
	@Override
	protected IContext[] getContexts() {
		return new IContext[] { context };
	}
}
