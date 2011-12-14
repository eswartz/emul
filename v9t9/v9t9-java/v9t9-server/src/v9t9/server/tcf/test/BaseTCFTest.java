/**
 * 
 */
package v9t9.server.tcf.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import v9t9.server.tcf.EmulatorTCFQueue;
import v9t9.server.tcf.EmulatorTCFServiceProvider;
import v9t9.server.tcf.services.IRegistersV2;
import v9t9.server.tcf.services.ISettings;
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
	private static boolean connected;
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
	public static void setupEmulator() throws Throwable {
		System.out.println("setupEmulator()");
		if (!connected)
			connectEmulator();
		
		
		pauseMachine();
	}
	
	protected static void connectEmulator() {
		
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
		
		connected = true;
	}
	
	protected static void pauseMachine() throws Throwable {
		if (channel == null)
			return;

		new TCFCommandWrapper() {
			
			@Override
			public IToken run() throws Exception {
				IService settings = channel.getRemoteService(ISettings.NAME);
				return new Command(channel, settings, "set", new Object[] { "PauseMachine", Boolean.TRUE }) {
					@Override
					public void done(Exception error, Object[] args) {
						try {
							assertNull(error);
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
					
				}.token;
			}
		};
	}
	
	@Before
	public void assertConnected() {
		if (peer == null || channel == null)
			fail("not connected");
	}

	public static void resumeMachine() throws Throwable {
		if (channel == null)
			return;
			
		new TCFCommandWrapper() {
			
			@Override
			public IToken run() throws Exception {
				IService settings = channel.getRemoteService(ISettings.NAME);
				return new Command(channel, settings, "set", new Object[] { "PauseMachine", Boolean.FALSE }) {
					@Override
					public void done(Exception error, Object[] args) {
						try {
							assertNull(error);
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
					
				}.token;
			}
		};
	}
	
	@AfterClass
	public static void teardownEmulator() throws Throwable {
		resumeMachine();
		
		disconnectEmulator();
	}
	
	protected static void disconnectEmulator() {
	
		// thanks, TCF, for not having a way to shut down and restart
		if (false) {
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
	public static abstract class TCFCommandWrapper {

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
	
	protected RegistersContext getRegistersContext(final IRegisters reg, final String contextId) throws Throwable {
		final RegistersContext[] ctxs = { null };

		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return reg.getContext(contextId, new IRegisters.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							RegistersContext context) {
						try {
							assertNull(error);
							ctxs[0] = context;
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}

				});
			}
		};
		return ctxs[0];
	}

	
	// TODO: this is assuming big-endian
	protected void setReg(final IRegisters reg, final String regContextId, final int value) throws Throwable {
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				try {
					return new Command(channel, reg, "set", new Object[] { 
							regContextId,
							toBigEndianArray(value)
					}) {
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.core.Command#done(java.lang.Exception, java.lang.Object[])
						 */
						@Override
						public void done(Exception error, Object[] args) {
							if (error == null) {
								assert args.length == 1;
								error = toError(args[0]);
							}
						}
					}.token;
				} finally {
					tcfDone();
				}
			}		
		};
		
	}


	// TODO: this is assuming big-endian
	protected int getReg(final IRegisters reg, final String regContextId) throws Throwable {
		final int[] values = { 0 };
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				try {
					return new Command(channel, reg, "get", new Object[] { 
							regContextId
					}) {
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.core.Command#done(java.lang.Exception, java.lang.Object[])
						 */
						@Override
						public void done(Exception error, Object[] args) {
							if (error == null) {
								assert args.length == 2;
								error = toError(args[0]);
								values[0] = fromBigEndianArray(JSON.toByteArray(args[1]));
							}
						}
					}.token;
				} finally {
					tcfDone();
				}
			}		
		};
		return values[0];
	}

	protected JSON.Binary toBigEndianArray(int value) {
		int size = 4;
		byte[] data = new byte[size];
		
		for (int i = 0; i < size; i++) {
			data[size - i - 1] = (byte) value;
			value >>= 8;
		}

		return new JSON.Binary(data, 0, size);
	}
	
	protected int fromBigEndianArray(byte[] data) {
		if (data == null)
			return 0;
		
		int size = data.length;

		int value = 0;
		for (int i = 0; i < size; i++) {
			value |= data[size - i - 1] << (8 * i);
		}
		
		return value;
	}


	/**
	 * @param regV2
	 * @param contexts
	 * @param regIdToNumberMap
	 * @param regNumberToIdMap
	 * @throws Throwable
	 * @throws InterruptedException
	 */
	protected void gatherRegisterContexts(final IRegistersV2 regV2,
			final String contextId, 
			final Map<String, RegistersContext> regContexts,
			final Map<String, Integer> regIdToNumberMap,
			final Map<Integer, String> regNumberToIdMap) throws Throwable {
		// this set tracks outstanding Registers#getContext events
		final String[][] kidsArr = { null };
		final boolean[] finished = { true };
		final Set<IToken> waiting = new HashSet<IToken>();
		final Map<String, RegistersContext> contexts = new HashMap<String, IRegisters.RegistersContext>();
		

		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return regV2.getChildren(contextId, new IRegisters.DoneGetChildren() {
					
					/* (non-Javadoc)
					 * @see org.eclipse.tm.tcf.services.IMemory.DoneGetChildren#doneGetChildren(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
					 */
					@Override
					public void doneGetChildren(IToken token, Exception error,
							String[] context_ids) {
						try {
							assertNoError(error);
							kidsArr[0] = context_ids;
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
		
		
		// asynchronously fetch all the contexts
		for (final String kid : kidsArr[0]) {
			final IRegisters.DoneGetContext done = new IRegisters.DoneGetContext() {

				/* (non-Javadoc)
				 * @see org.eclipse.tm.tcf.services.IRegisters.DoneGetContext#doneGetContext(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tm.tcf.services.IRegisters.RegistersContext)
				 */
				@Override
				public void doneGetContext(IToken token, Exception error,
						RegistersContext context) {
					assertNoError(error);
	
					synchronized (waiting) {
						contexts.put(kid, context);
						waiting.remove(token);
						if (waiting.isEmpty())
							finished[0] = true;
					}
				}
			};
			
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					try {
						IToken token = regV2.getContext(kid, done);
						synchronized (waiting) {
							finished[0] = false;
							waiting.add(token);
						}
						return token;
					} finally {
						tcfDone();
					}
				}		
			};
		}
		
		
		// wait...
		long timeout = System.currentTimeMillis() + 10 * 1000;
		while (!finished[0]) {
			if (System.currentTimeMillis() > timeout)
				fail("timed out waiting for context fetches");
			Thread.sleep(500);
		}
					
		for (String kid : kidsArr[0]) {
			RegistersContext ctx = contexts.get(kid);
			int reg = ((Number) ctx.getProperties().get(IRegistersV2.PROP_NUMBER)).intValue();
			if (regContexts != null)
				regContexts.put(kid, ctx);
			if (regIdToNumberMap != null)
				regIdToNumberMap.put(kid, reg);
			if (regNumberToIdMap != null)
				regNumberToIdMap.put(reg, kid);
		}
	}
}
