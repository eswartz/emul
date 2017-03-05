/*
  EnhancedRamByteArea.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory;

import v9t9.engine.memory.ByteMemoryArea;


/** Enhanced console RAM, byte-accessible */
public class EnhancedRamByteArea extends ByteMemoryArea {

    public EnhancedRamByteArea(int latency, int size) {
    	super(latency);
    	
        memory = new byte[size];
        read = memory;
        write = memory;
    }
}