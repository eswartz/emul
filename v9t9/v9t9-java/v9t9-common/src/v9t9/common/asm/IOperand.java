/*
  IOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;



/**
 * @author ejs
 */
public interface IOperand {
    // Operand changes
    public static final int OP_DEST_FALSE = 0;
    public static final int OP_DEST_TRUE = 1;
    public static final int OP_DEST_KILLED = 2;

}
