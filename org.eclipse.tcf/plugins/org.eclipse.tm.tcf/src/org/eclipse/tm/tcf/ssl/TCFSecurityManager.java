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
package org.eclipse.tm.tcf.ssl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.tm.tcf.Activator;
import org.eclipse.tm.tcf.core.Base64;
import org.eclipse.tm.tcf.protocol.Protocol;


/**
 * This class implements keys and certificates management for secure TCF channels.
 */
public class TCFSecurityManager {

    public static File getCertificatesDirectory() {
        File certs;
        try {
            certs = Activator.getDefault().getStateLocation().append("certificates").toFile(); //$NON-NLS-1$
        }
        catch (IllegalStateException e) {
            // An RCP workspace-less environment (-data @none)
            certs = new File(System.getProperty("user.home"), ".tcf");
            certs = new File(certs, "certificates");
        }
        if (!certs.exists()) certs.mkdirs();
        return certs;
    }

    public static File getSysCertificatesDirectory() {
        File file = null;
        String osname = System.getProperty("os.name", "");
        if (osname.startsWith("Windows")) {
            try {
                String sys_root = "SystemRoot";
                Process prs = Runtime.getRuntime().exec(new String[]{ "cmd", "/c", "set", sys_root }, null);
                BufferedReader inp = new BufferedReader(new InputStreamReader(prs.getInputStream()));
                for (;;) {
                    String s = inp.readLine();
                    if (s == null) break;
                    int i = s.indexOf('=');
                    if (i > 0) {
                        String name = s.substring(0, i);
                        if (name.equalsIgnoreCase(sys_root)) {
                            File root = new File(s.substring(i + 1));
                            if (root.exists()) file = new File(root, "TCF/ssl");
                        }
                    }
                }
                try {
                    prs.getErrorStream().close();
                    prs.getOutputStream().close();
                    inp.close();
                }
                catch (IOException x) {
                }
                prs.waitFor();
            }
            catch (Throwable x) {
            }
        }
        else {
            file = new File("/etc/tcf/ssl");
        }
        if (file == null) return null;
        if (!file.exists()) return null;
        if (!file.isDirectory()) return null;
        return file;
    }

    public static SSLContext createSSLContext() {
        try {
            final File usr_certs = getCertificatesDirectory();
            final File sys_certs = getSysCertificatesDirectory();
            if (!usr_certs.exists()) usr_certs.mkdirs();
            final CertificateFactory cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
            SSLContext context = SSLContext.getInstance("TLS"); //$NON-NLS-1$

            X509ExtendedKeyManager km = new X509ExtendedKeyManager() {

                public X509Certificate[] getCertificateChain(String alias) {
                    File f = new File(usr_certs, "local.cert"); //$NON-NLS-1$
                    if (!f.exists() && sys_certs != null) f = new File(sys_certs, "local.cert"); //$NON-NLS-1$
                    try {
                        InputStream inp = new BufferedInputStream(new FileInputStream(f));
                        X509Certificate cert = (X509Certificate)cf.generateCertificate(inp);
                        inp.close();
                        return new X509Certificate[] { cert };
                    }
                    catch (Exception x) {
                        Protocol.log("Cannot read certificate: " + f, x); //$NON-NLS-1$
                        return null;
                    }
                }

                public PrivateKey getPrivateKey(String alias) {
                    File f = new File(usr_certs, "local.priv"); //$NON-NLS-1$
                    if (!f.exists() && sys_certs != null) f = new File(sys_certs, "local.priv"); //$NON-NLS-1$
                    try {
                        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ASCII")); //$NON-NLS-1$
                        StringBuffer bf = new StringBuffer();
                        boolean app = false;
                        for (;;) {
                            String s = r.readLine();
                            if (s == null) new Exception("Invalid format"); //$NON-NLS-1$
                            else if (s.indexOf("-----BEGIN ") == 0) app = true; //$NON-NLS-1$
                            else if (s.indexOf("-----END ") == 0) break; //$NON-NLS-1$
                            else if (app) bf.append(s);
                        }
                        r.close();
                        KeyFactory kf = KeyFactory.getInstance("RSA"); //$NON-NLS-1$
                        byte[] bytes = Base64.toByteArray(bf.toString().toCharArray());
                        return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
                    }
                    catch (Exception x) {
                        Protocol.log("Cannot read private key: " + f, x); //$NON-NLS-1$
                        return null;
                    }
                }

                public String[] getClientAliases(String keyType, Principal[] issuers) {
                    return new String[] { "TCF" }; //$NON-NLS-1$
                }

                public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
                    return "TCF"; //$NON-NLS-1$
                }

                public String[] getServerAliases(String keyType, Principal[] issuers) {
                    return new String[] { "TCF" }; //$NON-NLS-1$
                }

                public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                    return "TCF"; //$NON-NLS-1$
                }
            };

            X509TrustManager tm = new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] chain, String auth_type) throws CertificateException {
                    if ("RSA".equals(auth_type) && chain != null && chain.length == 1) { //$NON-NLS-1$
                        for (X509Certificate cert : getAcceptedIssuers()) {
                            if (cert.equals(chain[0])) return;
                        }
                    }
                    throw new CertificateException("Client certificate validation failed"); //$NON-NLS-1$
                }

                public void checkServerTrusted(X509Certificate[] chain, String auth_type) throws CertificateException {
                    if ("RSA".equals(auth_type) && chain != null && chain.length == 1) { //$NON-NLS-1$
                        for (X509Certificate cert : getAcceptedIssuers()) {
                            if (cert.equals(chain[0])) return;
                        }
                    }
                    throw new CertificateException("Server certificate validation failed"); //$NON-NLS-1$
                }

                public X509Certificate[] getAcceptedIssuers() {
                    ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
                    for (String fnm : usr_certs.list()) {
                        if (!fnm.endsWith(".cert")) continue; //$NON-NLS-1$
                        try {
                            InputStream inp = new BufferedInputStream(new FileInputStream(new File(usr_certs, fnm)));
                            X509Certificate cert = (X509Certificate)cf.generateCertificate(inp);
                            inp.close();
                            list.add(cert);
                        }
                        catch (Throwable x) {
                            Protocol.log("Cannot load certificate: " + fnm, x); //$NON-NLS-1$
                        }
                    }
                    if (sys_certs != null) {
                        String[] arr = sys_certs.list();
                        if (arr != null) {
                            for (String fnm : arr) {
                                if (!fnm.endsWith(".cert")) continue; //$NON-NLS-1$
                                try {
                                    InputStream inp = new BufferedInputStream(new FileInputStream(new File(sys_certs, fnm)));
                                    X509Certificate cert = (X509Certificate)cf.generateCertificate(inp);
                                    inp.close();
                                    list.add(cert);
                                }
                                catch (Throwable x) {
                                    Protocol.log("Cannot load certificate: " + fnm, x); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                    return list.toArray(new X509Certificate[list.size()]);
                }
            };

            context.init(new KeyManager[] { km }, new TrustManager[] { tm }, null);
            return context;
        }
        catch (Throwable x) {
            Protocol.log("Cannot initialize SSL context", x); //$NON-NLS-1$
            return null;
        }
    }
}
