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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.AbstractDMEvent;
import org.eclipse.dd.dsf.datamodel.CompositeDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.tm.internal.tcf.dsf.Activator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters.DoneGet;
import org.eclipse.tm.tcf.services.IRegisters.DoneSearch;
import org.eclipse.tm.tcf.services.IRegisters.DoneSet;
import org.eclipse.tm.tcf.services.IRegisters.NamedValue;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.osgi.framework.BundleContext;


public class TCFDSFRegisters extends AbstractDsfService implements org.eclipse.dd.dsf.debug.service.IRegisters {
    
    private class ObjectDMC extends AbstractDMContext implements IFormattedDataDMContext {

        final String id;
        final RegisterChildrenCache children;
        final Map<String,ValueDMC> values;
        
        org.eclipse.tm.tcf.services.IRegisters.RegistersContext context;  
        boolean disposed;

        ObjectDMC(String session_id, IDMContext[] parents, String id) {
            super(session_id, parents);
            this.id = id;
            children = new RegisterChildrenCache(channel, id, new IDMContext[]{ this });
            values = new HashMap<String,ValueDMC>();
            model.put(id, this);
        }

        ObjectDMC(String session_id, IDMContext[] parents, String id, RegisterChildrenCache children) {
            super(session_id, parents);
            this.id = id;
            this.children = children;
            values = new HashMap<String,ValueDMC>();
        }

