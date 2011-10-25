/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.ISymbols;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;
import org.eclipse.tm.tcf.util.TCFDataCache;

/**
 * A memory block retrieval allows the user interface to request a memory block from a debugger when needed.
 * TCF memory block retrieval is based on TCF Memory service.
 */
class TCFMemoryBlockRetrieval implements IMemoryBlockRetrievalExtension {

    private final TCFNodeExecContext exec_ctx;
    private final HashSet<MemoryBlock> mem_blocks = new HashSet<MemoryBlock>();

    private class MemoryBlock extends PlatformObject implements IMemoryBlockExtension {

        private final String expression;
        private final long length;
        private final Set<Object> connections = new HashSet<Object>();
        private final TCFDataCache<IExpressions.Expression> remote_expression;
        private final TCFDataCache<IExpressions.Value> expression_value;
        private final TCFDataCache<ISymbols.Symbol> expression_type;

        private boolean disposed;

        MemoryBlock(final String expression, long length) {
            this.expression = expression;
            this.length = length;
            mem_blocks.add(this);
            final TCFLaunch launch = exec_ctx.model.getLaunch();
            final IChannel channel = launch.getChannel();
            remote_expression = new TCFDataCache<IExpressions.Expression>(channel) {
                @Override
                protected boolean startDataRetrieval() {
                    IExpressions exps = launch.getService(IExpressions.class);
                    if (exps == null) {
                        set(null, new Exception("Expressions service not available"), null);
                        return true;
                    }
                    command = exps.create(exec_ctx.id, null, expression, new IExpressions.DoneCreate() {
                        public void doneCreate(IToken token, Exception error, IExpressions.Expression context) {
                            if (disposed) {
                                IExpressions exps = channel.getRemoteService(IExpressions.class);
                                exps.dispose(context.getID(), new IExpressions.DoneDispose() {
                                    public void doneDispose(IToken token, Exception error) {
                                        if (error == null) return;
                                        if (channel.getState() != IChannel.STATE_OPEN) return;
                                        Activator.log("Error disposing remote expression evaluator", error);
                                    }
                                });
                                return;
                            }
                            set(token, error, context);
                        }
                    });
                    return false;
                }
            };
            expression_value = new TCFDataCache<IExpressions.Value>(channel) {
                @Override
                protected boolean startDataRetrieval() {
                    if (!remote_expression.validate(this)) return false;
                    final IExpressions.Expression ctx = remote_expression.getData();
                    if (ctx == null) {
                        set(null, null, null);
                        return true;
                    }
                    IExpressions exps = launch.getService(IExpressions.class);
                    command = exps.evaluate(ctx.getID(), new IExpressions.DoneEvaluate() {
                        public void doneEvaluate(IToken token, Exception error, IExpressions.Value value) {
                            set(token, error, value);
                        }
                    });
                    return false;
                }
            };
            expression_type = new TCFDataCache<ISymbols.Symbol>(channel) {
                @Override
                protected boolean startDataRetrieval() {
                    if (!expression_value.validate(this)) return false;
                    IExpressions.Value val = expression_value.getData();
                    if (val == null) {
                        set(null, expression_value.getError(), null);
                        return true;
                    }
                    TCFDataCache<ISymbols.Symbol> type_cache = exec_ctx.model.getSymbolInfoCache(val.getTypeID());
                    if (type_cache == null) {
                        set(null, null, null);
                        return true;
                    }
                    if (!type_cache.validate(this)) return false;
                    set(null, type_cache.getError(), type_cache.getData());
                    return true;
                }
            };
        }

        public synchronized void connect(Object client) {
            connections.add(client);
        }

        public synchronized void disconnect(Object client) {
            connections.remove(client);
        }

        public synchronized Object[] getConnections() {
            return connections.toArray(new Object[connections.size()]);
        }

        public void dispose() throws DebugException {
            new TCFDebugTask<Boolean>(exec_ctx.getChannel()) {
                public void run() {
                    disposed = true;
                    expression_value.dispose();
                    expression_type.dispose();
                    if (remote_expression.isValid() && remote_expression.getData() != null) {
                        final IChannel channel = exec_ctx.channel;
                        if (channel.getState() == IChannel.STATE_OPEN) {
                            IExpressions exps = channel.getRemoteService(IExpressions.class);
                            exps.dispose(remote_expression.getData().getID(), new IExpressions.DoneDispose() {
                                public void doneDispose(IToken token, Exception error) {
                                    if (error == null) return;
                                    if (channel.getState() != IChannel.STATE_OPEN) return;
                                    Activator.log("Error disposing remote expression evaluator", error);
                                }
                            });
                        }
                    }
                    remote_expression.dispose();
                    mem_blocks.remove(MemoryBlock.this);
                    done(Boolean.TRUE);
                }
            }.getD();
        }

