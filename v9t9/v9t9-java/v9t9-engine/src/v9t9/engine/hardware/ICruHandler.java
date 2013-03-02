/*
  ICruHandler.java

  (c) 2005-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.hardware;
/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */


/**
 * Handle the behavior of the CRU.
 * 
 * @author ejs
 */
public interface ICruHandler {
    public void writeBits(int addr, int val, int num);
    public int readBits(int addr, int num);
}
