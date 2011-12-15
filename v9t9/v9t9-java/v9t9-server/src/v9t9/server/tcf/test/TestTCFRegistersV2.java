/**
 * 
 */
package v9t9.server.tcf.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRegisters.Location;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.junit.Before;
import org.junit.Test;

import v9t9.base.utils.Pair;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.f99b.cpu.CpuF99b;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.server.tcf.services.IRegistersV2;
import v9t9.server.tcf.services.IRegistersV2.RegisterChange;

/**
 * @author ejs
 *
 */
public class TestTCFRegistersV2 extends BaseTCFTest {

	private IRegistersV2 regV2;
	
	@Before
	public void getServices() {
		regV2 = (IRegistersV2) getService(IRegistersV2.NAME);
	}
	
	class RegRunner implements IRegistersV2.RegisterContentChangeListener {
		private final String notifyId;
		private final String contextId;
		private final Collection<Integer> regNums;
		private final int msDelay;
		private final int granularity;
		
		final List<List<Pair<Integer, RegisterChange[]>>> changeMap = new ArrayList<List<Pair<Integer, RegisterChange[]>>>();
		final Map<Integer, String> regNumContexts = new HashMap<Integer, String>();
		final Map<String, Integer> regNameIds = new HashMap<String, Integer>();
		
		final Set<Integer> expRegs = new HashSet<Integer>();
		private boolean isListening;
		//private Map<String,Integer> origRegVals;
		private byte[][] origRegVals = new byte[1][];
		private Location[] origRegLocs;
		
		public RegRunner(final String notifyId, final String contextId, 
				final Collection<Integer> regNums,
				final int msDelay,
				final int granularity) throws Throwable {
			assertNotNull(regNums);
			this.notifyId = notifyId;
			this.contextId = contextId;
			this.regNums = regNums;
			this.msDelay = msDelay;
			this.granularity = granularity;

			gatherRegisterContexts(regV2, contextId, null, regNameIds, regNumContexts);
		}
		
