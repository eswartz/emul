/*
  ConsoleSpeechWriteArea.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory.mmio;



public class ConsoleSpeechWriteArea extends ConsoleMmioWriteArea {
    public ConsoleSpeechWriteArea(SpeechMmio mmio) {
        super(mmio, 4);
    }
}