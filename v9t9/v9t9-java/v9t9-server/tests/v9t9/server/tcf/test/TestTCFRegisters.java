/*
  TestTCFRegisters.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.server.tcf.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters;
import org.eclipse.tm.tcf.services.IRegisters.RegistersContext;
import org.junit.Before;
import org.junit.Test;

import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author ejs
 *
 */
public class TestTCFRegisters extends BaseTCFTest {

	private IRegisters reg;
	
	@Before
	public void getServices() {
		reg = (IRegisters) getService(IRegisters.NAME);
	}
	
	@Test
	public void testGetRootChildren() throws Throwable {
		for (final String parent : new String[] { "", "root", null }) {
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return reg.getChildren(parent, new IRegisters.DoneGetChildren() {
						
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.services.IMemory.DoneGetChildren#doneGetChildren(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
						 */
						@Override
						public void doneGetChildren(IToken token, Exception error,
								String[] context_ids) {
							try {
								assertNoError(error);
								
								assertEquals(3, context_ids.length);
								List<String> kids = Arrays.asList(context_ids);
								assertTrue(kids.contains(IRegisterAccess.ID_CPU));
								assertTrue(kids.contains(IRegisterAccess.ID_VIDEO));
								assertTrue(kids.contains(IRegisterAccess.ID_SOUND));
							} catch (Throwable t) {
								excs[0] = t;
							} finally {
								tcfDone();
							}
						}
					});
				}
			};
		};
		
	}
	
	@Test
	public void testGetContexts() throws Throwable {
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return reg.getContext(IMemoryDomain.NAME_CPU, new IRegisters.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							RegistersContext context) {
						try {
							assertNoError(error);
							
							assertEquals(IMemoryDomain.NAME_CPU, context.getID());
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return reg.getContext(IMemoryDomain.NAME_VIDEO, new IRegisters.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							RegistersContext context) {
						try {
							assertNoError(error);
							
							assertEquals(IMemoryDomain.NAME_VIDEO, context.getID());
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
	}

	@Test
	public void testGetContextErrors() throws Throwable {
		for (final String context : new String[] { "root", "", null }) {
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return reg.getContext(context, new IRegisters.DoneGetContext() {
						
						@Override
						public void doneGetContext(IToken token, Exception error,
								RegistersContext context) {
							try {
								assertNotNull("expected error", error);
							} catch (Throwable t) {
								excs[0] = t;
							} finally {
								tcfDone();
							}
						}
	
					});
				}
			};
		}
	
		for (final String contextId : new String[] { IMemoryDomain.NAME_CPU, IMemoryDomain.NAME_VIDEO }) {
			
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return reg.getContext(contextId + ".BOGUS", new IRegisters.DoneGetContext() {
						
						@Override
						public void doneGetContext(IToken token, Exception error,
								RegistersContext context) {
							try {
								assertNotNull(error);
							} catch (Throwable t) {
								excs[0] = t;
							} finally {
								tcfDone();
							}
						}
	
					});
				}
			};
			
		}
	}
	

	@Test
	public void testGetGroupChildren() throws Throwable {
		final RegistersContext cpu = getRegistersContext(reg, IMemoryDomain.NAME_CPU);
		final RegistersContext video = getRegistersContext(reg, IMemoryDomain.NAME_VIDEO);
		
		for (final String contextId : new String[] { IMemoryDomain.NAME_CPU, IMemoryDomain.NAME_VIDEO }) {
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return reg.getContext(contextId, new IRegisters.DoneGetContext() {
						
						@Override
						public void doneGetContext(IToken token, Exception error,
								RegistersContext context) {
							try {
								assertNull(error);
								assertEquals(contextId, context.getID());
								assertNotNull(context.getName());
							} catch (Throwable t) {
								excs[0] = t;
							} finally {
								tcfDone();
							}
						}
	
					});
				}
			};
		}
		
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return reg.getChildren(IMemoryDomain.NAME_CPU, new IRegisters.DoneGetChildren() {
					
					/* (non-Javadoc)
					 * @see org.eclipse.tm.tcf.services.IMemory.DoneGetChildren#doneGetChildren(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
					 */
					@Override
					public void doneGetChildren(IToken token, Exception error,
							String[] context_ids) {
						try {
							assertNoError(error);
							
							assertTrue(context_ids.length+"", context_ids.length > 0);
							List<String> ids = Arrays.asList(context_ids);
							assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".PC"));
							
							if (cpu.getName().contains("F99b")) {
								assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".SR"));
								assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".UP"));
								assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".RP"));
								assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".LP"));
							}
							else if (cpu.getName().contains("9900")) {
								assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".R0"));
								assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".R15"));
								assertTrue(ids.contains(IMemoryDomain.NAME_CPU + ".WP"));
							}
							else {
								fail("unknown CPU");
							}
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
		
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return reg.getChildren(IMemoryDomain.NAME_VIDEO, new IRegisters.DoneGetChildren() {
					
					/* (non-Javadoc)
					 * @see org.eclipse.tm.tcf.services.IMemory.DoneGetChildren#doneGetChildren(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
					 */
					@Override
					public void doneGetChildren(IToken token, Exception error,
							String[] context_ids) {
						try {
							assertNoError(error);
							
							assertTrue(context_ids.length+"", context_ids.length > 0);
							List<String> ids = Arrays.asList(context_ids);
							assertTrue("VR0", ids.contains(IMemoryDomain.NAME_VIDEO + ".VR0"));
							assertTrue("VR7", ids.contains(IMemoryDomain.NAME_VIDEO + ".VR7"));
							assertTrue("ST", ids.contains(IMemoryDomain.NAME_VIDEO + ".ST"));
							
							if (video.getName().contains("9938")) {
								assertTrue("VR46", ids.contains(IMemoryDomain.NAME_VIDEO + ".VR46"));
								assertTrue("SR7", ids.contains(IMemoryDomain.NAME_VIDEO + ".SR7"));
								assertTrue("PAL15", ids.contains(IMemoryDomain.NAME_VIDEO + ".PAL15"));
								
							}
							else if (video.getName().contains("9918")) {
								assertTrue("VR7", ids.contains(IMemoryDomain.NAME_VIDEO + ".VR7"));
								assertFalse("VR46", ids.contains(IMemoryDomain.NAME_VIDEO + ".VR46"));
								assertFalse("SR0", ids.contains(IMemoryDomain.NAME_VIDEO + ".SR0"));
							}
							else {
								fail("unknown video");
							}
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
		
	}
	

	@Test
	public void testGetCpuRegisterContexts() throws Throwable {
		final RegistersContext cpu = getRegistersContext(reg, IMemoryDomain.NAME_CPU);

		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return reg.getContext(IMemoryDomain.NAME_CPU + ".PC", new IRegisters.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							RegistersContext context) {
						try {
							assertNull(error);
							assertEquals(IMemoryDomain.NAME_CPU + ".PC", context.getID());
							assertNotNull(context.getName());
							assertEquals(IRegisters.ROLE_PC, context.getRole());
							assertEquals(2, context.getSize());
							assertNull(context.getMemoryAddress());
							assertNull(context.getMemoryContext());
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}

				});
			}
		};

		
		if (cpu.getName().contains("9900")) {
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return reg.getContext(IMemoryDomain.NAME_CPU + ".WP", new IRegisters.DoneGetContext() {
						
						@Override
						public void doneGetContext(IToken token, Exception error,
								RegistersContext context) {
							try {
								assertNull(error);
								assertEquals(IMemoryDomain.NAME_CPU + ".WP", context.getID());
								assertNotNull(context.getName());
								assertEquals(IRegisters.ROLE_FP, context.getRole());
								assertEquals(2, context.getSize());
								assertNull(context.getMemoryAddress());
								assertNull(context.getMemoryContext());
							} catch (Throwable t) {
								excs[0] = t;
							} finally {
								tcfDone();
							}
						}

					});
				}
			};
			
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return reg.getContext(IMemoryDomain.NAME_CPU + ".R4", new IRegisters.DoneGetContext() {
						
						@Override
						public void doneGetContext(IToken token, Exception error,
								RegistersContext context) {
							try {
								assertNull(error);
								assertEquals(IMemoryDomain.NAME_CPU + ".R4", context.getID());
								assertNull(context.getName());	// boring register
								assertEquals(IRegisters.ROLE_CORE, context.getRole());
								assertEquals(2, context.getSize());
								assertEquals(IMemoryDomain.NAME_CPU, context.getMemoryContext());
								assertNotNull(context.getMemoryAddress());
							} catch (Throwable t) {
								excs[0] = t;
							} finally {
								tcfDone();
							}
						}

					});
				}
			};

			
		}
		else if (cpu.getName().contains("F99b")) {
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return reg.getContext(IMemoryDomain.NAME_CPU + ".SP", new IRegisters.DoneGetContext() {
						
						@Override
						public void doneGetContext(IToken token, Exception error,
								RegistersContext context) {
							try {
								assertNull(error);
								assertEquals(IMemoryDomain.NAME_CPU + ".SP", context.getID());
								assertNotNull(context.getName());
								assertEquals(IRegisters.ROLE_SP, context.getRole());
								assertEquals(2, context.getSize());
								assertNull(context.getMemoryAddress());
								assertNull(context.getMemoryContext());
							} catch (Throwable t) {
								excs[0] = t;
							} finally {
								tcfDone();
							}
						}

					});
				}
			};

		}
		else {
			fail("unknown CPU");
			
		}
	}


	@Test
	public void testGetSetCpuRegisters() throws Throwable {
		final RegistersContext cpuPC = getRegistersContext(reg, IMemoryDomain.NAME_CPU + ".PC");

		final byte[][] saved = { null };
		
		// read...
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return cpuPC.get(new IRegisters.DoneGet() {
					
					@Override
					public void doneGet(IToken token, Exception error, byte[] value) {
						try {
							assertNull(error);
							assertNotNull(value);
							assertEquals(2, value.length);
							saved[0] = value;
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
		
		// change...
		final byte[] newValue = new byte[] { (byte) 0xff, (byte) 0xaa };
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return cpuPC.set(newValue, new IRegisters.DoneSet() {
					
					@Override
					public void doneSet(IToken token, Exception error) {
						try {
							assertNull(error);
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
		
		// verify...
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return cpuPC.get(new IRegisters.DoneGet() {
					
					@Override
					public void doneGet(IToken token, Exception error, byte[] value) {
						try {
							assertNull(error);
							assertNotNull(value);
							assertEquals(2, value.length);
							assertTrue("got saved value", Arrays.equals(newValue, value));
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
		
		// restore...
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return cpuPC.set(saved[0], new IRegisters.DoneSet() {
					
					@Override
					public void doneSet(IToken token, Exception error) {
						try {
							assertNull(error);
						} catch (Throwable t) {
							excs[0] = t;
						} finally {
							tcfDone();
						}
					}
				});
			}
		};
	}

	
}
