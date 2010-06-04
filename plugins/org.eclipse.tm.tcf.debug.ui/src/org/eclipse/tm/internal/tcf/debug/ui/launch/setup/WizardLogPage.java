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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import org.eclipse.tm.tcf.ssl.TCFSecurityManager;
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
            exec("cd /tmp");
            send("uname -o", true);
            String os = waitPrompt().replace('\n', ' ').trim();
            send("uname -m", true);
            String machine = waitPrompt().replace('\n', ' ').trim();
            String version = "0.3.0";
            Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

            URL url = FileLocator.find(bundle, new Path("agent/get-os-tag"), null);
            if (url == null) throw new Exception("Cannot find get-os-tag script");
            send("cat >get-os-tag", true);
            InputStream inp = url.openStream();
            byte[] buf = new byte[0x100];
            for (;;) {
                int len = inp.read(buf);
                if (len < 0) break;
                shell.write(new String(buf, 0, len, "ASCII"));
                for (int i = 0; i < len; i++) {
                    if (buf[i] == '\n') shell.expect("\n");
                }
            }
            inp.close();
            shell.write("\004");
            s = waitPrompt();
            if (s.length() > 0) throw new Exception(s);

            exec("chmod u+x get-os-tag");

            send("./get-os-tag", true);
            String os_tag = waitPrompt().replace('\n', ' ').trim();

            exec("rm -f get-os-tag");

            url = null;
            String fnm = null;
            String machine0 = machine;
            for (;;) {
                for (int release = 16; url == null && release > 0; release--) {
                    fnm = "tcf-agent-" + version + "-" + release + "." + os_tag + "." + machine + ".rpm";
                    url = FileLocator.find(bundle, new Path("agent/" + os + "/" + machine + "/" + fnm), null);
                }
                if (url != null) break;
                if (machine.equals("i686")) machine = "i586";
                else if (machine.equals("i586")) machine = "i486";
                else if (machine.equals("i486")) machine = "i386";
                else {
                    machine = machine0;
                    if (os_tag.startsWith("fc")) {
                        int n = Integer.parseInt(os_tag.substring(2)) - 1;
                        if (n <= 0) break;
                        os_tag = "fc" + n;
                    }
                    else if (os_tag.startsWith("rh")) {
                        int n = Integer.parseInt(os_tag.substring(2)) - 1;
                        if (n <= 0) break;
                        os_tag = "rh" + n;
                    }
                    else break;
                }
            }
            if (url == null) throw new Exception("Unsupported target OS or CPU");

            inp = url.openStream();
            send("which base64", true);
            s = waitPrompt();
            if (s.indexOf(':') < 0) {
                send("base64 -di >" + fnm, true);
                sendBase64(inp);
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
                    sendBase64(inp);
                    send("====", true);
                    s = waitPrompt();
                    if (s.length() > 0) throw new Exception(s);
                }
                else {
                    throw new Exception("No base64 or uudecode commands available");
                }
            }
            inp.close();

            send("rpm -e --quiet tcf-agent", true);
            waitPrompt();
            exec("rm -f /etc/init.d/tcf-agent*");
            exec("rpm -i --quiet " + fnm);
            exec("rm -f " + fnm);

            File certs = TCFSecurityManager.getCertificatesDirectory();

            File local_cert = new File(certs, "local.cert");
            File local_priv = new File(certs, "local.priv");

            if (!local_cert.exists() || !local_priv.exists()) {
                copyRemoteSecret("local.cert", local_cert);
                copyRemoteSecret("local.priv", local_priv);
                exec("/usr/sbin/tcf-agent -c");
            }

            copyRemoteSecret("local.cert", new File(certs, host + ".cert"));
            copyLocalSecret(local_cert, InetAddress.getLocalHost().getHostName() + ".cert");

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

    private void sendBase64(InputStream inp) throws Exception {
        byte[] buf = new byte[0x100 * 3];
        for (;;) {
            int len = 0;
            while (len < buf.length) {
                int rd = inp.read(buf, len, buf.length - len);
                if (rd < 0) break;
                len += rd;
            }
            if (len == 0) break;
            send(new String(Base64.toBase64(buf, 0, len)), false);
        }
    }

    private void copyRemoteSecret(String from, File to) throws Exception {
        send("cat /etc/tcf/ssl/" + from, true);
        String s = waitPrompt();
        if (s.indexOf("-----BEGIN ") != 0) throw new Exception(s);
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(to), "ASCII"));
        wr.write(s);
        wr.close();
    }

    private void copyLocalSecret(File from, String to) throws Exception {
        send("cat >/etc/tcf/ssl/" + to, true);
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                new FileInputStream(from), "ASCII"));
        for (;;) {
            String s = rd.readLine();
            if (s == null) break;
            send(s, true);
        }
        shell.write("\004");
        String s = waitPrompt();
        if (s.length() > 0) throw new Exception(s);
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

    private void exec(String cmd) throws Exception {
        send(cmd, true);
        String s = waitPrompt();
        if (s.length() > 0) throw new Exception(s);
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