        @Override
        public boolean equals(Object other) {
            return super.baseEquals(other) && ((ObjectDMC)other).id.equals(id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        public String getName() {
            return context.getName();
        }
        
        void dispose() {
            assert !disposed;
            children.dispose();
            for (ValueDMC v : values.values()) v.dispose();
            values.clear();
            model.remove(id);
            disposed = true;
        }
    }
    
    private class RegisterGroupDMC extends ObjectDMC implements IRegisterGroupDMContext {
        
        RegisterGroupDMC(String session_id, IDMContext[] parents, String id) {
            super(session_id, parents, id);
        }
        
        /* Constructor for a fake register group - DSF requires at least one group object */
        RegisterGroupDMC(String session_id, IDMContext[] parents, final String id, RegisterChildrenCache children) {
            super(session_id, parents, id, children);
            context = new org.eclipse.tm.tcf.services.IRegisters.RegistersContext() {
                public int[] getBitNumbers() {
                    return null;
                }
                public String getDescription() {
                    return null;
                }
                public int getFirstBitNumber() {
                    return 0;
                }
                public String getID() {
                    return id;
                }
                public String getName() {
                    return null;
                }
                public NamedValue[] getNamedValues() {
                    return null;
                }
                public int getSize() {
                    return 0;
                }
                public String getParentID() {
                    return null;
                }
                public Map<String,Object> getProperties() {
                    return null;
                }
                public boolean hasSideEffects() {
                    return false;
                }
                public boolean isBigEndian() {
                    return false;
                }
                public boolean isFloat() {
                    return false;
                }
                public boolean isLeftToRight() {
                    return false;
                }
                public boolean isReadOnce() {
                    return false;
                }
                public boolean isReadable() {
                    return false;
                }
                public boolean isVolatile() {
                    return false;
                }
                public boolean isWriteOnce() {
                    return false;
                }
                public boolean isWriteable() {
                    return false;
                }
                public Collection<String> canSearch() {
                    return null;
                }
                public Number getMemoryAddress() {
                    return null;
                }
                public String getMemoryContext() {
                    return null;
                }
                public String getProcessID() {
                    return null;
                }
                public String getRole() {
                    return null;
                }
                public IToken get(DoneGet done) {
                    throw new Error();
                }
                public IToken set(byte[] value, DoneSet done) {
                    throw new Error();
                }
                public IToken search(Map<String, Object> filter, DoneSearch done) {
                    throw new Error();
                }
            };
        }
    }
    
    private class RegisterDMC extends ObjectDMC implements IRegisterDMContext {
        
        RegisterDMC(String session_id, IDMContext[] parents, String id) {
            super(session_id, parents, id);
        }
    }
    
    private class BitFieldDMC extends ObjectDMC implements IBitFieldDMContext {

        BitFieldDMC(String session_id, IDMContext[] parents, String id) {
            super(session_id, parents, id);
        }
    }
    
    private class ValueDMC extends FormattedValueDMContext {
        
        final RegisterValueCache cache;
        
        boolean disposed;

        ValueDMC(ObjectDMC parent, String fmt) {
            super(TCFDSFRegisters.this, parent, fmt);
            cache = new RegisterValueCache(channel, parent.context, fmt);
        }
        
        void dispose() {
            assert !disposed;
            cache.dispose();
            disposed = true;
        }
    }
    
    private class RegisterGroupData implements IRegisterGroupDMData {
        
        final org.eclipse.tm.tcf.services.IRegisters.RegistersContext context;
        
        RegisterGroupData(org.eclipse.tm.tcf.services.IRegisters.RegistersContext context) {
            this.context = context;
        }

        public String getDescription() {
            return context.getDescription();
        }

        public String getName() {
            return context.getName();
        }
    }
    
    private class RegisterData implements IRegisterDMData {

        final org.eclipse.tm.tcf.services.IRegisters.RegistersContext context;
        
        RegisterData(org.eclipse.tm.tcf.services.IRegisters.RegistersContext context) {
            this.context = context;
        }

        public String getDescription() {
            return context.getDescription();
        }

        public String getName() {
            return context.getName();
        }

        public boolean hasSideEffects() {
            return context.hasSideEffects();
        }

        public boolean isFloat() {
            return context.isFloat();
        }

        public boolean isReadOnce() {
            return context.isReadOnce();
        }

        public boolean isReadable() {
            return context.isReadable();
        }

        public boolean isVolatile() {
            return context.isVolatile();
        }

        public boolean isWriteOnce() {
            return context.isWriteOnce();
        }

        public boolean isWriteable() {
            return context.isWriteable();
        }
    }
    
    private class BitFieldData implements IBitFieldDMData {

        final org.eclipse.tm.tcf.services.IRegisters.RegistersContext context;
        
        IMnemonic[] mnemonics;
        IBitGroup[] bit_groups;
        
        BitFieldData(org.eclipse.tm.tcf.services.IRegisters.RegistersContext context) {
            this.context = context;
        }

        public IBitGroup[] getBitGroup() {
            if (bit_groups == null) {
                int[] arr = context.getBitNumbers();
                if (arr == null) {
                    bit_groups = new IBitGroup[0];
                }
                else {
                    Arrays.sort(arr);
                    ArrayList<IBitGroup> l = new ArrayList<IBitGroup>();
                    int i = 0;
                    while (i < arr.length) {
                        int j = i;
                        while (j + 1 < arr.length && arr[j + 1] == arr[j] + 1) j++;
                        final int i0 = i;
                        final int i1 = j;
                        l.add(new IBitGroup() {
                            public int bitCount() {
                                return i1 - i0 + 1;
                            }
                            public int startBit() {
                                return i0;
                            }
                        });
                        i = j + 1;
                    }
                    bit_groups = l.toArray(new IBitGroup[l.size()]);
                }
            }
            return bit_groups;
        }

        public IMnemonic getCurrentMnemonicValue() {
            // TODO getCurrentMnemonicValue() should be async
            return null;
        }

        public String getDescription() {
            return context.getDescription();
        }

        public IMnemonic[] getMnemonics() {
            if (mnemonics == null) {
                NamedValue[] arr = context.getNamedValues();
                if (arr == null) {
                    mnemonics = new IMnemonic[0];
                }
                else {
                    int cnt = 0;
                    mnemonics = new IMnemonic[arr.length];
                    for (final NamedValue v : arr) {
                        mnemonics[cnt++] = new IMnemonic() {
                            public String getLongName() {
                                return v.getDescription();
                            }
                            public String getShortName() {
                                return v.getName();
                            }
                        };
                    }
                }
            }
            return mnemonics;
        }

        public String getName() {
            return context.getName();
        }

        public boolean hasSideEffects() {
            return context.hasSideEffects();
        }

        public boolean isReadOnce() {
            return context.isReadOnce();
        }

        public boolean isReadable() {
            return context.isReadable();
        }

        public boolean isWriteOnce() {
            return context.isWriteOnce();
        }

        public boolean isWriteable() {
            return context.isWriteable();
        }

        public boolean isZeroBasedNumbering() {
            return context.getFirstBitNumber() == 0;
        }

        public boolean isZeroBitLeftMost() {
            return context.isLeftToRight();
        }
    }
    
    private class RegisterChildrenCache extends TCFDataCache<Map<String,ObjectDMC>> {
        
        final String id;
        final IDMContext[] parents;
        
        Map<String,ObjectDMC> dmc_pool = new HashMap<String,ObjectDMC>();;
        boolean disposed;
        
        public RegisterChildrenCache(IChannel channel, String id, IDMContext[] parents) {
            super(channel);
            this.id = id;
            this.parents = parents;
        }

        void invalidateRegContents() {
            for (ObjectDMC dmc : dmc_pool.values()) {
                for (ValueDMC val : dmc.values.values()) val.cache.reset();
                dmc.children.invalidateRegContents();
            }
        }
        
        void dispose() {
            assert !disposed;
            reset();
            for (ObjectDMC dmc : dmc_pool.values()) dmc.dispose();
            dmc_pool.clear();
            disposed = true;
        }

        @Override
        public boolean startDataRetrieval() {
            assert command == null;
            assert !disposed;
            if (tcf_reg_service == null) {
                reset(null);
                return true;
            }
            command = tcf_reg_service.getChildren(id, new org.eclipse.tm.tcf.services.IRegisters.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception err, String[] contexts) {
                    if (command != token) return;
                    final LinkedHashMap<String,ObjectDMC> data = new LinkedHashMap<String,ObjectDMC>();
                    if (err != null || contexts == null || contexts.length == 0) {
                        set(token, err, data);
                        return;
                    }
                    // TODO DSF service design does not support lazy retrieval of context attributes (because getName() is not async)
                    final Set<IToken> cmds = new HashSet<IToken>();
                    final IToken cb = new IToken() {
                        public boolean cancel() {
                            for (IToken x : cmds) x.cancel();
                            return false;
                        }
                    };
                    command = cb;
                    org.eclipse.tm.tcf.services.IRegisters.DoneGetContext done = new org.eclipse.tm.tcf.services.IRegisters.DoneGetContext() {
                        public void doneGetContext(IToken token, Exception err,
                                org.eclipse.tm.tcf.services.IRegisters.RegistersContext context) {
                            cmds.remove(token);
                            if (command != cb) return;
                            if (err != null) {
                                command.cancel();
                                set(cb, err, data);
                                return;
                            }
                            String id = context.getID();
                            ObjectDMC dmc = model.get(id);
                            if (dmc == null) {
                                if (context.getBitNumbers() != null) {
                                    dmc = new BitFieldDMC(getSession().getId(), parents, id);
                                }
                                else if (context.isReadable() || context.isWriteable()) {
                                    dmc = new RegisterDMC(getSession().getId(), parents, id);
                                }
                                else {
                                    dmc = new RegisterGroupDMC(getSession().getId(), parents, id);
                                }
                            }
                            dmc_pool.put(id, dmc);
                            dmc.context = context;
                            data.put(id, dmc);
                            if (cmds.isEmpty()) set(cb, null, data);
                        }
                    };
                    for (String id : contexts) cmds.add(tcf_reg_service.getContext(id, done));
                }
            });
            return false;
        }
    }
    
