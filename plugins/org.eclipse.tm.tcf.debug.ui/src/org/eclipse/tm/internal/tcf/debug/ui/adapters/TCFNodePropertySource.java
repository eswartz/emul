/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.adapters;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext.MemoryRegion;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Adapts TCFNode to IPropertySource.
 */
public class TCFNodePropertySource implements IPropertySource {

    private final TCFNode fNode;
    private final Map<String, Object> fProperties = new HashMap<String, Object>();
    private IPropertyDescriptor[] fDescriptors;

    public TCFNodePropertySource(TCFNode node) {
        fNode = node;
    }

    public Object getEditableValue() {
        return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (fDescriptors == null) {
            try {
                fDescriptors = new TCFTask<IPropertyDescriptor[]>(fNode.getChannel()) {
                    final List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
                    public void run() {
                        if (fNode instanceof TCFNodeExecContext) {
                            getExecContextDescriptors((TCFNodeExecContext) fNode);
                        } else if (fNode instanceof TCFNodeStackFrame) {
                            getFrameDescriptors((TCFNodeStackFrame) fNode);
                        } else {
                            done(descriptors.toArray(new IPropertyDescriptor[descriptors.size()]));
                        }
                    }

                    private void getFrameDescriptors(TCFNodeStackFrame frameNode) {
                        TCFDataCache<IStackTrace.StackTraceContext> ctx_cache = frameNode.getStackTraceContext();
                        TCFDataCache<TCFSourceRef> line_info_cache = frameNode.getLineInfo();
                        if (!validateAll(ctx_cache, line_info_cache)) return;
                        IStackTrace.StackTraceContext ctx = ctx_cache.getData();
                        if (ctx != null) {
                            Map<String, Object> props = ctx.getProperties();
                            for (String key : props.keySet()) {
                                Object value = props.get(key);
                                if (value instanceof Number) {
                                    value = toHexAddrString((Number) value);
                                }
                                addDescriptor("Context", key, value);
                            }
                        }
                        TCFSourceRef sourceRef = line_info_cache.getData();
                        if (sourceRef != null) {
                            if (sourceRef.area != null) {
                                addDescriptor("Source", "Directory", sourceRef.area.directory);
                                addDescriptor("Source", "File", sourceRef.area.file);
                                addDescriptor("Source", "Line", sourceRef.area.start_line);
                            }
                            if (sourceRef.error != null) {
                                addDescriptor("Source", "Error", sourceRef.error);
                            }
                        }
                        done(descriptors.toArray(new IPropertyDescriptor[descriptors.size()]));
                    }
                    private void getExecContextDescriptors(TCFNodeExecContext exeNode) {
                        TCFDataCache<IRunControl.RunControlContext> ctx_cache = exeNode.getRunContext();
                        TCFDataCache<TCFContextState> state_cache = exeNode.getState();
                        TCFDataCache<MemoryRegion[]> mem_map_cache = exeNode.getMemoryMap();
                        if (!validateAll(ctx_cache, state_cache, mem_map_cache)) return;
                        IRunControl.RunControlContext ctx = ctx_cache.getData();
                        if (ctx != null) {
                            Map<String, Object> props = ctx.getProperties();
                            for (String key : props.keySet()) {
                                Object value = props.get(key);
                                if (value instanceof Number) {
                                    value = toHexAddrString((Number) value);
                                }
                                addDescriptor("Context", key, value);
                            }
                        }
                        TCFContextState state = state_cache.getData();
                        if (state != null) {
                            addDescriptor("State", "Suspended", state.is_suspended);
                            if (state.is_suspended) {
                                addDescriptor("State", "Suspend reason", state.suspend_reason);
                                addDescriptor("State", "PC", toHexAddrString(new BigInteger(state.suspend_pc)));
                            }
                            addDescriptor("State", "Active", !exeNode.isNotActive());
                        }
                        MemoryRegion[] mem_map = mem_map_cache.getData();
                        if (mem_map != null && mem_map.length > 0) {
                            int idx = 0;
                            for (MemoryRegion region : mem_map) {
                                Map<String, Object> props = region.region.getProperties();
                                for (String key : props.keySet()) {
                                    Object value = props.get(key);
                                    if (value instanceof Number) {
                                        value = toHexAddrString((Number) value);
                                    }
                                    addDescriptor("MemoryRegion["+(idx++)+']', key, value);
                                }
                            }
                        }
                        done(descriptors.toArray(new IPropertyDescriptor[descriptors.size()]));
                    }
                    private void addDescriptor(String category, String key, Object value) {
                        String id = category + '.' + key;
                        PropertyDescriptor desc = new PropertyDescriptor(id, key);
                        desc.setCategory(category);
                        descriptors.add(desc);
                        fProperties.put(id, value);
                    }
                    boolean validateAll(TCFDataCache<?> ... caches) {
                        TCFDataCache<?> pending = null;
                        for (TCFDataCache<?> cache : caches) {
                            if (!cache.validate()) {
                                pending = cache;
                            }
                        }
                        if (pending != null) {
                            pending.wait(this);
                            return false;
                        }
                        return true;
                    }
                }.get(5, TimeUnit.SECONDS);
            }
            catch (Exception e) {
                Activator.log("Error retrieving property data", e);
                fDescriptors = new IPropertyDescriptor[0];
            }
        }
        return fDescriptors;
    }

    public Object getPropertyValue(final Object id) {
        return fProperties.get(id);
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {
    }

    public void setPropertyValue(Object id, Object value) {
    }

    private static String toHexAddrString(Number num) {
        BigInteger n;
        if (num instanceof BigInteger) {
            n = (BigInteger) num;
        } else {
            n = BigInteger.valueOf(num.longValue());
        }
        String s = n.toString(16);
        int sz = s.length() > 8 ? 16 : 8;
        int l = sz - s.length();
        if (l < 0) l = 0;
        if (l > 16) l = 16;
        return "0x0000000000000000".substring(0, 2 + l) + s;
    }

}
