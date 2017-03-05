/*
  EnhancedRamArea.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory;


/** Enhanced console RAM */
public class EnhancedRamArea extends ConsoleMemoryArea {

    public EnhancedRamArea(int latency, int size) {
    	super(latency);
    	
        memory = new short[size/2];
        read = memory;
        write = memory;
    }
}