    private class RegisterValueCache extends TCFDataCache<FormattedValueDMData> {
        
        final org.eclipse.tm.tcf.services.IRegisters.RegistersContext context;
        final String fmt;
        
        boolean disposed;

        public RegisterValueCache(IChannel channel,
                org.eclipse.tm.tcf.services.IRegisters.RegistersContext context, String fmt) {
            super(channel);
            this.context = context;
            this.fmt = fmt;
        }

        @Override
        public boolean startDataRetrieval() {
            assert command == null;
            assert tcf_reg_service != null;
            assert context != null;
            assert !disposed;
            command = context.get(new org.eclipse.tm.tcf.services.IRegisters.DoneGet() {
                public void doneGet(IToken token, Exception err, byte[] value) {
                    if (command != token) return;
                    FormattedValueDMData data = null;
                    if (value != null) {
                        int radix = 10;
                        if (fmt.equals(HEX_FORMAT)) radix = 16; 
                        else if (fmt.equals(OCTAL_FORMAT)) radix = 8; 
                        byte[] temp = new byte[value.length + 1];
                        temp[0] = 0; // Extra byte to avoid sign extension by BigInteger
                        if (context.isBigEndian()) {
                            System.arraycopy(value, 0, temp, 1, value.length);
                        }
                        else {
                            for (int i = 0; i < value.length; i++) {
                                temp[temp.length - i - 1] = value[i];
                            }
                        }
                        String s = new BigInteger(temp).toString(radix);
                        switch (radix) {
                        case 8:
                            if (!s.startsWith("0")) s = "0" + s;
                            break;
                        case 16:
                            int l = value.length * 2 - s.length();
                            if (l < 0) l = 0;
                            if (l > 16) l = 16;
                            s = "0000000000000000".substring(0, l) + s;
                            break;
                        }
                        data = new FormattedValueDMData(s);
                    }
                    set(token, err, data);
                }
            });
            return false;
        }
        
