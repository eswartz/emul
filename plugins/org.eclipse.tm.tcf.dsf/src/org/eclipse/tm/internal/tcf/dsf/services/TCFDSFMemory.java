/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.services;

import java.util.Hashtable;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.internal.tcf.dsf.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IMemory.MemoryContext;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;
import org.osgi.framework.BundleContext;


public class TCFDSFMemory extends AbstractDsfService implements org.eclipse.dd.dsf.debug.service.IMemory {
    
    private class MemoryCache implements TCFDSFExecutionDMC.DataCache {
        
        final TCFDataCache<org.eclipse.tm.tcf.services.IMemory.MemoryContext> context;
        
        MemoryCache(final IChannel channel, final TCFDSFExecutionDMC exe) {
            context = new TCFDataCache<org.eclipse.tm.tcf.services.IMemory.MemoryContext>(channel) {
                @Override
                public boolean startDataRetrieval() {
                    assert command == null;
                    String id = exe.getTcfContextId();
                    if (id == null || tcf_mem_service == null) {
                        data = null;
                        valid = true;
                        return true;
                    }
                    command = tcf_mem_service.getContext(id,
                            new org.eclipse.tm.tcf.services.IMemory.DoneGetContext() {
                        public void doneGetContext(IToken token, Exception err,
                                org.eclipse.tm.tcf.services.IMemory.MemoryContext ctx) {
                            if (command != token) return;
                            command = null;
                            if (err != null) {
                                error = err;
                            }
                            else {
                                data = ctx;
                            }
                            valid = true;
                            validate();
                        }
                    });
                    return false;
                }
            };
        }
    }
    
    private final org.eclipse.tm.tcf.services.IMemory.MemoryListener mem_listener =
        new org.eclipse.tm.tcf.services.IMemory.MemoryListener() {

            public void contextAdded(MemoryContext[] contexts) {
            }

            public void contextChanged(MemoryContext[] contexts) {
            }

            public void contextRemoved(String[] context_ids) {
            }

            public void memoryChanged(String context_id, Number[] addr, long[] size) {
                TCFDSFRunControl rc = getServicesTracker().getService(TCFDSFRunControl.class);
                TCFDSFExecutionDMC exe = rc.getContext(context_id);
                if (exe == null || exe.memory_cache == null) return;
                for (int n = 0; n < addr.length; n++) {
                    long count = size[n];
                    // TODO: DSF does not support address ranges
                    if (count > 256) count = 256;
                    IAddress[] addresses = new IAddress[(int)count];
                    for (int i = 0; i < (int)count; i++) {
                        addresses[i] = new TCFAddress(addr[n]).add(i);
                    }
                    getSession().dispatchEvent(new MemoryChangedEvent(exe, addresses), getProperties());
                }
            }
    };
    
    private final IChannel channel;
    private final org.eclipse.tm.tcf.services.IMemory tcf_mem_service;

    public TCFDSFMemory(DsfSession session, IChannel channel, final RequestMonitor monitor) {
        super(session);
        this.channel = channel;
        tcf_mem_service = channel.getRemoteService(org.eclipse.tm.tcf.services.IMemory.class);
        if (tcf_mem_service != null) tcf_mem_service.addListener(mem_listener);
        initialize(new RequestMonitor(getExecutor(), monitor) { 
            @Override
            protected void handleOK() {
                String[] class_names = {
                        org.eclipse.dd.dsf.debug.service.IMemory.class.getName(),
                        TCFDSFMemory.class.getName()
                };
                register(class_names, new Hashtable<String,String>());
                monitor.done();
            }
        });
    }

