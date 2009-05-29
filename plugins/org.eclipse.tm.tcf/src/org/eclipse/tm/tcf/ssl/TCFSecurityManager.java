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
package org.eclipse.tm.tcf.ssl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
        File certs = Activator.getDefault().getStateLocation().append("certificates").toFile(); //$NON-NLS-1$
        if (!certs.exists()) certs.mkdirs();
        return certs;
    }

    public static SSLContext createSSLContext() {
        try {
            final File certs = getCertificatesDirectory();
            if (!certs.exists()) certs.mkdirs();
            final CertificateFactory cf = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
            SSLContext context = SSLContext.getInstance("TLS"); //$NON-NLS-1$
            
            X509ExtendedKeyManager km = new X509ExtendedKeyManager() {

                public X509Certificate[] getCertificateChain(String alias) {
                    File f = new File(certs, "Local.cert"); //$NON-NLS-1$
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
                    File f = new File(certs, "Local.priv"); //$NON-NLS-1$
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
                    for (String fnm : certs.list()) {
                        if (!fnm.endsWith(".cert")) continue; //$NON-NLS-1$
                        try {
                            InputStream inp = new BufferedInputStream(new FileInputStream(new File(certs, fnm)));
                            X509Certificate cert = (X509Certificate)cf.generateCertificate(inp);
                            inp.close();
                            list.add(cert);
                        }
                        catch (Throwable x) {
                            Protocol.log("Cannot load certificate: " + fnm, x); //$NON-NLS-1$
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
