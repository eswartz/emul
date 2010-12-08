 /*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc., Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 
 * Contributors:
 *    Liping Ke        (Intel Corp.) - initial API and implementation
 *    Sheldon D'souza  (Celunite)    - LoginThread and readUntil implementation
 ******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.shells;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.terminals.AbstractTerminalShell;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.tm.internal.tcf.rse.ITCFSessionProvider;
import org.eclipse.tm.internal.tcf.rse.Messages;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorService;
import org.eclipse.tm.internal.tcf.rse.TCFRSETask;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.services.ITerminals;


public class TCFTerminalShell extends AbstractTerminalShell {
    private ITCFSessionProvider fSessionProvider;
    private IChannel fChannel;
    private String fPtyType;
    private ITerminals.TerminalContext terminalContext;
    private String fEncoding;
    private InputStream fInputStream;
    private OutputStream fOutputStream;
    private Writer fOutputStreamWriter;
    private int fWidth = 0;
    private int fHeight = 0;
    private String fContextID;
    private String in_id;
    private String out_id;
    private boolean connected = false;
    private ITerminals terminal;
    private int status;

    private IPropertySet tcfPropertySet = null;

    private static String defaultEncoding = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[0])).getEncoding();
    private ITerminals.TerminalsListener listeners = new ITerminals.TerminalsListener(){

        public void exited(String terminalId, int exitCode) {

            if(!terminalContext.getID().equals(terminalId))
                return;
            terminal.removeListener(listeners);
            TCFTerminalShell.this.connected = false;
        }


        public void winSizeChanged(String terminalId, int newWidth,
                int newHeight) {

        }
    };

    /* LoginThread and readUntil functionality are cloned from TelnetConnectorService
     * and then modified for our own needs
     * */
    private class LoginThread extends Thread {

        private String username;
        private String password;
        private int status = ITCFSessionProvider.SUCCESS_CODE;
        public LoginThread(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void run() {
            tcfPropertySet = ((TCFConnectorService)fSessionProvider).getTCFPropertySet();
            String login_required = tcfPropertySet.getPropertyValue(TCFConnectorService.PROPERTY_LOGIN_REQUIRED);
            String login_prompt = tcfPropertySet.getPropertyValue(TCFConnectorService.PROPERTY_LOGIN_PROMPT);
            String password_prompt =tcfPropertySet.getPropertyValue(TCFConnectorService.PROPERTY_PASSWORD_PROMPT);
            String command_prompt = tcfPropertySet.getPropertyValue(TCFConnectorService.PROPERTY_COMMAND_PROMPT);
            String pwd_required = tcfPropertySet.getPropertyValue(TCFConnectorService.PROPERTY_PWD_REQUIRED);

            /* Before Login service is implemented, we only support non-login mode. After Login service
             * is available, below assignment should be removed */
            login_required = String.valueOf(false);
            
            if (Boolean.valueOf(login_required).booleanValue()) {
                status = ITCFSessionProvider.SUCCESS_CODE;
                if (login_prompt != null && login_prompt.length() > 0) {
                    status = readUntil(login_prompt,fInputStream);
                    write(username + "\n");
                }
                if (Boolean.valueOf(pwd_required).booleanValue()) {
                    if (status == ITCFSessionProvider.SUCCESS_CODE && password_prompt != null && password_prompt.length() > 0) {
                        status = readUntil(password_prompt,fInputStream);
                        write(password + "\n");
                    }
                }                    
                if (status == ITCFSessionProvider.SUCCESS_CODE && command_prompt != null && command_prompt.length() > 0) {
                    status = readUntil(command_prompt,fInputStream);
                    write("\n");
                }
            } else {
                if (command_prompt != null && command_prompt.length() > 0) {
                    status = readUntil(command_prompt,fInputStream);
                    write("\n");
                }
            }
        }

        public int readUntil(String pattern,InputStream in) {
            try {
                char lastChar = pattern.charAt(pattern.length() - 1);
                StringBuffer sb = new StringBuffer();
                int ch = in.read();
                while (ch >= 0) {
                    char tch = (char) ch;
                    sb.append(tch);
                    if (tch=='t' && sb.indexOf("incorrect") >= 0) { //$NON-NLS-1$
                        return ITCFSessionProvider.ERROR_CODE;
                    }
                    if (tch=='d' && sb.indexOf("closed") >= 0) { //$NON-NLS-1$
                        return ITCFSessionProvider.CONNECT_CLOSED;
                    }
                    if (tch == lastChar) {
                        if (sb.toString().endsWith(pattern)) {
                            return ITCFSessionProvider.SUCCESS_CODE;
                        }
                    }
                    ch = in.read();
                } 
            }
            catch (Exception e) {
                e.printStackTrace();
                SystemBasePlugin.logError(e.getMessage() == null ?      e.getClass().getName() : e.getMessage(), e);
            }
            return ITCFSessionProvider.CONNECT_CLOSED;
        }        

        public int getLoginStatus() {
            return this.status;
        }

    }

    public void write(String value) {
        try {
            fOutputStream.write(value.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    

    private int login(String username, String password) throws InterruptedException
    {
        long millisToEnd = System.currentTimeMillis() + ITCFSessionProvider.TCP_CONNECT_TIMEOUT*1000;
        LoginThread checkLogin = new LoginThread(username, password);
        status = ITCFSessionProvider.ERROR_CODE;
        checkLogin.start();
        while (checkLogin.isAlive() && System.currentTimeMillis()<millisToEnd)
            checkLogin.join(500);
        status = checkLogin.getLoginStatus();
        checkLogin.join();
        return status;
    }
    /**
     * Construct a new TCF connection.
     *
     * The TCF channel is immediately connected in the Constructor.
     *
     * @param sessionProvider TCF session provider
     * @param ptyType Terminal type to set, or <code>null</code> if not
     *            relevant
     * @param encoding The default encoding to use for initial command.
     * @param environment Environment array to set, or <code>null</code> if
     *            not relevant.
     * @param initialWorkingDirectory initial directory to open the Terminal in.
     *            Use <code>null</code> or empty String ("") to start in a
     *            default directory. Empty String will typically start in the
     *            home directory.
     * @param commandToRun initial command to send.
     * @throws SystemMessageException in case anything goes wrong. Channels and
     *             Streams are all cleaned up again in this case.
     * @see ITerminalService
     */
    public TCFTerminalShell(final ITCFSessionProvider sessionProvider, final String ptyType,
            final String encoding, final String[] environment,
            String initialWorkingDirectory, String commandToRun)
    throws SystemMessageException {
        Map<String, Object> map_ids;
        Exception nestedException = null;
        try {
            fSessionProvider = sessionProvider;
            fEncoding = encoding;
            fPtyType = ptyType;
            fChannel = fSessionProvider.getChannel();

            if (fChannel == null || fChannel.getState() != IChannel.STATE_OPEN)
                throw new Exception("TCP channel is not connected!");//$NON-NLS-1$
            if (((TCFConnectorService)sessionProvider).isSubscribed() == false)
                ((TCFConnectorService)sessionProvider).subscribe();
            assert (((TCFConnectorService)sessionProvider).isSubscribed());

            new TCFRSETask<ITerminals.TerminalContext>() {
                public void run() {
                    terminal = ((TCFConnectorService)sessionProvider).getService(ITerminals.class);
                    terminal.addListener(listeners);
                    terminal.launch(ptyType, encoding, environment, new ITerminals.DoneLaunch() {
                        public void doneLaunch(IToken token, Exception error,
                                ITerminals.TerminalContext terminal) {

                            terminalContext = terminal;
                            if (error != null) 
                                error(error);
                            else done(terminal);
                        }
                    });
                }

            }.getS(null, Messages.TCFShellService_Name); //$NON-NLS-1$

            fPtyType = terminalContext.getPtyType();
            fEncoding = terminalContext.getEncoding();
            fContextID = terminalContext.getID();
            fWidth = terminalContext.getWidth();
            fHeight = terminalContext.getHeight();
            map_ids = terminalContext.getProperties();
            in_id = (String)map_ids.get(ITerminals.PROP_STDOUT_ID);
            out_id = (String)map_ids.get(ITerminals.PROP_STDIN_ID);

            String user = fSessionProvider.getSessionUserId();
            String password = fSessionProvider.getSessionPassword(); //$NON-NLS-1$
            status = ITCFSessionProvider.ERROR_CODE;

            IStreams streams = ((TCFConnectorService)sessionProvider).getService(IStreams.class);
            fOutputStream = new TCFTerminalOutputStream(streams, out_id);
            fInputStream = new TCFTerminalInputStream(streams, in_id);
            if (fEncoding != null) {
                fOutputStreamWriter = new BufferedWriter(new OutputStreamWriter(fOutputStream, encoding));
            } else {
                // default encoding == System.getProperty("file.encoding")
                // TODO should try to determine remote encoding if possible
                fOutputStreamWriter = new BufferedWriter(new OutputStreamWriter(fOutputStream));
            }

            try {
                status = login(user, password);
            }
            finally
            {
                if ((status == ITCFSessionProvider.CONNECT_CLOSED))
                {
                    //Give one time chance of retrying....
                }

            }

            //give another chance of retrying
            if ((status == ITCFSessionProvider.CONNECT_CLOSED))
            {
                ((TCFConnectorService)sessionProvider).unsubscribe();
                ((TCFConnectorService)sessionProvider).subscribe();
                assert (((TCFConnectorService)sessionProvider).isSubscribed());
                status = login(user, password);
            }

            connected = true;
            if (initialWorkingDirectory!=null && initialWorkingDirectory.length()>0
                    && !initialWorkingDirectory.equals(".") //$NON-NLS-1$
                    && !initialWorkingDirectory.equals("Command Shell") //$NON-NLS-1$ //FIXME workaround for bug 153047
            ) {
                writeToShell("cd " + PathUtility.enQuoteUnix(initialWorkingDirectory)); //$NON-NLS-1$
            }

            if (commandToRun != null && commandToRun.length() > 0) {
                writeToShell(commandToRun);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            nestedException = e;
        }
        finally {
            if (status == ITCFSessionProvider.SUCCESS_CODE) {
            }
            else {
                SystemMessage msg;

                if (nestedException!=null) {
                    msg = new SimpleSystemMessage(org.eclipse.tm.internal.tcf.rse.Activator.PLUGIN_ID,
                            ICommonMessageIds.MSG_EXCEPTION_OCCURRED,
                            IStatus.ERROR,
                            CommonMessages.MSG_EXCEPTION_OCCURRED, nestedException);
                } else {
                    String strErr;
                    if (status == ITCFSessionProvider.CONNECT_CLOSED)
                        strErr = "Connection closed!";//$NON-NLS-1$
                    else if (status == ITCFSessionProvider.ERROR_CODE)
                        strErr = "Login Incorrect or meet other unknown error!";//$NON-NLS-1$
                    else
                        strErr = "Not identified Errors";//$NON-NLS-1$

                    msg = new SimpleSystemMessage(org.eclipse.tm.internal.tcf.rse.Activator.PLUGIN_ID,
                            ICommonMessageIds.MSG_COMM_AUTH_FAILED,
                            IStatus.ERROR,
                            strErr,
                    "Meet error when trying to login in!");//$NON-NLS-1$
                    msg.makeSubstitution(((TCFConnectorService)fSessionProvider).getHost().getAliasName());
                }
                throw new SystemMessageException(msg);//$NON-NLS-1$
            }                
        }

    }

    public void writeToShell(String command) throws IOException {
        if (isActive()) {
            if ("#break".equals(command)) { //$NON-NLS-1$
                command = "\u0003"; // Unicode 3 == Ctrl+C //$NON-NLS-1$
            } else {
                command += "\r\n"; //$NON-NLS-1$
            }
            fOutputStreamWriter.write(command);
        }
    }

    public void exit() {

        if (fChannel == null || (fChannel.getState() == IChannel.STATE_CLOSED) || !connected) {
            return;
        }
        try {
            getOutputStream().close();
            getInputStream().close();

        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        } //$NON-NLS-1$

        try {
            new TCFRSETask<Object>() {
                public void run() {
                    terminalContext.exit(new ITerminals.DoneCommand(){
                        public void doneCommand(IToken token,
                                Exception error) {
                            if (error != null) 
                                error(error);
                            else {
                                done(this);
                            }
                        }
                    });
                }}.getS(null, Messages.TCFShellService_Name); //seems no need block here. need further modification.
        }
        catch (SystemMessageException e) {
            e.printStackTrace();
        } //$NON-NLS-1$;
    }

    public InputStream getInputStream() {
        return fInputStream;
    }

    public OutputStream getOutputStream() {
        return fOutputStream;
    }

    public boolean isActive() {
        if (fChannel != null && !(fChannel.getState() == IChannel.STATE_CLOSED) && connected) {
            return true;
        }
        exit();
        // shell is not active: check for session lost
        return false;
    }

    public String getPtyType() {
        return fPtyType;
    }

    public void setTerminalSize(int newWidth, int newHeight) {
        // do nothing
        if (fChannel == null || (fChannel.getState() == IChannel.STATE_CLOSED) || !connected) {
            return;
        }
        fWidth = newWidth;
        fHeight = newHeight;
        try {
            new TCFRSETask<Object>() {
                public void run() {
                    if (fChannel != null && connected) {
                        terminal.setWinSize(fContextID, fWidth, fHeight, new ITerminals.DoneCommand(){

                            public void doneCommand(IToken token, Exception error) {
                                if (error != null) 
                                    error(error);
                                else
                                    done(this);

                            }});

                    }   
                }}.getS(null, Messages.TCFShellService_Name);
        }
        catch (SystemMessageException e) {
            e.printStackTrace();
        } //$NON-NLS-1$;
    }

    public String getDefaultEncoding() {
        if (fEncoding != null) return fEncoding;
        return defaultEncoding;
    }

}
