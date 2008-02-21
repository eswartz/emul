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
import java.util.Iterator;
import java.util.Set;

public class ReadOnlyCollection<E> implements Set<E> {
    
    private final Collection<E> base;
    
    public ReadOnlyCollection(Collection<E> base) {
        this.base = base;
    }

    private void error() {
        throw new Error("Read only Collection");
    }

    public boolean add(E e) {
        error();
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        error();
        return false;
    }

    public void clear() {
        error();
    }

    public boolean contains(Object o) {
        return base.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return base.containsAll(c);
    }

    public boolean isEmpty() {
        return base.isEmpty();
    }

    public Iterator<E> iterator() {
        final Iterator<E> iterator = base.iterator();
        return new Iterator<E>() {
            
            public boolean hasNext() {
                return iterator.hasNext();
            }

            public E next() {
                return iterator.next();
            }

            public void remove() {
                error();
            }
        };
    }

    public boolean remove(Object o) {
        error();
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        error();
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        error();
        return false;
    }

    public int size() {
        return base.size();
    }

    public Object[] toArray() {
        return base.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return base.toArray(a);
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
