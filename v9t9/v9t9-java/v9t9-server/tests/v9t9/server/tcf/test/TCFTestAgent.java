/*
  TCFTestAgent.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.tcf.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;

import v9t9.server.tcf.EmulatorTCFQueue;

public class TCFTestAgent {
	public static final String ERROR_OPTION = "/error";
	private static String NAME;
	private Map<String, Object> settings;
	protected List<AbstractPeer> peers = new ArrayList<AbstractPeer>();
	protected AbstractPeer peer;
	protected IChannel channel;
	protected Map<IService, IChannel.IEventListener> serviceListeners = new HashMap<IService, IChannel.IEventListener>();
	private static IServiceProvider sServiceProvider;

	static {
		NAME = "TCF Test Agent";

		sServiceProvider = new IServiceProvider() {
			public IService[] getLocalService(IChannel channel) {
				return null;
			}

			public IService getServiceProxy(IChannel channel, String serviceName) {
				return null;
			}
		};
		Protocol.addServiceProvider(sServiceProvider);
	}

	private AbstractPeer getServerPeer() {
		String type = (String) this.settings.get("TransportName");
		String host = (String) this.settings.get("Host");
		String port = (String) this.settings.get("Port");

		String id = type
				+ (port != null ? ":" + (host != null ? host + ":" : "") + port
						: "");

		ILocator loc = Protocol.getLocator();
		IPeer peer = (IPeer) loc.getPeers().get(id);
		if (peer != null) {
			return (AbstractPeer) peer;
		}
		for (AbstractPeer p : this.peers) {
			if (this.settings.equals(p.getAttributes())) {
				return p;
			}

		}

		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("ID", id);
		attrs.put("Name", NAME);
		attrs.put("OSName", System.getProperty("os.name"));
		attrs.put("TransportName", type);
		if (this.settings.containsKey("Host")) {
			attrs.put("Host", host);
		}
		attrs.put("Port", port);
		attrs.put("Proxy", "");

		ServerPeer p = new ServerPeer(attrs);
		return p;
	}

	public TCFTestAgent(Map<String, Object> settings) {
		this.settings = settings;
	}

	public static void main(String[] args) {
		try {
			TCFTestAgent agent = new TCFTestAgent(new HashMap<String, Object>());
			agent.run();
		} finally {
		}
	}

	public void run() {
		EmulatorTCFQueue queue = new EmulatorTCFQueue();
		Protocol.setEventQueue(queue);
		queue.start();
		try {
			commandLoop();
		} finally {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					if (TCFTestAgent.this.channel != null)
						TCFTestAgent.this.channel.close();
					if (TCFTestAgent.this.peer != null)
						TCFTestAgent.this.peer.dispose();
				}
			});
			queue.shutdown();
		}
	}

	private void commandLoop() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		help(System.out);
		String line;
		while (true) {
			prompt();
			try {
				line = reader.readLine().trim();
			} catch (IOException e) {
				break;
			}
			if (line == null) {
				break;
			}
			if (line.trim().isEmpty()) {
				continue;
			}
			String[] tokens = line.split("\\s+");
			if (tokens[0].equals("peers")) {
				peers();
				continue;
			}
			if (tokens[0].equals("connect")) {
				connect(tokens.length > 1 ? tokens[1] : null);
				continue;
			}
			if (tokens[0].equals("services")) {
				services(System.out);
				continue;
			}
			if (tokens[0].equals("tcf")) {
				command(tokens);
				continue;
			}
			if (tokens[0].equals("test")) {
				test(tokens);
				continue;
			}
			if (tokens[0].equals("disconnect")) {
				disconnect();
				continue;
			}
			if (tokens[0].equals("exit")) {
				break;
			}
			System.err.println("Unknown command");
			help(System.err);
		}
	}

	private void prompt() {
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				System.out.print((peer != null ? TCFTestAgent.this.peer.getName() : "<none>")
						+ (channel != null && TCFTestAgent.this.channel.getState() == 0 ? " <opening>"
								: channel != null && TCFTestAgent.this.channel.getState() == 1 ? ""
										: " <closed>"));
				System.out.print("> ");
				System.out.flush();
			}
		});
	}

	private void services(final PrintStream stream) {
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				if (!TCFTestAgent.this.validateChannel())
					return;
				Collection<String> services = TCFTestAgent.this.channel
						.getRemoteServices();
				stream.println("Available services:");
				for (String serv : services) {
					stream.print(serv);
					stream.print(' ');
				}
				stream.println();
			}
		});
	}

	private void help(PrintStream stream) {
		stream.println("Commands:\n\tpeers -- list discovered peers and known transports\n" +
				"\tconnect <transport>:[<host>:]<port> -- connect to peer\n" +
				"\tservices -- list services on connected peer\n" +
				"\ttcf <service> <command> <args> -- send command to connected peer\n" +
				"\tdisconnect -- disconnect from peer\n\texit -- exit the client\n");
	}

	private void peers() {
		Protocol.invokeLater(new Runnable() {
			public void run() {
				ILocator loc = Protocol.getLocator();

				Map<String, IPeer> peers = loc.getPeers();

				ArrayList<IPeer> sortedPeers = new ArrayList<IPeer>(peers.values());
				Collections.sort(sortedPeers, new Comparator<IPeer>() {
					public int compare(IPeer o1, IPeer o2) {
						return o1.getID().compareTo(o2.getID());
					}
				});
				for (IPeer p : sortedPeers) {
					System.out.println(p.getID() + " peers: " + p.getName());
				}
				System.out.println();
			}
		});
	}

	private void disconnect() {
		if ((this.peer == null) || (this.channel == null)) {
			System.err.println("was not connected");
			this.peer = null;
			this.channel = null;
			return;
		}
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				TCFTestAgent.this.channel.close();
				if (TCFTestAgent.this.peers.contains(TCFTestAgent.this.peer)) {
					TCFTestAgent.this.peer.dispose();
				}
				TCFTestAgent.this.peer = null;
				TCFTestAgent.this.channel = null;
			}
		});
	}

	private void command(final String[] tokens) {
		if (!validateChannel())
			return;
		if (tokens.length < 3) {
			System.err.println("Expected: tcf Service command ...");
			return;
		}
		final String service = tokens[1];
		final String command = tokens[2];

		StringBuilder fullcmd = new StringBuilder();
		for (int i = 3; i < tokens.length; i++)
			fullcmd.append(i > 3 ? " " : "").append(tokens[i]);
		final String cmdArgs = fullcmd.toString();

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				IService remoteService = TCFTestAgent.this
						.getServiceAndListen(service);
				if (remoteService == null) {
					return;
				}
				Object value = cmdArgs;
				Object[] objValues = (Object[]) null;

				if (cmdArgs.length() == 0) {
					objValues = new Object[0];
				} else {
					int i = 0;
					try {
						value = JSON.parseOne(cmdArgs.getBytes());
						objValues = new Object[] { value };
					} catch (IOException e) {
						objValues = new Object[tokens.length - 3];
						i = 3;
						do {
							value = tokens[i];
							try {
								value = JSON.parseOne(tokens[i].getBytes());
							} catch (IOException localIOException1) {
							}
							objValues[(i - 3)] = value;
							
							i++;
						} while (i < tokens.length);
					}
				}

				byte[] seq;
				try {
					seq = JSON.toJSONSequence(objValues);
				} catch (IOException e) {
					System.err.println("Error in command: " + e.getMessage());
					return;
				}
				TCFTestAgent.this.channel.sendCommand(remoteService, command,
						seq, new TCFTestAgent.TestCommandListener());
			}
		});
	}

	private void test(String[] tokens) {
		if (!validateChannel())
			return;
		if (tokens.length < 2) {
			System.err.println("Expected: test testName arguments...");
			return;
		}
		String testName = tokens[1];

		System.err.println("Unknown test: " + testName);
	}

	private String jsonSequenceToString(byte[] data) {
		return new String(data).replace((char) 0, ' ');
		/*
		try {
			Object[] objs = JSON.parseSequence(data);
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Object obj : objs) {
				if (first)
					first = false;
				else
					sb.append(' ');
				if (obj instanceof byte[])
					sb.append(new String(JSON.toJSONSequence(new Object[] { 
							new Binary((byte[])obj, 0, ((byte[])obj).length) }, true)));
				else
					//sb.append(JSON.toJSON(obj));
					sb.append(new String(JSON.toJSONSequence(new Object[] { obj }, true)));
			}
			return sb.toString();
		} catch (IOException e) {
			return "<<exception: " + e.getMessage() + ">>";
		}
		*/
	}

	private boolean validateChannel() {
		if (this.channel == null) {
			System.err.println("Channel not open (use 'connect')");
			return false;
		}

		if (this.channel.getState() == 0) {
			System.err.println("Still waiting for hello message...");
			return false;
		}
		return true;
	}

	private void connect(String string) {
		if (this.peer != null) {
			System.err.println("Already connected");
			return;
		}

		if (string == null || string.length() == 0 || string.equals("ti")) {
			string = "TCP:127.0.0.1:9900";
		}
		String host = null;
		String port = null;
		String transport = null;

		int idx = string.indexOf(':');
		if (idx < 0) {
			transport = string;
		} else {
			transport = string.substring(0, idx);
			String rest = string.substring(idx + 1);
			idx = rest.indexOf(':');
			if (idx < 0) {
				port = rest;
			} else {
				host = rest.substring(0, idx);
				port = rest.substring(idx + 1);
			}
		}
		this.settings.put("TransportName", transport);
		if (host != null)
			this.settings.put("Host", host);
		if (port != null)
			this.settings.put("Port", port);

		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				try {
					TCFTestAgent.this.peer = TCFTestAgent.this.getServerPeer();
					TCFTestAgent.this.channel = TCFTestAgent.this.peer
							.openChannel();

					TCFTestAgent.this.channel
							.addChannelListener(new IChannel.IChannelListener() {
								public void onChannelOpened() {
									System.out.println("Channel opened");
									TCFTestAgent.this.prompt();
								}

								public void onChannelClosed(Throwable error) {
									if (error != null)
										System.err.println(error.getMessage());
									TCFTestAgent.this.peer = null;
									TCFTestAgent.this.channel = null;
								}

								public void congestionLevel(int level) {
								}
							});
				} catch (Throwable x) {
					System.err
							.println("TCF Server: failed to create a channel");
					x.printStackTrace();
					if (TCFTestAgent.this.peer != null)
						TCFTestAgent.this.peer.dispose();
					TCFTestAgent.this.peer = null;
					TCFTestAgent.this.channel = null;
					TCFTestAgent.this.prompt();
				}
			}
		});
	}

	private IService getServiceAndListen(String service) {
		if (this.channel == null)
			return null;
		final IService remoteService = this.channel.getRemoteService(service);
		if (remoteService == null) {
			System.err.println("No such service '" + service + "' available");
			services(System.err);
			return null;
		}

		IChannel.IEventListener listener = (IChannel.IEventListener) this.serviceListeners
				.get(remoteService);
		if (listener == null) {
			listener = new IChannel.IEventListener() {
				public void event(String name, byte[] data) {
					System.out.println("Event: " + remoteService.getName()
							+ ":" + name + ": "
							+ TCFTestAgent.this.jsonSequenceToString(data));
					TCFTestAgent.this.prompt();
				}
			};
			this.channel.addEventListener(remoteService, listener);
			this.serviceListeners.put(remoteService, listener);
		}
		return remoteService;
	}

	private class TestCommandListener implements IChannel.ICommandListener {
		private TestCommandListener() {
		}

		public void terminated(IToken token, Exception error) {
			System.out.println("\nCommand terminated: " + token + ": "
					+ error.getMessage());
			if ((error instanceof IErrorReport)) {
				IErrorReport report = (IErrorReport) error;
				if (report.getAltOrg() != null)
					System.out.println(report.getAltOrg() + ": "
							+ report.getAltCode() + " ("
							+ Integer.toHexString(report.getAltCode()) + ")");
			}
			TCFTestAgent.this.prompt();
		}

		public void result(IToken token, byte[] data) {
			String string = TCFTestAgent.this.jsonSequenceToString(data);
			System.out.println("\nCommand result: " + token + "\n" + string);
			TCFTestAgent.this.prompt();
		}

		public void progress(IToken token, byte[] data) {
			try {
				System.out.println("Progress: " + token + "\n"
						+ JSON.parseSequence(data));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}