        void dispose() {
            assert !disposed;
            reset();
            disposed = true;
        }
    }
    
    private static class RegisterGroupChangedEvent extends AbstractDMEvent<IRegisterGroupDMContext>
            implements IGroupChangedDMEvent {

        public RegisterGroupChangedEvent(IRegisterGroupDMContext context) {
            super(context);
        }
    }
    
    private static class RegisterChangedEvent extends AbstractDMEvent<IRegisterDMContext>
            implements IRegisterChangedDMEvent {

        public RegisterChangedEvent(IRegisterDMContext context) {
            super(context);
        }
    }

    private static class BitFieldChangedEvent extends AbstractDMEvent<IBitFieldDMContext>
            implements IBitFieldChangedDMEvent {
        
        public BitFieldChangedEvent(IBitFieldDMContext context) {
            super(context);
        }
    }
    
    private static class GroupsChangedEvent extends AbstractDMEvent<IDMContext> implements IGroupsChangedDMEvent {

        public GroupsChangedEvent(IExecutionDMContext context) {
            super(context);
        }
    }

    private final org.eclipse.tm.tcf.services.IRegisters.RegistersListener listener =
        new org.eclipse.tm.tcf.services.IRegisters.RegistersListener() {

            public void contextChanged() {
                TCFDSFRunControl rc = getServicesTracker().getService(TCFDSFRunControl.class);
                for (TCFDSFExecutionDMC dmc : rc.getCachedContexts()) {
                    RegisterChildrenCache c = (RegisterChildrenCache)dmc.registers_cache;
                    if (c != null) {
                        c.dispose();
                        dmc.registers_cache = null;
                        getSession().dispatchEvent(new GroupsChangedEvent(dmc), getProperties());
                    }
                }
            }

            public void registerChanged(String id) {
                ObjectDMC dmc = model.get(id);
                if (dmc != null) {
                    for (ValueDMC val : dmc.values.values()) val.cache.reset();
                    dmc.children.invalidateRegContents();
                    if (dmc instanceof RegisterGroupDMC) {
                        getSession().dispatchEvent(new RegisterGroupChangedEvent((RegisterGroupDMC)dmc), getProperties());
                    }
                    else if (dmc instanceof RegisterDMC) {
                        getSession().dispatchEvent(new RegisterChangedEvent((RegisterDMC)dmc), getProperties());
                    }
                    else if (dmc instanceof BitFieldDMC) {
                        getSession().dispatchEvent(new BitFieldChangedEvent((BitFieldDMC)dmc), getProperties());
                    }
                }
            }
    };
    