        public int getAddressSize() throws DebugException {
            return new TCFDebugTask<Integer>(exec_ctx.getChannel()) {
                public void run() {
                    if (exec_ctx.isDisposed()) {
                        error("Context is disposed");
                    }
                    else {
                        TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                        if (!cache.validate(this)) return;
                        if (cache.getError() != null) {
                            error(cache.getError());
                        }
                        else {
                            IMemory.MemoryContext mem = cache.getData();
                            if (mem == null) {
                                error("Context does not provide memory access");
                            }
                            else {
                                done(mem.getAddressSize());
                            }
                        }
                    }
                }
            }.getD();
        }

        public int getAddressableSize() throws DebugException {
            // TODO: support for addressable size other then 1 byte
            return 1;
        }

        public BigInteger getBigBaseAddress() throws DebugException {
            return new TCFDebugTask<BigInteger>(exec_ctx.getChannel()) {
                public void run() {
                    if (!expression_value.validate()) {
                        expression_value.wait(this);
                    }
                    else if (expression_value.getError() != null) {
                        error(expression_value.getError());
                    }
                    else if (expression_value.getData() == null) {
                        error("Address expression evaluation failed");
                    }
                    else if (!expression_type.validate()) {
                        expression_type.wait(this);
                    }
                    else if (expression_type.getError() != null) {
                        error(expression_type.getError());
                    }
                    else {
                        IExpressions.Value value = expression_value.getData();
                        byte[] data = value.getValue();
                        if (data == null || data.length == 0) {
                            error("Address expression value is empty (void)");
                        }
                        else {
                            ISymbols.Symbol type = expression_type.getData();
                            boolean signed = type != null && type.getTypeClass() == ISymbols.TypeClass.integer;
                            done(TCFNumberFormat.toBigInteger(data, 0, data.length, value.isBigEndian(), signed));
                        }
                    }
                }
            }.getD();
        }

        public MemoryByte[] getBytesFromAddress(final BigInteger address, final long units) throws DebugException {
            return new TCFDebugTask<MemoryByte[]>(exec_ctx.getChannel()) {
                int offs = 0;
                public void run() {
                    if (exec_ctx.isDisposed()) {
                        error("Context is disposed");
                        return;
                    }
                    TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                    if (!cache.validate(this)) return;
                    if (cache.getError() != null) {
                        error(cache.getError());
                        return;
                    }
                    final IMemory.MemoryContext mem = cache.getData();
                    if (mem == null) {
                        error("Context does not provide memory access");
                        return;
                    }
                    final int size = (int)units;
                    final int mode = IMemory.MODE_CONTINUEONERROR | IMemory.MODE_VERIFY;
                    final byte[] buf = new byte[size];
                    final MemoryByte[] res = new MemoryByte[size];
                    mem.get(address, 1, buf, 0, size, mode, new IMemory.DoneMemory() {
                        public void doneMemory(IToken token, MemoryError error) {
                            int big_endian = 0;
                            if (mem.getProperties().get(IMemory.PROP_BIG_ENDIAN) != null) {
                                big_endian |= MemoryByte.ENDIANESS_KNOWN;
                                if (mem.isBigEndian()) big_endian |= MemoryByte.BIG_ENDIAN;
                            }
                            int cnt = 0;
                            while (offs < size) {
                                int flags = big_endian;
                                if (error instanceof IMemory.ErrorOffset) {
                                    IMemory.ErrorOffset ofs = (IMemory.ErrorOffset)error;
                                    int status = ofs.getStatus(cnt);
                                    if (status == IMemory.ErrorOffset.BYTE_VALID) {
                                        flags = MemoryByte.READABLE | MemoryByte.WRITABLE;
                                    }
                                    else if ((status & IMemory.ErrorOffset.BYTE_UNKNOWN) != 0) {
                                        if (cnt > 0) break;
                                    }
                                }
                                else if (error == null) {
                                    flags = MemoryByte.READABLE | MemoryByte.WRITABLE;
                                }
                                res[offs] = new MemoryByte(buf[offs], (byte)flags);
                                offs++;
                                cnt++;
                            }
                            if (offs < size) {
                                mem.get(address.add(BigInteger.valueOf(offs)), 1, buf, offs, size - offs, mode, this);
                            }
                            else {
                                done(res);
                            }
                        }
                    });
                }
            }.getD();
        }

