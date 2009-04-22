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
package org.eclipse.tm.internal.tcf.debug.ui.launch.setup;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.tcf.core.Base64;
import org.osgi.framework.Bundle;

class WizardLogPage extends WizardPage implements Runnable {
    
    private final SetupWizardDialog wizard;
    
    private Thread thread;
    private String protocol;
    private String host;
    private String user;
    private String user_password;
    private String root_password;
    
    private Text log_text;
    
    private Display display;
    private Shell parent;
    private IRemoteShell shell;

    WizardLogPage(SetupWizardDialog wizard) {
        super("LogPage");
        this.wizard = wizard;
        setTitle("Remote host login");
        setDescription("");
    }

    public void createControl(Composite parent) {
        display = parent.getDisplay();
        this.parent = parent.getShell();
        ((WizardDialog)getContainer()).addPageChangingListener(new IPageChangingListener() {
            public void handlePageChanging(PageChangingEvent event) {
                if (event.getCurrentPage() instanceof WizardLoginPage && event.getTargetPage() == WizardLogPage.this) {
                    runSetupJob((WizardLoginPage)event.getCurrentPage());
                }
            }
        });

        Composite composite =  new Composite(parent, SWT.NULL);
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        composite.setLayout(gl);

        new Label(composite, SWT.WRAP).setText("Agent installation log:");
        
        log_text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        log_text.setLayoutData(gd);
        
        setControl(composite);
    }
    
    private void runSetupJob(WizardLoginPage login) {
        assert display != null;
        assert display.getThread() == Thread.currentThread();
        if (thread != null) return;
        setErrorMessage(null);
        protocol = login.protocol.getText();
        host = login.host.getText();
        user = login.user.getText();
        user_password = login.user_password.getText();
        root_password = login.root_password.getText();
        log_text.setText("Connect to " + host + "\n");
        thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        Throwable error = null;
        try {
            if ("Telnet".equals(protocol)) {
                shell = new TelnetClient(InetAddress.getByName(host), 23, user, user_password);
            }
            else if ("SSH".equals(protocol)) {
                shell = new SSHClient(parent, host, user, user_password);
            }
            else {
                throw new Exception("Invalid protocol name: " + protocol);
            }
            
            String s;
            
            if (!user.equals("root")) {
                send("su", true);
                expect("Password: ", true);
                send(root_password, false);
                s = waitPrompt();
                if (s.length() > 0) throw new Exception(s);
            }
            send("cd /tmp", true);
            s = waitPrompt();
            if (s.length() > 0) throw new Exception(s);
            send("uname -o", true);
            String os = waitPrompt();
            while (os.endsWith("\n")) os = os.substring(0, os.length() - 1);
            send("uname -m", true);
            String machine = waitPrompt();
            while (machine.endsWith("\n")) machine = machine.substring(0, machine.length() - 1);
            String version = "0.0.1";
            String release = "1.fc5";
            
            URL url = null;
            String fnm = null;
            Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
            for (;;) {
                fnm = "tcf-agent-" + version + "-" + release + "." + machine + ".rpm";
                url = FileLocator.find(bundle, new Path("agent/" + os + "/" + machine + "/" + fnm), null);
                if (url != null) break;
                if (machine.equals("i686")) machine = "i586";
                else if (machine.equals("i586")) machine = "i486";
                else if (machine.equals("i486")) machine = "i386";
                else break;
            }
            if (url == null) throw new Exception("Unsupported target OS or CPU");
            
            send("which base64", true);
            s = waitPrompt();
            if (s.indexOf(':') < 0) {
                send("base64 -di >" + fnm, true);
                InputStream inp = url.openStream();
                byte[] buf = new byte[0x100 * 3];
                for (;;) {
                    int len = inp.read(buf);
                    if (len < 0) break;
                    send(new String(Base64.toBase64(buf, 0, len)), false);
                }
                inp.close();
                shell.write("\004");
                s = waitPrompt();
                if (s.length() > 0) throw new Exception(s);
            }
            else {
                send("which uudecode", true);
                s = waitPrompt();
                if (s.indexOf(':') < 0) {
                    send("uudecode", true);
                    send("begin-base64 644 " + fnm, true);
                    InputStream inp = url.openStream();
                    byte[] buf = new byte[0x100 * 3];
                    for (;;) {
                        int len = inp.read(buf);
                        if (len < 0) break;
                        send(new String(Base64.toBase64(buf, 0, len)), false);
                    }
                    inp.close();
                    send("====", true);
                    s = waitPrompt();
                    if (s.length() > 0) throw new Exception(s);
                }
                else {
                    throw new Exception("No base64 or uudecode commands available");
                }
            }
            send("rpm -e tcf-agent", true);
            waitPrompt();
            send("rpm -i " + fnm, true);
            s = waitPrompt();
            if (s.length() > 0) throw new Exception(s);
            send("rm -f " + fnm, true);
            s = waitPrompt();
            if (s.length() > 0) throw new Exception(s);
            if (!user.equals("root")) {
                send("exit", true);
                waitPrompt();
            }
        }
        catch (Throwable x) {
            error = x;
        }
        if (shell != null) {
            try {
                shell.close();
                shell = null;
            }
            catch (Throwable x) {
                if (error != null) error = x;
            }
        }
        done(error);
    }
    
    private void send(final String s, boolean log) throws IOException {
        if (log) {
            display.asyncExec(new Runnable() {
                public void run() {
                    if (log_text.isDisposed()) return;
                    log_text.append("Send: ");
                    log_text.append(s);
                    if (!s.endsWith("\n")) log_text.append("\n");
                }
            });
        }
        shell.write(s);
        shell.write("\n");
        if (log) shell.expect(s);
        shell.expect("\n");
    }
    
    private void expect(final String s, boolean log) throws IOException {
        if (log) {
            display.asyncExec(new Runnable() {
                public void run() {
                    if (log_text.isDisposed()) return;
                    log_text.append("Expect: ");
                    log_text.append(s);
                    if (!s.endsWith("\n")) log_text.append("\n");
                }
            });
        }
        shell.expect(s);
    }
    
    private String waitPrompt() throws IOException {
        display.asyncExec(new Runnable() {
            public void run() {
                if (log_text.isDisposed()) return;
                log_text.append("Wait for shell prompt\n");
            }
        });
        final String s = shell.waitPrompt();
        if (s.length() > 0) {
            display.asyncExec(new Runnable() {
                public void run() {
                    if (log_text.isDisposed()) return;
                    log_text.append("Got: ");
                    log_text.append(s);
                    if (!s.endsWith("\n")) log_text.append("\n");
                }
            });
        }
        return s;
    }
    
    private void done(final Throwable error) {
        display.asyncExec(new Runnable() {
            public void run() {
                thread = null;
                protocol = null;
                host = null;
                user = null;
                user_password = null;
                root_password = null;
                if (log_text.isDisposed()) return;
                if (error != null) {
                    StringWriter buf = new StringWriter();
                    PrintWriter pwr = new PrintWriter(buf);
                    error.printStackTrace(pwr);
                    pwr.flush();
                    log_text.append(buf.toString());
                    setErrorMessage(error.getClass().getName() + ": " + error.getLocalizedMessage());
                }
                else {
                    log_text.append("Done\n");
                    setErrorMessage(null);
                }
                getContainer().updateButtons();
            }
        });
    }
    
    @Override
    public IWizardPage getPreviousPage() {
        if (thread != null) return null;
        return wizard.getPage("LoginPage");
    }
    
    @Override
    public IWizardPage getNextPage() {
        return null;
    }

    public boolean canFinish() {
        return thread == null && getErrorMessage() == null;
    }
}