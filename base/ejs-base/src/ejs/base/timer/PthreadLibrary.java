/*
  PthreadLibrary.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.timer;


/**
 * @author ejs
 *
 */
public interface PthreadLibrary extends com.sun.jna.Library  {

	public static final java.lang.String JNA_LIBRARY_NAME = "pthread";
	public static final com.sun.jna.NativeLibrary JNA_NATIVE_LIB = com.sun.jna.NativeLibrary.getInstance(JNA_LIBRARY_NAME);
	public static final PthreadLibrary INSTANCE = (PthreadLibrary)com.sun.jna.Native.loadLibrary(JNA_LIBRARY_NAME, PthreadLibrary.class);

	int pthread_self();
}