    private final IChannel channel;
    private final org.eclipse.tm.tcf.services.IRegisters tcf_reg_service;
    private final Map<String,ObjectDMC> model;
    
    private final String[] available_formats = {
            HEX_FORMAT,
            DECIMAL_FORMAT,
            OCTAL_FORMAT
    };

    public TCFDSFRegisters(DsfSession session, IChannel channel, final RequestMonitor monitor) {
        super(session);
        this.channel = channel;
        model = new HashMap<String,ObjectDMC>();
        tcf_reg_service = channel.getRemoteService(org.eclipse.tm.tcf.services.IRegisters.class);
        if (tcf_reg_service != null) tcf_reg_service.addListener(listener);
        initialize(new RequestMonitor(getExecutor(), monitor) { 
            @Override
            protected void handleSuccess() {
                String[] class_names = {
                        org.eclipse.dd.dsf.debug.service.IRegisters.class.getName(),
                        TCFDSFRegisters.class.getName()
                };
                register(class_names, new Hashtable<String,String>());
                getSession().addServiceEventListener(TCFDSFRegisters.this, null);
                monitor.done();
            }
        });
    }

    @Override 
    public void shutdown(RequestMonitor monitor) {
        getSession().removeServiceEventListener(this);
        unregister();
        super.shutdown(monitor);
    }

    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    public void getRegisterGroupData(IRegisterGroupDMContext dmc, DataRequestMonitor<IRegisterGroupDMData> rm) {
        if (tcf_reg_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Registers service is not available", null)); //$NON-NLS-1$
        }
        else if (dmc instanceof RegisterGroupDMC) {
            if (((ObjectDMC)dmc).disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
            }
            else {
                rm.setData(new RegisterGroupData(((RegisterGroupDMC)dmc).context));
            }
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        rm.done();
    }

    public void getRegisterData(IRegisterDMContext dmc, DataRequestMonitor<IRegisterDMData> rm) {
        if (tcf_reg_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Registers service is not available", null)); //$NON-NLS-1$
        }
        else if (dmc instanceof RegisterDMC) {
            if (((ObjectDMC)dmc).disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
            }
            else {
                rm.setData(new RegisterData(((RegisterDMC)dmc).context));
            }
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        rm.done();
    }

    public void getBitFieldData(IBitFieldDMContext dmc, DataRequestMonitor<IBitFieldDMData> rm) {
        if (tcf_reg_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Registers service is not available", null)); //$NON-NLS-1$
        }
        else if (dmc instanceof BitFieldDMC) {
            if (((ObjectDMC)dmc).disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
            }
            else {
                rm.setData(new BitFieldData(((BitFieldDMC)dmc).context));
            }
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        rm.done();
    }
    
    private RegisterChildrenCache getRegisterChildrenCache(IDMContext dmc, DataRequestMonitor<?> rm) {
        RegisterChildrenCache cache = null;
        if (dmc instanceof CompositeDMContext) {
            for (IDMContext ctx : dmc.getParents()) {
                if (ctx instanceof TCFDSFExecutionDMC || ctx instanceof TCFDSFStack.TCFFrameDMC || 
                        ctx instanceof RegisterGroupDMC || ctx instanceof RegisterDMC) {
                    dmc = ctx;
                    break;
                }
            }
        }
        if (tcf_reg_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Registers service is not available", null)); //$NON-NLS-1$
        }
        else if (dmc instanceof TCFDSFExecutionDMC) {
            TCFDSFExecutionDMC exe = (TCFDSFExecutionDMC)dmc;
            if (exe.registers_cache == null) exe.registers_cache =
                new RegisterChildrenCache(channel, exe.getTcfContextId(), new IDMContext[]{ exe });
            cache = (RegisterChildrenCache)exe.registers_cache;
        }
        else if (dmc instanceof ObjectDMC) {
            if (((ObjectDMC)dmc).disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
            }
            else {
                cache = ((ObjectDMC)dmc).children;
            }
        }
        else if (dmc instanceof TCFDSFStack.TCFFrameDMC && ((TCFDSFStack.TCFFrameDMC)dmc).level == 0) {
            TCFDSFExecutionDMC exe = ((TCFDSFStack.TCFFrameDMC)dmc).exe_dmc;
            if (exe.registers_cache == null) exe.registers_cache =
                new RegisterChildrenCache(channel, exe.getTcfContextId(), new IDMContext[]{ exe });
            cache = (RegisterChildrenCache)exe.registers_cache;
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        return cache;
    }

    public void getRegisterGroups(final IDMContext dmc, final DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
        if (rm.isCanceled()) return;
        RegisterChildrenCache cache = getRegisterChildrenCache(dmc, rm);
        if (cache != null) {
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        getRegisterGroups(dmc, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            Map<String,ObjectDMC> c = cache.getData();
            int cnt = 0;
            for (IDMContext x : c.values()) {
                if (x instanceof RegisterGroupDMC) cnt++;
            }
            RegisterGroupDMC[] arr = new RegisterGroupDMC[cnt];
            cnt = 0;
            for (IDMContext x : c.values()) {
                if (x instanceof RegisterGroupDMC) arr[cnt++] = (RegisterGroupDMC)x;
            }
            rm.setData(arr);
        }
        rm.done();
    }

    public void getRegisterSubGroups(IDMContext dmc, DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
        getRegisterGroups(dmc, rm);
    }

    public void getRegisters(final IDMContext dmc, final DataRequestMonitor<IRegisterDMContext[]> rm) {
        if (rm.isCanceled()) return;
        RegisterChildrenCache cache = getRegisterChildrenCache(dmc, rm);
        if (cache != null) {
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        getRegisters(dmc, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            Map<String,ObjectDMC> c = cache.getData();
            int cnt = 0;
            for (IDMContext x : c.values()) {
                if (x instanceof RegisterDMC) cnt++;
            }
            RegisterDMC[] arr = new RegisterDMC[cnt];
            cnt = 0;
            for (IDMContext x : c.values()) {
                if (x instanceof RegisterDMC) arr[cnt++] = (RegisterDMC)x;
            }
            rm.setData(arr);
        }
        rm.done();
    }

    public void getBitFields(final IDMContext dmc, final DataRequestMonitor<IBitFieldDMContext[]> rm) {
        if (rm.isCanceled()) return;
        RegisterChildrenCache cache = getRegisterChildrenCache(dmc, rm);
        if (cache != null) {
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        getBitFields(dmc, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            Map<String,ObjectDMC> c = cache.getData();
            int cnt = 0;
            for (IDMContext x : c.values()) {
                if (x instanceof BitFieldDMC) cnt++;
            }
            BitFieldDMC[] arr = new BitFieldDMC[cnt];
            cnt = 0;
            for (IDMContext x : c.values()) {
                if (x instanceof BitFieldDMC) arr[cnt++] = (BitFieldDMC)x;
            }
            rm.setData(arr);
        }
        rm.done();
    }

    public void findBitField(final IDMContext dmc, final String name, final DataRequestMonitor<IBitFieldDMContext> rm) {
        if (rm.isCanceled()) return;
        RegisterChildrenCache cache = getRegisterChildrenCache(dmc, rm);
        if (cache != null) {
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        findBitField(dmc, name, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            Map<String,ObjectDMC> c = cache.getData();
            BitFieldDMC res = null;
            for (IDMContext x : c.values()) {
                if (x instanceof BitFieldDMC) {
                    if (((BitFieldDMC)x).getName().equals(name)) {
                        res = (BitFieldDMC)x;
                        break;
                    }
                }
            }
            if (res != null) rm.setData(res);
            else rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    REQUEST_FAILED, "Not found", null)); //$NON-NLS-1$
        }
        rm.done();
    }

    public void findRegister(final IDMContext dmc, final String name, final DataRequestMonitor<IRegisterDMContext> rm) {
        if (rm.isCanceled()) return;
        RegisterChildrenCache cache = getRegisterChildrenCache(dmc, rm);
        if (cache != null) {
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        findRegister(dmc, name, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            Map<String,ObjectDMC> c = cache.getData();
            RegisterDMC res = null;
            for (IDMContext x : c.values()) {
                if (x instanceof RegisterDMC) {
                    if (((RegisterDMC)x).getName().equals(name)) {
                        res = (RegisterDMC)x;
                        break;
                    }
                }
            }
            if (res != null) rm.setData(res);
            else rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    REQUEST_FAILED, "Not found", null)); //$NON-NLS-1$
        }
        rm.done();
    }

    public void findRegisterGroup(final IDMContext dmc, final String name, final DataRequestMonitor<IRegisterGroupDMContext> rm) {
        if (rm.isCanceled()) return;
        RegisterChildrenCache cache = getRegisterChildrenCache(dmc, rm);
        if (cache != null) {
            if (!cache.validate()) {
                cache.wait(new Runnable() {
                    public void run() {
                        findRegisterGroup(dmc, name, rm);
                    }
                });
                return;
            }
            if (cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            Map<String,ObjectDMC> c = cache.getData();
            RegisterGroupDMC res = null;
            for (IDMContext x : c.values()) {
                if (x instanceof RegisterGroupDMC) {
                    if (((RegisterGroupDMC)x).getName().equals(name)) {
                        res = (RegisterGroupDMC)x;
                        break;
                    }
                }
            }
            if (res != null) rm.setData(res);
            else rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    REQUEST_FAILED, "Not found", null)); //$NON-NLS-1$
        }
        rm.done();
    }

    public void writeBitField(IBitFieldDMContext dmc, String val, String fmt, final RequestMonitor rm) {
        if (tcf_reg_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Registers service is not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (dmc instanceof ObjectDMC) {
            if (((ObjectDMC)dmc).disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            int radix = 10;
            if (fmt.equals(HEX_FORMAT)) radix = 16; 
            else if (fmt.equals(OCTAL_FORMAT)) radix = 8; 
            byte[] data = new BigInteger(val, radix).toByteArray();
            if (!((ObjectDMC)dmc).context.isBigEndian()) {
                byte[] temp = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    temp[temp.length - i - 1] = data[i];
                }
                data = temp;
            }
            ((ObjectDMC)dmc).context.set(data, new org.eclipse.tm.tcf.services.IRegisters.DoneSet() {
                public void doneSet(IToken token, Exception error) {
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

    public void writeBitField(IBitFieldDMContext dmc, IMnemonic mnemonic, final RequestMonitor rm) {
        if (tcf_reg_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Registers service is not available", null)); //$NON-NLS-1$
        }
        else if (dmc instanceof ObjectDMC) {
            if (((ObjectDMC)dmc).disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            NamedValue[] arr = ((ObjectDMC)dmc).context.getNamedValues();
            if (arr != null) {
                for (NamedValue nv : arr) {
                    if (nv.getName().equals(mnemonic.getShortName())) {
                        byte[] val = nv.getValue();
                        ((ObjectDMC)dmc).context.set(val, new org.eclipse.tm.tcf.services.IRegisters.DoneSet() {
                            public void doneSet(IToken token, Exception error) {
                                if (rm.isCanceled()) return;
                                if (error != null) {
                                    rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                            REQUEST_FAILED, "Command error", error)); //$NON-NLS-1$
                                }
                                rm.done();
                            }
                        });
                        return;
                    }
                }
            }
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown mnemonic", null)); //$NON-NLS-1$
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        rm.done();
    }

    public void writeRegister(IRegisterDMContext dmc, String val, String fmt, final RequestMonitor rm) {
        if (tcf_reg_service == null) {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Registers service is not available", null)); //$NON-NLS-1$
            rm.done();
        }
        else if (dmc instanceof ObjectDMC) {
            if (((ObjectDMC)dmc).disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            int radix = 10;
            if (fmt.equals(HEX_FORMAT)) radix = 16; 
            else if (fmt.equals(OCTAL_FORMAT)) radix = 8; 
            byte[] data = new BigInteger(val, radix).toByteArray();
            if (!((ObjectDMC)dmc).context.isBigEndian()) {
                byte[] temp = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    temp[temp.length - i - 1] = data[i];
                }
                data = temp;
            }
            ((ObjectDMC)dmc).context.set(data, new org.eclipse.tm.tcf.services.IRegisters.DoneSet() {
                public void doneSet(IToken token, Exception error) {
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

    public void getAvailableFormats(IFormattedDataDMContext dmc, DataRequestMonitor<String[]> rm) {
        rm.setData(available_formats);
        rm.done();
    }

    public void getFormattedExpressionValue(final FormattedValueDMContext dmc,
            final DataRequestMonitor<FormattedValueDMData> rm) {
        if (rm.isCanceled()) return;
        if (dmc instanceof ValueDMC) {
            ValueDMC vmc = (ValueDMC)dmc;
            if (vmc.disposed) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        INVALID_HANDLE, "Disposed DMC", null)); //$NON-NLS-1$
                rm.done();
                return;
            }
            if (!vmc.cache.validate()) {
                vmc.cache.wait(new Runnable() {
                    public void run() {
                        getFormattedExpressionValue(dmc, rm);
                    }
                });
                return;
            }
            if (vmc.cache.getError() != null) {
                rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        REQUEST_FAILED, "Data error", vmc.cache.getError())); //$NON-NLS-1$
                rm.done();
                return;
            }
            rm.setData(vmc.cache.getData());
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null)); //$NON-NLS-1$
        }
        rm.done();
    }

    public FormattedValueDMContext getFormattedValueContext(IFormattedDataDMContext dmc, String fmt) {
        if (dmc instanceof ObjectDMC) {
            ObjectDMC omc = (ObjectDMC)dmc;
            ValueDMC res = omc.values.get(fmt);
            if (res == null) {
                omc.values.put(fmt, res = new ValueDMC(omc, fmt));
            }
            return res;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void getModelData(IDMContext dmc, DataRequestMonitor<?> rm) {
        if (dmc instanceof RegisterGroupDMC) {
            getRegisterGroupData((RegisterGroupDMC)dmc, (DataRequestMonitor<IRegisterGroupDMData>)rm);
        }
        else if (dmc instanceof RegisterDMC) {
            getRegisterData((RegisterDMC)dmc, (DataRequestMonitor<IRegisterDMData>)rm);
        }
        else if (dmc instanceof BitFieldDMC) {
            getBitFieldData((BitFieldDMC)dmc, (DataRequestMonitor<IBitFieldDMData>)rm);
        }
        else if (dmc instanceof FormattedValueDMContext) {
            getFormattedExpressionValue((FormattedValueDMContext)dmc, (DataRequestMonitor<FormattedValueDMData>)rm);
        }
        else {
            rm.setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    INVALID_HANDLE, "Unknown DMC type", null));  //$NON-NLS-1$
            rm.done();
        }
    }

    @DsfServiceEventHandler
    public void eventDispatched(org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent e) {
        if (e.getReason() != StateChangeReason.STEP) {
            RegisterChildrenCache cache = (RegisterChildrenCache)((TCFDSFExecutionDMC)e.getDMContext()).registers_cache;
            if (cache != null) cache.invalidateRegContents();
        }
    }
    
    @DsfServiceEventHandler
    public void eventDispatched(org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent e) {
        RegisterChildrenCache cache = (RegisterChildrenCache)((TCFDSFExecutionDMC)e.getDMContext()).registers_cache;
        if (cache != null) cache.invalidateRegContents();
    }

    @DsfServiceEventHandler
    public void eventDispatched(org.eclipse.dd.dsf.debug.service.IRunControl.IExitedDMEvent e) {
        RegisterChildrenCache cache = (RegisterChildrenCache)((TCFDSFExecutionDMC)e.getDMContext()).registers_cache;
        if (cache != null) cache.dispose();
    }
}
