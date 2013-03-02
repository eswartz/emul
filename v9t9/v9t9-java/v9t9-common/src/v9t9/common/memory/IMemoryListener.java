/*
  IMemoryListener.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.memory;

public interface IMemoryListener {
	/** The mapping of addresses changed, e.g., due to banking */
    void logicalMemoryMapChanged(IMemoryEntry entry);
    /** The mapping of entries changed */
    void physicalMemoryMapChanged(IMemoryEntry entry);
}