        public MemoryByte[] getBytesFromOffset(BigInteger offset, long units) throws DebugException {
            return getBytesFromAddress(getBigBaseAddress().add(offset), units);
        }

        public String getExpression() {
            return expression;
        }

        public IMemoryBlockRetrieval getMemoryBlockRetrieval() {
            return TCFMemoryBlockRetrieval.this;
        }

        public long getStartAddress() {
            return 0; // Unbounded
        }

        public long getLength() {
            return length;
        }

        public BigInteger getMemoryBlockStartAddress() throws DebugException {
            return null; // Unbounded
        }

        public BigInteger getMemoryBlockEndAddress() throws DebugException {
            return null; // Unbounded
        }

        public BigInteger getBigLength() throws DebugException {
            return BigInteger.valueOf(length);
        }

        public void setBaseAddress(BigInteger address) throws DebugException {
        }

        public void setValue(BigInteger offset, final byte[] bytes) throws DebugException {
            final BigInteger address = getBigBaseAddress().add(offset);
            new TCFDebugTask<Object>(exec_ctx.getChannel()) {
                public void run() {
                    if (exec_ctx.isDisposed()) {
                        error("Context is disposed");
                        return;
                    }
                    TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                    if (!cache.validate(this)) return;
                    if (cache.getError() != null) {
                        error(cache.getError());
                        return;
                    }
                    final IMemory.MemoryContext mem = cache.getData();
                    if (mem == null) {
                        error("Context does not provide memory access");
                        return;
                    }
                    final int mode = IMemory.MODE_CONTINUEONERROR | IMemory.MODE_VERIFY;
                    mem.set(address, 1, bytes, 0, bytes.length, mode, new IMemory.DoneMemory() {
                        public void doneMemory(IToken token, MemoryError error) {
                            if (error != null) {
                                error(error);
                            }
                            else {
                                done(null);
                            }
                        }
                    });
                }
            }.getD();
        }

        public boolean supportBaseAddressModification() throws DebugException {
            return false;
        }

        public boolean supportsChangeManagement() {
            return false;
        }

        public byte[] getBytes() throws DebugException {
            return null;
        }

        public void setValue(long offset, byte[] bytes) throws DebugException {
            setValue(BigInteger.valueOf(offset), bytes);
        }

        public boolean supportsValueModification() {
            return true;
        }

        public IDebugTarget getDebugTarget() {
            return null;
        }

        public ILaunch getLaunch() {
            return exec_ctx.model.getLaunch();
        }

        public String getModelIdentifier() {
            return ITCFConstants.ID_TCF_DEBUG_MODEL;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Object getAdapter(Class adapter) {
            if (adapter == IMemoryBlockRetrieval.class) return TCFMemoryBlockRetrieval.this;
            if (adapter == IMemoryBlockRetrievalExtension.class) return TCFMemoryBlockRetrieval.this;
            return super.getAdapter(adapter);
        }
    }

    TCFMemoryBlockRetrieval(TCFNodeExecContext exec_ctx) {
        this.exec_ctx = exec_ctx;
    }

    public IMemoryBlockExtension getExtendedMemoryBlock(final String expression, Object context) throws DebugException {
        return new TCFDebugTask<IMemoryBlockExtension>() {
            public void run() {
                done(new MemoryBlock(expression, -1));
            }
        }.getD();
    }

    public IMemoryBlock getMemoryBlock(final long address, final long length) throws DebugException {
        return new TCFDebugTask<IMemoryBlockExtension>() {
            public void run() {
                done(new MemoryBlock("0x" + Long.toHexString(address), length));
            }
        }.getD();
    }

    public boolean supportsStorageRetrieval() {
        return true;
    }

    void onMemoryChanged() {
        if (mem_blocks.size() == 0) return;
        ArrayList<DebugEvent> list = new ArrayList<DebugEvent>();
        for (MemoryBlock b : mem_blocks) {
            list.add(new DebugEvent(b, DebugEvent.CHANGE, DebugEvent.CONTENT));
        }
        DebugPlugin.getDefault().fireDebugEventSet(list.toArray(new DebugEvent[list.size()]));
    }
}
