/*
  TestTCFMemory.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.tcf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IMemory.MemoryContext;
import org.junit.Before;
import org.junit.Test;

import v9t9.common.memory.IMemoryDomain;

/**
 * @author ejs
 *
 */
public class TestTCFMemory extends BaseTCFTest {

	private IMemory mem;
	
	@Before
	public void getServices() {
		mem = (IMemory) getService(IMemory.NAME);
	}
	
	@Test
	public void testGetRootChildren() throws Throwable {
		for (final String parent : new String[] { "", "root", null }) {
			new TCFCommandWrapper() {
				public IToken run() throws Exception {
					return mem.getChildren(parent, new IMemory.DoneGetChildren() {
						
						/* (non-Javadoc)
						 * @see org.eclipse.tm.tcf.services.IMemory.DoneGetChildren#doneGetChildren(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
						 */
						@Override
						public void doneGetChildren(IToken token, Exception error,
								String[] context_ids) {
							try {
								assertNoError(error);
								
								assertEquals(4, context_ids.length);
								List<String> kids = Arrays.asList(context_ids);
								assertTrue(kids.contains(IMemoryDomain.NAME_CPU));
								assertTrue(kids.contains(IMemoryDomain.NAME_GRAPHICS));
								assertTrue(kids.contains(IMemoryDomain.NAME_SPEECH));
								assertTrue(kids.contains(IMemoryDomain.NAME_VIDEO));
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
	public void testGetContextErrors() throws Throwable {
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return mem.getContext("root", new IMemory.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							MemoryContext context) {
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
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return mem.getContext("", new IMemory.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							MemoryContext context) {
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
		
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return mem.getContext(null, new IMemory.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							MemoryContext context) {
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
	@Test
	public void testGetContexts() throws Throwable {
		new TCFCommandWrapper() {
			public IToken run() throws Exception {
				return mem.getContext(IMemoryDomain.NAME_CPU, new IMemory.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							MemoryContext context) {
						try {
							assertNoError(error);
							
							assertEquals(0, context.getStartBound());
							assertEquals(65536, context.getEndBound());
							assertEquals(IMemoryDomain.NAME_CPU, context.getID());
							assertEquals("Console", context.getName());
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
				return mem.getContext(IMemoryDomain.NAME_VIDEO, new IMemory.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							MemoryContext context) {
						try {
							assertNoError(error);
							
							assertEquals(0, context.getStartBound());
							assertTrue(context.getEndBound().intValue() > 0);
							assertEquals(IMemoryDomain.NAME_VIDEO, context.getID());
							assertEquals("VDP", context.getName());
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
				return mem.getContext(IMemoryDomain.NAME_GRAPHICS, new IMemory.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							MemoryContext context) {
						try {
							assertNoError(error);
							
							assertEquals(0, context.getStartBound());
							assertTrue(context.getEndBound().intValue() > 0);
							assertEquals(IMemoryDomain.NAME_GRAPHICS, context.getID());
							assertEquals("GROM/GRAM", context.getName());
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
				return mem.getContext(IMemoryDomain.NAME_SPEECH, new IMemory.DoneGetContext() {
					
					@Override
					public void doneGetContext(IToken token, Exception error,
							MemoryContext context) {
						try {
							assertNoError(error);
							
							assertEquals(0, context.getStartBound());
							assertTrue(context.getEndBound().intValue() > 0);
							assertEquals(IMemoryDomain.NAME_SPEECH, context.getID());
							assertEquals("Speech", context.getName());
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
