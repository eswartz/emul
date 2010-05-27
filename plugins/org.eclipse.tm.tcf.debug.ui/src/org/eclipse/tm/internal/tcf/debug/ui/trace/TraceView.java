/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.trace;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.ui.part.ViewPart;


public class TraceView extends ViewPart implements Protocol.ChannelOpenListener {

    private Composite parent;
    private TabFolder tabs;
    private Label no_data;
    private final Map<TabItem,Page> tab2page = new HashMap<TabItem,Page>();

    private class Page implements AbstractChannel.TraceListener {

        final AbstractChannel channel;

        private TabItem tab;
        private Text text;

        private final StringBuffer bf = new StringBuffer();
        private int bf_line_cnt = 0;
        private boolean closed;

        private final Thread update_thread = new Thread() {
            public void run() {
                synchronized (Page.this) {
                    while (!closed) {
                        if (bf_line_cnt > 0) {
                            Runnable r = new Runnable() {
                                public void run() {
                                    String str = null;
                                    int cnt = 0;
                                    synchronized (Page.this) {
                                        str = bf.toString();
                                        cnt = bf_line_cnt;
                                        bf.setLength(0);
                                        bf_line_cnt = 0;
                                    }
                                    if (text == null) return;
                                    if (text.getLineCount() > 1000 - cnt) {
                                        String s = text.getText();
                                        int n = 0;
                                        int i = -1;
                                        while (n < cnt) {
                                            int j = s.indexOf('\n', i + 1);
                                            if (j < 0) break;
                                            i = j;
                                            n++;
                                        }
                                        if (i >= 0) {
                                            text.setText(s.substring(i + 1));
                                        }
                                    }
                                    text.append(str);
                                }
                            };
                            getSite().getShell().getDisplay().asyncExec(r);
                        }
                        try {
                            Page.this.wait(1000);
                        }
                        catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        };

        Page(AbstractChannel channel) {
            this.channel = channel;
            update_thread.setName("TCF Trace View");
            update_thread.start();
        }

        public void dispose() {
            if (closed) return;
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    channel.removeTraceListener(Page.this);
                }
            });
            synchronized (this) {
                closed = true;
                update_thread.interrupt();
            }
            try {
                update_thread.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            tab2page.remove(tab);
            tab.dispose();
            tab = null;
            text = null;
            if (tab2page.isEmpty()) hideTabs();
        }

        public synchronized void onChannelClosed(Throwable error) {
            if (error == null) {
                getSite().getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        dispose();
                    }
                });
            }
            else {
                bf.append("Channel terminated: " + error);
                bf_line_cnt++;
            }
        }

        public synchronized void onMessageReceived(char type, String token,
                String service, String name, byte[] data) {
            try {
                if ("Locator".equals(service) && "peerHeartBeat".equals(name)) return;
                bf.append("Inp: ");
                bf.append(type);
                if (token != null) {
                    bf.append(' ');
                    bf.append(token);
                }
                if (service != null) {
                    bf.append(' ');
                    bf.append(service);
                }
                if (name != null) {
                    bf.append(' ');
                    bf.append(name);
                }
                if (data != null) {
                    appendData(bf, data);
                }
                bf.append('\n');
                bf_line_cnt++;
            }
            catch (UnsupportedEncodingException x) {
                x.printStackTrace();
            }
        }

        public synchronized void onMessageSent(char type, String token,
                String service, String name, byte[] data) {
            try {
                if ("Locator".equals(service) && "peerHeartBeat".equals(name)) return;
                bf.append("Out: ");
                bf.append(type);
                if (token != null) {
                    bf.append(' ');
                    bf.append(token);
                }
                if (service != null) {
                    bf.append(' ');
                    bf.append(service);
                }
                if (name != null) {
                    bf.append(' ');
                    bf.append(name);
                }
                if (data != null) {
                    appendData(bf, data);
                }
                bf.append('\n');
                bf_line_cnt++;
            }
            catch (UnsupportedEncodingException x) {
                x.printStackTrace();
            }
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                IChannel[] arr = Protocol.getOpenChannels();
                for (IChannel c : arr) onChannelOpen(c);
                Protocol.addChannelOpenListener(TraceView.this);
            }
        });
        if (tab2page.size() == 0) hideTabs();
    }

    @Override
    public void setFocus() {
        if (tabs != null) tabs.setFocus();
    }

    @Override
    public void dispose() {
        final Page[] pages = tab2page.values().toArray(new Page[tab2page.size()]);
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                Protocol.removeChannelOpenListener(TraceView.this);
            }
        });
        for (Page p : pages) p.dispose();
        assert tab2page.isEmpty();
        if (tabs != null) {
            tabs.dispose();
            tabs = null;
        }
        if (no_data != null) {
            no_data.dispose();
            no_data = null;
        }
        super.dispose();
    }

    public void onChannelOpen(final IChannel channel) {
        if (!(channel instanceof AbstractChannel)) return;
        AbstractChannel c = (AbstractChannel)channel;
        IPeer rp = c.getRemotePeer();
        final String name = rp.getName();
        final String host = rp.getAttributes().get(IPeer.ATTR_IP_HOST);
        final String port = rp.getAttributes().get(IPeer.ATTR_IP_PORT);
        final Page p = new Page(c);
        c.addTraceListener(p);
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                showTabs();
                p.tab = new TabItem(tabs, SWT.NONE);
                tab2page.put(p.tab, p);
                String title = name;
                if (host != null) {
                    title += ", " + host;
                    if (port != null) {
                        title += ":" + port;
                    }
                }
                p.tab.setText(title);
                p.text = new Text(tabs, SWT.H_SCROLL | SWT.V_SCROLL |
                        SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
                p.tab.setControl(p.text);
                p.text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            }
        });
    }

    private void appendData(StringBuffer bf, byte[] data) throws UnsupportedEncodingException {
        int pos = bf.length();
        try {
            Object[] o = JSON.parseSequence(data);
            for (int i = 0; i < o.length; i++) {
                bf.append(' ');
                appendJSON(bf, o[i]);
            }
        }
        catch (Throwable z) {
            bf.setLength(pos);
            for (int i = 0; i < data.length; i++) {
                bf.append(' ');
                int x = (data[i] >> 4) & 0xf;
                int y = data[i] & 0xf;
                bf.append((char)(x < 10 ? '0' + x : 'a' + x - 10));
                bf.append((char)(y < 10 ? '0' + y : 'a' + y - 10));
            }
        }
    }

    private void appendJSON(StringBuffer bf, Object o) {
        if (o instanceof byte[]) {
            int l = ((byte[])o).length;
            bf.append('(');
            bf.append(l);
            bf.append(')');
        }
        else if (o instanceof Collection) {
            int cnt = 0;
            bf.append('[');
            for (Object i : (Collection<?>)o) {
                if (cnt > 0) bf.append(',');
                appendJSON(bf, i);
                cnt++;
            }
            bf.append(']');
        }
        else if (o instanceof Map) {
            int cnt = 0;
            bf.append('{');
            for (Object k : ((Map<?,?>)o).keySet()) {
                if (cnt > 0) bf.append(',');
                bf.append(k.toString());
                bf.append(':');
                appendJSON(bf, ((Map<?,?>)o).get(k));
                cnt++;
            }
            bf.append('}');
        }
        else if (o instanceof String) {
            bf.append('"');
            String s = (String)o;
            int l = s.length();
            for (int i = 0; i < l; i++) {
                char ch = s.charAt(i);
                if (ch < ' ') {
                    bf.append('\\');
                    bf.append('u');
                    for (int j = 0; j < 4; j++) {
                        int x = (ch >> (4 * (3 - j))) & 0xf;
                        bf.append((char)(x < 10 ? '0' + x : 'a' + x - 10));
                    }
                }
                else {
                    bf.append(ch);
                }
            }
            bf.append('"');
        }
        else {
            bf.append(o);
        }
    }

    private void showTabs() {
        boolean b = false;
        if (no_data != null) {
            no_data.dispose();
            no_data = null;
            b = true;
        }
        if (tabs == null) {
            tabs = new TabFolder(parent, SWT.NONE);
            Menu menu = new Menu(tabs);
            MenuItem mi_close = new MenuItem(menu, SWT.NONE);
            mi_close.setText("Close");
            mi_close.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                }
                public void widgetSelected(SelectionEvent e) {
                    if (tabs == null) return;
                    TabItem[] s = tabs.getSelection();
                    for (TabItem i : s) {
                        Page p = tab2page.get(i);
                        if (p != null) p.dispose();
                        else i.dispose();
                    }
                }
            });
            MenuItem mi_close_all = new MenuItem(menu, SWT.NONE);
            mi_close_all.setText("Close All");
            mi_close_all.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                }
                public void widgetSelected(SelectionEvent e) {
                    if (tabs == null) return;
                    TabItem[] s = tabs.getItems();
                    for (TabItem i : s) {
                        Page p = tab2page.get(i);
                        if (p != null) p.dispose();
                        else i.dispose();
                    }
                }
            });
            tabs.setMenu(menu);
            b = true;
        }
        if (b) parent.layout();
    }

    private void hideTabs() {
        boolean b = false;
        if (tabs != null) {
            tabs.dispose();
            tabs = null;
            b = true;
        }
        if (!parent.isDisposed()) {
            if (no_data == null) {
                no_data = new Label(parent, SWT.NONE);
                no_data.setText("No open communication channels at this time.");
                b = true;
            }
            if (b) parent.layout();
        }
    }
}