		public void startListening() throws Throwable {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					regV2.addListener(RegRunner.this);
				}
			});
			
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return regV2.startChangeNotify(
							notifyId, contextId, regNums, msDelay, granularity,
						new IRegistersV2.DoneCommand() {
							
							@Override
							public void done(Exception error) {
								try {
									assertNoError(error);
								} catch (Throwable t) {
									excs[0] = t;
								} finally {
									tcfDone();
								}								
							}
					});
				}		
			};

			isListening = true;

			// we should get one initial contentChanged report for the full range
			validateInitialContentChanged();
		}
		
		public void stopListening() throws Throwable {
			if (!isListening)
				return;
			
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return regV2.stopChangeNotify(notifyId,
						new IRegistersV2.DoneCommand() {
							
							@Override
							public void done(Exception error) {
								try {
									assertNoError(error);
								} catch (Throwable t) {
									excs[0] = t;
								} finally {
									tcfDone();
								}								
							}
					});
				}		
			};

			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					regV2.removeListener(RegRunner.this);
				}
			});
			
			isListening = false;
		}
		
		private void validateInitialContentChanged() {
			long limit = System.currentTimeMillis() + 5 * 1000;
			while (true) {
				synchronized (changeMap) {
					if (!changeMap.isEmpty())
						break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					break;
				}
				if (System.currentTimeMillis() > limit)
					fail("did not receive initial event");
			}

			synchronized (changeMap) {
				assertEquals("initial change", 1, changeMap.size());
				List<Pair<Integer, RegisterChange[]>> changeBlock = changeMap.get(0);
				assertEquals("initial change", 1, changeBlock.size());
				Pair<Integer, RegisterChange[]> changes = changeBlock.get(0);
				
				// each one should be covered
				assertEquals("initial change", regNums.size(), changes.second.length);
				
				for (RegisterChange change : changes.second) {
					assertTrue(change.regNum +"", regNums.contains(change.regNum));
				}
				
				changeMap.clear();
				expRegs.clear();
			}
		}
		
		/* (non-Javadoc)
		 * @see v9t9.server.tcf.services.IRegistersV2.RegisterContentChangeListener#contentChanged(java.lang.String, java.util.List)
		 */
		@Override
		public void contentChanged(String notifyId,
				List<Pair<Integer, RegisterChange[]>> regChanges) {
			synchronized (changeMap) {
				changeMap.add(regChanges);
			}
		}

		/** Sanity checking for register change reports */
		public void validateChanges() {
			synchronized (changeMap) {
				for (List<Pair<Integer, RegisterChange[]>> changeBlock : changeMap) {
					for (Pair<Integer, RegisterChange[]> changes : changeBlock) {
						int delta = changes.first;
						if (granularity > 5 && delta > granularity * 2)
							fail("unexpectedly large timestamp: " + delta);
						
						// make sure only interested regs were changed
						for (RegisterChange change : changes.second) {
							assertTrue(change.regNum +"", regNums.contains(change.regNum));
						}
						
						// make sure only *expected* regs were changed
						// (i.e. no left-over writes)
						for (RegisterChange change : changes.second) {
							assertTrue(change.regNum +"", expRegs.contains(change.regNum));
						}
					}
				}
			}
		}
		
		/** Sanity checking for the kinds of changes reported -- 
		 * each change in values[] should be detected.
		 * This assumes the caller knows exactly which events will
		 * come through and not be coalesced */
		public void validateRegChanges(int regNum, int mask, int[] values) {
		
			synchronized (changeMap) {
				int valIdx = 0;
				for (List<Pair<Integer, RegisterChange[]>> changeBlock : changeMap) {
					for (Pair<Integer, RegisterChange[]> changes : changeBlock) {
						for (RegisterChange change : changes.second) {
							if (change.regNum == regNum) {
								if (valIdx >= values.length)
									fail("too many changes reported");
								assertEquals("reg " + regNum + " at " + valIdx, 
										(values[valIdx] & mask), (change.value & mask))  ;
								valIdx++;
							}
						}
					}
				}
				
				if (valIdx < values.length)
					fail("reg " + regNum + ": did not see all changes, only " + valIdx + " of " + values.length);
			}
		}
		
		public void resetChanges() {
			synchronized (changeMap) {
				changeMap.clear();
				expRegs.clear();
			}
		}
		
		protected int getReg(final String contextId) throws Throwable {
			return TestTCFRegistersV2.this.getReg(regV2, contextId);
		}
		protected synchronized void setReg(final String contextId, int value) throws Throwable {
			TestTCFRegistersV2.this.setReg(regV2, contextId, value);
			synchronized (changeMap) {
				expRegs.add(regNameIds.get(contextId));
			}
		}

		/**
		 * @throws Throwable 
		 * 
		 */
		public void saveRegs() throws Throwable {
			
			Integer[] regs = regNums.toArray(new Integer[regNums.size()]);
			
			// remember and restore WP first (if it exists)
			Arrays.sort(regs, new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					if (regNumContexts.get(o1).equals("CPU.WP"))
						return -1;
					if (regNumContexts.get(o2).equals("CPU.WP"))
						return 1;
					return o1 - o2;
				}
				
			});
			
			final Location[] locs = new Location[regs.length];
			for (int i = 0; i < regs.length; i++) {
				locs[i] = new Location(regNumContexts.get(regs[i]), 0, 4);
			}
			origRegLocs = locs;
			
			new TCFCommandWrapper() {
				@Override
				public IToken run() throws Exception {
					return regV2.getm(locs,new IRegisters.DoneGet() {

						@Override
						public void doneGet(IToken token, Exception error,
								byte[] value) {
							excs[0] = error;
							origRegVals[0] = value;
							tcfDone();
						}
					});
				}
			};
			
			/*
			origRegVals = new HashMap<String, Integer>();
			
			// save off
			for (Integer regNum : regNums) {
				String id = regNumContexts.get(regNum);
				origRegVals.put(id, getReg(id));
			}
			*/
			
		}

		/**
		 * @param origRegVals
		 * @throws Throwable 
		 */
		public void restoreRegs() throws Throwable {
			new TCFCommandWrapper() {
				@Override
				public IToken run() throws Exception {
					return regV2.setm(origRegLocs, origRegVals[0], new IRegisters.DoneSet() {

						@Override
						public void doneSet(IToken token, Exception error) {
							excs[0] = error;
							tcfDone();
						}
					});
				}
			};
			
			/*
			if (origRegVals.containsKey("CPU.WP")) {
				// set this first since it affects the others!
				setReg("CPU.WP", origRegVals.get("CPU.WP"));
			}
			for (String id : origRegVals.keySet()) {
				setReg(id, origRegVals.get(id));
			}
			for (String id : origRegVals.keySet()) {
				int value = getReg(id);
				assertEquals("restoring " + id, origRegVals.get(id), (Integer) value);
			}
			*/
			
			final byte[][] copy = { null };
			

			new TCFCommandWrapper() {
				@Override
				public IToken run() throws Exception {
					return regV2.getm(origRegLocs, new IRegisters.DoneGet() {

						@Override
						public void doneGet(IToken token, Exception error,
								byte[] value) {
							excs[0] = error;
							copy[0] = value;
							tcfDone();
						}
					});
				}
			};
			
			assertTrue("reg copying", Arrays.equals(origRegVals[0], copy[0]));
		}
		
	};



	/** Make sure each register publishes the PROP_NUMBER attribute */
	@Test
	public void testGetRegisterContextNumbers() throws Throwable {
		for (final String contextId : new String[] { IMemoryDomain.NAME_CPU, IMemoryDomain.NAME_VIDEO }) {
			final String[][] kidsArr = { null };

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
			
			

			// this set tracks outstanding Registers#getContext events
			final boolean[] finished = { true };
			final Set<IToken> waiting = new HashSet<IToken>();
			
			final Map<String, RegistersContext> contexts 
				= new HashMap<String, IRegisters.RegistersContext>();
			
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
				assertTrue(kid, ctx.getProperties().get(IRegistersV2.PROP_NUMBER) instanceof Number);
			}
			
		}
	}

	/**
	 * @param contextId
	 * @return
	 */
	public Map<String, Integer> getRegisterContextToNumberMap(String contextId) {
		
		return null;
	}

	@Test
	public void testSimpleChange() throws Throwable {
		RegistersContext cpu = getRegistersContext(regV2, IMemoryDomain.NAME_CPU);
		String pcId = cpu.getID() + ".PC";
		int pcReg = cpu.getName().contains("9900") ? Cpu9900.REG_PC :
			cpu.getName().contains("F99b") ? CpuF99b.PC : -1;
		assertTrue(pcReg >= 0);
		
		final int QUANTUM = 50;
		
		// get only a single report per cycle
		RegRunner runner = new RegRunner("test1", IMemoryDomain.NAME_CPU, 
				Arrays.asList(new Integer[] { pcReg }),
				QUANTUM, 0);
		
		runner.saveRegs();
		try {
			runner.startListening();
			
			runner.setReg(pcId, 100);
			
			Thread.sleep(QUANTUM * 2);
			
			runner.setReg(pcId, 120);
			
			Thread.sleep(QUANTUM * 2);
			
			// flush events
			runner.stopListening();
			
			runner.validateChanges();
			runner.validateRegChanges(pcReg, 0xffff, new int[] { 100, 120 });
			runner.resetChanges();
		} finally {
			runner.stopListening();
			runner.restoreRegs();
		}
				
	}
	

	@Test
	public void testSimpleAllRegChange() throws Throwable {
		Map<String, RegistersContext> regContexts = new LinkedHashMap<String, IRegisters.RegistersContext>();
		Map<String, Integer> regIdToNumMap = new LinkedHashMap<String, Integer>();
		
		gatherRegisterContexts(regV2, IMemoryDomain.NAME_CPU, regContexts, regIdToNumMap, null);
		
		final int QUANTUM = 50;
		
		// get only a single report per cycle
		RegRunner runner = new RegRunner("test1", IMemoryDomain.NAME_CPU, 
				regIdToNumMap.values(), QUANTUM, 0);
		
		runner.saveRegs();
		
		try {
			runner.startListening();
			
			int cnt = 0;
			
			for (String id : regIdToNumMap.keySet()) {
				runner.setReg(id, cnt);
				cnt += 2;
			}
			
			Thread.sleep(QUANTUM * 2);

			cnt = 0;
			for (String id : regIdToNumMap.keySet()) {
				runner.setReg(id, cnt + 128);
				cnt += 2;
			}
			
			Thread.sleep(QUANTUM * 2);
			
			// flush events
			runner.stopListening();
			
			runner.validateChanges();
			
			cnt = 0;
			for (int val : regIdToNumMap.values()) {
				runner.validateRegChanges(val, 0xffff, new int[] { cnt, cnt + 128 });
				cnt += 2;
			}
			
			runner.resetChanges();
		} finally {
			runner.stopListening();
			runner.restoreRegs();
		}
				
	}
	

	@Test
	public void testContinuousAllRegChange() throws Throwable {
		Map<String, RegistersContext> regContexts = new LinkedHashMap<String, IRegisters.RegistersContext>();
		Map<String, Integer> regIdToNumMap = new LinkedHashMap<String, Integer>();
		
		gatherRegisterContexts(regV2, IMemoryDomain.NAME_VIDEO, regContexts, regIdToNumMap, null);
		
		final int QUANTUM = 1000;
		
		// get continuous reports of changes
		RegRunner runner = new RegRunner("testV", IMemoryDomain.NAME_VIDEO, 
				regIdToNumMap.values(), QUANTUM, -1);
		
		runner.saveRegs();
		
		try {
			runner.startListening();
			
			int cnt = 0;
			
			for (String id : regIdToNumMap.keySet()) {
				runner.setReg(id, cnt);
				runner.setReg(id, -cnt);
				cnt += 2;
			}

			// no delay
			
			cnt = 0;
			for (String id : regIdToNumMap.keySet()) {
				runner.setReg(id, cnt + 128);
				runner.setReg(id, -(cnt + 128));
				cnt += 2;
			}
			
			// no delay
			
			
			// flush events
			runner.stopListening();
			
			runner.validateChanges();
			
			cnt = 0;
			for (int val : regIdToNumMap.values()) {
				runner.validateRegChanges(val, 0xff, new int[] { cnt, -cnt, cnt + 128, -(cnt + 128) });
				cnt += 2;
			}
			
			runner.resetChanges();
		} finally {
			runner.stopListening();
			runner.restoreRegs();
		}
				
	}

}

