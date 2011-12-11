/**
 * 
 */
package v9t9.server.tcf.test;

import java.io.IOException;
import java.util.ArrayList;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import v9t9.server.tcf.EmulatorTCFQueue;
import v9t9.server.tcf.EmulatorTCFServiceProvider;
import static org.junit.Assert.*;

/**
 * @author ejs
 * 
 */
public class BaseTCFTest {

	protected static Map<String, Object> settings = new HashMap<String, Object>();
	protected static AbstractPeer peer;

	protected static List<AbstractPeer> peers = new ArrayList<AbstractPeer>();
	protected static IChannel channel;
	private static EmulatorTCFQueue queue;
	private static IServiceProvider serviceProvider;
	protected Map<String, IService> cachedServices = new HashMap<String, IService>();
	protected Map<IService, IChannel.IEventListener> serviceListeners = new HashMap<IService, IChannel.IEventListener>();

	protected static AbstractPeer getServerPeer() {
		String type = (String) settings.get("TransportName");
		String host = (String) settings.get("Host");
		String port = (String) settings.get("Port");

		String id = type
				+ (port != null ? ":" + (host != null ? host + ":" : "") + port
						: "");

		ILocator loc = Protocol.getLocator();
		IPeer peer = (IPeer) loc.getPeers().get(id);
		if (peer != null) {
			return (AbstractPeer) peer;
		}
		for (AbstractPeer p : peers) {
			if (settings.equals(p.getAttributes())) {
				return p;
			}

		}

		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("ID", id);
		attrs.put("Name", "TCF Test Agent");
		attrs.put("OSName", System.getProperty("os.name"));
		attrs.put("TransportName", type);
		if (settings.containsKey("Host")) {
			attrs.put("Host", host);
		}
		attrs.put("Port", port);
		attrs.put("Proxy", "");

		ServerPeer p = new ServerPeer(attrs);
		return p;
	}

	protected String stringizeJSON(byte[] data) {
		String info = new String(data);
		info = info.replace((char) 0, ' ');
		return info;
	}

	/**
	 * @param name
	 * @return
	 */
	protected IService getService(final String name) {
		IService service = cachedServices.get(name);
		if (service == null) {
			final IService[] services = { null };
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					services[0] = channel.getRemoteService(name);
				}
			});
			if (services[0] == null)
				fail("cannot locate remote service " + name);
			service = services[0];
			cachedServices.put(name, service);
		}
		return service;
	}

	protected class TestCommandListener implements IChannel.ICommandListener {
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
		}

		public void result(IToken token, byte[] data) {
			String string = stringizeJSON(data);
			System.out.println("\nCommand result: " + token + "\n" + string);
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

	@BeforeClass
	public static void connectEmulator() {
		System.out.println("connectEmulator()");
		queue = new EmulatorTCFQueue();
		Protocol.setEventQueue(queue);
		queue.start();

		String string = "TCP:127.0.0.1:9900";

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

		settings.put("TransportName", transport);
		if (host != null)
			settings.put("Host", host);
		if (port != null)
			settings.put("Port", port);

		serviceProvider = new EmulatorTCFServiceProvider(null);
		Protocol.addServiceProvider(serviceProvider);
		
		final Throwable[] errors = { null };
		final boolean[] opened = { false };
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				try {
					peer = getServerPeer();
					channel = peer.openChannel();

					channel.addChannelListener(new IChannel.IChannelListener() {
						public void onChannelOpened() {
							System.out.println("Channel opened");
							opened[0] = true;
						}

						public void onChannelClosed(Throwable error) {
							if (error != null)
								error.printStackTrace();
							errors[0] = error;
							peer = null;
							channel = null;
						}

						public void congestionLevel(int level) {
						}
					});
				} catch (Throwable x) {
					x.printStackTrace();
					fail("TCF Server: failed to create a channel: "
							+ x.toString());
					if (peer != null)
						peer.dispose();
					peer = null;
					channel = null;
					errors[0] = x;
				}
			}
		});

		long timeout = System.currentTimeMillis() + 5 * 1000;
		while (System.currentTimeMillis() < timeout) {
			if (opened[0] || errors[0] != null)
				break;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				break;
			}
		}

		if (errors[0] != null)
			fail(errors[0].toString());
	}

	@Before
	public void assertConnected() {
		if (peer == null || channel == null)
			fail("not connected");
	}

	@AfterClass
	public static void disconnectEmulator() {
		System.out.println("disconnectEmulator()");
		Protocol.invokeAndWait(new Runnable() {
			public void run() {
				if (channel != null)
					channel.close();
			}
		});
		
		Protocol.removeServiceProvider(serviceProvider);
		
		queue.shutdown();
	}

	@SuppressWarnings("unchecked")
	protected String errorString(Object err) {
		if (err == null)
			return "<null>";
		if (err instanceof Map)
			return mapToString((Map<String, Object>) err);
		if (err instanceof IErrorReport)
			return errorReportToString((IErrorReport) err);
		return err.toString();
	}

	protected String errorReportToString(IErrorReport err) {
		return mapToString(err.getAttributes());
	}

	protected String mapToString(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (sb.length() != 0)
				sb.append(",");
			sb.append(entry.getKey() + ":" + errorString(entry.getValue()));
		}
		return sb.toString();
	}

	protected Object[] expectSuccessReply(IToken token, int expArgs) {
		return expectSuccessReply(token, 0);
	}

	/**
	 * @author ejs
	 * 
	 */
	public abstract class TCFCommandWrapper {

		protected Throwable[] excs = { null };
		private volatile boolean done;

		protected synchronized void tcfDone() {
			done = true;
		}

		public abstract IToken run() throws Exception;

		/**
		 * 
		 */
		protected TCFCommandWrapper() throws Throwable {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					try {
						TCFCommandWrapper.this.run();
					} catch (Throwable t) {
						excs[0] = t;
					}
				}
			});

			long timeout = System.currentTimeMillis() + 60 * 1000;
			while (true) {
				synchronized (this) {
					if (done)
						break;
					if (channel == null
							|| channel.getState() == IChannel.STATE_CLOSED)
						fail("channel closed");
				}
				Thread.sleep(100);
				if (System.currentTimeMillis() > timeout)
					fail("timeout");
			}

			if (excs[0] != null) {
				if (excs[0] instanceof AssertionError)
					throw (Throwable) excs[0];
				excs[0].printStackTrace();
				fail(excs[0].toString());
			}
		}
	}
	
	protected void assertNoError(Throwable error) {
		if (error == null)
			return;
		error.printStackTrace();
		while (error.getCause() != null)
			error = error.getCause();
		assertNull(errorString(error), error);
	}
}