    @Override 
    public void shutdown(RequestMonitor monitor) {
        unregister();
        super.shutdown(monitor);
    }

    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    public void fillMemory(final IDMContext dmc, final IAddress address, final long offset,
            final int word_size, final int count, final byte[] pattern, final RequestMonitor rm) {
        if (tcf_mem_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Memory access service is not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (dmc instanceof TCFDSFExecutionDMC) {
            final TCFDSFExecutionDMC ctx = (TCFDSFExecutionDMC)dmc;
            if (ctx.memory_cache == null) ctx.memory_cache = new MemoryCache(channel, ctx);
            MemoryCache cache = (MemoryCache)ctx.memory_cache;
            if (!cache.context.validate()) {
                cache.context.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        fillMemory(dmc, address, offset, word_size, count, pattern, rm);
                    }
                });
                return;
            }
            if (cache.context.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.context.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            org.eclipse.tm.tcf.services.IMemory.MemoryContext mem = cache.context.getData();
            if (mem == null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Invalid DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            mem.fill(address.add(offset).getValue(), word_size, pattern, count * word_size, 0,
                    new org.eclipse.tm.tcf.services.IMemory.DoneMemory() {
                public void doneMemory(IToken token, MemoryError error) {
                    if (rm.isCanceled()) return;
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                    }
                    rm.done();
                }
            });
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void getMemory(final IDMContext dmc, final IAddress address, final long offset,
            final int word_size, final int count, final DataRequestMonitor<MemoryByte[]> rm) {
        if (tcf_mem_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Memory access service is not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (dmc instanceof TCFDSFExecutionDMC) {
            final TCFDSFExecutionDMC ctx = (TCFDSFExecutionDMC)dmc;
            if (ctx.memory_cache == null) ctx.memory_cache = new MemoryCache(channel, ctx);
            MemoryCache cache = (MemoryCache)ctx.memory_cache;
            if (!cache.context.validate()) {
                cache.context.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        getMemory(dmc, address, offset, word_size, count, rm);
                    }
                });
                return;
            }
            if (cache.context.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.context.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            org.eclipse.tm.tcf.services.IMemory.MemoryContext mem = cache.context.getData();
            if (mem == null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Invalid DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            final byte[] buffer = new byte[word_size * count];
            mem.get(address.add(offset).getValue(), word_size, buffer, 0, count * word_size, 0,
                    new org.eclipse.tm.tcf.services.IMemory.DoneMemory() {
                public void doneMemory(IToken token, MemoryError error) {
                    if (rm.isCanceled()) return;
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                    }
                    MemoryByte[] res = new MemoryByte[buffer.length];
                    for (int i = 0; i < buffer.length; i++) {
                        res[i] = new MemoryByte(buffer[i]);
                    }
                    rm.done();
                }
            });
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void setMemory(final IDMContext dmc, final IAddress address, final long offset,
            final int word_size, final int count, final byte[] buffer, final RequestMonitor rm) {
        if (tcf_mem_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Memory access service is not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (dmc instanceof TCFDSFExecutionDMC) {
            final TCFDSFExecutionDMC ctx = (TCFDSFExecutionDMC)dmc;
            if (ctx.memory_cache == null) ctx.memory_cache = new MemoryCache(channel, ctx);
            MemoryCache cache = (MemoryCache)ctx.memory_cache;
            if (!cache.context.validate()) {
                cache.context.addWaitingRequest(new IDataRequest() {
                    public void cancel() {
                        rm.setStatus(new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Canceled", null)); //$NON-NLS-1$
                        rm.setCanceled(true);
                        rm.done();
                    }
                    public void done() {
                        setMemory(dmc, address, offset, word_size, count, buffer, rm);
                    }
                });
                return;
            }
            if (cache.context.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.context.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            org.eclipse.tm.tcf.services.IMemory.MemoryContext mem = cache.context.getData();
            if (mem == null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Invalid DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            mem.set(address.add(offset).getValue(), word_size, buffer, 0, count * word_size, 0,
                    new org.eclipse.tm.tcf.services.IMemory.DoneMemory() {
                public void doneMemory(IToken token, MemoryError error) {
                    if (rm.isCanceled()) return;
                    if (error != null) {
                        rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                    }
                    rm.done();
                }
            });
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
            rm.done();
        }
    }
}
