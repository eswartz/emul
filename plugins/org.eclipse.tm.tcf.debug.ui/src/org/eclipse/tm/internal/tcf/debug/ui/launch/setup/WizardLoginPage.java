/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.launch.setup;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.osgi.framework.Bundle;

class WizardLoginPage extends WizardPage implements Listener {

    private static final String
        PREF_PROTOCOL = "setup.login.protocol",
        PREF_HOST     = "setup.login.host",
        PREF_USER     = "setup.login.user";

    private final SetupWizardDialog wizard;

    final String[] protocols = { "Telnet", "SSH" };

    Combo protocol;
    Button prefs;
    Text host;
    Text user;
    Text user_password;
    Text root_password;

    WizardLoginPage(SetupWizardDialog wizard) {
        super("LoginPage");
        this.wizard = wizard;
        setTitle("Remote TCF agent configuration");
        setDescription("Enter remote host login data");
    }

    public void handleEvent(Event event) {
        getContainer().updateButtons();

        Preferences p = Activator.getDefault().getPluginPreferences();
        p.setValue(PREF_PROTOCOL, protocol.getText());
        p.setValue(PREF_HOST, host.getText());
        p.setValue(PREF_USER, user.getText());

        root_password.setEnabled(!user.getText().equals("root"));
        prefs.setEnabled(protocol.getText().equals(protocols[1]));
    }

    public void createControl(final Composite parent) {
        GridData gd;
        Composite composite =  new Composite(parent, SWT.NULL);
        GridLayout gl = new GridLayout();
        gl.numColumns = 3;
        composite.setLayout(gl);

        new Label(composite, SWT.NONE).setText("Protocol:");
        protocol = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        protocol.setLayoutData(gd);
        protocol.setItems(protocols);
        protocol.addListener(SWT.Selection, this);

        prefs = new Button(composite, SWT.PUSH);
        prefs.setText("Preferences");
        gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.widthHint = 100;
        prefs.setLayoutData(gd);
        prefs.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent n) {
                openProtocolPreferences(parent.getShell(), prefs.getText());
            }
        });

        new Label(composite, SWT.NONE).setText("Host:");
        host = new Text(composite, SWT.BORDER);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.widthHint = 200;
        host.setLayoutData(gd);
        host.addListener(SWT.KeyUp, this);

        new Label(composite, SWT.NONE).setText("User:");
        user = new Text(composite, SWT.BORDER);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.widthHint = 200;
        user.setLayoutData(gd);
        user.addListener(SWT.KeyUp, this);

        new Label(composite, SWT.NONE).setText("Password:");
        user_password = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.widthHint = 200;
        user_password.setLayoutData(gd);
        user_password.addListener(SWT.KeyUp, this);

        new Label(composite, SWT.NONE).setText("Root password:");
        root_password = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.widthHint = 200;
        root_password.setLayoutData(gd);
        root_password.addListener(SWT.KeyUp, this);

        Preferences p = Activator.getDefault().getPluginPreferences();
        p.setDefault(PREF_PROTOCOL, protocols[0]);
        protocol.setText(p.getString(PREF_PROTOCOL));
        host.setText(p.getString(PREF_HOST));
        user.setText(p.getString(PREF_USER));

        root_password.setEnabled(!user.getText().equals("root"));
        prefs.setEnabled(protocol.getText().equals(protocols[1]));

        setControl(composite);
    }

    @SuppressWarnings("unchecked")
    private void openProtocolPreferences(Shell shell, String title) {
        try {
            PreferenceManager mgr = new PreferenceManager();
            IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.ui", "preferencePages");
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                IConfigurationElement[] e = extensions[i].getConfigurationElements();
                for (int j = 0; j < e.length; j++) {
                    String nm = e[j].getName();
                    if (nm.equals("page")) { //$NON-NLS-1$
                        String cnm = e[j].getAttribute("class"); //$NON-NLS-1$
                        if (cnm == null) continue;
                        if (!cnm.startsWith("org.eclipse.jsch.")) continue;
                        String id = e[j].getAttribute("id"); //$NON-NLS-1$
                        if (id == null) id = cnm;
                        Bundle bundle = Platform.getBundle(extensions[i].getNamespaceIdentifier());
                        Class c = bundle.loadClass(cnm);
                        IPreferencePage page = (IPreferencePage)c.newInstance();
                        String pnm = e[j].getAttribute("name"); //$NON-NLS-1$
                        if (pnm != null) page.setTitle(pnm);
                        mgr.addToRoot(new PreferenceNode(id, page));
                    }
                }
            }
            PreferenceDialog dialog = new PreferenceDialog(shell, mgr);
            dialog.create();
            dialog.setMessage(title);
            dialog.open();
        }
        catch (Throwable err) {
            String msg = err.getLocalizedMessage();
            if (msg == null || msg.length() == 0) msg = err.getClass().getName();
            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            mb.setText("Error");
            mb.setMessage("Cannot open preferences dialog:\n" + msg);
            mb.open();
        }
    }

    @Override
    public IWizardPage getNextPage() {
        if (host.getText().length() > 0 &&
                user.getText().length() > 0 &&
                (user_password.getText().length() > 0 || protocol.getText().equals("SSH")) &&
                (!root_password.isEnabled() || root_password.getText().length() > 0))
            return wizard.getPage("LogPage");
        return null;
    }
}