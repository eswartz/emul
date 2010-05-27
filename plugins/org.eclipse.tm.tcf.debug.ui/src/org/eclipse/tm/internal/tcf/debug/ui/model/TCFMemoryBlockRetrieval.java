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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
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

    private class MemoryBlock extends PlatformObject implements IMemoryBlockExtension {

        private final String expression;
        private final Set<Object> connections = new HashSet<Object>();
        private final TCFDataCache<IExpressions.Expression> remote_expression;
        private final TCFDataCache<IExpressions.Value> expression_value;
        private final TCFDataCache<ISymbols.Symbol> expression_type;

        private boolean disposed;

        MemoryBlock(final String expression) {
            this.expression = expression;
            final TCFLaunch launch = exec_ctx.getModel().getLaunch();
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
                    TCFDataCache<ISymbols.Symbol> type_cache = exec_ctx.getModel().getSymbolInfoCache(val.getTypeID());
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
            new TCFDebugTask<Boolean>() {
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
                    done(Boolean.TRUE);
                }
            }.getD();
        }

        public int getAddressSize() throws DebugException {
            return new TCFDebugTask<Integer>() {
                public void run() {
                    if (exec_ctx.isDisposed()) {
                        error("Context is disposed");
                    }
                    else  {
                        TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                        if (!cache.validate(this)) return;
                        IMemory.MemoryContext mem = cache.getData();
                        if (mem == null) {
                            error("Context does not provide memory access");
                        }
                        else {
                            done(mem.getAddressSize());
                        }
                    }
                }
            }.getD();
        }

        public int getAddressableSize() throws DebugException {
            // TODO: support for addressable size other then 1 byte
            return 1;
        }

        private BigInteger toBigInteger(byte[] data, boolean big_endian, boolean sign_extension) {
            byte[] temp = null;
            if (sign_extension) {
                temp = new byte[data.length];
            }
            else {
                temp = new byte[data.length + 1];
                temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
            }
            if (big_endian) {
                System.arraycopy(data, 0, temp, sign_extension ? 0 : 1, data.length);
            }
            else {
                for (int i = 0; i < data.length; i++) {
                    temp[temp.length - i - 1] = data[i];
                }
            }
            return new BigInteger(temp);
        }

        public BigInteger getBigBaseAddress() throws DebugException {
            return new TCFDebugTask<BigInteger>() {
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
                            done(toBigInteger(data, value.isBigEndian(), signed));
                        }
                    }
                }
            }.getD();
        }

        public MemoryByte[] getBytesFromAddress(final BigInteger address, final long units) throws DebugException {
            return new TCFDebugTask<MemoryByte[]>() {
                public void run() {
                    if (exec_ctx.isDisposed()) {
                        error("Context is disposed");
                    }
                    else {
                        TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                        if (!cache.validate(this)) return;
                        final IMemory.MemoryContext mem = cache.getData();
                        if (mem == null) {
                            error("Context does not provide memory access");
                        }
                        else {
                            int size = (int)units;
                            int mode = IMemory.MODE_CONTINUEONERROR | IMemory.MODE_VERIFY;
                            final byte[] buf = new byte[size];
                            mem.get(address, 1, buf, 0, size, mode, new IMemory.DoneMemory() {
                                public void doneMemory(IToken token, MemoryError error) {
                                    MemoryByte[] res = new MemoryByte[buf.length];
                                    int big_endian = 0;
                                    if (mem.getProperties().get(IMemory.PROP_BIG_ENDIAN) != null) {
                                        big_endian |= MemoryByte.ENDIANESS_KNOWN;
                                        if (mem.isBigEndian()) big_endian |= MemoryByte.BIG_ENDIAN;
                                    }
                                    for (int i = 0; i < buf.length; i++) {
                                        int flags = big_endian;
                                        if (error instanceof IMemory.ErrorOffset) {
                                            IMemory.ErrorOffset ofs = (IMemory.ErrorOffset)error;
                                            if (ofs.getStatus(i) == IMemory.ErrorOffset.BYTE_VALID) {
                                                flags = MemoryByte.READABLE | MemoryByte.WRITABLE;
                                            }
                                        }
                                        else if (error == null) {
                                            flags = MemoryByte.READABLE | MemoryByte.WRITABLE;
                                        }
                                        res[i] = new MemoryByte(buf[i], (byte)flags);
                                    }
                                    done(res);
                                }
                            });
                        }
                    }
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
            return -1; // Unbounded
        }

        public BigInteger getMemoryBlockStartAddress() throws DebugException {
            return null; // Unbounded
        }

        public BigInteger getMemoryBlockEndAddress() throws DebugException {
            return null; // Unbounded
        }

        public BigInteger getBigLength() throws DebugException {
            return BigInteger.valueOf(-1); // Unbounded
        }

        public void setBaseAddress(BigInteger address) throws DebugException {
        }

        public void setValue(BigInteger offset, byte[] bytes) throws DebugException {
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
        }

        public boolean supportsValueModification() {
            return false;
        }

        public IDebugTarget getDebugTarget() {
            return null;
        }

        public ILaunch getLaunch() {
            return exec_ctx.getModel().getLaunch();
        }

        public String getModelIdentifier() {
            return ITCFConstants.ID_TCF_DEBUG_MODEL;
        }

        @SuppressWarnings("unchecked")
        @Override
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
                done(new MemoryBlock(expression));
            }
        }.getD();
    }

    public IMemoryBlock getMemoryBlock(long address, long length) throws DebugException {
        return getExtendedMemoryBlock("0x" + Long.toHexString(address), exec_ctx);
    }

    public boolean supportsStorageRetrieval() {
        return true;
    }
}
