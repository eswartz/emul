/*
  AbortedException.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

/**
 * This exception is thrown from within an interpreter
 * or compiled code to indicate the current stream of
 * execution must be restarted somehow -- either due to
 * interrupts changing, due to changes in the memory map,
 * etc. 
 * @author ejs
 *
 */
public class AbortedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AbortedException() {}
}