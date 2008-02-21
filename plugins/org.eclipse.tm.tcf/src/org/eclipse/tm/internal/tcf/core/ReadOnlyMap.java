/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ReadOnlyMap<K,V> implements Map<K,V> {
    
    private final Map<K,V> base;
    private Set<K> key_set;
    private Set<Map.Entry<K, V>> entry_set;
    private Collection<V> values;
    
    public ReadOnlyMap(Map<K,V> base) {
        this.base = base;
    }
    
    private void error() {
        throw new Error("Read only Map");
    }

    public void clear() {
        error();
    }

    public boolean containsKey(Object key) {
        return base.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return base.containsValue(value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        if (entry_set == null) entry_set = new ReadOnlyCollection<Map.Entry<K, V>>(base.entrySet());
        return entry_set;
    }

    public V get(Object key) {
        return base.get(key);
    }

    public boolean isEmpty() {
        return base.isEmpty();
    }

    public Set<K> keySet() {
        if (key_set == null) key_set = new ReadOnlyCollection<K>(base.keySet());
        return key_set;
    }

    public V put(K key, V value) {
        error();
        return null;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        error();
    }

    public V remove(Object key) {
        error();
        return null;
    }

    public int size() {
        return base.size();
    }

    public Collection<V> values() {
        if (values == null) values = new ReadOnlyCollection<V>(base.values());
        return values;
    }

    public boolean equals(Object o) {
        return base.equals(o);
    }

    public int hashCode() {
        return base.hashCode();
    }
    
    public String toString() {
        return base.toString();
    }
}
