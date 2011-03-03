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
package org.eclipse.tm.internal.tcf.cdt.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * TCF launch delegate for CDT based launch configuration types.
 * The launch configuration is converted on the fly to be compatible
 * with the TCFLaunch.
 */
public class TCFCDTLaunchDelegate extends TCFLaunchDelegate {

    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, final String mode) throws CoreException {
        final ILaunchConfiguration tcfLaunchConfig = convertToTcfConfig(configuration);
        return super.getLaunch(tcfLaunchConfig, mode);
    }

    public void launch(ILaunchConfiguration configuration, final String mode,
            final ILaunch launch, final IProgressMonitor monitor) throws CoreException {

        ILaunchConfiguration tcfLaunchConfig = convertToTcfConfig(configuration);
        final String peerId;
        if (isAttachLaunch(tcfLaunchConfig)) {
            ContextSelection selection = promptForContext(tcfLaunchConfig);
            if (selection == null) {
                // canceled
                throw new CoreException(Status.OK_STATUS);
            }
            launch.setAttribute("attach_to_context", selection.fContextId);
            if (!selection.fIsAttached) {
                launch.setAttribute("attach_to_process", selection.fContextId);
            }
            peerId = selection.fPeerId;
        } else if (isRemoteLaunch(tcfLaunchConfig)) {
            peerId = tcfLaunchConfig.getAttribute(TCFLaunchDelegate.ATTR_PEER_ID, (String) null);
        } else {
            peerId = null;
        }
        if (peerId == null) {
            super.launch(tcfLaunchConfig, mode, launch, monitor);
        } else {
            if (monitor != null) monitor.beginTask("Launching TCF debugger session", 1); //$NON-NLS-1$
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    ((TCFLaunch)launch).launchTCF(mode, peerId);
                    if (monitor != null) monitor.done();
                }
            });
        }
    }

    private boolean isAttachLaunch(ILaunchConfiguration config) throws CoreException {
        String configTypeName = config.getType().getIdentifier();
        return ICDTLaunchConfigurationConstants.ID_LAUNCH_C_ATTACH.equals(configTypeName);
    }

    private boolean isRemoteLaunch(ILaunchConfiguration config) throws CoreException {
        String configTypeName = config.getType().getIdentifier();
        return "org.eclipse.tcf.cdt.launch.remoteApplicationLaunchType".equals(configTypeName);
    }

    private ContextSelection promptForContext(ILaunchConfiguration config) throws CoreException {
        IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200, "", null); //$NON-NLS-1$//$NON-NLS-2$
        IStatus contextPrompt = new Status(IStatus.INFO, "org.eclipse.tm.tcf.cdt.core", 100, "", null); //$NON-NLS-1$//$NON-NLS-2$
        // consult a status handler
        IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
        if (prompter != null) {
            Object result = prompter.handleStatus(contextPrompt, config);
            if (result instanceof ContextSelection) {
                return (ContextSelection) result;
            }
        }
        return null;
    }

    protected ILaunchConfiguration convertToTcfConfig(ILaunchConfiguration orig) throws CoreException {
        ILaunchConfigurationWorkingCopy copy = orig.copy(orig.getName());
        boolean changed = copyStringAttribute(orig, copy, ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, 
                TCFLaunchDelegate.ATTR_PROJECT_NAME);
        if (isAttachLaunch(orig)) {
            changed = setStringAttribute(copy, TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, null) || changed;
            changed = setStringAttribute(copy, TCFLaunchDelegate.ATTR_REMOTE_PROGRAM_FILE, null) || changed;
        } else {
            changed = copyStringAttribute(orig, copy, ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
                    TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE) || changed;
            changed = copyStringAttribute(orig, copy, ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                    TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS) || changed;
            changed = copyStringAttribute(orig, copy, ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, 
                    TCFLaunchDelegate.ATTR_WORKING_DIRECTORY) || changed;
            changed = copyBooleanAttribute(orig, copy, ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, 
                    TCFLaunchDelegate.ATTR_USE_TERMINAL) || changed;
        }
        return changed ? copy.doSave() : orig;
    }

    private boolean copyStringAttribute(ILaunchConfiguration orig, ILaunchConfigurationWorkingCopy copy, String origAttr,
            String newAttr) throws CoreException {
        String newValue = orig.getAttribute(newAttr, (String) null);
        return setStringAttribute(copy, newAttr, newValue);
    }

    private boolean setStringAttribute(ILaunchConfigurationWorkingCopy copy, String attr, String newValue) throws CoreException {
        String origValue = copy.getAttribute(attr, (String) null);
        if (origValue == newValue || origValue != null && origValue.equals(newValue)) {
            return false;
        }
        copy.setAttribute(attr, newValue);
        return true;
    }

    private boolean copyBooleanAttribute(ILaunchConfiguration orig, ILaunchConfigurationWorkingCopy copy, String origAttr,
            String newAttr) throws CoreException {
        boolean origValue = orig.getAttribute(origAttr, false);
        boolean newValue = orig.getAttribute(newAttr, false);
        if (origValue == newValue) {
            return false;
        }
        copy.setAttribute(newAttr, origValue);
        return true;
    }

}
